<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml"/>
    <xsl:template match="/requestWrapper">

        <page xmlns:comm="http://ws.online.fnb.co.za/v1/common/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" id="boxConatinerID" title="Box Container" template="main" layout="" version="1">
            <!--Page Heading-->
            <symbol xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="comm:subTabGroup" comm:subTabGroupHeading="Mandates and resolutions"/>

            <!--Box Symbols-->
            <symbol xsi:type="comm:formLayout">
                <comm:form comm:name="searchResults" comm:action="http://localhost:8445">
                    <comm:sections comm:width="full">
                        <!--Sub Heading-->
                        <comm:symbol xsi:type="comm:textHeading">
                            <comm:value>Search Results</comm:value>
                        </comm:symbol>

                        <!--Company registration number-->
                        <comm:symbol xsi:type="comm:boxContainer" comm:id="boxDiv">
                            <comm:box xsi:type="comm:box">
                                <comm:boxSymbol xsi:type="comm:textHeading" comm:size="4">
                                    <comm:value>Company registration number</comm:value>
                                </comm:boxSymbol>
                                <comm:boxSymbol xsi:type="comm:boxSplit" comm:width="33">
                                    <comm:boxSplitSymbol xsi:type="comm:textReadout" comm:color="ghostmedium">
                                        <comm:value>
                                            <xsl:value-of select="/requestWrapper/request/registrationNumber"/>
                                        </comm:value>
                                    </comm:boxSplitSymbol>
                                </comm:boxSymbol>
                            </comm:box>
                        </comm:symbol>

                        <comm:symbol xsi:type="comm:boxContainer">
                            <comm:box xsi:type="comm:box">
                                <comm:boxSymbol xsi:type="comm:textHeading" comm:size="4">
                                    <comm:value>Company details</comm:value>
                                </comm:boxSymbol>

                                <!-- Company Name -->
                                <comm:boxSymbol xsi:type="comm:boxSplit" comm:width="33">
                                    <xsl:choose>
                                        <xsl:when test="/requestWrapper/request/editable='true'">
                                            <comm:boxSplitSymbol xsi:type="comm:input"
                                                                 comm:name="companyName"
                                                                 comm:label="Company name">
                                                <comm:value>
                                                    <xsl:value-of select="/requestWrapper/request/companyName"/>
                                                </comm:value>
                                            </comm:boxSplitSymbol>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <comm:boxSplitSymbol xsi:type="comm:textReadout"
                                                                 comm:subHeading="Company name"
                                                                 comm:color="ghostmedium">
                                                <comm:value>
                                                    <xsl:value-of select="/requestWrapper/request/companyName"/>
                                                </comm:value>
                                            </comm:boxSplitSymbol>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </comm:boxSymbol>

                                <!-- Registration Number -->
                                <comm:boxSymbol xsi:type="comm:boxSplit" comm:width="33">
                                    <comm:boxSplitSymbol xsi:type="comm:textReadout"
                                                         comm:subHeading="Registration number"
                                                         comm:color="ghostmedium">
                                        <comm:value>
                                            <xsl:value-of select="/requestWrapper/request/registrationNumber"/>
                                        </comm:value>
                                    </comm:boxSplitSymbol>
                                </comm:boxSymbol>

                                <!-- Company Address -->
                                <comm:boxSymbol xsi:type="comm:boxSplit" comm:width="33">
                                    <xsl:choose>
                                        <xsl:when test="/requestWrapper/request/editable='true'">
                                            <comm:boxSplitSymbol xsi:type="comm:input"
                                                                 comm:name="companyAddress"
                                                                 comm:label="Company address">
                                                <comm:value>
                                                    <xsl:value-of select="/requestWrapper/request/companyAddress"/>
                                                </comm:value>
                                            </comm:boxSplitSymbol>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <comm:boxSplitSymbol xsi:type="comm:textReadout"
                                                                 comm:subHeading="Company address"
                                                                 comm:color="ghostmedium">
                                                <comm:value>
                                                    <xsl:value-of select="/requestWrapper/request/companyAddress"/>
                                                </comm:value>
                                            </comm:boxSplitSymbol>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </comm:boxSymbol>

                            </comm:box>
                        </comm:symbol>


                        <!--Company waiver-->
                        <comm:symbol xsi:type="comm:boxContainer" comm:id="boxDiv">
                            <comm:box xsi:type="comm:box">
                                <comm:boxSymbol xsi:type="comm:textHeading" comm:size="4">
                                    <comm:value>Company waiver</comm:value>
                                </comm:boxSymbol>

                                <!--Dynamic rendering of tools-->
                                <xsl:for-each select="/requestWrapper/request/documentumTool">
                                    <comm:boxSymbol xsi:type="comm:boxSplit">
                                        <xsl:choose>
                                            <xsl:when test="/requestWrapper/request/editable='true'">
                                                <comm:boxSplitSymbol xsi:type="comm:input"
                                                                     comm:name="documentumTool"
                                                                     comm:label="Tool">
                                                    <comm:value><xsl:value-of select="."/></comm:value>
                                                </comm:boxSplitSymbol>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <comm:boxSplitSymbol xsi:type="comm:textReadout"
                                                                     comm:subHeading="Tool"
                                                                     comm:color="ghostmedium">
                                                    <comm:value><xsl:value-of select="."/></comm:value>
                                                </comm:boxSplitSymbol>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </comm:boxSymbol>
                                </xsl:for-each>

                                <!--Fallback if no tools exist-->
                                <xsl:if test="not(/requestWrapper/request/documentumTool)">
                                    <comm:boxSymbol xsi:type="comm:boxSplit">
                                        <comm:boxSplitSymbol xsi:type="comm:textReadout" comm:subHeading="Tool" comm:color="ghostmedium">
                                            <comm:value>No tools available</comm:value>
                                        </comm:boxSplitSymbol>
                                    </comm:boxSymbol>
                                </xsl:if>

                            </comm:box>
                        </comm:symbol>

                        <!-- Directors Table -->
                        <comm:symbol xsi:type="comm:fullTable"
                                     comm:id="MyTable"
                                     comm:action="GBLanding"
                                     comm:downloadLink=""
                                     comm:endpoint=""
                                     comm:heading="Directors"
                                     comm:showSearch="false"
                                     comm:showTotal="false"
                                     comm:showSaveAndPrint="true"
                                     comm:showPrintAndDownload="false"
                                     comm:headingColor="black">

                            <comm:tableColumn comm:align="left" comm:fieldName="alias" comm:groupId="name"
                                              comm:heading="Name" comm:id="nameid" comm:calcTotal="false"
                                              comm:selectAll="false"/>
                            <comm:tableColumn comm:align="left" comm:fieldName="alias" comm:groupId="surname"
                                              comm:heading="Surname" comm:id="surnameid" comm:calcTotal="false"
                                              comm:selectAll="false"/>
                            <comm:tableColumn comm:align="left" comm:fieldName="alias" comm:groupId="designation"
                                              comm:heading="Designation" comm:id="designationid" comm:calcTotal="false"
                                              comm:selectAll="false"/>
                            <comm:tableColumn comm:align="left" comm:fieldName="remove" comm:groupId="remove"
                                              comm:heading="Remove" comm:id="remove" comm:calcTotal="false"
                                              comm:selectAll="false"/>

                            <comm:rowGroup xsi:type="comm:rowGroup" comm:groupId="directors" comm:groupHeaderLabel="Directors List">
                                <!-- Add Director Button -->
                                <comm:groupTableButton xsi:type="comm:imageButton"
                                                       comm:id="addDirectorBtn"
                                                       comm:label="Add a director"
                                                       comm:tip="Click to add a new director"
                                                       comm:url="{concat('app-domain/ui/searchCompanyDetails?companyRegNumber=', /requestWrapper/request/registrationNumber, '&amp;directorCount=', count(/requestWrapper/request/directors/director) + 1)}"/>
                            </comm:rowGroup>

                            <!-- Render each director as editable row -->
                            <xsl:for-each select="/requestWrapper/request/directors/director">
                                <comm:row xsi:type="comm:fullTableRow" comm:groupId="directors">

                                    <!-- Name input field -->
                                    <comm:cell xsi:type="comm:cell" comm:col_id="name">
                                        <comm:cellItem xsi:type="comm:cellItem">
                                            <xsl:choose>
                                                <xsl:when test="../../editable='true'">
                                                    <comm:item xsi:type="comm:input" comm:name="name">
                                                        <comm:value><xsl:value-of select="name"/></comm:value>
                                                    </comm:item>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    <comm:item xsi:type="comm:textReadout">
                                                        <comm:value><xsl:value-of select="name"/></comm:value>
                                                    </comm:item>
                                                </xsl:otherwise>
                                            </xsl:choose>
                                        </comm:cellItem>
                                    </comm:cell>

                                    <!-- Surname input field -->
                                    <comm:cell xsi:type="comm:cell" comm:col_id="surname">
                                        <comm:cellItem xsi:type="comm:cellItem">
                                            <xsl:choose>
                                                <xsl:when test="../../editable='true'">
                                                    <comm:item xsi:type="comm:input" comm:name="surname">
                                                        <comm:value><xsl:value-of select="surname"/></comm:value>
                                                    </comm:item>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    <comm:item xsi:type="comm:textReadout">
                                                        <comm:value><xsl:value-of select="surname"/></comm:value>
                                                    </comm:item>
                                                </xsl:otherwise>
                                            </xsl:choose>
                                        </comm:cellItem>
                                    </comm:cell>

                                    <!-- Designation input field -->
                                    <comm:cell xsi:type="comm:cell" comm:col_id="designation">
                                        <comm:cellItem xsi:type="comm:cellItem">
                                            <xsl:choose>
                                                <xsl:when test="../../editable='true'">
                                                    <comm:item xsi:type="comm:input" comm:name="designation">
                                                        <comm:value><xsl:value-of select="designation"/></comm:value>
                                                    </comm:item>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    <comm:item xsi:type="comm:textReadout">
                                                        <comm:value><xsl:value-of select="designation"/></comm:value>
                                                    </comm:item>
                                                </xsl:otherwise>
                                            </xsl:choose>
                                        </comm:cellItem>
                                    </comm:cell>

                                    <!--Remove (Delete) row-->
                                    <comm:cell comm:col_id="remove">
                                        <comm:cellItem>
                                            <comm:item xsi:type="comm:button"
                                                       comm:id="{concat('removeDirectorBtn_', position())}"
                                                       comm:type="paper"
                                                       comm:width="2"
                                                       comm:url="{concat('app-domain/ui/searchCompanyDetails?companyRegNumber=', /requestWrapper/request/registrationNumber, '&amp;removeDirectorAt=', position())}"
                                                       comm:target="main"
                                                       comm:formSubmit="false"
                                                       comm:label="Remove"/>
                                        </comm:cellItem>
                                    </comm:cell>
                                </comm:row>
                            </xsl:for-each>
                        </comm:symbol>
                    </comm:sections>

                    <comm:sections comm:align="left" comm:width="full">

                        <!-- Request Type Dropdown -->
                        <comm:symbol comm:id="mandateResolution"
                                     comm:label="Request type *"
                                     comm:pleaseSelectOptionEnabled="true"
                                     xsi:type="comm:dropdown"
                                     comm:errorMessage="{requestWrapper/request/errorMessage}">

                            <comm:label>Mandate</comm:label>
                            <comm:value xsi:type="comm:eventValue">
                                <comm:value>1</comm:value>
                            </comm:value>

                            <comm:label>Resolution</comm:label>
                            <comm:value xsi:type="comm:eventValue">
                                <comm:value>2</comm:value>
                            </comm:value>

                            <comm:label>Mandate and resolution</comm:label>
                            <comm:value xsi:type="comm:eventValue">
                                <comm:value>3</comm:value>
                            </comm:value>
                        </comm:symbol>

                        <!-- Mandate Details -->
                        <comm:symbol xsi:type="comm:divContainer" comm:id="mandateDetails" comm:hidden="true">
                            <comm:divElement xsi:type="comm:textParagraph" comm:subHeading="Required Documents">
                                <comm:value>Please attach the required mandate documents</comm:value>
                            </comm:divElement>

                            <comm:divElement xsi:type="comm:fileUpload"
                                             comm:name="signatoriesMandateDoc"
                                             comm:label="Replace authorised signatories"
                                             comm:fileUploadUrl="https://your-upload-endpoint"
                                             comm:fileErrorUrl="https://your-error-handler"
                                             comm:showInput="true">
                                <comm:value/>
                            </comm:divElement>

                            <comm:divElement xsi:type="comm:fileUpload"
                                             comm:name="signatureMandateCardDoc"
                                             comm:label="Signature card"
                                             comm:fileUploadUrl="https://your-upload-endpoint"
                                             comm:fileErrorUrl="https://your-error-handler"
                                             comm:showInput="true">
                                <comm:value/>
                            </comm:divElement>

                            <comm:divElement xsi:type="comm:textParagraph">
                                <comm:value>Ensure that all fonts, text and pictures of documents supplied are legible.</comm:value>
                            </comm:divElement>
                            <comm:divElement xsi:type="comm:textParagraph">
                                <comm:value>Valid formats: PDF and JPG. Upload should not exceed 10MB.</comm:value>
                            </comm:divElement>

                            <comm:divElement xsi:type="comm:input"
                                             comm:name="confirmationCheckMandate"
                                             comm:inputType="checkbox"
                                             comm:unCheckedValue="No"
                                             comm:selected="false">
                                <comm:value/>
                                <comm:inputItem comm:id="confirmationCheckMandate"
                                                comm:label="I confirm that the signatures of the provided documents align with the waiver requirements"
                                                comm:type="checkbox"
                                                comm:value="1"
                                                comm:unCheckedValue="No"
                                                comm:selected="false"/>
                            </comm:divElement>
                        </comm:symbol>

                        <!-- Resolution Details -->
                        <comm:symbol xsi:type="comm:divContainer" comm:id="resolutionDetails" comm:hidden="true">
                            <comm:divElement xsi:type="comm:textParagraph" comm:subHeading="Required Documents">
                                <comm:value>Please attach the required resolution documents</comm:value>
                            </comm:divElement>

                            <comm:divElement xsi:type="comm:fileUpload"
                                             comm:name="signatoriesResolutionDoc"
                                             comm:label="Replace authorised signatories"
                                             comm:fileUploadUrl="https://your-upload-endpoint"
                                             comm:fileErrorUrl="https://your-error-handler"
                                             comm:showInput="true">
                                <comm:value/>
                            </comm:divElement>

                            <comm:divElement xsi:type="comm:textParagraph">
                                <comm:value>Ensure that all fonts, text and pictures of documents supplied are legible.</comm:value>
                            </comm:divElement>
                            <comm:divElement xsi:type="comm:textParagraph">
                                <comm:value>Valid formats: PDF and JPG. Upload should not exceed 10MB.</comm:value>
                            </comm:divElement>

                            <comm:divElement xsi:type="comm:input"
                                             comm:name="confirmationCheckResolution"
                                             comm:inputType="checkbox"
                                             comm:unCheckedValue="No"
                                             comm:selected="false">
                                <comm:value/>
                                <comm:inputItem comm:id="confirmationCheckResolution"
                                                comm:label="I confirm that the signatures of the provided documents align with the waiver requirements"
                                                comm:type="checkbox"
                                                comm:value="1"
                                                comm:unCheckedValue="No"
                                                comm:selected="false"/>
                            </comm:divElement>
                        </comm:symbol>

                        <!-- Mandate and Resolution Details -->
                        <comm:symbol xsi:type="comm:divContainer" comm:id="mandateResolutionDetails" comm:hidden="true">
                            <comm:divElement xsi:type="comm:textParagraph" comm:subHeading="Required Documents">
                                <comm:value>Please attach the required mandate and resolution documents</comm:value>
                            </comm:divElement>

                            <comm:divElement xsi:type="comm:fileUpload"
                                             comm:name="signatoriesMandateResolutionDoc"
                                             comm:label="Replace authorised signatories"
                                             comm:fileUploadUrl="https://your-upload-endpoint"
                                             comm:fileErrorUrl="https://your-error-handler"
                                             comm:showInput="true">
                                <comm:value/>
                            </comm:divElement>

                            <comm:divElement xsi:type="comm:fileUpload"
                                             comm:name="signatureMandateResolutionCardDoc"
                                             comm:label="Signature card"
                                             comm:fileUploadUrl="https://your-upload-endpoint"
                                             comm:fileErrorUrl="https://your-error-handler"
                                             comm:showInput="true">
                                <comm:value/>
                            </comm:divElement>

                            <comm:divElement xsi:type="comm:fileUpload"
                                             comm:name="boardOfDirectorsMandateResolutionDoc"
                                             comm:label="Resolutions of board of directors document"
                                             comm:fileUploadUrl="https://your-upload-endpoint"
                                             comm:fileErrorUrl="https://your-error-handler"
                                             comm:showInput="true">
                                <comm:value/>
                            </comm:divElement>

                            <comm:divElement xsi:type="comm:textParagraph">
                                <comm:value>Ensure that all fonts, text and pictures of documents supplied are legible.</comm:value>
                            </comm:divElement>
                            <comm:divElement xsi:type="comm:textParagraph">
                                <comm:value>Valid formats: PDF and JPG. Upload should not exceed 10MB.</comm:value>
                            </comm:divElement>

                            <comm:divElement xsi:type="comm:input"
                                             comm:name="confirmationCheckMandateResolution"
                                             comm:inputType="checkbox"
                                             comm:unCheckedValue="No"
                                             comm:selected="false">
                                <comm:value/>
                                <comm:inputItem comm:id="confirmationCheckMandateResolution"
                                                comm:label="I confirm that the signatures of the provided documents align with the waiver requirements"
                                                comm:type="checkbox"
                                                comm:value="1"
                                                comm:unCheckedValue="No"
                                                comm:selected="false"/>
                            </comm:divElement>
                        </comm:symbol>

                    </comm:sections>
                </comm:form>
            </symbol>

            <!--Changes the file upload based on what dropdown value is selected-->
            <xsl:element name="symbol">
                <xsl:attribute name="xsi:type">comm:setEvent</xsl:attribute>
                <xsl:attribute name="comm:id">mandateResolution</xsl:attribute>
                <xsl:attribute name="comm:event">mousedown</xsl:attribute>
                <xsl:attribute name="comm:show">
                    {'1':'mandateDetails','2':'resolutionDetails','3':'mandateResolutionDetails'}
                </xsl:attribute>
                <xsl:attribute name="comm:hide">
                    {'1':'mandateResolutionDetails','2':'mandateDetails','3':'resolutionDetails','-1':'mandateDetails|resolutionDetails|mandateResolutionDetails'}
                </xsl:attribute>
            </xsl:element>

            <!--Footer-->
            <symbol xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="comm:footer" comm:text="" comm:textAlign="left" comm:buttonAlign="right">
                <comm:baseButton comm:id="next" comm:target="main" comm:url="app-domain/ui/nextStep" comm:label="Next" comm:formSubmit="true"/>
                <comm:baseButton comm:id="backSearch" comm:target="main" comm:url="app-domain/ui/createRequest" comm:label="Back" comm:formSubmit="true"/>
            </symbol>

        </page>
    </xsl:template>
</xsl:stylesheet>