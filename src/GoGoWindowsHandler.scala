package org.nlogo.extensions.gogo

import java.util.prefs.Preferences
import java.io.File
import org.nlogo.workspace.AbstractWorkspace
import org.nlogo.app.App
import org.nlogo.swing.OptionDialog
import org.nlogo.api.{I18N, ExtensionManager}

object GoGoWindowsHandler {

  private val WinDirEnvVarName = "WINDIR"
  private val WinDirPathExtension = "\\System32\\DriverStore\\FileRepository"
  private val NetLogoPrefNodeName = "/org/nlogo/NetLogo"
  private val AskAboutGoGoDriversKey = "gogo.pester"
  private val GoGoDriverEvidenceName = "gogo_c"
  private val SerialInstallerPath = "Windows"
  private val SerialInstallerName = "WindowsGoGoInstaller_%d.exe"
  private val StopBotheringMeString = "My GoGo Board Is Installed Fine / Stop Bothering Me"
  private val GoGoANoGoMessage = "Your GoGo Board does not appear to have been properly recognized by Windows."
  private val WindowsPromptMessage =
    GoGoANoGoMessage + "\n\n" +
    "If you would like, NetLogo can launch a driver installer that should fix this issue.\n\n" +
    "In order to do so, you will need administrator access to the computer, and you will be asked to accept the installation of an \"unsigned\" driver.\n\n" +
    "Afterwards, you will need to disconnect and reconnect your GoGo Board in order for it to be properly recognized."
  private val UnsupportedOSMessage =
    GoGoANoGoMessage + "\n\n" +
    "Unfortunately, NetLogo cannot automatically install the GoGo board drivers on this operating system.\n\n" +
    "For further instructions on how to install your GoGo board, please see the `netlogolab.html` document," +
    "which can be found in your NetLogo installation's `docs` folder, or online at http://ccl.northwestern.edu/netlogo/docs/netlogolab.html," +
    "and view the \"Windows XP\" bulletpoint in the \"Installing and testing the GoGo Extension\" -> \"Windows\" section."

  def run(em: ExtensionManager) {
    val baseDirPath = new File(this.getClass.getProtectionDomain.getCodeSource.getLocation.getFile).getParent
    val fileSep = System.getProperty("file.separator")
    verifyDriverValidity(baseDirPath, fileSep, em)
  }

  private def verifyDriverValidity(baseDirPath: String, fileSep: String, extensionManager: ExtensionManager) {
    if (deviceNeedsInstallation && canInstall) {
      if (obtainPermissionToInstall(extensionManager)) {
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
          case e: java.io.IOException =>
            System.err.println("Could not execute serial driver installer: " + e.getMessage)
        }
      }
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
      case e: Throwable =>
        System.err.println("Could not find path " + file.getAbsolutePath + "  See: " + e.getMessage)
        false
    }
  }

  private def canInstall: Boolean = {
    val osName = System.getProperty("os.name")
    if (osName.contains("Windows 7") || osName.contains("Windows Vista"))
      true
    else {
      if (AbstractWorkspace.isApp) {
        val result = OptionDialog.show(App.app.frame, "User Message", UnsupportedOSMessage, Array(I18N.gui.get("common.buttons.ok"), StopBotheringMeString))
        if (result == 1) {
          val prefs = Preferences.userRoot.node(NetLogoPrefNodeName)
          prefs.putBoolean(AskAboutGoGoDriversKey, false)
        }
      }
      false
    }
  }

  private def obtainPermissionToInstall(extensionManager: ExtensionManager): Boolean = {
    try
      AbstractWorkspace.isApp && {
        val parent = App.app.frame
        val result = OptionDialog.show(parent, "User Message", WindowsPromptMessage,
          Array(I18N.gui.get("common.buttons.ok"), StopBotheringMeString, I18N.gui.get("common.buttons.halt")))
        if (result == 1) {
          val prefs = Preferences.userRoot.node(NetLogoPrefNodeName)
          prefs.putBoolean(AskAboutGoGoDriversKey, false)
        }
        result == 0
      }
    catch {
      case e: Throwable =>
        System.err.println("Could not obtain permission to install Windows driver fix: " + e.getMessage)
        false
    }
  }

}
