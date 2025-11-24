<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="xml"/>
    <!-- start from the root of the xml document "/" -->
    <xsl:template match="/">
        <page xmlns:comm="http://ws.online.fnb.co.za/v1/common/"
              xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
              id="boxConatinerID"
              title="Box Container" template="main" layout="" version="1">
            <symbol xsi:type="comm:subTabGroup" comm:subTabGroupHeading="Mandates and resolutions"/>
            <symbol xsi:type="comm:formLayout">
                <comm:form comm:name="selectRequirementsForm"
                           comm:action="">
                    <comm:sections comm:align="left" comm:width="full">
                        <comm:symbol xsi:type="comm:fullTable"
                                     comm:id="MyTable2"
                                     comm:action="GBLanding" comm:downloadLink=""
                                     comm:endpoint=""
                                     comm:heading="appointed directors"
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
                                                       comm:url="app-domain/mandates-and-resolutions/tablePopupReso"
                                                       comm:id="gp1"/>
                            </comm:rowGroup>

                            <xsl:for-each
                                    select="requestWrapper/listOfDirectors">
                                <comm:row xsi:type="comm:fullTableRow"
                                          comm:groupId="xxxx">
                                    <comm:cell xsi:type="comm:cell"
                                               comm:col_id="title">
                                        <comm:cellItem
                                                xsi:type="comm:cellItem">
                                            <comm:item xsi:type="comm:simpleText" comm:align="center">
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
                                            <comm:item xsi:type="comm:simpleText" comm:align="center">
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
                                            <comm:item xsi:type="comm:simpleText" comm:align="center">
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
                                            <comm:item xsi:type="comm:simpleText" comm:align="center">
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
                                            <comm:item xsi:type="comm:button"
                                                       comm:id="remove"
                                                       comm:type="paper"
                                                       comm:width="4"
                                                       comm:url="app-domain/mandates-and-resolutions/removeDirectorReso/{userInList}"
                                                       comm:formSubmit="false"
                                                       comm:target="main"
                                                       comm:tooltip=""
                                                       comm:label="Remove"/>
                                        </comm:cellItem>
                                    </comm:cell>
                                </comm:row>
                            </xsl:for-each>
                            <comm:tableNavigator comm:pageSize="10"/>
                        </comm:symbol>
                    </comm:sections>
                    <xsl:if test="requestWrapper/checkSecondDirectorList = 'true'">
                        <comm:sections comm:width="full">
                            <comm:symbol xsi:type="comm:textHeading" comm:size="4">
                                <comm:value>At least one Director is required for this request !
                                </comm:value>
                            </comm:symbol>
                        </comm:sections>
                    </xsl:if>
                </comm:form>
            </symbol>
            <symbol xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                    xsi:type="comm:footer" comm:text="" comm:textAlign="left"
                    comm:buttonAlign="right">
                <xsl:if test="requestWrapper/checkResolution = 'false'">
                    <comm:baseButton comm:id="back"
                                     comm:url="app-domain/mandates-and-resolutions/backSignatureCard"
                                     comm:target="main" comm:formSubmit="false" comm:tooltip=""
                                     comm:label="Back"/>
                    <comm:baseButton comm:id="next"
                                     comm:url="app-domain/mandates-and-resolutions/saveAppointedDirectors"
                                     comm:target="main" comm:formSubmit="true" comm:tooltip=""
                                     comm:label="Save"/>
                    <comm:baseButton comm:id="next"
                                     comm:url="app-domain/mandates-and-resolutions/submitFinalRecord"
                                     comm:target="main" comm:formSubmit="true" comm:tooltip=""
                                     comm:label="Submit"/>
                </xsl:if>
                <xsl:if test="requestWrapper/checkResolution = 'true'">
                    <comm:baseButton comm:id="back"
                                     comm:url="app-domain/mandates-and-resolutions/backToAccountSearch"
                                     comm:target="main" comm:formSubmit="false" comm:tooltip=""
                                     comm:label="Back"/>
                    <comm:baseButton comm:id="next"
                                     comm:url="app-domain/mandates-and-resolutions/saveAppointedDirectors"
                                     comm:target="main" comm:formSubmit="true" comm:tooltip=""
                                     comm:label="Save"/>
                    <comm:baseButton comm:id="next"
                                     comm:url="app-domain/mandates-and-resolutions/submitResoFinalRecord"
                                     comm:target="main" comm:formSubmit="true" comm:tooltip=""
                                     comm:label="Submit"/>
                </xsl:if>
            </symbol>
        </page>
    </xsl:template>
</xsl:stylesheet>



