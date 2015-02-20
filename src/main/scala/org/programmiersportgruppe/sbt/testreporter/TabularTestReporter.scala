package org.programmiersportgruppe.sbt.testreporter

import java.io.File
import java.nio.file.{Paths, Files}
import java.text.SimpleDateFormat
import java.util.Date

import _root_.sbt._
import java.io._
import java.net.InetAddress

import scala.collection.mutable.ListBuffer
import sbt.testing.{Event => TEvent, Status => TStatus, Logger => TLogger, NestedTestSelector, TestSelector, AnnotatedFingerprint, SubclassFingerprint}

class TabularTestReporter(val outputDir: String) extends TestsListener {

    /** The dir in which we put all result files. Is equal to the given dir + "/test-reports" */
    val targetDir = new File(outputDir + "/test-reports/")

    var results: Seq[String] = Seq()

    /**
     * Gathers data for one Test Suite. We map test groups to TestSuites.
     */
    class TestSuite(val suiteName: String) {
        val events: ListBuffer[TEvent] = new ListBuffer()
        val start = System.currentTimeMillis
        var end = System.currentTimeMillis


        def addEvent(e: TEvent) = events += e

        /**
         * Stops the time measuring and emits the results for
         * All tests collected so far.
         */
        def stop(): Seq[String] = {
            val duration = events.foldLeft(0L)((acc, e) => if (e.duration() < 0) acc else acc + e.duration())

            for (e <- events) yield {

                val className = e.fullyQualifiedName()

                val name = e.selector match {
                    case t: TestSelector => t.testName()
                    case n: NestedTestSelector => n.testName()
                    case _ => "TOSTRING:" + e.selector().toString
                }

                val time = if (e.duration() < 0) "0.0" else (e.duration() / 1000.0).toString

                val statusText =
                    e.status() match {
                        case TStatus.Success => "SUCCESS"
                        case TStatus.Error => "ERROR"
                        case TStatus.Failure => "FAILURE"
                        case TStatus.Skipped => "SKIPPED"
                        case TStatus.Ignored => "IGNORED"
                        case TStatus.Canceled => "CANCELED"
                        case TStatus.Pending => "PENDING"
                    }

                val error = if (e.throwable().isDefined) {
                    e.throwable().get().getMessage.split("\n")(0)
                } else {
                    ""
                }

                Seq(
                    statusText.padTo(7, ' '),
                    time.reverse.padTo(8, ' ').reverse,
                    className,
                    name.replaceAll("\\s+", "_")
                    , error
                ).mkString(" ")
            }
        }
    }

    /** The currently running test suite */
    var testSuite: TestSuite = null

    /** Creates the output Dir */
    override def doInit() = {
        targetDir.mkdirs()
    }

    /**
     * Starts a new, initially empty Suite with the given name.
     */
    override def startGroup(name: String) {
        testSuite = new TestSuite(name)
    }

    override def testEvent(event: TestEvent): Unit = {
        for (e <- event.detail) {
            testSuite.addEvent(e)
        }
    }

    override def endGroup(name: String, t: Throwable) = {
        System.err.println("Throwable escaped the test run of '" + name + "': " + t)
        t.printStackTrace(System.err)
    }

    override def endGroup(name: String, result: TestResult.Value) = {
        results ++= testSuite.stop()
    }

    /** Does nothing, as we write each file after a suite is done. */
    override def doComplete(finalResult: TestResult.Value): Unit = {
        val timeStamp: String = new SimpleDateFormat("YMMdd-HHmmss").format(new Date())
        val path: String = new sbt.File(targetDir, s"test-results-${timeStamp}.txt").getAbsolutePath
        val out = new OutputStreamWriter(new FileOutputStream(path), "UTF-8")
        out.write(results.mkString("\n"))
        out.close()

        val symlink = new File(targetDir, "test-results-latest.txt")
        if (symlink.exists()) {
            symlink.delete()
        }

        Files.createSymbolicLink(symlink.toPath, Paths.get(path))
    }

    /** Returns None */
    override def contentLogger(test: TestDefinition): Option[ContentLogger] = None
}
