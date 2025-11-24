<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="xml"/>
    <!-- start from the root of the xml document "/" -->
    <xsl:template match="/">
        <page xmlns:comm="http://ws.online.fnb.co.za/v1/common/"
              xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
              id="boxConatinerID"
              title="Box Container" template="main" layout="" version="1">
            <symbol xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                    xsi:type="comm:subTabGroup"
                    comm:subTabGroupHeading="Signature card confirmation"/>
            <symbol xsi:type="comm:formLayout">
                <comm:form comm:name="selectRequirementsForm" comm:action="">
                    <xsl:if test="requestWrapper/checkSignatureCard = 'true'">
                        <comm:sections comm:width="full">
                            <comm:symbol xsi:type="comm:textHeading" comm:size="4">
                                <comm:value>Please Fill Up All the Record before proceed !
                                </comm:value>
                            </comm:symbol>
                        </comm:sections>
                    </xsl:if>
                    <xsl:for-each
                            select="requestWrapper/listOfAddAccount">
                            <comm:sections comm:width="full">
                                <comm:symbol xsi:type="comm:textHeading" comm:size="4">
                                    <comm:value>Account
                                        <xsl:value-of select="position()"/>
                                        :<xsl:value-of select="accountName"/>, (<xsl:value-of
                                                select="accountNumber"/>)
                                    </comm:value>
                                </comm:symbol>
                                <xsl:for-each
                                        select="listOfSignatory">
                                    <xsl:if test="instruction = 'Add'">
                                    <comm:symbol xsi:type="comm:boxContainer"
                                                 comm:id="boxDiv">
                                        <comm:box xsi:type="comm:box">
                                            <comm:boxSymbol xsi:type="comm:textHeading"
                                                            comm:size="4">
                                                <comm:value>Added Signatories</comm:value>
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
                                                                     comm:url="app-domain/mandates-and-resolutions/editAddedSignatory/{userInList}/{userInAccount}"
                                                                     comm:label="Edit"
                                                                     comm:width="3"
                                                                     comm:formSubmit="false"
                                                                     comm:type="paper"/>
                                            </comm:boxSymbol>
                                            <comm:boxSymbol xsi:type="comm:input"
                                                            comm:name="fullName"
                                                            comm:label="Full Name *"
                                                            comm:inputType="text" comm:message=""
                                                            comm:unCheckedValue="No"
                                                            comm:selected="true"
                                                            comm:required="true"
                                                            comm:readonly="true"
                                                            comm:maxlength="50">
                                                <comm:value>
                                                    <xsl:value-of
                                                            select="fullName"/>
                                                </comm:value>
                                            </comm:boxSymbol>
                                            <comm:boxSymbol xsi:type="comm:input"
                                                            comm:name="capacity{userInList}{userInAccount}"
                                                            comm:label="Capacity *"
                                                            comm:inputType="text" comm:message=""
                                                            comm:unCheckedValue="No"
                                                            comm:selected="true"
                                                            comm:required="true"
                                                            comm:readonly="true"
                                                            comm:maxlength="50">
                                                <comm:value>
                                                    <xsl:value-of
                                                            select="capacity"/>
                                                </comm:value>
                                            </comm:boxSymbol>
                                            <comm:boxSymbol xsi:type="comm:input"
                                                            comm:name="idNumber"
                                                            comm:label="ID Number *"
                                                            comm:inputType="text" comm:message=""
                                                            comm:unCheckedValue="No"
                                                            comm:selected="true"
                                                            comm:required="true"
                                                            comm:readonly="true"
                                                            comm:maxlength="50">
                                                <comm:value>
                                                    <xsl:value-of
                                                            select="idNumber"/>
                                                </comm:value>
                                            </comm:boxSymbol>
                                            <comm:boxSymbol xsi:type="comm:input"
                                                            comm:name="Group{userInList}{userInAccount}"
                                                            comm:label="Group *"
                                                            comm:inputType="text" comm:message=""
                                                            comm:unCheckedValue="No"
                                                            comm:selected="true"
                                                            comm:required="true"
                                                            comm:readonly="true"
                                                            comm:maxlength="50">
                                                <comm:value>
                                                    <xsl:value-of
                                                            select="group"/>
                                                </comm:value>
                                            </comm:boxSymbol>
                                        </comm:box>
                                    </comm:symbol>
                                    </xsl:if>
                                </xsl:for-each>
                            </comm:sections>
                    </xsl:for-each>
                </comm:form>
            </symbol>
            <symbol xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                    xsi:type="comm:footer" comm:text="" comm:textAlign="left"
                    comm:buttonAlign="right">
                <comm:baseButton comm:id="back"
                                 comm:url="app-domain/mandates-and-resolutions/cancelToSignaturePage"
                                 comm:target="main" comm:formSubmit="false" comm:tooltip=""
                                 comm:label="Back"/>
                <comm:baseButton comm:id="save"
                                 comm:url="app-domain/mandates-and-resolutions/saveSignatureCard"
                                 comm:target="main" comm:formSubmit="false" comm:tooltip=""
                                 comm:label="Save"/>
                <xsl:if test="requestWrapper/requestType = 'Mandate'">
                    <comm:baseButton comm:id="next"
                                     comm:url="app-domain/mandates-and-resolutions/submitMandateFinalRecord"
                                     comm:target="main" comm:formSubmit="true" comm:tooltip=""
                                     comm:label="Submit"/>
                </xsl:if>
                <xsl:if test="requestWrapper/requestType != 'Mandate'">
                    <comm:baseButton comm:id="next"
                                     comm:url="app-domain/mandates-and-resolutions/proceedSignatureCard"
                                     comm:target="main" comm:formSubmit="true" comm:tooltip=""
                                     comm:label="Continue"/>
                </xsl:if>
            </symbol>
        </page>
    </xsl:template>
</xsl:stylesheet>



