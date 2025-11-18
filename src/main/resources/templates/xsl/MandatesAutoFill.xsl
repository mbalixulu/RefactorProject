<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="xml"/>
    <xsl:template match="/">
        <page xmlns:comm="http://ws.online.fnb.co.za/v1/common/"
              id="WrapupLandingPage"
              title="Switch" template="main" layout="" version="1">
            <symbol xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                    xsi:type="comm:subTabGroup" comm:subTabGroupHeading="Mandates and Resolutions"/>
            <symbol xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                    xsi:type="comm:formLayout">
                <comm:form comm:action=""
                           comm:name="WrapupLandingForm">
                    <xsl:if test="requestWrapper/accountCheck = 'false'">
                        <comm:sections comm:width="full">
                            <comm:symbol xsi:type="comm:textHeading" comm:size="4">
                                <comm:value>Please Add your Account....(Without Account we can't
                                    Proceed).
                                </comm:value>
                            </comm:symbol>
                        </comm:sections>
                    </xsl:if>
                    <xsl:if test="requestWrapper/accountCheck = 'true'">
                        <xsl:for-each
                                select="requestWrapper/listOfAddAccount">
                            <comm:sections comm:width="full">
                                <comm:symbol xsi:type="comm:textHeading"
                                             comm:size="4">
                                    <comm:value>Account
                                        <xsl:value-of select="position()"/>
                                    </comm:value>
                                </comm:symbol>
                                <comm:symbol xsi:type="comm:boxContainer"
                                             comm:id="boxDiv">
                                    <comm:box xsi:type="comm:box">
                                        <comm:boxSymbol xsi:type="comm:boxSplit"
                                                        comm:width="25">
                                            <comm:boxSplitSymbol xsi:type="comm:textReadout"
                                                                 comm:subHeading=""
                                                                 comm:color="ghostmedium">
                                                <comm:value></comm:value>
                                            </comm:boxSplitSymbol>
                                        </comm:boxSymbol>
                                        <comm:boxSymbol xsi:type="comm:boxSplit"
                                                        comm:width="25">
                                            <comm:boxSplitSymbol xsi:type="comm:textReadout"
                                                                 comm:subHeading=""
                                                                 comm:color="ghostmedium">
                                                <comm:value></comm:value>
                                            </comm:boxSplitSymbol>
                                        </comm:boxSymbol>
                                        <comm:boxSymbol xsi:type="comm:boxSplit"
                                                        comm:width="25">
                                            <comm:boxSplitSymbol xsi:type="comm:button"
                                                                 comm:id="assignee"
                                                                 comm:target="main"
                                                                 comm:url="app-domain/mandates-and-resolutions/editSignatoryWithAccount/{userInList}"
                                                                 comm:label="Edit"
                                                                 comm:width="4"
                                                                 comm:formSubmit="false"
                                                                 comm:type="paper"/>
                                        </comm:boxSymbol>
                                        <comm:boxSymbol xsi:type="comm:boxSplit"
                                                        comm:width="25">
                                            <comm:boxSplitSymbol xsi:type="comm:button"
                                                                 comm:id="assignee"
                                                                 comm:target="main"
                                                                 comm:url="app-domain/mandates-and-resolutions/deleteSignatoryWithAccount/{userInList}"
                                                                 comm:label="Delete"
                                                                 comm:width="4"
                                                                 comm:formSubmit="false"
                                                                 comm:type="paper"/>
                                        </comm:boxSymbol>
                                        <comm:boxSymbol xsi:type="comm:boxSplit"
                                                        comm:width="50">
                                            <comm:boxSplitSymbol xsi:type="comm:textReadout"
                                                                 comm:subHeading="Account Name:"
                                                                 comm:color="ghostmedium">
                                                <comm:value>
                                                    <xsl:value-of
                                                            select="accountName"/>
                                                </comm:value>
                                            </comm:boxSplitSymbol>
                                        </comm:boxSymbol>
                                        <comm:boxSymbol xsi:type="comm:boxSplit"
                                                        comm:width="50">
                                            <comm:boxSplitSymbol xsi:type="comm:textReadout"
                                                                 comm:subHeading="Account Number:"
                                                                 comm:color="ghostmedium">
                                                <comm:value>
                                                    <xsl:value-of
                                                            select="accountNumber"/>
                                                </comm:value>
                                            </comm:boxSplitSymbol>
                                        </comm:boxSymbol>
                                    </comm:box>
                                </comm:symbol>
                                <!-- Directors Table -->
                                <comm:symbol xsi:type="comm:fullTable"
                                             comm:id="directorsTable{position()}"
                                             comm:action="GBLanding"
                                             comm:downloadLink=""
                                             comm:endpoint=""
                                             comm:heading="Signatory/ies"
                                             comm:showSearch="false"
                                             comm:showTotal="false"
                                             comm:showSaveAndPrint="true"
                                             comm:showPrintAndDownload="false"
                                             comm:headingColor="black">

                                    <comm:tableColumn comm:align="center" comm:fieldName="alias"
                                                      comm:groupId="name"
                                                      comm:heading="Full Name" comm:id="nameid"
                                                      comm:calcTotal="false"
                                                      comm:selectAll="false"/>
                                    <comm:tableColumn comm:align="center" comm:fieldName="alias"
                                                      comm:groupId="ID Number"
                                                      comm:heading="ID Number" comm:id="surnameid"
                                                      comm:calcTotal="false"
                                                      comm:selectAll="false"/>
                                    <comm:tableColumn comm:align="center" comm:fieldName="alias"
                                                      comm:groupId="instruction"
                                                      comm:heading="Instruction"
                                                      comm:id="instructionId"
                                                      comm:calcTotal="false"
                                                      comm:selectAll="false"/>
                                    <!-- Directors Group -->
                                    <comm:rowGroup xsi:type="comm:rowGroup"
                                                   comm:groupId="directors"
                                                   comm:groupHeaderLabel="Directors List">
                                    </comm:rowGroup>

                                    <!-- Render each director as editable row -->
                                    <xsl:for-each
                                            select="listOfSignatory">
                                        <comm:row xsi:type="comm:fullTableRow"
                                                  comm:groupId="xxxx">
                                            <comm:cell xsi:type="comm:cell"
                                                       comm:col_id="fullName">
                                                <comm:cellItem
                                                        xsi:type="comm:cellItem">
                                                    <comm:item xsi:type="comm:simpleText" comm:align="center">
                                                        <comm:value>
                                                            <xsl:value-of
                                                                    select="fullName"/>
                                                        </comm:value>
                                                    </comm:item>
                                                </comm:cellItem>
                                            </comm:cell>
                                            <comm:cell xsi:type="comm:cell"
                                                       comm:col_id="idNumber">
                                                <comm:cellItem
                                                        xsi:type="comm:cellItem">
                                                    <comm:item xsi:type="comm:simpleText" comm:align="center">
                                                        <comm:value>
                                                            <xsl:value-of
                                                                    select="idNumber"/>
                                                        </comm:value>
                                                    </comm:item>
                                                </comm:cellItem>
                                            </comm:cell>
                                            <comm:cell xsi:type="comm:cell"
                                                       comm:col_id="instruction">
                                                <comm:cellItem
                                                        xsi:type="comm:cellItem">
                                                    <comm:item xsi:type="comm:simpleText" comm:align="center">
                                                        <comm:value>
                                                            <xsl:value-of
                                                                    select="instruction"/>
                                                        </comm:value>
                                                    </comm:item>
                                                </comm:cellItem>
                                            </comm:cell>
                                        </comm:row>
                                    </xsl:for-each>
                                </comm:symbol>
                            </comm:sections>
                        </xsl:for-each>
                    </xsl:if>
                </comm:form>
            </symbol>
            <symbol xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="comm:footer"
                    comm:text="" comm:textAlign="left" comm:buttonAlign="right">
                <comm:baseButton comm:id="backBtn"
                                 comm:url="app-domain/mandates-and-resolutions/backToAccountSearch"
                                 comm:label="Back" comm:formSubmit="true" comm:target="main"/>
                <comm:baseButton comm:id="addAccountBtn"
                                 comm:url="app-domain/mandates-and-resolutions/addAccount"
                                 comm:label="Add Account" comm:formSubmit="true"
                                 comm:target="main"/>
                <xsl:if test="requestWrapper/accountCheck = 'true'">
                    <comm:baseButton comm:id="save"
                                     comm:url="app-domain/mandates-and-resolutions/saveAccounts"
                                     comm:label="Save"
                                     comm:formSubmit="true"
                                     comm:target="main"/>
                    <comm:baseButton comm:id="proceed"
                                     comm:url="app-domain/mandates-and-resolutions/proceedToSignaturePage"
                                     comm:label="Proceed" comm:formSubmit="true" comm:target="main"/>
                </xsl:if>
            </symbol>
        </page>
    </xsl:template>
</xsl:stylesheet>