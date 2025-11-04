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

            <symbol xsi:type="comm:formLayout" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
                <comm:form comm:name="exportForm" comm:action="app-domain/mandates-and-resolutions/exportRequests">
                    <comm:sections comm:width="full">
                        <comm:symbol xsi:type="comm:boxContainer" comm:id="boxDiv">
                            <comm:box xsi:type="comm:box">
                                <comm:boxSymbol xsi:type="comm:textHeading" comm:size="4">
                                    <comm:value>Report details</comm:value>
                                </comm:boxSymbol>

                                <comm:boxSymbol xsi:type="comm:boxSplit" comm:width="50">
                                    <comm:boxSplitSymbol
                                            xsi:type="comm:dropdown"
                                            comm:id="status"
                                            comm:label="Status"
                                            comm:pleaseSelectOptionEnabled="true"
                                            comm:required="true">
                                        <!-- pull from RequestWrapper.lovs.statuses -->
                                        <xsl:for-each select="//*[local-name()='lovs']/*[local-name()='statuses']/*[local-name()='status']">
                                            <xsl:variable name="v" select="normalize-space(string(.))"/>
                                            <xsl:if test="string-length($v) &gt; 0">
                                                <comm:label><xsl:value-of select="$v"/></comm:label>
                                                <comm:value xsi:type="comm:eventValue">
                                                    <comm:value><xsl:value-of select="$v"/></comm:value>
                                                </comm:value>
                                            </xsl:if>
                                        </xsl:for-each>
                                    </comm:boxSplitSymbol>

                                    <comm:boxSplitSymbol
                                            xsi:type="comm:date"
                                            comm:id="fromDate"
                                            comm:label="From Date"
                                            comm:placeholder="YYYY-MM-DD"
                                            comm:required="true"/>

                                    <comm:boxSplitSymbol
                                            xsi:type="comm:date"
                                            comm:id="toDate"
                                            comm:label="To Date"
                                            comm:placeholder="YYYY-MM-DD"
                                            comm:required="true"/>
                                </comm:boxSymbol>

<!--                                <comm:boxSymbol-->
<!--                                        xsi:type="comm:button"-->
<!--                                        comm:id="export"-->
<!--                                        comm:label="Export"-->
<!--                                        comm:type="primary"-->
<!--                                        comm:width="3"-->
<!--                                        comm:url="app-domain/mandates-and-resolutions/exportRequests"-->
<!--                                        comm:target="download"-->
<!--                                        comm:formSubmit="true"-->
<!--                                        comm:formName="exportForm"/>-->

                            </comm:box>
                        </comm:symbol>
                    </comm:sections>
                </comm:form>
            </symbol>

            <!-- Footer button must submit the named form -->
            <symbol xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                    xsi:type="comm:footer" comm:text="" comm:textAlign="left" comm:buttonAlign="right">
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
                        comm:target="download"
                        comm:formSubmit="true"/>
            </symbol>
        </page>
    </xsl:template>
</xsl:stylesheet>