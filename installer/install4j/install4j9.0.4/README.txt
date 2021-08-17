-------------------------------------------------------------------------
install4j readme

version 9.0.4
released on 2021-07-09
-------------------------------------------------------------------------

I. LICENSE

The license agreement (EULA) can be found in license.html in the same
directory as this readme file.


II. RUNNING install4j

1. WINDOWS

Start install4j by executing

   $INSTALL4J_HOME\bin\install4j.exe
   
   
2. LINUX/UNIX

Start install4j by executing the shell script

   $INSTALL4J_HOME/bin/install4j


3. macOS

Start install4j with the installed application bundle. Usually, install4j 
will be installed to:

   /Applications/install4j


III. Upgrading install4j

You may install a new version of install4j on top of an older version.
Older configuration files can be read by install4j.


IV. DOCUMENTATION

Help is available 

1. from the "Help" button in the install4j wizard
2. as PDF in the doc directory of your install4j installation.
3. online at https://www.ej-technologies.com/resources/install4j/help/doc/
4. by executing install4jc.exe --help


V. DIRECTORY LAYOUT

An installation of install4j contains the following directories:

   bin
         contains the executables for install4j.
   
   config
         contains configuration templates for install4j
   
   doc
         contains the documentation for install4j

   extensions
         contains extensions for install4j. See the README.txt in that
         directory for more information.

   javadoc
         contains the API documentation for install4j

   jre         
         contains the JRE install4j runs with if you downloaded an
         installer with a bundled JRE.

   jres         
         contains the JREs you can bundle with your applications. You can
         download JREs with the download wizard in the install4j GUI.
         
   lib
         contains external libraries used by install4j. If the libraries
         come with an different license, it is reproduced in that
         directory.
         
   resource
         contains resources for the install4j compiler
         
   samples
         contains sample projects for install4j

