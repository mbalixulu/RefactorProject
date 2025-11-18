<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="xml"/>
    <xsl:template match="/">
        <page xmlns:comm="http://ws.online.fnb.co.za/v1/common/"
              id="WrapupLandingPage"
              title="Switch" template="main" layout="" version="1">
            <symbol xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                    xsi:type="comm:formLayout">
                <comm:form comm:action=""
                           comm:name="WrapupLandingForm">
                    <comm:sections comm:width="full">
                        <comm:symbol xsi:type="comm:boxContainer"
                                     comm:id="boxDiv">
                            <comm:box xsi:type="comm:box">
                                <comm:boxSymbol xsi:type="comm:textHeading"
                                                comm:size="4">
                                    <comm:value>Please Fill The Director
                                        Details
                                    </comm:value>
                                </comm:boxSymbol>
                                <comm:boxSymbol xsi:type="comm:input"
                                                comm:name="name"
                                                comm:label="Name *"
                                                comm:inputType="text" comm:message=""
                                                comm:unCheckedValue="No"
                                                comm:selected="true"
                                                comm:required="true"
                                                comm:errorMessage="{directorModel/directorErrorModel/name}"
                                                comm:maxlength="50">
                                    <comm:value>
                                        <xsl:value-of
                                                select="directorModel/name"/>
                                    </comm:value>
                                </comm:boxSymbol>
                                <comm:boxSymbol xsi:type="comm:input"
                                                comm:name="surname"
                                                comm:label="Surname *"
                                                comm:inputType="text" comm:message=""
                                                comm:unCheckedValue="No"
                                                comm:tooltip="Enter Phone Number include Country code"
                                                comm:selected="true"
                                                comm:required="true"
                                                comm:errorMessage="{directorModel/directorErrorModel/surname}"
                                                comm:maxlength="50">
                                    <comm:value>
                                        <xsl:value-of
                                                select="directorModel/surname"/>
                                    </comm:value>
                                </comm:boxSymbol>
                                <comm:boxSymbol xsi:type="comm:input"
                                                comm:name="designation"
                                                comm:label="Designation *"
                                                comm:inputType="text" comm:message=""
                                                comm:unCheckedValue="No"
                                                comm:selected="true"
                                                comm:required="true"
                                                comm:errorMessage="{directorModel/directorErrorModel/designation}"
                                                comm:maxlength="50">
                                    <comm:value>
                                        <xsl:value-of
                                                select="directorModel/designation"/>
                                    </comm:value>
                                </comm:boxSymbol>
                                <xsl:if test="directorModel/pageCheck = 'true'">
                                    <comm:boxSymbol
                                            xsi:type="comm:dropdown"
                                            comm:id="instructions"
                                            comm:label="Instruction *"
                                            comm:selectedValue="{directorModel/instructions}"
                                            comm:errorMessage="{directorModel/directorErrorModel/instruction}">
                                        <comm:label>Add</comm:label>
                                        <comm:value xsi:type="comm:eventValue">
                                            <comm:value>Add</comm:value>
                                        </comm:value>

                                        <comm:label>Remove</comm:label>
                                        <comm:value xsi:type="comm:eventValue">
                                            <comm:value>Remove</comm:value>
                                        </comm:value>
                                    </comm:boxSymbol>
                                </xsl:if>
                            </comm:box>
                        </comm:symbol>
                    </comm:sections>
                </comm:form>
            </symbol>

            <symbol xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                    xsi:type="comm:footer" comm:text="" comm:textAlign="left"
                    comm:buttonAlign="right">
                <xsl:if test="directorModel/pageCheck = 'false'">
                    <comm:baseButton comm:id="cancel"
                                     comm:url="app-domain/mandates-and-resolutions/backDirectorPopup"
                                     comm:target="main" comm:formSubmit="false" comm:tooltip=""
                                     comm:label="Back"/>
                    <xsl:if test="directorModel/buttonCheck = 'true'">
                        <comm:baseButton comm:id="next"
                                         comm:url="app-domain/mandates-and-resolutions/submitAdminDetails"
                                         comm:target="main" comm:formSubmit="true" comm:tooltip=""
                                         comm:label="Submit"/>
                    </xsl:if>
                    <xsl:if test="directorModel/buttonCheck = 'false'">
                        <comm:baseButton comm:id="next"
                                         comm:url="app-domain/mandates-and-resolutions/updateDirectors/{directorModel/userInList}"
                                         comm:target="main" comm:formSubmit="true" comm:tooltip=""
                                         comm:label="Update"/>
                    </xsl:if>
                </xsl:if>
                <xsl:if test="directorModel/pageCheck = 'true'">
                    <comm:baseButton comm:id="cancel"
                                     comm:url="app-domain/mandates-and-resolutions/backDirectorPopupReso"
                                     comm:target="main" comm:formSubmit="false" comm:tooltip=""
                                     comm:label="Back"/>
                    <xsl:if test="directorModel/buttonCheck = 'true'">
                        <comm:baseButton comm:id="next"
                                         comm:url="app-domain/mandates-and-resolutions/submitAdminDetailsReso"
                                         comm:target="main" comm:formSubmit="true" comm:tooltip=""
                                         comm:label="Submit"/>
                    </xsl:if>
                    <xsl:if test="directorModel/buttonCheck = 'false'">
                        <comm:baseButton comm:id="next"
                                         comm:url="app-domain/mandates-and-resolutions/updateDirectorsReso/{directorModel/userInList}"
                                         comm:target="main" comm:formSubmit="true" comm:tooltip=""
                                         comm:label="Update"/>
                    </xsl:if>
                </xsl:if>
            </symbol>
        </page>
    </xsl:template>
</xsl:stylesheet>