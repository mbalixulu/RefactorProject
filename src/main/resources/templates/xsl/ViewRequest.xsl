<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml" indent="yes"/>
    <xsl:template match="/">
        <page xmlns:ns1="http://ws.online.fnb.co.za/v1/common/" id="mandatesAutoFillForm" title="Mandates Auto Fill Form" template="main" layout="" version="1">
            <!--Page heading-->
            <symbol xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="ns1:subTabGroup" ns1:subTabGroupHeading="Mandates and resolution"/>
            <!--Page layout-->
            <symbol xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="ns1:formLayout">
                <ns1:form ns1:action="http://localhost:8080/bifrost/" ns1:name="salesForm">
                    <!--Sub heading-->
                    <ns1:sections ns1:align="left" ns1:width="full">
                        <ns1:symbol xsi:type="ns1:textHeading" ns1:align="left">

                            <ns1:value>View Request</ns1:value>
                        </ns1:symbol>
                    </ns1:sections>

                    <!--Account section-->
                    <ns1:sections ns1:align="left" ns1:width="full">

                        <ns1:symbol xsi:type="ns1:boxContainer" ns1:id="boxDiv">
                            <ns1:box xsi:type="ns1:box">
                                <!--Left Side-->
                                <ns1:boxSymbol xsi:type="ns1:boxSplit" ns1:width="50">
                                    <!--Request ID-->
                                    <ns1:boxSplitSymbol xsi:type="ns1:input" ns1:name="requestID" ns1:label="Request ID" ns1:inputType="text" ns1:message="" ns1:unCheckedValue="No" ns1:selected="true" ns1:maxlength="50">
                                        <ns1:value/>
                                    </ns1:boxSplitSymbol>
                                    <!--Request Type-->
                                    <ns1:boxSplitSymbol xsi:type="ns1:input" ns1:name="requestType" ns1:label="Request Type" ns1:inputType="text" ns1:message="" ns1:unCheckedValue="No" ns1:selected="true" ns1:maxlength="50">
                                        <ns1:value/>
                                    </ns1:boxSplitSymbol>
                                    <!--Request Type-->
                                    <ns1:boxSplitSymbol xsi:type="ns1:input" ns1:name="requestType" ns1:label="Request Type" ns1:inputType="text" ns1:message="" ns1:unCheckedValue="No" ns1:selected="true" ns1:maxlength="50">
                                        <ns1:value/>
                                    </ns1:boxSplitSymbol>
                                    <!--Substatus-->
                                    <ns1:boxSplitSymbol xsi:type="ns1:input" ns1:name="subStatus" ns1:label="Substatus" ns1:inputType="text" ns1:message="" ns1:unCheckedValue="No" ns1:selected="true" ns1:maxlength="50">
                                        <ns1:value/>
                                    </ns1:boxSplitSymbol>
                                    <!--Last modified-->
                                    <ns1:boxSplitSymbol xsi:type="ns1:input" ns1:name="lastModified" ns1:label="Last modified" ns1:inputType="text" ns1:message="" ns1:unCheckedValue="No" ns1:selected="true" ns1:maxlength="50">
                                        <ns1:value/>
                                    </ns1:boxSplitSymbol>
                                </ns1:boxSymbol>

                                <!--Right side-->
                                <ns1:boxSymbol xsi:type="ns1:boxSplit" ns1:width="50">
                                    <!--Company name-->
                                    <ns1:boxSplitSymbol xsi:type="ns1:input" ns1:name="companyName" ns1:label="Company name" ns1:inputType="text" ns1:message="" ns1:unCheckedValue="No" ns1:selected="true" ns1:maxlength="50">
                                        <ns1:value/>
                                    </ns1:boxSplitSymbol>
                                    <!--Status-->
                                    <ns1:boxSplitSymbol xsi:type="ns1:input" ns1:name="status" ns1:label="Status" ns1:inputType="text" ns1:message="" ns1:unCheckedValue="No" ns1:selected="true" ns1:maxlength="50">
                                        <ns1:value/>
                                    </ns1:boxSplitSymbol>
                                    <!--Status-->
                                    <ns1:boxSplitSymbol xsi:type="ns1:input" ns1:name="status" ns1:label="Status" ns1:inputType="text" ns1:message="" ns1:unCheckedValue="No" ns1:selected="true" ns1:maxlength="50">
                                        <ns1:value/>
                                    </ns1:boxSplitSymbol>
                                    <!--Created date-->
                                    <ns1:boxSplitSymbol xsi:type="ns1:input" ns1:name="createdDate" ns1:label="Created date" ns1:inputType="text" ns1:message="" ns1:unCheckedValue="No" ns1:selected="true" ns1:maxlength="50">
                                        <ns1:value/>
                                    </ns1:boxSplitSymbol>
                                    <!--SLA-->
                                    <ns1:boxSplitSymbol xsi:type="ns1:progressTracker" ns1:date="15 Apr" ns1:day="Mon" ns1:heading="SLA" ns1:type="completed" ns1:showLiner="false"/>
                                </ns1:boxSymbol>
                            </ns1:box>
                        </ns1:symbol>


                        <ns1:symbol xsi:type="ns1:boxContainer" ns1:id="boxDiv">
                            <ns1:box xsi:type="ns1:box">

                                <!--Left Side-->
                                <ns1:boxSymbol xsi:type="ns1:boxSplit" ns1:width="50">
                                    <ns1:boxSplitSymbol xsi:type="ns1:input" ns1:name="accountName"
                                                        ns1:label="Account Name" ns1:inputType="text" ns1:message=""
                                                        ns1:unCheckedValue="No" ns1:selected="true" ns1:maxlength="50">
                                        <ns1:value/>
                                    </ns1:boxSplitSymbol>
                                </ns1:boxSymbol>

                                <!--Right side-->
                                <ns1:boxSymbol xsi:type="ns1:boxSplit" ns1:width="50">
                                    <ns1:boxSplitSymbol xsi:type="ns1:input" ns1:name="accountNo"
                                                        ns1:label="Account no." ns1:inputType="text" ns1:message=""
                                                        ns1:unCheckedValue="No" ns1:selected="true" ns1:maxlength="50">
                                        <ns1:value/>
                                    </ns1:boxSplitSymbol>
                                </ns1:boxSymbol>


                                <ns1:boxSymbol xsi:type="ns1:fullTable" ns1:id="MyTable" ns1:action="GBLanding"
                                               ns1:downloadLink="" ns1:endpoint=""
                                               ns1:heading="Add appointed signatory/ies" ns1:showTotal="false"
                                               ns1:defaultSortIndex="0" ns1:defaultSortDirection="ascending"
                                               ns1:headingColor="black">
                                    <ns1:addButton xsi:type="ns1:imageButton" ns1:target="main" ns1:id="addSignatoryBtn" ns1:label="Add a signatory" ns1:tooltip="true" ns1:tip="tip" ns1:url="app-domain/ui" ns1:formName="addSignatoryForm">
                                        <ns1:imageButtonOptions xsi:type="ns1:hyperlinkList" ns1:id="addSignatoryBtn">
                                            <ns1:hyperlinkListItem xsi:type="ns1:hyperlinkListItem" ns1:target="main" ns1:label="Add a signatory" ns1:url="app-domain/ui"/>
                                        </ns1:imageButtonOptions>
                                    </ns1:addButton>

                                    <ns1:tableColumn ns1:align="left" ns1:fieldName="fullName" ns1:groupId="group1"
                                                     ns1:heading="Full name (As per identity document)" ns1:id="nameid"
                                                     ns1:calcTotal="false" ns1:selectAll="false"
                                                     ns1:disableSorting="true"/>
                                    <ns1:tableColumn ns1:align="left" ns1:fieldName="idNumber" ns1:groupId="group1"
                                                     ns1:heading="ID number" ns1:id="aliasid" ns1:calcTotal="false"
                                                     ns1:selectAll="false" ns1:disableSorting="true"/>
                                    <ns1:tableColumn ns1:align="left" ns1:fieldName="instructions" ns1:groupId="group1"
                                                     ns1:heading="Instruction" ns1:id="codeid" ns1:calcTotal="false"
                                                     ns1:selectAll="false" ns1:disableSorting="true"/>

                                    <ns1:rowGroup xsi:type="ns1:rowGroup" ns1:groupId="signatory"
                                                  ns1:groupHeaderLabel="">
                                        <ns1:totalsRow ns1:category=" ">
                                            <ns1:cell xsi:type="ns1:cell" ns1:col_id="alias">
                                                <ns1:cellItem xsi:type="ns1:cellItem">
                                                    <ns1:item xsi:type="ns1:simpleText">
                                                        <ns1:value/>
                                                    </ns1:item>
                                                </ns1:cellItem>
                                            </ns1:cell>
                                        </ns1:totalsRow>
                                        <ns1:groupTableButton xsi:type="ns1:imageButton" ns1:id="addSignatoryBtn"
                                                              ns1:label="Add a signatory"
                                                              ns1:tip="tip of the button" ns1:url="app-domain/ui"/>
                                    </ns1:rowGroup>
                                    <!--Full name input field-->
                                    <ns1:row xsi:type="ns1:fullTableRow" ns1:groupId="signatory">
                                        <ns1:cell xsi:type="ns1:cell" ns1:col_id="fullName">
                                            <ns1:cellItem xsi:type="ns1:cellItem">
                                                <ns1:item xsi:type="ns1:input" ns1:name="fullName">
                                                    <ns1:value/>
                                                </ns1:item>
                                            </ns1:cellItem>
                                        </ns1:cell>
                                        <!--ID number input field-->
                                        <ns1:cell xsi:type="ns1:cell" ns1:col_id="idNumber">
                                            <ns1:cellItem xsi:type="ns1:cellItem">
                                                <ns1:item xsi:type="ns1:input" ns1:name="idNumber">
                                                    <ns1:value/>
                                                </ns1:item>
                                            </ns1:cellItem>
                                        </ns1:cell>
                                        <!--Instruction drop-down field-->
                                        <ns1:cell xsi:type="ns1:cell" ns1:col_id="instruction">
                                            <ns1:cellItem xsi:type="ns1:cellItem">
                                                <ns1:item xsi:type="ns1:input" ns1:name="instruction">
                                                    <ns1:value/>
                                                </ns1:item>
                                            </ns1:cellItem>
                                        </ns1:cell>
                                    </ns1:row>
                                </ns1:boxSymbol>
                            </ns1:box>
                        </ns1:symbol>
                    </ns1:sections>
                </ns1:form>
            </symbol>

            <symbol xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="ns1:footer" ns1:text="" ns1:textAlign="left" ns1:buttonAlign="right">
                <ns1:baseButton ns1:id="editBtn" ns1:url="app-domain/ui/viewRequest" ns1:target="panel" ns1:formSubmit="false" ns1:label="Edit"/>
                <ns1:baseButton ns1:id="overrideBtn" ns1:url="app-domain/ui/viewRequest" ns1:target="panel" ns1:formSubmit="false" ns1:label="Override Status"/>
                <ns1:baseButton ns1:id="reject" ns1:url="app-domain/ui/viewRequestReject" ns1:target="panel" ns1:formSubmit="false" ns1:label="Reject"/>
                <ns1:baseButton ns1:id="approve" ns1:url="app-domain/ui/viewRequestSuccess" ns1:target="main" ns1:formSubmit="false" ns1:label="Approve"/>
                <ns1:baseButton ns1:id="back" ns1:url="app-domain/ui/requestTable" ns1:target="main" ns1:formSubmit="false" ns1:label="Back"/>
            </symbol>
        </page>
    </xsl:template>
</xsl:stylesheet>