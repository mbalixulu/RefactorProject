<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:comm="http://ws.online.fnb.co.za/v1/common/"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">

    <xsl:output method="xml" indent="yes"/>

    <xsl:template match="/requestWrapper">
        <page xmlns:comm="http://ws.online.fnb.co.za/v1/common/"
              xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
              id="reassignPanel"
              title="Re-Assign"
              template="main"
              version="1">

            <symbol xsi:type="comm:formLayout">
                <!--Form posts to submit endpoint; hidden field carries requestId-->
                <comm:form
                        comm:action="app-domain/ui/adminReassignSubmit"
                        comm:name="reassignForm">

                    <!--Hidden requestId-->
                    <comm:sections comm:align="left" comm:width="full">
                        <comm:symbol
                                xsi:type="comm:input"
                                comm:name="requestId"
                                comm:inputType="hidden">
                            <comm:value>
                                <xsl:value-of select="/requestWrapper/request/requestId"/>
                            </comm:value>
                        </comm:symbol>
                    </comm:sections>

                    <!--Box + dropdown-->
                    <comm:sections comm:align="left" comm:width="full">
                        <comm:symbol xsi:type="comm:boxContainer">
                            <comm:box xsi:type="comm:box">

                                <comm:boxSymbol xsi:type="comm:textHeading" comm:size="3" comm:color="primary">
                                    <comm:value>Re-Assign Request</comm:value>
                                </comm:boxSymbol>

                                <!--Dropdown with users + roles-->
                                <comm:boxSymbol
                                        xsi:type="comm:dropdown"
                                        comm:id="selectUser"
                                        comm:label="Re-Assign to"
                                        comm:tooltip="Select a user to assign the request to."
                                        comm:tooltipPosition="topRight">

                                    <xsl:for-each select="/requestWrapper/request/userOptions/userOption">
                                        <comm:label>
                                            <xsl:value-of select="concat(username, ' (', userRole, ')')"/>
                                        </comm:label>
                                        <comm:value xsi:type="comm:eventValue">
                                            <comm:value>
                                                <xsl:value-of select="username"/>
                                            </comm:value>
                                        </comm:value>
                                    </xsl:for-each>

                                </comm:boxSymbol>

                            </comm:box>
                        </comm:symbol>
                    </comm:sections>

                    <!-- Buttons -->
                    <comm:sections comm:align="left" comm:width="full">
                        <!--Submit updates the request and takes you back to the admin view request page-->
                        <comm:symbol
                                xsi:type="comm:button"
                                comm:url="app-domain/ui/adminReassignSubmit"
                                comm:target="main"
                                comm:label="Reassign"
                                comm:width="1"
                                comm:formSubmit="true"
                                comm:align="right"/>

                        <!--Cancel: navigate back to the exact request -->
                        <comm:symbol
                                xsi:type="comm:button"
                                comm:url="{concat('app-domain/ui/adminView/', /requestWrapper/request/requestId)}"
                                comm:target="main"
                                comm:label="Cancel"
                                comm:width="1"
                                comm:formSubmit="false"
                                comm:align="right"/>
                    </comm:sections>
                </comm:form>
            </symbol>
        </page>
    </xsl:template>
</xsl:stylesheet>
