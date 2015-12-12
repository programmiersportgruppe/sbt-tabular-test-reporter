package org.programmiersportgruppe.sbt.testreporter

import scala.xml.{Unparsed, Elem}


class HtmlFormatter (results: Seq[TestSummary]){
    def htmlReport: Elem =
        <html>
            <head>
                <script src="https://cdnjs.cloudflare.com/ajax/libs/tablesort/2.2.4/tablesort.min.js"></script>
                <style>
                    {Unparsed("""
                body {font-family: Arial, Helvetica, sans-serif;}
                table {width:100%;}
                table {border-spacing:0; background-color:#eee; padding:4px; border-collapse:collapse;}
                table td {border-width:1px 0; border-style:solid; border-color:#888;}
                table th, table td {max-width:1000px; font-size:90%;padding: 0.4em;}
                table thead {background-color:white;text-align:left}
                tbody tr:nth-child(even) {background-color:#fff;}
                .ignored {color:#E2B207;}
                .failure {color:#F21807;}
                .success {color:#60D606;}
                th.sort-header::-moz-selection { background:transparent; }
                th.sort-header::selection      { background:transparent; }
                th.sort-header {
                    cursor:pointer;
                }
                th.sort-header::-moz-selection,
                th.sort-header::selection {
                    background:transparent;
                }
                table th.sort-header:after {
                    content:'';
                    float:right;
                    margin-top:7px;
                    border-width:0 4px 4px;
                    border-style:solid;
                    border-color:#404040 transparent;
                    visibility:hidden;
                }
                table th.sort-header:hover:after {
                    visibility:visible;
                }
                table th.sort-up:after,
                table th.sort-down:after,
                table th.sort-down:hover:after {
                    visibility:visible;
                    opacity:0.4;
                }
                table th.sort-up:after {
                    border-bottom:none;
                    border-width:4px 4px 0;
                }
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
                            <th>Duration w/o Setup</th>
                            <th>Suite</th>
                            <th>Name</th>
                            <th>Failure Msg</th>
                        </tr>
                    </thead>
                    <tbody>{
                        results.map(_.toColumns).map( row => {
                            val cssClass = row(1).trim.toLowerCase
                            <tr>
                                <td>
                                    {row(0)}
                                </td>

                                <td class={cssClass}>
                                    {row(1)}
                                </td>

                                {row.tail.tail.map(col =>
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


}
