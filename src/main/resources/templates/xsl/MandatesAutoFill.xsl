<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:ns1="http://ws.online.fnb.co.za/v1/common/"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">

    <xsl:output method="xml" indent="yes"/>

    <xsl:template match="/">
        <page xmlns:ns1="http://ws.online.fnb.co.za/v1/common/" id="mandatesAutoFillForm" title="Mandates Auto Fill Form" template="main" layout=""
              version="1">

            <!--Page Heading-->
            <symbol xsi:type="ns1:subTabGroup" ns1:subTabGroupHeading="Mandates"/>

            <symbol xsi:type="ns1:formLayout">
                <ns1:form ns1:action="http://localhost:8080/bifrost/"
                          ns1:name="salesForm">

                    <!--Heading-->
                    <ns1:sections ns1:align="left" ns1:width="full">
                        <ns1:symbol xsi:type="ns1:textHeading">
                            <ns1:value>Auto fill</ns1:value>
                        </ns1:symbol>
                    </ns1:sections>

                    <!--Accounts with Embedded Signatory Tables-->
                    <ns1:sections ns1:align="left" ns1:width="full">
                        <xsl:for-each select="/requestWrapper/request/accounts/account">
                            <xsl:variable name="pos" select="position()" />

                            <ns1:symbol xsi:type="ns1:boxContainer" ns1:id="{concat('box_', $pos)}">
                                <ns1:box xsi:type="ns1:box">

                                    <!--Account Title-->
                                    <ns1:boxSymbol xsi:type="ns1:textHeading" ns1:size="4">
                                        <ns1:value>
                                            <xsl:text>Account name </xsl:text>
                                            <xsl:value-of select="$pos" />
                                        </ns1:value>
                                    </ns1:boxSymbol>

                                    <!--Account Name Input-->
                                    <ns1:boxSymbol xsi:type="ns1:boxSplit" ns1:width="50">
                                        <ns1:boxSplitSymbol xsi:type="ns1:input"
                                                            ns1:name="{concat('accountName_', $pos)}"
                                                            ns1:label="Account Name"
                                                            ns1:inputType="text">
                                            <ns1:value><xsl:value-of select="accountName"/></ns1:value>
                                        </ns1:boxSplitSymbol>
                                    </ns1:boxSymbol>

                                    <!--Account No Input-->
                                    <ns1:boxSymbol xsi:type="ns1:boxSplit" ns1:width="50">
                                        <ns1:boxSplitSymbol xsi:type="ns1:input"
                                                            ns1:name="{concat('accountNo_', $pos)}"
                                                            ns1:label="Account no."
                                                            ns1:inputType="text">
                                            <ns1:value><xsl:value-of select="accountNo"/></ns1:value>
                                        </ns1:boxSplitSymbol>
                                    </ns1:boxSymbol>

                                    <!--Signatory Table-->
                                    <ns1:boxSymbol xsi:type="ns1:fullTable"
                                                   ns1:id="{concat('signTable_', $pos)}"
                                                   ns1:heading="Add appointed signatory/ies"
                                                   ns1:showTotal="false"
                                                   ns1:headingColor="black">

                                        <!-- Table Columns -->
                                        <ns1:tableColumn ns1:align="left" ns1:fieldName="fullName" ns1:heading="Full Name" ns1:disableSorting="true"/>
                                        <ns1:tableColumn ns1:align="left" ns1:fieldName="idNumber" ns1:heading="ID Number" ns1:disableSorting="true"/>
                                        <ns1:tableColumn ns1:align="left" ns1:fieldName="instruction" ns1:heading="Instruction" ns1:disableSorting="true"/>
                                        <ns1:tableColumn ns1:align="left" ns1:fieldName="remove" ns1:heading="Remove" ns1:disableSorting="true"/>

                                        <!-- RowGroup -->
                                        <ns1:rowGroup xsi:type="ns1:rowGroup"
                                                      ns1:groupId="{concat('signatory_', $pos)}"
                                                      ns1:groupHeaderLabel="">
                                            <ns1:totalsRow ns1:category=" ">
                                                <ns1:cell xsi:type="ns1:cell" ns1:col_id="fullName">
                                                    <ns1:cellItem xsi:type="ns1:cellItem">
                                                        <ns1:item xsi:type="ns1:simpleText">
                                                            <ns1:value/>
                                                        </ns1:item>
                                                    </ns1:cellItem>
                                                </ns1:cell>
                                            </ns1:totalsRow>

                                            <!--Add Signatory Button-->
                                            <ns1:groupTableButton xsi:type="ns1:imageButton"
                                                                  ns1:id="{concat('addSignBtn_', $pos)}"
                                                                  ns1:label="Add a signatory"
                                                                  ns1:tip="Add a signatory"
                                                                  ns1:url="{concat('app-domain/ui/mandatesFill?accountCount=', count(/requestWrapper/request/accounts/account), '&amp;addSignatoryAt=', $pos)}"/>
                                        </ns1:rowGroup>

                                        <!--Existing Signatories-->
                                        <xsl:for-each select="signatories/signatory">
                                            <xsl:variable name="signPos" select="position()" />
                                            <ns1:row xsi:type="ns1:fullTableRow" ns1:groupId="{concat('signatory_', $pos)}">

                                                <!--Full Name-->
                                                <ns1:cell xsi:type="ns1:cell" ns1:col_id="fullName">
                                                    <ns1:cellItem xsi:type="ns1:cellItem">
                                                        <ns1:item xsi:type="ns1:input"
                                                                  ns1:name="{concat('fullName_', $pos, '_', $signPos)}">
                                                            <ns1:value><xsl:value-of select="fullName"/></ns1:value>
                                                        </ns1:item>
                                                    </ns1:cellItem>
                                                </ns1:cell>

                                                <!--ID Number-->
                                                <ns1:cell xsi:type="ns1:cell" ns1:col_id="idNumber">
                                                    <ns1:cellItem xsi:type="ns1:cellItem">
                                                        <ns1:item xsi:type="ns1:input"
                                                                  ns1:name="{concat('idNumber_', $pos, '_', $signPos)}">
                                                            <ns1:value><xsl:value-of select="idNumber"/></ns1:value>
                                                        </ns1:item>
                                                    </ns1:cellItem>
                                                </ns1:cell>

                                                <!--Instruction-->
                                                <ns1:cell xsi:type="ns1:cell" ns1:col_id="instruction">
                                                    <ns1:cellItem xsi:type="ns1:cellItem">
                                                        <ns1:item xsi:type="ns1:input"
                                                                  ns1:name="{concat('instruction_', $pos, '_', $signPos)}">
                                                            <ns1:value><xsl:value-of select="instruction"/></ns1:value>
                                                        </ns1:item>
                                                    </ns1:cellItem>
                                                </ns1:cell>

                                                <!--Remove Button Column -->
                                                <ns1:cell xsi:type="ns1:cell" ns1:col_id="remove">
                                                    <ns1:cellItem xsi:type="ns1:cellItem">
                                                        <ns1:item xsi:type="ns1:button"
                                                                  ns1:id="{concat('removeSignBtn_', $pos, '_', $signPos)}"
                                                                  ns1:type="paper"
                                                                  ns1:label="Remove"
                                                                  ns1:formSubmit="false"
                                                                  ns1:target="main"
                                                                  ns1:width="2"
                                                                  ns1:url="{concat('app-domain/ui/mandatesFill?accountCount=', count(/requestWrapper/request/accounts/account), '&amp;removeSignatoryAt=', $pos, '_', $signPos)}"/>
                                                    </ns1:cellItem>
                                                </ns1:cell>
                                            </ns1:row>
                                        </xsl:for-each>
                                    </ns1:boxSymbol>

                                    <!--Remove Account Button-->
                                    <ns1:boxSymbol xsi:type="ns1:button"
                                                   ns1:align="center"
                                                   ns1:id="{concat('deleteBtn_', $pos)}"
                                                   ns1:target="main"
                                                   ns1:label="Delete Account"
                                                   ns1:width="3"
                                                   ns1:formSubmit="false"
                                                   ns1:type="primary"
                                                   ns1:url="{concat('app-domain/ui/mandatesFill?accountCount=', count(/requestWrapper/request/accounts/account), '&amp;removeAccountAt=', $pos)}"/>
                                </ns1:box>
                            </ns1:symbol>
                        </xsl:for-each>
                    </ns1:sections>
                </ns1:form>
            </symbol>

            <!-- Footer -->
            <symbol xsi:type="ns1:footer" ns1:buttonAlign="right">
                <ns1:baseButton ns1:id="next"
                                ns1:url="app-domain/ui/mandatesSignatureCard"
                                ns1:label="Next"
                                ns1:formSubmit="false"
                                ns1:target="main"/>
                <ns1:baseButton ns1:id="addAccountBtn"
                                ns1:url="{concat('app-domain/ui/mandatesFill?accountCount=', count(/requestWrapper/request/accounts/account) + 1)}"
                                ns1:label="Add Account"
                                ns1:formSubmit="false"
                                ns1:target="main"/>
                <ns1:baseButton ns1:id="backBtn"
                                ns1:url="app-domain/ui/createRequest"
                                ns1:label="Back"
                                ns1:formSubmit="false"
                                ns1:target="main"/>
            </symbol>
        </page>
    </xsl:template>
</xsl:stylesheet>