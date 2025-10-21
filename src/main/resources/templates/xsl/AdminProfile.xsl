<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml"/>
    <xsl:template match="/">
        <page xmlns:ns1="http://ws.online.fnb.co.za/v1/common/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" id="ticketTable" title="Ticket Table" template="main" layout="" version="1">
            <!--Heading-->
            <symbol xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="ns1:subTabGroup" ns1:subTabGroupHeading="Mandates and resolutions"/>

            <!--View Switcher-->
            <symbol xsi:type="ns1:viewGroup" ns1:align="left">
                <ns1:view ns1:text="Approvals"      ns1:type="inactive" ns1:id="pendingRequests" ns1:url="app-domain/mandates-and-resolutions/adminApproval"/>
                <ns1:view ns1:text="Breached"       ns1:type="inactive" ns1:id="onHold"          ns1:url="app-domain/mandates-and-resolutions/adminBreach"/>
                <ns1:view ns1:text="All"            ns1:type="inactive" ns1:id="all"             ns1:url="app-domain/mandates-and-resolutions/adminAll"/>
                <ns1:view ns1:text="In Progress"    ns1:type="inactive"   ns1:id="pendingRequests" ns1:url="app-domain/mandates-and-resolutions/adminInProgress"/>
                <ns1:view ns1:text="On Hold"        ns1:type="inactive" ns1:id="onHold"          ns1:url="app-domain/mandates-and-resolutions/adminOnHold"/>
                <ns1:view ns1:text="Completed"      ns1:type="inactive" ns1:id="completed"       ns1:url="app-domain/mandates-and-resolutions/adminCompleted"/>
                <ns1:view ns1:text="Draft"          ns1:type="inactive" ns1:id="draft"           ns1:url="app-domain/mandates-and-resolutions/adminDraft"/>
                <ns1:view ns1:text="Profile"        ns1:type="active" ns1:id="profile"         ns1:url="app-domain/mandates-and-resolutions/adminProfile"/>
            </symbol>

            <symbol xsi:type="ns1:formLayout">
                <ns1:form ns1:action="app-domain/mandates-and-resolutions/requestTableProfile" ns1:name="salesForm">
                    <ns1:sections ns1:width="full">
                        <ns1:symbol xsi:type="ns1:boxContainer">
                            <ns1:box xsi:type="ns1:box">
                                <ns1:boxSymbol xsi:type="ns1:textHeading" ns1:size="4">
                                    <ns1:value>User Profile</ns1:value>
                                </ns1:boxSymbol>

                                <!-- Username -->
                                <ns1:boxSymbol xsi:type="ns1:boxSplit" ns1:width="50">
                                    <ns1:boxSplitSymbol xsi:type="ns1:textReadout"
                                                        ns1:subHeading="Username"
                                                        ns1:color="ghostmedium">
                                        <ns1:value>
                                            <xsl:choose>
                                                <xsl:when test="string-length(normalize-space(/requestWrapper/request/loggedInUsername)) &gt; 0">
                                                    <xsl:value-of select="/requestWrapper/request/loggedInUsername"/>
                                                </xsl:when>
                                                <xsl:otherwise>-</xsl:otherwise>
                                            </xsl:choose>
                                        </ns1:value>
                                    </ns1:boxSplitSymbol>
                                </ns1:boxSymbol>

                                <!-- Email Address -->
                                <ns1:boxSymbol xsi:type="ns1:boxSplit" ns1:width="50">
                                    <ns1:boxSplitSymbol xsi:type="ns1:textReadout"
                                                        ns1:subHeading="Email Address"
                                                        ns1:color="ghostmedium">
                                        <ns1:value>
                                            <xsl:choose>
                                                <xsl:when test="string-length(normalize-space(/requestWrapper/request/loggedInEmail)) &gt; 0">
                                                    <xsl:value-of select="/requestWrapper/request/loggedInEmail"/>
                                                </xsl:when>
                                                <xsl:otherwise>-</xsl:otherwise>
                                            </xsl:choose>
                                        </ns1:value>
                                    </ns1:boxSplitSymbol>
                                </ns1:boxSymbol>
                            </ns1:box>
                        </ns1:symbol>
                    </ns1:sections>
                </ns1:form>
            </symbol>

            <symbol xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="ns1:footer" ns1:text="" ns1:textAlign="left" ns1:buttonAlign="right">
                <ns1:baseButton ns1:id="logout" ns1:target="main" ns1:url="app-domain/mandates-and-resolutions/logout" ns1:label="Log out" ns1:formSubmit="true"/>
            </symbol>

        </page>
    </xsl:template>
</xsl:stylesheet>