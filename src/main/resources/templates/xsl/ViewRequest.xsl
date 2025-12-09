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
                                                                            select="capacity"/>
                                                                </comm:value>
                                                            </comm:item>
                                                        </comm:cellItem>
                                                    </comm:cell>
                                                    <comm:cell xsi:type="comm:cell"
                                                               comm:col_id="group">
                                                        <comm:cellItem xsi:type="comm:cellItem">
                                                            <comm:item
                                                                    xsi:type="comm:simpleText">
                                                                <comm:value>
                                                                    <xsl:value-of
                                                                            select="group"/>
                                                                </comm:value>
                                                            </comm:item>
                                                        </comm:cellItem>
                                                    </comm:cell>
                                                    <comm:cell xsi:type="comm:cell"
                                                               comm:col_id="instructions">
                                                        <comm:cellItem xsi:type="comm:cellItem">
                                                            <comm:item
                                                                    xsi:type="comm:simpleText">
                                                                <comm:value>
                                                                    <xsl:value-of
                                                                            select="instruction"/>
                                                                </comm:value>
                                                            </comm:item>
                                                        </comm:cellItem>
                                                    </comm:cell>
                                                </comm:row>
                                            </xsl:if>
                                        </xsl:for-each>
                                    </comm:boxSymbol>

                                    <comm:boxSymbol xsi:type="comm:textHeading" comm:size="5">
                                        <comm:value>Removed Signatories</comm:value>
                                    </comm:boxSymbol>

                                    <comm:boxSymbol xsi:type="comm:fullTable"
                                                    comm:id="yxzs{position()}"
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
                                            <xsl:if test="instruction = 'Remove'">
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
                                                               comm:col_id="instructions">
                                                        <comm:cellItem xsi:type="comm:cellItem">
                                                            <comm:item
                                                                    xsi:type="comm:simpleText">
                                                                <comm:value>
                                                                    <xsl:value-of
                                                                            select="instruction"/>
                                                                </comm:value>
                                                            </comm:item>
                                                        </comm:cellItem>
                                                    </comm:cell>
                                                </comm:row>
                                            </xsl:if>
                                        </xsl:for-each>
                                    </comm:boxSymbol>
                                </comm:box>
                            </comm:symbol>
                        </comm:sections>
                    </xsl:for-each>
                    <xsl:if test="requestDetails/type !='Mandate'">
                    <comm:sections comm:align="left" comm:width="full">
                        <comm:symbol xsi:type="comm:textHeading" comm:align="left">
                            <comm:value>Appointed Directors</comm:value>
                        </comm:symbol>
                    </comm:sections>
                    <comm:sections comm:align="left" comm:width="full">
                        <comm:symbol xsi:type="comm:boxContainer" comm:id="trs">
                            <comm:box xsi:type="comm:box">
                                <comm:boxSymbol xsi:type="comm:textHeading" comm:size="5">
                                    <comm:value>Added Directors</comm:value>
                                </comm:boxSymbol>
                                <comm:boxSymbol xsi:type="comm:fullTable"
                                                comm:id="Directors"
                                                comm:heading="" comm:showTotal="false"
                                                comm:headingColor="black">
                                    <comm:tableColumn comm:align="left"
                                                      comm:fieldName="fullName"
                                                      comm:heading="First Name"
                                                      comm:disableSorting="true"/>
                                    <comm:tableColumn comm:align="left"
                                                      comm:fieldName="Surname"
                                                      comm:heading="Surname"
                                                      comm:disableSorting="true"/>
                                    <comm:tableColumn comm:align="left"
                                                      comm:fieldName="Designation"
                                                      comm:heading="Designation"
                                                      comm:disableSorting="true"/>
                                    <comm:tableColumn comm:align="left"
                                                      comm:fieldName="instructions"
                                                      comm:heading="instructions"
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
                                            select="requestDetails/listOfDirector">

                                        <comm:row xsi:type="comm:fullTableRow"
                                                  comm:groupId="abc">
                                            <comm:cell xsi:type="comm:cell"
                                                       comm:col_id="fullName">
                                                <comm:cellItem xsi:type="comm:cellItem">
                                                    <comm:item
                                                            xsi:type="comm:simpleText">
                                                        <comm:value>
                                                            <xsl:value-of
                                                                    select="name"/>
                                                        </comm:value>
                                                    </comm:item>
                                                </comm:cellItem>
                                            </comm:cell>
                                            <comm:cell xsi:type="comm:cell"
                                                       comm:col_id="Surname">
                                                <comm:cellItem xsi:type="comm:cellItem">
                                                    <comm:item
                                                            xsi:type="comm:simpleText">
                                                        <comm:value>
                                                            <xsl:value-of
                                                                    select="surname"/>
                                                        </comm:value>
                                                    </comm:item>
                                                </comm:cellItem>
                                            </comm:cell>
                                            <comm:cell xsi:type="comm:cell"
                                                       comm:col_id="Designation">
                                                <comm:cellItem xsi:type="comm:cellItem">
                                                    <comm:item
                                                            xsi:type="comm:simpleText">
                                                        <comm:value>
                                                            <xsl:value-of
                                                                    select="designation"/>
                                                        </comm:value>
                                                    </comm:item>
                                                </comm:cellItem>
                                            </comm:cell>
                                            <comm:cell xsi:type="comm:cell"
                                                       comm:col_id="instructions">
                                                <comm:cellItem xsi:type="comm:cellItem">
                                                    <comm:item
                                                            xsi:type="comm:simpleText">
                                                        <comm:value>
                                                            <xsl:value-of
                                                                    select="instructions"/>
                                                        </comm:value>
                                                    </comm:item>
                                                </comm:cellItem>
                                            </comm:cell>
                                        </comm:row>
                                    </xsl:for-each>
                                </comm:boxSymbol>
                            </comm:box>
                        </comm:symbol>
                    </comm:sections>
                    </xsl:if>
                    <comm:sections comm:align="left" comm:width="full">
                        <comm:symbol xsi:type="comm:textHeading" comm:align="left">
                            <comm:value>Comments</comm:value>
                        </comm:symbol>
                    </comm:sections>
                    <comm:sections comm:align="left" comm:width="full">
                        <comm:symbol xsi:type="comm:boxContainer" comm:id="trs">
                            <comm:box xsi:type="comm:box">
                                <comm:boxSymbol xsi:type="comm:textHeading" comm:size="5">
                                    <comm:value></comm:value>
                                </comm:boxSymbol>
                                <comm:boxSymbol xsi:type="comm:fullTable"
                                                comm:id="comment"
                                                comm:heading="" comm:showTotal="false"
                                                comm:headingColor="black">
                                    <comm:tableColumn comm:align="left"
                                                      comm:fieldName="fullName"
                                                      comm:heading="Name"
                                                      comm:disableSorting="true"/>
                                    <comm:tableColumn comm:align="left"
                                                      comm:fieldName="Surname"
                                                      comm:heading="Created Date"
                                                      comm:disableSorting="true"/>
                                    <comm:tableColumn comm:align="left"
                                                      comm:fieldName="Designation"
                                                      comm:heading="Comments"
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
                                            select="requestDetails/listOfComment">
                                        <comm:row xsi:type="comm:fullTableRow"
                                                  comm:groupId="abc">
                                            <comm:cell xsi:type="comm:cell"
                                                       comm:col_id="fullName">
                                                <comm:cellItem xsi:type="comm:cellItem">
                                                    <comm:item
                                                            xsi:type="comm:simpleText">
                                                        <comm:value>
                                                            <xsl:value-of
                                                                    select="name"/>
                                                        </comm:value>
                                                    </comm:item>
                                                </comm:cellItem>
                                            </comm:cell>
                                            <comm:cell xsi:type="comm:cell"
                                                       comm:col_id="Surname">
                                                <comm:cellItem xsi:type="comm:cellItem">
                                                    <comm:item
                                                            xsi:type="comm:simpleText">
                                                        <comm:value>
                                                            <xsl:value-of
                                                                    select="createdDate"/>
                                                        </comm:value>
                                                    </comm:item>
                                                </comm:cellItem>
                                            </comm:cell>
                                            <comm:cell xsi:type="comm:cell"
                                                       comm:col_id="Designation">
                                                <comm:cellItem xsi:type="comm:cellItem">
                                                    <comm:item
                                                            xsi:type="comm:simpleText">
                                                        <comm:value>
                                                            <xsl:value-of
                                                                    select="commentedText"/>
                                                        </comm:value>
                                                    </comm:item>
                                                </comm:cellItem>
                                            </comm:cell>
                                        </comm:row>
                                    </xsl:for-each>
                                </comm:boxSymbol>
                            </comm:box>
                        </comm:symbol>
                    </comm:sections>
                    <xsl:if test="requestDetails/status != 'On Hold' and requestDetails/status != 'Completed' and requestDetails/status != 'Auto Closed'">
                    <comm:sections comm:align="left" comm:width="full">
                        <comm:symbol xsi:type="comm:boxContainer" comm:id="instructionsBox">
                            <comm:box xsi:type="comm:box">
                                <comm:boxSymbol xsi:type="comm:textList"
                                                comm:subHeading="Instructions">
                                    <comm:value/>
                                </comm:boxSymbol>
                                <xsl:for-each
                                        select="requestDetails/listOfInstruction">
                                    <comm:boxSymbol xsi:type="comm:textReadout"
                                                    comm:subHeading="">
                                        <comm:value>.
                                            <xsl:value-of
                                                    select="instruction"/>
                                        </comm:value>
                                    </comm:boxSymbol>
                                </xsl:for-each>
                                <comm:boxSymbol xsi:type="comm:input"
                                                comm:name="confirmationCheckMandate"
                                                comm:inputType="checkbox"
                                                comm:unCheckedValue="No"
                                                comm:errorMessage="{requestDetails/viewPageError}"
                                                comm:selected="false">
                                    <comm:value/>
                                    <comm:inputItem comm:id="confirmationCheckMandate"
                                                    comm:label="I confirm that the Instructions have been followed as mentioned above."
                                                    comm:type="checkbox"
                                                    comm:value="1"
                                                    comm:unCheckedValue="false"
                                                    comm:selected="false"/>
                                </comm:boxSymbol>
                            </comm:box>
                        </comm:symbol>
                    </comm:sections>
                    </xsl:if>
                </comm:form>
            </symbol>
            <symbol xsi:type="comm:footer" comm:text="" comm:textAlign="left"
                    comm:buttonAlign="right">
                <xsl:if test="requestDetails/checkStatus = 'false'">
                    <xsl:if test="requestDetails/checkReassignee = 'true'">
                        <comm:baseButton comm:id="assign"
                                         comm:url="app-domain/mandates-and-resolutions/adminReassign"
                                         comm:target="main" comm:formSubmit="false" comm:tooltip=""
                                         comm:label="Re-Assign"/>
                    </xsl:if>
                    <comm:baseButton comm:id="edit"
                                     comm:url="app-domain/mandates-and-resolutions/editRequest"
                                     comm:target="main" comm:formSubmit="false" comm:tooltip=""
                                     comm:label="Edit"/>
                    <xsl:if test="requestDetails/status = 'In Progress' or requestDetails/status = 'Breached'">
                        <comm:baseButton comm:id="hold"
                                         comm:url="app-domain/mandates-and-resolutions/viewRequestHold/{requestDetails/requestId}"
                                         comm:target="main" comm:formSubmit="false" comm:tooltip=""
                                         comm:label="Hold"/>
                    </xsl:if>
                    <xsl:if test="requestDetails/status = 'On Hold'">
                        <comm:baseButton comm:id="unhold"
                                         comm:url="app-domain/mandates-and-resolutions/viewRequestUnhold/{requestDetails/requestId}"
                                         comm:target="main" comm:formSubmit="false" comm:tooltip=""
                                         comm:label="UnHold"/>
                    </xsl:if>
                    <xsl:if test="requestDetails/status = 'In Progress' or requestDetails/status = 'Breached'">
                        <xsl:if test="requestDetails/checkHoganVarificationPending = 'true'">
                            <comm:baseButton comm:id="hogan"
                                             comm:url="app-domain/mandates-and-resolutions/approve/{requestDetails/requestId}"
                                             comm:target="main" comm:formSubmit="true"
                                             comm:tooltip="" comm:label="Verify for Hogan"/>
                        </xsl:if>
                        <xsl:if test="requestDetails/checkWindeedVarificationPending = 'true'">
                            <comm:baseButton comm:id="hogan"
                                             comm:url="app-domain/mandates-and-resolutions/approve/{requestDetails/requestId}"
                                             comm:target="main" comm:formSubmit="true"
                                             comm:tooltip="" comm:label="Verify for Windeed"/>
                        </xsl:if>
                        <xsl:if test="requestDetails/checkHanisVarificationPending = 'true'">
                            <comm:baseButton comm:id="hogan"
                                             comm:url="app-domain/mandates-and-resolutions/approve/{requestDetails/requestId}"
                                             comm:target="main" comm:formSubmit="true"
                                             comm:tooltip="" comm:label="Verify for Hanis"/>
                        </xsl:if>
                        <xsl:if test="requestDetails/checkAdminApprovePending = 'true'">
                            <comm:baseButton comm:id="hogan"
                                             comm:url="app-domain/mandates-and-resolutions/approve/{requestDetails/requestId}"
                                             comm:target="main" comm:formSubmit="true"
                                             comm:tooltip="" comm:label="Verify for Admin Approve"/>
                        </xsl:if>
                        <xsl:if test="requestDetails/checkHoganUpdatePending = 'true'">
                            <comm:baseButton comm:id="hogan"
                                             comm:url="app-domain/mandates-and-resolutions/approve/{requestDetails/requestId}"
                                             comm:target="main" comm:formSubmit="true"
                                             comm:tooltip="" comm:label="Verify for Hogan Update"/>
                        </xsl:if>
                        <xsl:if test="requestDetails/checkDocumentUpdatePending = 'true'">
                            <comm:baseButton comm:id="hogan"
                                             comm:url="app-domain/mandates-and-resolutions/approve/{requestDetails/requestId}"
                                             comm:target="main" comm:formSubmit="true"
                                             comm:tooltip="" comm:label="Verify for Document"/>
                        </xsl:if>
                        <comm:baseButton comm:id="reject"
                                         comm:url="app-domain/mandates-and-resolutions/reject-validate/{requestDetails/requestId}"
                                         comm:target="main" comm:formSubmit="true"
                                         comm:tooltip="" comm:label="Reject"/>
                    </xsl:if>
                </xsl:if>
                <comm:baseButton comm:id="back"
                                 comm:url="app-domain/mandates-and-resolutions/finish"
                                 comm:target="main" comm:formSubmit="false"
                                 comm:tooltip="" comm:label="Back"/>
            </symbol>
        </page>
    </xsl:template>
</xsl:stylesheet>