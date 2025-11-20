<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="xml"/>
    <xsl:template match="/">
        <page xmlns:comm="http://ws.online.fnb.co.za/v1/common/"
              id="WrapupLandingPage"
              title="Switch" template="main" layout="" version="1">
            <symbol xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                    xsi:type="comm:subTabGroup" comm:subTabGroupHeading="Add Request"/>
            <symbol xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                    xsi:type="comm:formLayout">
                <comm:form comm:name="searchResults"
                           comm:action="">
                    <comm:sections comm:width="full">
                        <comm:symbol xsi:type="comm:textHeading"
                                     comm:size="4">
                            <comm:value>Account</comm:value>
                        </comm:symbol>
                        <comm:symbol xsi:type="comm:boxContainer"
                                     comm:id="boxDiv">
                            <comm:box xsi:type="comm:box">
                                <comm:boxSymbol xsi:type="comm:input"
                                                comm:name="accountName"
                                                comm:label="Account Name *"
                                                comm:inputType="text" comm:message=""
                                                comm:unCheckedValue="No"
                                                comm:selected="true"
                                                comm:errorMessage="{addAccountModel/signatoryErrorModel/accountName}"
                                                comm:required="true"
                                                comm:maxlength="50">
                                    <comm:value>
                                        <xsl:value-of
                                                select="addAccountModel/accountName"/>
                                    </comm:value>
                                </comm:boxSymbol>
                                <comm:boxSymbol xsi:type="comm:input"
                                                comm:name="accountNo"
                                                comm:label="Account No *"
                                                comm:inputType="text" comm:message=""
                                                comm:unCheckedValue="No"
                                                comm:selected="true"
                                                comm:errorMessage="{addAccountModel/signatoryErrorModel/accountNumber}"
                                                comm:required="true"
                                                comm:maxlength="50">
                                    <comm:value>
                                        <xsl:value-of
                                                select="addAccountModel/accountNumber"/>
                                    </comm:value>
                                </comm:boxSymbol>
                            </comm:box>
                        </comm:symbol>

                        <comm:symbol xsi:type="comm:fullTable"
                                     comm:id="directorsTable"
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
                                              comm:widthPercent="25"
                                              comm:selectAll="false"/>
                            <comm:tableColumn comm:align="center" comm:fieldName="alias"
                                              comm:groupId="ID Number"
                                              comm:heading="ID Number" comm:id="surnameid"
                                              comm:calcTotal="false"
                                              comm:widthPercent="25"
                                              comm:selectAll="false"/>
                            <comm:tableColumn comm:align="center" comm:fieldName="alias"
                                              comm:groupId="instruction"
                                              comm:heading="Instruction" comm:id="instructionId"
                                              comm:calcTotal="false"
                                              comm:widthPercent="25"
                                              comm:selectAll="false"/>
                            <comm:tableColumn comm:align="" comm:fieldName="remove"
                                              comm:groupId="edit"
                                              comm:heading="" comm:id="space"
                                              comm:calcTotal="false"
                                              comm:widthPercent="1"
                                              comm:selectAll="false"/>
                            <comm:tableColumn comm:align="center" comm:fieldName="remove"
                                              comm:groupId="edit"
                                              comm:heading="Edit" comm:id="editScreen"
                                              comm:calcTotal="false"
                                              comm:widthPercent="5"
                                              comm:selectAll="false"/>
                            <comm:tableColumn comm:align="" comm:fieldName="remove"
                                              comm:groupId="edit"
                                              comm:heading="" comm:id="space"
                                              comm:calcTotal="false"
                                              comm:widthPercent="5"
                                              comm:selectAll="false"/>
                            <comm:tableColumn comm:align="center" comm:fieldName="remove"
                                              comm:groupId="remove"
                                              comm:heading="Remove" comm:id="remove"
                                              comm:calcTotal="false"
                                              comm:widthPercent="10"
                                              comm:selectAll="false"/>

                            <!-- Directors Group -->
                            <comm:rowGroup xsi:type="comm:rowGroup"
                                           comm:groupId="directors"
                                           comm:groupHeaderLabel="Directors List">
                                <comm:groupTableButton xsi:type="comm:imageButton"
                                                       comm:tooltip="true"
                                                       comm:formName="searchResults"
                                                       comm:tip="" comm:target="main"
                                                       comm:url="app-domain/mandates-and-resolutions/signatoryTablePopup"
                                                       comm:id="gp1"/>
                            </comm:rowGroup>

                            <!-- Render each director as editable row -->
                            <xsl:for-each select="addAccountModel/listOfSignatory">
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
                                    <comm:cell xsi:type="comm:cell"
                                               comm:col_id="space">
                                        <comm:cellItem
                                                xsi:type="comm:cellItem">
                                            <comm:item
                                                    xsi:type="comm:textReadout">
                                                <comm:value>-</comm:value>
                                            </comm:item>
                                        </comm:cellItem>
                                    </comm:cell>
                                    <comm:cell xsi:type="comm:cell"
                                               comm:col_id="editScreen">
                                        <comm:cellItem
                                                xsi:type="comm:cellItem">
                                            <comm:item xsi:type="comm:button"
                                                       comm:id="edit"
                                                       comm:type="paper"
                                                       comm:width="4"
                                                       comm:url="app-domain/mandates-and-resolutions/SignatoryEdit/{userInList}"
                                                       comm:formSubmit="false"
                                                       comm:target="main"
                                                       comm:tooltip=""
                                                       comm:label="Edit"/>
                                        </comm:cellItem>
                                    </comm:cell>
                                    <comm:cell xsi:type="comm:cell"
                                               comm:col_id="space">
                                        <comm:cellItem
                                                xsi:type="comm:cellItem">
                                            <comm:item
                                                    xsi:type="comm:textReadout">
                                                <comm:value>-</comm:value>
                                            </comm:item>
                                        </comm:cellItem>
                                    </comm:cell>
                                    <!-- Remove (Delete) row -->
                                    <comm:cell comm:col_id="remove">
                                        <comm:cellItem>
                                            <comm:item
                                                    xsi:type="comm:button"
                                                    comm:id="remove"
                                                    comm:type="paper"
                                                    comm:width="2"
                                                    comm:target="main"
                                                    comm:formSubmit="false"
                                                    comm:label="Remove"
                                                    comm:url="app-domain/mandates-and-resolutions/SignatoryRemove/{userInList}"/>
                                        </comm:cellItem>
                                    </comm:cell>
                                </comm:row>
                            </xsl:for-each>
                        </comm:symbol>
                    </comm:sections>
                    <xsl:if test="addAccountModel/checkSignatoryList = 'true'">
                        <comm:sections comm:width="full">
                            <comm:symbol xsi:type="comm:textHeading" comm:size="4">
                                <comm:value>At least one Signatory is required for this request !
                                </comm:value>
                            </comm:symbol>
                        </comm:sections>
                    </xsl:if>
                </comm:form>
            </symbol>
            <symbol xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="comm:footer"
                    comm:text="" comm:textAlign="left" comm:buttonAlign="right">
                <comm:baseButton comm:id="proceed" comm:target="main"
                                 comm:url="app-domain/mandates-and-resolutions/cancelAddAccount"
                                 comm:label="Cancel" comm:formSubmit="true"/>
                <xsl:if test="addAccountModel/buttonCheck = 'false'">
                    <comm:baseButton comm:id="backSearch" comm:target="main"
                                     comm:url="app-domain/mandates-and-resolutions/addSignatoryWithAccount"
                                     comm:label="Add +" comm:formSubmit="true"/>
                </xsl:if>
                <xsl:if test="addAccountModel/buttonCheck = 'true'">
                    <comm:baseButton comm:id="backSearch" comm:target="main"
                                     comm:url="app-domain/mandates-and-resolutions/updateSignatoryWithAccount/{addAccountModel/userInList}"
                                     comm:label="Update" comm:formSubmit="true"/>
                </xsl:if>
            </symbol>

        </page>
    </xsl:template>
</xsl:stylesheet>