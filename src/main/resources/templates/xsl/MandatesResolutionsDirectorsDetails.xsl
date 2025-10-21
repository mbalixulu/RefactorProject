<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:ns1="http://ws.online.fnb.co.za/v1/common/"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <xsl:output method="xml" indent="yes"/>

    <xsl:template match="/requestWrapper">
        <xsl:variable name="directorCount" select="count(/requestWrapper/request/directors/director)"/>
        <xsl:variable name="directorCountStr" select="string($directorCount)"/>
        <xsl:variable name="directorCountPlusOneStr" select="string($directorCount + 1)"/>

        <page xmlns:ns1="http://ws.online.fnb.co.za/v1/common/"
              id="mandatesResolutionAutoFillForm"
              title="Mandates and Resolutions Directors Details"
              template="main"
              version="1">

            <symbol xsi:type="ns1:subTabGroup" ns1:subTabGroupHeading="Resolutions"/>

            <symbol xsi:type="ns1:formLayout">
                <ns1:form ns1:action="app-domain/mandates-and-resolutions" ns1:name="salesForm">

                    <ns1:sections ns1:align="left" ns1:width="full">
                        <ns1:symbol xsi:type="ns1:textHeading" ns1:size="5">
                            <ns1:value>Directors Details</ns1:value>
                        </ns1:symbol>
                    </ns1:sections>

                    <!-- Hidden context (IDs + page code + top-level + waiver tools) -->
                    <ns1:sections ns1:align="left" ns1:width="full">
                        <ns1:symbol xsi:type="ns1:input" ns1:name="pdfSessionId" ns1:inputType="hidden">
                            <ns1:value><xsl:value-of select="/requestWrapper/request/pdfSessionId"/></ns1:value>
                        </ns1:symbol>
                        <ns1:symbol xsi:type="ns1:input" ns1:name="stagingId" ns1:inputType="hidden">
                            <ns1:value><xsl:value-of select="/requestWrapper/request/stagingId"/></ns1:value>
                        </ns1:symbol>
                        <ns1:symbol xsi:type="ns1:input" ns1:name="pageCode" ns1:inputType="hidden">
                            <ns1:value>DIRECTORS_DETAILS</ns1:value>
                        </ns1:symbol>

                        <ns1:symbol xsi:type="ns1:input" ns1:name="mandateResolution" ns1:inputType="hidden">
                            <ns1:value><xsl:value-of select="/requestWrapper/request/mandateResolution"/></ns1:value>
                        </ns1:symbol>
                        <ns1:symbol xsi:type="ns1:input" ns1:name="companyName" ns1:inputType="hidden">
                            <ns1:value><xsl:value-of select="/requestWrapper/request/companyName"/></ns1:value>
                        </ns1:symbol>
                        <ns1:symbol xsi:type="ns1:input" ns1:name="companyAddress" ns1:inputType="hidden">
                            <ns1:value><xsl:value-of select="/requestWrapper/request/companyAddress"/></ns1:value>
                        </ns1:symbol>
                        <ns1:symbol xsi:type="ns1:input" ns1:name="registrationNumber" ns1:inputType="hidden">
                            <ns1:value><xsl:value-of select="/requestWrapper/request/registrationNumber"/></ns1:value>
                        </ns1:symbol>

                        <!-- Waiver tools -->
                        <xsl:for-each select="/requestWrapper/request/documentumTools/* | /requestWrapper/request/documentumTool">
                            <ns1:symbol xsi:type="ns1:input"
                                        ns1:name="{concat('documentumTools[', position()-1, ']')}"
                                        ns1:inputType="hidden">
                                <ns1:value><xsl:value-of select="."/></ns1:value>
                            </ns1:symbol>
                        </xsl:for-each>
                    </ns1:sections>

                    <!-- Directors table -->
                    <ns1:sections ns1:align="left" ns1:width="full">
                        <ns1:symbol xsi:type="ns1:fullTable"
                                    ns1:id="DirectorsTable"
                                    ns1:heading="Add appointed directors"
                                    ns1:showTotal="false"
                                    ns1:headingColor="black">

                            <ns1:tableColumn ns1:align="left" ns1:fieldName="name"        ns1:heading="Name"        ns1:disableSorting="true"/>
                            <ns1:tableColumn ns1:align="left" ns1:fieldName="surname"     ns1:heading="Surname"     ns1:disableSorting="true"/>
                            <ns1:tableColumn ns1:align="left" ns1:fieldName="designation" ns1:heading="Designation" ns1:disableSorting="true"/>
                            <!-- NEW: Instruction (dropdown) -->
                            <ns1:tableColumn ns1:align="left" ns1:fieldName="instruction" ns1:heading="Instruction" ns1:disableSorting="true"/>
                            <ns1:tableColumn ns1:align="left" ns1:fieldName="remove"      ns1:heading="Remove"      ns1:disableSorting="true"/>

                            <ns1:rowGroup xsi:type="ns1:rowGroup" ns1:groupId="directors" ns1:groupHeaderLabel="">
                                <ns1:groupTableButton xsi:type="ns1:imageButton"
                                                      ns1:id="addDirectorBtn"
                                                      ns1:label="Add a director"
                                                      ns1:tip="Add a director"
                                                      ns1:url="{concat(
                                        'app-domain/mandates-and-resolutions/mandatesResolutionsDirectorsDetails?directorCount=',
                                        $directorCountPlusOneStr,
                                        '&amp;pdfSessionId=',
                                        /requestWrapper/request/pdfSessionId
                                      )}"/>
                            </ns1:rowGroup>

                            <xsl:for-each select="/requestWrapper/request/directors/director">
                                <xsl:variable name="pos" select="position()"/>
                                <ns1:row xsi:type="ns1:fullTableRow" ns1:groupId="directors">

                                    <ns1:cell xsi:type="ns1:cell" ns1:col_id="name">
                                        <ns1:cellItem xsi:type="ns1:cellItem">
                                            <ns1:item xsi:type="ns1:input"
                                                      ns1:name="{concat('directorName_', $pos)}"
                                                      ns1:required="true"
                                                      ns1:errorMessage="{/requestWrapper/resolutionsAutoFillErrorModel/directorName}">
                                                <ns1:value><xsl:value-of select="normalize-space(name)"/></ns1:value>
                                            </ns1:item>
                                        </ns1:cellItem>
                                    </ns1:cell>

                                    <ns1:cell xsi:type="ns1:cell" ns1:col_id="surname">
                                        <ns1:cellItem xsi:type="ns1:cellItem">
                                            <ns1:item xsi:type="ns1:input"
                                                      ns1:name="{concat('directorSurname_', $pos)}"
                                                      ns1:required="true"
                                                      ns1:errorMessage="{/requestWrapper/resolutionsAutoFillErrorModel/directorSurname}">
                                                <ns1:value><xsl:value-of select="normalize-space(surname)"/></ns1:value>
                                            </ns1:item>
                                        </ns1:cellItem>
                                    </ns1:cell>

                                    <ns1:cell xsi:type="ns1:cell" ns1:col_id="designation">
                                        <ns1:cellItem xsi:type="ns1:cellItem">
                                            <ns1:item xsi:type="ns1:input"
                                                      ns1:name="{concat('directorDesignation_', $pos)}"
                                                      ns1:required="true"
                                                      ns1:errorMessage="{/requestWrapper/resolutionsAutoFillErrorModel/directorDesignation}">
                                                <ns1:value><xsl:value-of select="normalize-space(designation)"/></ns1:value>
                                            </ns1:item>
                                        </ns1:cellItem>
                                    </ns1:cell>

                                    <!-- NEW: Instruction dropdown -->
                                    <ns1:cell xsi:type="ns1:cell" ns1:col_id="instruction">
                                        <ns1:cellItem xsi:type="ns1:cellItem">
                                            <xsl:variable name="cur" select="normalize-space(instruction)"/>
                                            <xsl:choose>
                                                <!-- Dynamic LOV if present -->
                                                <xsl:when test="count(/requestWrapper/lovs/instructions/*) &gt; 0">
                                                    <ns1:item xsi:type="ns1:dropdown"
                                                              ns1:id="{concat('directorInstruction_', $pos)}"
                                                              ns1:label="Instruction"
                                                              ns1:required="true"
                                                              ns1:errorMessage="{/requestWrapper/resolutionsAutoFillErrorModel/directorInstruction}">
                                                        <xsl:if test="string-length($cur) &gt; 0">
                                                            <ns1:label><xsl:value-of select="$cur"/></ns1:label>
                                                            <ns1:value xsi:type="ns1:eventValue"><ns1:value><xsl:value-of select="$cur"/></ns1:value></ns1:value>
                                                        </xsl:if>
                                                        <xsl:for-each select="/requestWrapper/lovs/instructions/*[normalize-space(.) != $cur]">
                                                            <ns1:label><xsl:value-of select="normalize-space(.)"/></ns1:label>
                                                            <ns1:value xsi:type="ns1:eventValue"><ns1:value><xsl:value-of select="normalize-space(.)"/></ns1:value></ns1:value>
                                                        </xsl:for-each>
                                                    </ns1:item>
                                                </xsl:when>
                                                <!-- Fallback Add/Remove -->
                                                <xsl:otherwise>
                                                    <ns1:item xsi:type="ns1:dropdown"
                                                              ns1:id="{concat('directorInstruction_', $pos)}"
                                                              ns1:label="Instruction"
                                                              ns1:required="true"
                                                              ns1:errorMessage="{/requestWrapper/resolutionsAutoFillErrorModel/directorInstruction}">
                                                        <xsl:choose>
                                                            <xsl:when test="translate($cur,'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ')='REMOVE'">
                                                                <ns1:label>Remove</ns1:label>
                                                                <ns1:value xsi:type="ns1:eventValue"><ns1:value>Remove</ns1:value></ns1:value>
                                                                <ns1:label>Add</ns1:label>
                                                                <ns1:value xsi:type="ns1:eventValue"><ns1:value>Add</ns1:value></ns1:value>
                                                            </xsl:when>
                                                            <xsl:otherwise>
                                                                <ns1:label>Add</ns1:label>
                                                                <ns1:value xsi:type="ns1:eventValue"><ns1:value>Add</ns1:value></ns1:value>
                                                                <ns1:label>Remove</ns1:label>
                                                                <ns1:value xsi:type="ns1:eventValue"><ns1:value>Remove</ns1:value></ns1:value>
                                                            </xsl:otherwise>
                                                        </xsl:choose>
                                                    </ns1:item>
                                                </xsl:otherwise>
                                            </xsl:choose>
                                        </ns1:cellItem>
                                    </ns1:cell>

                                    <ns1:cell xsi:type="ns1:cell" ns1:col_id="remove">
                                        <ns1:cellItem xsi:type="ns1:cellItem">
                                            <ns1:item xsi:type="ns1:button"
                                                      ns1:id="{concat('removeDirectorBtn_', $pos)}"
                                                      ns1:type="paper"
                                                      ns1:label="Remove"
                                                      ns1:formSubmit="false"
                                                      ns1:target="main"
                                                      ns1:width="2"
                                                      ns1:url="{concat(
                                  'app-domain/mandates-and-resolutions/mandatesResolutionsDirectorsDetails?directorCount=',
                                  $directorCountStr,
                                  '&amp;removeDirectorAt=', $pos,
                                  '&amp;pdfSessionId=',
                                  /requestWrapper/request/pdfSessionId
                                )}"/>
                                        </ns1:cellItem>
                                    </ns1:cell>
                                </ns1:row>
                            </xsl:for-each>
                        </ns1:symbol>
                    </ns1:sections>
                </ns1:form>
            </symbol>

            <!-- Footer -->
            <symbol xsi:type="ns1:footer" ns1:buttonAlign="right">
                <ns1:baseButton ns1:id="backBtn"
                                ns1:url="{concat('app-domain/mandates-and-resolutions/mandatesResolutionsSignatureCard?pdfSessionId=', /requestWrapper/request/pdfSessionId)}"
                                ns1:label="Back" ns1:formSubmit="true" ns1:target="main"/>
                <ns1:baseButton ns1:id="save"
                                ns1:url="app-domain/mandates-and-resolutions/draft/save"
                                ns1:label="Save"
                                ns1:formSubmit="true" ns1:target="main"/>
                <ns1:baseButton ns1:id="submitBtn"
                                ns1:url="app-domain/mandates-and-resolutions/mandatesResolutionsSubmit"
                                ns1:label="Submit" ns1:formSubmit="true" ns1:target="main"/>
            </symbol>
        </page>
    </xsl:template>
</xsl:stylesheet>