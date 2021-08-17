<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- install4j 9.0 -->
<xsl:transform version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns=""
               xmlns:math="http://exslt.org/math"
               xmlns:ext="com.install4j.config.XsltExtensions"
               extension-element-prefixes="ext math">

  <xsl:include href="common.xsl"/>

  <xsl:template match="//action[@beanClass='com.install4j.runtime.beans.actions.InstallFilesAction']/serializedBean/property[@name='updateBundledJre']"/>

  <xsl:template match="//install4j/application/@*[(local-name()='javaMinVersion' or local-name()='javaMaxVersion') and (starts-with(., '1.5') or starts-with(., '1.6') or starts-with(., '1.7'))]">
    <xsl:attribute name="{name()}">
      <xsl:text>1.8</xsl:text>
    </xsl:attribute>
  </xsl:template>

  <xsl:template match="/install4j/application/jreBundles[@jdkProviderId='JBR']/@release">
    <xsl:attribute name="{name()}">
      <xsl:choose>
        <xsl:when test="starts-with(., '8u')">
          <xsl:text>8/</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:text>11/</xsl:text>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:value-of select="."/>
    </xsl:attribute>
  </xsl:template>

  <xsl:template match="/install4j/application/jreBundles[@jdkProviderId='Liberica']/@release">
    <xsl:attribute name="{name()}">
      <xsl:choose>
        <xsl:when test="starts-with(., 'OpenJDK 8u')">
          <xsl:text>8</xsl:text>
        </xsl:when>
        <xsl:when test="contains(., '.')">
          <xsl:value-of select="substring-before(substring-after(., 'OpenJDK '), '.')"/>
        </xsl:when>
        <xsl:when test="contains(., '+')">
          <xsl:value-of select="substring-before(substring-after(., 'OpenJDK '), '+')"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="substring-after(., 'OpenJDK ')"/>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:text>/</xsl:text>
      <xsl:value-of select="substring-after(., 'OpenJDK ')"/>
    </xsl:attribute>
  </xsl:template>

  <xsl:template match="/install4j/application/jreBundles[@jdkProviderId='AdoptOpenJDK']">
    <jreBundles>
      <xsl:attribute name="jdkProviderId">
        <xsl:choose>
          <xsl:when test="contains(@release, 'openj9')">
            <xsl:text>AdoptOpenJDK-OpenJ9</xsl:text>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text>AdoptOpenJDK</xsl:text>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:attribute>
      <xsl:attribute name="release">
        <xsl:choose>
          <xsl:when test="starts-with(@release, 'openjdk')">
            <xsl:value-of select="substring-after(@release, 'openjdk')"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="@release"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:attribute>
    </jreBundles>
  </xsl:template>

  <xsl:template match="/install4j/mediaSets/*[not(jreBundle)]">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <jreBundle jreBundleSource="none"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="/install4j/mediaSets/*/jreBundle[not(@jreBundleSource)]">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:attribute name="jreBundleSource">none</xsl:attribute>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="/install4j/files">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:attribute name="preserveSymlinks">false</xsl:attribute>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="//serializedBean/property[@name='backgroundColor']/object[
      @class='java.awt.Color' and count(int[text()='255']) = 4
    ]">
    <object class="com.install4j.runtime.beans.LightOrDarkColor">
      <xsl:copy-of select="."/>
      <object class="java.awt.Color">
        <int>49</int>
        <int>52</int>
        <int>53</int>
        <int>255</int>
      </object>
    </object>
  </xsl:template>

  <xsl:template match="//serializedBean/property[@name='imageEdgeBackgroundColor']/object[
      @class='java.awt.Color' and int[text()='25'] and int[text()='143'] and int[text()='220'] and int[text()='255']
    ]">
    <object class="com.install4j.runtime.beans.LightOrDarkColor">
      <xsl:copy-of select="."/>
      <object class="java.awt.Color">
        <int>0</int>
        <int>74</int>
        <int>151</int>
        <int>255</int>
      </object>
    </object>
  </xsl:template>

  <xsl:template match="//action[@beanClass='com.install4j.runtime.beans.actions.misc.RequestPrivilegesAction']//serializedBean/property[@name='failIfNotRootUnix' and @value='true']">
    <property name="failIfNotRootUnix" type="boolean" value="true" />
    <property name="linuxPrivilegeRequirement" type="enum" class="com.install4j.runtime.beans.actions.misc.PrivilegeRequirement" value="ROOT" />
  </xsl:template>

  <xsl:template match="/install4j/installerGui/staticMembers">
    <xsl:copy>
      <xsl:value-of select="@script"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="/install4j/files/mountPoints/mountPoint[@mode and (not(/install4j/files/@defaultDirMode)) or @mode != /install4j/files/@defaultDirMode]">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:attribute name="overrideMode">true</xsl:attribute>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

</xsl:transform>
