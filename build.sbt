scalaVersion := "2.10.0"

scalaSource in Compile <<= baseDirectory(_ / "src")

javaSource in Compile <<= baseDirectory(_ / "src")

scalacOptions ++= Seq("-deprecation", "-unchecked", "-Xfatal-warnings",
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

artifactName := { (_, _, _) => "gogo.jar" }

packageOptions +=
  Package.ManifestAttributes(
    ("Extension-Name", "gogo"),
    ("Class-Manager", "org.nlogo.extensions.gogo.GoGoExtension"),
    ("NetLogo-Extension-API-Version", "5.0"))

packageBin in Compile <<= (packageBin in Compile, dependencyClasspath in Runtime, baseDirectory, streams) map {
  (jar, classpath, base, s) =>
    IO.copyFile(jar, base / "gogo.jar")
    IO.createDirectory(base / "lib")
    val url = "http://ccl.northwestern.edu/devel/rxtx-2.2pre2-bins/"
    val specs =
      Seq(("Mac OS X", "mac-10.5", Seq("librxtxSerial.jnilib")),
          ("Windows", "win32", Seq("rxtxSerial.dll")),
          ("Linux-x86", "i686-pc-linux-gnu", Seq("librxtxParallel.so", "librxtxSerial.so")),
          ("Linux-amd64", "i686-pc-linux-gnu", Seq("librxtxSerial.so")))
    for((arch, _, _) <- specs)
      IO.createDirectory(base / "lib" / arch)
    val natives =
      for((arch, dirname, libnames) <- specs; libname <- libnames)
      yield {
        val native = base / "lib" / arch / libname
        IO.download(new java.net.URL(url + dirname + "/" + libname),
                    native)
        native
      }
    val libraryJarPaths =
      classpath.files.filter{path =>
        path.getName.endsWith(".jar") &&
        path.getName != "scala-library.jar" &&
        !path.getName.startsWith("NetLogo")}
    for(path <- libraryJarPaths)
      IO.copyFile(path, base / path.getName)
    if(Process("git diff --quiet --exit-code HEAD").! == 0) {
      Process("git archive -o gogo.zip --prefix=gogo/ HEAD").!!
      IO.createDirectory(base / "gogo")
      val zipExtras =
        (libraryJarPaths.map(_.getName) :+ "gogo.jar")
      for(extra <- zipExtras)
        IO.copyFile(base / extra, base / "gogo" / extra)
      IO.copyDirectory(base / "lib", base / "gogo" / "lib")
      Process("zip -r gogo.zip gogo/lib " + zipExtras.map("gogo/" + _).mkString(" ")).!!
      IO.delete(base / "gogo")
    }
    else {
      s.log.warn("working tree not clean; no zip archive made")
      IO.delete(base / "gogo.zip")
    }
    jar
  }

cleanFiles <++= baseDirectory { base =>
  Seq(base / "gogo.jar",
      base / "gogo.zip") }
