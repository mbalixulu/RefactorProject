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
                            <comm:value>Edit Request:</comm:value>
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
                                                select="requestDetails/lastModifiedDate"/>
                                    </comm:value>
                                </comm:boxSplitSymbol>
                                <comm:boxSplitSymbol xsi:type="comm:textReadout"
                                                     comm:subHeading="Last Modified By:"
                                                     comm:color="ghostmedium">
                                    <comm:value>
                                        <xsl:value-of
                                                select="requestDetails/lastModifiedBy"/>
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
                    <comm:sections comm:align="left" comm:width="full">
                        <comm:symbol xsi:type="comm:textHeading" comm:align="left">
                            <comm:value>Account Signatories</comm:value>
                        </comm:symbol>
                    </comm:sections>
                    <xsl:for-each
                            select="requestDetails/listOfAddAccountModel">
                        <comm:sections comm:width="full">
                            <comm:symbol xsi:type="comm:textHeading"
                                         comm:size="4">
                                <comm:value>Account
                                    <xsl:value-of select="position()"/>
                                </comm:value>
                            </comm:symbol>
                            <comm:symbol xsi:type="comm:boxContainer"
                                         comm:id="boxDiv">
                                <comm:box xsi:type="comm:box">
                                    <comm:boxSymbol xsi:type="comm:boxSplit"
                                                    comm:width="25">
                                        <comm:boxSplitSymbol xsi:type="comm:textReadout"
                                                             comm:subHeading=""
                                                             comm:color="ghostmedium">
                                            <comm:value></comm:value>
                                        </comm:boxSplitSymbol>
                                    </comm:boxSymbol>
                                    <comm:boxSymbol xsi:type="comm:boxSplit"
                                                    comm:width="25">
                                        <comm:boxSplitSymbol xsi:type="comm:textReadout"
                                                             comm:subHeading=""
                                                             comm:color="ghostmedium">
                                            <comm:value></comm:value>
                                        </comm:boxSplitSymbol>
                                    </comm:boxSymbol>
                                    <comm:boxSymbol xsi:type="comm:boxSplit"
                                                    comm:width="25">
                                        <comm:boxSplitSymbol xsi:type="comm:button"
                                                             comm:id="assignee"
                                                             comm:target="main"
                                                             comm:url="app-domain/mandates-and-resolutions/editAccount/{userInList}"
                                                             comm:label="Edit"
                                                             comm:width="3"
                                                             comm:formSubmit="false"
                                                             comm:type="paper"/>
                                    </comm:boxSymbol>
                                    <comm:boxSymbol xsi:type="comm:boxSplit"
                                                    comm:width="25">
                                        <xsl:if test="checkDelete = 'No'">
                                            <comm:boxSplitSymbol xsi:type="comm:button"
                                                                 comm:id="assignee"
                                                                 comm:target="main"
                                                                 comm:url="app-domain/mandates-and-resolutions/deleteAccount/{userInList}"
                                                                 comm:label="Delete"
                                                                 comm:width="3"
                                                                 comm:formSubmit="false"
                                                                 comm:type="paper"/>
                                        </xsl:if>
                                        <xsl:if test="checkDelete = 'Yes'">
                                            <comm:boxSplitSymbol xsi:type="comm:button"
                                                                 comm:id="assignee"
                                                                 comm:target="main"
                                                                 comm:url="app-domain/mandates-and-resolutions/deleteAccountUndo/{userInList}"
                                                                 comm:label="Undo"
                                                                 comm:width="3"
                                                                 comm:formSubmit="false"
                                                                 comm:type="paper"/>
                                        </xsl:if>
                                    </comm:boxSymbol>
                                    <comm:boxSymbol xsi:type="comm:boxSplit"
                                                    comm:width="50">
                                        <comm:boxSplitSymbol xsi:type="comm:textReadout"
                                                             comm:subHeading="Account Name:"
                                                             comm:color="ghostmedium">
                                            <comm:value>
                                                <xsl:value-of
                                                        select="accountName"/>
                                            </comm:value>
                                        </comm:boxSplitSymbol>
                                    </comm:boxSymbol>
                                    <comm:boxSymbol xsi:type="comm:boxSplit"
                                                    comm:width="50">
                                        <comm:boxSplitSymbol xsi:type="comm:textReadout"
                                                             comm:subHeading="Account Number:"
                                                             comm:color="ghostmedium">
                                            <comm:value>
                                                <xsl:value-of
                                                        select="accountNumber"/>
                                            </comm:value>
                                        </comm:boxSplitSymbol>
                                    </comm:boxSymbol>
                                </comm:box>
                            </comm:symbol>

                            <comm:symbol xsi:type="comm:fullTable"
                                         comm:id="directorsTable{position()}"
                                         comm:action="GBLanding"
                                         comm:downloadLink=""
                                         comm:endpoint=""
                                         comm:heading="Signatory/ies"
                                         comm:showSearch="false"
                                         comm:showTotal="false"
                                         comm:showSaveAndPrint="true"
                                         comm:showPrintAndDownload="false"
                                         comm:headingColor="black">

                                <comm:tableColumn comm:align="center" comm:fieldName="alias"
                                                  comm:groupId="name"
                                                  comm:heading="Full Name" comm:id="nameid"
                                                  comm:calcTotal="false"
                                                  comm:selectAll="false"/>
                                <comm:tableColumn comm:align="center" comm:fieldName="alias"
                                                  comm:groupId="ID Number"
                                                  comm:heading="ID Number" comm:id="surnameid"
                                                  comm:calcTotal="false"
                                                  comm:selectAll="false"/>
                                <comm:tableColumn comm:align="center" comm:fieldName="alias"
                                                  comm:groupId="instruction"
                                                  comm:heading="Instruction"
                                                  comm:id="instructionId"
                                                  comm:calcTotal="false"
                                                  comm:selectAll="false"/>
                                <!-- Directors Group -->
                                <comm:rowGroup xsi:type="comm:rowGroup"
                                               comm:groupId="directors"
                                               comm:groupHeaderLabel="Directors List">
                                </comm:rowGroup>

                                <!-- Render each director as editable row -->
                                <xsl:for-each
                                        select="listOfSignatory">
                                    <comm:row xsi:type="comm:fullTableRow"
                                              comm:groupId="xxxx">
                                        <comm:cell xsi:type="comm:cell"
                                                   comm:col_id="fullName">
                                            <comm:cellItem
                                                    xsi:type="comm:cellItem">
                                                <comm:item xsi:type="comm:simpleText"
                                                           comm:align="center">
                                                    <comm:value>
                                                        <xsl:value-of
                                                                select="fullName"/>
                                                    </comm:value>
                                                </comm:item>
                                            </comm:cellItem>
                                        </comm:cell>
                                        <comm:cell xsi:type="comm:cell"
                                                   comm:col_id="idNumber">
                                            <comm:cellItem
                                                    xsi:type="comm:cellItem">
                                                <comm:item xsi:type="comm:simpleText"
                                                           comm:align="center">
                                                    <comm:value>
                                                        <xsl:value-of
                                                                select="idNumber"/>
                                                    </comm:value>
                                                </comm:item>
                                            </comm:cellItem>
                                        </comm:cell>
                                        <comm:cell xsi:type="comm:cell"
                                                   comm:col_id="instruction">
                                            <comm:cellItem
                                                    xsi:type="comm:cellItem">
                                                <comm:item xsi:type="comm:simpleText"
                                                           comm:align="center">
                                                    <comm:value>
                                                        <xsl:value-of
                                                                select="instruction"/>
                                                    </comm:value>
                                                </comm:item>
                                            </comm:cellItem>
                                        </comm:cell>
                                    </comm:row>
                                </xsl:for-each>
                            </comm:symbol>
                        </comm:sections>
                    </xsl:for-each>
                    <comm:sections comm:align="left" comm:width="full">
                        <comm:symbol xsi:type="comm:boxContainer" comm:id="trs">
                            <comm:box xsi:type="comm:box">
                                <comm:boxSymbol xsi:type="comm:fullTable"
                                                comm:id="MyTable2"
                                                comm:action="GBLanding" comm:downloadLink=""
                                                comm:endpoint=""
                                                comm:heading="Appointed Directors"
                                                comm:showTotal="false"
                                                comm:showSaveAndPrint="true"
                                                comm:defaultSortIndex="1"
                                                comm:headingColor="black">
                                    <comm:tableColumn comm:align="center"
                                                      comm:fieldName="title"
                                                      comm:groupId="group1"
                                                      comm:heading="Name"
                                                      comm:id="title" comm:calcTotal="false"
                                                      comm:selectAll="false"
                                                      comm:widthPercent="30"/>
                                    <comm:tableColumn comm:align="center"
                                                      comm:fieldName="fullName"
                                                      comm:groupId="group1"
                                                      comm:heading="Surname"
                                                      comm:id="fullName"
                                                      comm:calcTotal="false"
                                                      comm:selectAll="false"
                                                      comm:widthPercent="30"/>
                                    <comm:tableColumn comm:align="center"
                                                      comm:fieldName="idPassport"
                                                      comm:groupId="idPassport"
                                                      comm:heading="Designation"
                                                      comm:id="idPassport"
                                                      comm:calcTotal="false"
                                                      comm:selectAll="false"
                                                      comm:widthPercent="30"/>
                                    <comm:tableColumn comm:align="center"
                                                      comm:fieldName="idPassport"
                                                      comm:groupId="instruction"
                                                      comm:heading="Instruction"
                                                      comm:id="instruction"
                                                      comm:calcTotal="false"
                                                      comm:selectAll="false"
                                                      comm:widthPercent="30"/>
                                    <comm:tableColumn comm:align="center"
                                                      comm:fieldName="edit"
                                                      comm:groupId="group1"
                                                      comm:heading=""
                                                      comm:id="space"
                                                      comm:calcTotal="false"
                                                      comm:selectAll="false"
                                                      comm:widthPercent="5"
                                                      comm:disableSorting="true"/>
                                    <comm:tableColumn comm:align="left"
                                                      comm:fieldName="edit"
                                                      comm:groupId="group1"
                                                      comm:heading="Edit Director"
                                                      comm:id="editScreen"
                                                      comm:calcTotal="false"
                                                      comm:selectAll="false"
                                                      comm:widthPercent="30"
                                                      comm:disableSorting="true"/>
                                    <comm:tableColumn comm:align="center"
                                                      comm:fieldName="edit"
                                                      comm:groupId="group1"
                                                      comm:heading=""
                                                      comm:id="space"
                                                      comm:calcTotal="false"
                                                      comm:selectAll="false"
                                                      comm:widthPercent="1"
                                                      comm:disableSorting="true"/>
                                    <comm:tableColumn comm:align="left"
                                                      comm:fieldName="phoneNumber"
                                                      comm:groupId="group1"
                                                      comm:heading="Remove"
                                                      comm:id="Remove"
                                                      comm:calcTotal="false"
                                                      comm:selectAll="false"
                                                      comm:widthPercent="30"
                                                      comm:disableSorting="true"/>
                                    <comm:rowGroup xsi:type="comm:rowGroup"
                                                   comm:groupId="xxxx"
                                                   comm:groupHeaderLabel="Label XXXXX">
                                        <comm:groupTableButton xsi:type="comm:imageButton"
                                                               comm:tooltip="true"
                                                               comm:formName="selectRequirementsForm"
                                                               comm:tip="" comm:target="main"
                                                               comm:url="app-domain/mandates-and-resolutions/tablePopupResoEdit"
                                                               comm:id="gp1"/>
                                    </comm:rowGroup>

                                    <xsl:for-each
                                            select="requestDetails/listOfDirector">
                                        <comm:row xsi:type="comm:fullTableRow"
                                                  comm:groupId="xxxx">
                                            <comm:cell xsi:type="comm:cell"
                                                       comm:col_id="title">
                                                <comm:cellItem
                                                        xsi:type="comm:cellItem">
                                                    <comm:item xsi:type="comm:simpleText"
                                                               comm:align="center">
                                                        <comm:value>
                                                            <xsl:value-of
                                                                    select="name"/>
                                                        </comm:value>
                                                    </comm:item>
                                                </comm:cellItem>
                                            </comm:cell>
                                            <comm:cell xsi:type="comm:cell"
                                                       comm:col_id="fullName">
                                                <comm:cellItem
                                                        xsi:type="comm:cellItem">
                                                    <comm:item xsi:type="comm:simpleText"
                                                               comm:align="center">
                                                        <comm:value>
                                                            <xsl:value-of
                                                                    select="surname"/>
                                                        </comm:value>
                                                    </comm:item>
                                                </comm:cellItem>
                                            </comm:cell>
                                            <comm:cell xsi:type="comm:cell"
                                                       comm:col_id="idPassport">
                                                <comm:cellItem
                                                        xsi:type="comm:cellItem">
                                                    <comm:item xsi:type="comm:simpleText"
                                                               comm:align="center">
                                                        <comm:value>
                                                            <xsl:value-of
                                                                    select="designation"/>
                                                        </comm:value>
                                                    </comm:item>
                                                </comm:cellItem>
                                            </comm:cell>
                                            <comm:cell xsi:type="comm:cell"
                                                       comm:col_id="instruction">
                                                <comm:cellItem
                                                        xsi:type="comm:cellItem">
                                                    <comm:item xsi:type="comm:simpleText"
                                                               comm:align="center">
                                                        <comm:value>
                                                            <xsl:value-of
                                                                    select="instructions"/>
                                                        </comm:value>
                                                    </comm:item>
                                                </comm:cellItem>
                                            </comm:cell>
                                            <comm:cell xsi:type="comm:cell"
                                                       comm:col_id="space">
                                                <comm:cellItem
                                                        xsi:type="comm:cellItem">
                                                    <comm:item
                                                            xsi:type="comm:textReadout">
                                                        <comm:value>-</comm:value>
                                                    </comm:item>
                                                </comm:cellItem>
                                            </comm:cell>
                                            <comm:cell xsi:type="comm:cell"
                                                       comm:col_id="editScreen">
                                                <comm:cellItem
                                                        xsi:type="comm:cellItem">
                                                    <comm:item xsi:type="comm:button"
                                                               comm:id="edit"
                                                               comm:type="paper"
                                                               comm:width="4"
                                                               comm:url="app-domain/mandates-and-resolutions/editDirectorReso/{userInList}"
                                                               comm:formSubmit="false"
                                                               comm:target="main"
                                                               comm:tooltip=""
                                                               comm:label="Edit"/>
                                                </comm:cellItem>
                                            </comm:cell>
                                            <comm:cell xsi:type="comm:cell"
                                                       comm:col_id="space">
                                                <comm:cellItem
                                                        xsi:type="comm:cellItem">
                                                    <comm:item
                                                            xsi:type="comm:textReadout">
                                                        <comm:value>-</comm:value>
                                                    </comm:item>
                                                </comm:cellItem>
                                            </comm:cell>
                                            <comm:cell xsi:type="comm:cell"
                                                       comm:col_id="Remove">
                                                <comm:cellItem
                                                        xsi:type="comm:cellItem">
                                                    <xsl:if test="checkDelete = 'No'">
                                                    <comm:item xsi:type="comm:button"
                                                               comm:id="remove"
                                                               comm:type="paper"
                                                               comm:width="4"
                                                               comm:url="app-domain/mandates-and-resolutions/removeDirectorResoEdit/{userInList}"
                                                               comm:formSubmit="false"
                                                               comm:target="main"
                                                               comm:tooltip=""
                                                               comm:label="Remove"/>
                                                    </xsl:if>
                                                    <xsl:if test="checkDelete = 'Yes'">
                                                    <comm:item xsi:type="comm:button"
                                                               comm:id="remove"
                                                               comm:type="paper"
                                                               comm:width="4"
                                                               comm:url="app-domain/mandates-and-resolutions/undoEditDirector"
                                                               comm:formSubmit="false"
                                                               comm:target="main"
                                                               comm:tooltip=""
                                                               comm:label="Undo"/>
                                                    </xsl:if>
                                                </comm:cellItem>
                                            </comm:cell>
                                        </comm:row>
                                    </xsl:for-each>
                                    <comm:tableNavigator comm:pageSize="10"/>
                                </comm:boxSymbol>
                            </comm:box>
                        </comm:symbol>
                    </comm:sections>
                </comm:form>
            </symbol>
            <symbol xsi:type="comm:footer" comm:text="" comm:textAlign="left"
                    comm:buttonAlign="right">
                <comm:baseButton comm:id="cancel"
                                 comm:url="app-domain/mandates-and-resolutions/adminViewBack"
                                 comm:target="main" comm:formSubmit="false" comm:tooltip=""
                                 comm:label="Back"/>
                <comm:baseButton comm:id="next"
                                 comm:url="app-domain/mandates-and-resolutions/updateDirectorsResoEdit/{directorModel/userInList}"
                                 comm:target="main" comm:formSubmit="true" comm:tooltip=""
                                 comm:label="Update"/>
            </symbol>
        </page>
    </xsl:template>
</xsl:stylesheet>