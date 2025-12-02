<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml"/>
    <xsl:template match="/">
        <page xmlns:comm="http://ws.online.fnb.co.za/v1/common/"
              xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
              id="boxConatinerID" title="Box Container" template="main" layout="" version="1">

            <description>Exporting data as a CSV (Excel) file.</description>

            <symbol xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                    xsi:type="comm:subTabGroup"
                    comm:subTabGroupHeading="Export request"/>

            <symbol xsi:type="comm:formLayout"
                    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
                <comm:form comm:name="exportForm" comm:action="">
                    <comm:sections comm:width="full">
                        <comm:symbol xsi:type="comm:boxContainer" comm:id="boxDiv">
                            <comm:box xsi:type="comm:box">
                                <comm:boxSymbol xsi:type="comm:textHeading" comm:size="4">
                                    <comm:value>Report details</comm:value>
                                </comm:boxSymbol>
                                <comm:boxSymbol
                                        xsi:type="comm:dropdown"
                                        comm:id="status"
                                        comm:label="Status"
                                        comm:selectedValue="{exportModel/status}"
                                        comm:required="true">
                                    <comm:label>In Progress</comm:label>
                                    <comm:value xsi:type="comm:eventValue">
                                        <comm:value>In Progress</comm:value>
                                    </comm:value>
                                    <comm:label>On Hold</comm:label>
                                    <comm:value xsi:type="comm:eventValue">
                                        <comm:value>On Hold</comm:value>
                                    </comm:value>
                                    <comm:label>Completed</comm:label>
                                    <comm:value xsi:type="comm:eventValue">
                                        <comm:value>Completed</comm:value>
                                    </comm:value>
                                    <comm:label>Breached</comm:label>
                                    <comm:value xsi:type="comm:eventValue">
                                        <comm:value>Breached</comm:value>
                                    </comm:value>
                                </comm:boxSymbol>

                                <comm:boxSymbol
                                        xsi:type="comm:date"
                                        comm:id="fromDate"
                                        comm:label="From Date"
                                        comm:placeholder="YYYY-MM-DD"
                                        comm:value="{exportModel/fromDate}"
                                        comm:required="true"/>

                                <comm:boxSymbol
                                        xsi:type="comm:date"
                                        comm:id="toDate"
                                        comm:label="To Date"
                                        comm:placeholder="YYYY-MM-DD"
                                        comm:value="{exportModel/toDate}"
                                        comm:required="true"/>
                            </comm:box>
                        </comm:symbol>
                    </comm:sections>
                </comm:form>
            </symbol>
            <symbol xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                    xsi:type="comm:footer" comm:text="" comm:textAlign="left"
                    comm:buttonAlign="right">
                <xsl:if test="exportModel/buttonCheck = 'false'">
                    <comm:baseButton
                            comm:id="back"
                            comm:target="main"
                            comm:url="app-domain/mandates-and-resolutions/adminAll"
                            comm:label="Back"
                            comm:formSubmit="false"/>
                    <comm:baseButton
                            comm:id="export"
                            comm:label="Export"
                            comm:url="app-domain/mandates-and-resolutions/exportRequests"
                            comm:target="main"
                            comm:formSubmit="true"/>
                </xsl:if>
                <xsl:if test="exportModel/buttonCheck = 'true'">
                    <comm:baseButton
                            comm:id="back"
                            comm:target="main"
                            comm:url="app-domain/mandates-and-resolutions/returnToExport"
                            comm:label="Back"
                            comm:formSubmit="false"/>
                    <comm:baseButton comm:id="op1" comm:type="paper"
                                     comm:url="app-domain/mandates-and-resolutions/downloadCSV"
                                     comm:target="download" comm:formSubmit="false"
                                     comm:label="Download"/>
                </xsl:if>
            </symbol>
            <symbol xsi:type="comm:setEventAjax"
                    comm:url="app-domain/mandates-and-resolutions/returnToExport"
                    comm:id="op1" comm:target="main" comm:event="click"
                    comm:ajaxEventType="submitFormToWorkspace"
                    comm:formName="exportForm"/>
        </page>
    </xsl:template>
</xsl:stylesheet>