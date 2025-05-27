# Welcome to Magellan!

Magellan is a full-featured client tool for the play-by-e-mail game Eressea. It displays a map of your part of the Eressea world, supports you in writing orders, performs many complex calculations for you and is available completely in English.

Magellan is only one among many other client programs for Eressea and is in no way related to the developers of Eressea. To relieve them of unnecessary work please contact the Magellan Community in case you have any questions or problems.

## How To Compile

Actually the Magellan Client is very old. It was originally designed to run with Java 6. Then we switched to Java 8 and Java 11. Then the development was more or less closed, because most really necessary features are implemented and everybody could manage also big Eressea CR reports. In the last years, we made only very limited changes for some new in game features.

With (f.e.) Eclipse IDE it is very easy to import this repository as a project. Also running the project with newer JDKs is not an issue. It works out of the box

- checkout the sources from https://github.com/magellan2/magellan2
- in Eclipse or your prefered Java IDE open the project from file system..
- maybe add your current JRE system library to the Eclipse project. 

I've tested with Eclipse Temurin JDK 17.0.1 and OpenJDK 23.0.3. and both where compiling the sources and could run the Magellan Client `src-client/magellan.client.Client`. The build process also runs IZPack, which requires JDK 11 because of some deprecated classes.

To build a complete new release, we are still using Apache Ant and Java 11. To install Ant follow the instructions on https://ant.apache.org/. I'm using Homebrew with `brew install ant`. Worked as expected. But it's also included in this repository

Then open a terminal and run the following command to run the integrated unit tests, which shows helpful informations about any kind of compile issues in the future.

    ant -noinput -buildfile build.xml run_tests

There are several interestng ant targets, like

- distribute - build a new version based on Izpack (requires Java11 SDK)
- quick_build - only compiles code and creates new jars
- distribute_install4j - is the default target and uses the Install4J tool to create native installer. Be aware to set INSTALL4J_KEY with the license key to create your products.

## How To Run

When using the source code here in this repository and compiled the client with `ant build_client_jar` you can just run it with

    java -jar magallan-client.jar

## How To Release

We are currently in a process of switching from Izpack to Install4j to allow native installer for Windows, Linux and MacOS even when our Magallan client as a Java program can run on any platform that supports Java11. The process of new creating a new release is currently a bit difficulty.

- we protect the default branch, so it's not possible to commit anything to it without a merge request. When you develop a new feature or a fix a bug, you should create at least one new branch (like `develop`). Every changes must be done there. We have created a Github action that runs on your branch to test, if the application can be compiled.
- then you need to manually update `build.xml` and update the properties `VERSION.MAJOR`, `VERSION.MINOR` and `VERSION.SUB` as the Semantic versioning says. `VERSION.SUB` should increase for any bugfix release, `VERSION.MINOR` should be increased for any feature release and `VERSION.MAJOR` should be increased on major changes.
- we have also a build number in the hidden file `.build.number`, that increases during the build process once, but the build process pipeline doesnt commit it back to the branch, so please also increase it manually and commit it
- validate, if also the installer/izpack-install.template.xml is up2date with the new version number etc.
- please also update `RELASENOTES.txt` and `CHANGELOG.txt` to document your changes
- commit and push everything into your branch
- create a pull request
- when the PR is approved, the build pipeline should run again and should create a `stable` release, see `.github/workflow/publish.yml`. It contains installer for Java, Windows, Unix, MacOS. It also updates the home page and informs users about a new release.

## How to Distribute

Please feel free to distribute your ideas. Just create a Pull-Request like described above. Update `build.xml`, `.build.number`, `CHANGELOG.txt` and `RELEASENOTES.txt`. 

## Other links

- most devs are using Eressea to "validate" the client. See https://www.eressea.de/
- we use Install4J now to create installers for Windows, Linux and MacOS. See https://www.ej-technologies.com/install4j

## License(s)

Unless otherwise indicated, all files in this distribution are
provided to you under the terms and conditions of the GNU GPL
License Version 2.0. A copy of this license is provided in
COPYING.

Exceptions from the GNU GPL  are notified in the source files
itself or written down in a seperate file (e.g. 
src/release/lib/jflap.LICENSE.txt).

If you are not satisfied that EVERYTHING is under the GNU GPL 
License you should not use Magellan.
