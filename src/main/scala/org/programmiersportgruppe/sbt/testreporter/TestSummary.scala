package org.programmiersportgruppe.sbt.testreporter

import java.util.Date
import Utilities._

import scala.collection.immutable.ListMap

/* Represents the result of a single test case */
case class TestSummary(
    buildTime: Date,
    status: String,
    durationWithSetup: Double,
    duration: Double,
    suiteName: String,
    name: String,
    testCaseTime: Date,
    errorMessage: String,
    stackTrace: String
) {
    def toColumns: Seq[String] =
    Seq(
        buildTime.toIso8601,
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
            "timestamp" -> JsString(buildTime.toIso8601),
            "status"  -> JsString(status),
            "durationWithSetup" -> JsNumber(Math.round(durationWithSetup*1000)/1000.0),
            "duration" -> JsNumber(duration),
            "suite" -> JsString(suiteName),
            "test" -> JsString(name),
            "test-timestamp" -> JsString(testCaseTime.toIso8601),
            "errorMessage" -> JsString(errorMessage),
            "stackTrace" -> JsString(stackTrace)
        ).toMap).compactPrint
    }
}
