=======================
Taverna workbench 2.2.0
=======================
http://www.taverna.org.uk/
http://www.mygrid.org.uk/

Released by myGrid, 2010-07-05
(c) Copyright 2005-2010 University of Manchester, UK


Licence
=======
Taverna is licenced under the GNU Lesser General Public Licence. (LGPL) 2.1.
See the file LICENCE.txt for details.

If the source code was not included in this download, you can download it from
http://www.taverna.org.uk/download/source-code/

Taverna uses various third-party libraries that are included under compatible
open source licences such as Apache Licence.


Running Taverna
===============
For Windows, if you used the installer you should have 
Taverna->Taverna workbench 2.2.0 in your Start menu. If you used the
self-extracting archive, double-click on "taverna.exe" in the extracted folder.

For OS X, copy the Taverna application bundle to /Applications or your
preference. Eject the disk image, and run Taverna from the installed location.

For Linux, either double-click on "taverna.sh", or start a Terminal and execute
"./taverna.sh" or "sh taverna.sh" from the folder where you extracted Taverna.

Documentation
=============
See http://www.taverna.org.uk/documentation/taverna-2-x/ for
documentation and tutorials on using Taverna.

See the file known-issues.txt for known issues with this release, and the file
release-notes.txt for improvements since the previous version of Taverna.


Examples
========
Example Taverna 2.2 workflows can be found in the myExperiment starter pack at
http://www.myexperiment.org/packs/122

You can also access these as the tab 'Starter pack' under the *myExperiment*
view inside Taverna.

You can share and find other workflows at http://www.myexperiment.org/

You can find and describe web services at http://www.biocatalogue.org/


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


Registration
============
Taverna might ask if you would like to register as a Taverna user. The myGrid
team would appreciate if you do, as it would:

  * Allow us to support you better; future plans will be directed towards
    solutions Taverna users require

  * Help sustain Taverna development

By registering, you will *not* be giving us access to your data or service
usage. We will *not* be monitoring your usage of Taverna, we will only record
the information you provide at registration time.

For full terms and condition of our registration, see
http://www.taverna.org.uk/about/legal-stuff/terms/


Requirements
============
Taverna requires the Java Runtime Environment (JRE) version 5 or 6 from Sun.
No other versions of Java are officially tested with Taverna. 

*Note that future versions of Taverna will require Java 6.*

Mac OS X 10.5 (Leopard) and later should come with Java 5 or newer.  

Windows users might need to download Java from http://java.com/

Linux users have different options to install Java depending on their Linux
distribution. Some distributions, such as Ubuntu, might come with alternative
open source implementations of Java, like Gnu GCJ and OpenJDK. We've identified
some issues with these implementations, and recommend using the official Java
implementation from Sun. 

To download Sun Java 6 for Ubuntu, start a Terminal, and type the following:
  sudo aptitude install sun-java6-jre

and follow the instructions. You might also need to change the default Java
implementation by running:
  sudo update-alternatives --config java

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
  http://tinyurl.com/java6sec    -or- 
  https://cds.sun.com/is-bin/INTERSHOP.enfinity/WFS/CDS-CDS_Developer-Site/en_US/-/USD/ViewProductDetail-Start?ProductRef=jce_policy-6-oth-JPR@CDS-CDS_Developer

For Java 5, you can download the unlimited cryptography policy jar files from:
  http://tinyurl.com/java5sec    -or- 
  https://cds.sun.com/is-bin/INTERSHOP.enfinity/WFS/CDS-CDS_Developer-Site/en_US/-/USD/ViewProductDetail-Start?ProductRef=jce_policy-1.5.0-oth-JPR@CDS-CDS_Developer


Advanced
========

Logging
-------

If you are using a Windows operating system and want to see Taverna's
logging information, then run "taverna-debug.bat" from the installed
Taverna folder.

In OS X, start "/Applications/Utillities/Console".

In Linux run "taverna.sh" from a Terminal.

You can also find detailed logs in Taverna's home directory. You can 
view this directory from within Taverna by choosing 
Advanced->Show Log Folder in the menu.

See:

    Windows XP: C:\Documents and settings\JohnDoe\
                   Application Data\taverna-2.2.0\logs

    Vista/Windows 7: C:\Users\JohnDoe\AppData\Roaming\taverna-2.2.0\logs

    OS X: /Users/JohnDoe/Library/Application support/taverna-2.2.0/logs

    Linux: /home/johndoe/.taverna-2.2.0/logs

Note that "Application data" and "AppData" are hidden folders in Windows.

To modify the log levels, edit "conf/log4j.properties" in the Taverna
installation folder.

Default services and plugins
----------------------------

You can edit the default service list for the Taverna installation by
editing "conf/default_service_providers.xml". You can generate this
service list from within Taverna by right-clicking on "Available
Services" and selecting "Export Services To File".
 
Similarly you can replace the installation "plugins/plugins.xml" with
"plugins.xml" from the Taverna home directory to force installation of a
plugin by default. Note that in this case it is also recommended to copy
the full content of "repository" from the Taverna home directory to
append the installation directory's "repository".


Memory usage 
------------
For OS X and Linux, Taverna will use a maximum of 400 MB. In Windows,
"taverna-debug.bat" will also use 400 MB, while "taverna.exe" will use
400 MB or up to 50% of available memory when launching Taverna.

If Taverna happens to run out of memory while running a workflow with
large data (or large number of data), you can set Taverna to store data
in a database by going to Preferences->Data and Provenance and remove
the tick for "In-memory storage". This will reduce execution speed
slightly, but should consume less memory. Also remember to click
'Remove' on old runs you are no longer interested in.

If you need to increase the available memory, edit "taverna.sh" or
"taverna-debug.bat" and replace "-Xmx400m" with say "-Xmx600m" to use
600 MiB.

For OS X you would need to right click on "Taverna.app" and go inside
the application bundle to edit Contents/Info.plist and change the same
parameter using the Property List Editor.
