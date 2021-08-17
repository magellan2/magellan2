<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- install4j 5.1 -->
<xsl:transform version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns=""
               xmlns:math="http://exslt.org/math"
               xmlns:ext="com.install4j.config.XsltExtensions"
               extension-element-prefixes="ext math">

  <xsl:include href="common.xsl"/>

  <xsl:template match="//action[@beanClass='com.install4j.runtime.beans.actions.misc.RunExecutableAction']">
    <xsl:variable name="executionType" select="serializedBean/java/object/void[@property='executionType']/object/@field"/>
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:attribute name="actionElevationType">
        <xsl:choose>
          <xsl:when test="$executionType='ORIGINAL_USER'"><xsl:text>none</xsl:text></xsl:when>
          <xsl:otherwise><xsl:text>elevated</xsl:text></xsl:otherwise>
        </xsl:choose>
      </xsl:attribute>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="//object[@class='com.install4j.runtime.beans.groups.ScreenGroup' or
                                @class='com.install4j.runtime.beans.groups.ActionGroup']">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:if test="void[@property='loopExpression']">
        <void property="loop">
          <boolean>true</boolean>
        </void>
      </xsl:if>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="//object[@class='com.install4j.runtime.beans.actions.misc.RunExecutableAction']/void[@property='executionType']"/>
  <xsl:template match="//object[@class='com.install4j.runtime.beans.actions.misc.RunExecutableAction']/void[@property='failOnRedirectionError']"/>

  <xsl:template match="//object[@class='com.install4j.runtime.beans.actions.misc.RunExecutableAction']">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:if test="void[@property='stdoutFile']">
        <void property="stdoutRedirectionMode">
          <object class="com.install4j.runtime.installer.helper.launching.OutputRedirectionMode" field="FILE" />
        </void>
      </xsl:if>
      <xsl:if test="void[@property='stderrFile']">
        <void property="stderrRedirectionMode">
          <object class="com.install4j.runtime.installer.helper.launching.OutputRedirectionMode" field="FILE" />
        </void>
      </xsl:if>
      <xsl:if test="void[@property='stdinFile']">
        <void property="stdinRedirectionMode">
          <object class="com.install4j.runtime.installer.helper.launching.InputRedirectionMode" field="FILE" />
        </void>
      </xsl:if>
      <xsl:if test="void[@property='failOnRedirectionError']">
        <void property="failOnStdoutFileError">
          <boolean>true</boolean>
        </void>
        <void property="failOnStderrFileError">
          <boolean>true</boolean>
        </void>
        <void property="failOnStdinFileError">
          <boolean>true</boolean>
        </void>
      </xsl:if>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

</xsl:transform>
