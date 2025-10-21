<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml"/>

    <xsl:template match="/">
        <page xmlns:ns1="http://ws.online.fnb.co.za/v1/common/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" id="ticketTable" title="Ticket Table" template="main" layout="" version="1">
            <!--Heading-->
            <symbol xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="ns1:subTabGroup" ns1:subTabGroupHeading="Mandates and resolutions"/>

            <!--View Switcher-->
            <symbol xsi:type="ns1:viewGroup" ns1:align="left">
                <ns1:view ns1:text="Pending requests" ns1:type="inactive" ns1:id="pendingRequests" ns1:url="app-domain/mandates-and-resolutions/requestTable"/>
                <ns1:view ns1:text="On hold"          ns1:type="inactive" ns1:id="onHold"          ns1:url="app-domain/mandates-and-resolutions/requestTableOnHold"/>
                <ns1:view ns1:text="Completed"        ns1:type="active"   ns1:id="completed"       ns1:url="app-domain/mandates-and-resolutions/requestTableCompleted"/>
                <ns1:view ns1:text="Draft"            ns1:type="inactive" ns1:id="draft"           ns1:url="app-domain/mandates-and-resolutions/requestTableDraft"/>
                <ns1:view ns1:text="Profile"          ns1:type="inactive" ns1:id="profile"         ns1:url="app-domain/mandates-and-resolutions/requestTableProfile"/>
            </symbol>

            <!--Table-->
            <symbol xsi:type="ns1:formLayout" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
                <ns1:form ns1:action="app-domain/mandates-and-resolutions" ns1:name="fetchTicketForm">
                    <ns1:sections ns1:id="tableSection" ns1:align="center" ns1:width="full" ns1:tooltip="">
                        <xsl:attribute name="ns1:label">
                            <xsl:choose>
                                <xsl:when test="status = 'onhold'">On hold</xsl:when>
                                <xsl:when test="status = 'completed'">Completed</xsl:when>
                                <xsl:otherwise>Completed tickets</xsl:otherwise>
                            </xsl:choose>
                        </xsl:attribute>

                        <ns1:symbol xsi:type="ns1:fullTable" ns1:id="TemplateTable" ns1:headingColor="primary" ns1:showTotal="true">
                            <xsl:attribute name="ns1:heading">
                                <xsl:choose>
                                    <xsl:when test="status = 'onhold'">On hold</xsl:when>
                                    <xsl:when test="status = 'completed'">Completed</xsl:when>
                                    <xsl:otherwise>Completed tickets</xsl:otherwise>
                                </xsl:choose>
                            </xsl:attribute>

                            <!--Create Request Button-->
                            <ns1:addButton xsi:type="ns1:imageButton" ns1:target="main" ns1:id="createRequestbtn" ns1:label="Create Request" ns1:tooltip="true" ns1:tip="tip" ns1:url="app-domain/mandates-and-resolutions/createRequest" ns1:formName="ticketForm">
                                <ns1:imageButtonOptions xsi:type="ns1:hyperlinkList" ns1:id="createRequestbtn">
                                    <ns1:hyperlinkListItem xsi:type="ns1:hyperlinkListItem" ns1:target="main" ns1:label="Create Request" ns1:url="app-domain/mandates-and-resolutions/createRequest"/>
                                </ns1:imageButtonOptions>
                            </ns1:addButton>

                            <!--Search-->
                            <ns1:tableSearch ns1:searchPlaceholder="Search Ticket"/>

                            <!--Columns (match Pending)-->
                            <ns1:tableColumn ns1:id="requestID"    ns1:heading="Request ID"    ns1:fieldName="requestID"    ns1:disableSorting="true" ns1:widthPercent="9"/>
                            <ns1:tableColumn ns1:id="processId"    ns1:heading="Process ID"    ns1:fieldName="processId"    ns1:disableSorting="true" ns1:widthPercent="9"/>
                            <ns1:tableColumn ns1:id="assignedUser" ns1:heading="Assigned User" ns1:fieldName="assignedUser" ns1:disableSorting="true" ns1:widthPercent="9"/>
                            <ns1:tableColumn ns1:id="sla"          ns1:heading="SLA"           ns1:fieldName="sla"          ns1:disableSorting="true" ns1:widthPercent="9"/>
                            <ns1:tableColumn ns1:id="companyName"  ns1:heading="Company Name"  ns1:fieldName="companyName"  ns1:disableSorting="true" ns1:widthPercent="9"/>
                            <ns1:tableColumn ns1:id="status"       ns1:heading="Status"        ns1:fieldName="status"       ns1:disableSorting="true" ns1:widthPercent="9"/>
                            <ns1:tableColumn ns1:id="subStatus"    ns1:heading="Sub Status"    ns1:fieldName="subStatus"    ns1:disableSorting="true" ns1:widthPercent="9"/>
                            <ns1:tableColumn ns1:id="requestType"  ns1:heading="Request Type"  ns1:fieldName="requestType"  ns1:disableSorting="true" ns1:widthPercent="9"/>
                            <ns1:tableColumn ns1:id="dateCreated"  ns1:heading="Date Created"  ns1:fieldName="dateCreated"  ns1:disableSorting="true" ns1:widthPercent="9"/>
                            <ns1:tableColumn ns1:id="view"         ns1:heading="View"          ns1:fieldName="view"         ns1:disableSorting="true" ns1:widthPercent="9"/>

                            <ns1:rowGroup ns1:groupId="rows" ns1:groupHeaderLabel=""/>

                            <!--Rows-->
                            <xsl:for-each select="requests/request">
                                <ns1:row ns1:groupId="rows">

                                    <!-- Request ID (display id with fallback) -->
                                    <ns1:cell ns1:col_id="requestID">
                                        <ns1:cellItem>
                                            <ns1:item xsi:type="ns1:simpleText" ns1:label="Request ID">
                                                <ns1:value>
                                                    <xsl:choose>
                                                        <xsl:when test="normalize-space(requestIdForDisplay) != ''">
                                                            <xsl:value-of select="requestIdForDisplay"/>
                                                        </xsl:when>
                                                        <xsl:otherwise>
                                                            <xsl:value-of select="requestId"/>
                                                        </xsl:otherwise>
                                                    </xsl:choose>
                                                </ns1:value>
                                            </ns1:item>
                                        </ns1:cellItem>
                                    </ns1:cell>

                                    <!-- Process ID -->
                                    <ns1:cell ns1:col_id="processId">
                                        <ns1:cellItem>
                                            <ns1:item xsi:type="ns1:simpleText" ns1:label="Process ID">
                                                <ns1:value>
                                                    <xsl:choose>
                                                        <xsl:when test="normalize-space(processId) != ''">
                                                            <xsl:value-of select="processId"/>
                                                        </xsl:when>
                                                        <xsl:otherwise>—</xsl:otherwise>
                                                    </xsl:choose>
                                                </ns1:value>
                                            </ns1:item>
                                        </ns1:cellItem>
                                    </ns1:cell>

                                    <!-- Assigned User -->
                                    <ns1:cell ns1:col_id="assignedUser">
                                        <ns1:cellItem>
                                            <ns1:item xsi:type="ns1:simpleText" ns1:label="Assigned User">
                                                <ns1:value>
                                                    <xsl:choose>
                                                        <xsl:when test="normalize-space(assignedUser) != ''">
                                                            <xsl:value-of select="assignedUser"/>
                                                        </xsl:when>
                                                        <xsl:otherwise>—</xsl:otherwise>
                                                    </xsl:choose>
                                                </ns1:value>
                                            </ns1:item>
                                        </ns1:cellItem>
                                    </ns1:cell>

                                    <!-- SLA (success icon looks nice for completed) -->
                                    <ns1:cell ns1:col_id="sla">
                                        <ns1:cellItem>
                                            <ns1:item xsi:type="ns1:simpleText" ns1:label="SLA">
                                                <ns1:value><xsl:value-of select="sla"/></ns1:value>
                                            </ns1:item>
                                        </ns1:cellItem>
                                    </ns1:cell>

                                    <!-- Company Name -->
                                    <ns1:cell ns1:col_id="companyName">
                                        <ns1:cellItem>
                                            <ns1:item xsi:type="ns1:simpleText" ns1:label="Company Name">
                                                <ns1:value><xsl:value-of select="companyName"/></ns1:value>
                                            </ns1:item>
                                        </ns1:cellItem>
                                    </ns1:cell>

                                    <!-- Status -->
                                    <ns1:cell ns1:col_id="status">
                                        <ns1:cellItem>
                                            <ns1:item xsi:type="ns1:simpleText" ns1:label="Status">
                                                <ns1:value><xsl:value-of select="status"/></ns1:value>
                                            </ns1:item>
                                        </ns1:cellItem>
                                    </ns1:cell>

                                    <!-- Sub Status -->
                                    <ns1:cell ns1:col_id="subStatus">
                                        <ns1:cellItem>
                                            <ns1:item xsi:type="ns1:simpleText" ns1:label="Sub Status">
                                                <ns1:value><xsl:value-of select="subStatus"/></ns1:value>
                                            </ns1:item>
                                        </ns1:cellItem>
                                    </ns1:cell>

                                    <!-- Request Type (same mapping as Pending) -->
                                    <ns1:cell ns1:col_id="requestType">
                                        <ns1:cellItem>
                                            <ns1:item xsi:type="ns1:simpleText" ns1:label="Request Type">
                                                <ns1:value>
                                                    <xsl:choose>
                                                        <xsl:when test="normalize-space(mandateResolution)='1'">Mandate</xsl:when>
                                                        <xsl:when test="normalize-space(mandateResolution)='2'">Resolution</xsl:when>
                                                        <xsl:when test="normalize-space(mandateResolution)='3'">Mandate and resolution</xsl:when>

                                                        <xsl:when test="contains(translate(normalize-space((requestType|type)[1]),'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ'),'BOTH')">
                                                            Mandate and resolution
                                                        </xsl:when>
                                                        <xsl:when test="contains(translate(normalize-space((requestType|type)[1]),'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ'),'RESOLUTION')">
                                                            Resolution
                                                        </xsl:when>
                                                        <xsl:when test="contains(translate(normalize-space((requestType|type)[1]),'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ'),'MANDATE')">
                                                            Mandate
                                                        </xsl:when>
                                                        <xsl:otherwise>—</xsl:otherwise>
                                                    </xsl:choose>
                                                </ns1:value>
                                            </ns1:item>
                                        </ns1:cellItem>
                                    </ns1:cell>

                                    <!-- Date Created -->
                                    <ns1:cell ns1:col_id="dateCreated">
                                        <ns1:cellItem>
                                            <ns1:item xsi:type="ns1:simpleText" ns1:label="Date Created">
                                                <ns1:value><xsl:value-of select="created"/></ns1:value>
                                            </ns1:item>
                                        </ns1:cellItem>
                                    </ns1:cell>

                                    <!-- View -->
                                    <ns1:cell ns1:col_id="view">
                                        <ns1:cellItem>
                                            <ns1:item xsi:type="ns1:button"
                                                      ns1:id="{concat('viewBtn_', requestId)}"
                                                      ns1:type="action"
                                                      ns1:width="2"
                                                      ns1:url="{concat('app-domain/mandates-and-resolutions/viewRequest/', requestId)}"
                                                      ns1:target="main"
                                                      ns1:formSubmit="false"
                                                      ns1:label="View"/>
                                        </ns1:cellItem>
                                    </ns1:cell>
                                </ns1:row>
                            </xsl:for-each>

                            <ns1:tableNavigator ns1:pageSize="10"/>
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