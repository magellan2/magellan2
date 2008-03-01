<?xml version='1.0' encoding='ISO-8859-1' ?>
<!DOCTYPE helpset PUBLIC "-//Sun Microsystems Inc.//DTD JavaHelp HelpSet Version 1.0//EN" "http://java.sun.com/products/javahelp/helpset_1_0.dtd">
<helpset version="1.0" xml:lang="en">

  <!-- title -->
  <title>Magellan Help</title>

  <!-- maps -->
  <maps>
     <homeID>intro</homeID>
     <mapref location="en/magellan.jhm"/>
  </maps>

  <!-- views -->
  <view>
    <name>TOC</name>
    <label>Magellan</label>
    <type>javax.help.TOCView</type>
    <data>en/magellanTOC.xml</data>
  </view>

  <view>
    <name>TOC</name>
    <label>Eressea Reference</label>
    <type>javax.help.TOCView</type>
    <data>en/eresseaTOC.xml</data>
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
