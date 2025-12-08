<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml"/>

    <xsl:template match="/">
        <page xmlns:ns1="http://ws.online.fnb.co.za/v1/common/"
              xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
              id="adminAllPage" title="Admin All Page" template="main" layout="" version="1">

            <!--Heading-->
            <symbol xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                    xsi:type="ns1:subTabGroup"
                    ns1:subTabGroupHeading="Mandates and resolutions"/>

            <!--View Switcher-->
            <symbol xsi:type="ns1:viewGroup" ns1:align="left">
                <ns1:view ns1:text="All" ns1:type="active" ns1:id="all"
                          ns1:url="app-domain/mandates-and-resolutions/adminAll"/>
                <ns1:view ns1:text="Approvals" ns1:type="inactive" ns1:id="pendingRequests"
                          ns1:url="app-domain/mandates-and-resolutions/adminApproval"/>
                <ns1:view ns1:text="Breached" ns1:type="inactive" ns1:id="onHold"
                          ns1:url="app-domain/mandates-and-resolutions/adminBreach"/>
                <ns1:view ns1:text="In Progress" ns1:type="inactive" ns1:id="pendingRequests"
                          ns1:url="app-domain/mandates-and-resolutions/inProgressRequests"/>
                <ns1:view ns1:text="On Hold" ns1:type="inactive" ns1:id="onHold"
                          ns1:url="app-domain/mandates-and-resolutions/onHoldRequests"/>
                <ns1:view ns1:text="Completed" ns1:type="inactive" ns1:id="completed"
                          ns1:url="app-domain/mandates-and-resolutions/completedRequests"/>
                <ns1:view ns1:text="Draft" ns1:type="inactive" ns1:id="draft"
                          ns1:url="app-domain/mandates-and-resolutions/draftRequests"/>
            </symbol>

            <!--Table-->
            <symbol xsi:type="ns1:formLayout" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
                <ns1:form ns1:action="app-domain/mandates-and-resolutions/adminAll" ns1:name="fetchTicketForm">
                    <ns1:sections ns1:id="tableSection" ns1:align="center" ns1:width="full" ns1:tooltip="">
                        <xsl:attribute name="ns1:label">
                            <xsl:choose>
                                <xsl:when test="status = 'onhold'">On hold</xsl:when>
                                <xsl:when test="status = 'completed'">Completed</xsl:when>
                                <xsl:otherwise>All requests</xsl:otherwise>
                            </xsl:choose>
                        </xsl:attribute>

                        <ns1:symbol xsi:type="ns1:fullTable" ns1:id="TemplateTable"
                                    ns1:headingColor="primary" ns1:showTotal="true"
                                    ns1:defaultSortIndex="7"
                                    ns1:defaultSortDirection="descending">
                            <xsl:attribute name="ns1:heading">
                                <xsl:choose>
                                    <xsl:when test="status = 'onhold'">On hold</xsl:when>
                                    <xsl:when test="status = 'completed'">Completed</xsl:when>
                                    <xsl:otherwise>All requests</xsl:otherwise>
                                </xsl:choose>
                            </xsl:attribute>

                            <!--Create Request + Export as CSV Button-->
                            <ns1:addButton xsi:type="ns1:imageButton"
                                           ns1:target="main"
                                           ns1:id="createRequestbtn"
                                           ns1:label="Actions"
                                           ns1:tooltip="true"
                                           ns1:tip="tip"
                                           ns1:url="app-domain/mandates-and-resolutions/createRequest"
                                           ns1:formName="ticketForm">
                                <ns1:imageButtonOptions xsi:type="ns1:hyperlinkList" ns1:id="tableActionsList">
                                    <ns1:hyperlinkListItem xsi:type="ns1:hyperlinkListItem"
                                                           ns1:label="Create Request"
                                                           ns1:target="main"
                                                           ns1:url="app-domain/mandates-and-resolutions/createRequest"/>
                                    <ns1:hyperlinkListItem xsi:type="ns1:hyperlinkListItem"
                                                           ns1:label="Export CSV"
                                                           ns1:target="main"
                                                           ns1:url="app-domain/mandates-and-resolutions/exportCSV"/>
                                </ns1:imageButtonOptions>
                            </ns1:addButton>

                            <!--Search-->
                            <ns1:tableSearch ns1:searchPlaceholder="Search Ticket"/>

                            <!--Columns-->
                            <ns1:tableColumn ns1:id="requestID"
                                             ns1:heading="Request ID"
                                             ns1:fieldName="requestID"
                                             ns1:disableSorting="true"
                                             ns1:widthPercent="9"/>
                            <ns1:tableColumn ns1:id="assignedUser"
                                             ns1:heading="Assigned User"
                                             ns1:fieldName="assignedUser"
                                             ns1:disableSorting="false"
                                             ns1:widthPercent="9"/>
                            <ns1:tableColumn ns1:id="sla"
                                             ns1:heading="SLA"
                                             ns1:fieldName="sla"
                                             ns1:disableSorting="false"
                                             ns1:widthPercent="6"/>
                            <ns1:tableColumn ns1:id="companyName"
                                             ns1:heading="Company"
                                             ns1:fieldName="companyName"
                                             ns1:disableSorting="false"
                                             ns1:widthPercent="9"/>
                            <ns1:tableColumn ns1:id="status"
                                             ns1:heading="Status"
                                             ns1:fieldName="status"
                                             ns1:disableSorting="false"
                                             ns1:widthPercent="9"/>
                            <ns1:tableColumn ns1:id="subStatus"
                                             ns1:heading="Sub Status"
                                             ns1:fieldName="subStatus"
                                             ns1:disableSorting="false"
                                             ns1:widthPercent="9"/>
                            <ns1:tableColumn ns1:id="requestType"
                                             ns1:heading="Request Type"
                                             ns1:fieldName="requestType"
                                             ns1:disableSorting="false"
                                             ns1:widthPercent="9"/>
                            <ns1:tableColumn ns1:id="dateCreated"
                                             ns1:heading="Date"
                                             ns1:fieldName="dateCreated"
                                             ns1:disableSorting="false"
                                             ns1:widthPercent="9"/>
                            <ns1:rowGroup ns1:groupId="rows" ns1:groupHeaderLabel=""/>

                            <!--Rows-->
                            <xsl:for-each select="requests/request">
                                <ns1:row ns1:groupId="rows">
                                    <!-- Request ID (now shows requestIdForDisplay when available) -->
                                    <ns1:cell ns1:col_id="requestID">
                                        <ns1:cellItem>
                                            <ns1:item
                                                    xsi:type="ns1:hyperlink"
                                                    ns1:target="main"
                                                    ns1:url="{concat('app-domain/mandates-and-resolutions/adminView/', requestId)}">
                                                <!-- IMPORTANT: hyperlink text goes in the ns1:text attribute, not a child -->
                                                <xsl:attribute name="ns1:text">
                                                    <xsl:choose>
                                                        <xsl:when test="normalize-space(requestIdForDisplay) != ''">
                                                            <xsl:value-of select="requestIdForDisplay"/>
                                                        </xsl:when>
                                                        <xsl:otherwise>
                                                            <xsl:value-of select="requestId"/>
                                                        </xsl:otherwise>
                                                    </xsl:choose>
                                                </xsl:attribute>
                                            </ns1:item>
                                        </ns1:cellItem>
                                    </ns1:cell>

                                    <!--Assigned User -->
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
                                    <!-- SLA (pending) -->
                                    <ns1:cell ns1:col_id="sla">
                                        <ns1:cellItem>
                                            <ns1:item xsi:type="ns1:simpleText" ns1:label="SLA">
                                                <ns1:value><xsl:value-of select="sla"/></ns1:value>
                                            </ns1:item>
                                        </ns1:cellItem>
                                    </ns1:cell>

                                    <!-- Company -->
                                    <ns1:cell ns1:col_id="companyName">
                                        <ns1:cellItem>
                                            <ns1:item xsi:type="ns1:simpleText" ns1:label="Company">
                                                <ns1:value><xsl:value-of select="companyName"/></ns1:value>
                                            </ns1:item>
                                        </ns1:cellItem>
                                    </ns1:cell>

                                    <!-- Status (plain text) -->
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

                                    <!--Request Type -->
                                    <ns1:cell ns1:col_id="requestType">
                                        <ns1:cellItem>
                                            <ns1:item xsi:type="ns1:simpleText" ns1:label="Request Type">
                                                <ns1:value>
                                                    <xsl:value-of select="type"/>
                                                </ns1:value>
                                            </ns1:item>
                                        </ns1:cellItem>
                                    </ns1:cell>

                                    <!-- Date Created -->
                                    <ns1:cell ns1:col_id="dateCreated">
                                        <ns1:cellItem>
                                            <ns1:item xsi:type="ns1:simpleText" ns1:label="Date">
                                                <ns1:value><xsl:value-of select="created"/></ns1:value>
                                            </ns1:item>
                                        </ns1:cellItem>
                                    </ns1:cell>
                                </ns1:row>
                            </xsl:for-each>
                            <ns1:tableNavigator ns1:pageSize="10"/>
                        </ns1:symbol>
                    </ns1:sections>
                </ns1:form>
            </symbol>

            <!--Footer-->
            <symbol xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                    xsi:type="ns1:footer" ns1:text="" ns1:textAlign="left" ns1:buttonAlign="right">
                <ns1:baseButton ns1:id="logout" ns1:target="main" ns1:url="app-domain/mandates-and-resolutions/logout" ns1:label="Log out" ns1:formSubmit="true"/>
            </symbol>
        </page>
    </xsl:template>
</xsl:stylesheet>