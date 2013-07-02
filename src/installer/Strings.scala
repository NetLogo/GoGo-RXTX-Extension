package org.nlogo.extensions.gogo.installer

private[installer] object Strings {

  val WinDirEnvVarName       = "WINDIR"
  val WinDirPathExtension    = """\System32\DriverStore\FileRepository"""
  val NetLogoPrefNodeName    = "/org/nlogo/NetLogo"
  val AskAboutGoGoDriversKey = "gogo.pester"
  val GoGoDriverEvidenceName = "gogo_c"
  val SerialInstallerPath    = "Windows"
  val SerialInstallerName    = "WindowsGoGoInstaller_%d.exe"
  val StopBotheringMeString  = "My GoGo Board Is Installed Fine / Stop Bothering Me"

  private val GoGoANoGoMsg = "Your GoGo Board does not appear to have been properly recognized by Windows."

  val WindowsPromptMsg =
    """|%s
       |
       |If you would like, NetLogo can launch a driver installer that should fix this issue.
       |
       |In order to do so, you will need administrator access to the computer, and you will be asked to accept the installation of an "unsigned" driver.
       |
       |Afterwards, you will need to disconnect and reconnect your GoGo Board in order for it to be properly recognized.""".stripMargin.format(GoGoANoGoMsg)

  val UnsupportedOSMsg =
    """|%s
      |
      |Unfortunately, NetLogo cannot automatically install the GoGo board drivers on this operating system.
      |
      |For further instructions on how to install your GoGo board, please see the 'netlogolab.html' document,
      |
      |which can be found in your NetLogo installation's 'docs' folder, or online at 'http://ccl.northwestern.edu/netlogo/docs/netlogolab.html',
      |
      |and view the "Windows XP" bulletpoint in the "Installing and testing the GoGo Extension" -> "Windows" section.""".stripMargin.format(GoGoANoGoMsg)

}
