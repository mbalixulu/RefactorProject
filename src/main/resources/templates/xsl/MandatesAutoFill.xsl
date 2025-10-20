<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:ns1="http://ws.online.fnb.co.za/v1/common/"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">

    <xsl:output method="xml" indent="yes"/>

    <xsl:template match="/requestWrapper">
        <page xmlns:ns1="http://ws.online.fnb.co.za/v1/common/"
              xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
              id="mandatesAutoFillForm"
              title="Mandates Auto Fill Form"
              template="main"
              version="1">

            <!-- Header -->
            <symbol xsi:type="ns1:subTabGroup" ns1:subTabGroupHeading="Mandates"/>

            <!-- Form -->
            <symbol xsi:type="ns1:formLayout">
                <ns1:form ns1:action="app-domain/mandates-and-resolutions/mandatesFill" ns1:name="salesForm">

                    <!-- Heading -->
                    <ns1:sections ns1:align="left" ns1:width="full">
                        <ns1:symbol xsi:type="ns1:textHeading">
                            <ns1:value>Account Details</ns1:value>
                        </ns1:symbol>
                    </ns1:sections>

                    <!-- Keep ids + page + top-level + tools + directors -->
                    <ns1:sections ns1:align="left" ns1:width="full">
                        <ns1:symbol xsi:type="ns1:input" ns1:name="pdfSessionId" ns1:inputType="hidden">
                            <ns1:value><xsl:value-of select="/requestWrapper/request/pdfSessionId"/></ns1:value>
                        </ns1:symbol>
                        <ns1:symbol xsi:type="ns1:input" ns1:name="stagingId" ns1:inputType="hidden">
                            <ns1:value><xsl:value-of select="/requestWrapper/request/stagingId"/></ns1:value>
                        </ns1:symbol>
                        <ns1:symbol xsi:type="ns1:input" ns1:name="pageCode" ns1:inputType="hidden">
                            <ns1:value>MANDATES_AUTOFILL</ns1:value>
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

                        <!-- waiver tools (supports both <documentumTools><documentumTool/></documentumTools> and flat) -->
                        <xsl:for-each select="/requestWrapper/request/documentumTools/documentumTool | /requestWrapper/request/documentumTool">
                            <ns1:symbol xsi:type="ns1:input"
                                        ns1:name="{concat('documentumTools[', position()-1, ']')}"
                                        ns1:inputType="hidden">
                                <ns1:value><xsl:value-of select="."/></ns1:value>
                            </ns1:symbol>
                        </xsl:for-each>

                        <!-- directors (array-style to match parseDirectorsFromParamsGeneric) -->
                        <xsl:for-each select="/requestWrapper/request/directors/director">
                            <xsl:variable name="i" select="position()-1"/>
                            <ns1:symbol xsi:type="ns1:input"
                                        ns1:name="{concat('directors[', $i, '].name')}"
                                        ns1:inputType="hidden">
                                <ns1:value><xsl:value-of select="normalize-space(name)"/></ns1:value>
                            </ns1:symbol>
                            <ns1:symbol xsi:type="ns1:input"
                                        ns1:name="{concat('directors[', $i, '].surname')}"
                                        ns1:inputType="hidden">
                                <ns1:value><xsl:value-of select="normalize-space(surname)"/></ns1:value>
                            </ns1:symbol>
                            <ns1:symbol xsi:type="ns1:input"
                                        ns1:name="{concat('directors[', $i, '].designation')}"
                                        ns1:inputType="hidden">
                                <ns1:value><xsl:value-of select="normalize-space(designation)"/></ns1:value>
                            </ns1:symbol>
                        </xsl:for-each>
                    </ns1:sections>

                    <!-- Accounts -->
                    <ns1:sections ns1:align="left" ns1:width="full">
                        <xsl:for-each select="/requestWrapper/request/accounts/account">
                            <xsl:variable name="pos" select="position()"/>

                            <ns1:symbol xsi:type="ns1:boxContainer" ns1:id="{concat('box_', $pos)}">
                                <ns1:box xsi:type="ns1:box">

                                    <ns1:boxSymbol xsi:type="ns1:textHeading" ns1:size="4">
                                        <ns1:value>
                                            <xsl:text>Account </xsl:text><xsl:value-of select="$pos"/>
                                        </ns1:value>
                                    </ns1:boxSymbol>

                                    <ns1:boxSymbol xsi:type="ns1:boxSplit" ns1:width="50">
                                        <ns1:boxSplitSymbol xsi:type="ns1:input"
                                                            ns1:name="{concat('accountName_', $pos)}"
                                                            ns1:label="Account Name"
                                                            ns1:inputType="text"
                                                            ns1:required="true"
                                                            ns1:errorMessage="{/requestWrapper/mandatesAutoFillErrorModel/accountName}">
                                            <ns1:value><xsl:value-of select="normalize-space(accountName)"/></ns1:value>
                                        </ns1:boxSplitSymbol>
                                    </ns1:boxSymbol>

                                    <ns1:boxSymbol xsi:type="ns1:boxSplit" ns1:width="50">
                                        <ns1:boxSplitSymbol xsi:type="ns1:input"
                                                            ns1:name="{concat('accountNo_', $pos)}"
                                                            ns1:label="Account no."
                                                            ns1:inputType="text"
                                                            ns1:required="true"
                                                            ns1:errorMessage="{/requestWrapper/mandatesAutoFillErrorModel/accountNo}">
                                            <ns1:value><xsl:value-of select="normalize-space(accountNo)"/></ns1:value>
                                        </ns1:boxSplitSymbol>
                                    </ns1:boxSymbol>

                                    <!-- Signatory table (required: Full Name, ID Number, Instruction) -->
                                    <ns1:boxSymbol xsi:type="ns1:fullTable"
                                                   ns1:id="{concat('signTable_', $pos)}"
                                                   ns1:heading="Appointed signatory/ies"
                                                   ns1:showTotal="false"
                                                   ns1:headingColor="black">

                                        <ns1:tableColumn ns1:align="left" ns1:fieldName="fullName"    ns1:heading="Full Name"    ns1:disableSorting="true"/>
                                        <ns1:tableColumn ns1:align="left" ns1:fieldName="idNumber"    ns1:heading="ID Number"    ns1:disableSorting="true"/>
                                        <ns1:tableColumn ns1:align="left" ns1:fieldName="instruction" ns1:heading="Instruction"  ns1:disableSorting="true"/>
                                        <ns1:tableColumn ns1:align="left" ns1:fieldName="remove"      ns1:heading="Remove"       ns1:disableSorting="true"/>

                                        <ns1:rowGroup xsi:type="ns1:rowGroup"
                                                      ns1:groupId="{concat('signatory_', $pos)}"
                                                      ns1:groupHeaderLabel="">
                                            <ns1:totalsRow ns1:category=" ">
                                                <ns1:cell xsi:type="ns1:cell" ns1:col_id="fullName">
                                                    <ns1:cellItem xsi:type="ns1:cellItem">
                                                        <ns1:item xsi:type="ns1:simpleText"><ns1:value/></ns1:item>
                                                    </ns1:cellItem>
                                                </ns1:cell>
                                            </ns1:totalsRow>

                                            <ns1:groupTableButton xsi:type="ns1:imageButton"
                                                                  ns1:id="{concat('addSignBtn_', $pos)}"
                                                                  ns1:label="Add a signatory"
                                                                  ns1:tip="Add a signatory"
                                                                  ns1:url="{concat(
                                              'app-domain/mandates-and-resolutions/mandatesFill?accountCount=',
                                              count(/requestWrapper/request/accounts/account),
                                              '&amp;addSignatoryAt=', $pos,
                                              '&amp;pdfSessionId=', /requestWrapper/request/pdfSessionId
                                            )}"/>
                                        </ns1:rowGroup>

                                        <!-- Existing signatories -->
                                        <xsl:for-each select="signatories/signatory">
                                            <xsl:variable name="signPos" select="position()"/>
                                            <ns1:row xsi:type="ns1:fullTableRow" ns1:groupId="{concat('signatory_', $pos)}">

                                                <ns1:cell xsi:type="ns1:cell" ns1:col_id="fullName">
                                                    <ns1:cellItem xsi:type="ns1:cellItem">
                                                        <ns1:item xsi:type="ns1:input"
                                                                  ns1:name="{concat('fullName_', $pos, '_', $signPos)}"
                                                                  ns1:required="true"
                                                                  ns1:errorMessage="{/requestWrapper/mandatesAutoFillErrorModel/signatoryFullName}">
                                                            <ns1:value><xsl:value-of select="normalize-space(fullName)"/></ns1:value>
                                                        </ns1:item>
                                                    </ns1:cellItem>
                                                </ns1:cell>

                                                <ns1:cell xsi:type="ns1:cell" ns1:col_id="idNumber">
                                                    <ns1:cellItem xsi:type="ns1:cellItem">
                                                        <ns1:item xsi:type="ns1:input"
                                                                  ns1:name="{concat('idNumber_', $pos, '_', $signPos)}"
                                                                  ns1:required="true"
                                                                  ns1:errorMessage="{/requestWrapper/mandatesAutoFillErrorModel/signatoryIdNumber}">
                                                            <ns1:value><xsl:value-of select="normalize-space(idNumber)"/></ns1:value>
                                                        </ns1:item>
                                                    </ns1:cellItem>
                                                </ns1:cell>

                                                <ns1:cell xsi:type="ns1:cell" ns1:col_id="instruction">
                                                    <ns1:cellItem xsi:type="ns1:cellItem">
                                                        <xsl:variable name="cur" select="normalize-space(instruction)"/>

                                                        <xsl:choose>
                                                            <!-- If we have LOVs in the wrapper, use them -->
                                                            <xsl:when test="count(/requestWrapper/lovs/instructions/*) &gt; 0">
                                                                <ns1:item xsi:type="ns1:dropdown"
                                                                          ns1:id="{concat('instruction_', $pos, '_', $signPos)}"
                                                                          ns1:label="Instruction"
                                                                          ns1:required="true"
                                                                          ns1:errorMessage="{/requestWrapper/mandatesAutoFillErrorModel/signatoryInstruction}">
                                                                    <!-- Current value first so it shows as selected -->
                                                                    <xsl:if test="string-length($cur) &gt; 0">
                                                                        <ns1:label><xsl:value-of select="$cur"/></ns1:label>
                                                                        <ns1:value xsi:type="ns1:eventValue"><ns1:value><xsl:value-of select="$cur"/></ns1:value></ns1:value>
                                                                    </xsl:if>

                                                                    <!-- Remaining LOV values (excluding current) -->
                                                                    <xsl:for-each select="/requestWrapper/lovs/instructions/*[normalize-space(.) != $cur]">
                                                                        <ns1:label><xsl:value-of select="normalize-space(.)"/></ns1:label>
                                                                        <ns1:value xsi:type="ns1:eventValue"><ns1:value><xsl:value-of select="normalize-space(.)"/></ns1:value></ns1:value>
                                                                    </xsl:for-each>
                                                                </ns1:item>
                                                            </xsl:when>

                                                            <!-- Fallback: static two-option dropdown -->
                                                            <xsl:otherwise>
                                                                <ns1:item xsi:type="ns1:dropdown"
                                                                          ns1:id="{concat('instruction_', $pos, '_', $signPos)}"
                                                                          ns1:label="Instruction"
                                                                          ns1:required="true"
                                                                          ns1:errorMessage="{/requestWrapper/mandatesAutoFillErrorModel/signatoryInstruction}">
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
                                                                  ns1:id="{concat('removeSignBtn_', $pos, '_', $signPos)}"
                                                                  ns1:type="paper"
                                                                  ns1:label="Remove"
                                                                  ns1:formSubmit="true"
                                                                  ns1:target="main"
                                                                  ns1:width="2"
                                                                  ns1:url="{concat(
                                        'app-domain/mandates-and-resolutions/mandatesFill?accountCount=',
                                        count(/requestWrapper/request/accounts/account),
                                        '&amp;removeSignatoryAt=', $pos, '_', $signPos,
                                        '&amp;pdfSessionId=', /requestWrapper/request/pdfSessionId
                                      )}"/>
                                                    </ns1:cellItem>
                                                </ns1:cell>

                                            </ns1:row>
                                        </xsl:for-each>

                                        <!-- Fallback row when there are NO signatories yet (so required can trigger) -->
                                        <xsl:if test="count(signatories/signatory) = 0">
                                            <ns1:row xsi:type="ns1:fullTableRow" ns1:groupId="{concat('signatory_', $pos)}">
                                                <ns1:cell xsi:type="ns1:cell" ns1:col_id="fullName">
                                                    <ns1:cellItem xsi:type="ns1:cellItem">
                                                        <ns1:item xsi:type="ns1:input"
                                                                  ns1:name="{concat('fullName_', $pos, '_', 1)}"
                                                                  ns1:required="true"
                                                                  ns1:errorMessage="{/requestWrapper/mandatesAutoFillErrorModel/signatoryFullName}">
                                                            <ns1:value/>
                                                        </ns1:item>
                                                    </ns1:cellItem>
                                                </ns1:cell>
                                                <ns1:cell xsi:type="ns1:cell" ns1:col_id="idNumber">
                                                    <ns1:cellItem xsi:type="ns1:cellItem">
                                                        <ns1:item xsi:type="ns1:input"
                                                                  ns1:name="{concat('idNumber_', $pos, '_', 1)}"
                                                                  ns1:required="true"
                                                                  ns1:errorMessage="{/requestWrapper/mandatesAutoFillErrorModel/signatoryIdNumber}">
                                                            <ns1:value/>
                                                        </ns1:item>
                                                    </ns1:cellItem>
                                                </ns1:cell>
                                                <ns1:cell xsi:type="ns1:cell" ns1:col_id="instruction">
                                                    <ns1:cellItem xsi:type="ns1:cellItem">
                                                        <ns1:item xsi:type="ns1:dropdown"
                                                                  ns1:id="{concat('instruction_', $pos, '_', 1)}"
                                                                  ns1:label="Instruction"
                                                                  ns1:required="true"
                                                                  ns1:errorMessage="{/requestWrapper/mandatesAutoFillErrorModel/signatoryInstruction}">
                                                            <ns1:label>Add</ns1:label>
                                                            <ns1:value xsi:type="ns1:eventValue"><ns1:value>Add</ns1:value></ns1:value>
                                                            <ns1:label>Remove</ns1:label>
                                                            <ns1:value xsi:type="ns1:eventValue"><ns1:value>Remove</ns1:value></ns1:value>
                                                        </ns1:item>
                                                    </ns1:cellItem>
                                                </ns1:cell>
                                                <ns1:cell xsi:type="ns1:cell" ns1:col_id="remove">
                                                    <ns1:cellItem xsi:type="ns1:cellItem">
                                                        <ns1:item xsi:type="ns1:button"
                                                                  ns1:id="{concat('removeSignBtn_', $pos, '_', 1)}"
                                                                  ns1:type="paper"
                                                                  ns1:label="Remove"
                                                                  ns1:formSubmit="true"
                                                                  ns1:target="main"
                                                                  ns1:width="2"
                                                                  ns1:url="{concat(
                                                                    'app-domain/mandates-and-resolutions/mandatesFill?accountCount=',
                                                                    count(/requestWrapper/request/accounts/account),
                                                                    '&amp;removeSignatoryAt=', $pos, '_', 1,
                                                                    '&amp;pdfSessionId=', /requestWrapper/request/pdfSessionId
                                                                  )}"/>
                                                    </ns1:cellItem>
                                                </ns1:cell>
                                            </ns1:row>
                                        </xsl:if>

                                    </ns1:boxSymbol>

                                    <!-- Delete Account -->
                                    <ns1:boxSymbol xsi:type="ns1:button"
                                                   ns1:align="center"
                                                   ns1:id="{concat('deleteBtn_', $pos)}"
                                                   ns1:target="main"
                                                   ns1:label="Delete Account"
                                                   ns1:width="3"
                                                   ns1:formSubmit="true"
                                                   ns1:type="primary"
                                                   ns1:url="{concat(
                                   'app-domain/mandates-and-resolutions/mandatesFill?accountCount=',
                                   count(/requestWrapper/request/accounts/account) - 1,
                                   '&amp;removeAccountAt=', $pos,
                                   '&amp;pdfSessionId=', /requestWrapper/request/pdfSessionId
                                 )}"/>
                                </ns1:box>
                            </ns1:symbol>

                        </xsl:for-each>
                    </ns1:sections>

                </ns1:form>
            </symbol>

            <!-- Footer -->
            <symbol xsi:type="ns1:footer" ns1:buttonAlign="right">
                <ns1:baseButton ns1:id="backBtn"
                                ns1:url="{concat('app-domain/mandates-and-resolutions/nextStep?back=1&amp;pdfSessionId=', /requestWrapper/request/pdfSessionId)}"
                                ns1:label="Back"
                                ns1:formSubmit="true"
                                ns1:target="main"/>

                <ns1:baseButton ns1:id="addAccountBtn"
                                ns1:url="{concat(
                          'app-domain/mandates-and-resolutions/mandatesFill?accountCount=',
                          count(/requestWrapper/request/accounts/account) + 1,
                          '&amp;pdfSessionId=', /requestWrapper/request/pdfSessionId
                        )}"
                                ns1:label="Add Account"
                                ns1:formSubmit="true"
                                ns1:target="main"/>

                <ns1:baseButton ns1:id="save"
                                ns1:url="app-domain/mandates-and-resolutions/draft/save"
                                ns1:label="Save"
                                ns1:formSubmit="true"
                                ns1:target="main"/>

                <!-- Clicking Proceed posts to /mandatesSignatureCard where we validate and either stay here with errors or forward -->
                <ns1:baseButton ns1:id="proceed"
                                ns1:url="app-domain/mandates-and-resolutions/mandatesSignatureCard"
                                ns1:label="Proceed"
                                ns1:formSubmit="true"
                                ns1:target="main"/>
            </symbol>

        </page>
    </xsl:template>
</xsl:stylesheet>
