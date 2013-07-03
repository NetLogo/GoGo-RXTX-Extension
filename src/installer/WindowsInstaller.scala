package org.nlogo.extensions.gogo.installer

import
  java.{ io, util => jutil },
    io.{ File, IOException },
    jutil.prefs.Preferences

import
  org.nlogo.{ api, app, swing, workspace },
    api.I18N,
    app.App,
    swing.OptionDialog,
    workspace.AbstractWorkspace

object WindowsInstaller {

  import Strings._

  def apply(verify: Boolean) {
    if (System.getProperty("os.name").startsWith("Windows")) {
      val baseDirPath = new File(this.getClass.getProtectionDomain.getCodeSource.getLocation.getFile).getParent
      val fileSep = System.getProperty("file.separator")
      verifyDriverValidity(baseDirPath, fileSep, verify)
    }
  }

  private def deviceNeedsInstallation: Boolean = {
    val prefs = Preferences.userRoot.node(NetLogoPrefNodeName)
    val isOkWithBeingPestered = prefs.getBoolean(AskAboutGoGoDriversKey, true)
    isOkWithBeingPestered && {
      val winDirPath = System.getenv(WinDirEnvVarName)
      val hostDirPath = winDirPath + WinDirPathExtension
      !canFindDriverDirectory(new File(hostDirPath))
    }
  }

  private def canFindDriverDirectory(file: File): Boolean = {
    try
      file.listFiles exists { f => f.isDirectory && f.getName.contains(GoGoDriverEvidenceName) }
    catch {
      case e: Exception =>
        System.err.println("Could not find path %s | See: %s".format(file.getAbsolutePath, e.getMessage))
        false
    }
  }

  private def install(baseDirPath: String, fileSep: String) {
    try {
      val subarch = System.getenv.get("PROCESSOR_ARCHITEW6432")
      val archBits =
        if (System.getProperty("os.arch").endsWith("64") || ((subarch != null) && subarch.endsWith("64")))
          64
        else
          32
      val installerName = SerialInstallerName.format(archBits)
      val builder =
        new ProcessBuilder("cmd.exe", "/C",
          baseDirPath + fileSep + SerialInstallerPath + fileSep + installerName)
      builder.start()
    }
    catch {
      case e: IOException =>
        System.err.println("Could not execute serial driver installer: " + e.getMessage)
    }
  }

  private def verifyDriverValidity(baseDirPath: String, fileSep: String, verify: Boolean = true) {
    if ((deviceNeedsInstallation && canInstall && obtainPermissionToInstall()) || !verify)
      install(baseDirPath, fileSep)
  }

  private def canInstall: Boolean = {
    val osName = System.getProperty("os.name")
    if (osName.contains("Windows 7") || osName.contains("Windows Vista"))
      true
    else {
      if (AbstractWorkspace.isApp) {
        val result = OptionDialog.show(App.app.frame, "User Message", UnsupportedOSMsg, Array(I18N.gui.get("common.buttons.ok"), StopBotheringMeString))
        if (result == 1) {
          val prefs = Preferences.userRoot.node(NetLogoPrefNodeName)
          prefs.putBoolean(AskAboutGoGoDriversKey, false)
        }
      }
      false
    }
  }

  private def obtainPermissionToInstall() : Boolean = {
    try
      AbstractWorkspace.isApp && {
        val parent = App.app.frame
        val result = OptionDialog.show(parent, "User Message", WindowsPromptMsg,
          Array(I18N.gui.get("common.buttons.ok"), StopBotheringMeString, I18N.gui.get("common.buttons.halt")))
        if (result == 1) {
          val prefs = Preferences.userRoot.node(NetLogoPrefNodeName)
          prefs.putBoolean(AskAboutGoGoDriversKey, false)
        }
        result == 0
      }
    catch {
      case e: Exception =>
        System.err.println("Could not obtain permission to install Windows driver fix: " + e.getMessage)
        false
    }
  }

}
