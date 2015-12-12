package org.programmiersportgruppe.sbt.testreporter

import java.io.{FileOutputStream, OutputStreamWriter}
import java.text.{SimpleDateFormat, DateFormat}
import java.util.Date

object Utilities {


    implicit class AugmentedString(s: String) {
        def save(path: String): Unit = {
            val out = new OutputStreamWriter(new FileOutputStream(path), "UTF-8")
            out.write(s)
            out.close()
        }
    }

    object Date {
        val iso8601: DateFormat  = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
    }

    implicit class AugmentedDate(d: Date) {
        import Date.iso8601

        def toIso8601: String = iso8601.format(d)
    }
}
