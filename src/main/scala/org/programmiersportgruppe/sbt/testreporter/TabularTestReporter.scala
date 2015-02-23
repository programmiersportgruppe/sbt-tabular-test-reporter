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

import sbt._
import Keys._

import scala.xml.{Unparsed, PCData, Text, Elem}

object TabularTestReporterPlugin extends AutoPlugin {
    override lazy val projectSettings = Seq(
        testListeners += new TabularTestReporter(target.value.getAbsolutePath)
    )

    override val trigger = AllRequirements
}

class TabularTestReporter(val outputDir: String) extends TestsListener {

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
                    name.replaceAll("\\s+", "_"), error
                )
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

        val timeStamp: String = new SimpleDateFormat("YMMdd-HHmmss").format(new Date())
        val textResultPath: String = new sbt.File(targetDir, s"test-results-${timeStamp}.txt").getAbsolutePath
        val htmlResultPath: String = new sbt.File(targetDir, s"test-results-${timeStamp}.html").getAbsolutePath
        val out = new OutputStreamWriter(new FileOutputStream(textResultPath), "UTF-8")
        out.write(results.map(cols => cols.mkString(" ")).mkString("\n") + "\n")
        out.close()

        scala.xml.XML.save(htmlResultPath, htmlReport)

        val symlink = new File(new File(outputDir), "test-results-latest.txt")
        if (symlink.exists()) {
            symlink.delete()
        }

        Files.createSymbolicLink(symlink.toPath, Paths.get(textResultPath))
    }

    /** Returns None */
    override def contentLogger(test: TestDefinition): Option[ContentLogger] = None
}
