<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- install4j 8.0 -->
<xsl:transform version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns=""
               xmlns:math="http://exslt.org/math"
               xmlns:ext="com.install4j.config.XsltExtensions"
               extension-element-prefixes="ext math">

  <xsl:include href="common.xsl"/>

  <xsl:variable name="newVmOptionsFormat" select="/install4j/application/variables/variable[@name='sys.ext.vmSpecificOptionFormat' and @value='new']"/>

  <xsl:template match="/install4j/launchers/launcher/java/vmOptions/options/@version">
    <xsl:choose>
      <xsl:when test="$newVmOptionsFormat">
        <xsl:copy/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:attribute name="{name()}">
          <xsl:text>"</xsl:text>
          <xsl:value-of select="."/>
          <xsl:text>*"</xsl:text>
        </xsl:attribute>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="/install4j/launchers/launcher[@external != 'true']">
    <xsl:copy>
      <xsl:copy-of select="@*"/>

      <macStaticAssociationActions mode="selected"/>

      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="/install4j/application/@createMd5Sums">
    <xsl:attribute name="createChecksums">
      <xsl:value-of select="."/>
    </xsl:attribute>
  </xsl:template>

  <xsl:template match="//object[@class='com.install4j.runtime.beans.actions.net.DownloadFileAction']">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:if test="void[@property='checkForMd5Sums']">
        <void property="digestAlgorithm">
          <object class="java.lang.Enum" method="valueOf">
            <class>com.install4j.runtime.beans.actions.net.DigestAlgorithm</class>
            <string>SHA256_OR_MD5</string>
          </object>
        </void>
      </xsl:if>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="//object[@class='com.install4j.runtime.beans.actions.net.DownloadFileAction']/void[@property='checkForMd5Sums']">
    <void property="checkDigest">
      <xsl:apply-templates />
    </void>
  </xsl:template>

  <xsl:template match="//object[@class='com.install4j.runtime.beans.actions.net.DownloadFileAction']/void[@property='md5Url']">
    <void property="digestUrl">
      <xsl:apply-templates />
    </void>
  </xsl:template>

  <xsl:template match="/install4j/mediaSets/*">
    <xsl:copy>
      <xsl:copy-of select="@*"/>

      <xsl:apply-templates />
      <jreBundle
          includedJre="{@includedJRE}"
          manualJreEntry="{@manualJREEntry}"
          bundleType="{@bundleType}"
          bundleUrl="{@jreURL}"
          shared="{@jreShared}"
          directDownload="{@directDownload}"
          installOnlyIfNecessary="{@installOnlyIfNecessary}"
          requiredVmIdPrefix="{@requiredVmIdPrefix}"
      >
        <xsl:attribute name="jreBundleSource">
          <xsl:choose>
            <xsl:when test="@includedJRE = ''">
              <xsl:value-of select="'none'"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="'preCreated'"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:attribute>
      </jreBundle>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="
      /install4j/launchers/launcher/customScript |
      /install4j/launchers/launcher/infoPlist |
      /install4j/installerGui/applications/application/customScript |
      /install4j/mediaSets/*/preInstallScript |
      /install4j/mediaSets/*/postInstallScript |
      /install4j/mediaSets/*/preUninstallScript |
      /install4j/mediaSets/*/postUninstallScript"
  >
    <xsl:copy>
      <xsl:call-template name="createFragmentText"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template name="createFragmentText">
    <xsl:choose>
      <xsl:when test="@mode='1'
          and ancestor::application[@id='installer']
          and /install4j/mediaSets/unixInstaller/installerScript[@mode!='1']"
      >
        <xsl:for-each select="/install4j/mediaSets/unixInstaller/installerScript[@mode!='1'][1]">
          <xsl:call-template name="createFragmentText"/>
        </xsl:for-each>
      </xsl:when>
      <xsl:when test="@mode='2'">
        <xsl:text>${compiler:file("</xsl:text>
        <xsl:value-of select="@file"/>
        <xsl:text>")}</xsl:text>
      </xsl:when>
      <xsl:when test="@mode='3'">
        <xsl:value-of select="content"/>
      </xsl:when>
    </xsl:choose>
  </xsl:template>
</xsl:transform>
