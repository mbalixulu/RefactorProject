<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="xml"/>
    <xsl:template match="/">
        <page xmlns:comm="http://ws.online.fnb.co.za/v1/common/"
              id="WrapupLandingPage"
              title="Switch" template="main" layout="" version="1"
              xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
            <symbol xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                    xsi:type="comm:formLayout">
                <comm:form
                        comm:action="app-domain/mandates-and-resolutions/tablePopup"
                        comm:name="WrapupLandingForm">
                    <comm:sections comm:width="full">
                        <comm:symbol xsi:type="comm:boxContainer"
                                     comm:id="boxDiv">
                            <comm:box xsi:type="comm:box">
                                <comm:boxSymbol xsi:type="comm:textHeading"
                                                comm:size="4">
                                    <comm:value>Search Result</comm:value>
                                </comm:boxSymbol>
                                <comm:boxSymbol xsi:type="comm:input"
                                                comm:name="companyRegiNumber"
                                                comm:label="Registration number"
                                                comm:readonly="true">
                                    <comm:value>
                                        <xsl:value-of
                                                select="requestWrapper/request/registrationNumber"/>
                                    </comm:value>
                                </comm:boxSymbol>
                                <comm:boxSymbol xsi:type="comm:input"
                                                comm:name="companyName"
                                                comm:errorMessage="{requestWrapper/searchResultsErrorModel/companyName}"
                                                comm:label="Company name *">
                                    <comm:value>
                                        <xsl:value-of
                                                select="requestWrapper/request/companyName"/>
                                    </comm:value>
                                </comm:boxSymbol>
                                <comm:boxSymbol xsi:type="comm:input"
                                                comm:name="companyAddress"
                                                comm:errorMessage="{requestWrapper/searchResultsErrorModel/companyAddress}"
                                                comm:label="Company address *">
                                    <comm:value>
                                        <xsl:value-of
                                                select="requestWrapper/request/companyAddress"/>
                                    </comm:value>
                                </comm:boxSymbol>
                            </comm:box>
                        </comm:symbol>
                        <comm:symbol xsi:type="comm:boxContainer" comm:id="waiverBox">
                            <comm:box xsi:type="comm:box">
                                <comm:boxSymbol xsi:type="comm:textHeading" comm:size="4">
                                    <comm:value>Company waiver</comm:value>
                                </comm:boxSymbol>
                                <xsl:for-each
                                        select="requestWrapper/listOfWaveModel">
                                    <comm:boxSymbol xsi:type="comm:input"
                                                    comm:name="tool{name}"
                                                    comm:label="Tool {position()}">
                                        <comm:value>
                                            <xsl:value-of select="waveTool"/>
                                        </comm:value>
                                    </comm:boxSymbol>
                                </xsl:for-each>
                            </comm:box>
                        </comm:symbol>
                    </comm:sections>
                    <xsl:if test="requestWrapper/checkWaiver = 'true'">
                        <comm:sections comm:width="full">
                            <comm:symbol xsi:type="comm:textHeading" comm:size="4">
                                <comm:value>At least one Waiver Tool is required for this request !
                                </comm:value>
                            </comm:symbol>
                        </comm:sections>
                    </xsl:if>
                    <comm:sections comm:align="left" comm:width="full">
                        <comm:symbol xsi:type="comm:fullTable"
                                     comm:id="MyTable2"
                                     comm:action="GBLanding" comm:downloadLink=""
                                     comm:endpoint=""
                                     comm:heading="Directors"
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
                            </comm:rowGroup>

                            <xsl:for-each
                                    select="requestWrapper/directorModels">
                                <comm:row xsi:type="comm:fullTableRow"
                                          comm:groupId="xxxx">
                                    <comm:cell xsi:type="comm:cell"
                                               comm:col_id="title">
                                        <comm:cellItem
                                                xsi:type="comm:cellItem">
                                            <comm:item
                                                    xsi:type="comm:input"
                                                    comm:readonly="true"
                                                    comm:name="title">
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
                                            <comm:item
                                                    xsi:type="comm:input"
                                                    comm:readonly="true"
                                                    comm:name="fullName">
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
                                            <comm:item
                                                    xsi:type="comm:input"
                                                    comm:readonly="true"
                                                    comm:name="idPassport">
                                                <comm:value>
                                                    <xsl:value-of
                                                            select="designation"/>
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
                                                       comm:url="app-domain/mandates-and-resolutions/editDirector/{userInList}"
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
                                                       comm:url="app-domain/mandates-and-resolutions/removeDirector/{userInList}"
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
                        <comm:symbol xsi:type="comm:button"
                                     comm:id="termsAndConditionsPDFa"
                                     comm:target="main"
                                     comm:url="app-domain/mandates-and-resolutions/tablePopup"
                                     comm:label="Add +"
                                     comm:width="5"
                                     comm:formSubmit="true"
                                     comm:type="highlight"/>
                    </comm:sections>
                    <xsl:if test="requestWrapper/checkDirectorEmpty = 'true'">
                        <comm:sections comm:width="full">
                            <comm:symbol xsi:type="comm:textHeading" comm:size="4">
                                <comm:value>At least one Director is required for this request !
                                </comm:value>
                            </comm:symbol>
                        </comm:sections>
                    </xsl:if>
                    <comm:sections comm:align="right" comm:width="full">
                        <comm:symbol
                                xsi:type="comm:dropdown"
                                comm:id="mandateResolution"
                                comm:label="Request type *"
                                comm:selectedValue="{requestWrapper/requestType}"
                                comm:errorMessage="{requestWrapper/searchResultsErrorModel/requestType}">
                            <comm:label>Mandate</comm:label>
                            <comm:value xsi:type="comm:eventValue">
                                <comm:value>Mandate</comm:value>
                            </comm:value>

                            <comm:label>Resolution</comm:label>
                            <comm:value xsi:type="comm:eventValue">
                                <comm:value>Resolution</comm:value>
                            </comm:value>

                            <comm:label>Mandate And Resolution</comm:label>
                            <comm:value xsi:type="comm:eventValue">
                                <comm:value>Mandate And Resolution</comm:value>
                            </comm:value>
                        </comm:symbol>
                        <xsl:if test="requestWrapper/checkMandates = 'true'">
                            <comm:symbol xsi:type="comm:input"
                                         comm:name="lname2"
                                         comm:label=""
                                         comm:inputType="checkbox"
                                         comm:message=""
                                         comm:errorMessage="{requestWrapper/searchResultsErrorModel/checkStyleOne}"
                                         comm:unCheckedValue="0"
                                         comm:selected="false">
                                <comm:value/>
                                <comm:inputItem comm:id="check1"
                                                comm:label="I confirm that the documents have been uploaded to sigma"
                                                comm:type="checkbox"
                                                comm:value="true"
                                                comm:unCheckedValue="false"
                                                comm:selected="{requestWrapper/checkStyleOne}"/>

                            </comm:symbol>
                            <comm:symbol xsi:type="comm:input"
                                         comm:name="lname2"
                                         comm:label=""
                                         comm:inputType="checkbox"
                                         comm:message=""
                                         comm:errorMessage="{requestWrapper/searchResultsErrorModel/checkStyleTwo}"
                                         comm:unCheckedValue="0"
                                         comm:selected="false">
                                <comm:value/>

                                <comm:inputItem comm:id="check2"
                                                comm:label="I confirm that the signatures of the provided documents align with the waiver requirements"
                                                comm:type="checkbox"
                                                comm:value="true"
                                                comm:unCheckedValue="false"
                                                comm:selected="{requestWrapper/checkStyleTwo}"/>

                            </comm:symbol>
                        </xsl:if>
                        <xsl:if test="requestWrapper/checkResolution = 'true'">
                            <comm:symbol xsi:type="comm:input"
                                         comm:name="lname2"
                                         comm:label=""
                                         comm:inputType="checkbox"
                                         comm:message=""
                                         comm:errorMessage="{requestWrapper/searchResultsErrorModel/checkStyleOne}"
                                         comm:unCheckedValue="0"
                                         comm:selected="false">
                                <comm:value/>

                                <comm:inputItem comm:id="check1"
                                                comm:label="I confirm that the documents have been uploaded to sigma"
                                                comm:type="checkbox"
                                                comm:value="true"
                                                comm:unCheckedValue="false"
                                                comm:selected="{requestWrapper/checkStyleOne}"/>

                            </comm:symbol>
                            <comm:symbol xsi:type="comm:input"
                                         comm:name="lname2"
                                         comm:label=""
                                         comm:inputType="checkbox"
                                         comm:message=""
                                         comm:errorMessage="{requestWrapper/searchResultsErrorModel/checkStyleTwo}"
                                         comm:unCheckedValue="0"
                                         comm:selected="false">
                                <comm:value/>

                                <comm:inputItem comm:id="check2"
                                                comm:label="I confirm that the signatures of the provided documents align with the waiver requirements"
                                                comm:type="checkbox"
                                                comm:value="true"
                                                comm:unCheckedValue="false"
                                                comm:selected="{requestWrapper/checkStyleTwo}"/>

                            </comm:symbol>
                        </xsl:if>
                        <xsl:if test="requestWrapper/checkMandatesAndresolution = 'true'">
                            <comm:symbol xsi:type="comm:input"
                                         comm:name="lname2"
                                         comm:label=""
                                         comm:inputType="checkbox"
                                         comm:message=""
                                         comm:errorMessage="{requestWrapper/searchResultsErrorModel/checkStyleOne}"
                                         comm:unCheckedValue="0"
                                         comm:selected="false">
                                <comm:value/>

                                <comm:inputItem comm:id="check1"
                                                comm:label="I confirm that the documents have been uploaded to sigma"
                                                comm:type="checkbox"
                                                comm:value="true"
                                                comm:unCheckedValue="false"
                                                comm:selected="{requestWrapper/checkStyleOne}"/>

                            </comm:symbol>
                            <comm:symbol xsi:type="comm:input"
                                         comm:name="lname2"
                                         comm:label=""
                                         comm:inputType="checkbox"
                                         comm:message=""
                                         comm:errorMessage="{requestWrapper/searchResultsErrorModel/checkStyleTwo}"
                                         comm:unCheckedValue="0"
                                         comm:selected="false">
                                <comm:value/>

                                <comm:inputItem comm:id="check2"
                                                comm:label="I confirm that the signatures of the provided documents align with the waiver requirements"
                                                comm:type="checkbox"
                                                comm:value="true"
                                                comm:unCheckedValue="false"
                                                comm:selected="{requestWrapper/checkStyleTwo}"/>

                            </comm:symbol>
                        </xsl:if>
                    </comm:sections>
                </comm:form>
            </symbol>
            <symbol xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="comm:footer"
                    comm:text="" comm:textAlign="left" comm:buttonAlign="right">
                <comm:baseButton comm:id="backSearch" comm:target="main"
                                 comm:url="app-domain/mandates-and-resolutions/backCreateReq"
                                 comm:label="Back" comm:formSubmit="true"/>
                <comm:baseButton comm:id="wave" comm:target="main"
                                 comm:url="app-domain/mandates-and-resolutions/wave"
                                 comm:label="Add Waiver Tool +" comm:formSubmit="true"/>
                <xsl:if test="requestWrapper/checkRemoveTool = 'true'">
                    <comm:baseButton comm:id="waveRemove" comm:target="main"
                                     comm:url="app-domain/mandates-and-resolutions/removeTool"
                                     comm:label="Remove Waiver Tool" comm:formSubmit="true"/>
                </xsl:if>
                <comm:baseButton comm:id="save"
                                 comm:target="main"
                                 comm:url="app-domain/mandates-and-resolutions/searchAccountSave"
                                 comm:label="Save"
                                 comm:formSubmit="true"/>
                <xsl:if test="requestWrapper/checkMandates = 'true'">
                    <comm:baseButton comm:id="proceed" comm:target="main"
                                     comm:url="app-domain/mandates-and-resolutions/proceedToAccount"
                                     comm:label="Proceed" comm:formSubmit="true"/>
                </xsl:if>
                <xsl:if test="requestWrapper/checkResolution = 'true'">
                    <comm:baseButton comm:id="proceed" comm:target="main"
                                     comm:url="app-domain/mandates-and-resolutions/proceedToAccountReso"
                                     comm:label="Proceed" comm:formSubmit="true"/>
                </xsl:if>
                <xsl:if test="requestWrapper/checkMandatesAndresolution = 'true'">
                    <comm:baseButton comm:id="proceed" comm:target="main"
                                     comm:url="app-domain/mandates-and-resolutions/proceedToAccount"
                                     comm:label="Proceed" comm:formSubmit="true"/>
                </xsl:if>
            </symbol>
            <symbol xsi:type="comm:setEventAjax"
                    comm:url="app-domain/mandates-and-resolutions/requestType"
                    comm:id="mandateResolution" comm:target="main" comm:event="mousedown"
                    comm:ajaxEventType="submitFormToWorkspace"
                    comm:formName="WrapupLandingForm"/>
        </page>
    </xsl:template>
</xsl:stylesheet>