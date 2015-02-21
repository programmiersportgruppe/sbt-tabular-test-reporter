import com.typesafe.sbt.SbtPgp._


description := "An sbt plugin that produces nice test reports."

homepage := Some(url("https://github.com/programmiersportgruppe/sbt-test-reporter"))

publishMavenStyle := true

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

publishArtifact in Test := false

pomExtra := (
  <licenses>
    <license>
      <name>BSD-style</name>
      <url>http://www.opensource.org/licenses/bsd-license.php</url>
    </license>
  </licenses>
    <scm>
      <url>https://github.com/programmiersportgruppe</url>
      <connection>scm:git:git@github.com:programmiersportgruppe/sbt-test-reporter.git</connection>
    </scm>
    <developers>
      <developer>
        <id>fleipold</id>
        <name>Felix Leipold</name>
        <url>https://github.com/fleipold</url>
      </developer>
    </developers>)

useGpg := true

sbtPlugin := true

name := "testreporter"

organization := "org.programmiersportgruppe.sbt"

description := "A simple plugin that outputs tab separated test results."

version :=  sys.props.getOrElse("release.version", default = "LOCAL-SNAPSHOT")

scalaVersion := "2.10.4"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-encoding", "UTF8")


//ScriptedPlugin.scriptedSettings
//
//scriptedLaunchOpts := { scriptedLaunchOpts.value ++
//  Seq("-Xmx1024M", "-XX:MaxPermSize=256M", "-Dplugin.version=" + version.value)
//}
//
//scriptedBufferLog := false
