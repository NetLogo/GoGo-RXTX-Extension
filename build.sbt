scalaVersion := "2.10.0"

scalaSource in Compile <<= baseDirectory(_ / "src")

javaSource in Compile <<= baseDirectory(_ / "src")

scalacOptions ++= Seq("-deprecation", "-unchecked", "-Xlint", "-Xfatal-warnings",
                      "-encoding", "us-ascii")

javacOptions ++= Seq("-g", "-deprecation", "-Xlint:all", "-Xlint:-serial", "-Xlint:-path",
                     "-encoding", "us-ascii",
                     "-source", "1.5", "-target", "1.5")

libraryDependencies ++= Seq(
  "org.nlogo" % "NetLogo" % "5.x-8568f6f4" from
    "http://ccl.northwestern.edu/devel/NetLogo-8568f6f4.jar",
  "org.nlogo" % "NetLogoHeadless" % "5.x-8568f6f4" from
    "http://ccl.northwestern.edu/devel/NetLogoHeadless-8568f6f4.jar",
  "rxtx" % "rxtx" % "2.2pre2" from
    "http://ccl.northwestern.edu/devel/rxtx-2.2pre2-bins/RXTXcomm.jar",
  "org.picocontainer" % "picocontainer" % "2.13.6"
)

name := "gogo"

NetLogoExtension.settings

NetLogoExtension.classManager := "org.nlogo.extensions.gogo.GoGoExtension"
