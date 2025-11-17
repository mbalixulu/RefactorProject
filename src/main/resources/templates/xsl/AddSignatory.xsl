<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="xml"/>
    <xsl:template match="/">
        <page xmlns:comm="http://ws.online.fnb.co.za/v1/common/"
              id="WrapupLandingPage"
              title="Switch" template="main" layout="" version="1">
            <symbol xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                    xsi:type="comm:formLayout">
                <comm:form comm:action="" comm:name="WrapupLandingForm">
                    <comm:sections comm:width="full">
                        <comm:symbol xsi:type="comm:boxContainer" comm:id="boxDiv">
                            <comm:box xsi:type="comm:box">
                                <comm:boxSymbol xsi:type="comm:textHeading" comm:size="4">
                                    <comm:value>Please Fill The Signatory Details
                                    </comm:value>
                                </comm:boxSymbol>
                                    <comm:boxSymbol xsi:type="comm:input"
                                                         comm:name="fullName"
                                                         comm:label="Full Name *"
                                                         comm:inputType="text" comm:message=""
                                                         comm:unCheckedValue="No"
                                                         comm:selected="true"
                                                         comm:errorMessage="{signatoryModel/signatoryErrorModel/fullName}"
                                                         comm:required="true"
                                                         comm:maxlength="50">
                                        <comm:value>
                                            <xsl:value-of
                                                    select="signatoryModel/fullName"/>
                                        </comm:value>
                                    </comm:boxSymbol>
                                    <comm:boxSymbol xsi:type="comm:input"
                                                         comm:name="idNumber"
                                                         comm:label="ID Number *"
                                                         comm:inputType="text" comm:message=""
                                                         comm:unCheckedValue="No"
                                                         comm:selected="true"
                                                         comm:errorMessage="{signatoryModel/signatoryErrorModel/idNumber}"
                                                         comm:required="true"
                                                         comm:maxlength="50">
                                        <comm:value>
                                            <xsl:value-of
                                                    select="signatoryModel/idNumber"/>
                                        </comm:value>
                                    </comm:boxSymbol>
                                    <comm:boxSymbol xsi:type="comm:dropdown"
                                                         comm:id="accountRef1"
                                                         comm:selectedValue="{signatoryModel/instruction}"
                                                         comm:errorMessage="{signatoryModel/signatoryErrorModel/instruction}"
                                                         comm:label="Instruction *">
                                        <comm:label>Add</comm:label>
                                        <comm:value xsi:type="comm:eventValue">
                                            <comm:value>Add</comm:value>
                                        </comm:value>
                                        <comm:label>Remove</comm:label>
                                        <comm:value xsi:type="comm:eventValue">
                                            <comm:value>Remove</comm:value>
                                        </comm:value>
                                    </comm:boxSymbol>
                            </comm:box>
                        </comm:symbol>
                    </comm:sections>
                </comm:form>
            </symbol>
            <symbol xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                    xsi:type="comm:footer" comm:text="" comm:textAlign="left"
                    comm:buttonAlign="right">
                <comm:baseButton comm:id="cancel"
                                 comm:url="app-domain/mandates-and-resolutions/backToAddAccount"
                                 comm:target="main" comm:formSubmit="false" comm:tooltip=""
                                 comm:label="Back"/>
                <xsl:if test="signatoryModel/buttonCheck = 'true'">
                <comm:baseButton comm:id="next"
                                 comm:url="app-domain/mandates-and-resolutions/submitSignatoryDetails"
                                 comm:target="main" comm:formSubmit="true" comm:tooltip=""
                                 comm:label="Submit"/>
                </xsl:if>
                <xsl:if test="signatoryModel/buttonCheck = 'false'">
                <comm:baseButton comm:id="next"
                                 comm:url="app-domain/mandates-and-resolutions/EditSignatoryDetails/{signatoryModel/userInList}"
                                 comm:target="main" comm:formSubmit="true" comm:tooltip=""
                                 comm:label="Update"/>
                </xsl:if>
            </symbol>
        </page>
    </xsl:template>
</xsl:stylesheet>