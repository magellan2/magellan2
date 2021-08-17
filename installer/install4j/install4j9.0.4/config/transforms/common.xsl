<xsl:transform version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns=""
               xmlns:math="http://exslt.org/math"
               xmlns:ext="com.install4j.config.XsltExtensions"
               extension-element-prefixes="ext math">

  <xsl:output indent="yes"/>

  <xsl:variable name="maxId" select="ext:setMaxId(math:max(//@id[number(.) > 0]))"/>

  <xsl:template match="/install4j">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:attribute name="maxId">
        <xsl:value-of select="$maxId"/>
      </xsl:attribute>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template name="addIdAttribute">
    <xsl:attribute name="id">
      <xsl:value-of select="ext:getNextId()"/>
    </xsl:attribute>
  </xsl:template>

</xsl:transform>