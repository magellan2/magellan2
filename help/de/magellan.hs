<?xml version='1.0' encoding='ISO-8859-1' ?>
<!DOCTYPE helpset PUBLIC "-//Sun Microsystems Inc.//DTD JavaHelp HelpSet Version 1.0//EN" "http://java.sun.com/products/javahelp/helpset_1_0.dtd">
<helpset version="1.0" xml:lang="de">

  <!-- title -->
  <title>Magellan Hilfe</title>

  <!-- maps -->
  <maps>
     <homeID>intro</homeID>
     <mapref location="magellan.jhm"/>
  </maps>

  <!-- views -->
  <view>
    <name>TOC</name>
    <label>Magellan</label>
    <type>javax.help.TOCView</type>
    <data>magellanTOC.xml</data>
  </view>

  <view>
    <name>TOC</name>
    <label>Eressea Kurzreferenz</label>
    <type>javax.help.TOCView</type>
    <data>eresseaTOC.xml</data>
  </view>

  <view>
    <name>Search</name>
    <label>Suche</label>
    <type>javax.help.SearchView</type>
    <data engine="com.sun.java.help.search.DefaultSearchEngine">
      JavaHelpSearch
    </data>
  </view>

</helpset>
