# Welcome to Magellan!

Magellan is a full-featured client tool for the play-by-e-mail game Eressea. It displays a map of your part of the Eressea world, supports you in writing orders, performs many complex calculations for you and is available completely in English.

Magellan is only one among many other client programs for Eressea and is in no way related to the developers of Eressea. To relieve them of unnecessary work please contact the Magellan Community in case you have any questions or problems.

## How To Compile

Actually the Magellan Client is very old. It was originally designed to run with Java 6. Then we switched to Java 8 and Java 11. Then the development was more or less closed, because most really necessary features are implemented and everybody could manage also big Eressea CR reports. In the last years, we made only very limited changes for some new in game features.

With (f.e.) Eclipse IDE it is very easy to import this repository as a project. Also running the project with newer JDKs is not an issue. It works out of the box

- checkout the sources
- in Eclipse open the project from file system..
- maybe add your current JRE system library to the Eclipse project. 

I've tested with Eclipse Temurin JDK 17.0.1 and OpenJDK 23.0.3. and both where compiling the sources and could run the Magellan Client `src-client/magellan.client.Client`

To build a complete new release, we are still using Apache Ant. To install Ant follow the instructions on https://ant.apache.org/. I'm using Homebrew with `brew install ant`. Worked as expected.
Then open a terminal and run the following command to run the integrated unit tests, which shows helpful informations about any kind of compile issues in the future.

    ant -noinput -buildfile build.xml run_tests



## How Tu Run

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
