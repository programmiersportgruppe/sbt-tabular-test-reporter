package org.programmiersportgruppe.sbt.testreporter


sealed abstract class ReportFormat(val extension: String ) {}

object ReportFormat {
    case object WhiteSpaceDelimited extends ReportFormat("txt")
    case object Html extends ReportFormat("html")
}
