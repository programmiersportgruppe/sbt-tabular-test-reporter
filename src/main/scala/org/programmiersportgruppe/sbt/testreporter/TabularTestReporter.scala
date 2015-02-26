package org.programmiersportgruppe.sbt.testreporter

import java.io.File
import java.nio.file.{Paths, Files}
import java.text.{DateFormat, SimpleDateFormat}
import java.util.{TimeZone, Date}

import _root_.sbt._
import java.io._
import java.net.InetAddress

import scala.collection.mutable.ListBuffer
import sbt.testing.{Event => TEvent, Status => TStatus, Logger => TLogger, NestedTestSelector, TestSelector, AnnotatedFingerprint, SubclassFingerprint}

import sbt._
import Keys._

import scala.util.DynamicVariable
import scala.xml.{Unparsed, PCData, Text, Elem}

object TabularTestReporterPlugin extends AutoPlugin {
    override lazy val projectSettings = Seq(
        testListeners += new TabularTestReporter(target.value.getAbsolutePath)
    )

    override val trigger = AllRequirements
}

class TabularTestReporter(val outputDir: String) extends TestsListener {
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

    var results: Seq[Seq[String]] = Seq()

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
        def stop(): Seq[Seq[String]] = {
            val duration = events.foldLeft(0L)((acc, e) => if (e.duration() < 0) acc else acc + e.duration())

            for (e <- events) yield {

                val className = e.fullyQualifiedName()

                val name = e.selector match {
                    case t: TestSelector => t.testName()
                    case n: NestedTestSelector => n.testName()
                    case _ => "TOSTRING:" + e.selector().toString
                }

                val duration = if (e.duration() < 0) "0.0" else (e.duration() / 1000.0).toString

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
                    timeStampIsao8601,
                    statusText.padTo(7, ' '),
                    duration.reverse.padTo(8, ' ').reverse,
                    className,
                    name.replaceAll("\\s+", "_"),
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


    private def htmlReport: Elem =
    <html>
        <head>
            <script src="https://cdnjs.cloudflare.com/ajax/libs/tablesort/2.2.4/tablesort.min.js"></script>
            <style>
                {Unparsed("""
                table {width:100%;}
                table {border-spacing:0; background-color:#eee; padding:4px; border-collapse:collapse;}
                table td {border-width:1px 0; border-style:solid; border-color:#888;}
                table th, table td {max-width:1000px; font-size:90%;padding: 0.4em;}
                table thead {background-color:white;text-align:left}
                tbody tr:nth-child(even) {background-color:#fff;}
                .ignored {color:#F2F207;}
                .failure {color:#F21807;}
                .success {color:#60D606;}
                """)}
            </style>
        </head>
        <body>
            <table id="resultTable" class="tablesorter">
                <thead>
                    <tr>
                        <th>TimeStamp</th>
                        <th>Status</th>
                        <th>Duration</th>
                        <th>Suite</th>
                        <th>Name</th>
                        <th>Failure Msg</th>
                    </tr>
                </thead>
                <tbody>{
                    results.map( row => {
                        val cssClass = row(0).trim.toLowerCase
                        <tr>
                            <td class={cssClass}>
                                {row(0)}
                            </td>
                            {row.tail.map(col =>
                            <td>
                                {col}
                            </td>
                        )}
                        </tr>
                        }
                    )
                }</tbody>
            </table>
            <script>
                { Unparsed("""
                        new Tablesort(document.getElementById('resultTable'));
                """ )}
            </script>
        </body>
    </html>

    /** Does nothing, as we write each file after a suite is done. */
    override def doComplete(finalResult: TestResult.Value): Unit = {
        val textResultPath: String = new sbt.File(targetDir, s"test-results-${timeStampFileName}.txt").getAbsolutePath
        val htmlResultPath: String = new sbt.File(targetDir, s"test-results-${timeStampFileName}.html").getAbsolutePath
        val out = new OutputStreamWriter(new FileOutputStream(textResultPath), "UTF-8")
        out.write(results.map(cols => cols.mkString(" ")).mkString("\n") + "\n")
        out.close()

        scala.xml.XML.save(htmlResultPath, htmlReport, enc = "UTF-8")

        val symlink = new File(new File(outputDir), "test-results-latest.txt").toPath
        if (Files.isSymbolicLink(symlink)) {
            Files.delete(symlink)
        }

        Files.createSymbolicLink(symlink, Paths.get(textResultPath))
    }

    /** Returns None */
    override def contentLogger(test: TestDefinition): Option[ContentLogger] = None
}
