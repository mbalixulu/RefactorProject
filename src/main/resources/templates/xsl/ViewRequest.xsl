<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="xml"/>
    <xsl:template match="/">
        <page xmlns:comm="http://ws.online.fnb.co.za/v1/common/"
              xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
              id="boxConatinerID"
              title="Box Container" template="main" layout="" version="1">
            <symbol xsi:type="comm:formLayout">
                <comm:form comm:name="advancedSearchForm"
                           comm:action="">
                    <comm:sections comm:width="full">
                        <comm:symbol xsi:type="comm:textHeading" comm:size="4">
                            <comm:value>View Request:</comm:value>
                        </comm:symbol>
                        <comm:symbol xsi:type="comm:boxContainer"
                                     comm:id="boxDiv">
                            <comm:boxSymbol xsi:type="comm:textHeading"
                                            comm:size="4">
                                <comm:value></comm:value>
                            </comm:boxSymbol>

                            <comm:boxSymbol xsi:type="comm:boxSplit"
                                            comm:width="50">
                                <comm:boxSplitSymbol xsi:type="comm:textReadout"
                                                     comm:subHeading="Request ID:"
                                                     comm:color="ghostmedium">
                                    <comm:value>
                                        <xsl:value-of
                                                select="requestDetails/requestId"/>
                                    </comm:value>
                                </comm:boxSplitSymbol>
                                <comm:boxSplitSymbol xsi:type="comm:textReadout"
                                                     comm:subHeading="Process ID:"
                                                     comm:color="ghostmedium">
                                    <comm:value>
                                        <xsl:value-of
                                                select="requestDetails/processId"/>
                                    </comm:value>
                                </comm:boxSplitSymbol>
                                <comm:boxSplitSymbol xsi:type="comm:textReadout"
                                                     comm:subHeading="Request Type:"
                                                     comm:color="ghostmedium">
                                    <comm:value>
                                        <xsl:value-of
                                                select="requestDetails/type"/>
                                    </comm:value>
                                </comm:boxSplitSymbol>
                                <comm:boxSplitSymbol xsi:type="comm:textReadout"
                                                     comm:subHeading="Status:"
                                                     comm:color="ghostmedium">
                                    <comm:value>
                                        <xsl:value-of
                                                select="requestDetails/status"/>
                                    </comm:value>
                                </comm:boxSplitSymbol>
                                <comm:boxSplitSymbol xsi:type="comm:textReadout"
                                                     comm:subHeading="Sub Status:"
                                                     comm:color="ghostmedium">
                                    <comm:value>
                                        <xsl:value-of
                                                select="requestDetails/subStatus"/>
                                    </comm:value>
                                </comm:boxSplitSymbol>
                                <comm:boxSplitSymbol xsi:type="comm:textReadout"
                                                     comm:subHeading="Assigned User:"
                                                     comm:color="ghostmedium">
                                    <comm:value>
                                        <xsl:value-of
                                                select="requestDetails/assignedUser"/>
                                    </comm:value>
                                </comm:boxSplitSymbol>
                            </comm:boxSymbol>
                            <comm:boxSymbol xsi:type="comm:boxSplit"
                                            comm:width="50">
                                <comm:boxSplitSymbol xsi:type="comm:textReadout"
                                                     comm:subHeading="Company Name:"
                                                     comm:color="ghostmedium">
                                    <comm:value>
                                        <xsl:value-of
                                                select="requestDetails/companyName"/>
                                    </comm:value>
                                </comm:boxSplitSymbol>
                                <comm:boxSplitSymbol xsi:type="comm:textReadout"
                                                     comm:subHeading="SLA (days):"
                                                     comm:color="ghostmedium">
                                    <comm:value>
                                        <xsl:value-of
                                                select="requestDetails/sla"/>
                                    </comm:value>
                                </comm:boxSplitSymbol>
                                <comm:boxSplitSymbol xsi:type="comm:textReadout"
                                                     comm:subHeading="Last Modified Date:"
                                                     comm:color="ghostmedium">
                                    <comm:value>
                                        <xsl:value-of
                                                select="requestDetails/updatedReq"/>
                                    </comm:value>
                                </comm:boxSplitSymbol>
                                <comm:boxSplitSymbol xsi:type="comm:textReadout"
                                                     comm:subHeading="Last Modified By:"
                                                     comm:color="ghostmedium">
                                    <comm:value>
                                        <xsl:value-of
                                                select="requestDetails/updatorRequest"/>
                                    </comm:value>
                                </comm:boxSplitSymbol>
                                <comm:boxSplitSymbol xsi:type="comm:textReadout"
                                                     comm:subHeading="Created Date:"
                                                     comm:color="ghostmedium">
                                    <comm:value>
                                        <xsl:value-of
                                                select="requestDetails/createdReq"/>
                                    </comm:value>
                                </comm:boxSplitSymbol>
                                <comm:boxSplitSymbol xsi:type="comm:textReadout"
                                                     comm:subHeading="Created By:"
                                                     comm:color="ghostmedium">
                                    <comm:value>
                                        <xsl:value-of
                                                select="requestDetails/creatorRequest"/>
                                    </comm:value>
                                </comm:boxSplitSymbol>
                            </comm:boxSymbol>
                        </comm:symbol>
                    </comm:sections>
                    <xsl:if test="requestDetails/type = 'Mandate' or requestDetails/type = 'Mandate And Resolution'">
                        <comm:sections comm:align="left" comm:width="full">
                            <comm:symbol xsi:type="comm:textHeading" comm:align="left">
                                <comm:value>Account Signatories</comm:value>
                            </comm:symbol>
                        </comm:sections>
                    </xsl:if>
                    <xsl:for-each
                            select="requestDetails/listOfAddAccountModel">
                        <comm:sections comm:align="left" comm:width="full">
                            <comm:symbol xsi:type="comm:boxContainer" comm:id="trs">
                                <comm:box xsi:type="comm:box">
                                    <comm:boxSymbol xsi:type="comm:textHeading" comm:size="4">
                                        <comm:value>
                                            <xsl:value-of
                                                    select="accountName"/>
                                            :
                                            <xsl:value-of
                                                    select="accountNumber"/>
                                        </comm:value>
                                    </comm:boxSymbol>
                                    <comm:boxSymbol xsi:type="comm:textHeading" comm:size="5">
                                        <comm:value>Added Signatories</comm:value>
                                    </comm:boxSymbol>
                                    <comm:boxSymbol xsi:type="comm:fullTable"
                                                    comm:id="yxz{position()}"
                                                    comm:heading="" comm:showTotal="false"
                                                    comm:headingColor="black">
                                        <comm:tableColumn comm:align="left"
                                                          comm:fieldName="fullName"
                                                          comm:heading="Full Name"
                                                          comm:disableSorting="true"/>
                                        <comm:tableColumn comm:align="left"
                                                          comm:fieldName="idNumber"
                                                          comm:heading="ID Number"
                                                          comm:disableSorting="true"/>
                                        <comm:tableColumn comm:align="left"
                                                          comm:fieldName="capacity"
                                                          comm:heading="Capacity"
                                                          comm:disableSorting="true"/>
                                        <comm:tableColumn comm:align="left"
                                                          comm:fieldName="group"
                                                          comm:heading="Group"
                                                          comm:disableSorting="true"/>
                                        <comm:tableColumn comm:align="left"
                                                          comm:fieldName="instructions"
                                                          comm:heading="Instruction"
                                                          comm:disableSorting="true"/>

                                        <comm:rowGroup xsi:type="comm:rowGroup"
                                                       comm:groupId="efg"
                                                       comm:groupHeaderLabel="">
                                            <comm:totalsRow comm:category=" ">

                                                <comm:cell xsi:type="comm:cell"
                                                           comm:col_id="fullName">
                                                    <comm:cellItem xsi:type="comm:cellItem">
                                                        <comm:item xsi:type="comm:simpleText">
                                                            <comm:value/>
                                                        </comm:item>
                                                    </comm:cellItem>
                                                </comm:cell>
                                            </comm:totalsRow>
                                        </comm:rowGroup>
                                        <xsl:for-each
                                                select="listOfSignatory">
                                            <xsl:if test="instruction = 'Add'">
                                                <comm:row xsi:type="comm:fullTableRow"
                                                          comm:groupId="abc">
                                                    <comm:cell xsi:type="comm:cell"
                                                               comm:col_id="fullName">
                                                        <comm:cellItem xsi:type="comm:cellItem">
                                                            <comm:item
                                                                    xsi:type="comm:simpleText">
                                                                <comm:value>
                                                                    <xsl:value-of
                                                                            select="fullName"/>
                                                                </comm:value>
                                                            </comm:item>
                                                        </comm:cellItem>
                                                    </comm:cell>
                                                    <comm:cell xsi:type="comm:cell"
                                                               comm:col_id="idNumber">
                                                        <comm:cellItem xsi:type="comm:cellItem">
                                                            <comm:item
                                                                    xsi:type="comm:simpleText">
                                                                <comm:value>
                                                                    <xsl:value-of
                                                                            select="idNumber"/>
                                                                </comm:value>
                                                            </comm:item>
                                                        </comm:cellItem>
                                                    </comm:cell>
                                                    <comm:cell xsi:type="comm:cell"
                                                               comm:col_id="capacity">
                                                        <comm:cellItem xsi:type="comm:cellItem">
                                                            <comm:item
                                                                    xsi:type="comm:simpleText">
                                                                <comm:value>
                                                                    <xsl:value-of
                                                                            ...