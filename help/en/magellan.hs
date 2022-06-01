<?xml version='1.0' encoding='ISO-8859-1' ?>
<!DOCTYPE helpset PUBLIC "-//Sun Microsystems Inc.//DTD JavaHelp HelpSet Version 1.0//EN" "http://java.sun.com/products/javahelp/helpset_1_0.dtd">
<helpset version="1.0" xml:lang="en">

    <!-- title -->
    <title>Magellan Help</title>

    <!-- maps -->
    <maps>
        <homeID>intro</homeID>
        <mapref location="magellan.jhm" />
    </maps>

    <!-- views -->
    <view>
        <name>TOC</name>
        <label>Magellan</label>
        <type>javax.help.TOCView</type>
        <data>magellanTOC.xml</data>
    </view>

    <view>
        <name>ETOC</name>
        <label>Eressea Reference</label>
        <type>javax.help.TOCView</type>
        <data>eresseaTOC.xml</data>
    </view>

    <view>
        <name>Search</name>
        <label>Search</label>
        <type>javax.help.SearchView</type>
        <data engine="com.sun.java.help.search.DefaultSearchEngine">
            JavaHelpSearch
        </data>
    </view>

    <!-- An index navigator
        <view xml:lang="en" mergetype="javax.help.SortMerge">
        <name>Index</name>
        <label>Index</label>
        <type>javax.help.IndexView</type>
        <data>glossary.xml</data>
        </view>
    -->

    <!-- A glossary navigator
        <view mergetype="javax.help.SortMerge">
        <name>glossary</name>
        <label>Glossary</label>
        <type>javax.help.GlossaryView</type>
        <data>glossary.xml</data>
        </view>
    -->

    <!-- A favorites navigator -->
    <view>
        <name>favorites</name>
        <label>Favorites</label>
        <type>javax.help.FavoritesView</type>
    </view>

    <presentation default=true>
        <name>main window</name>
        <size width="800" height="600" />
        <!-- <location x="0" y="0" /> -->
        <title>Magellan Help</title>
        <toolbar>
            <helpaction>javax.help.BackAction</helpaction>
            <helpaction>javax.help.ForwardAction</helpaction>
            <helpaction>javax.help.SeparatorAction</helpaction>
            <!-- <helpaction>javax.help.ReloadAction</helpaction> -->
            <helpaction>javax.help.PrintAction</helpaction>
            <!-- <helpaction>javax.help.PrintSetupAction</helpaction> -->
            <helpaction>javax.help.FavoritesAction</helpaction>
        </toolbar>
    </presentation>

    <!-- This window is simpler than the main window.
        * It's intended to be used a secondary window.
        * It has no navigation pane or toolbar.
    -->
    <presentation displayviews=false>
        <name>secondary window</name>
        <size width="800" height="600" />
        <location x="100" y="100" />
    </presentation>
</helpset>
