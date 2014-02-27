#!/usr/bin/env python
"""
Produces a set of <artifactItem> statements for the maven-dependency-plugin section in 
the workbench-distro/pom.xml

By Stian Soiland-Reyes, 2009-11-17

Produced by first doing a nightly build (-Pnightly) but with the plugin
maven-dependency-plugin commented out. When unzipping taverna-XXXX-bin.zip the 
repository/ folder will be missing these artifacts below (mainly due to Raven 
not merging version differences). To find out which ones, start run.sh once, 
and then in $HOME/.taverna-123123123/repository/ run (in *NIX or Cygwin):



stain@ralph ~/AppData/Roaming/taverna-2.1-beta-3-SNAPSHOT-20091117/repository
$ find -type f > $HOME/files

stain@ralph ~/AppData/Roaming/taverna-2.1-beta-3-SNAPSHOT-20091117/repository
$ cd

stain@ralph ~
$ python generateArtifactItems.py > artifactItems.txt

The content of artifactItems.txt can then be pasted into the <artifactItems> element of the
maven-dependency-plugin in the pom.xml.
		
If you then do a new nightly build with the maven-dependency-plugin section enabled, and
delete your $HOME/.taverna-123123123/repository/ - then after running Taverna the 
$HOME/.taverna-123123123/repository/ should be empty.

If on unzipping you get warnings about overwriting files already existing you might also
need to redo this procedure, as this means that an artifactItem is adding a file already
added by the repositories-component of the assembly plugin. 
 

"""

import string
import re

xmlTemplate = string.Template(
"""                                <artifactItem>
                                    <groupId>${groupID}</groupId>
                                    <artifactId>${artifactID}</artifactId>
                                    <version>${version}</version>
                                    <type>${type}</type>
                                    <outputDirectory>${project.build.directory}/repository/${groupIDPath}/${artifactID}/${version}</outputDirectory>
                                </artifactItem>""")

pathPattern = re.compile(r"^\./(.+)/([^/]+)/([^/]+)/([^/]+)\.([a-zA-Z]+)$")


files = open("files")

for file in files:
    matches = pathPattern.match(file) 
    #print matches.groups()
    groupIDPath = matches.group(1)
    groupID = groupIDPath.replace("/", ".")
    artifactID = matches.group(2)
    version = matches.group(3)
    type = matches.group(5)
    print xmlTemplate.safe_substitute(locals())

