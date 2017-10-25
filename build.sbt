import Dependencies._
import Common._

lazy val commonSettings = Seq(
    version in ThisBuild := "1.5.3-bb-pre-1",
    organization in ThisBuild := "io.github.bbarker",
    homepage in ThisBuild := Some(url("https://github.com/bbarker/scalaxb")),
    licenses in ThisBuild := Seq("MIT License" -> url("https://github.com/eed3si9n/scalaxb/blob/master/LICENSE")),
    description in ThisBuild := """scalaxb is an XML data-binding tool for Scala that supports W3C XML Schema (xsd) and wsdl.""",
    // pgpReadOnly := false,
    scalacOptions := Seq("-deprecation", "-unchecked", "-feature", "-language:implicitConversions", "-language:postfixOps"),
    parallelExecution in Test := false,
    resolvers += Resolver.typesafeIvyRepo("releases")
  ) ++ sonatypeSettings

// Disably scalaxb plugin for now as sbt 0.13 plugins depend on Scala 2.10:
// https://github.com/eed3si9n/scalaxb/issues/447
lazy val root = (project in file(".")).
  enablePlugins(NoPublish).
  aggregate(app, integration /*, scalaxbPlugin */).
  settings(
    scalaVersion := scala211
   )

lazy val app = (project in file("cli")).
  settings(commonSettings: _*).
  settings(codegenSettings: _*).
  settings(
    name := "scalaxb",
    crossScalaVersions := Seq(scala212, scala211),
    scalaVersion := scala211,
    resolvers += sbtResolver.value,
    libraryDependencies ++= appDependencies(scalaVersion.value),
    scalacOptions := {
      val prev = scalacOptions.value
      prev :+ "-Xfatal-warnings"
    },

    mainClass          in assembly := Some("scalaxb.compiler.Main")
  , assemblyOption     in assembly := (assemblyOption in assembly).value.copy(prependShellScript = Some(sbtassembly.AssemblyPlugin.defaultShellScript))
  , assemblyOutputPath in assembly := file(s"./${name.value}-${version.value}")
  )

lazy val integration = (project in file("integration")).
  settings(commonSettings: _*).
  settings(
    crossScalaVersions := Seq(scala211),
    scalaVersion := scala211,
    publishArtifact := false,
    libraryDependencies ++= integrationDependencies(scalaVersion.value)
    // fork in test := true,
    // javaOptions in test ++= Seq("-Xmx2G", "-XX:MaxPermSize=512M")
  , parallelExecution in Test := false
  , testOptions in Test += Tests.Argument("sequential")
  ).
  dependsOn(app)

lazy val scalaxbPlugin = (project in file("sbt-scalaxb")).
  settings(commonSettings: _*).
  settings(
    sbtPlugin := true,
    name := "sbt-scalaxb",
    description := """sbt plugin to run scalaxb""",
    ScriptedPlugin.scriptedSettings,
    scriptedLaunchOpts := { scriptedLaunchOpts.value ++
      Seq("-Xmx1024M", "-XX:MaxPermSize=256M", "-Dplugin.version=" + version.value)
    },
    scriptedBufferLog := false,
    scripted := scripted.dependsOn(publishLocal in app).evaluated
  ).
  dependsOn(app)




