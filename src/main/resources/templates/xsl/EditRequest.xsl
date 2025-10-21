<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:ns1="http://ws.online.fnb.co.za/v1/common/"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">

    <xsl:output method="xml" indent="yes"/>

    <!-- helpers -->
    <xsl:variable name="LOWER" select="'abcdefghijklmnopqrstuvwxyz'"/>
    <xsl:variable name="UPPER" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ'"/>

    <!-- ========= Pick the correct <request> (XPath 1.0 safe) ========= -->
    <xsl:variable name="REQ1"
                  select="(//*[local-name()='request'][
                  .//*[local-name()='directors']/*[local-name()='director']
                  or
                  .//*[local-name()='authorities']/*[local-name()='authority']
                ])[1]"/>
    <xsl:variable name="REQ2" select="(//*[local-name()='request'])[1]"/>
    <xsl:variable name="REQ"  select="($REQ1 | $REQ2[not($REQ1)])[1]"/>

    <!-- Normalized request-type flags -->
    <xsl:variable name="TYPE_RAW" select="normalize-space($REQ/*[local-name()='type'])"/>
    <xsl:variable name="TYPE_UP"  select="translate($TYPE_RAW, $LOWER, $UPPER)"/>
    <xsl:variable name="IS_MANDATES"    select="contains($TYPE_UP,'MANDATE')"/>
    <xsl:variable name="IS_RESOLUTIONS" select="contains($TYPE_UP,'RESOLUTION')"/>
    <xsl:variable name="IS_BOTH"        select="contains($TYPE_UP,'BOTH')"/>
    <xsl:variable name="SHOW_MANDATES"    select="$IS_MANDATES or $IS_BOTH"/>
    <xsl:variable name="SHOW_RESOLUTIONS" select="$IS_RESOLUTIONS or $IS_BOTH"/>

    <!-- Accounts (from payload) -->
    <xsl:variable name="ACCOUNTS" select="$REQ/accounts/account | $REQ/accounts/* | $REQ/account"/>

    <!-- Group signatories by (accountNumber|accountNo, accountName|name) -->
    <xsl:key name="kAcctBySig"
             match="signatories/*"
             use="concat(
             normalize-space(string((accountNumber|accountNo)[1])),
             '|',
             normalize-space(string((accountName|name)[1]))
           )"/>

    <!-- Directors/Authorities source -->
    <xsl:variable name="AUTH_IN_REQ"
                  select="$REQ//*[ ( *[local-name()='firstname' or local-name()='firstName' or local-name()='name'] )
                                 and ( *[local-name()='surname' or local-name()='lastName' or local-name()='lastname'] )
                                 and ( *[local-name()='designation' or local-name()='role' or local-name()='title'] ) ]"/>
    <xsl:variable name="AUTH"
                  select="$AUTH_IN_REQ
                        | (//*[ ( *[local-name()='firstname' or local-name()='firstName' or local-name()='name'] )
                               and ( *[local-name()='surname' or local-name()='lastName' or local-name()='lastname'] )
                               and ( *[local-name()='designation' or local-name()='role' or local-name()='title'] ) ])[count($AUTH_IN_REQ)=0]"/>
    <xsl:variable name="HAS_AUTH" select="count($AUTH)"/>

    <xsl:template match="/">
        <page xmlns:ns1="http://ws.online.fnb.co.za/v1/common/"
              xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
              id="mandatesAutoFillForm"
              title="Mandates Auto Fill Form"
              template="main"
              layout=""
              version="1">

            <symbol xsi:type="ns1:subTabGroup" ns1:subTabGroupHeading="Mandates and resolution"/>

            <symbol xsi:type="ns1:formLayout">
                <ns1:form ns1:action="http://localhost:8080/bifrost/" ns1:name="salesForm">

                    <!-- Header -->
                    <ns1:sections ns1:align="left" ns1:width="full">
                        <ns1:symbol xsi:type="ns1:textHeading" ns1:align="left">
                            <ns1:value>Edit Request</ns1:value>
                        </ns1:symbol>
                    </ns1:sections>

                    <ns1:sections ns1:align="left" ns1:width="full">
                        <ns1:symbol xsi:type="ns1:boxContainer" ns1:id="headerBox">
                            <ns1:box xsi:type="ns1:box">
                                <ns1:boxSymbol xsi:type="ns1:boxSplit" ns1:width="50">
                                    <ns1:boxSplitSymbol xsi:type="ns1:input" ns1:name="requestID" ns1:label="Request ID" ns1:inputType="text">
                                        <ns1:value><xsl:value-of select="$REQ/*[local-name()='requestId']"/></ns1:value>
                                    </ns1:boxSplitSymbol>
                                    <ns1:boxSplitSymbol xsi:type="ns1:input" ns1:name="requestType" ns1:label="Request Type" ns1:inputType="text">
                                        <ns1:value><xsl:value-of select="$REQ/*[local-name()='type']"/></ns1:value>
                                    </ns1:boxSplitSymbol>
                                    <ns1:boxSplitSymbol xsi:type="ns1:input" ns1:name="subStatus" ns1:label="Sub Status" ns1:inputType="text">
                                        <ns1:value><xsl:value-of select="$REQ/*[local-name()='subStatus']"/></ns1:value>
                                    </ns1:boxSplitSymbol>
                                    <ns1:boxSplitSymbol xsi:type="ns1:input" ns1:name="lastModified" ns1:label="Last Modified" ns1:inputType="text">
                                        <ns1:value><xsl:value-of select="$REQ/*[local-name()='updated']"/></ns1:value>
                                    </ns1:boxSplitSymbol>

                                    <xsl:if test="string-length(normalize-space($REQ/*[local-name()='rejectComment'])) &gt; 0">
                                        <ns1:boxSplitSymbol xsi:type="ns1:textParagraph" ns1:subHeading="Reason for rejection">
                                            <ns1:value><xsl:value-of select="$REQ/*[local-name()='rejectComment']"/></ns1:value>
                                        </ns1:boxSplitSymbol>
                                    </xsl:if>
                                </ns1:boxSymbol>

                                <ns1:boxSymbol xsi:type="ns1:boxSplit" ns1:width="50">
                                    <ns1:boxSplitSymbol xsi:type="ns1:input" ns1:name="companyName" ns1:label="Company Name" ns1:inputType="text">
                                        <ns1:value><xsl:value-of select="$REQ/*[local-name()='companyName']"/></ns1:value>
                                    </ns1:boxSplitSymbol>
                                    <ns1:boxSplitSymbol xsi:type="ns1:input" ns1:name="status" ns1:label="Status" ns1:inputType="text">
                                        <ns1:value><xsl:value-of select="$REQ/*[local-name()='status']"/></ns1:value>
                                    </ns1:boxSplitSymbol>
                                    <ns1:boxSplitSymbol xsi:type="ns1:input" ns1:name="createdDate" ns1:label="Created Date" ns1:inputType="text">
                                        <ns1:value><xsl:value-of select="$REQ/*[local-name()='created']"/></ns1:value>
                                    </ns1:boxSplitSymbol>
                                    <ns1:boxSplitSymbol xsi:type="ns1:input" ns1:name="sla" ns1:label="SLA (days)" ns1:inputType="text">
                                        <ns1:value><xsl:value-of select="$REQ/*[local-name()='sla']"/></ns1:value>
                                    </ns1:boxSplitSymbol>

                                    <xsl:if test="string-length(normalize-space($REQ/*[local-name()='rejectComment'])) = 0 and string-length(normalize-space($REQ/*[local-name()='approveComment'])) &gt; 0">
                                        <ns1:boxSplitSymbol xsi:type="ns1:textParagraph" ns1:subHeading="Approval comment">
                                            <ns1:value><xsl:value-of select="$REQ/*[local-name()='approveComment']"/></ns1:value>
                                        </ns1:boxSplitSymbol>
                                    </xsl:if>
                                </ns1:boxSymbol>
                            </ns1:box>
                        </ns1:symbol>
                    </ns1:sections>

                    <!-- ================= Mandates (same grouping as View, but editable) ================= -->
                    <xsl:if test="$SHOW_MANDATES">
                        <ns1:sections ns1:align="left" ns1:width="full">
                            <ns1:symbol xsi:type="ns1:textHeading" ns1:align="left">
                                <ns1:value>Account Signatories</ns1:value>
                            </ns1:symbol>
                        </ns1:sections>

                        <ns1:sections ns1:align="left" ns1:width="full">
                            <xsl:variable name="ALL_SIGS" select="$REQ//signatories/*"/>

                            <xsl:choose>
                                <xsl:when test="count($ALL_SIGS) &gt; 0">
                                    <!-- Unique account groups exactly like View -->
                                    <xsl:for-each select="$ALL_SIGS[
                    generate-id() = generate-id(
                      key('kAcctBySig',
                        concat(
                          normalize-space(string((accountNumber|accountNo)[1])),
                          '|',
                          normalize-space(string((accountName|name)[1]))
                        )
                      )[1]
                    )
                  ]">
                                        <xsl:variable name="aPos" select="position()"/>

                                        <!-- Resolve account name/number (as in View) -->
                                        <xsl:variable name="accNameSig" select="normalize-space(string((accountName|name)[1]))"/>
                                        <xsl:variable name="accNoSig"   select="normalize-space(string((accountNumber|accountNo)[1]))"/>
                                        <xsl:variable name="accNameAnc"
                                                      select="normalize-space(string((ancestor::*[accountName or name][1]/accountName
                                               | ancestor::*[accountName or name][1]/name)[1]))"/>
                                        <xsl:variable name="accNoAnc"
                                                      select="normalize-space(string((ancestor::*[accountNumber or accountNo][1]/accountNumber
                                               | ancestor::*[accountNumber or accountNo][1]/accountNo)[1]))"/>

                                        <xsl:variable name="accNameEff">
                                            <xsl:choose>
                                                <xsl:when test="string-length($accNameSig) &gt; 0"><xsl:value-of select="$accNameSig"/></xsl:when>
                                                <xsl:when test="string-length($accNameAnc) &gt; 0"><xsl:value-of select="$accNameAnc"/></xsl:when>
                                                <xsl:otherwise>Account</xsl:otherwise>
                                            </xsl:choose>
                                        </xsl:variable>
                                        <xsl:variable name="accNoEff">
                                            <xsl:choose>
                                                <xsl:when test="string-length($accNoSig) &gt; 0"><xsl:value-of select="$accNoSig"/></xsl:when>
                                                <xsl:otherwise><xsl:value-of select="$accNoAnc"/></xsl:otherwise>
                                            </xsl:choose>
                                        </xsl:variable>

                                        <xsl:variable name="gKey"
                                                      select="concat(
                                    normalize-space(string((accountNumber|accountNo)[1])),
                                    '|',
                                    normalize-space(string((accountName|name)[1]))
                                  )"/>
                                        <xsl:variable name="SIGS_THIS" select="key('kAcctBySig', $gKey)"/>
                                        <xsl:variable name="ADD_SET"   select="$SIGS_THIS[translate(normalize-space(instructions), $LOWER, $UPPER)='ADD']"/>
                                        <xsl:variable name="REM_SET"   select="$SIGS_THIS[translate(normalize-space(instructions), $LOWER, $UPPER)='REMOVE']"/>
                                        <xsl:variable name="ADD_COUNT" select="count($ADD_SET)"/>

                                        <!-- Find matching accountId if present -->
                                        <xsl:variable name="ACC_MATCH"
                                                      select="$ACCOUNTS[
                                    normalize-space(string((accountName|name)[1])) = $accNameEff
                                    and
                                    normalize-space(string((accountNumber|accountNo)[1])) = $accNoEff
                                  ][1]"/>
                                        <xsl:variable name="ACC_ID" select="string($ACC_MATCH/accountId)"/>

                                        <ns1:symbol xsi:type="ns1:boxContainer" ns1:id="{concat('acc_', $aPos)}">
                                            <ns1:box xsi:type="ns1:box">

                                                <!-- Heading -->
                                                <ns1:boxSymbol xsi:type="ns1:textHeading" ns1:size="4">
                                                    <ns1:value>
                                                        <xsl:value-of select="$accNameEff"/>
                                                        <xsl:text> (</xsl:text><xsl:value-of select="$accNoEff"/><xsl:text>)</xsl:text>
                                                    </ns1:value>
                                                </ns1:boxSymbol>

                                                <!-- Hidden account fields -->
                                                <ns1:boxSymbol xsi:type="ns1:boxSplit" ns1:width="100">
                                                    <ns1:boxSplitSymbol xsi:type="ns1:input" ns1:name="{concat('accountId_', $aPos)}" ns1:inputType="hidden">
                                                        <ns1:value><xsl:value-of select="$ACC_ID"/></ns1:value>
                                                    </ns1:boxSplitSymbol>
                                                    <ns1:boxSplitSymbol xsi:type="ns1:input" ns1:name="{concat('accountName_', $aPos)}" ns1:inputType="hidden">
                                                        <ns1:value><xsl:value-of select="$accNameEff"/></ns1:value>
                                                    </ns1:boxSplitSymbol>
                                                    <ns1:boxSplitSymbol xsi:type="ns1:input" ns1:name="{concat('accountNo_', $aPos)}" ns1:inputType="hidden">
                                                        <ns1:value><xsl:value-of select="$accNoEff"/></ns1:value>
                                                    </ns1:boxSplitSymbol>
                                                </ns1:boxSymbol>

                                                <!-- ===== Added Signatories (editable) ===== -->
                                                <ns1:boxSymbol xsi:type="ns1:textHeading" ns1:size="5">
                                                    <ns1:value>Added Signatories</ns1:value>
                                                </ns1:boxSymbol>

                                                <ns1:boxSymbol xsi:type="ns1:fullTable"
                                                               ns1:id="{concat('sigTableAdd_', $aPos)}"
                                                               ns1:heading=""
                                                               ns1:showTotal="false"
                                                               ns1:headingColor="black">
                                                    <ns1:tableColumn ns1:align="left" ns1:fieldName="fullName"     ns1:heading="Full Name"     ns1:disableSorting="true"/>
                                                    <ns1:tableColumn ns1:align="left" ns1:fieldName="idNumber"     ns1:heading="ID Number"     ns1:disableSorting="true"/>
                                                    <ns1:tableColumn ns1:align="left" ns1:fieldName="instructions" ns1:heading="Instructions"  ns1:disableSorting="true"/>
                                                    <ns1:tableColumn ns1:align="left" ns1:fieldName="remove"       ns1:heading="Remove"        ns1:disableSorting="true"/>

                                                    <ns1:rowGroup xsi:type="ns1:rowGroup" ns1:groupId="{concat('gAdd_', $aPos)}" ns1:groupHeaderLabel="">
                                                        <ns1:totalsRow ns1:category=" ">
                                                            <ns1:cell xsi:type="ns1:cell" ns1:col_id="fullName">
                                                                <ns1:cellItem xsi:type="ns1:cellItem">
                                                                    <ns1:item xsi:type="ns1:simpleText"><ns1:value/></ns1:item>
                                                                </ns1:cellItem>
                                                            </ns1:cell>
                                                        </ns1:totalsRow>

                                                        <ns1:groupTableButton xsi:type="ns1:imageButton"
                                                                              ns1:id="{concat('addSignBtn_', $aPos)}"
                                                                              ns1:label="Add a signatory"
                                                                              ns1:tip="Add a signatory"
                                                                              ns1:url="{concat(
                                                    'app-domain/mandates-and-resolutions/editRequestAddSignatory/',
                                                    $REQ/*[local-name()='requestId'],
                                                    '?addSignatoryAt=',
                                                    $aPos
                                                  )}"/>
                                                    </ns1:rowGroup>

                                                    <xsl:choose>
                                                        <xsl:when test="$ADD_COUNT &gt; 0">
                                                            <xsl:for-each select="$ADD_SET">
                                                                <xsl:variable name="sPos" select="position()"/>
                                                                <ns1:row xsi:type="ns1:fullTableRow" ns1:groupId="{concat('gAdd_', $aPos)}">
                                                                    <!-- Full name + hidden capacity/group (split into separate cellItems) -->
                                                                    <ns1:cell xsi:type="ns1:cell" ns1:col_id="fullName">
                                                                        <ns1:cellItem xsi:type="ns1:cellItem">
                                                                            <ns1:item xsi:type="ns1:input" ns1:name="{concat('fullName_', $aPos, '_', $sPos)}">
                                                                                <ns1:value><xsl:value-of select="normalize-space((fullName|fullname|name)[1])"/></ns1:value>
                                                                            </ns1:item>
                                                                        </ns1:cellItem>
                                                                        <ns1:cellItem xsi:type="ns1:cellItem">
                                                                            <ns1:item xsi:type="ns1:input" ns1:name="{concat('capacity_', $aPos, '_', $sPos)}" ns1:inputType="hidden">
                                                                                <ns1:value><xsl:value-of select="normalize-space(capacity)"/></ns1:value>
                                                                            </ns1:item>
                                                                        </ns1:cellItem>
                                                                        <ns1:cellItem xsi:type="ns1:cellItem">
                                                                            <ns1:item xsi:type="ns1:input" ns1:name="{concat('group_', $aPos, '_', $sPos)}" ns1:inputType="hidden">
                                                                                <ns1:value><xsl:value-of select="normalize-space((groupCategory|group)[1])"/></ns1:value>
                                                                            </ns1:item>
                                                                        </ns1:cellItem>
                                                                    </ns1:cell>

                                                                    <ns1:cell xsi:type="ns1:cell" ns1:col_id="idNumber">
                                                                        <ns1:cellItem xsi:type="ns1:cellItem">
                                                                            <ns1:item xsi:type="ns1:input" ns1:name="{concat('idNumber_', $aPos, '_', $sPos)}">
                                                                                <ns1:value><xsl:value-of select="normalize-space((idNumber|id|identificationNumber)[1])"/></ns1:value>
                                                                            </ns1:item>
                                                                        </ns1:cellItem>
                                                                    </ns1:cell>

                                                                    <ns1:cell xsi:type="ns1:cell" ns1:col_id="instructions">
                                                                        <!-- hidden original -->
                                                                        <ns1:cellItem xsi:type="ns1:cellItem">
                                                                            <xsl:variable name="curRaw" select="normalize-space(instructions)"/>
                                                                            <xsl:variable name="curUp"  select="translate($curRaw,$LOWER,$UPPER)"/>
                                                                            <xsl:variable name="cur">
                                                                                <xsl:choose>
                                                                                    <xsl:when test="$curUp='REMOVE'">Remove</xsl:when>
                                                                                    <xsl:otherwise>Add</xsl:otherwise>
                                                                                </xsl:choose>
                                                                            </xsl:variable>
                                                                            <ns1:item xsi:type="ns1:input"
                                                                                      ns1:name="{concat('origInstruction_', $aPos, '_', $sPos)}"
                                                                                      ns1:inputType="hidden">
                                                                                <ns1:value><xsl:value-of select="$cur"/></ns1:value>
                                                                            </ns1:item>
                                                                        </ns1:cellItem>

                                                                        <!-- dropdown -->
                                                                        <ns1:cellItem xsi:type="ns1:cellItem">
                                                                            <xsl:variable name="curRaw2" select="normalize-space(instructions)"/>
                                                                            <xsl:variable name="curUp2"  select="translate($curRaw2,$LOWER,$UPPER)"/>
                                                                            <xsl:variable name="cur2">
                                                                                <xsl:choose>
                                                                                    <xsl:when test="$curUp2='REMOVE'">Remove</xsl:when>
                                                                                    <xsl:otherwise>Add</xsl:otherwise>
                                                                                </xsl:choose>
                                                                            </xsl:variable>
                                                                            <ns1:item xsi:type="ns1:dropdown"
                                                                                      ns1:id="{concat('instruction_', $aPos, '_', $sPos)}"
                                                                                      ns1:label="Instruction"
                                                                                      ns1:selectedValue="{$cur2}"
                                                                                      ns1:pleaseSelectOptionEnabled="false">
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
                                                                                      ns1:id="{concat('removeSignBtn_', $aPos, '_', $sPos)}"
                                                                                      ns1:type="paper"
                                                                                      ns1:label="Remove"
                                                                                      ns1:formSubmit="true"
                                                                                      ns1:target="main"
                                                                                      ns1:width="2"
                                                                                      ns1:url="{concat(
                                                  'app-domain/mandates-and-resolutions/editRequestRemoveSignatory/',
                                                  $REQ/*[local-name()='requestId'],
                                                  '?removeSignatoryAt=',
                                                  $aPos, '_', $sPos
                                                )}"/>
                                                                        </ns1:cellItem>
                                                                    </ns1:cell>
                                                                </ns1:row>
                                                            </xsl:for-each>
                                                        </xsl:when>
                                                        <xsl:otherwise>
                                                            <ns1:row xsi:type="ns1:fullTableRow" ns1:groupId="{concat('gAdd_', $aPos)}">
                                                                <ns1:cell xsi:type="ns1:cell" ns1:col_id="fullName">
                                                                    <ns1:cellItem xsi:type="ns1:cellItem">
                                                                        <ns1:item xsi:type="ns1:simpleText"><ns1:value>None captured</ns1:value></ns1:item>
                                                                    </ns1:cellItem>
                                                                </ns1:cell>
                                                                <ns1:cell xsi:type="ns1:cell" ns1:col_id="idNumber">
                                                                    <ns1:cellItem xsi:type="ns1:cellItem">
                                                                        <ns1:item xsi:type="ns1:simpleText"><ns1:value>-</ns1:value></ns1:item>
                                                                    </ns1:cellItem>
                                                                </ns1:cell>
                                                                <ns1:cell xsi:type="ns1:cell" ns1:col_id="instructions">
                                                                    <ns1:cellItem xsi:type="ns1:cellItem">
                                                                        <ns1:item xsi:type="ns1:simpleText"><ns1:value>-</ns1:value></ns1:item>
                                                                    </ns1:cellItem>
                                                                </ns1:cell>
                                                                <ns1:cell xsi:type="ns1:cell" ns1:col_id="remove">
                                                                    <ns1:cellItem xsi:type="ns1:cellItem">
                                                                        <ns1:item xsi:type="ns1:simpleText"><ns1:value/></ns1:item>
                                                                    </ns1:cellItem>
                                                                </ns1:cell>
                                                            </ns1:row>
                                                        </xsl:otherwise>
                                                    </xsl:choose>
                                                </ns1:boxSymbol>

                                                <!-- ===== Removed Signatories (editable) ===== -->
                                                <xsl:if test="count($REM_SET) &gt; 0">
                                                    <ns1:boxSymbol xsi:type="ns1:textHeading" ns1:size="5">
                                                        <ns1:value>Removed Signatories</ns1:value>
                                                    </ns1:boxSymbol>

                                                    <ns1:boxSymbol xsi:type="ns1:fullTable"
                                                                   ns1:id="{concat('sigTableRem_', $aPos)}"
                                                                   ns1:heading=""
                                                                   ns1:showTotal="false"
                                                                   ns1:headingColor="black">
                                                        <ns1:tableColumn ns1:align="left" ns1:fieldName="fullName"     ns1:heading="Full Name"     ns1:disableSorting="true"/>
                                                        <ns1:tableColumn ns1:align="left" ns1:fieldName="idNumber"     ns1:heading="ID Number"     ns1:disableSorting="true"/>
                                                        <ns1:tableColumn ns1:align="left" ns1:fieldName="instructions" ns1:heading="Instructions"  ns1:disableSorting="true"/>
                                                        <ns1:tableColumn ns1:align="left" ns1:fieldName="remove"       ns1:heading="Remove"        ns1:disableSorting="true"/>

                                                        <ns1:rowGroup xsi:type="ns1:rowGroup" ns1:groupId="{concat('gRem_', $aPos)}" ns1:groupHeaderLabel="">
                                                            <ns1:totalsRow ns1:category=" ">
                                                                <ns1:cell xsi:type="ns1:cell" ns1:col_id="fullName">
                                                                    <ns1:cellItem xsi:type="ns1:cellItem">
                                                                        <ns1:item xsi:type="ns1:simpleText"><ns1:value/></ns1:item>
                                                                    </ns1:cellItem>
                                                                </ns1:cell>
                                                            </ns1:totalsRow>
                                                            <ns1:groupTableButton xsi:type="ns1:imageButton"
                                                                                  ns1:id="{concat('addSignBtn_', $aPos)}"
                                                                                  ns1:label="Add a signatory"
                                                                                  ns1:tip="Add a signatory"
                                                                                  ns1:url="{concat(
                                                    'app-domain/mandates-and-resolutions/editRequestAddSignatory/',
                                                    $REQ/*[local-name()='requestId'],
                                                    '?addSignatoryAt=',
                                                    $aPos
                                                  )}"/>
                                                        </ns1:rowGroup>


                                                        <xsl:for-each select="$REM_SET">
                                                            <xsl:variable name="sPos" select="$ADD_COUNT + position()"/>
                                                            <ns1:row xsi:type="ns1:fullTableRow" ns1:groupId="{concat('gRem_', $aPos)}">
                                                                <ns1:cell xsi:type="ns1:cell" ns1:col_id="fullName">
                                                                    <ns1:cellItem xsi:type="ns1:cellItem">
                                                                        <ns1:item xsi:type="ns1:input" ns1:name="{concat('fullName_', $aPos, '_', $sPos)}">
                                                                            <ns1:value><xsl:value-of select="normalize-space((fullName|fullname|name)[1])"/></ns1:value>
                                                                        </ns1:item>
                                                                    </ns1:cellItem>
                                                                    <ns1:cellItem xsi:type="ns1:cellItem">
                                                                        <ns1:item xsi:type="ns1:input" ns1:name="{concat('capacity_', $aPos, '_', $sPos)}" ns1:inputType="hidden">
                                                                            <ns1:value><xsl:value-of select="normalize-space(capacity)"/></ns1:value>
                                                                        </ns1:item>
                                                                    </ns1:cellItem>
                                                                    <ns1:cellItem xsi:type="ns1:cellItem">
                                                                        <ns1:item xsi:type="ns1:input" ns1:name="{concat('group_', $aPos, '_', $sPos)}" ns1:inputType="hidden">
                                                                            <ns1:value><xsl:value-of select="normalize-space((groupCategory|group)[1])"/></ns1:value>
                                                                        </ns1:item>
                                                                    </ns1:cellItem>
                                                                </ns1:cell>

                                                                <ns1:cell xsi:type="ns1:cell" ns1:col_id="idNumber">
                                                                    <ns1:cellItem xsi:type="ns1:cellItem">
                                                                        <ns1:item xsi:type="ns1:input" ns1:name="{concat('idNumber_', $aPos, '_', $sPos)}">
                                                                            <ns1:value><xsl:value-of select="normalize-space((idNumber|id|identificationNumber)[1])"/></ns1:value>
                                                                        </ns1:item>
                                                                    </ns1:cellItem>
                                                                </ns1:cell>

                                                                <ns1:cell xsi:type="ns1:cell" ns1:col_id="instructions">
                                                                    <!-- hidden original -->
                                                                    <ns1:cellItem xsi:type="ns1:cellItem">
                                                                        <xsl:variable name="curRaw" select="normalize-space(instructions)"/>
                                                                        <xsl:variable name="curUp"  select="translate($curRaw,$LOWER,$UPPER)"/>
                                                                        <xsl:variable name="cur">
                                                                            <xsl:choose>
                                                                                <xsl:when test="$curUp='REMOVE'">Remove</xsl:when>
                                                                                <xsl:otherwise>Add</xsl:otherwise>
                                                                            </xsl:choose>
                                                                        </xsl:variable>
                                                                        <ns1:item xsi:type="ns1:input"
                                                                                  ns1:name="{concat('origInstruction_', $aPos, '_', $sPos)}"
                                                                                  ns1:inputType="hidden">
                                                                            <ns1:value><xsl:value-of select="$cur"/></ns1:value>
                                                                        </ns1:item>
                                                                    </ns1:cellItem>

                                                                    <!-- dropdown -->
                                                                    <ns1:cellItem xsi:type="ns1:cellItem">
                                                                        <xsl:variable name="curRaw2" select="normalize-space(instructions)"/>
                                                                        <xsl:variable name="curUp2"  select="translate($curRaw2,$LOWER,$UPPER)"/>
                                                                        <xsl:variable name="cur2">
                                                                            <xsl:choose>
                                                                                <xsl:when test="$curUp2='REMOVE'">Remove</xsl:when>
                                                                                <xsl:otherwise>Add</xsl:otherwise>
                                                                            </xsl:choose>
                                                                        </xsl:variable>
                                                                        <ns1:item xsi:type="ns1:dropdown"
                                                                                  ns1:id="{concat('instruction_', $aPos, '_', $sPos)}"
                                                                                  ns1:label="Instruction"
                                                                                  ns1:selectedValue="{$cur2}"
                                                                                  ns1:pleaseSelectOptionEnabled="false">
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
                                                                                  ns1:id="{concat('removeSignBtn_', $aPos, '_', $sPos)}"
                                                                                  ns1:type="paper"
                                                                                  ns1:label="Remove"
                                                                                  ns1:formSubmit="true"
                                                                                  ns1:target="main"
                                                                                  ns1:width="2"
                                                                                  ns1:url="{concat(
                                                'app-domain/mandates-and-resolutions/editRequestRemoveSignatory/',
                                                $REQ/*[local-name()='requestId'],
                                                '?removeSignatoryAt=',
                                                $aPos, '_', $sPos
                                              )}"/>
                                                                    </ns1:cellItem>
                                                                </ns1:cell>
                                                            </ns1:row>
                                                        </xsl:for-each>
                                                    </ns1:boxSymbol>
                                                </xsl:if>
                                            </ns1:box>
                                        </ns1:symbol>
                                    </xsl:for-each>
                                </xsl:when>

                                <xsl:otherwise>
                                    <ns1:symbol xsi:type="ns1:textParagraph">
                                        <ns1:value>No account/signatory data found.</ns1:value>
                                    </ns1:symbol>
                                </xsl:otherwise>
                            </xsl:choose>
                        </ns1:sections>
                    </xsl:if>
                    <!-- /Mandates -->

                    <!-- ================= Resolutions: Appointed Directors (editable) ================= -->
                    <xsl:if test="$SHOW_RESOLUTIONS or ($HAS_AUTH &gt; 0)">
                        <ns1:sections ns1:align="left" ns1:width="full">
                            <ns1:symbol xsi:type="ns1:textHeading" ns1:align="left">
                                <ns1:value>Directors Details</ns1:value>
                            </ns1:symbol>
                        </ns1:sections>

                        <ns1:sections ns1:align="left" ns1:width="full">
                            <ns1:symbol xsi:type="ns1:boxContainer" ns1:id="directorsBox">
                                <ns1:box xsi:type="ns1:box">

                                    <ns1:boxSymbol xsi:type="ns1:fullTable"
                                                   ns1:id="directorsTbl"
                                                   ns1:heading=""
                                                   ns1:showTotal="false"
                                                   ns1:headingColor="black">

                                        <ns1:tableColumn ns1:align="left" ns1:fieldName="firstName"   ns1:heading="First Name"   ns1:disableSorting="true"/>
                                        <ns1:tableColumn ns1:align="left" ns1:fieldName="surname"     ns1:heading="Surname"      ns1:disableSorting="true"/>
                                        <ns1:tableColumn ns1:align="left" ns1:fieldName="designation" ns1:heading="Designation"  ns1:disableSorting="true"/>
                                        <ns1:tableColumn ns1:align="left" ns1:fieldName="remove"      ns1:heading="Remove"       ns1:disableSorting="true"/>

                                        <ns1:rowGroup xsi:type="ns1:rowGroup" ns1:groupId="dir_g" ns1:groupHeaderLabel="">
                                            <ns1:totalsRow ns1:category=" ">
                                                <ns1:cell xsi:type="ns1:cell" ns1:col_id="firstName">
                                                    <ns1:cellItem xsi:type="ns1:cellItem">
                                                        <ns1:item xsi:type="ns1:simpleText"><ns1:value/></ns1:item>
                                                    </ns1:cellItem>
                                                </ns1:cell>
                                            </ns1:totalsRow>

                                            <ns1:groupTableButton xsi:type="ns1:imageButton"
                                                                  ns1:id="addDirectorBtn"
                                                                  ns1:label="Add a director"
                                                                  ns1:tip="Add a director"
                                                                  ns1:url="{concat(
                                              'app-domain/mandates-and-resolutions/editRequestAddDirector/',
                                              $REQ/*[local-name()='requestId']
                                            )}"/>
                                        </ns1:rowGroup>

                                        <xsl:choose>
                                            <xsl:when test="count($AUTH) &gt; 0">
                                                <xsl:for-each select="$AUTH">
                                                    <xsl:variable name="i" select="position()"/>
                                                    <xsl:variable name="first"
                                                                  select="normalize-space(string(( *[local-name()='firstName' or local-name()='firstname' or local-name()='name']
                                                                        | .//*[local-name()='firstName' or local-name()='firstname' or local-name()='name'] )[1]))"/>
                                                    <xsl:variable name="last"
                                                                  select="normalize-space(string((
                                                                   *[local-name()='surname' or local-name()='lastName' or local-name()='lastname']
                                                                 | .//*[local-name()='surname' or local-name()='lastName' or local-name()='lastname']
                                                                 )[1]))"/>

                                                    <xsl:variable name="role"
                                                                  select="normalize-space(string((
                                                                           *[local-name()='designation' or local-name()='role' or local-name()='title']
                                                                         | .//*[local-name()='designation' or local-name()='role' or local-name()='title']
                                                                         )[1]))"/>

                                                    <xsl:variable name="idv"
                                                                  select="normalize-space(string((
                                                                           *[local-name()='authorityId' or local-name()='id']
                                                                         | .//*[local-name()='authorityId' or local-name()='id']
                                                                         )[1]))"/>


                                                    <ns1:row xsi:type="ns1:fullTableRow" ns1:groupId="dir_g">
                                                        <ns1:cell xsi:type="ns1:cell" ns1:col_id="firstName">
                                                            <ns1:cellItem xsi:type="ns1:cellItem">
                                                                <ns1:item xsi:type="ns1:input" ns1:name="{concat('dirFirstName_', $i)}">
                                                                    <ns1:value><xsl:value-of select="$first"/></ns1:value>
                                                                </ns1:item>
                                                            </ns1:cellItem>
                                                            <ns1:cellItem xsi:type="ns1:cellItem">
                                                                <ns1:item xsi:type="ns1:input" ns1:name="{concat('dirId_', $i)}" ns1:inputType="hidden">
                                                                    <ns1:value><xsl:value-of select="$idv"/></ns1:value>
                                                                </ns1:item>
                                                            </ns1:cellItem>
                                                        </ns1:cell>

                                                        <ns1:cell xsi:type="ns1:cell" ns1:col_id="surname">
                                                            <ns1:cellItem xsi:type="ns1:cellItem">
                                                                <ns1:item xsi:type="ns1:input" ns1:name="{concat('dirSurname_', $i)}">
                                                                    <ns1:value><xsl:value-of select="$last"/></ns1:value>
                                                                </ns1:item>
                                                            </ns1:cellItem>
                                                        </ns1:cell>

                                                        <ns1:cell xsi:type="ns1:cell" ns1:col_id="designation">
                                                            <ns1:cellItem xsi:type="ns1:cellItem">
                                                                <ns1:item xsi:type="ns1:input" ns1:name="{concat('dirDesignation_', $i)}">
                                                                    <ns1:value><xsl:value-of select="$role"/></ns1:value>
                                                                </ns1:item>
                                                            </ns1:cellItem>
                                                        </ns1:cell>

                                                        <ns1:cell xsi:type="ns1:cell" ns1:col_id="remove">
                                                            <ns1:cellItem xsi:type="ns1:cellItem">
                                                                <ns1:item xsi:type="ns1:button"
                                                                          ns1:id="{concat('removeDirectorBtn_', $i)}"
                                                                          ns1:type="paper"
                                                                          ns1:label="Remove"
                                                                          ns1:formSubmit="true"
                                                                          ns1:target="main"
                                                                          ns1:width="2"
                                                                          ns1:url="{concat(
                                            'app-domain/mandates-and-resolutions/editRequestRemoveDirector/',
                                            $REQ/*[local-name()='requestId'],
                                            '?removeDirectorAt=',
                                            $i
                                          )}"/>
                                                            </ns1:cellItem>
                                                        </ns1:cell>
                                                    </ns1:row>
                                                </xsl:for-each>
                                            </xsl:when>

                                            <xsl:otherwise>
                                                <ns1:row xsi:type="ns1:fullTableRow" ns1:groupId="dir_g">
                                                    <ns1:cell xsi:type="ns1:cell" ns1:col_id="firstName">
                                                        <ns1:cellItem xsi:type="ns1:cellItem">
                                                            <ns1:item xsi:type="ns1:simpleText"><ns1:value>None captured</ns1:value></ns1:item>
                                                        </ns1:cellItem>
                                                    </ns1:cell>
                                                    <ns1:cell xsi:type="ns1:cell" ns1:col_id="surname">
                                                        <ns1:cellItem xsi:type="ns1:cellItem">
                                                            <ns1:item xsi:type="ns1:simpleText"><ns1:value>-</ns1:value></ns1:item>
                                                        </ns1:cellItem>
                                                    </ns1:cell>
                                                    <ns1:cell xsi:type="ns1:cell" ns1:col_id="designation">
                                                        <ns1:cellItem xsi:type="ns1:cellItem">
                                                            <ns1:item xsi:type="ns1:simpleText"><ns1:value>-</ns1:value></ns1:item>
                                                        </ns1:cellItem>
                                                    </ns1:cell>
                                                    <ns1:cell xsi:type="ns1:cell" ns1:col_id="remove">
                                                        <ns1:cellItem xsi:type="ns1:cellItem">
                                                            <ns1:item xsi:type="ns1:simpleText"><ns1:value/></ns1:item>
                                                        </ns1:cellItem>
                                                    </ns1:cell>
                                                </ns1:row>
                                            </xsl:otherwise>
                                        </xsl:choose>

                                    </ns1:boxSymbol>

                                </ns1:box>
                            </ns1:symbol>
                        </ns1:sections>
                    </xsl:if>
                    <!-- /Resolutions -->

                </ns1:form>
            </symbol>

            <!-- footer -->
            <symbol xsi:type="ns1:footer" ns1:text="" ns1:textAlign="left" ns1:buttonAlign="right">
                <ns1:baseButton ns1:id="addAccountBtnFooter"
                                ns1:url="{concat('app-domain/mandates-and-resolutions/editRequestAddAccount/', $REQ/*[local-name()='requestId'])}"
                                ns1:label="Add Account"
                                ns1:formSubmit="true"
                                ns1:target="main"/>

                <ns1:baseButton ns1:id="back"
                                ns1:url="app-domain/mandates-and-resolutions/requestTable"
                                ns1:target="main"
                                ns1:formSubmit="false"
                                ns1:label="Back"/>

                <ns1:baseButton ns1:id="save"
                                ns1:url="{concat('app-domain/mandates-and-resolutions/editRequestSave/', $REQ/*[local-name()='requestId'])}"
                                ns1:target="main"
                                ns1:formSubmit="true"
                                ns1:label="Save"/>
            </symbol>
        </page>
    </xsl:template>
</xsl:stylesheet>