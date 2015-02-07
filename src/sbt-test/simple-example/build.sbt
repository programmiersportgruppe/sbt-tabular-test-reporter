name := "Simple sbt Project"

version := "0.1"

scalaVersion := "2.11.2"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.0-M1"  % "test"

testListeners <<= target.map(t => Seq(new org.programmiersportgruppe.sbt.testreporter.TabularTestReporter(t.getAbsolutePath)))
