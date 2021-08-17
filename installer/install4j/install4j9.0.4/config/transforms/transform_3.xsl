<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- install4j 5.0 -->
<xsl:transform version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns=""
               xmlns:math="http://exslt.org/math"
               xmlns:ext="com.install4j.config.XsltExtensions"
               extension-element-prefixes="ext math">

  <xsl:include href="common.xsl"/>

  <xsl:variable name="requestPrivilegesId" select="ext:getNextId($maxId)"/>

  <xsl:template match="scriptLines">
    <content><xsl:for-each select="line">
      <xsl:value-of select="@content"/>
      <xsl:if test="following-sibling::line">
        <xsl:text>&#10;</xsl:text>
      </xsl:if>
    </xsl:for-each></content>
  </xsl:template>

  <xsl:template match="//action[@beanClass='com.install4j.runtime.beans.actions.misc.RequireAdminAction']"/>

  <xsl:template match="/install4j/installerGui/applications/application[@id='installer']/startup/screen[1]/actions">
    <actions>
      <action id="{$requestPrivilegesId}" beanClass="com.install4j.runtime.beans.actions.misc.RequestPrivilegesAction">
        <serializedBean>
          <java class="java.beans.XMLDecoder">
            <object class="com.install4j.runtime.beans.actions.misc.RequestPrivilegesAction">
              <xsl:if test="/install4j/installerGui/applications/application[@id='installer']//action[@beanClass='com.install4j.runtime.beans.actions.misc.RequireAdminAction']">
                <void property="failIfNotObtainedWin">
                  <boolean>true</boolean>
                </void>
                <void property="failIfNotObtainedMac">
                  <boolean>true</boolean>
                </void>
                <void property="failIfNotRootUnix">
                  <boolean>true</boolean>
                </void>
              </xsl:if>
            </object>
          </java>
        </serializedBean>
      </action>

      <xsl:apply-templates/>

    </actions>
  </xsl:template>

  <xsl:template match="/install4j/installerGui/applications/application[@id='uninstaller']/startup/screen[1]/actions">
    <actions>
      <xsl:apply-templates/>
      <xsl:if test="not(action[@beanClass='com.install4j.runtime.beans.actions.misc.LoadResponseFileAction'])">
        <action beanClass="com.install4j.runtime.beans.actions.misc.LoadResponseFileAction">
          <xsl:call-template name="addIdAttribute"/>
          <serializedBean>
            <java class="java.beans.XMLDecoder">
              <object class="com.install4j.runtime.beans.actions.misc.LoadResponseFileAction" />
            </java>
          </serializedBean>
        </action>
      </xsl:if>
      <action beanClass="com.install4j.runtime.beans.actions.misc.RequireInstallerPrivilegesAction">
        <xsl:call-template name="addIdAttribute"/>
        <serializedBean>
          <java class="java.beans.XMLDecoder">
            <object class="com.install4j.runtime.beans.actions.misc.RequireInstallerPrivilegesAction"/>
          </java>
        </serializedBean>
      </action>
    </actions>
  </xsl:template>

  <xsl:template match="//action[@beanClass='com.install4j.runtime.beans.actions.text.AppendToFileAction']">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:attribute name="beanClass">com.install4j.runtime.beans.actions.text.WriteTextFileAction</xsl:attribute>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="//object[@class='com.install4j.runtime.beans.actions.text.AppendToFileAction']">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:attribute name="class">com.install4j.runtime.beans.actions.text.WriteTextFileAction</xsl:attribute>

      <void property="append">
        <boolean>true</boolean>
      </void>

      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="//object[
      @class='com.install4j.runtime.beans.applications.InstallerApplication' or
      @class='com.install4j.runtime.beans.applications.UninstallerApplication' or
      @class='com.install4j.runtime.beans.applications.CustomApplication'
      ]">
    <xsl:copy>
      <xsl:copy-of select="@*"/>

      <void property="frameSizeClientArea">
        <boolean>false</boolean>
      </void>

      <void property="customIconImageFiles">
        <xsl:if test="void[@property='customPngIcon16File']">
          <void method="add">
            <object class="com.install4j.api.beans.ExternalFile">
              <string><xsl:value-of select="void[@property='customPngIcon16File']/object/string/text()"/></string>
            </object>
          </void>
        </xsl:if>
        <xsl:if test="void[@property='customPngIcon32File']">
          <void method="add">
            <object class="com.install4j.api.beans.ExternalFile">
              <string><xsl:value-of select="void[@property='customPngIcon32File']/object/string/text()"/></string>
            </object>
          </void>
        </xsl:if>
      </void>

      <xsl:apply-templates select="*[@property != 'customPngIcon16File' and @property != 'customPngIcon32File']"/>

    </xsl:copy>
  </xsl:template>

  <xsl:template match="//object[@class='com.install4j.runtime.beans.formcomponents.FileChooserComponent']">
    <xsl:copy>
      <xsl:copy-of select="@*"/>

      <xsl:if test="void[@property='fileFilterName']">
        <void property="useFileFilter">
          <boolean>true</boolean>
        </void>
      </xsl:if>

      <xsl:apply-templates/>

    </xsl:copy>
  </xsl:template>


  <xsl:template match="//object[
      @class='com.install4j.runtime.beans.screens.LicenseScreen' or
      @class='com.install4j.runtime.beans.screens.DefaultInfoScreen' or
      @class='com.install4j.runtime.beans.screens.CustomizableInfoScreen'
      ]">
    <xsl:copy>
      <xsl:copy-of select="@*"/>

      <xsl:choose>
        <xsl:when test="void[@property='displayedText']">
          <void property="textSource">
            <object class="com.install4j.runtime.beans.screens.components.TextSource" field="DIRECT" />
          </void>
        </xsl:when>
        <xsl:when test="void[@property='displayedTextFile']">
          <void property="displayedTextFile">
            <void property="languageIdToExternalFile">
              <void method="put">
                <string><xsl:value-of select="/install4j/application/languages/principalLanguage/@id"/></string>
                <object class="com.install4j.api.beans.ExternalFile">
                  <string><xsl:value-of select="void[@property='displayedTextFile']/object/string/text()"/></string>
                </object>
              </void>
            </void>
          </void>
        </xsl:when>
      </xsl:choose>

      <xsl:apply-templates select="*[@property != 'displayedTextFile']"/>

    </xsl:copy>
  </xsl:template>

  <xsl:template match="//object[
      @class='com.install4j.runtime.beans.actions.text.ReplaceInstallerVariablesAction' or
      @class='com.install4j.runtime.beans.actions.text.RegexTextFileAction' or
      @class='com.install4j.runtime.beans.actions.text.ModifyTextFileAction' or
      @class='com.install4j.runtime.beans.actions.text.FixCrLfAction' or
      @class='com.install4j.runtime.beans.actions.xml.XPathReplaceAction' or
      @class='com.install4j.runtime.beans.actions.files.AddWindowsFileRightsAction' or
      @class='com.install4j.runtime.beans.actions.files.SetOwnerAction' or
      @class='com.install4j.runtime.beans.actions.files.SetFiletimeAction' or
      @class='com.install4j.runtime.beans.actions.files.DeleteFileAction' or
      @class='com.install4j.runtime.beans.actions.files.SetModeAction'
      ]">
    <xsl:copy>
      <xsl:copy-of select="@*"/>

      <xsl:if test="void[@property='file']">
        <void property="files">
          <array class="java.io.File" length="1">
            <void index="0">
              <object class="java.io.File">
                <string><xsl:value-of select="void[@property='file']/object/string/text()"/></string>
              </object>
            </void>
          </array>
        </void>
      </xsl:if>

      <xsl:apply-templates select="*[@property != 'file']"/>

    </xsl:copy>
  </xsl:template>

  <xsl:template match="//object[@class='com.install4j.runtime.beans.actions.files.DeleteFileAction']/void[@property='recurse']">
    <void property="recursive">
      <xsl:apply-templates />
    </void>
  </xsl:template>

  <xsl:template match="//object[
      @class='com.install4j.runtime.beans.actions.files.CopyFileAction' or
      @class='com.install4j.runtime.beans.actions.files.MoveFileAction'
      ]">
    <xsl:copy>
      <xsl:copy-of select="@*"/>

      <xsl:if test="void[@property='sourceFile']">
        <void property="files">
          <array class="java.io.File" length="1">
            <void index="0">
              <object class="java.io.File">
                <string><xsl:value-of select="void[@property='sourceFile']/object/string/text()"/></string>
              </object>
            </void>
          </array>
        </void>
      </xsl:if>

      <xsl:apply-templates select="*[@property != 'sourceFile']"/>

    </xsl:copy>
  </xsl:template>

  <xsl:template match="//object[@class='com.install4j.runtime.beans.screens.StandardProgramGroupScreen']">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:variable name="action" select="//object[@class='com.install4j.runtime.beans.actions.desktop.CreateProgramGroupAction'][1]"/>
      <xsl:choose>
        <xsl:when test="$action">
          <xsl:if test="$action/void[@property='unixSymlinks']">
            <xsl:copy-of select="$action/void[@property='unixSymlinks']"/>
          </xsl:if>
          <xsl:if test="$action/void[@property='programGroupName']">
            <xsl:copy-of select="$action/void[@property='programGroupName']"/>
          </xsl:if>
          <xsl:if test="$action/void[@property='linkDirectory']">
            <xsl:copy-of select="$action/void[@property='linkDirectory']"/>
          </xsl:if>
          <xsl:if test="$action/void[@property='allUsers']">
            <xsl:copy-of select="$action/void[@property='allUsers']"/>
          </xsl:if>
          <xsl:if test="$action/void[@property='enabled']">
            <void property="programGroupEnabled">
              <boolean><xsl:value-of select="$action/void[@property='enabled']/boolean/text()"/></boolean>
            </void>
          </xsl:if>
        </xsl:when>
        <xsl:otherwise>
          <void property="programGroupName">
            <string>${compiler:sys.fullName}</string>
          </void>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates />
    </xsl:copy>
  </xsl:template>

  <xsl:template match="//object[@class='com.install4j.runtime.beans.actions.services.InstallServiceAction']">
    <xsl:variable name="launcherId" select="void[@property='launcherId']/string/text()"/>
    <xsl:variable name="launcher" select="/install4j/launchers/launcher[@id=$launcherId]"/>

    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <void property="autoStart">
        <boolean><xsl:value-of select="string($launcher/executable/@serviceStartType='2')"/></boolean>
      </void>

      <void property="description">
        <string><xsl:value-of select="$launcher/executable/@serviceDescription"/></string>
      </void>

      <void property="windowsDependencies">
        <string><xsl:value-of select="$launcher/executable/@serviceDependencies"/></string>
      </void>

      <xsl:apply-templates/>
    </xsl:copy>

  </xsl:template>

  <xsl:template match="//screen[@beanClass='com.install4j.runtime.beans.screens.ServicesScreen']" />

  <xsl:template match="//screen[@beanClass='com.install4j.runtime.beans.screens.WelcomeScreen']/actions">
    <xsl:copy>
      <action beanClass="com.install4j.runtime.beans.actions.misc.LoadResponseFileAction" multiExec="true">
        <xsl:call-template name="addIdAttribute"/>
        <serializedBean>
          <java class="java.beans.XMLDecoder">
            <object class="com.install4j.runtime.beans.actions.misc.LoadResponseFileAction" />
          </java>
        </serializedBean>
        <condition>context.getBooleanVariable("sys.confirmedUpdateInstallation")</condition>
      </action>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="//object[@class='com.install4j.runtime.beans.actions.desktop.CreateStartMenuEntryAction']">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <void property="programGroupName">
        <string></string>
      </void>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="//action[@beanClass='com.install4j.runtime.beans.actions.InstallFilesAction']">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:attribute name="errorMessage">${i18n:FileCorrupted}</xsl:attribute>
      <xsl:attribute name="failureStrategy">2</xsl:attribute>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="//screen[@beanClass='com.install4j.runtime.beans.screens.InstallationDirectoryScreen' or
                                @beanClass='com.install4j.runtime.beans.screens.StandardProgramGroupScreen']/condition[.='']">
    <condition>!context.getBooleanVariable("sys.confirmedUpdateInstallation")</condition>
  </xsl:template>

  <xsl:template match="//action[@beanClass='com.install4j.runtime.beans.actions.desktop.CreateProgramGroupAction']/condition[.='']">
    <condition>!context.getBooleanVariable("sys.programGroupDisabled")</condition>
  </xsl:template>

  <xsl:template match="//action[@beanClass='com.install4j.runtime.beans.actions.desktop.CreateStartMenuEntryAction']/condition[.=''
        and starts-with(../serializedBean/java/object/void[@property='entryName']/string/text(), '${installer:sys.programGroupDir}')]">
    <condition>!context.getBooleanVariable("sys.programGroupDisabled")</condition>
  </xsl:template>

  <xsl:template match="//object[@class='com.install4j.runtime.beans.actions.desktop.CreateProgramGroupAction']">
    <xsl:copy>
      <xsl:copy-of select="@*"/>

      <void property="uninstallerMenuName">
        <string>${i18n:UninstallerMenuEntry(${compiler:sys.fullName})}</string>
      </void>

      <xsl:choose>
        <xsl:when test="//object[@class='com.install4j.runtime.beans.screens.StandardProgramGroupScreen']">
          <void property="programGroupName">
            <string>${installer:sys.programGroupName}</string>
          </void>
          <void property="linkDirectory">
            <string>${installer:sys.symlinkDir}</string>
          </void>

          <xsl:apply-templates select="*[@property != 'enabled' and @property != 'programGroupName' and @property != 'linkDirectory']"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates select="*[@property != 'enabled']"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="//object[@class='com.install4j.runtime.beans.screens.components.ProgramGroupEntryConfig']">
    <object class="com.install4j.runtime.beans.screens.components.ProgramGroupFileConfig">
      <xsl:apply-templates />
    </object>
  </xsl:template>

  <xsl:template match="visibiltyScript">
    <visibilityScript>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates/>
    </visibilityScript>
  </xsl:template>

  <xsl:template match="/install4j/files">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:attribute name="keepModificationTimes">
        <xsl:value-of select="/install4j/application/@keepModificationTimes"/>
      </xsl:attribute>
      <xsl:attribute name="missingFilesStrategy">
        <xsl:value-of select="/install4j/application/@missingFilesStrategy"/>
      </xsl:attribute>

      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="//dirEntry | //fileEntry">
    <xsl:copy>
      <xsl:copy-of select="@*"/>

      <xsl:attribute name="overrideOverwriteMode">true</xsl:attribute>
      <xsl:attribute name="overrideUninstallMode">true</xsl:attribute>
      <xsl:attribute name="overrideFileMode">true</xsl:attribute>
      <xsl:attribute name="overrideDirMode">true</xsl:attribute>

      <xsl:attribute name="overwriteMode">
        <xsl:value-of select="@overwrite"/>
      </xsl:attribute>
      <xsl:attribute name="fileMode">
        <xsl:value-of select="@mode"/>
      </xsl:attribute>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="/install4j/mediaSets/*">

    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:if test="@requires64bit='true' and not(@jreBitType)">
        <xsl:attribute name="jreBitType">64</xsl:attribute>
      </xsl:if>

      <xsl:if test="@runAsAdmin='true'">
        <xsl:attribute name="executionLevel">requireAdministrator</xsl:attribute>
      </xsl:if>

      <xsl:apply-templates/>

    </xsl:copy>
  </xsl:template>

  <xsl:template match="/install4j/mediaSets/win32">
    <windows>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates/>
    </windows>
  </xsl:template>

  <xsl:template match="/install4j/mediaSets/win32Archive">
    <windowsArchive>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates/>
    </windowsArchive>
  </xsl:template>


  <xsl:template match="/install4j/files/components/component">

    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:if test="@mandatory='true'">
        <xsl:attribute name="changeable">false</xsl:attribute>
        <xsl:attribute name="selected">true</xsl:attribute>
      </xsl:if>

      <xsl:apply-templates/>

    </xsl:copy>
  </xsl:template>

  <xsl:template match="/install4j/launchers/launcher[@external='false']">

    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <iconImageFiles>
        <xsl:if test="@pngIcon16File">
          <file path="{@pngIcon16File}"/>
        </xsl:if>
        <xsl:if test="@pngIcon32File">
          <file path="{@pngIcon32File}"/>
        </xsl:if>
      </iconImageFiles>

      <vmOptionsFile mode="none" />

      <xsl:apply-templates/>

    </xsl:copy>
  </xsl:template>

</xsl:transform>
