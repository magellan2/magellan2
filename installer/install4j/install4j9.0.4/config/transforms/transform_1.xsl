<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- install4j 4.0 --> 
<xsl:transform version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="">

  <xsl:output indent="yes"/>

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="/install4j/files/components/component">
    <xsl:copy>
      <xsl:variable name="screen" select="/install4j/installerGui/standardScreens/screen[@id='components']"/>
      <xsl:attribute name="selected">
        <xsl:value-of select="string($screen/@allSelected = 'true' or $screen/selectedComponents/componentIds[@refId = current()/@id])" />
      </xsl:attribute>
      <xsl:attribute name="mandatory">
        <xsl:value-of select="string((not(preceding-sibling::component) and $screen/@firstMandatory='true') or $screen/mandatoryComponents/componentIds[@refId=current()/@id])" />
      </xsl:attribute>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="/install4j/files/entries/*[@dontUninstall = 'true']">
    <xsl:copy>
      <xsl:attribute name="uninstallMode">1</xsl:attribute>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="/install4j/installerGui">
    <xsl:copy>
      <xsl:attribute name="suggestPreviousProgramGroup">
        <xsl:value-of select="@suggestPreviousLocations" />
      </xsl:attribute>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates/>
      <xsl:call-template name="addInstallerScreens" />
      <xsl:call-template name="addUninstallerScreens" />
      <installerStartup>
        <xsl:call-template name="addStartupScreen" />
      </installerStartup>
      <uninstallerStartup>
        <xsl:call-template name="addStartupScreen" />
      </uninstallerStartup>
    </xsl:copy>
  </xsl:template>

  <xsl:template name="addInstallerScreens">
    <installerScreens>
      <xsl:call-template name="addBannerScreen" >
        <xsl:with-param name="screenId" select="'welcome'"/>
        <xsl:with-param name="beanClass" select="'com.install4j.runtime.beans.screens.WelcomeScreen'"/>
      </xsl:call-template>
      <xsl:call-template name="addLicenseScreen"/>
      <xsl:call-template name="addLocationScreen"/>
      <xsl:call-template name="addSimpleScreen" >
        <xsl:with-param name="screenId" select="'components'"/>
        <xsl:with-param name="beanClass" select="'com.install4j.runtime.beans.screens.ComponentsScreen'"/>
      </xsl:call-template>
      <xsl:call-template name="addSimpleScreen" >
        <xsl:with-param name="screenId" select="'programGroup'"/>
        <xsl:with-param name="beanClass" select="'com.install4j.runtime.beans.screens.StandardProgramGroupScreen'"/>
      </xsl:call-template>
      <xsl:call-template name="addSimpleScreen" >
        <xsl:with-param name="screenId" select="'fileAssociations'"/>
        <xsl:with-param name="beanClass" select="'com.install4j.runtime.beans.screens.FileAssociationsScreen'"/>
      </xsl:call-template>
      <xsl:if test="/install4j/launchers/launcher/executable[@executableMode='3']">
        <xsl:call-template name="addSimpleScreen">
          <xsl:with-param name="screenId" select="'services'"/>
          <xsl:with-param name="beanClass" select="'com.install4j.runtime.beans.screens.ServicesScreen'"/>
        </xsl:call-template>
      </xsl:if>
      <xsl:call-template name="addInfoScreen" >
        <xsl:with-param name="screenId" select="'preInfo'"/>
      </xsl:call-template>
      <xsl:call-template name="addInstallationScreen"/>
      <xsl:call-template name="addInfoScreen" >
        <xsl:with-param name="screenId" select="'postInfo'"/>
      </xsl:call-template>
      <xsl:call-template name="addBannerScreen" >
        <xsl:with-param name="screenId" select="'finished'"/>
        <xsl:with-param name="beanClass" select="'com.install4j.runtime.beans.screens.FinishedScreen'"/>
        <xsl:with-param name="finishScreen" select="true()"/>
      </xsl:call-template>
    </installerScreens>
  </xsl:template>

  <xsl:template name="addSimpleScreen">
    <xsl:param name="screenId"/>
    <xsl:param name="beanClass"/>
    <xsl:variable name="screen" select="/install4j/installerGui/standardScreens/screen[@id=$screenId]"/>

    <xsl:if test="$screen/@enabled = 'true'">
      <screen beanClass="{$beanClass}">
        <serializedBean>
          <java class="java.beans.XMLDecoder">
            <object class="{$beanClass}"/>
          </java>
        </serializedBean>
      </screen>
    </xsl:if>
  </xsl:template>

  <xsl:template name="addBannerScreen">
    <xsl:param name="screenId"/>
    <xsl:param name="beanClass"/>
    <xsl:param name="finishScreen" select="false()"/>
    <xsl:variable name="screen" select="/install4j/installerGui/standardScreens/screen[@id=$screenId]"/>

    <screen beanClass="{$beanClass}">
      <xsl:if test="$finishScreen">
        <xsl:attribute name="finishScreen">true</xsl:attribute>
      </xsl:if>
      <serializedBean>
        <java class="java.beans.XMLDecoder">
          <object class="{$beanClass}">
            <xsl:if test="$screen/@useCustomBanner = 'true'">
              <void property="bannerBackground">
                <object class="java.awt.Color">

                  <xsl:variable name="background" select="$screen/@background"/>
                  <xsl:variable name="red" select="substring-before($background, ',')" />
                  <xsl:variable name="afterRed" select="substring-after($background, ',')" />
                  <xsl:variable name="green" select="substring-before($afterRed, ',')" />
                  <xsl:variable name="blue" select="substring-after($afterRed, ',')" />

                  <int><xsl:value-of select="$red"/></int>
                  <int><xsl:value-of select="$green"/></int>
                  <int><xsl:value-of select="$blue"/></int>
                  <int>255</int>
                </object>
              </void>
              <void property="bannerImageFile">
                <object class="com.install4j.api.beans.ExternalFile">
                  <string><xsl:value-of select="$screen/@bannerImageFile"/></string>
                </object>
              </void>
            </xsl:if>
          </object>
        </java>
      </serializedBean>
    </screen>
  </xsl:template>

  <xsl:template name="addLicenseScreen">
    <xsl:variable name="screen" select="/install4j/installerGui/standardScreens/screen[@id ='license']"/>
    <xsl:if test="$screen/@enabled = 'true'">
      <screen beanClass="com.install4j.runtime.beans.screens.LicenseScreen">
        <serializedBean>
          <java class="java.beans.XMLDecoder">
            <object class="com.install4j.runtime.beans.screens.LicenseScreen">
              <void property="displayedTextFile">
                <object class="com.install4j.api.beans.ExternalFile">
                  <string><xsl:value-of select="$screen/@file"/></string>
                </object>
              </void>
            </object>
          </java>
        </serializedBean>
      </screen>
    </xsl:if>
  </xsl:template>

  <xsl:template name="addLocationScreen">
    <xsl:variable name="screen" select="/install4j/installerGui/standardScreens/screen[@id ='location']"/>
    <xsl:if test="$screen/@enabled = 'true'">
      <screen beanClass="com.install4j.runtime.beans.screens.InstallationDirectoryScreen">
        <serializedBean>
          <java class="java.beans.XMLDecoder">
            <object class="com.install4j.runtime.beans.screens.InstallationDirectoryScreen">
              <void property="showRequiredDiskSpace">
                <boolean><xsl:value-of select="$screen/@showSpace"/></boolean>
              </void>
              <void property="suggestAppDir">
                <boolean><xsl:value-of select="$screen/@suggestAppDir"/></boolean>
              </void>
            </object>
          </java>
        </serializedBean>
      </screen>
    </xsl:if>
  </xsl:template>

  <xsl:template name="addInfoScreen">
    <xsl:param name="screenId"/>
    <xsl:variable name="screen" select="/install4j/installerGui/standardScreens/screen[@id=$screenId]"/>
    <xsl:if test="$screen/@enabled = 'true'">
      <screen beanClass="com.install4j.runtime.beans.screens.DefaultInfoScreen">
        <serializedBean>
          <java class="java.beans.XMLDecoder">
            <object class="com.install4j.runtime.beans.screens.DefaultInfoScreen">
              <void property="displayedTextFile">
                <object class="com.install4j.api.beans.ExternalFile">
                  <string><xsl:value-of select="$screen/@file"/></string>
                </object>
              </void>
            </object>
          </java>
        </serializedBean>
      </screen>
    </xsl:if>
  </xsl:template>

  <xsl:template name="addInstallationScreen">
    <screen beanClass="com.install4j.runtime.beans.screens.InstallationScreen" rollbackBarrier="true">
      <serializedBean>
        <java class="java.beans.XMLDecoder">
          <object class="com.install4j.runtime.beans.screens.InstallationScreen" />
        </java>
      </serializedBean>
      <actions>
          <xsl:call-template name="addInstallationActions"/>
      </actions>
    </screen>
  </xsl:template>

  <xsl:template name="addInstallationActions">
    <xsl:if test="/install4j/installerGui/@runUninstallerOnUpdate = 'true'">
      <xsl:call-template name="addSimpleAction" >
        <xsl:with-param name="beanClass" select="'com.install4j.runtime.beans.actions.UninstallPreviousAction'"/>
      </xsl:call-template>
    </xsl:if>
    <xsl:call-template name="addSimpleAction" >
      <xsl:with-param name="beanClass" select="'com.install4j.runtime.beans.actions.InstallFilesAction'"/>
    </xsl:call-template>
    <xsl:call-template name="addCreateProgramGroupAction"/>
    <xsl:call-template name="addRegisterAddRemoveAction"/>
    <xsl:for-each select="/install4j/installerGui/standardScreens/screen[@id ='fileAssociations']/associations/association">
      <xsl:call-template name="addCreateFileAssociationAction"/>
    </xsl:for-each>
    <xsl:for-each select="/install4j/launchers/launcher[executable/@executableMode='3']">
      <xsl:call-template name="addServiceActions"/>
    </xsl:for-each>

  </xsl:template>

  <xsl:template name="addSimpleAction">
    <xsl:param name="beanClass"/>
    <action beanClass="{$beanClass}">
      <serializedBean>
        <java class="java.beans.XMLDecoder">
          <object class="{$beanClass}" />
        </java>
      </serializedBean>
    </action>

  </xsl:template>

  <xsl:template name="addCreateProgramGroupAction">
    <action beanClass="com.install4j.runtime.beans.actions.desktop.CreateProgramGroupAction">
      <serializedBean>
        <java class="java.beans.XMLDecoder">
          <object class="com.install4j.runtime.beans.actions.desktop.CreateProgramGroupAction">
            <void property="programGroupName">
              <string>${compiler:sys.fullName}</string>
            </void>
          </object>
        </java>
      </serializedBean>
    </action>
  </xsl:template>

  <xsl:template name="addRegisterAddRemoveAction">
    <action beanClass="com.install4j.runtime.beans.actions.desktop.RegisterAddRemoveAction">
      <serializedBean>
        <java class="java.beans.XMLDecoder">
          <object class="com.install4j.runtime.beans.actions.desktop.RegisterAddRemoveAction">
            <void property="itemName">
              <string>${compiler:sys.fullName} ${compiler:sys.version}</string>
            </void>
          </object>
        </java>
      </serializedBean>
    </action>

  </xsl:template>

  <xsl:template name="addCreateFileAssociationAction">
    <action name="" beanClass="com.install4j.runtime.beans.actions.desktop.CreateFileAssociationAction">
      <serializedBean>
        <java class="java.beans.XMLDecoder">
          <object class="com.install4j.runtime.beans.actions.desktop.CreateFileAssociationAction">
            <void property="description">
              <string><xsl:value-of select="@description"/></string>
            </void>
            <void property="extension">
              <string><xsl:value-of select="@extension"/></string>
            </void>
            <void property="launcherId">
              <string><xsl:value-of select="@launcherId"/></string>
            </void>
            <void property="mac">
              <boolean><xsl:value-of select="@macos"/></boolean>
            </void>
            <void property="macIconFile">
              <object class="com.install4j.api.beans.ExternalFile">
                <string><xsl:value-of select="@macIconFile"/></string>
              </object>
            </void>
            <void property="selected">
              <boolean><xsl:value-of select="@selected"/></boolean>
            </void>
            <void property="windows">
              <boolean><xsl:value-of select="@windows"/></boolean>
            </void>
            <void property="windowsIconFile">
              <object class="com.install4j.api.beans.ExternalFile">
                <string><xsl:value-of select="@winIconFile"/></string>
              </object>
            </void>
          </object>
        </java>
      </serializedBean>
    </action>
  </xsl:template>

  <xsl:template name="addServiceActions">
    <action beanClass="com.install4j.runtime.beans.actions.services.InstallServiceAction">
      <serializedBean>
        <java class="java.beans.XMLDecoder">
          <object class="com.install4j.runtime.beans.actions.services.InstallServiceAction">
            <void property="allowUserChangeStartType">
              <boolean><xsl:value-of select="@allowUserChangeServiceStartType"/></boolean>
            </void>
            <void property="launcherId">
              <string><xsl:value-of select="@id"/></string>
            </void>
            <void property="selected">
              <boolean>
                <xsl:variable name="servicesScreen" select="/install4j/installerGui/standardScreens/screen[@id ='services']"/>
                <xsl:value-of select="string($servicesScreen/@allSelected or $servicesScreen/selectedServiceLaunchers/launcher[@refId = current()/@id])"/></boolean>
            </void>
          </object>
        </java>
      </serializedBean>
    </action>
    <action beanClass="com.install4j.runtime.beans.actions.services.StartServiceAction">
      <serializedBean>
        <java class="java.beans.XMLDecoder">
          <object class="com.install4j.runtime.beans.actions.services.StartServiceAction">
            <void property="launcherId">
              <string><xsl:value-of select="@id"/></string>
            </void>
          </object>
        </java>
      </serializedBean>
    </action>
  </xsl:template>

  <xsl:template name="addUninstallerScreens">
    <uninstallerScreens>
      <screen beanClass="com.install4j.runtime.beans.screens.UninstallWelcomeScreen">
        <serializedBean>
          <java class="java.beans.XMLDecoder">
            <object class="com.install4j.runtime.beans.screens.UninstallWelcomeScreen" />
          </java>
        </serializedBean>
      </screen>
      <screen beanClass="com.install4j.runtime.beans.screens.UninstallationScreen">
        <serializedBean>
          <java class="java.beans.XMLDecoder">
            <object class="com.install4j.runtime.beans.screens.UninstallationScreen" />
          </java>
        </serializedBean>
        <actions>
          <action beanClass="com.install4j.runtime.beans.actions.UninstallFilesAction">
            <serializedBean>
              <java class="java.beans.XMLDecoder">
                <object class="com.install4j.runtime.beans.actions.UninstallFilesAction" />
              </java>
            </serializedBean>
          </action>
        </actions>
      </screen>
      <screen beanClass="com.install4j.runtime.beans.screens.UninstallFailureScreen" finishScreen="true">
        <serializedBean>
          <java class="java.beans.XMLDecoder">
            <object class="com.install4j.runtime.beans.screens.UninstallFailureScreen" />
          </java>
        </serializedBean>
      </screen>
      <screen beanClass="com.install4j.runtime.beans.screens.UninstallSuccessScreen" finishScreen="true">
        <serializedBean>
          <java class="java.beans.XMLDecoder">
            <object class="com.install4j.runtime.beans.screens.UninstallSuccessScreen" />
          </java>
        </serializedBean>
      </screen>
    </uninstallerScreens>

  </xsl:template>

  <xsl:template name="addStartupScreen">
    <screen beanClass="com.install4j.runtime.beans.screens.StartupScreen">
      <serializedBean>
        <java class="java.beans.XMLDecoder">
          <object class="com.install4j.runtime.beans.screens.StartupScreen" />
        </java>
      </serializedBean>

      <xsl:if test="/install4j/launchers/launcher/executable[@executableMode='3']">
        <actions>
          <action beanClass="com.install4j.runtime.beans.actions.misc.RequireAdminAction" failureStrategy="2">
            <serializedBean>
              <java class="java.beans.XMLDecoder">
                <object class="com.install4j.runtime.beans.actions.misc.RequireAdminAction" />
              </java>
            </serializedBean>
          </action>
        </actions>
      </xsl:if>

    </screen>
  </xsl:template>

  <xsl:template match="/install4j/mediaSets/*">
    <xsl:variable name="mediaSet" select="." />

    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:variable name="languageId">
        <xsl:choose>
          <xsl:when test="@languageID">
            <xsl:value-of select="@languageID"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="'en'"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <xsl:variable name="macosSingleBundle" select="local-name(.) = 'macos'"/>
      <xsl:if test="$macosSingleBundle">
        <xsl:attribute name="launcherId"><xsl:value-of select="$mediaSet/selectedLaunchers/launcher/@id"/></xsl:attribute>
      </xsl:if>
      <xsl:attribute name="overridePrincipalLanguage">true</xsl:attribute>

      <overriddenPrincipalLanguage id="{$languageId}"/>

      <xsl:if test="@allLaunchers='false' and not($macosSingleBundle)">
        <excludedLaunchers>
          <xsl:for-each select="/install4j/launchers/launcher">
            <xsl:if test="not($mediaSet/selectedLaunchers/launcher[@id=current()/@id])">
              <launcher id="{@id}"/>
            </xsl:if>
          </xsl:for-each>
        </excludedLaunchers>
      </xsl:if>

      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>


</xsl:transform>
