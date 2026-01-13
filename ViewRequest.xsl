<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="xml"/>
    <xsl:template match="/">
        <page xmlns:comm="http://ws.online.fnb.co.za/v1/common/"
              xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
              id="boxConatinerID"
              title="Box Container" template="main" layout="" version="1">
            <symbol xsi:type="comm:formLayout">
                <comm:form comm:name="advancedSearchForm"
                           comm:action="">
                    <comm:sections comm:width="full">
                        <comm:symbol xsi:type="comm:textHeading" comm:size="4">
                            <comm:value>View Request:</comm:value>
                        </comm:symbol>
                        <comm:symbol xsi:type="comm:boxContainer"
                                     comm:id="boxDiv">
                            <comm:boxSymbol xsi:type="comm:textHeading"
                                            comm:size="4">
                                <comm:value></comm:value>
                            </comm:boxSymbol>

                            <comm:boxSymbol xsi:type="comm:boxSplit"
                                            comm:width="50">
                                <comm:boxSplitSymbol xsi:type="comm:textReadout"
                                                     comm:subHeading="Back"
                                                     comm:color="ghostmedium">
                                    <comm:value>
                                        <xsl:value-of
                                                select="requestDetails/back"/>
                                    </comm:value>
                                </comm:boxSplitSymbol>
                                <comm:boxSplitSymbol xsi:type="comm:textReadout"
                                                     comm:subHeading="Hold"
                                                     comm:color="ghostmedium">
                                    <comm:value>
                                        <xsl:value-of
                                                select="requestDetails/hold"/>
                                    </comm:value>
                                </comm:boxSplitSymbol>
                                <comm:boxSplitSymbol xsi:type="comm:textReadout"
                                                     comm:subHeading="Edit"
                                                     comm:color="ghostmedium">
                                    <comm:value>
                                        <xsl:value-of
                                                select="requestDetails/edit"/>
                                    </comm:value>
                                </comm:boxSplitSymbol>
                                <comm:boxSplitSymbol xsi:type="comm:textReadout"
                                                     comm:subHeading="Reject"
                                                     comm:color="ghostmedium">
                                    <comm:value>
                                        <xsl:value-of
                                                select="requestDetails/reject"/>
                                    </comm:value>
                                </comm:boxSplitSymbol>
                                <comm:boxSplitSymbol xsi:type="comm:textReadout"
                                                     comm:subHeading="Verify for Hogan"
                                                     comm:color="ghostmedium">
                                    <comm:value>
                                        <xsl:value-of
                                                select="requestDetails/verifyForHogan"/>
                                    </comm:value>
                                </comm:boxSplitSymbol>
                            </comm:boxSymbol>
                        </comm:symbol>
                    </comm:sections>
                </comm:form>
            </symbol>
        </page>
    </xsl:template>
</xsl:stylesheet>