The Test Reporter plugin provides a test listener, TabularTestReporter that writes test results in a whitespace
separated tabular format to a file, so that it is easy to analyse test results using standard unix tools, such as
sort, awk and uniq.

Here is an example result:


    SUCCESS    0.015 ExampleSpec should_pass
    FAILURE    0.017 ExampleSpec failure_should_be_reported
    FAILURE      0.0 ExampleSpec errors_should_be_reported
    SUCCESS    3.001 ExampleSpec test_should_take_approximately_3_seconds
    SUCCESS    1.002 ExampleSpec test_should_take_approximately_1_second
    IGNORED      0.0 ExampleSpec this_should_be_ignored

All the test results for a project are written into a single file, that has a time stamped filename, such as:

    test-results-20150207-130331.txt

There is also a symlink that links to the latest:

    test-results-latest.txt


Get Started
===========

Add the following lines to either ~/.sbt/plugins/build.sbt (user-specific) or project/plugins/build.sbt (project-specific):

    addSbtPlugin("org.programmierportgruppe.sbt" %% "testreporter" % "1.0.0")

This will add the dependency to the plugin. The next step is to configure your build to output the XML. The following will output the XML in target/test-reports:

    testListeners <<= target.map(t => Seq(new org.programmiersportgruppe.sbt.testreporter.TabularTestReporter(t.getAbsolutePath)))

Note that the line as shown is enough in a *.sbt file. In *.scala files (full configuration), you must collect the result of the expression into the settings of all projects that should produce the XML output.

Open
====

* (How) should we render details on test failures (without loosing the nice - single lineness)?
    + Perhaps alternative single line json document rendering
* Is time stamping the filename the right solution or should we have an "archiving plugin"?
* Investigate whether and how the time taken for tearDown and setUp is accounted for.
* Make automatic testing with scripted work.
* Make it an auto plugin.
