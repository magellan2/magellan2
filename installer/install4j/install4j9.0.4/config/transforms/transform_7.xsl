<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- install4j 7.0.1 -->
<xsl:transform version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns=""
               xmlns:math="http://exslt.org/math"
               xmlns:ext="com.install4j.config.XsltExtensions"
               extension-element-prefixes="ext math">

  <xsl:include href="common.xsl"/>

  <xsl:template match="//formComponent[@beanClass='com.install4j.runtime.beans.styles.ContentComponent']/@insetTop[.='5']">
    <xsl:attribute name="insetTop">10</xsl:attribute>
  </xsl:template>

  <xsl:template match="//object[
    @class='com.install4j.runtime.beans.applications.InstallerApplication'
    or @class='com.install4j.runtime.beans.applications.UninstallerApplication'
    or @class='com.install4j.runtime.beans.applications.CustomApplication'
    ]/void[
      @property='watermark'
      or @property='customWatermarkText']"
  />
</xsl:transform>
