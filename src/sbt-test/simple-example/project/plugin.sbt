scalaVersion := "2.11.2"

resolvers += "Scala Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"



//{
//  val pluginVersion = System.getProperty("plugin.version")
//  if(pluginVersion == null)
//    throw new RuntimeException("""|The system property 'plugin.version' is not defined.
//                                 |Specify this property using the scriptedLaunchOpts -D.""".stripMargin)
//  else addSbtPlugin("org.programmierportgruppe.sbt" %% "testreporter" % pluginVersion)
//}

addSbtPlugin("org.programmierportgruppe.sbt" %% "testreporter" % "1.0.0")

