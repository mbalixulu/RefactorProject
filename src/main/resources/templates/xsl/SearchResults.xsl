<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml"/>

    <!-- 1) Primary: numeric "1|2|3" from the wrapper -->
    <xsl:variable name="SEL_NUM" select="normalize-space(/requestWrapper/request/mandateResolution)"/>

    <!-- 2) Fallback: textual type if ever present in a wrapper (safe to keep) -->
    <xsl:variable name="TYPE_RAW"
                  select="normalize-space((/requestWrapper/request/type
                                       | /requestWrapper/request/requestType)[1])"/>
    <xsl:variable name="TYPE_UP"
                  select="translate($TYPE_RAW,'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ')"/>

    <!-- Map textual MANDATE/RESOLUTION/BOTH -> 1/2/3 -->
    <xsl:variable name="SEL_FROM_TYPE">
        <xsl:choose>
            <xsl:when test="contains($TYPE_UP,'BOTH')">3</xsl:when>
            <xsl:when test="contains($TYPE_UP,'RESOLUTION')">2</xsl:when>
            <xsl:when test="contains($TYPE_UP,'MANDATE')">1</xsl:when>
            <xsl:otherwise/>
        </xsl:choose>
    </xsl:variable>

    <!-- Final selection used by dropdown and visibility -->
    <xsl:variable name="SEL_EFFECTIVE">
        <xsl:choose>
            <xsl:when test="string-length($SEL_NUM)"><xsl:value-of select="$SEL_NUM"/></xsl:when>
            <xsl:otherwise><xsl:value-of select="$SEL_FROM_TYPE"/></xsl:otherwise>
        </xsl:choose>
    </xsl:variable>
    <xsl:variable name="SEL" select="normalize-space($SEL_EFFECTIVE)"/>

    <!-- Booleans for show/hide -->
    <xsl:variable name="IS_MANDATE"    select="$SEL='1' or $SEL='3'"/>
    <xsl:variable name="IS_RESOLUTION" select="$SEL='2' or $SEL='3'"/>

    <xsl:template match="/requestWrapper">
        <page xmlns:comm="http://ws.online.fnb.co.za/v1/common/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" id="boxConatinerID" title="Box Container" template="main" layout="" version="1">
            <!--Page Heading-->
            <symbol xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="comm:subTabGroup" comm:subTabGroupHeading="Mandates and resolutions"/>

            <!--Box Symbols-->
            <symbol xsi:type="comm:formLayout">
                <comm:form comm:name="searchResults" comm:action="app-domain/mandates-and-resolutions/nextStep">
                    <comm:sections comm:width="full">

                        <!--Mirror current tools into hidden inputs so they post on 'Add' -->
                        <xsl:for-each select="/requestWrapper/request/documentumTool">
                            <comm:symbol xsi:type="comm:input"
                                         comm:name="{concat('documentumTools[', position()-1, ']')}"
                                         comm:inputType="hidden">
                                <comm:value><xsl:value-of select="."/></comm:value>
                            </comm:symbol>
                        </xsl:for-each>

                        <!--Current directors into hidden inputs so they post on 'Add' -->
                        <xsl:for-each select="/requestWrapper/request/directors/director">
                            <xsl:variable name="idx" select="position()-1"/>
                            <comm:symbol xsi:type="comm:input"
                                         comm:name="{concat('directors[', $idx, '].name')}"
                                         comm:inputType="hidden">
                                <comm:value><xsl:value-of select="normalize-space(name)"/></comm:value>
                            </comm:symbol>
                            <comm:symbol xsi:type="comm:input"
                                         comm:name="{concat('directors[', $idx, '].surname')}"
                                         comm:inputType="hidden">
                                <comm:value><xsl:value-of select="normalize-space(surname)"/></comm:value>
                            </comm:symbol>
                            <comm:symbol xsi:type="comm:input"
                                         comm:name="{concat('directors[', $idx, '].designation')}"
                                         comm:inputType="hidden">
                                <comm:value><xsl:value-of select="normalize-space(designation)"/></comm:value>
                            </comm:symbol>
                        </xsl:for-each>

                        <!-- Keep session id so POST carries it -->
                        <comm:symbol xsi:type="comm:input" comm:name="pdfSessionId" comm:inputType="hidden">
                            <comm:value><xsl:value-of select="/requestWrapper/request/pdfSessionId"/></comm:value>
                        </comm:symbol>

                        <!-- Carry the draft id -->
                        <comm:symbol xsi:type="comm:input" comm:name="stagingId" comm:inputType="hidden">
                            <comm:value><xsl:value-of select="/requestWrapper/request/stagingId"/></comm:value>
                        </comm:symbol>

                        <!-- Which page is saving -->
                        <comm:symbol xsi:type="comm:input" comm:name="pageCode" comm:inputType="hidden">
                            <comm:value>SEARCH_RESULTS</comm:value>
                        </comm:symbol>

                        <comm:symbol xsi:type="comm:boxContainer">
                            <comm:box xsi:type="comm:box">
                                <comm:boxSymbol xsi:type="comm:textHeading" comm:size="4">
                                    <comm:value>Company details</comm:value>
                                </comm:boxSymbol>

                                <!-- Company Name -->
                                <comm:boxSymbol xsi:type="comm:boxSplit" comm:width="33">
                                    <comm:boxSplitSymbol xsi:type="comm:input"
                                                         comm:name="companyName"
                                                         comm:label="Company name">
                                        <comm:value>
                                            <xsl:value-of select="/requestWrapper/request/companyName"/>
                                        </comm:value>
                                    </comm:boxSplitSymbol>
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
                        <comm:symbol xsi:type="comm:boxContainer" comm:id="waiverBox">
                            <comm:box xsi:type="comm:box">
                                <comm:boxSymbol xsi:type="comm:textHeading" comm:size="4">
                                    <comm:value>Company waiver</comm:value>
                                </comm:boxSymbol>

                                <!-- Add Waiver Tool button-->
                                <comm:boxSymbol
                                        xsi:type="comm:button"
                                        comm:id="addToolBtn"
                                        comm:label="Add Waiver Tool"
                                        comm:target="main"
                                        comm:type="primary"
                                        comm:formSubmit="true"
                                        comm:width="3"
                                        comm:url="{concat(
                                                      'app-domain/mandates-and-resolutions/searchCompanyDetails?companyRegNumber=',
                                                      /requestWrapper/request/registrationNumber,
                                                      '&amp;pdfSessionId=', /requestWrapper/request/pdfSessionId,
                                                      '&amp;toolCount=',
                                                      (count(/requestWrapper/request/documentumTool) &gt; 0)
                                                        * (count(/requestWrapper/request/documentumTool) + 1)
                                                        + (count(/requestWrapper/request/documentumTool) = 0) * 2,
                                                      '&amp;directorCount=', count(/requestWrapper/request/directors/director),
                                                      '&amp;action=addTool#waiverBox'
                                                    )}">
                            </comm:boxSymbol>

                                <!-- Existing tools (labelled Tool 1, Tool 2, ...) -->
                                <xsl:for-each select="/requestWrapper/request/documentumTool">
                                    <comm:boxSymbol xsi:type="comm:boxSplit">
                                        <xsl:choose>
                                            <xsl:when test="/requestWrapper/request/editable='true'">
                                                <comm:boxSplitSymbol xsi:type="comm:input"
                                                                     comm:name="{concat('documentumTools[', position()-1, ']')}"
                                                                     comm:label="{concat('Tool ', position())}">
                                                    <comm:value><xsl:value-of select="."/></comm:value>
                                                </comm:boxSplitSymbol>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <comm:boxSplitSymbol xsi:type="comm:textReadout"
                                                                     comm:subHeading="{concat('Tool ', position())}"
                                                                     comm:color="ghostmedium">
                                                    <comm:value><xsl:value-of select="."/></comm:value>
                                                </comm:boxSplitSymbol>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </comm:boxSymbol>
                                </xsl:for-each>

                                <!-- Default: if no tools exist yet, show one editable input as "Tool 1" -->
                                <xsl:if test="count(/requestWrapper/request/documentumTool)=0">
                                    <comm:boxSymbol xsi:type="comm:boxSplit">
                                        <xsl:choose>
                                            <xsl:when test="/requestWrapper/request/editable='true'">
                                                <comm:boxSplitSymbol xsi:type="comm:input"
                                                                     comm:name="documentumTools[0]"
                                                                     comm:label="Tool 1">
                                                    <comm:value/>
                                                </comm:boxSplitSymbol>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <comm:boxSplitSymbol xsi:type="comm:textReadout"
                                                                     comm:subHeading="Tool 1"
                                                                     comm:color="ghostmedium">
                                                    <comm:value>No tools available</comm:value>
                                                </comm:boxSplitSymbol>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </comm:boxSymbol>
                                </xsl:if>

                            </comm:box>
                        </comm:symbol>

                        <!-- Directors Table -->
                        <comm:symbol xsi:type="comm:fullTable"
                                     comm:id="directorsTable"
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

                            <!-- Directors Group -->
                            <comm:rowGroup xsi:type="comm:rowGroup"
                                           comm:groupId="directors"
                                           comm:groupHeaderLabel="Directors List">

                                <!-- Add Director button -->
                                <comm:groupTableButton
                                        xsi:type="comm:imageButton"
                                        comm:id="addDirectorBtn"
                                        comm:label="Add a director"
                                        comm:tip="Click to add a new director"
                                        comm:target="main"
                                        comm:url="{concat(
                                                          'app-domain/mandates-and-resolutions/searchCompanyDetails?',
                                                          'pdfSessionId=', /requestWrapper/request/pdfSessionId,
                                                          '&amp;companyRegNumber=', /requestWrapper/request/registrationNumber,
                                                          '&amp;directorCount=', count(/requestWrapper/request/directors/director) + 1,
                                                          '&amp;toolCount=', count(/requestWrapper/request/documentumTool),
                                                          '#directorsTable'
                                                        )}"/>
                            </comm:rowGroup>

                            <!-- Render each director as editable row -->
                            <xsl:for-each select="/requestWrapper/request/directors/director">
                                <comm:row xsi:type="comm:fullTableRow" comm:groupId="directors">

                                    <!-- Name input field -->
                                    <comm:cell xsi:type="comm:cell" comm:col_id="name">
                                        <comm:cellItem xsi:type="comm:cellItem">
                                            <xsl:choose>
                                                <xsl:when test="../../editable='true'">
                                                    <comm:item xsi:type="comm:input" comm:name="{concat('directors[', position()-1, '].name')}">
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
                                                    <comm:item xsi:type="comm:input" comm:name="{concat('directors[', position()-1, '].surname')}">
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
                                                    <comm:item xsi:type="comm:input" comm:name="{concat('directors[', position()-1, '].designation')}">
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

                                    <!-- Remove (Delete) row -->
                                    <comm:cell comm:col_id="remove">
                                        <comm:cellItem>
                                            <comm:item
                                                    xsi:type="comm:button"
                                                    comm:id="{concat('removeDirectorBtn_', position())}"
                                                    comm:type="paper"
                                                    comm:width="2"
                                                    comm:target="main"
                                                    comm:formSubmit="true"
                                                    comm:label="Remove"
                                                    comm:url="{concat(
                                                                      'app-domain/mandates-and-resolutions/searchCompanyDetails?',
                                                                      'pdfSessionId=', /requestWrapper/request/pdfSessionId,
                                                                      '&amp;companyRegNumber=', /requestWrapper/request/registrationNumber,
                                                                      '&amp;removeDirectorAt=', position(),
                                                                      '&amp;directorCount=', count(/requestWrapper/request/directors/director),
                                                                      '&amp;toolCount=', count(/requestWrapper/request/documentumTool),
                                                                      '#directorsTable'
                                                                    )}"/>
                                        </comm:cellItem>
                                    </comm:cell>
                                </comm:row>
                            </xsl:for-each>
                        </comm:symbol>
                    </comm:sections>

                    <comm:sections comm:align="left" comm:width="full">

                        <!-- Dropdown bound to $SEL -->
                        <comm:symbol
                                xsi:type="comm:dropdown"
                                comm:id="mandateResolution"
                                comm:label="Request type *"
                                comm:selectedValue="{$SEL}"
                                comm:pleaseSelectOptionEnabled="{not(string-length($SEL))}"
                                comm:errorMessage="{/requestWrapper/request/errorMessage}">

                            <comm:label>Mandate</comm:label>
                            <comm:value xsi:type="comm:eventValue"><comm:value>1</comm:value></comm:value>

                            <comm:label>Resolution</comm:label>
                            <comm:value xsi:type="comm:eventValue"><comm:value>2</comm:value></comm:value>

                            <comm:label>Mandate and resolution</comm:label>
                            <comm:value xsi:type="comm:eventValue"><comm:value>3</comm:value></comm:value>
                        </comm:symbol>

                        <comm:symbol xsi:type="comm:input" comm:name="registrationNumber" comm:inputType="hidden">
                            <comm:value>
                                <xsl:value-of select="/requestWrapper/request/registrationNumber"/>
                            </comm:value>
                        </comm:symbol>

                        <!-- Mandate Details -->
                        <comm:symbol xsi:type="comm:divContainer"
                                     comm:id="mandateDetails"
                                     comm:hidden="{not($IS_MANDATE)}">
                            <comm:divElement xsi:type="comm:textParagraph" comm:subHeading="Required Documents">
                                <comm:value>Please attach the required mandate documents</comm:value>
                            </comm:divElement>

                            <!-- Mandate file upload (AUTO-POSTS on select) -->
                            <comm:divElement
                                    xsi:type="comm:fileUpload"
                                    comm:name="file"
                                    comm:label="Replace authorised signatories"
                                    comm:fileUploadUrl="app-domain/mandates-and-resolutions/mandates/attachment/upload"
                                    comm:fileErrorUrl="app-domain/mandates-and-resolutions/errorPage"
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

                            <comm:divElement xsi:type="comm:divContainer" comm:id="resolutionUploadContainer">
                                <xsl:for-each select="/requestWrapper/request/resolutionDocs/resolutionDoc">
                                    <comm:divElement
                                            xsi:type="comm:fileUpload"
                                            comm:name="{concat('requiredResolutionDoc_', position())}"
                                            comm:label="Required Document"
                                            comm:fileUploadUrl="https://your-upload-endpoint"
                                            comm:fileErrorUrl="https://your-error-endpoint"
                                            comm:showInput="true">
                                        <comm:value>
                                            <xsl:value-of select="."/>
                                        </comm:value>
                                    </comm:divElement>
                                </xsl:for-each>
                            </comm:divElement>

                            <comm:divElement
                                    xsi:type="comm:button"
                                    comm:id="mandatesProceedBtn"
                                    comm:target="main"
                                    comm:url="app-domain/mandates-and-resolutions/proceedPdfExtraction"
                                    comm:label="Upload Document"
                                    comm:width="3"
                                    comm:formSubmit="true"
                                    comm:type="primary"/>

                            <comm:divElement
                                    xsi:type="comm:button"
                                    comm:id="addRequiredResolutionDoc"
                                    comm:target="main"
                                    comm:url="{concat('app-domain/mandates-and-resolutions/searchCompanyDetails?companyRegNumber=', /requestWrapper/request/registrationNumber, '&amp;resolutionDocCount=', count(/requestWrapper/request/resolutionDocs/resolutionDoc) + 1)}"
                                    comm:label="Add Required Document"
                                    comm:width="3"
                                    comm:formSubmit="false"
                                    comm:type="primary"/>

                            <comm:divElement xsi:type="comm:textParagraph">
                                <comm:value>Files will be uploaded automatically when selected. Ensure that all fonts, text and pictures of documents supplied are legible.</comm:value>
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
                        <comm:symbol xsi:type="comm:divContainer"
                                     comm:id="resolutionDetails"
                                     comm:hidden="{not($IS_RESOLUTION)}">
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

                            <comm:divElement xsi:type="comm:divContainer" comm:id="resolutionUploadContainer">
                                <xsl:for-each select="/requestWrapper/request/resolutionDocs/resolutionDoc">
                                    <comm:divElement
                                            xsi:type="comm:fileUpload"
                                            comm:name="{concat('requiredResolutionDoc_', position())}"
                                            comm:label="Required Document"
                                            comm:fileUploadUrl="https://your-upload-endpoint"
                                            comm:fileErrorUrl="https://your-error-endpoint"
                                            comm:showInput="true">
                                        <comm:value>
                                            <xsl:value-of select="."/>
                                        </comm:value>
                                    </comm:divElement>
                                </xsl:for-each>
                            </comm:divElement>

                            <comm:divElement
                                    xsi:type="comm:button"
                                    comm:id="addRequiredResolutionDoc"
                                    comm:target="main"
                                    comm:url="{concat('app-domain/mandates-and-resolutions/searchCompanyDetails?companyRegNumber=', /requestWrapper/request/registrationNumber, '&amp;resolutionDocCount=', count(/requestWrapper/request/resolutionDocs/resolutionDoc) + 1)}"
                                    comm:label="Add Required Document"
                                    comm:width="3"
                                    comm:formSubmit="false"
                                    comm:type="primary"/>

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
                        <comm:symbol xsi:type="comm:divContainer"
                                     comm:id="mandateResolutionDetails"
                                     comm:hidden="{not($IS_MANDATE and $IS_RESOLUTION)}">
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

                            <comm:divElement xsi:type="comm:divContainer" comm:id="resolutionUploadContainer">
                                <xsl:for-each select="/requestWrapper/request/resolutionDocs/resolutionDoc">
                                    <comm:divElement
                                            xsi:type="comm:fileUpload"
                                            comm:name="{concat('requiredResolutionDoc_', position())}"
                                            comm:label="Required Document"
                                            comm:fileUploadUrl="https://your-upload-endpoint"
                                            comm:fileErrorUrl="https://your-error-endpoint"
                                            comm:showInput="true">
                                        <comm:value>
                                            <xsl:value-of select="."/>
                                        </comm:value>
                                    </comm:divElement>
                                </xsl:for-each>
                            </comm:divElement>

                            <comm:divElement
                                    xsi:type="comm:button"
                                    comm:id="addRequiredManResDoc"
                                    comm:target="main"
                                    comm:url="{concat('app-domain/mandates-and-resolutions/searchCompanyDetails?companyRegNumber=', /requestWrapper/request/registrationNumber, '&amp;resolutionDocCount=', count(/requestWrapper/request/resolutionDocs/resolutionDoc) + 1)}"
                                    comm:label="Add Required Document"
                                    comm:width="3"
                                    comm:formSubmit="false"
                                    comm:type="primary"/>

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

            <symbol xsi:type="comm:analytics" comm:eventName="DocUpload">
                <comm:keyValuePair comm:key="eventName" comm:value="DocUpload"/>
            </symbol>

            <!-- setEvent: use allowed events (no 'load') and avoid AVT by using xsl:attribute -->
            <xsl:element name="symbol">
                <xsl:attribute name="xsi:type">comm:setEvent</xsl:attribute>
                <xsl:attribute name="comm:id">mandateResolution</xsl:attribute>
                <xsl:attribute name="comm:event">change</xsl:attribute>
                <xsl:attribute name="comm:show">{'1':'mandateDetails','2':'resolutionDetails','3':'mandateResolutionDetails'}</xsl:attribute>
                <xsl:attribute name="comm:hide">{'1':'mandateResolutionDetails','2':'mandateDetails','3':'resolutionDetails','-1':'mandateDetails|resolutionDetails|mandateResolutionDetails'}</xsl:attribute>
            </xsl:element>

            <xsl:element name="symbol">
                <xsl:attribute name="xsi:type">comm:setEvent</xsl:attribute>
                <xsl:attribute name="comm:id">mandateResolution</xsl:attribute>
                <xsl:attribute name="comm:event">mousedown</xsl:attribute>
                <xsl:attribute name="comm:show">{'1':'mandateDetails','2':'resolutionDetails','3':'mandateResolutionDetails'}</xsl:attribute>
                <xsl:attribute name="comm:hide">{'1':'mandateResolutionDetails','2':'mandateDetails','3':'resolutionDetails','-1':'mandateDetails|resolutionDetails|mandateResolutionDetails'}</xsl:attribute>
            </xsl:element>

            <!--Footer-->
            <symbol xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="comm:footer" comm:text="" comm:textAlign="left" comm:buttonAlign="right">
                <comm:baseButton comm:id="proceed" comm:target="main" comm:url="app-domain/mandates-and-resolutions/nextStep" comm:label="Proceed" comm:formSubmit="true"/>
                <comm:baseButton comm:id="save"
                                 comm:target="main"
                                 comm:url="app-domain/mandates-and-resolutions/draft/save"
                                 comm:label="Save"
                                 comm:formSubmit="true"/>
                <comm:baseButton comm:id="backSearch" comm:target="main" comm:url="app-domain/mandates-and-resolutions/createRequest" comm:label="Back" comm:formSubmit="true"/>
            </symbol>
        </page>
    </xsl:template>
</xsl:stylesheet>