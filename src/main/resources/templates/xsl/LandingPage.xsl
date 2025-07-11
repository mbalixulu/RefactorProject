<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml"/>
    <xsl:template match="/">
        <page xmlns:ns1="http://ws.online.fnb.co.za/v1/common/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" id="ticketTable" title="Ticket Table" template="main" layout="" version="1">
            <!--Heading-->
            <symbol xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="ns1:subTabGroup" ns1:subTabGroupHeading="Mandates and resolutions"/>

            <!--View Switcher for Filtering Tickets-->
            <symbol xsi:type="ns1:viewGroup" ns1:align="left">
                <ns1:view ns1:text="Pending requests" ns1:type="active" ns1:id="pendingRequests" ns1:url="app-domain/ui/requestTable"/>
                <ns1:view ns1:text="On hold" ns1:type="inactive" ns1:id="onHold" ns1:url="app-domain/ui/requestTable"/>
                <ns1:view ns1:text="Completed" ns1:type="inactive" ns1:id="completed" ns1:url="app-domain/ui/requestTable"/>
            </symbol>

            <!--Three containers | Table-->
            <!--            <symbol xsi:type="ns1:formLayout" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">-->
            <!--                <ns1:form ns1:action="app-domain/ui" ns1:name="fetchTicketForm">-->

            <!--                    <ns1:sections ns1:align="center" ns1:width="full">-->
            <!--                        <ns1:symbol xsi:type="ns1:spacer" ns1:id="space" ns1:height="1"/>-->
            <!--                        <ns1:symbol xsi:type="ns1:cardCarousel" ns1:id="carousel" ns1:height="12">-->
            <!--                            <ns1:cards ns1:id="pendingRequests" ns1:align="center" ns1:width="4">-->
            <!--                                <ns1:cardDivider ns1:position="top">-->
            <!--                                    <ns1:symbol xsi:type="ns1:textHeading" ns1:size="1" ns1:align="left" ns1:color="black">-->
            <!--                                        <ns1:value>Pending Requests</ns1:value>-->
            <!--                                    </ns1:symbol>-->
            <!--                                    <ns1:symbol xsi:type="ns1:hyperlinkList" ns1:id="listID">-->
            <!--                                        <ns1:hyperlinkListItem ns1:id="item2" ns1:label="1" ns1:url="app-domain/ui" ns1:target="panel"/>-->
            <!--                                    </ns1:symbol>-->
            <!--                                </ns1:cardDivider>-->
            <!--                            </ns1:cards>-->
            <!--                            <ns1:cards ns1:id="onHold" ns1:align="center" ns1:width="4">-->
            <!--                                <ns1:cardDivider ns1:position="top">-->
            <!--                                    <ns1:symbol xsi:type="ns1:textHeading" ns1:size="1" ns1:align="left" ns1:color="black">-->
            <!--                                        <ns1:value>On Hold</ns1:value>-->
            <!--                                    </ns1:symbol>-->
            <!--                                </ns1:cardDivider>-->
            <!--                            </ns1:cards>-->
            <!--                            <ns1:cards ns1:id="completed" ns1:align="center" ns1:width="4">-->
            <!--                                <ns1:cardDivider ns1:position="top">-->
            <!--                                    <ns1:symbol xsi:type="ns1:textHeading" ns1:size="1" ns1:align="left" ns1:color="black">-->
            <!--                                        <ns1:value>Completed</ns1:value>-->
            <!--                                    </ns1:symbol>-->
            <!--                                </ns1:cardDivider>-->
            <!--                            </ns1:cards>-->
            <!--                        </ns1:symbol>-->
            <!--                    </ns1:sections>-->

            <!--Table will change depending on which view button is selected-->
            <symbol xsi:type="ns1:formLayout" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
                <ns1:form ns1:action="app-domain/ui" ns1:name="fetchTicketForm">
                    <ns1:sections ns1:id="tableSection" ns1:align="center" ns1:width="full" ns1:tooltip="">
                        <xsl:attribute name="ns1:label">
                            <xsl:choose>
                                <xsl:when test="status = 'onhold'">On hold</xsl:when>
                                <xsl:when test="status = 'completed'">Completed</xsl:when>
                                <xsl:otherwise>Pending tickets</xsl:otherwise>
                            </xsl:choose>
                        </xsl:attribute>

                        <ns1:symbol xsi:type="ns1:fullTable" ns1:id="TemplateTable" ns1:headingColor="primary" ns1:showTotal="true">
                            <xsl:attribute name="ns1:heading">
                                <xsl:choose>
                                    <xsl:when test="status = 'onhold'">On hold</xsl:when>
                                    <xsl:when test="status = 'completed'">Completed</xsl:when>
                                    <xsl:otherwise>Pending tickets</xsl:otherwise>
                                </xsl:choose>
                            </xsl:attribute>

                            <!--Create Request Button-->
                            <ns1:addButton xsi:type="ns1:imageButton" ns1:target="main" ns1:id="createRequestbtn" ns1:label="Create Request" ns1:tooltip="true" ns1:tip="tip" ns1:url="app-domain/ui/createRequest" ns1:formName="ticketForm">
                                <ns1:imageButtonOptions xsi:type="ns1:hyperlinkList" ns1:id="createRequestbtn">
                                    <ns1:hyperlinkListItem xsi:type="ns1:hyperlinkListItem" ns1:target="main" ns1:label="Create Request" ns1:url="app-domain/ui/createRequest"/>
                                </ns1:imageButtonOptions>
                            </ns1:addButton>

                            <!--Search Button-->
                            <ns1:tableSearch ns1:searchPlaceholder="Search Ticket"/>

                            <!--Table Columns-->
                            <ns1:tableColumn ns1:id="requestID" ns1:heading="Request ID" ns1:fieldName="requestID" ns1:disableSorting="true" ns1:widthPercent="9"/>

                            <ns1:tableColumn ns1:id="sla" ns1:heading="SLA" ns1:fieldName="sla" ns1:disableSorting="true" ns1:widthPercent="9"/>

                            <ns1:tableColumn ns1:id="companyName" ns1:heading="Company Name" ns1:fieldName="companyName" ns1:disableSorting="true" ns1:widthPercent="9"/>

                            <ns1:tableColumn ns1:id="status" ns1:heading="Status" ns1:fieldName="status" ns1:disableSorting="true" ns1:widthPercent="9"/>

                            <ns1:tableColumn ns1:id="subStatus" ns1:heading="Sub Status" ns1:fieldName="subStatus" ns1:disableSorting="true" ns1:widthPercent="9"/>

                            <ns1:tableColumn ns1:id="dateCreated" ns1:heading="Date Created" ns1:fieldName="dateCreated" ns1:disableSorting="true" ns1:widthPercent="9"/>

                            <ns1:tableColumn ns1:id="view" ns1:heading="View" ns1:fieldName="view" ns1:disableSorting="true" ns1:widthPercent="9"/>

                            <ns1:rowGroup ns1:groupId="rows"
                                          ns1:groupHeaderLabel=""/>
                            <!--Row Data-->
                            <xsl:for-each select="Templates">
                                <ns1:row ns1:groupId="rows">
                                    <ns1:cell ns1:col_id="templateName">
                                        <ns1:cellItem>
                                            <ns1:item xsi:type="ns1:simpleText"
                                                      ns1:label="Template Name">
                                                <ns1:value>
                                                    <xsl:value-of
                                                            select="templateName"/>
                                                </ns1:value>
                                            </ns1:item>
                                        </ns1:cellItem>
                                    </ns1:cell>
                                    <ns1:cell ns1:col_id="subject">
                                        <ns1:cellItem>
                                            <ns1:item xsi:type="ns1:simpleText"
                                                      ns1:label="Subject">
                                                <ns1:value>
                                                    <xsl:value-of
                                                            select="subject"/>
                                                </ns1:value>
                                            </ns1:item>
                                        </ns1:cellItem>
                                    </ns1:cell>
                                    <ns1:cell ns1:col_id="body">
                                        <ns1:cellItem>
                                            <ns1:item xsi:type="ns1:simpleText" ns1:label="Body">
                                                <ns1:value>
                                                    <xsl:choose>
                                                        <xsl:when test="string-length(body) &gt; 50">
                                                            <xsl:value-of select="concat(substring(body, 1, 50), '...')"/>
                                                        </xsl:when>
                                                        <xsl:otherwise>
                                                            <xsl:value-of select="body"/>
                                                        </xsl:otherwise>
                                                    </xsl:choose>
                                                </ns1:value>
                                            </ns1:item>
                                        </ns1:cellItem>
                                    </ns1:cell>
                                    <ns1:cell ns1:col_id="Status">
                                        <ns1:cellItem>
                                            <ns1:item xsi:type="ns1:simpleText"
                                                      ns1:label="Status">
                                                <ns1:value>
                                                    <xsl:value-of
                                                            select="status"/>
                                                </ns1:value>
                                            </ns1:item>
                                        </ns1:cellItem>
                                    </ns1:cell>
                                    <ns1:cell ns1:col_id="Event">
                                        <ns1:cellItem>
                                            <ns1:item xsi:type="ns1:simpleText"
                                                      ns1:label="Event">
                                                <ns1:value>
                                                    <xsl:value-of
                                                            select="event"/>
                                                </ns1:value>
                                            </ns1:item>
                                        </ns1:cellItem>
                                    </ns1:cell>
                                    <ns1:cell ns1:col_id="viewTemplate">
                                        <ns1:cellItem>
                                            <ns1:item xsi:type="ns1:button"
                                                      ns1:id="{concat('viewBtn_', templateId)}"
                                                      ns1:type="action"
                                                      ns1:width="2"
                                                      ns1:url="{concat('app-domain/notification/viewTemplate/', templateId)}"
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
            <!--Footer button-->
            <symbol xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="ns1:footer" ns1:text="" ns1:textAlign="left" ns1:buttonAlign="right">
                <ns1:baseButton ns1:id="viewBtn" ns1:type="action" ns1:url="app-domain/ui/viewRequest" ns1:target="main" ns1:formSubmit="true" ns1:label="View Request"/>
            </symbol>
        </page>
    </xsl:template>
</xsl:stylesheet>