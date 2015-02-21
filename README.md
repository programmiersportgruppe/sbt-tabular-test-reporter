What it Does
============

The Test Reporter plugin provides a test listener, TabularTestReporter that writes test results in a whitespace
separated tabular format to a file, so that it is easy to analyse test results using standard unix tools, such as
sort, awk and uniq.

Here is an example result for an example [test suite](https://github.com/programmiersportgruppe/sbt-tabular-test-reporter/blob/master/src/sbt-test/simple-example/src/test/scala/ExampleSpec.scala):

    SUCCESS    0.015 ExampleSpec should_pass
    FAILURE    0.017 ExampleSpec failure_should_be_reported "[A]" was not equal to "[B]"
    FAILURE      0.0 ExampleSpec errors_should_be_reported My error
    SUCCESS    3.001 ExampleSpec test_should_take_approximately_3_seconds
    SUCCESS    1.002 ExampleSpec test_should_take_approximately_1_second
    IGNORED      0.0 ExampleSpec this_should_be_ignored

All the test results for a project are written into a single file, that has a time stamped filename, such as:

    test-results-20150207-130331.txt

This helps analysing issues across build runs.

There is also a symlink that links to the latest:

    test-results-latest.txt

In addition to the text report, there is also html file with a table that supports header sorting, here sorted by
test duration:

![HTML Table Rendering](doc/html-report.png)


Get Started
===========

Add the following lines to either ~/.sbt/plugins/build.sbt (user-specific) or project/plugins/build.sbt (project-specific):

    addSbtPlugin("org.programmiersportgruppe.sbt" %% "tabulartestreporter" % "1.2.0")

This will add the dependency to the plugin and also register the Test Reporter as a test listener, because it is an
auto plugin.


Open
====

Features
--------
* Investigate whether and how the time taken for tearDown and setUp can be accounted for.
* Make automatic testing with scripted work.
* Make output format configurable, e.g. tab separated.

Questions
---------

* Should the hostname be include in the file?
* Should the the current time be included in the file?
* Should the current commit and a flag indicating whether the working copy is clean be included?
* Is time stamping the filename the right solution or should we have an "archiving plugin"
  that copies files after a successful run into the archive folder?

