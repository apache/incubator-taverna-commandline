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

# Apache Taverna Commandline

Launcher for running
[Apache Taverna](http://taverna.incubator.apache.org/) workflows.

Note that, except for command line parsing, this module relies on other
[Apache Taverna modules](http://taverna.incubator.apache.org/code) for
the actual workflow execution.


## License

* (c) 2007-2014 University of Manchester
* (c) 2014-2016 Apache Software Foundation

This product includes software developed at The
[Apache Software Foundation](http://www.apache.org/).

Licensed under the
[Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0),
see the file [LICENSE](LICENSE) for details.

The file [NOTICE](NOTICE) contains any additional attributions and
details about embedded third-party libraries and source code.


# Contribute

Please subscribe to and contact the
[dev@taverna](http://taverna.incubator.apache.org/community/lists#dev mailing list)
mailing list for any questions, suggestions and discussions about
Apache Taverna.

Bugs and planned features are tracked in the Jira
[issue tracker](https://issues.apache.org/jira/browse/TAVERNA/component/12326812)
under the `TAVERNA` component _Taverna Commandline._ Feel free
to [add an issue](https://taverna.incubator.apache.org/community/issue-tracker)!

To suggest changes to this source code, feel free to raise a
[GitHub pull request](https://github.com/apache/incubator-taverna-commandline/pulls).

Apache Taverna Command Line product relies on
[other Taverna components](https://taverna.incubator.apache.org/code/)
which have their own source code repositories.

Any contributions received are assumed to be covered by the [Apache License
2.0](https://www.apache.org/licenses/LICENSE-2.0). We might ask you
to sign a [Contributor License Agreement](https://www.apache.org/licenses/#clas)
before accepting a larger contribution.

## Disclaimer

Apache Taverna is an effort undergoing incubation at the
[Apache Software Foundation (ASF)](http://www.apache.org/),
sponsored by the [Apache Incubator PMC](http://incubator.apache.org/).

[Incubation](http://incubator.apache.org/incubation/Process_Description.html)
is required of all newly accepted projects until a further review
indicates that the infrastructure, communications, and decision-making process
have stabilized in a manner consistent with other successful ASF projects.

While incubation status is not necessarily a reflection of the completeness
or stability of the code, it does indicate that the project has yet to be
fully endorsed by the ASF.



## Prerequisites

* Java 1.8 or newer (tested with OpenJDK 1.8)
* [Apache Maven](https://maven.apache.org/download.html) 3.2.5 or newer (older
  versions probably also work)


# Building

To build, use

    mvn clean install

This will build each module and run their tests.

Note that this repository relies on
other [Apache Taverna modules](https://taverna.incubator.apache.org/code)
which will be downloaded from Maven repositories if they are not
already present in the equivalent of your `~/.m2/repository` in the
correct version.


## Skipping tests

To skip the tests (these can be time-consuming), use:

    mvn clean install -DskipTests


If you are modifying this source code independent of the
Apache Taverna project, you may not want to run the
[Rat Maven plugin](https://creadur.apache.org/rat/apache-rat-plugin/)
that enforces Apache headers in every source file - to disable it, try:

    mvn clean install -Drat.skip=true


## SNAPSHOT dependencies

If you are building a non-released version of this repository,
(e.g.  the `pom.xml` declares a `-SNAPSHOT` version), then Maven might
download unreleased
[snapshot builds](https://taverna.incubator.apache.org/code/#snapshot-builds)
for other `-SNAPSHOT` Taverna dependencies.


If you are developing one of the
[Apache Taverna modules](http://taverna.incubator.apache.org/code)
and want to test it with the Command Line, make sure you build it
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
which contain the built Apache Taverna Command Line product.

If you prefer a ZIP file, then instead build with
the Maven `-Prelease` option. You can then unzip at a location of
your choice.

If you are running on Windows you may need to
put Taverna Command Line in a folder high in the
disk hierarchy (e.g. `C:\Taverna`), this helps
avoid problems with Windows path length restrictions.


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

The folder `examples` contain a Hello World type example workflow in
[SCUFL2 format](http://taverna.incubator.apache.org/documentation/scufl2/).

```
$ ./executeworkflow.sh examples/helloworld.wfbundle
Outputs will be saved to the directory: /home/johndoe/apache-taverna-commandline-3.1.0/Hello_World_output
Workflow completed.

$ cat Hello_World_output/greeting ; echo
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

* Apache Taverna Command Line depends on and interacts with the
  [Apache Taverna Engine](http://taverna.incubator.apache.org/download/engine/),
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
  [Apache Taverna Language](http://taverna.incubator.apache.org/download/language/),
  [Apache Taverna OSGi](http://taverna.incubator.apache.org/download/osgi/),
  [Apache Taverna Engine](http://taverna.incubator.apache.org/download/engine/),
  and
  [Apache Taverna Common Activities](http://taverna.incubator.apache.org/download/common-activities/).
