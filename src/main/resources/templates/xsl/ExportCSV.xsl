<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml"/>

    <xsl:template match="/">
        <page xmlns:comm="http://ws.online.fnb.co.za/v1/common/"
              xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" id="boxConatinerID" title="Box Container"
              template="main" layout="" version="1">

            <!--Description-->
            <description>Exporting data as a CSV (Excel) file.</description>

            <!--Heading-->
            <symbol xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                    xsi:type="comm:subTabGroup"
                    comm:subTabGroupHeading="Export request"/>

            <symbol xsi:type="comm:formLayout">
                <comm:form comm:name="advancedSearchForm" comm:action="http://localhost:8445">
                    <comm:sections comm:width="full">
                        <comm:symbol xsi:type="comm:boxContainer" comm:id="boxDiv">
                            <comm:box xsi:type="comm:box">
                                <comm:boxSymbol xsi:type="comm:textHeading" comm:size="4">
                                    <comm:value>Report details</comm:value>
                                </comm:boxSymbol>

                                <!-- Status Dropdown -->
                                <comm:boxSymbol xsi:type="comm:boxSplit" comm:width="33">
                                    <comm:boxSplitSymbol
                                            xsi:type="comm:dropdown"
                                            comm:id="status"
                                            comm:label="Status"
                                            comm:placeholder="Select status"
                                            comm:selectedValue="All"
                                            comm:pleaseSelectOptionEnabled="false">
                                        <comm:label>All</comm:label>
                                        <comm:value xsi:type="comm:eventValue"><comm:value>All</comm:value></comm:value>

                                        <comm:label>Approved</comm:label>
                                        <comm:value xsi:type="comm:eventValue"><comm:value>Approved</comm:value></comm:value>

                                        <comm:label>Breached</comm:label>
                                        <comm:value xsi:type="comm:eventValue"><comm:value>Breached</comm:value></comm:value>

                                        <comm:label>In Progress</comm:label>
                                        <comm:value xsi:type="comm:eventValue"><comm:value>In Progress</comm:value></comm:value>

                                        <comm:label>On Hold</comm:label>
                                        <comm:value xsi:type="comm:eventValue"><comm:value>On Hold</comm:value></comm:value>

                                        <comm:label>Completed</comm:label>
                                        <comm:value xsi:type="comm:eventValue"><comm:value>Completed</comm:value></comm:value>
                                    </comm:boxSplitSymbol>
                                </comm:boxSymbol>

                                <!-- From Date -->
                                <comm:boxSymbol xsi:type="comm:boxSplit" comm:width="25">
                                    <comm:boxSplitSymbol
                                            xsi:type="comm:date"
                                            comm:id="fromDate"
                                            comm:label="From Date"
                                            comm:placeholder="YYYY-MM-DD"/>
                                </comm:boxSymbol>

                                <!-- To Date -->
                                <comm:boxSymbol xsi:type="comm:boxSplit" comm:width="25">
                                    <comm:boxSplitSymbol
                                            xsi:type="comm:date"
                                            comm:id="toDate"
                                            comm:label="To Date"
                                            comm:placeholder="YYYY-MM-DD"/>
                                </comm:boxSymbol>

                            </comm:box>
                        </comm:symbol>
                    </comm:sections>
                </comm:form>
            </symbol>

            <symbol xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="comm:footer"
                    comm:text="" comm:textAlign="left" comm:buttonAlign="right">

                <comm:baseButton comm:id="back" comm:target="main"
                                 comm:url="app-domain/mandates-and-resolutions/adminAll"
                                 comm:label="Back" comm:formSubmit="false"/>

                <comm:baseButton comm:id="export" comm:target="main"
                                 comm:url="app-domain/mandates-and-resolutions/adminAll"
                                 comm:label="Export" comm:formSubmit="false"/>
            </symbol>
        </page>
    </xsl:template>
</xsl:stylesheet>