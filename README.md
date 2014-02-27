Taverna command line tool
==========================

_[Taverna](http://www.taverna.org.uk/) Command Line product build and installers_

Released by [myGrid](http://www.mygrid.org.uk/)

(c) Copyright 2005-2014 University of Manchester, UK


Introduction
------------

This repository is a Maven source code trees that do not themselves contain 
any code, but which are used to assemble the distribution, add launchers and
configurations files, and package this as installers. 

You can think of a product as the last build step before having a 
downloadable/executable software distribution. Each product includes their 
required code from the common 
[Taverna source code](http://dev.mygrid.org.uk/wiki/display/developer/Taverna+source+code), 
by retrieved the compiled JARs from the 
[myGrid Maven repositories](http://dev.mygrid.org.uk/wiki/display/developer/Maven+repository) 
unless they have already been built locally with Maven.

This is the build product for the standalone 
[Taverna Command Line tool](http://www.taverna.org.uk/download/command-line-tool/). 

In most cases you do not need to compile Taverna, instead you can 
download the official distribution for your operating system from the
[Taverna download pages](http://www.taverna.org.uk/download/).

For usage of this product, see the 
[Command Line Tool documentation](http://dev.mygrid.org.uk/wiki/display/taverna/Command+Line+Tool).



Editions
--------
This repository have several "edition" branches, one for each domain-specific builds. 
Each domain-specific build extends the core branch by modifying the plugin set 
(and ensuring the repository folder is populated for those plugins), but may also 
modify configurations such as the default service sets.

Branches:

  *  `core` - for any domain - general activities (WSDL, REST, Beanshell, Component, Interaction, etc) - also basis for the other branches (can be considered the 'master' or 'trunk' branch)
  *  `bioinformatics` - formerly maintenance, adds BioMart, BioMoby, Soaplab, WebDAV, corresponding default services and [BioCatalogue](https://www.biocatalogue.org/)
  *  `digitalpreservation` - core + WebDAV
  *  `biodiversity` - core + WebDAV + [BioDiversityCatalogue](https://www.biodiversitycatalogue.org/)
  *  `astronomy` - core + [AstroTaverna](http://wf4ever.github.io/astrotaverna/)
  *  `enterprise` - core with all possible plugins


Licence
=======
Taverna is licenced under the GNU Lesser General Public Licence. (LGPL) 2.1.
See the file LICENCE.txt or http://www.gnu.org/licenses/lgpl-2.1.html for
details.

If the source code was not included in this download, you can download it from
http://www.taverna.org.uk/download/workbench/2-5/#download-source or
http://www.taverna.org.uk/download/source-code/

If you installed a OS-specific distribution of Taverna it may come
bundled with a distribution of OpenJDK. OpenJDK is distributed under the
GNU Public License (GPL) 2.0 w/Classpath exception. See jre/LICENSE.txt or
http://hg.openjdk.java.net/jdk7u/jdk7u/raw-file/da55264ff2fb/LICENSE
for details.

Taverna uses various third-party libraries that are included under compatible
open source licences such as the Apache Licence.

  
  
Building
--------
First, make sure you have checked out the edition you want to build. 

Build requirements:
 * Java [JDK 7](http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html) or OpenJDK 7
 * [Apache Maven 3.x](http://maven.apache.org/download.cgi)
 * [Install4j](http://www.ej-technologies.com/products/install4j/overview.html) (optional)


The below will assume you want to build the `core` edition. 

    git fetch --all          # fetch all branches/editions
    git checkout core        # replace 'core' with your edition
    mvn clean install
    
The packaging will take several minutes. The first time, this might take up to an
hour as several libraries are downloaded from Maven repositories.

    [INFO] ------------------------------------------------------------------------
    [INFO] BUILD SUCCESS
    [INFO] ------------------------------------------------------------------------
    [INFO] Total time: 5:39.145s
    [INFO] Finished at: Thu Feb 27 01:06:08 GMT 2014
    [INFO] Final Memory: 47M/1200M
    [INFO] ------------------------------------------------------------------------


After a successful build, the file 
`target/taverna-commandline-core-2.5-SNAPSHOT.zip` (or equivalent) contains
the platform-independent distribution of the Taverna Command Line Tool. 



Installers
----------

If you would like to build the platform-specific installers, you will need
an installation of 
[Install4j](http://www.ej-technologies.com/products/install4j/overview.html). 

First configure Maven to find your Install4j installation.

    stain@biggie:~$ cat .m2/settings.xml 
    <settings>
      	<profiles>
      	    <profile>
      		<id>dist</id>
        		<properties>
        		    <install4j.home>/opt/install4j5</install4j.home>
    		    </properties>
  	        </profile>
  	    </profiles>
      </settings>

Start `install4j` to install your license key or run a 30-day trial, then quit the user interface.

To generate the installers, run:

    mvn clean install -Pdist

Note that on first execution, this will also download [OpenJDK binaries](http://build.mygrid.org.uk/openjdk/) 
for embedding in OS-specifc installers. OpenJDK is licensed as GPL 2.1 with Classpath exception.

The `media/media` folder will contain installers for different operating systems.

    stain@biggie:~/src/net.sf.taverna.t2.taverna-commandline$ ls target/media/
    md5sums
    output.txt
    taverna-commandline-core-2.5-SNAPSHOT-linux_amd64.deb
    taverna-commandline-core-2.5-SNAPSHOT-linux_x86_64.rpm
    taverna-commandline-core-2.5-SNAPSHOT-macos.tgz
    taverna-commandline-core-2.5-SNAPSHOT-unix.tar.gz
    taverna-commandline-core-2.5-SNAPSHOT-windows-x64.exe
    taverna-commandline-core-2.5-SNAPSHOT-windows-x86.exe
    taverna-commandline-core-2.5-SNAPSHOT.zip
    updates.xml


Support
=======
See http://www.taverna.org.uk/about/contact-us/ for contact details.

You may email support@mygrid.org.uk for any questions on using Taverna
workbench. myGrid's support team should respond to your query within a
week.

