package org.nlogo.extensions.gogo

import java.util.prefs.Preferences
import java.io.File
import org.nlogo.workspace.AbstractWorkspace
import org.nlogo.app.App
import org.nlogo.swing.OptionDialog
import org.nlogo.api.{I18N, ExtensionManager}
import java.security.{Policy, PrivilegedAction, AccessController}

object GoGoWindowsHandler {

  private val WIN_DIR_ENV_VAR_NAME = "WINDIR"
  private val WIN_DIR_PATH_EXTENSION = "\\System32\\DriverStore\\FileRepository"
  private val NETLOGO_PREF_NODE_NAME = "/org/nlogo/NetLogo"
  private val ASK_ABOUT_GOGO_DRIVERS_KEY = "gogo.pester"
  private val GOGO_DRIVER_EVIDENCE_NAME = "gogo_c"
  private val SERIAL_INSTALLER_PATH = "Windows"
  private val SERIAL_INSTALLER_NAME = "WindowsGoGoInstaller_%d.exe"
  private val HALT_FOREVER_STRING = "Halt and Don't Remind Me Again"
  private val WINDOWS_PROMPT_MESSAGE =
    "Your GoGo Board does not appear to have been properly recognized by Windows.\n\n" +
    "If you would like, NetLogo can launch a driver installer that should fix this issue.\n\n" +
    "In order to do so, you will need administrator access to the computer, and you will be asked to accept the installation of an \"unsigned\" driver.\n\n" +
    "Afterwards, you will need to disconnect and reconnect your GoGo Board in order for it to be properly recognized."

  def run(em: ExtensionManager) {
    val baseDirPath = new File(this.getClass.getProtectionDomain.getCodeSource.getLocation.getFile).getParent
    val fileSep = System.getProperty("file.separator")
    verifyDriverValidity(baseDirPath, fileSep, em)
  }

  private def verifyDriverValidity(baseDirPath: String, fileSep: String, extensionManager: ExtensionManager) {
    if (deviceNeedsInstallation) {
      if (obtainPermissionToInstall(extensionManager)) {
        try {
          val subarch = System.getenv.get("PROCESSOR_ARCHITEW6432")
          val archBits =
            if (System.getProperty("os.arch").endsWith("64") || ((subarch != null) && subarch.endsWith("64")))
              64
            else
              32
          val installerName = SERIAL_INSTALLER_NAME.format(archBits)
          val builder =
            new ProcessBuilder("cmd.exe", "/C",
                               baseDirPath + fileSep + SERIAL_INSTALLER_PATH + fileSep + installerName)
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
    val prefs = Preferences.userRoot.node(NETLOGO_PREF_NODE_NAME)
    val isOkWithBeingPestered = prefs.getBoolean(ASK_ABOUT_GOGO_DRIVERS_KEY, true)
    if (isOkWithBeingPestered) {
      val winDirPath = System.getenv(WIN_DIR_ENV_VAR_NAME)
      val hostDirPath = winDirPath + WIN_DIR_PATH_EXTENSION
      !canFindDriverDirectory(new File(hostDirPath))
    }
    else
      false
  }

  private def canFindDriverDirectory(file: File): Boolean = {
    try
      file.listFiles exists { case f => f.isDirectory && f.getName.contains(GOGO_DRIVER_EVIDENCE_NAME) }
    catch {
      case e =>
        System.err.println("Could not find path " + file.getAbsolutePath + "  See: " + e.getMessage)
    }
    false
  }

  private def obtainPermissionToInstall(extensionManager: ExtensionManager): Boolean = {
    try {
      if (AbstractWorkspace.isApp) {
        val parent = App.app.frame
        val result = OptionDialog.show(parent, "User Message", WINDOWS_PROMPT_MESSAGE,
          Array(I18N.gui.get("common.buttons.ok"), HALT_FOREVER_STRING, I18N.gui.get("common.buttons.halt")))
        if (result == 1) {
          val prefs = Preferences.userRoot.node(NETLOGO_PREF_NODE_NAME)
          prefs.putBoolean(ASK_ABOUT_GOGO_DRIVERS_KEY, false)
        }
        return (result == 0)
      }
      false
    }
    catch {
      case e =>
        System.err.println("Could not obtain permission to install Windows driver fix: " + e.getMessage)
        false
    }
  }

}
