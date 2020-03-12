<!--
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
-->

# Taverna Command-line Tool

## Taverna Project Retired

> tl;dr: The Taverna code base is **no longer maintained** 
> and is provided here for archival purposes.

From 2014 till 2020 this code base was maintained by the 
[Apache Incubator](https://incubator.apache.org/) project _Apache Taverna (incubating)_
(see [web archive](http://web.archive.org/web/20200312133332/https://taverna.incubator.apache.org/)
and [podling status](https://incubator.apache.org/projects/taverna.html)).

In 2020 the Taverna community 
[voted](https://lists.apache.org/thread.html/r559e0dd047103414fbf48a6ce1bac2e17e67504c546300f2751c067c%40%3Cdev.taverna.apache.org%3E)
to **retire** Taverna as a project and withdraw the code base from the Apache Software Foundation. 

This code base remains available under the Apache License 2.0 
(see _License_ below), but is now simply called 
_Taverna_ rather than ~~Apache Taverna (incubating)~~.

While the code base is no longer actively maintained, 
Pull Requests are welcome to the 
[GitHub organization taverna](http://github.com/taverna/), 
which may infrequently be considered by remaining 
volunteer caretakers.


### Previous releases

Releases 2015-2018 during incubation at Apache Software Foundation
are available from the ASF Download Archive <http://archive.apache.org/dist/incubator/taverna/>

Releases 2014 from the University of Manchester are on BitBucket <https://bitbucket.org/taverna/>

Releases 2009-2013 from myGrid are on LaunchPad <https://launchpad.net/taverna/>

Releases 2003-2009 are on SourceForge <https://sourceforge.net/projects/taverna/files/taverna/>

Binary JARs for Taverna are available from 
Maven Central <https://repo.maven.apache.org/maven2/org/apache/taverna/>
or the myGrid Maven repository <http://repository.mygrid.org.uk/>



## About Taverna Command-line Tool

Taverna Command-line Tool provides a shell command
for executing
[Taverna](https://taverna.incubator.apache.org/) workflows,
defined using either the [Taverna Language](https://taverna.incubator.apache.org/download/language/)
API in the
[SCUFL2](https://taverna.incubator.apache.org/documentation/scufl2/)
`.wfbundle` format, or in the `.t2flow` format from
[Taverna Workbench 2.5](https://taverna.incubator.apache.org/download/workbench/).

Workflow inputs can be provided as parameters or files,
while outputs can be saved either to a folder or a
[Research Object bundle](https://w3id.org/bundle)
including detailed provenance, which can be inspected
using Taverna Language's
[DataBundle](https://taverna.incubator.apache.org/javadoc/taverna-language/org/apache/taverna/databundle/DataBundles.html)
support.

In addition to the
[Taverna Common Activities](https://taverna.incubator.apache.org/download/common-activities/),
the Command-line supports plugins using
[Taverna OSGi services](https://taverna.incubator.apache.org/download/osgi/).


Note that, except for command-line handling, this
module relies on other
[Taverna components](https://taverna.incubator.apache.org/code) for the actual workflow execution.


## License

* (c) 2007-2014 University of Manchester
* (c) 2014-2020 Apache Software Foundation

This product includes software developed at The
[Apache Software Foundation](http://www.apache.org/).

Licensed under the
[Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0),
see the file [LICENSE](LICENSE) for details.

The file [NOTICE](NOTICE) contains any additional attributions and
details about embedded third-party libraries and source code.


# Contribute

<!--
Please subscribe to and contact the
[dev@taverna](http://taverna.incubator.apache.org/community/lists#dev) mailing list
mailing list for any questions, suggestions and discussions about
Taverna.

Bugs and planned features are tracked in the Jira
[issue tracker](https://issues.apache.org/jira/browse/TAVERNA/component/12326812)
under the `TAVERNA` component _Taverna Command-line Tool._ Feel free
to [add an issue](https://taverna.incubator.apache.org/community/issue-tracker)!

To suggest changes to this source code, feel free to raise a
[GitHub pull request](https://github.com/apache/incubator-taverna-commandline/pulls).
-->

Taverna Command-line Tool product relies on
[other Taverna components](http://web.archive.org/web/20200312133332/https://taverna.incubator.apache.org/code/)
which have their own source code repositories.

Any contributions received are assumed to be covered by the [Apache License
2.0](https://www.apache.org/licenses/LICENSE-2.0). 


## Prerequisites

* Java 1.8 or newer (tested with OpenJDK 1.8)
* [Apache Maven](https://maven.apache.org/download.html) 3.2.5 or newer (older
  versions probably also work)


This code relies on other
[Taverna modules](http://web.archive.org/web/20200312133332/https://taverna.incubator.apache.org/download/code/),
which Maven should download
automatically from
[Apache's Maven repository](http://web.archive.org/web/20200312133332/https://taverna.incubator.apache.org/download/maven/);
however you might want to compile these yourself in the order below:

* [taverna-language](http://web.archive.org/web/20200312133332/https://taverna.incubator.apache.org/download/language/)
* [taverna-osgi](http://web.archive.org/web/20200312133332/https://taverna.incubator.apache.org/download/osgi/)
* [taverna-engine](http://web.archive.org/web/20200312133332/https://taverna.incubator.apache.org/download/engine/)
* [taverna-common-activities](http://web.archive.org/web/20200312133332/https://taverna.incubator.apache.org/download/common-activities/)

Please see the `<properties>` of this [pom.xml](pom.xml) to find the
correct versions to build.

# Building

To build, use

    mvn clean install

This will build each module and run its tests.

Note that this repository relies on
other [Taverna modules](http://web.archive.org/web/20200312133332/https://taverna.incubator.apache.org/code)
which will be downloaded from Maven repositories if they are not
already present in the equivalent of your `~/.m2/repository` in the
correct version.

## Building on Windows

If you are building on Windows, ensure you unpack this source code
to a folder with a [short path name](http://stackoverflow.com/questions/1880321/why-does-the-260-character-path-length-limit-exist-in-windows)
lenght, e.g. `C:\src` - as
Windows has a [limitation on the total path length](https://msdn.microsoft.com/en-us/library/aa365247%28VS.85%29.aspx#maxpath)
which might otherwise
prevent this code from building successfully.

## Skipping tests

To skip the tests (these can be time-consuming), use:

    mvn clean install -DskipTests


If you are modifying this source code independent of the
Taverna project, you may not want to run the
[Rat Maven plugin](https://creadur.apache.org/rat/apache-rat-plugin/)
that enforces Apache headers in every source file - to disable it, try:

    mvn clean install -Drat.skip=true


## SNAPSHOT dependencies

If you are building a non-released version of this repository,
(e.g.  the `pom.xml` declares a `-SNAPSHOT` version), then Maven might
download unreleased
[snapshot builds](http://web.archive.org/web/20200312133332/https://taverna.incubator.apache.org/code/#snapshot-builds)
for other `-SNAPSHOT` Taverna dependencies.


If you are developing one of the
[Taverna modules](http://web.archive.org/web/20200312133332/http://taverna.incubator.apache.org/code)
and want to test it with the
Taverna Command-line Tool, make sure you build it
locally first with `mvn clean install` to avoid downloading it from
the snapshot repository.

Then check that the the `<properties>` section of the `pom.xml`
matches the `<version>` of the module you are developing.
See also `taverna-commandline-product/pom.xml`
to hard-code versions of other dependencies.

The default SNAPSHOT update policy for `mvn` is _daily_ -
you can modify this behaviour with
`--update-snapshots` or `--no-snapshot-updates`


## Binary distribution

To build a binary distribution ZIP file that
includes third-party dependencies as JAR files,
build with the `-Prelease` option, which would make
`taverna-commandline-product/target/apache-taverna-commandline-3.1.0-incubating-release.zip`
or equivalent.

After building, see the file `target/maven-shared-archive-resources/META-INF/DEPENDENCIES` for
details of the licenses of the third-party dependencies. All dependencies should
be [compatible with Apache License 2.0](http://www.apache.org/legal/resolved.html).


# Running

After [building](#building), see the `taverna-commandline-product/target`
directory. Inside you should find a folder like
`apache-taverna-commandline-3.1.0-incubating/`
which contains the built Taverna Command-line Tool product.

If you prefer a ZIP file, then instead build with
the Maven `-Prelease` option. You can then unzip at a location of
your choice.

If you are running on Windows you may need to
put Taverna Command-line Tool in a folder high in the
disk hierarchy (e.g. `C:\Taverna`), this helps
avoid problems with Windows path-length restrictions.


Running `executeworkflow.sh` (or `executeworkflow.bat`) without arguments
will show the help:

```
$ ./executeworkflow.sh
usage: executeworkflow [options] [workflow]
-bundle <bundle>                        Save outputs to a new Workflow
                                     Run Bundle (zip).
-clientserver                           Connect as a client to a derby
                                     server instance.
-cmdir <directory path>                 Absolute path to a directory
                                     where Credential Manager's files
                                     (keystore and truststore) are
                                     located.
...
```

The folder `examples` contains a Hello World type example workflow in
[SCUFL2 format](http://taverna.incubator.apache.org/documentation/scufl2/).

```
$ ./executeworkflow.sh examples/helloworld.wfbundle
Outputs will be saved to the directory: /home/johndoe/apache-taverna-commandline-3.1.0/Hello_World_output
Workflow completed.

$ cat Hello_World_output/greeting ; echo
Hello, World!
```

On Windows:

```
C:\home\apache-taverna-commandline-3.1.0>executeworkflow.bat examples\helloworld.wfbundle
Outputs will be saved to the directory: C:\home\apache-taverna-commandline-3.1.0\Hello_World_output
Workflow completed.

C:\home\apache-taverna-commandline-3.1.0>type Hello_World_output\greeting
Hello, World!
```

# Export restrictions

This distribution includes cryptographic software.
The country in which you currently reside may have restrictions
on the import, possession, use, and/or re-export to another country,
of encryption software. BEFORE using any encryption software,
please check your country's laws, regulations and policies
concerning the import, possession, or use, and re-export of
encryption software, to see if this is permitted.
See <http://www.wassenaar.org/> for more information.

The U.S. Government Department of Commerce, Bureau of Industry and Security (BIS),
has classified this software as Export Commodity Control Number (ECCN) 5D002.C.1,
which includes information security software using or performing
cryptographic functions with asymmetric algorithms.
The form and manner of this Apache Software Foundation distribution makes
it eligible for export under the License Exception
ENC Technology Software Unrestricted (TSU) exception
(see the BIS Export Administration Regulations, Section 740.13)
for both object code and source code.

The following provides more details on the included cryptographic software:

* Taverna Command-line Tool depends on and interacts with the
  [Taverna Engine](http://web.archive.org/web/20200312133332/https://taverna.incubator.apache.org/download/engine/),
  credential manager.
* After building, the [taverna-commandline-product](taverna-commandline-product)
  archive `lib` folder includes
  [BouncyCastle](https://www.bouncycastle.org/) bcprov encryption library,
  [Apache HttpComponents](https://hc.apache.org/) Core and Client,
  [Apache Derby](http://db.apache.org/derby/),
  [Jetty](http://www.eclipse.org/jetty/),
  [Apache WSS4J](https://ws.apache.org/wss4j/),
  [Apache XML Security for Java](https://santuario.apache.org/javaindex.html),
  [Open SAML Java](https://shibboleth.net/products/opensaml-java.html),
  [Taverna Language](http://web.archive.org/web/20200312133332/https://taverna.incubator.apache.org/download/language/),
  [Taverna OSGi](http://web.archive.org/web/20200312133332/https://taverna.incubator.apache.org/download/osgi/),
  [Taverna Engine](http://web.archive.org/web/20200312133332/https://taverna.incubator.apache.org/download/engine/),
  and
  [Taverna Common Activities](http://web.archive.org/web/20200312133332/https://taverna.incubator.apache.org/download/common-activities/).
