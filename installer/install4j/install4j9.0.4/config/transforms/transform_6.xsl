<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- install4j 7.0 -->
<xsl:transform version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns=""
               xmlns:math="http://exslt.org/math"
               xmlns:ext="com.install4j.config.XsltExtensions"
               extension-element-prefixes="ext math">

  <xsl:include href="common.xsl"/>

  <xsl:template match="//action[@beanClass='com.install4j.runtime.beans.actions.desktop.CreateQuicklaunchIconAction']"/>

  <xsl:template match="//object[@class='com.install4j.runtime.beans.actions.desktop.CreateProgramGroupAction']">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:if test="not(void[@property='addUninstaller'])">
        <void property="addUninstaller">
          <boolean>true</boolean>
        </void>
      </xsl:if>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="//object/void[@property='labelFont']">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
    <void property="labelFontType">
      <object class="java.lang.Enum" method="valueOf">
        <class>com.install4j.runtime.beans.formcomponents.FontType</class>
        <string>CUSTOM</string>
      </object>
    </void>
  </xsl:template>

  <xsl:template match="//object/void[@property='valueLabelFont']">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
    <void property="valueLabelFontType">
      <object class="java.lang.Enum" method="valueOf">
        <class>com.install4j.runtime.beans.formcomponents.FontType</class>
        <string>CUSTOM</string>
      </object>
    </void>
  </xsl:template>

  <xsl:template match="//object/void[@property='textFont']">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
    <void property="textFontType">
      <object class="java.lang.Enum" method="valueOf">
        <class>com.install4j.runtime.beans.formcomponents.FontType</class>
        <string>CUSTOM</string>
      </object>
    </void>
  </xsl:template>

  <xsl:template match="//applications/application">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:if test="serializedBean/java/object/void[
        @property='customHeaderImage'
        or @property='headerBackground'
        or @property='headerForeground'
        or @property='headerIconAnchor'
        or @property='headerIconOverlap'
        or @property='customWatermarkText'
        or @property='watermark'
      ]">
        <xsl:attribute name="styleName">Standard</xsl:attribute>
        <styleOverrides>
          <xsl:if test="serializedBean/java/object/void[
            @property='customHeaderImage'
            or @property='headerBackground'
            or @property='headerForeground'
            or @property='headerIconAnchor'
            or @property='headerIconOverlap'
          ]">
            <styleOverride name="Customize title bar" enabled="true">
              <group id="#" beanClass="com.install4j.runtime.beans.groups.VerticalFormComponentGroup">
                <serializedBean>
                  <java class="java.beans.XMLDecoder">
                    <object class="com.install4j.runtime.beans.groups.VerticalFormComponentGroup">
                      <void property="backgroundColor">
                        <xsl:choose>
                          <xsl:when test="serializedBean/java/object/void[@property='headerBackground']">
                            <xsl:copy-of select="serializedBean/java/object/void[@property='headerBackground']/object"/>
                          </xsl:when>
                          <xsl:otherwise>
                            <object class="java.awt.Color">
                              <int>255</int>
                              <int>255</int>
                              <int>255</int>
                              <int>255</int>
                            </object>
                          </xsl:otherwise>
                        </xsl:choose>
                      </void>
                      <xsl:if test="serializedBean/java/object/void[@property='headerForeground']">
                        <void property="foregroundColor">
                          <xsl:copy-of select="serializedBean/java/object/void[@property='headerForeground']/object"/>
                        </void>
                      </xsl:if>
                      <void property="imageAnchor">
                        <xsl:choose>
                          <xsl:when test="serializedBean/java/object/void[@property='headerIconAnchor']">
                            <xsl:copy-of select="serializedBean/java/object/void[@property='headerIconAnchor']/object"/>
                          </xsl:when>
                          <xsl:otherwise>
                            <object class="java.lang.Enum" method="valueOf">
                              <class>com.install4j.api.beans.Anchor</class>
                              <string>NORTHEAST</string>
                            </object>
                          </xsl:otherwise>
                        </xsl:choose>
                      </void>
                      <void property="imageFile">
                        <xsl:choose>
                          <xsl:when test="serializedBean/java/object/void[@property='customHeaderImage']">
                            <xsl:copy-of select="serializedBean/java/object/void[@property='customHeaderImage']/object"/>
                          </xsl:when>
                          <xsl:otherwise>
                            <object class="com.install4j.api.beans.ExternalFile">
                              <string>icon:${installer:sys.installerApplicationMode}_header.png</string>
                            </object>
                          </xsl:otherwise>
                        </xsl:choose>
                      </void>
                      <void property="imageOverlap">
                        <xsl:choose>
                          <xsl:when test="serializedBean/java/object/void[@property='headerIconOverlap']">
                            <xsl:copy-of select="serializedBean/java/object/void[@property='headerIconOverlap']/boolean"/>
                          </xsl:when>
                          <xsl:otherwise>
                            <boolean>false</boolean>
                          </xsl:otherwise>
                        </xsl:choose>
                      </void>
                    </object>
                  </java>
                </serializedBean>
              </group>
            </styleOverride>
          </xsl:if>
          <xsl:if test="serializedBean/java/object/void[
            @property='customWatermarkText'
            or @property='watermark'
          ]">
            <styleOverride name="Custom watermark" enabled="true">
              <formComponent name="Watermark" id="#" beanClass="com.install4j.runtime.beans.formcomponents.SeparatorComponent">
                <serializedBean>
                  <java class="java.beans.XMLDecoder">
                    <object class="com.install4j.runtime.beans.formcomponents.SeparatorComponent">
                      <void property="labelText">
                        <xsl:choose>
                          <xsl:when test="serializedBean/java/object/void[@property='watermark']/boolean/text()='false'">
                            <string/>
                          </xsl:when>
                          <xsl:otherwise>
                            <xsl:copy-of select="serializedBean/java/object/void[@property='customWatermarkText']/string"/>
                          </xsl:otherwise>
                        </xsl:choose>
                      </void>
                    </object>
                  </java>
                </serializedBean>
              </formComponent>
            </styleOverride>
          </xsl:if>
        </styleOverrides>
      </xsl:if>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="//object[
    @class='com.install4j.runtime.beans.applications.InstallerApplication'
    or @class='com.install4j.runtime.beans.applications.UninstallerApplication'
    or @class='com.install4j.runtime.beans.applications.CustomApplication'
  ]">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates select="*[
        @property!='customHeaderImage'
        and @property!='headerBackground'
        and @property!='headerForeground'
        and @property!='headerIconAnchor'
        and @property!='headerIconOverlap'
        and @property!='customWatermarkText'
        and @property!='watermark'
      ]"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="//screen[
    @beanClass='com.install4j.runtime.beans.screens.CustomizableBannerScreen'
    or @beanClass='com.install4j.runtime.beans.screens.BannerFormScreen'
    or @beanClass='com.install4j.runtime.beans.screens.UninstallSuccessScreen'
    or @beanClass='com.install4j.runtime.beans.screens.FinishedScreen'
    or @beanClass='com.install4j.runtime.beans.screens.UninstallWelcomeScreen'
    or @beanClass='com.install4j.runtime.beans.screens.WelcomeScreen'
  ]">
    <xsl:copy>
      <xsl:attribute name="styleName">Banner</xsl:attribute>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:if test="serializedBean/java/object/void[
        @property='bannerBackground'
        or @property='bannerImageFile'
        or @property='bannerImageAnchor'
      ]">
        <styleOverrides>
          <styleOverride name="Customize banner image" enabled="true">
            <group id="#" beanClass="com.install4j.runtime.beans.groups.VerticalFormComponentGroup">
              <serializedBean>
                <java class="java.beans.XMLDecoder">
                  <object class="com.install4j.runtime.beans.groups.VerticalFormComponentGroup">
                    <void property="imageAnchor">
                      <xsl:choose>
                        <xsl:when test="serializedBean/java/object/void[@property='bannerImageAnchor']/object/string/text()='CENTER'">
                          <object class="java.lang.Enum" method="valueOf">
                            <class>com.install4j.api.beans.Anchor</class>
                            <string>WEST</string>
                          </object>
                        </xsl:when>
                        <xsl:when test="serializedBean/java/object/void[@property='bannerImageAnchor']">
                          <xsl:copy-of select="serializedBean/java/object/void[@property='bannerImageAnchor']/object"/>
                        </xsl:when>
                        <xsl:otherwise>
                          <object class="java.lang.Enum" method="valueOf">
                            <class>com.install4j.api.beans.Anchor</class>
                            <string>NORTHWEST</string>
                          </object>
                        </xsl:otherwise>
                      </xsl:choose>
                    </void>
                    <void property="imageEdgeBackgroundColor">
                      <xsl:choose>
                        <xsl:when test="serializedBean/java/object/void[@property='bannerBackground']">
                          <xsl:copy-of select="serializedBean/java/object/void[@property='bannerBackground']/object"/>
                        </xsl:when>
                        <xsl:otherwise>
                          <object class="java.awt.Color">
                            <int>25</int>
                            <int>143</int>
                            <int>220</int>
                            <int>255</int>
                          </object>
                        </xsl:otherwise>
                      </xsl:choose>
                    </void>
                    <void property="imageFile">
                      <xsl:choose>
                        <xsl:when test="serializedBean/java/object/void[@property='bannerImageFile']">
                          <xsl:copy-of select="serializedBean/java/object/void[@property='bannerImageFile']/object"/>
                        </xsl:when>
                        <xsl:otherwise>
                          <object class="com.install4j.api.beans.ExternalFile">
                            <string>${compiler:sys.install4jHome}/resource/styles/wizard.png</string>
                          </object>
                        </xsl:otherwise>
                      </xsl:choose>
                    </void>
                  </object>
                </java>
              </serializedBean>
            </group>
          </styleOverride>
        </styleOverrides>
      </xsl:if>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="//object[
    @class='com.install4j.runtime.beans.screens.CustomizableBannerScreen'
    or @class='com.install4j.runtime.beans.screens.BannerFormScreen'
    or @class='com.install4j.runtime.beans.screens.UninstallSuccessScreen'
    or @class='com.install4j.runtime.beans.screens.FinishedScreen'
    or @class='com.install4j.runtime.beans.screens.UninstallWelcomeScreen'
    or @class='com.install4j.runtime.beans.screens.WelcomeScreen'
  ]">
    <xsl:copy>
      <xsl:apply-templates select="@*|*[
        @property!='bannerBackground'
        and @property!='bannerImageFile'
        and @property!='bannerImageAnchor'
      ]"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="//object[
    @class='com.install4j.runtime.beans.formcomponents.DirectoryChooserComponent'
    or @class='com.install4j.runtime.beans.formcomponents.InstallationDirectoryChooserComponent'
    or @class='com.install4j.runtime.beans.screens.CustomizableDirectoryScreen'
    or @class='com.install4j.runtime.beans.screens.InstallationDirectoryScreen'
    or @class='com.install4j.runtime.beans.screens.ComponentsScreen']/void[@property='allowNewFolderCreation']"/>

  <xsl:template match="//object[
    @class='com.install4j.runtime.beans.screens.InstallationDirectoryScreen']
      /void[
    @property='suggestAppDir'
    or @property='existingDirWarning'
    or @property='validateApplicationId'
    or @property='checkWritable'
    or @property='manualEntryAllowed'
    or @property='checkFreeSpace'
    or @property='showRequiredDiskSpace'
    or @property='showFreeDiskSpace'
    or @property='allowSpacesOnUnix'
    or @property='validationScript'
    or @property='standardValidation'
    ]"/>

  <xsl:template match="screen[@beanClass='com.install4j.runtime.beans.screens.InstallationDirectoryScreen']/formComponents">
    <formComponents reinitialize="true">
      <formComponent beanClass="com.install4j.runtime.beans.formcomponents.InstallationDirectoryChooserComponent">
        <serializedBean>
          <java class="java.beans.XMLDecoder">
            <object class="com.install4j.runtime.beans.formcomponents.InstallationDirectoryChooserComponent">
              <xsl:copy-of select="../serializedBean/java/object/void[
                @property='suggestAppDir'
                or @property='existingDirWarning'
                or @property='validateApplicationId'
                or @property='checkWritable'
                or @property='manualEntryAllowed'
                or @property='checkFreeSpace'
                or @property='showRequiredDiskSpace'
                or @property='showFreeDiskSpace'
                or @property='allowSpacesOnUnix'
                or @property='validationScript'
                or @property='standardValidation'
              ]"/>
            </object>
          </java>
        </serializedBean>
      </formComponent>
    </formComponents>
  </xsl:template>

  <xsl:template match="//object[@class='com.install4j.runtime.beans.screens.WelcomeScreen']/void[@property='updateCheck']"/>

  <xsl:template match="screen[@beanClass='com.install4j.runtime.beans.screens.WelcomeScreen']/formComponents">
    <formComponents reinitialize="true">
      <formComponent beanClass="com.install4j.runtime.beans.formcomponents.UpdateAlertComponent">
        <serializedBean>
          <java class="java.beans.XMLDecoder">
            <object class="com.install4j.runtime.beans.formcomponents.UpdateAlertComponent">
              <xsl:copy-of select="../serializedBean/java/object/void[@property='updateCheck']"/>
            </object>
          </java>
        </serializedBean>
      </formComponent>
      <xsl:apply-templates/>
    </formComponents>
  </xsl:template>

  <xsl:template match="//@*[.='com.install4j.runtime.beans.screens.CustomizableBannerScreen']">
    <xsl:attribute name="{name(.)}">
      <xsl:text>com.install4j.runtime.beans.screens.BannerFormScreen</xsl:text>
    </xsl:attribute>
  </xsl:template>

  <xsl:template match="//object[
    @class='com.install4j.runtime.beans.screens.BannerFormScreen'
    or @class='com.install4j.runtime.beans.screens.CustomizableBannerScreen'
    ]/void[@property='infoText']"/>

  <xsl:template match="screen[
    @beanClass='com.install4j.runtime.beans.screens.BannerFormScreen'
    or @beanClass='com.install4j.runtime.beans.screens.CustomizableBannerScreen'
  ]/formComponents">
    <formComponents reinitialize="true">
      <xsl:call-template name="addInfoTextComponent"/>
      <xsl:apply-templates/>
    </formComponents>
  </xsl:template>

  <xsl:template match="screen[
    @beanClass='com.install4j.runtime.beans.screens.FinishedScreen'
    or @beanClass='com.install4j.runtime.beans.screens.UninstallWelcomeScreen'
    or @beanClass='com.install4j.runtime.beans.screens.UninstallSuccessScreen'
    or @beanClass='com.install4j.runtime.beans.screens.AdditionalConfirmationsScreen'
    ]/formComponents">
    <formComponents reinitialize="true">
      <xsl:apply-templates/>
    </formComponents>
  </xsl:template>

  <xsl:template match="//object[
    @class='com.install4j.runtime.beans.screens.DefaultInfoScreen'
    or @class='com.install4j.runtime.beans.screens.CustomizableInfoScreen'
    ]/void[
    @property='textSource'
    or @property='displayedText'
    or @property='displayedTextFile'
    or @property='variableName'
    or @property='infoText'
  ]"/>

  <xsl:template match="//object[
    @class='com.install4j.runtime.beans.screens.CustomizableInfoScreen'
    or @class='com.install4j.runtime.beans.screens.CustomizableProgramGroupScreen'
    or @class='com.install4j.extensions.pdf.PdfScreen'
  ]">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <void property="scrollable">
        <boolean>false</boolean>
      </void>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="screen[
    @beanClass='com.install4j.runtime.beans.screens.DefaultInfoScreen'
    or @beanClass='com.install4j.runtime.beans.screens.CustomizableInfoScreen'
    ]/formComponents">
    <formComponents reinitialize="true">
      <xsl:if test="../@beanClass='com.install4j.runtime.beans.screens.CustomizableInfoScreen'">
        <xsl:call-template name="addInfoTextComponent"/>
      </xsl:if>
      <formComponent beanClass="com.install4j.runtime.beans.formcomponents.HtmlDisplayFormComponent">
        <serializedBean>
          <java class="java.beans.XMLDecoder">
            <object class="com.install4j.runtime.beans.formcomponents.HtmlDisplayFormComponent">
              <xsl:copy-of select="../serializedBean/java/object/void[
                @property='textSource'
                or @property='displayedText'
                or @property='displayedTextFile'
                or @property='variableName'
              ]"/>
            </object>
          </java>
        </serializedBean>
      </formComponent>
      <xsl:apply-templates/>
    </formComponents>
  </xsl:template>

  <xsl:template name="addInfoTextComponent">
    <formComponent beanClass="com.install4j.runtime.beans.formcomponents.MultilineLabelComponent">
      <serializedBean>
        <java class="java.beans.XMLDecoder">
          <object class="com.install4j.runtime.beans.formcomponents.MultilineLabelComponent">
            <void property="labelText">
              <xsl:copy-of select="../serializedBean/java/object/void[@property='infoText']/string"/>
            </void>
          </object>
        </java>
      </serializedBean>
    </formComponent>
  </xsl:template>

  <xsl:template match="//object[@class='com.install4j.runtime.beans.screens.LicenseScreen']/void[
    @property='textSource'
    or @property='displayedText'
    or @property='displayedTextFile'
    or @property='variableName'
    or @property='acceptInitiallySelected'
    or @property='readAllRequired'
  ]"/>

  <xsl:template match="screen[@beanClass='com.install4j.runtime.beans.screens.LicenseScreen']/formComponents">
    <formComponents reinitialize="true">
      <formComponent beanClass="com.install4j.runtime.beans.formcomponents.LicenseComponent">
        <serializedBean>
          <java class="java.beans.XMLDecoder">
            <object class="com.install4j.runtime.beans.formcomponents.LicenseComponent">
              <xsl:copy-of select="../serializedBean/java/object/void[
                @property='textSource'
                or @property='displayedText'
                or @property='displayedTextFile'
                or @property='variableName'
                or @property='acceptInitiallySelected'
                or @property='readAllRequired'
              ]"/>
            </object>
          </java>
        </serializedBean>
      </formComponent>
      <xsl:apply-templates/>
    </formComponents>
  </xsl:template>

  <xsl:template match="//object[@class='com.install4j.runtime.beans.screens.CustomizableDirectoryScreen']/void[
    @property='variableName'
    or @property='initialDirectory'
    or @property='standardDirectoryName'
    or @property='onlyWritable'
    or @property='directoryDescription'
    or @property='infoText'
    or @property='allowSpacesOnUnix'
    or @property='manualEntryAllowed'
    or @property='allowEmpty'
    or @property='standardValidation'
    or @property='validationScript'
  ]"/>

  <xsl:template match="screen[@beanClass='com.install4j.runtime.beans.screens.CustomizableDirectoryScreen']/formComponents">
    <formComponents reinitialize="true">
      <xsl:call-template name="addInfoTextComponent"/>
      <formComponent beanClass="com.install4j.runtime.beans.formcomponents.DirectoryChooserComponent">
        <serializedBean>
          <java class="java.beans.XMLDecoder">
            <object class="com.install4j.runtime.beans.formcomponents.DirectoryChooserComponent">
              <xsl:copy-of select="../serializedBean/java/object/void[
                @property='variableName'
                or @property='standardDirectoryName'
                or @property='onlyWritable'
                or @property='directoryDescription'
                or @property='allowSpacesOnUnix'
                or @property='manualEntryAllowed'
                or @property='allowEmpty'
                or @property='standardValidation'
                or @property='validationScript'
              ]"/>
              <xsl:if test="../serializedBean/java/object/void[@property='initialDirectory']">
                <void property="initialFile">
                  <xsl:copy-of select="../serializedBean/java/object/void[@property='initialDirectory']/string"/>
                </void>
              </xsl:if>
            </object>
          </java>
        </serializedBean>
      </formComponent>
      <xsl:apply-templates/>
    </formComponents>
  </xsl:template>

  <xsl:template match="//object[@class='com.install4j.runtime.beans.screens.CustomizableProgramGroupScreen']/void[
    @property='variableName'
    or @property='initialProgramGroup'
    or @property='allUsers'
    or @property='showWarningIfExists'
    or @property='infoText'
  ]"/>

  <xsl:template match="screen[@beanClass='com.install4j.runtime.beans.screens.CustomizableProgramGroupScreen']/condition">
    <condition>Util.isWindows()</condition>
  </xsl:template>

  <xsl:template match="screen[@beanClass='com.install4j.runtime.beans.screens.CustomizableProgramGroupScreen']/formComponents">
    <formComponents reinitialize="true">
      <xsl:call-template name="addInfoTextComponent"/>
      <formComponent beanClass="com.install4j.runtime.beans.formcomponents.ProgramGroupComponent">
        <serializedBean>
          <java class="java.beans.XMLDecoder">
            <object class="com.install4j.runtime.beans.formcomponents.ProgramGroupComponent">
              <xsl:copy-of select="../serializedBean/java/object/void[
                @property='variableName'
                or @property='initialProgramGroup'
                or @property='allUsers'
                or @property='showWarningIfExists'
              ]"/>
            </object>
          </java>
        </serializedBean>
      </formComponent>
      <xsl:apply-templates/>
    </formComponents>
  </xsl:template>

  <xsl:template match="//screen[@beanClass='com.install4j.runtime.beans.screens.CustomizableProgressScreen']/preActivation">
    <xsl:copy>
      <xsl:if test="../serializedBean/java/object/void[@property='cancelEnabled']/boolean/text()='false'">
        <xsl:text>context.getWizardContext().setControlButtonEnabled(ControlButtonType.CANCEL, false);&#xa;</xsl:text>
      </xsl:if>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="//screen[@beanClass='com.install4j.runtime.beans.screens.CustomizableProgressScreen']/postActivation">
    <xsl:copy>
      <xsl:text>context.getWizardContext().setControlButtonVisible(ControlButtonType.NEXT, false);&#xa;context.getWizardContext().setControlButtonVisible(ControlButtonType.PREVIOUS, false);&#xa;context.goForward(1, true, true);&#xa;</xsl:text>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="//object[@class='com.install4j.runtime.beans.screens.CustomizableProgressScreen']/void[
    @property='initialStatusMessage'
    or @property='cancelEnabled'
  ]"/>

  <xsl:template match="screen[
    @beanClass='com.install4j.runtime.beans.screens.CustomizableProgressScreen'
    or @beanClass='com.install4j.runtime.beans.screens.InstallationScreen'
    or @beanClass='com.install4j.runtime.beans.screens.UninstallationScreen'
    ]/formComponents">
    <formComponents reinitialize="true">
      <formComponent beanClass="com.install4j.runtime.beans.formcomponents.ProgressComponent">
        <serializedBean>
          <java class="java.beans.XMLDecoder">
            <object class="com.install4j.runtime.beans.formcomponents.ProgressComponent">
              <xsl:copy-of select="../serializedBean/java/object/void[
                @property='initialStatusMessage'
              ]"/>
            </object>
          </java>
        </serializedBean>
      </formComponent>
      <xsl:apply-templates/>
    </formComponents>
  </xsl:template>

  <xsl:template match="//object[@class='com.install4j.extensions.pdf.PdfScreen']/void[@property='pdfFile']"/>

  <xsl:template match="screen[@beanClass='com.install4j.extensions.pdf.PdfScreen']/formComponents">
    <formComponents reinitialize="true">
      <formComponent beanClass="com.install4j.extensions.pdf.PdfComponent">
        <serializedBean>
          <java class="java.beans.XMLDecoder">
            <object class="com.install4j.extensions.pdf.PdfComponent">
              <xsl:copy-of select="../serializedBean/java/object/void[
                @property='pdfFile'
              ]"/>
            </object>
          </java>
        </serializedBean>
      </formComponent>
      <xsl:apply-templates/>
    </formComponents>
  </xsl:template>

  <xsl:template match="//object[@class='com.install4j.runtime.beans.screens.FileAssociationsScreen']/void[@property='showSelectionButtons']"/>

  <xsl:template match="screen[@beanClass='com.install4j.runtime.beans.screens.FileAssociationsScreen']/formComponents">
    <formComponents reinitialize="true">
      <formComponent beanClass="com.install4j.runtime.beans.formcomponents.FileAssociationsComponent">
        <serializedBean>
          <java class="java.beans.XMLDecoder">
            <object class="com.install4j.runtime.beans.formcomponents.FileAssociationsComponent">
              <xsl:copy-of select="../serializedBean/java/object/void[
                @property='showSelectionButtons'
              ]"/>
            </object>
          </java>
        </serializedBean>
      </formComponent>
      <xsl:apply-templates/>
    </formComponents>
  </xsl:template>

  <xsl:template match="//object[@class='com.install4j.runtime.beans.screens.ComponentsScreen']/void[
    @property='showInstallationDirectoryChooser'
    or @property='selectionChangedScript'
    or @property='showRequiredDiskSpace'
    or @property='showFreeDiskSpace'
    or @property='checkFreeSpace'
    or @property='suggestAppDir'
    or @property='existingDirWarning'
    or @property='validateApplicationId'
    or @property='checkWritable'
    or @property='allowSpacesOnUnix'
    or @property='validationScript'
    or @property='manualEntryAllowed'
    or @property='standardValidation'
    or @property='boldDescription'
    or @property='italicDescription'
    or @property='smallerDescription'
  ]"/>

  <xsl:template match="screen[@beanClass='com.install4j.runtime.beans.screens.ComponentsScreen']/formComponents">
    <formComponents reinitialize="true">
      <formComponent beanClass="com.install4j.runtime.beans.formcomponents.ComponentSelectorComponent">
        <serializedBean>
          <java class="java.beans.XMLDecoder">
            <object class="com.install4j.runtime.beans.formcomponents.ComponentSelectorComponent">
              <xsl:copy-of select="../serializedBean/java/object/void[
                @property='selectionChangedScript'
                or @property='boldDescription'
                or @property='italicDescription'
                or @property='smallerDescription'
              ]"/>
            </object>
          </java>
        </serializedBean>
      </formComponent>
      <xsl:if test="../serializedBean/java/object/void[@property='showInstallationDirectoryChooser']/boolean/text()='true'">
        <formComponent beanClass="com.install4j.runtime.beans.formcomponents.InstallationDirectoryChooserComponent">
          <serializedBean>
            <java class="java.beans.XMLDecoder">
              <object class="com.install4j.runtime.beans.formcomponents.InstallationDirectoryChooserComponent">
                <xsl:copy-of select="../serializedBean/java/object/void[
                  @property='showRequiredDiskSpace'
                  or @property='showFreeDiskSpace'
                  or @property='checkFreeSpace'
                  or @property='suggestAppDir'
                  or @property='existingDirWarning'
                  or @property='validateApplicationId'
                  or @property='checkWritable'
                  or @property='allowSpacesOnUnix'
                  or @property='validationScript'
                  or @property='manualEntryAllowed'
                  or @property='standardValidation'
                ]"/>
                <void property="labelText">
                  <string>${i18n:ReadyMemoDir}</string>
                </void>
              </object>
            </java>
          </serializedBean>
          <visibilityScript>!context.getBooleanVariable("sys.confirmedUpdateInstallation")</visibilityScript>
        </formComponent>
      </xsl:if>
      <xsl:apply-templates/>
    </formComponents>
  </xsl:template>

  <xsl:template match="screen[@name='Update message']/preActivation">
    <preActivation/>
  </xsl:template>
  <xsl:template match="screen[@name='Update message']/postActivation">
    <postActivation>
      <xsl:value-of select="../preActivation/text()"/>
    </postActivation>
  </xsl:template>

</xsl:transform>
