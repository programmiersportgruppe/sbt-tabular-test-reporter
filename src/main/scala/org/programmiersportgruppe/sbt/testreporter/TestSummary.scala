package org.programmiersportgruppe.sbt.testreporter

import java.util.Date
import Utilities._

case class TestSummary(
    timestamp: Date,
    status: String,
    durationWithSetup: Double,
    duration: Double,
    suiteName: String,
    name: String,
    errorMessage: String
) {
    def toColumns: Seq[String] =
    Seq(
        timestamp.toIso8601,
        status.padTo(7, ' '),
        "%8.3f".format(durationWithSetup),
        "%8.3f".format(duration),
        suiteName,
        name.replaceAll("\\s+", "_"),
        errorMessage
    )
}
