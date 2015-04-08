package org.programmiersportgruppe.sbt.testreporter

import scala.xml.{Unparsed, Elem}


class HtmlFormatter (results: Seq[Seq[String]]){
    def htmlReport: Elem =
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
                            <th>Duration w/o Setup</th>
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


}
