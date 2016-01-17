lazy val root = (project in file(".")).
  settings(
    name := "mi",
    organization := "com.miriamlaurel",
    scalaVersion := "2.11.7",
    version := "0.1.0",
    sbtVersion := "0.13.9",
    libraryDependencies ++= Seq(
      "com.miriamlaurel" %% "fxcore" % "2.0,
      "org.scalatest" %% "scalatest" % "2.2.6" % "test"
    )
  )
