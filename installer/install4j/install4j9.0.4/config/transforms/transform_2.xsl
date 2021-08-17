<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- install4j 4.1 --> 
<xsl:transform version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="">

  <xsl:output indent="yes"/>

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="/install4j/installerGui">
    <xsl:copy>
      <xsl:copy-of select="@*"/>

      <applications>
        <application name="" id="installer" beanClass="com.install4j.runtime.beans.applications.InstallerApplication" useCustomIcon="{@useCustomIcon}" customIcnsFile="{@customIcnsFile}" customPngIcon16File="{@customPngIcon16File}" customPngIcon32File="{@customPngIcon32File}" customIcoFile="{@customIcoFile}">
          <serializedBean>
            <java class="java.beans.XMLDecoder">
              <object class="com.install4j.runtime.beans.applications.InstallerApplication">
                <xsl:if test="@useCustomInstallerHeaderImage = 'true'">
                  <void property="customHeaderImage">
                    <object class="com.install4j.api.beans.ExternalFile">
                      <string><xsl:value-of select="@customInstallerHeaderImage"/></string>
                    </object>
                  </void>
                </xsl:if>
                <void property="suppressProgressDialog">
                  <boolean><xsl:value-of select="@suppressProgressDialog"/></boolean>
                </void>

                <xsl:call-template name="addCommonApplicationProperties" />

              </object>
            </java>
          </serializedBean>
          <startup>
            <xsl:copy-of select="installerStartup/screen"/>
          </startup>
          <screens>
            <xsl:copy-of select="installerScreens/screen"/>
          </screens>
        </application>

        <application name="" id="uninstaller" beanClass="com.install4j.runtime.beans.applications.UninstallerApplication" useCustomIcon="{@useCustomIcon}" customIcnsFile="{@customIcnsFile}" customPngIcon16File="{@customPngIcon16File}" customPngIcon32File="{@customPngIcon32File}" customIcoFile="{@customIcoFile}">
          <serializedBean>
            <java class="java.beans.XMLDecoder">
              <object class="com.install4j.runtime.beans.applications.UninstallerApplication">
                <xsl:if test="@useCustomUninstallerHeaderImage = 'true'">
                  <void property="customHeaderImage">
                    <object class="com.install4j.api.beans.ExternalFile">
                      <string><xsl:value-of select="@customUninstallerHeaderImage"/></string>
                    </object>
                  </void>
                </xsl:if>

                <xsl:call-template name="addCommonApplicationProperties" />

              </object>
            </java>
          </serializedBean>
          <startup>
            <xsl:copy-of select="uninstallerStartup/screen"/>
          </startup>
          <screens>
            <xsl:copy-of select="uninstallerScreens/screen"/>
          </screens>
        </application>
      </applications>

      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <xsl:template name="addCommonApplicationProperties">

    <void property="useCustomIcon">
      <boolean><xsl:value-of select="@useCustomIcon"/></boolean>
    </void>
    <xsl:if test="@useCustomIcon = 'true'">
      <void property="customPngIcon16File">
        <object class="com.install4j.api.beans.ExternalFile">
          <string><xsl:value-of select="@customPngIcon16File"/></string>
        </object>
      </void>
      <void property="customPngIcon32File">
        <object class="com.install4j.api.beans.ExternalFile">
          <string><xsl:value-of select="@customPngIcon32File"/></string>
        </object>
      </void>
    </xsl:if>

    <void property="allowUnattended">
      <boolean><xsl:value-of select="@allowUnattended"/></boolean>
    </void>
    <void property="allowConsole">
      <boolean><xsl:value-of select="@allowConsole"/></boolean>
    </void>
    <void property="frameWidth">
      <int><xsl:value-of select="@customWidth"/></int>
    </void>
    <void property="frameHeight">
      <int><xsl:value-of select="@customHeight"/></int>
    </void>
    <void property="vmParameters">
      <string><xsl:value-of select="@vmParameters"/></string>
    </void>
    <void property="watermark">
      <boolean><xsl:value-of select="@watermark"/></boolean>
    </void>
    <void property="resizable">
      <boolean><xsl:value-of select="@resizable"/></boolean>
    </void>
  </xsl:template>

  <xsl:template match="/install4j/mediaSets/*">

    <xsl:copy>
      <xsl:copy-of select="@*"/>

      <xsl:if test="excludedInstallerScreens">
        <excludedBeans>
          <xsl:call-template name="addExcludedScreens">
            <xsl:with-param name="screens" select="excludedInstallerScreens/*"/>
          </xsl:call-template>
          <xsl:call-template name="addExcludedScreens">
            <xsl:with-param name="screens" select="excludedUninstallerScreens/*"/>
          </xsl:call-template>
        </excludedBeans>
      </xsl:if>

      <xsl:apply-templates />
      
    </xsl:copy>

  </xsl:template>

  <xsl:template name="addExcludedScreens">
    <xsl:param name="screens"/>
    <xsl:for-each select="$screens">
      <bean refId="{@refId}"/>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="entry/@launcher[.='true']">
    <xsl:attribute name="fileType">
      <xsl:text>launcher</xsl:text>
    </xsl:attribute>
  </xsl:template>

</xsl:transform>
