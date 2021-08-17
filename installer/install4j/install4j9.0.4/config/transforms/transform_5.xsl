<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- install4j 6.0 -->
<xsl:transform version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns=""
               xmlns:math="http://exslt.org/math"
               xmlns:ext="com.install4j.config.XsltExtensions"
               extension-element-prefixes="ext math">

  <xsl:include href="common.xsl"/>

  <xsl:template match="//object[@class='com.install4j.runtime.beans.actions.misc.LoadResponseFileAction']">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:if test="void[@property='overwrite']/boolean/text()='false'">
        <void property="overwriteStrategy">
          <object class="java.lang.Enum" method="valueOf">
            <class>com.install4j.runtime.beans.actions.misc.OverwriteStrategy</class>
            <string>OFF</string>
          </object>
        </void>
      </xsl:if>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="//object[@class='com.install4j.runtime.beans.actions.misc.LoadResponseFileAction']/void[@property='overwrite']"/>

  <xsl:template match="//object[@class='com.install4j.runtime.beans.actions.control.WaitForHttpPortAction']">
    <xsl:copy>
      <xsl:attribute name="class">
        <xsl:text>com.install4j.runtime.beans.actions.net.WaitForHttpServerAction</xsl:text>
      </xsl:attribute>
      <void property="url">
        <string>
          <xsl:text>http://</xsl:text>
          <xsl:choose>
            <xsl:when test="void[@property='host']">
              <xsl:value-of select="void[@property='host']/string"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:text>localhost</xsl:text>
            </xsl:otherwise>
          </xsl:choose>
          <xsl:if test="void[@property='port']">
            <xsl:text>:</xsl:text>
            <xsl:value-of select="void[@property='port']/int"/>
          </xsl:if>
          <xsl:if test="void[@property='file']">
            <xsl:text>/</xsl:text>
            <xsl:value-of select="void[@property='file']/string"/>
          </xsl:if>
        </string>
      </void>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="//object[@class='com.install4j.runtime.beans.actions.control.WaitForHttpPortAction']/void[@property='file' or @property='host' or @property='port']"/>

  <xsl:template match="//@*[.='com.install4j.runtime.beans.actions.control.WaitForHttpPortAction']">
    <xsl:attribute name="{name(.)}">
      <xsl:text>com.install4j.runtime.beans.actions.net.WaitForHttpServerAction</xsl:text>
    </xsl:attribute>
  </xsl:template>

  <xsl:template match="//@*[.='com.install4j.runtime.beans.actions.update.DownloadFileAction']">
    <xsl:attribute name="{name(.)}">
      <xsl:text>com.install4j.runtime.beans.actions.net.DownloadFileAction</xsl:text>
    </xsl:attribute>
  </xsl:template>

  <xsl:template match="//object[@class='com.install4j.runtime.beans.actions.update.DownloadFileAction']/void[@property='targetFile']">
    <void property="targetFile">
      <object class="java.io.File">
        <string><xsl:value-of select="string"/></string>
      </object>
    </void>
  </xsl:template>

  <xsl:template match="//object[@class='com.install4j.runtime.beans.actions.update.CheckForUpdateAction']/void[@property='updateDescriptorUrl']/@property">
    <xsl:attribute name="property">
      <xsl:text>url</xsl:text>
    </xsl:attribute>
  </xsl:template>

  <xsl:template match="//object[@class='com.install4j.runtime.beans.actions.desktop.RegisterAddRemoveAction']">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:if test="void[@property='icon']">
        <void property="iconSource">
          <object class="java.lang.Enum" method="valueOf">
            <class>com.install4j.runtime.beans.actions.desktop.IconSource</class>
            <string>CUSTOM</string>
          </object>
        </void>
      </xsl:if>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="//@*[.='com.crionics.jpdf.PdfScreen']">
    <xsl:attribute name="{name(.)}">
      <xsl:text>com.install4j.extensions.pdf.PdfScreen</xsl:text>
    </xsl:attribute>
  </xsl:template>

  <xsl:template match="//@*[.='com.crionics.jpdf.ShowPdfAction']">
    <xsl:attribute name="{name(.)}">
      <xsl:text>com.install4j.extensions.pdf.ShowPdfAction</xsl:text>
    </xsl:attribute>
  </xsl:template>

  <xsl:template match="//object[@class='com.crionics.jpdf.PdfScreen']/void[@property='showNavigationToolbar' or @property='showMatrixToolbar' or @property='showZoomsToolbar']"/>

  <xsl:template match="//object[@class='com.install4j.runtime.beans.applications.UninstallerApplication']">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <void property="useCustomMacosExecutableName">
        <boolean>true</boolean>
      </void>
      <void property="customMacosExecutableName">
        <string>${i18n:UninstallerMenuEntry(${compiler:sys.fullName})}</string>
      </void>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="/install4j/mediaSets/macos | /install4j/mediaSets/macosFolder">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:variable name="installerName" select="/install4j/application/@installerName"/>
      <xsl:if test="$installerName != ''">
        <xsl:attribute name="installerName">
          <xsl:value-of select="$installerName"/>
        </xsl:attribute>
      </xsl:if>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="splashScreen">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:if test="@show='true' and @java6SplashScreen='false'">
        <xsl:attribute name="windowsNative">true</xsl:attribute>
        <xsl:attribute name="textOverlay">true</xsl:attribute>
      </xsl:if>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="statusLine | versionLine">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:attribute name="bold">
        <xsl:value-of select="string(@fontWeight &gt; 500)"/>
      </xsl:attribute>
    </xsl:copy>
  </xsl:template>

</xsl:transform>
