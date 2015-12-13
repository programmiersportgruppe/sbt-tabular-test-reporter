package org.programmiersportgruppe.sbt.testreporter

import java.util.Date
import Utilities._

import scala.collection.immutable.ListMap

/* Represents the result of a single test case */
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

    def toJson: String = {
        import spray.json._
        JsObject(ListMap(
            "timestamp" -> JsString(timestamp.toIso8601),
            "status"  -> JsString(status),
            "durationWithSetup" -> JsNumber(durationWithSetup),
            "duration" -> JsNumber(duration),
            "suite" -> JsString(suiteName),
            "test" -> JsString(name),
            "errorMessage" -> JsString(errorMessage)
        ).toMap).compactPrint
    }
}
