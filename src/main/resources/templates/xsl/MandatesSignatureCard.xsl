<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:ns1="http://ws.online.fnb.co.za/v1/common/"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <xsl:output method="xml" indent="yes"/>

    <!-- case-insensitive helpers -->
    <xsl:variable name="LOWER" select="'abcdefghijklmnopqrstuvwxyz'"/>
    <xsl:variable name="UPPER" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ'"/>

    <xsl:template match="/requestWrapper">
        <page xmlns:ns1="http://ws.online.fnb.co.za/v1/common/"
              xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
              id="mandatesSignatureForm"
              title="Mandates Signature Form"
              template="main"
              version="1">

            <symbol xsi:type="ns1:subTabGroup" ns1:subTabGroupHeading="Mandates"/>

            <symbol xsi:type="ns1:formLayout">
                <ns1:form ns1:action="app-domain/mandates-and-resolutions/mandatesSubmit" ns1:name="salesForm">

                    <ns1:sections ns1:align="left" ns1:width="full">
                        <ns1:symbol xsi:type="ns1:textHeading">
                            <ns1:value>Signature card confirmation</ns1:value>
                        </ns1:symbol>
                    </ns1:sections>

                    <!-- Carry-through: IDs and page code -->
                    <ns1:sections ns1:align="left" ns1:width="full">
                        <ns1:symbol xsi:type="ns1:input" ns1:name="pdfSessionId" ns1:inputType="hidden">
                            <ns1:value><xsl:value-of select="/requestWrapper/request/pdfSessionId"/></ns1:value>
                        </ns1:symbol>
                        <ns1:symbol xsi:type="ns1:input" ns1:name="stagingId" ns1:inputType="hidden">
                            <ns1:value><xsl:value-of select="/requestWrapper/request/stagingId"/></ns1:value>
                        </ns1:symbol>
                        <ns1:symbol xsi:type="ns1:input" ns1:name="pageCode" ns1:inputType="hidden">
                            <ns1:value>MANDATES_SIGNATURE_CARD</ns1:value>
                        </ns1:symbol>

                        <!-- Carry-through: top-level -->
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

                        <!-- Waiver tools (supports both shapes) -->
                        <xsl:for-each select="/requestWrapper/request/documentumTools/* | /requestWrapper/request/documentumTool">
                            <ns1:symbol xsi:type="ns1:input"
                                        ns1:name="{concat('documentumTools[', position()-1, ']')}"
                                        ns1:inputType="hidden">
                                <ns1:value><xsl:value-of select="."/></ns1:value>
                            </ns1:symbol>
                        </xsl:for-each>
                    </ns1:sections>

                    <!-- Accounts -->
                    <ns1:sections ns1:align="left" ns1:width="full">
                        <xsl:for-each select="/requestWrapper/request/accounts/account">
                            <xsl:variable name="pos" select="position()"/>

                            <ns1:symbol xsi:type="ns1:boxContainer" ns1:id="{concat('sig_box_', $pos)}">
                                <ns1:box xsi:type="ns1:box">

                                    <!-- Header -->
                                    <ns1:boxSymbol xsi:type="ns1:textHeading" ns1:size="3">
                                        <ns1:value>
                                            <xsl:text>Account </xsl:text>
                                            <xsl:value-of select="$pos"/>
                                            <xsl:text>: </xsl:text>
                                            <xsl:value-of select="normalize-space(accountName)"/>
                                            <xsl:text> (</xsl:text>
                                            <xsl:value-of select="normalize-space(accountNo)"/>
                                            <xsl:text>)</xsl:text>
                                        </ns1:value>
                                    </ns1:boxSymbol>

                                    <!-- Editable account fields -->
                                    <ns1:boxSymbol xsi:type="ns1:boxSplit" ns1:width="50">
                                        <ns1:boxSplitSymbol xsi:type="ns1:input"
                                                            ns1:name="{concat('accountName_', $pos)}"
                                                            ns1:label="Account Name"
                                                            ns1:inputType="text">
                                            <ns1:value><xsl:value-of select="normalize-space(accountName)"/></ns1:value>
                                        </ns1:boxSplitSymbol>
                                    </ns1:boxSymbol>

                                    <ns1:boxSymbol xsi:type="ns1:boxSplit" ns1:width="50">
                                        <ns1:boxSplitSymbol xsi:type="ns1:input"
                                                            ns1:name="{concat('accountNo_', $pos)}"
                                                            ns1:label="Account no."
                                                            ns1:inputType="text">
                                            <ns1:value><xsl:value-of select="normalize-space(accountNo)"/></ns1:value>
                                        </ns1:boxSplitSymbol>
                                    </ns1:boxSymbol>

                                    <!-- ===== Signatories (split Added / Removed) ===== -->
                                    <xsl:variable name="addedCount"
                                                  select="count(signatories/signatory[translate(normalize-space(instruction), $LOWER, $UPPER)='ADD'])"/>
                                    <xsl:variable name="removedCount"
                                                  select="count(signatories/signatory[translate(normalize-space(instruction), $LOWER, $UPPER)='REMOVE'])"/>

                                    <!-- ===== Added Signatories (visible) ===== -->
                                    <xsl:if test="$addedCount &gt; 0">
                                        <ns1:boxSymbol xsi:type="ns1:textHeading">
                                            <ns1:value>Added Signatories</ns1:value>
                                        </ns1:boxSymbol>

                                        <xsl:for-each select="signatories/signatory[translate(normalize-space(instruction), $LOWER, $UPPER)='ADD']">
                                            <xsl:variable name="spos" select="count(preceding-sibling::signatory) + 1"/>

                                            <!-- LEFT column: Full name + ID no. -->
                                            <ns1:boxSymbol xsi:type="ns1:boxSplit" ns1:width="50">
                                                <ns1:boxSplitSymbol xsi:type="ns1:input"
                                                                    ns1:name="{concat('fullName_', $pos, '_', $spos)}"
                                                                    ns1:label="Full name (As per identity document)"
                                                                    ns1:inputType="text" ns1:maxlength="50">
                                                    <ns1:value><xsl:value-of select="normalize-space(fullName)"/></ns1:value>
                                                </ns1:boxSplitSymbol>

                                                <ns1:boxSplitSymbol xsi:type="ns1:input"
                                                                    ns1:name="{concat('idNumber_', $pos, '_', $spos)}"
                                                                    ns1:label="ID no."
                                                                    ns1:inputType="text" ns1:maxlength="13">
                                                    <ns1:value><xsl:value-of select="normalize-space(idNumber)"/></ns1:value>
                                                </ns1:boxSplitSymbol>
                                            </ns1:boxSymbol>

                                            <!-- RIGHT column: Capacity + Group -->
                                            <ns1:boxSymbol xsi:type="ns1:boxSplit" ns1:width="50">
                                                <ns1:boxSplitSymbol xsi:type="ns1:input"
                                                                    ns1:name="{concat('capacity_', $pos, '_', $spos)}"
                                                                    ns1:label="Capacity (e.g. Director, Manager)"
                                                                    ns1:inputType="text" ns1:maxlength="50">
                                                    <ns1:value><xsl:value-of select="normalize-space(capacity)"/></ns1:value>
                                                </ns1:boxSplitSymbol>

                                                <ns1:boxSplitSymbol xsi:type="ns1:input"
                                                                    ns1:name="{concat('group_', $pos, '_', $spos)}"
                                                                    ns1:label="Group (If any, e.g. A/B/C)"
                                                                    ns1:inputType="text" ns1:maxlength="13">
                                                    <ns1:value><xsl:value-of select="normalize-space(group)"/></ns1:value>
                                                </ns1:boxSplitSymbol>
                                            </ns1:boxSymbol>

                                            <!-- Hidden instruction -->
                                            <ns1:boxSymbol xsi:type="ns1:boxSplit" ns1:width="50">
                                                <ns1:boxSplitSymbol xsi:type="ns1:input"
                                                                    ns1:name="{concat('instruction_', $pos, '_', $spos)}"
                                                                    ns1:label="Instruction (Add/Remove)"
                                                                    ns1:inputType="hidden">
                                                    <ns1:value>
                                                        <xsl:choose>
                                                            <xsl:when test="normalize-space(instruction) != ''">
                                                                <xsl:value-of select="normalize-space(instruction)"/>
                                                            </xsl:when>
                                                            <xsl:otherwise>Add</xsl:otherwise>
                                                        </xsl:choose>
                                                    </ns1:value>
                                                </ns1:boxSplitSymbol>
                                            </ns1:boxSymbol>
                                        </xsl:for-each>
                                    </xsl:if>

                                    <!-- Removed Signatories UI intentionally hidden -->

                                    <!-- Hidden carry-through for removed signatories -->
                                    <xsl:for-each select="signatories/signatory[translate(normalize-space(instruction), $LOWER, $UPPER)='REMOVE']">
                                        <xsl:variable name="spos" select="count(preceding-sibling::signatory) + 1"/>
                                        <ns1:boxSymbol xsi:type="ns1:boxSplit" ns1:width="100">
                                            <ns1:boxSplitSymbol xsi:type="ns1:input"
                                                                ns1:name="{concat('fullName_', $pos, '_', $spos)}"
                                                                ns1:inputType="hidden">
                                                <ns1:value><xsl:value-of select="normalize-space(fullName)"/></ns1:value>
                                            </ns1:boxSplitSymbol>
                                            <ns1:boxSplitSymbol xsi:type="ns1:input"
                                                                ns1:name="{concat('idNumber_', $pos, '_', $spos)}"
                                                                ns1:inputType="hidden">
                                                <ns1:value><xsl:value-of select="normalize-space(idNumber)"/></ns1:value>
                                            </ns1:boxSplitSymbol>
                                            <ns1:boxSplitSymbol xsi:type="ns1:input"
                                                                ns1:name="{concat('capacity_', $pos, '_', $spos)}"
                                                                ns1:inputType="hidden">
                                                <ns1:value><xsl:value-of select="normalize-space(capacity)"/></ns1:value>
                                            </ns1:boxSplitSymbol>
                                            <ns1:boxSplitSymbol xsi:type="ns1:input"
                                                                ns1:name="{concat('group_', $pos, '_', $spos)}"
                                                                ns1:inputType="hidden">
                                                <ns1:value><xsl:value-of select="normalize-space(group)"/></ns1:value>
                                            </ns1:boxSplitSymbol>
                                            <ns1:boxSplitSymbol xsi:type="ns1:input"
                                                                ns1:name="{concat('instruction_', $pos, '_', $spos)}"
                                                                ns1:inputType="hidden">
                                                <ns1:value>
                                                    <xsl:choose>
                                                        <xsl:when test="normalize-space(instruction) != ''">
                                                            <xsl:value-of select="normalize-space(instruction)"/>
                                                        </xsl:when>
                                                        <xsl:otherwise>Remove</xsl:otherwise>
                                                    </xsl:choose>
                                                </ns1:value>
                                            </ns1:boxSplitSymbol>
                                        </ns1:boxSymbol>
                                    </xsl:for-each>

                                    <!-- If no signatories, render one empty set -->
                                    <xsl:if test="count(signatories/signatory)=0">
                                        <xsl:variable name="spos" select="1"/>

                                        <!-- LEFT column: Full name + ID no. -->
                                        <ns1:boxSymbol xsi:type="ns1:boxSplit" ns1:width="50">
                                            <ns1:boxSplitSymbol xsi:type="ns1:input"
                                                                ns1:name="{concat('fullName_', $pos, '_', $spos)}"
                                                                ns1:label="Full name (As per identity document)"
                                                                ns1:inputType="text" ns1:maxlength="50">
                                                <ns1:value/>
                                            </ns1:boxSplitSymbol>
                                            <ns1:boxSplitSymbol xsi:type="ns1:input"
                                                                ns1:name="{concat('idNumber_', $pos, '_', $spos)}"
                                                                ns1:label="ID no."
                                                                ns1:inputType="text" ns1:maxlength="13">
                                                <ns1:value/>
                                            </ns1:boxSplitSymbol>
                                        </ns1:boxSymbol>

                                        <!-- RIGHT column: Capacity + Group -->
                                        <ns1:boxSymbol xsi:type="ns1:boxSplit" ns1:width="50">
                                            <ns1:boxSplitSymbol xsi:type="ns1:input"
                                                                ns1:name="{concat('capacity_', $pos, '_', $spos)}"
                                                                ns1:label="Capacity (e.g. Director, Manager)"
                                                                ns1:inputType="text" ns1:maxlength="50">
                                                <ns1:value/>
                                            </ns1:boxSplitSymbol>
                                            <ns1:boxSplitSymbol xsi:type="ns1:input"
                                                                ns1:name="{concat('group_', $pos, '_', $spos)}"
                                                                ns1:label="Group (If any, e.g. A/B/C)"
                                                                ns1:inputType="text" ns1:maxlength="13">
                                                <ns1:value/>
                                            </ns1:boxSplitSymbol>
                                        </ns1:boxSymbol>

                                        <!-- Hidden instruction -->
                                        <ns1:boxSymbol xsi:type="ns1:boxSplit" ns1:width="50">
                                            <ns1:boxSplitSymbol xsi:type="ns1:input"
                                                                ns1:name="{concat('instruction_', $pos, '_', $spos)}"
                                                                ns1:label="Instruction (Add/Remove)"
                                                                ns1:inputType="hidden">
                                                <ns1:value>Add</ns1:value>
                                            </ns1:boxSplitSymbol>
                                        </ns1:boxSymbol>
                                    </xsl:if>

                                </ns1:box>
                            </ns1:symbol>
                        </xsl:for-each>
                    </ns1:sections>
                </ns1:form>
            </symbol>

            <!-- Footer -->
            <symbol xsi:type="ns1:footer" ns1:text="" ns1:textAlign="left" ns1:buttonAlign="right">
                <ns1:baseButton ns1:id="backBtn"
                                ns1:url="{concat('app-domain/mandates-and-resolutions/mandatesFill?pdfSessionId=', /requestWrapper/request/pdfSessionId)}"
                                ns1:target="main"
                                ns1:formSubmit="false"
                                ns1:label="Back"/>
                <ns1:baseButton ns1:id="save"
                                ns1:url="app-domain/mandates-and-resolutions/draft/save"
                                ns1:label="Save"
                                ns1:formSubmit="true"
                                ns1:target="main"/>
                <ns1:baseButton ns1:id="submitBtn"
                                ns1:url="app-domain/mandates-and-resolutions/mandatesSubmit"
                                ns1:target="main"
                                ns1:formSubmit="true"
                                ns1:label="Submit"/>
            </symbol>
        </page>
    </xsl:template>
</xsl:stylesheet>
