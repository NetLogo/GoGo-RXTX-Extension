# NetLogo GoGo board extension

This package contains the NetLogo GoGo board extension.

## Using

This extension is pre-installed in NetLogo. For instructions on using it in your model, or for more information about NetLogo extensions, see the NetLogo User Manual.

## Building

Run `./sbt package` to build the extension.

If the build succeeds, `gogo.jar` is created.

## How the Windows Installers Were Made

Inside the folder named "Windows", you will notice two installers and an .inf file.  The two installers are identical, except that one (suffixed with "\_32") is designed to work only on 32-bit Windows systems, while the other (suffixed with "\_64") is designed to work only on 64-bit Windows systems.  Each installer was created to automatically install the .inf file that can be found in the directory with them (and which was included to allow people (especially those on Windows XP) to be able to install the drivers manually).

At some point, though, someone might wish to regenerate the installers for some particular reason.  To do so, first, you will need to get ahold of the various dpinst.exe tools that Microsoft has made.  The easiest way to do that is to download the Windows Driver Kit, and navigate to %DRIVER\_KIT\_DIRECTORY%\redist\DIFx\dpinst\EngMui.  There, you should see three folders, each containing a dpinst.exe file (plus additional folders for other locales).  The "amd64" folder was used for creating the 64-bit installer, and the "x86" folder was used for creating the 32-bit installer.

Additionally, you will also want to make an XML file for dpinst.exe (as described [here](http://msdn.microsoft.com/en-us/library/windows/hardware/ff553383%28v=vs.85%29.aspx)).  Both installers can use the same XML file, and the one that was used to generate the existing installers was actually very simple:

    <?xml version="1.0"?>
    <dpInst>
        <legacyMode/>
    </dpInst>

The "legacyMode" flag tells dpinst.exe that it's alright to install an unsigned driver (like the one used)--but at the expense of requiring admin access to launch the installer.

After gathering up an .inf, a dpinst.exe, and an XML file for dpinst.exe to reference, you're ready to make the base installer.  To do that, run IExpress.exe (which can be done simply on Windows systems by pressing (Windows key)+R and typing "iexpress").  Navigate through the wizard as you see fit, until asked to include "Packaged files".  At that point, add the .inf driver file you want to use, the .xml file you want dpinst.exe to refer to, and the relevant version of dpinst.exe .  On the next page, you will be asked to select your "Install Program".  Choose "dpinst.exe" from the dropdown.  Proceed through the wizard as normal, and, when it's done, you will have yourself a brand new base installer.  Repeat these steps accordingly to create installers for each particular installation configuration that you would like accomodate for.

But wait!  You're not done yet!  Assuming you built the installer with an unsigned driver, upon trying to _launch_ the installer, you will be told that doing so would require elevated privileges.  While this can be easily circumvented by right-clicking the installer and selecting "Run as administrator", it's not a very good solution to the problem.  If you wanted to be able to automatically run the installer through NetLogo (as is currently done by the GoGo extension), you would then need users to think ahead and launch _NetLogo_ as admin in order for the driver installers to be launched as admin.  That defeats the purpose of the seamlessness behind the GoGo driver installer solution in NetLogo, so... how do we get around that?

If you said, "More XML manifests to the rescue!", you are correct!  In order to do this, you'll first want to get ahold of Microsoft's mt.exe (which is generally bundled with Visual Studio).  Next, you'll need to make an XML manifest file (as per [Microsoft's instructions](http://msdn.microsoft.com/en-us/library/bb756929.aspx)).  The manifest can pretty much be a straight-up copy+paste of the example manifest given at the above link.  The one used for the installers actually _is_ a copy+paste of that, with the .exe's name changed accordingly.

Once you have the base installer, the manifest file, and mt.exe all together, you're ready for the final step.  Open Command Prompt (cmd.exe) and run this command from the location of your mt.exe :

    mt.exe -manifest <manifest name>.manifest -outputresource:<your installer's name>.exe;#1

Congratulations.  You now have a working, seemlessly-launchable-from-NetLogo driver installer!

## Terms of Use

[![CC0](http://i.creativecommons.org/p/zero/1.0/88x31.png)](http://creativecommons.org/publicdomain/zero/1.0/)

The NetLogo GoGo board extension is in the public domain.  To the extent possible under law, Uri Wilensky has waived all copyright and related or neighboring rights.

For managing connections with GoGo devices, this extension uses the RXTX library, Copyright 1997-2007 by Trent Jarvi tjarvi@qbang.org "and others who actually wrote it".  See individual source files for more information (http://users.frii.com/jarvi/rxtx/). The library is covered by the GNU LGPL (Lesser General Public License). The text of that license is included in the "docs" folder which accompanies NetLogo, and is also available from http://www.gnu.org/copyleft/lesser.html .
