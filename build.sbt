import sbtunidoc.Plugin.ScalaUnidoc
import sbtunidoc.Plugin.UnidocKeys.unidoc

val `sbt-scalajs-bundler` =
  project.in(file("sbt-scalajs-bundler"))
    .settings(commonSettings: _*)
    .settings(
      sbtPlugin := true,
      name := "sbt-scalajs-bundler",
      description := "Module bundler for Scala.js projects",
      addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.19")
    )

val `sbt-web-scalajs-bundler` =
  project.in(file("sbt-web-scalajs-bundler"))
    .settings(commonSettings: _*)
    .settings(
      sbtPlugin := true,
      scriptedDependencies := {
        val () = scriptedDependencies.value
        val () = publishLocal.value
        val () = (publishLocal in `sbt-scalajs-bundler`).value
      },
      name := "sbt-web-scalajs-bundler",
      description := "Module bundler for Scala.js projects (integration with sbt-web-scalajs)",
      addSbtPlugin("com.vmunier" % "sbt-web-scalajs" % "1.0.6")
    )
    .dependsOn(`sbt-scalajs-bundler`)

// Dummy project that exists just for the purpose of aggregating the two sbt
// plugins. I can not do that in the `doc` project below because the
// scalaVersion is not compatible.
val apiDoc =
  project.in(file("api-doc"))
    .settings(noPublishSettings ++ unidocSettings: _*)
    .settings(
      scalacOptions in (ScalaUnidoc, unidoc) ++= Seq(
        "-groups",
        "-doc-source-url", s"https://github.com/scalacenter/scalajs-bundler/blob/v${version.value}€{FILE_PATH}.scala",
        "-sourcepath", (baseDirectory in ThisBuild).value.absolutePath
      )
    )
    .aggregate(`sbt-scalajs-bundler`, `sbt-web-scalajs-bundler`)

val ornateTarget = Def.setting(target.value / "ornate")

val manual =
  project.in(file("manual"))
    .enablePlugins(OrnatePlugin)
    .settings(noPublishSettings ++ ghpages.settings: _*)
    .settings(
      scalaVersion := "2.11.8",
      git.remoteRepo := "git@github.com:scalacenter/scalajs-bundler.git",
      ornateSourceDir := Some(sourceDirectory.value / "ornate"),
      ornateTargetDir := Some(ornateTarget.value),
      siteSubdirName in ornate := "",
      addMappingsToSiteDir(mappings in ornate, siteSubdirName in ornate),
      mappings in ornate := {
        val _ = ornate.value
        val output = ornateTarget.value
        output ** AllPassFilter --- output pair relativeTo(output)
      },
      siteSubdirName in packageDoc := "api/latest",
      addMappingsToSiteDir(mappings in ScalaUnidoc in packageDoc in apiDoc, siteSubdirName in packageDoc)
    )

import ReleaseTransformations._

val `scalajs-bundler` =
  project.in(file("."))
    .settings(noPublishSettings: _*)
    .settings(
      releaseProcess := Seq[ReleaseStep](
        checkSnapshotDependencies,
        inquireVersions,
        runClean,
        runTest,
        releaseStepInputTask(scripted in `sbt-scalajs-bundler`),
        releaseStepInputTask(scripted in `sbt-web-scalajs-bundler`),
        releaseStepTask(ornate in manual),
        setReleaseVersion,
        commitReleaseVersion,
        tagRelease,
        releaseStepTask(PgpKeys.publishSigned in `sbt-scalajs-bundler`),
        releaseStepTask(PgpKeys.publishSigned in `sbt-web-scalajs-bundler`),
        setNextVersion,
        commitNextVersion,
        pushChanges,
        releaseStepTask(GhPagesKeys.pushSite in manual)
      )
    )
    .aggregate(`sbt-scalajs-bundler`, `sbt-web-scalajs-bundler`, manual, apiDoc)

lazy val commonSettings =
  ScriptedPlugin.scriptedSettings ++ Seq(
    scalacOptions ++= Seq(
      "-feature",
      "-deprecation",
      "-encoding", "UTF-8",
      "-unchecked",
      "-Xlint",
      "-Yno-adapted-args",
      "-Ywarn-dead-code",
      "-Ywarn-numeric-widen",
      "-Ywarn-value-discard",
      "-Xfuture"
    ),
    organization := "ch.epfl.scala",
    pomExtra :=
      <developers>
        <developer>
          <id>julienrf</id>
          <name>Julien Richard-Foy</name>
          <url>http://julien.richard-foy.fr</url>
        </developer>
      </developers>,
    homepage := Some(url(s"https://github.com/scalacenter/scalajs-bundler")),
    licenses := Seq("MIT License" -> url("http://opensource.org/licenses/mit-license.php")),
    scmInfo := Some(
      ScmInfo(
        url("https://github.com/scalacenter/scalajs-bundler"),
        "scm:git:git@github.com:scalacenter/scalajs-bundler.git"
      )
    ),
    crossSbtVersions := List("0.13.16", "1.0.1"),
    scalaVersion := {
      (sbtBinaryVersion in pluginCrossBuild).value match {
        case "0.13" => "2.10.6"
        case _ => "2.12.3"
      }
    },
    // fixed in https://github.com/sbt/sbt/pull/3397 (for sbt 0.13.17)
    sbtBinaryVersion in update := (sbtBinaryVersion in pluginCrossBuild).value,
    scriptedLaunchOpts += "-Dplugin.version=" + version.value,
    scriptedBufferLog := false
  )

lazy val noPublishSettings =
  Seq(
    publishArtifact := false,
    publish := (),
    publishLocal := ()
  )
