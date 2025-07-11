<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml" indent="yes"/>
    <xsl:template match="/">
        <page xmlns:ns1="http://ws.online.fnb.co.za/v1/common/" id="mandatesSignatureForm" title="Mandates Signature Form" template="main" layout="" version="1">
            <!--Page heading-->
            <symbol xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="ns1:subTabGroup" ns1:subTabGroupHeading="Mandates"/>
            <!--Page layout-->
            <symbol xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="ns1:formLayout">
                <ns1:form ns1:action="http://localhost:8080/bifrost/" ns1:name="salesForm">
                    <!--Sub heading-->
                    <ns1:sections ns1:align="left" ns1:width="full">
                        <ns1:symbol xsi:type="ns1:textHeading">
                            <ns1:value>Signature card confirmation</ns1:value>
                        </ns1:symbol>
                    </ns1:sections>
                    <!--Account section-->
                    <ns1:sections ns1:align="left" ns1:width="full">
                        <ns1:symbol xsi:type="ns1:boxContainer" ns1:id="boxDiv">
                            <ns1:box xsi:type="ns1:box">
                                <ns1:boxSymbol xsi:type="ns1:textHeading" ns1:size="3">
                                    <ns1:value>Account name 1</ns1:value>
                                </ns1:boxSymbol>
                                <!--Box heading-->
                                <ns1:boxSymbol xsi:type="ns1:textHeading">
                                    <ns1:value>Added Signatory</ns1:value>
                                </ns1:boxSymbol>

                                <!--Left Side-->
                                <ns1:boxSymbol xsi:type="ns1:boxSplit" ns1:width="50">
                                    <!--Full name-->
                                    <ns1:boxSplitSymbol xsi:type="ns1:input" ns1:name="fullName" ns1:label="Full name (As per identity document)" ns1:inputType="text" ns1:message="" ns1:unCheckedValue="No" ns1:selected="true" ns1:maxlength="50">
                                        <ns1:value/>
                                    </ns1:boxSplitSymbol>
                                    <!--Capacity-->
                                    <ns1:boxSplitSymbol xsi:type="ns1:input" ns1:name="capacity" ns1:label="Capacity (i.e Director,Manager etc...)" ns1:inputType="text" ns1:message="" ns1:unCheckedValue="No" ns1:selected="true" ns1:maxlength="50">
                                        <ns1:value/>
                                    </ns1:boxSplitSymbol>
                                </ns1:boxSymbol>

                                <!--Right side-->
                                <ns1:boxSymbol xsi:type="ns1:boxSplit" ns1:width="50">
                                    <!--ID number-->
                                    <ns1:boxSplitSymbol xsi:type="ns1:input" ns1:name="idNumber" ns1:label="ID no." ns1:inputType="number" ns1:message="" ns1:unCheckedValue="No" ns1:selected="true" ns1:maxlength="13">
                                        <ns1:value/>
                                    </ns1:boxSplitSymbol>
                                    <!--Group-->
                                    <ns1:boxSplitSymbol xsi:type="ns1:input" ns1:name="group" ns1:label="Group (If any,e.g A/B/C) " ns1:inputType="text" ns1:message="" ns1:unCheckedValue="No" ns1:selected="true" ns1:maxlength="13">
                                        <ns1:value/>
                                    </ns1:boxSplitSymbol>
                                </ns1:boxSymbol>
                                <!--Confirmation checkbox for alignments-->
                                <ns1:boxSymbol xsi:type="ns1:input" ns1:name="confirmationCB" ns1:inputType="checkbox" ns1:unCheckedValue="No" ns1:selected="false">
                                    <ns1:value/>
                                    <ns1:inputItem ns1:id="check1" ns1:label="I confirm that the signature card was signed and it aligns with the waiver requirements." ns1:type="checkbox" ns1:value="1" ns1:unCheckedValue="No" ns1:selected="false"/>
                                </ns1:boxSymbol>

                            </ns1:box>
                        </ns1:symbol>
                    </ns1:sections>
                </ns1:form>
            </symbol>
            <symbol xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="ns1:footer" ns1:text="" ns1:textAlign="left" ns1:buttonAlign="right">
                <ns1:baseButton ns1:id="submitBtn" ns1:url="app-domain/ui/mandatesSuccess" ns1:target="main" ns1:formSubmit="false" ns1:label="Submit"/>
                <ns1:baseButton ns1:id="backBtn" ns1:url="app-domain/ui/mandatesFill" ns1:target="main" ns1:formSubmit="false" ns1:label="Back"/>
            </symbol>
        </page>
    </xsl:template>
</xsl:stylesheet>