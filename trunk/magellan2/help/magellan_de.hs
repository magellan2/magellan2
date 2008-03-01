<?xml version='1.0' encoding='ISO-8859-1' ?>
<!DOCTYPE helpset PUBLIC "-//Sun Microsystems Inc.//DTD JavaHelp HelpSet Version 1.0//EN" "http://java.sun.com/products/javahelp/helpset_1_0.dtd">
<helpset version="1.0" xml:lang="de">

  <!-- title -->
  <title>Magellan Hilfe</title>

  <!-- maps -->
  <maps>
     <homeID>intro</homeID>
     <mapref location="de/magellan.jhm"/>
  </maps>

  <!-- views -->
  <view>
    <name>TOC</name>
    <label>Magellan</label>
    <type>javax.help.TOCView</type>
    <data>de/magellanTOC.xml</data>
  </view>

  <view>
    <name>TOC</name>
    <label>Eressea Kurzreferenz</label>
    <type>javax.help.TOCView</type>
    <data>de/eresseaTOC.xml</data>
  </view>

  <view>
    <name>Search</name>
    <label>Search</label>
    <type>javax.help.SearchView</type>
    <data engine="com.sun.java.help.search.DefaultSearchEngine">
      JavaHelpSearch
    </data>
  </view>

</helpset>

<!-- $Id: magellan_de.hs 8 2003-10-12 21:05:43Z eressea $ -->