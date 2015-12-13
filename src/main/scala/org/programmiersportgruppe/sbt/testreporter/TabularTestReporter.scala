package org.programmiersportgruppe.sbt.testreporter

import java.io.File
import java.nio.file.{Paths, Files}
import java.text.{DateFormat, SimpleDateFormat}
import java.util.{TimeZone, Date}

import java.io._

import org.programmiersportgruppe.sbt.testreporter.ReportFormat._

import scala.collection.mutable.ListBuffer
import sbt.testing.{Event => TEvent, Status => TStatus, _}

import sbt._
import Keys._

import scala.util.DynamicVariable
import Utilities._

object TabularTestReporterPlugin extends AutoPlugin {

    object autoImport {
        val Html = ReportFormat.Html
        val WhiteSpaceDelimited = ReportFormat.WhiteSpaceDelimited

        lazy val testReportFormats = settingKey[Set[ReportFormat]]("report formats")
    }

    override lazy val projectSettings = Seq(
        autoImport.testReportFormats := Set(WhiteSpaceDelimited, Html),
        testListeners += new TabularTestReporter(target.value.getAbsolutePath, autoImport.testReportFormats.value)
    )

    override val trigger = AllRequirements
}

class TabularTestReporter(val outputDir: String, formats: Set[ReportFormat]) extends TestsListener {
    private val timeStamp: Date = new Date()

    val timeStampFileName: String = new SimpleDateFormat("YMMdd-HHmmss").format(timeStamp)

    val timeStampIsao8601 = {
        //val tz = TimeZone.getTimeZone("UTC");
        val df: DateFormat  = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        //df.setTimeZone(tz)
        df.format(timeStamp)
    }

    /** The dir in which we put all result files. Is equal to the given dir + "/test-reports" */
    val targetDir = new File(outputDir + "/test-reports/")

    var results: Seq[TestSummary] = Seq()

    /**
     * Gathers data for one Test Suite. We map test groups to TestSuites.
     */
    class TestSuite(val suiteName: String) {
        val events: ListBuffer[TEvent] = new ListBuffer()
        val start = System.currentTimeMillis

        def addEvent(e: TEvent) = events += e

        /**
         * Stops the time measuring and emits the results for
         * All tests collected so far.
         */
        def stop(): Seq[TestSummary] = {
            var end = System.currentTimeMillis

            val durationSetup = {
                val totalDurationTestsWithSetup = end - start
                val totalDurationTestsWithoutSetup = events.map(_.duration).filter(_ >= 0).sum
                totalDurationTestsWithSetup - totalDurationTestsWithoutSetup
            }

            def wasRun(event: TEvent): Boolean = !Set(TStatus.Ignored, TStatus.Skipped).contains(event.status)

            val numberOfTestsRun = events.count(wasRun)

            val setupTimePerTestRun = durationSetup.toDouble / numberOfTestsRun

            for (e <- events) yield {

                val className = e.fullyQualifiedName()

                val name = e.selector match {
                    case t: TestSelector => t.testName()
                    case n: NestedTestSelector => n.testName()
                    case _: SuiteSelector => "(suite level failure)"
                    case _ => "TOSTRING:" + e.selector().toString
                }

                val rawDuration = math.max(0, e.duration)  // e.duration can be -1, if no duration was available
                val durationWithSetup: Double = rawDuration + (if (wasRun(e)) setupTimePerTestRun else 0)

                val statusText = e.status.toString.toUpperCase

                val error = e.throwable match {
                    case t if t.isDefined =>
                        Option(t.get.getMessage).fold(t.get.getClass.getName)(_.split("\n")(0))
                    case _ =>
                        ""
                }

                TestSummary(
                    timeStamp,
                    statusText,
                    durationWithSetup / 1000.0,
                    rawDuration / 1000.0,
                    className,
                    name,
                    error
                )
            }
        }
    }

    /** The currently running test suite */
    var testSuite: DynamicVariable[TestSuite] = new DynamicVariable[TestSuite](null)

    /** Creates the output Dir */
    override def doInit() = {
        targetDir.mkdirs()
    }

    /**
     * Starts a new, initially empty Suite with the given name.
     */
    override def startGroup(name: String) {
        testSuite.value = new TestSuite(name)
    }

    override def testEvent(event: TestEvent): Unit = {
        for (e <- event.detail) {
            testSuite.value.addEvent(e)
        }
    }

    override def endGroup(name: String, t: Throwable) = {
        System.err.println("Throwable escaped the test run of '" + name + "': " + t)
        t.printStackTrace(System.err)
    }

    override def endGroup(name: String, result: TestResult.Value) = {
        this.synchronized {
            results ++= testSuite.value.stop()
        }
    }

    def createOrUpdateSymLink(resultPath: String, linkName: String) : Unit = {
        try {

            val symlink = new File(new File(outputDir), linkName).toPath
            if (Files.isSymbolicLink(symlink)) {
                Files.delete(symlink)
            }

            Files.createSymbolicLink(symlink, Paths.get(resultPath))
        } catch {case ex: java.nio.file.FileSystemException => (

            )}
    }

    /** Does nothing, as we write each file after a suite is done. */
    override def doComplete(finalResult: TestResult.Value): Unit = {
        println("formats: " + formats)
        formats.foreach((format: ReportFormat) =>{

            val resultPath: String = new sbt.File(targetDir, s"test-results-${timeStampFileName}.${format.extension}").getAbsolutePath
            format match {
                case WhiteSpaceDelimited => {
                    (results.map(result => result.toColumns.mkString(" ")).mkString("\n") + "\n").save(resultPath)
                }
                case Html => {scala.xml.XML.save(resultPath, new HtmlFormatter(results).htmlReport, enc = "UTF-8")}
            }
            createOrUpdateSymLink(resultPath, s"test-results-latest.${format.extension}")
        } )
    }

    /** Returns None */
    override def contentLogger(test: TestDefinition): Option[ContentLogger] = None
}
