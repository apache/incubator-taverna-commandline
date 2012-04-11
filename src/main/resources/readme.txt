===============================
Taverna command line tool 2.4.0
===============================
http://www.taverna.org.uk/
http://www.mygrid.org.uk/

Released by myGrid, 2011-07-14
(c) Copyright 2005-2011 University of Manchester, UK


Licence
=======
Taverna is licenced under the GNU Lesser General Public Licence. (LGPL) 2.1.
See the file LICENCE.txt or http://www.gnu.org/licenses/lgpl-2.1.html for
details.

If the source code was not included in this download, you can download it from
http://www.taverna.org.uk/download/workbench/2-3/#download-source or 
http://www.taverna.org.uk/download/source-code/

Taverna uses various third-party libraries that are included under compatible
open source licences such as the Apache Licence.


Documentation
=============
See http://www.taverna.org.uk/documentation/taverna-2-x/ for
documentation and tutorials on using Taverna.

In particular
http://www.taverna.org.uk/documentation/taverna-2-x/command-line-tool/
and http://www.mygrid.org.uk/dev/wiki/display/taverna23/Command+Line+Tool
will explain how to use the command line tool.

See http://www.mygrid.org.uk/dev/wiki/display/taverna/User+Manual for the
Taverna 2.4 user manual.

See the file "known-issues.txt" for known issues with this release, and the file
"release-notes.txt" for improvements since the previous version of Taverna.


Usage
=====
In Windows, execute "executeworkflow.bat", while on OSX/Linux/UNIX,
execute "sh executeworkflow.sh".

On Linux/OSX/UNIX you may set the executable bit using 
"chmod 755 executeworkflow.sh", allowing you to execute ./executeworkflow.sh
directly. You can make symlinks to this shell script from /usr/local/bin or
equivalent. 

Run executeworkflow with parameter -help for detailed help, also see
http://www.taverna.org.uk/documentation/taverna-2-x/command-line-tool/



usage: executeworkflow [options] [workflow]
 -clientserver                           connects as a client to a derby
                                         server instance.
 -dbproperties <filename>                loads a properties file to
                                         configure the database
 -embedded                               connects to an embedded Derby
                                         database. This can prevent
                                         mulitple invocations
 -help                                   displays comprehensive help
                                         information
 -inmemory                               runs the workflow with data
                                         stored in-memory rather than in a
                                         database. This can give
                                         performance inprovements, at the
                                         cost of overall memory usage
 -inputdelimiter <inputname delimiter>   causes an inputvalue or inputfile
                                         to be split into a list according
                                         to the delimiter. The associated
                                         workflow input must be expected
                                         to receive a list
 -inputdoc <document>                    load inputs from a Baclava
                                         document
 -inputfile <inputname filename>         load the named input from file or
                                         URL
 -inputvalue <inputname value>           directly use the value for the
                                         named input
 -logfile <filename>                     the logfile to which more verbose
                                         logging will be written to
 -outputdir <directory>                  save outputs as files in
                                         directory, default is to make a
                                         new directory workflowName_output
 -outputdoc <document>                   save outputs to a new Baclava
                                         document
 -port <portnumber>                      the port that the database is
                                         running on. If set requested to
                                         start its own internal server,
                                         this is the start port that will
                                         be used.
 -provenance                             generates provenance information
                                         and stores it in the database.
 -startdb                                automatically starts an internal
                                         Derby database server.

For example:

$ taverna-commandline-2.4.0/executeworkflow.sh Retrieve_sequence_in_EMBL_format.t2flow
Outputs will be saved to the directory:
/home/stain/Desktop/Retrieve_sequence_in_EMBL_format_output

$ cat Retrieve_sequence_in_EMBL_format_output/sequence 
ID   X52524; SV 1; linear; genomic DNA; STD; INV; 4507 BP.
XX
AC   X52524;
XX
DT   20-SEP-1990 (Rel. 25, Created)
DT   18-FEB-1991 (Rel. 27, Last updated, Version 6)
..
     aggttagaaa aaataaataa aaataaaatt gagaagaatg taaattaaat atagaattcg      4500
     agctcgg                                                                4507
//


You can use full URLs to workflows and inputs, for instance: 
http://www.myexperiment.org/workflows/1004/download/retrieve_sequence_in_embl_format_873401.t2flow

   
Examples
========
Example Taverna 2.4 workflows can be found in the myExperiment starter pack at
http://www.myexperiment.org/packs/254

You can share and find other workflows at http://www.myexperiment.org/


Support
=======
See http://www.taverna.org.uk/about/contact-us/ for contact details.

You may email support@mygrid.org.uk for any questions on using Taverna
workbench. myGrid's support team should respond to your query within a 
week.


Mailing lists
-------------

We also encourage you to sign up to the public *taverna-users* mailing list,
where you may post about any problem or give us feedback on using Taverna.
myGrid developers are actively monitoring the list.

 * http://lists.sourceforge.net/lists/listinfo/taverna-users
 * http://taverna-users.markmail.org/search/?q=


If you are a developer, writing plugins for Taverna, dealing with the code
behind Taverna or integrating Taverna with other software, you might find it
interesting to also sign up for the public *taverna-hackers* mailing list,
where you can also track the latest developments of Taverna.

  * http://lists.sourceforge.net/lists/listinfo/taverna-hackers
  * http://taverna-hackers.markmail.org/search/?q=


Requirements
============

Java
-----
Taverna requires the Java Runtime Environment (JRE) version 6 or later from
Oracle.  No other versions of Java are officially tested with Taverna. 

Windows users might need to download Java from http://java.com/ 

Linux users have different options to install Java depending on their Linux
distribution. Some distributions, such as Ubuntu, might come with alternative
open source implementations of Java, like Gnu GCJ and OpenJDK. We've identified
some issues with these implementations, and recommend using the official Java
implementation from Sun/Oracle. 

To download Oracle Java 6 for Ubuntu, start a Terminal, and type the following:
  sudo apt-get install sun-java6-jre

and follow the instructions. You might also need to change the default Java
implementation by running:
  sudo update-alternatives --config java

To check your version of Java on the command line, try:

  $ java -version
  java version "1.6.0_22"
  Java(TM) SE Runtime Environment (build 1.6.0_22-b04)
  Java HotSpot(TM) 64-Bit Server VM (build 17.1-b03, mixed mode)


Read http://www.taverna.org.uk/download/workbench/system-requirements/
for more requirement details.


Secure web services
-------------------
If you need to invoke secure services or access secured data from your
workflows (e.g. if you need to provide username and password to gain access to
your service/data or your service's URL starts with HTTPS), then you need to
allow Taverna to communicate securely to such resources using the strongest
possible cryptography. 

To do so, you have to install the 'Unlimited Strength Java Cryptography
Extension' policy instead of the default restrictive policy that is
shipped with Java; the default policy will for export reasons only
allows the use of "weak" cryptography (e.g. short passwords and keys).

Java by default only comes with limited-strength cryptography support
because of import control restrictions in some countries, where
unlimited-strength cryptography is classified as a "weapon technology".
Thus, you must determine whether your country's laws allow you to
install such software and you are responsible for doing so.

The policy files on your system are located in:

   <java-home>/lib/security/local_policy.jar
   <java-home>/lib/security/US_export_policy.jar

where <java-home> is the jre directory of the Java Development Kit (JDK) or the
top-level directory of the JRE (Java Runtime Environment) on your system.

If eligible, you may download the unlimited strength versions of the above
policy files and replace the ones that were installed by default.  This will
effectively "turn on" the use of unlimited-strength security in Java on your
system. Not installing these files will most probably cause invoking of secure
services from Taverna to fail. Note that if you switch to another Java version
you will have to install the policy files again in the appropriate directory of
your new Java installation.

For Java 6, you can download the unlimited cryptography policy jar files from:
http://www.oracle.com/technetwork/java/javase/downloads/index.html

Advanced
========

Logging
-------

You will find detailed logs in Taverna's home directory. You can 
view this directory from within Taverna by choosing 
Advanced->Show Log Folder in the menu.

See:

    Windows XP: C:\Documents and settings\JohnDoe\
                   Application Data\taverna-cmd-2.4.0\logs

    Vista/Windows 7: C:\Users\JohnDoe\AppData\Roaming\taverna-cmd-2.4.0\logs

    OS X: /Users/JohnDoe/Library/Application support/taverna-cmd-2.4.0/logs

    Linux: /home/johndoe/.taverna-cmd-2.4.0/logs

Note that "Application data" and "AppData" are hidden folders in Windows.

To modify the log levels, edit "conf/log4j.properties" in the Taverna
installation folder.


Memory usage 
------------
The Taverna command line tool will by default use a maximum of 
300 MB. 

If Taverna happens to run out of memory while running a workflow with
large data by using the option -embedded This will reduce execution speed
slightly, but should consume less memory.

If you need to increase the available memory, edit "executeworkflow.sh" or
"executeworkflow.bat" and replace "-Xmx300m" with say "-Xmx600m" to use
600 MiB.

