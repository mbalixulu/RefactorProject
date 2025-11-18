<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml"/>

    <xsl:template match="/">
        <page xmlns:ns1="http://ws.online.fnb.co.za/v1/common/"
              xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
              id="ticketTable" title="Ticket Table" template="main" layout="" version="1">

            <!--Heading-->
            <symbol xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                    xsi:type="ns1:subTabGroup"
                    ns1:subTabGroupHeading="Mandates and resolutions"/>

            <!--View Switcher-->
            <symbol xsi:type="ns1:viewGroup" ns1:align="left">
                <xsl:if test="requests/requestDTO/subStatus = 'Admin'">
                    <ns1:view ns1:text="All" ns1:type="inactive" ns1:id="all"
                              ns1:url="app-domain/mandates-and-resolutions/adminAll"/>
                    <ns1:view ns1:text="Approvals" ns1:type="inactive" ns1:id="approvals"
                              ns1:url="app-domain/mandates-and-resolutions/adminApproval"/>
                    <ns1:view ns1:text="Breached" ns1:type="inactive" ns1:id="breached"
                              ns1:url="app-domain/mandates-and-resolutions/adminBreach"/>
                </xsl:if>
                <ns1:view ns1:text="In Progress" ns1:type="inactive" ns1:id="inProgress"
                          ns1:url="app-domain/mandates-and-resolutions/inProgressRequests"/>
                <ns1:view ns1:text="On Hold" ns1:type="inactive" ns1:id="onHold"
                          ns1:url="app-domain/mandates-and-resolutions/onHoldRequests"/>
                <ns1:view ns1:text="Completed" ns1:type="inactive" ns1:id="completed"
                          ns1:url="app-domain/mandates-and-resolutions/completedRequests"/>
                <ns1:view ns1:text="Draft" ns1:type="active" ns1:id="draft"
                          ns1:url="app-domain/mandates-and-resolutions/draftRequests"/>
            </symbol>
            <symbol xsi:type="ns1:formLayout">
                <ns1:form ns1:action="app-domain/mandates-and-resolutions"
                          ns1:name="fetchTicketForm">
                    <ns1:sections ns1:id="tableSection" ns1:align="center" ns1:width="full">
                        <xsl:attribute name="ns1:label">Draft requests</xsl:attribute>

                        <ns1:symbol xsi:type="ns1:fullTable" ns1:id="TemplateTable"
                                    ns1:headingColor="primary" ns1:showTotal="true">
                            <xsl:attribute name="ns1:heading">Draft requests</xsl:attribute>
                            <!--Create Request Button-->
                            <ns1:addButton xsi:type="ns1:imageButton" ns1:target="main"
                                           ns1:id="createRequestbtn" ns1:label="Create Request"
                                           ns1:tooltip="true" ns1:tip="tip"
                                           ns1:url="app-domain/mandates-and-resolutions/createRequest"
                                           ns1:formName="ticketForm">
                                <ns1:imageButtonOptions xsi:type="ns1:hyperlinkList"
                                                        ns1:id="createRequestbtn">
                                    <ns1:hyperlinkListItem xsi:type="ns1:hyperlinkListItem"
                                                           ns1:target="main"
                                                           ns1:label="Create Request"
                                                           ns1:url="app-domain/mandates-and-resolutions/createRequest"/>
                                    <xsl:if test="requests/requestDTO/subStatus = 'Admin'">
                                        <ns1:hyperlinkListItem ns1:label="Export CSV"
                                                               ns1:url="app-domain/mandates-and-resolutions/exportCSV"/>
                                    </xsl:if>
                                </ns1:imageButtonOptions>
                            </ns1:addButton>

                            <ns1:tableSearch ns1:searchPlaceholder="Search Draft"/>

                            <ns1:tableColumn ns1:id="stagingId" ns1:heading="Staging ID"
                                             ns1:fieldName="requestId" ns1:disableSorting="false"
                                             ns1:widthPercent="10"/>
                            <ns1:tableColumn ns1:id="companyName" ns1:heading="Company Name"
                                             ns1:fieldName="companyName" ns1:disableSorting="false"
                                             ns1:widthPercent="18"/>
                            <ns1:tableColumn ns1:id="companyRegNumber"
                                             ns1:heading="Company Reg Number"
                                             ns1:fieldName="registrationNumber"
                                             ns1:disableSorting="false" ns1:widthPercent="18"/>
                            <ns1:tableColumn ns1:id="status" ns1:heading="Status"
                                             ns1:fieldName="status" ns1:disableSorting="false"
                                             ns1:widthPercent="12"/>
                            <ns1:tableColumn ns1:id="subStatus" ns1:heading="Sub Status"
                                             ns1:fieldName="subStatus" ns1:disableSorting="false"
                                             ns1:widthPercent="12"/>
                            <ns1:tableColumn ns1:id="type" ns1:heading="Request Type"
                                             ns1:fieldName="type" ns1:disableSorting="false"
                                             ns1:widthPercent="12"/>
                            <ns1:tableColumn ns1:id="created" ns1:heading="Date Created"
                                             ns1:fieldName="created" ns1:disableSorting="false"
                                             ns1:widthPercent="10"/>
                            <!--                            <ns1:tableColumn ns1:id="viewBtn"          ns1:heading="View"               ns1:fieldName="viewBtn"          ns1:disableSorting="true" ns1:widthPercent="8"/>-->

                            <ns1:rowGroup ns1:groupId="rows" ns1:groupHeaderLabel=""/>

                            <xsl:for-each select="request">
                                <ns1:row ns1:groupId="rows">
                                    <ns1:cell ns1:col_id="stagingId">
                                        <ns1:cellItem>
                                            <ns1:item xsi:type="ns1:hyperlink"
                                                      ns1:target="main"
                                                      ns1:url="{concat('app-domain/mandates-and-resolutions/draft/view?id=', requestId)}">
                                                <xsl:attribute name="ns1:text">
                                                    <xsl:value-of select="requestId"/>
                                                </xsl:attribute>
                                            </ns1:item>
                                        </ns1:cellItem>
                                    </ns1:cell>

                                    <ns1:cell ns1:col_id="companyName">
                                        <ns1:cellItem>
                                            <ns1:item xsi:type="ns1:simpleText"
                                                      ns1:label="Company Name">
                                                <ns1:value>
                                                    <xsl:value-of select="companyName"/>
                                                </ns1:value>
                                            </ns1:item>
                                        </ns1:cellItem>
                                    </ns1:cell>

                                    <ns1:cell ns1:col_id="companyRegNumber">
                                        <ns1:cellItem>
                                            <ns1:item xsi:type="ns1:simpleText"
                                                      ns1:label="Company Reg Number">
                                                <ns1:value>
                                                    <xsl:value-of select="registrationNumber"/>
                                                </ns1:value>
                                            </ns1:item>
                                        </ns1:cellItem>
                                    </ns1:cell>

                                    <ns1:cell ns1:col_id="status">
                                        <ns1:cellItem>
                                            <ns1:item xsi:type="ns1:simpleText" ns1:label="Status">
                                                <ns1:value>
                                                    <xsl:value-of select="status"/>
                                                </ns1:value>
                                            </ns1:item>
                                        </ns1:cellItem>
                                    </ns1:cell>

                                    <ns1:cell ns1:col_id="subStatus">
                                        <ns1:cellItem>
                                            <ns1:item xsi:type="ns1:simpleText"
                                                      ns1:label="Sub Status">
                                                <ns1:value>
                                                    <xsl:value-of select="subStatus"/>
                                                </ns1:value>
                                            </ns1:item>
                                        </ns1:cellItem>
                                    </ns1:cell>

                                    <!-- Request Type -->
                                    <ns1:cell ns1:col_id="type">
                                        <ns1:cellItem>
                                            <ns1:item xsi:type="ns1:simpleText"
                                                      ns1:label="Request Type">
                                                <ns1:value>
                                                    <xsl:value-of select="type"/>
                                                </ns1:value>
                                            </ns1:item>
                                        </ns1:cellItem>
                                    </ns1:cell>

                                    <!-- Date Created -->
                                    <ns1:cell ns1:col_id="created">
                                        <ns1:cellItem>
                                            <ns1:item xsi:type="ns1:simpleText"
                                                      ns1:label="Date Created">
                                                <ns1:value>
                                                    <xsl:value-of select="created"/>
                                                </ns1:value>
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

            <symbol xsi:type="ns1:footer" ns1:text="" ns1:textAlign="left" ns1:buttonAlign="right">
                <ns1:baseButton ns1:id="logout" ns1:target="main"
                                ns1:url="app-domain/mandates-and-resolutions/logout"
                                ns1:label="Log out" ns1:formSubmit="true"/>
            </symbol>
        </page>
    </xsl:template>
</xsl:stylesheet>
