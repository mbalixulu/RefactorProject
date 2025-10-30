<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:ns1="http://ws.online.fnb.co.za/v1/common/"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">

    <xsl:output method="xml" indent="yes"/>
    <xsl:strip-space elements="*"/>

    <!-- ===== Helpers ===== -->
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
    <!-- Prefer REQ1 when it exists; otherwise fall back to REQ2 -->
    <xsl:variable name="REQ" select="($REQ1 | $REQ2[not($REQ1)])[1]"/>

    <!-- Request type flags -->
    <xsl:variable name="TYPE_RAW" select="normalize-space($REQ/*[local-name()='type'])"/>
    <xsl:variable name="TYPE_UP"  select="translate($TYPE_RAW, $LOWER, $UPPER)"/>
    <xsl:variable name="IS_MANDATES"     select="contains($TYPE_UP,'MANDATE')"/>
    <xsl:variable name="IS_RESOLUTIONS"  select="contains($TYPE_UP,'RESOLUTION')"/>
    <xsl:variable name="IS_BOTH"         select="contains($TYPE_UP,'BOTH')"/>
    <xsl:variable name="SHOW_MANDATES"    select="$IS_MANDATES or $IS_BOTH"/>
    <xsl:variable name="SHOW_RESOLUTIONS" select="$IS_RESOLUTIONS or $IS_BOTH"/>

    <!-- ===== Key for grouping signatories by (accountNumber|accountName) ===== -->
    <xsl:key name="kAcctBySig"
             match="signatories/*"
             use="concat(
             normalize-space(string((accountNumber|accountNo)[1])),
             '|',
             normalize-space(string((accountName|name)[1]))
           )"/>

    <!-- ===== Director/Authority sources (robust) ===== -->
    <!-- Under the request (preferred) -->
    <xsl:variable name="DIR_FROM_REQ"  select="$REQ//*[local-name()='directors']/*[local-name()='director']"/>
    <xsl:variable name="AUTH_FROM_REQ" select="$REQ//*[local-name()='authorities']/*[local-name()='authority']"/>
    <!-- Anywhere in the document (unscoped) -->
    <xsl:variable name="DIR_ANY_UNSCOPED"
                  select="//*[local-name()='directors']/*[local-name()='director']
                        | //*[local-name()='authorities']/*[local-name()='authority']"/>
    <!-- Last-resort: any node that looks like a director -->
    <xsl:variable name="LOOKS_LIKE_DIR"
                  select="//*[
                  ( *[local-name()='firstname' or local-name()='firstName' or local-name()='name'] )
                  and ( *[local-name()='surname' or local-name()='lastName' or local-name()='lastname'] )
                  and ( *[local-name()='designation' or local-name()='role' or local-name()='title'] )
                ]"/>
    <!-- Prefer request-scoped; else unscoped; else heuristic -->
    <xsl:variable name="DIR_SRC"
                  select="($DIR_FROM_REQ | $AUTH_FROM_REQ)
                        | ($DIR_ANY_UNSCOPED[count($DIR_FROM_REQ | $AUTH_FROM_REQ)=0])
                        | ($LOOKS_LIKE_DIR[count($DIR_FROM_REQ | $AUTH_FROM_REQ | $DIR_ANY_UNSCOPED)=0])"/>

    <xsl:template match="/">
        <page xmlns:ns1="http://ws.online.fnb.co.za/v1/common/"
              xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
              id="viewRequest" title="View Request" template="main" version="1">

            <symbol xsi:type="ns1:subTabGroup" ns1:subTabGroupHeading="Mandates and resolution"/>

            <symbol xsi:type="ns1:formLayout">
                <ns1:form ns1:action="http://localhost:8080/bifrost/" ns1:name="salesForm">

                    <!-- ===== Header ===== -->
                    <ns1:sections ns1:align="left" ns1:width="full">
                        <ns1:symbol xsi:type="ns1:textHeading" ns1:align="left">
                            <ns1:value>View Request</ns1:value>
                        </ns1:symbol>
                    </ns1:sections>

                    <ns1:sections ns1:align="left" ns1:width="full">
                        <ns1:symbol xsi:type="ns1:boxContainer" ns1:id="headerBox">
                            <ns1:box xsi:type="ns1:box">
                                <ns1:boxSymbol xsi:type="ns1:boxSplit" ns1:width="50">
                                    <ns1:boxSplitSymbol xsi:type="ns1:textReadout" ns1:subHeading="Request ID" ns1:headingColor="ghostmedium">
                                        <ns1:value>
                                            <xsl:choose>
                                                <xsl:when test="string-length(normalize-space($REQ/*[local-name()='requestIdForDisplay'])) &gt; 0">
                                                    <xsl:value-of select="$REQ/*[local-name()='requestIdForDisplay']"/>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    <xsl:value-of select="$REQ/*[local-name()='requestId']"/>
                                                </xsl:otherwise>
                                            </xsl:choose>
                                        </ns1:value>
                                    </ns1:boxSplitSymbol>
                                    <ns1:boxSplitSymbol xsi:type="ns1:textReadout" ns1:subHeading="Process ID" ns1:headingColor="ghostmedium">
                                        <ns1:value><xsl:value-of select="$REQ/*[local-name()='processId']"/></ns1:value>
                                    </ns1:boxSplitSymbol>
                                    <ns1:boxSplitSymbol xsi:type="ns1:textReadout" ns1:subHeading="Request Type" ns1:headingColor="ghostmedium">
                                        <ns1:value><xsl:value-of select="$REQ/*[local-name()='type']"/></ns1:value>
                                    </ns1:boxSplitSymbol>
                                    <ns1:boxSplitSymbol xsi:type="ns1:textReadout" ns1:subHeading="Status" ns1:headingColor="ghostmedium">
                                        <ns1:value><xsl:value-of select="$REQ/*[local-name()='status']"/></ns1:value>
                                    </ns1:boxSplitSymbol>
                                    <ns1:boxSplitSymbol xsi:type="ns1:textReadout" ns1:subHeading="Sub Status" ns1:headingColor="ghostmedium">
                                        <ns1:value><xsl:value-of select="$REQ/*[local-name()='subStatus']"/></ns1:value>
                                    </ns1:boxSplitSymbol>
                                    <ns1:boxSplitSymbol xsi:type="ns1:textReadout" ns1:subHeading="Assigned User" ns1:headingColor="ghostmedium">
                                        <ns1:value><xsl:value-of select="$REQ/*[local-name()='assignedUser']"/></ns1:value>
                                    </ns1:boxSplitSymbol>
                                </ns1:boxSymbol>

                                <ns1:boxSymbol xsi:type="ns1:boxSplit" ns1:width="50">
                                    <ns1:boxSplitSymbol xsi:type="ns1:textReadout" ns1:subHeading="Company Name" ns1:headingColor="ghostmedium">
                                        <ns1:value><xsl:value-of select="$REQ/*[local-name()='companyName']"/></ns1:value>
                                    </ns1:boxSplitSymbol>
                                    <ns1:boxSplitSymbol xsi:type="ns1:textReadout" ns1:subHeading="SLA (days)" ns1:headingColor="ghostmedium">
                                        <ns1:value><xsl:value-of select="$REQ/*[local-name()='sla']"/></ns1:value>
                                    </ns1:boxSplitSymbol>
                                    <ns1:boxSplitSymbol xsi:type="ns1:textReadout" ns1:subHeading="Last Modified Date" ns1:headingColor="ghostmedium">
                                        <ns1:value><xsl:value-of select="$REQ/*[local-name()='updated']"/></ns1:value>
                                    </ns1:boxSplitSymbol>
                                    <ns1:boxSplitSymbol xsi:type="ns1:textReadout" ns1:subHeading="Last Modified By" ns1:headingColor="ghostmedium">
                                        <ns1:value><xsl:value-of select="$REQ/*[local-name()='updator']"/></ns1:value>
                                    </ns1:boxSplitSymbol>
                                    <ns1:boxSplitSymbol xsi:type="ns1:textReadout" ns1:subHeading="Created Date" ns1:headingColor="ghostmedium">
                                        <ns1:value><xsl:value-of select="$REQ/*[local-name()='created']"/></ns1:value>
                                    </ns1:boxSplitSymbol>
                                    <ns1:boxSplitSymbol xsi:type="ns1:textReadout" ns1:subHeading="Created By" ns1:headingColor="ghostmedium">
                                        <ns1:value><xsl:value-of select="$REQ/*[local-name()='creator']"/></ns1:value>
                                    </ns1:boxSplitSymbol>
                                </ns1:boxSymbol>
                            </ns1:box>
                        </ns1:symbol>
                    </ns1:sections>

                    <!-- ================= Mandates ================= -->
                    <!-- (UNCHANGED FROM YOUR LAST WORKING COPY) -->
                    <xsl:if test="$SHOW_MANDATES">
                        <ns1:sections ns1:align="left" ns1:width="full">
                            <ns1:symbol xsi:type="ns1:textHeading" ns1:align="left">
                                <ns1:value>Account Signatories</ns1:value>
                            </ns1:symbol>
                        </ns1:sections>

                        <ns1:sections ns1:align="left" ns1:width="full">
                            <xsl:variable name="ALL_SIGS" select="$REQ//*[local-name()='signatories']/*"/>

                            <xsl:choose>
                                <xsl:when test="count($ALL_SIGS) &gt; 0">
                                    <!-- Distinct accounts by (number|name) -->
                                    <xsl:for-each select="$ALL_SIGS[
                    generate-id() =
                    generate-id(
                      key('kAcctBySig',
                        concat(
                          normalize-space(string((accountNumber|accountNo)[1])),
                          '|',
                          normalize-space(string((accountName|name)[1]))
                        )
                      )[1]
                    )
                  ]">
                                        <xsl:variable name="pos" select="position()"/>
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

                                        <ns1:symbol xsi:type="ns1:boxContainer" ns1:id="{concat('acc_', $pos)}">
                                            <ns1:box xsi:type="ns1:box">

                                                <ns1:boxSymbol xsi:type="ns1:textHeading" ns1:size="4">
                                                    <ns1:value>
                                                        <xsl:value-of select="$accNameEff"/>
                                                        <xsl:text> (</xsl:text><xsl:value-of select="$accNoEff"/><xsl:text>)</xsl:text>
                                                    </ns1:value>
                                                </ns1:boxSymbol>

                                                <!-- ===== Added Signatories ===== -->
                                                <ns1:boxSymbol xsi:type="ns1:textHeading" ns1:size="5">
                                                    <ns1:value>Added Signatories</ns1:value>
                                                </ns1:boxSymbol>

                                                <ns1:boxSymbol xsi:type="ns1:fullTable"
                                                               ns1:id="{concat('sigTableAdd_', $pos)}"
                                                               ns1:heading="" ns1:showTotal="false" ns1:headingColor="black">
                                                    <ns1:tableColumn ns1:align="left" ns1:fieldName="fullName"     ns1:heading="Full Name"    ns1:disableSorting="true"/>
                                                    <ns1:tableColumn ns1:align="left" ns1:fieldName="idNumber"     ns1:heading="ID Number"    ns1:disableSorting="true"/>
                                                    <ns1:tableColumn ns1:align="left" ns1:fieldName="capacity"     ns1:heading="Capacity"     ns1:disableSorting="true"/>
                                                    <ns1:tableColumn ns1:align="left" ns1:fieldName="group"        ns1:heading="Group"        ns1:disableSorting="true"/>
                                                    <ns1:tableColumn ns1:align="left" ns1:fieldName="instructions" ns1:heading="Instruction"  ns1:disableSorting="true"/>

                                                    <ns1:rowGroup xsi:type="ns1:rowGroup" ns1:groupId="{concat('gAdd_', $pos)}" ns1:groupHeaderLabel="">
                                                        <ns1:totalsRow ns1:category=" ">
                                                            <ns1:cell xsi:type="ns1:cell" ns1:col_id="fullName">
                                                                <ns1:cellItem xsi:type="ns1:cellItem">
                                                                    <ns1:item xsi:type="ns1:simpleText"><ns1:value/></ns1:item>
                                                                </ns1:cellItem>
                                                            </ns1:cell>
                                                        </ns1:totalsRow>
                                                    </ns1:rowGroup>

                                                    <xsl:choose>
                                                        <xsl:when test="count($SIGS_THIS[translate(normalize-space((instructions|instruction)[1]), $LOWER, $UPPER)='ADD']) &gt; 0">
                                                            <xsl:for-each select="$SIGS_THIS[translate(normalize-space((instructions|instruction)[1]), $LOWER, $UPPER)='ADD']">
                                                                <ns1:row xsi:type="ns1:fullTableRow" ns1:groupId="{concat('gAdd_', $pos)}">
                                                                    <ns1:cell xsi:type="ns1:cell" ns1:col_id="fullName">
                                                                        <ns1:cellItem xsi:type="ns1:cellItem">
                                                                            <ns1:item xsi:type="ns1:simpleText"><ns1:value><xsl:value-of select="(fullName|fullname|name)[1]"/></ns1:value></ns1:item>
                                                                        </ns1:cellItem>
                                                                    </ns1:cell>
                                                                    <ns1:cell xsi:type="ns1:cell" ns1:col_id="idNumber">
                                                                        <ns1:cellItem xsi:type="ns1:cellItem">
                                                                            <ns1:item xsi:type="ns1:simpleText"><ns1:value><xsl:value-of select="(idNumber|id|identificationNumber)[1]"/></ns1:value></ns1:item>
                                                                        </ns1:cellItem>
                                                                    </ns1:cell>
                                                                    <ns1:cell xsi:type="ns1:cell" ns1:col_id="capacity">
                                                                        <ns1:cellItem xsi:type="ns1:cellItem">
                                                                            <ns1:item xsi:type="ns1:simpleText">
                                                                                <ns1:value><xsl:value-of select="(*[local-name()='capacity' or local-name()='signatoryCapacity'])[1]"/></ns1:value>
                                                                            </ns1:item>
                                                                        </ns1:cellItem>
                                                                    </ns1:cell>
                                                                    <ns1:cell xsi:type="ns1:cell" ns1:col_id="group">
                                                                        <ns1:cellItem xsi:type="ns1:cellItem">
                                                                            <ns1:item xsi:type="ns1:simpleText">
                                                                                <ns1:value><xsl:value-of select="(*[local-name()='group' or local-name()='groupCategory' or local-name()='groupName' or local-name()='groupname' or local-name()='groupcategory'])[1]"/></ns1:value>
                                                                            </ns1:item>
                                                                        </ns1:cellItem>
                                                                    </ns1:cell>
                                                                    <ns1:cell xsi:type="ns1:cell" ns1:col_id="instructions">
                                                                        <ns1:cellItem xsi:type="ns1:cellItem">
                                                                            <ns1:item xsi:type="ns1:simpleText"><ns1:value><xsl:value-of select="(instructions|instruction)[1]"/></ns1:value></ns1:item>
                                                                        </ns1:cellItem>
                                                                    </ns1:cell>
                                                                </ns1:row>
                                                            </xsl:for-each>
                                                        </xsl:when>
                                                        <xsl:otherwise>
                                                            <ns1:row xsi:type="ns1:fullTableRow" ns1:groupId="{concat('gAdd_', $pos)}">
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
                                                                <ns1:cell xsi:type="ns1:cell" ns1:col_id="capacity">
                                                                    <ns1:cellItem xsi:type="ns1:cellItem">
                                                                        <ns1:item xsi:type="ns1:simpleText"><ns1:value>-</ns1:value></ns1:item>
                                                                    </ns1:cellItem>
                                                                </ns1:cell>
                                                                <ns1:cell xsi:type="ns1:cell" ns1:col_id="group">
                                                                    <ns1:cellItem xsi:type="ns1:cellItem">
                                                                        <ns1:item xsi:type="ns1:simpleText"><ns1:value>-</ns1:value></ns1:item>
                                                                    </ns1:cellItem>
                                                                </ns1:cell>
                                                                <ns1:cell xsi:type="ns1:cell" ns1:col_id="instructions">
                                                                    <ns1:cellItem xsi:type="ns1:cellItem">
                                                                        <ns1:item xsi:type="ns1:simpleText"><ns1:value>-</ns1:value></ns1:item>
                                                                    </ns1:cellItem>
                                                                </ns1:cell>
                                                            </ns1:row>
                                                        </xsl:otherwise>
                                                    </xsl:choose>
                                                </ns1:boxSymbol>

                                                <!-- ===== Removed Signatories ===== -->
                                                <xsl:if test="count($SIGS_THIS[translate(normalize-space((instructions|instruction)[1]), $LOWER, $UPPER)='REMOVE']) &gt; 0">
                                                    <ns1:boxSymbol xsi:type="ns1:textHeading" ns1:size="5">
                                                        <ns1:value>Removed Signatories</ns1:value>
                                                    </ns1:boxSymbol>

                                                    <ns1:boxSymbol xsi:type="ns1:fullTable"
                                                                   ns1:id="{concat('sigTableRem_', $pos)}"
                                                                   ns1:heading="" ns1:showTotal="false" ns1:headingColor="black">
                                                        <ns1:tableColumn ns1:align="left" ns1:fieldName="fullName"     ns1:heading="Full Name"    ns1:disableSorting="true"/>
                                                        <ns1:tableColumn ns1:align="left" ns1:fieldName="idNumber"     ns1:heading="ID Number"    ns1:disableSorting="true"/>
                                                        <!--                                                        <ns1:tableColumn ns1:align="left" ns1:fieldName="capacity"     ns1:heading="Capacity"     ns1:disableSorting="true"/>-->
                                                        <!--                                                        <ns1:tableColumn ns1:align="left" ns1:fieldName="group"        ns1:heading="Group"        ns1:disableSorting="true"/>-->
                                                        <ns1:tableColumn ns1:align="left" ns1:fieldName="instructions" ns1:heading="Instruction"  ns1:disableSorting="true"/>

                                                        <ns1:rowGroup xsi:type="ns1:rowGroup" ns1:groupId="{concat('gRem_', $pos)}" ns1:groupHeaderLabel="">
                                                            <ns1:totalsRow ns1:category=" ">
                                                                <ns1:cell xsi:type="ns1:cell" ns1:col_id="fullName">
                                                                    <ns1:cellItem xsi:type="ns1:cellItem">
                                                                        <ns1:item xsi:type="ns1:simpleText"><ns1:value/></ns1:item>
                                                                    </ns1:cellItem>
                                                                </ns1:cell>
                                                            </ns1:totalsRow>
                                                        </ns1:rowGroup>

                                                        <xsl:for-each select="$SIGS_THIS[translate(normalize-space((instructions|instruction)[1]), $LOWER, $UPPER)='REMOVE']">
                                                            <ns1:row xsi:type="ns1:fullTableRow" ns1:groupId="{concat('gRem_', $pos)}">
                                                                <ns1:cell xsi:type="ns1:cell" ns1:col_id="fullName">
                                                                    <ns1:cellItem xsi:type="ns1:cellItem">
                                                                        <ns1:item xsi:type="ns1:simpleText"><ns1:value><xsl:value-of select="(fullName|fullname|name)[1]"/></ns1:value></ns1:item>
                                                                    </ns1:cellItem>
                                                                </ns1:cell>
                                                                <ns1:cell xsi:type="ns1:cell" ns1:col_id="idNumber">
                                                                    <ns1:cellItem xsi:type="ns1:cellItem">
                                                                        <ns1:item xsi:type="ns1:simpleText"><ns1:value><xsl:value-of select="(idNumber|id|identificationNumber)[1]"/></ns1:value></ns1:item>
                                                                    </ns1:cellItem>
                                                                </ns1:cell>
                                                                <!--                                                                <ns1:cell xsi:type="ns1:cell" ns1:col_id="capacity">-->
                                                                <!--                                                                    <ns1:cellItem xsi:type="ns1:cellItem">-->
                                                                <!--                                                                        <ns1:item xsi:type="ns1:simpleText">-->
                                                                <!--                                                                            <ns1:value><xsl:value-of select="(*[local-name()='capacity' or local-name()='signatoryCapacity'])[1]"/></ns1:value>-->
                                                                <!--                                                                        </ns1:item>-->
                                                                <!--                                                                    </ns1:cellItem>-->
                                                                <!--                                                                </ns1:cell>-->
                                                                <!--                                                                <ns1:cell xsi:type="ns1:cell" ns1:col_id="group">-->
                                                                <!--                                                                    <ns1:cellItem xsi:type="ns1:cellItem">-->
                                                                <!--                                                                        <ns1:item xsi:type="ns1:simpleText">-->
                                                                <!--                                                                            <ns1:value><xsl:value-of select="(*[local-name()='group' or local-name()='groupCategory' or local-name()='groupName' or local-name()='groupname' or local-name()='groupcategory'])[1]"/></ns1:value>-->
                                                                <!--                                                                        </ns1:item>-->
                                                                <!--                                                                    </ns1:cellItem>-->
                                                                <!--                                                                </ns1:cell>-->
                                                                <ns1:cell xsi:type="ns1:cell" ns1:col_id="instructions">
                                                                    <ns1:cellItem xsi:type="ns1:cellItem">
                                                                        <ns1:item xsi:type="ns1:simpleText"><ns1:value><xsl:value-of select="(instructions|instruction)[1]"/></ns1:value></ns1:item>
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

                    <!-- ================= Resolutions: Appointed Directors (Added/Removed + Instruction) ================= -->
                    <xsl:if test="$SHOW_RESOLUTIONS">
                        <xsl:variable name="D" select="$DIR_SRC"/>

                        <!-- Build 'removed' set -->
                        <xsl:variable name="REM_SET"
                                      select="$D[
                                          contains(
                                            translate(
                                              normalize-space(string((
                                                *[local-name()='instructions' or local-name()='instruction' or local-name()='action'
                                                  or local-name()='change' or local-name()='changeType' or local-name()='status'][1]
                                                | @instructions | @instruction | @action | @change | @changeType | @status
                                              ))),
                                              $LOWER, $UPPER
                                            ),
                                            'REMOVE'
                                          )
                                          or translate(normalize-space(string(( *[local-name()='isActive'][1] | @isActive ))), $LOWER, $UPPER) = 'FALSE'
                                          or normalize-space(string(( *[local-name()='isActive'][1] | @isActive ))) = '0'
                                        ]"/>

                        <ns1:sections ns1:align="left" ns1:width="full">
                            <ns1:symbol xsi:type="ns1:textHeading" ns1:align="left">
                                <ns1:value>Appointed Directors</ns1:value>
                            </ns1:symbol>
                        </ns1:sections>

                        <ns1:sections ns1:align="left" ns1:width="full">
                            <ns1:symbol xsi:type="ns1:boxContainer" ns1:id="directorsBox">
                                <ns1:box xsi:type="ns1:box">

                                    <!-- Added Directors -->
                                    <ns1:boxSymbol xsi:type="ns1:textHeading" ns1:size="5">
                                        <ns1:value>Added Directors</ns1:value>
                                    </ns1:boxSymbol>

                                    <ns1:boxSymbol xsi:type="ns1:fullTable" ns1:id="dirAddTbl"
                                                   ns1:heading="" ns1:showTotal="false" ns1:headingColor="black">
                                        <ns1:tableColumn ns1:align="left" ns1:fieldName="firstName"   ns1:heading="First Name"   ns1:disableSorting="true"/>
                                        <ns1:tableColumn ns1:align="left" ns1:fieldName="surname"     ns1:heading="Surname"      ns1:disableSorting="true"/>
                                        <ns1:tableColumn ns1:align="left" ns1:fieldName="designation" ns1:heading="Designation"  ns1:disableSorting="true"/>
                                        <ns1:tableColumn ns1:align="left" ns1:fieldName="instruction" ns1:heading="Instruction"  ns1:disableSorting="true"/>

                                        <ns1:rowGroup xsi:type="ns1:rowGroup" ns1:groupId="dir_add_g" ns1:groupHeaderLabel="">
                                            <ns1:totalsRow ns1:category=" ">
                                                <ns1:cell xsi:type="ns1:cell" ns1:col_id="firstName">
                                                    <ns1:cellItem xsi:type="ns1:cellItem">
                                                        <ns1:item xsi:type="ns1:simpleText"><ns1:value/></ns1:item>
                                                    </ns1:cellItem>
                                                </ns1:cell>
                                            </ns1:totalsRow>
                                        </ns1:rowGroup>

                                        <!-- Inline the predicate so it's evaluated per node -->
                                        <xsl:choose>
                                            <xsl:when test="count($D[
                    not(contains(
                      translate(
                        normalize-space(string((
                          *[local-name()='instructions' or local-name()='instruction' or local-name()='action'
                            or local-name()='change' or local-name()='changeType' or local-name()='status'][1]
                          | @instructions | @instruction | @action | @change | @changeType | @status
                        ))),
                        $LOWER, $UPPER
                      ),
                      'REMOVE'
                    ))
                    and
                    not( translate(normalize-space(string(( *[local-name()='isActive'][1] | @isActive ))), $LOWER, $UPPER) = 'FALSE'
                         or normalize-space(string(( *[local-name()='isActive'][1] | @isActive ))) = '0' )
                  ]) &gt; 0">

                                                <xsl:for-each select="$D[
                    not(contains(
                      translate(
                        normalize-space(string((
                          *[local-name()='instructions' or local-name()='instruction' or local-name()='action'
                            or local-name()='change' or local-name()='changeType' or local-name()='status'][1]
                          | @instructions | @instruction | @action | @change | @changeType | @status
                        ))),
                        $LOWER, $UPPER
                      ),
                      'REMOVE'
                    ))
                    and
                    not( translate(normalize-space(string(( *[local-name()='isActive'][1] | @isActive ))), $LOWER, $UPPER) = 'FALSE'
                         or normalize-space(string(( *[local-name()='isActive'][1] | @isActive ))) = '0' )
                  ]">
                                                    <ns1:row xsi:type="ns1:fullTableRow" ns1:groupId="dir_add_g">
                                                        <ns1:cell xsi:type="ns1:cell" ns1:col_id="firstName">
                                                            <ns1:cellItem xsi:type="ns1:cellItem">
                                                                <ns1:item xsi:type="ns1:simpleText"><ns1:value><xsl:value-of select="( *[local-name()='firstName'] | *[local-name()='firstname'] | *[local-name()='name'] )[1]"/></ns1:value></ns1:item>
                                                            </ns1:cellItem>
                                                        </ns1:cell>
                                                        <ns1:cell xsi:type="ns1:cell" ns1:col_id="surname">
                                                            <ns1:cellItem xsi:type="ns1:cellItem">
                                                                <ns1:item xsi:type="ns1:simpleText"><ns1:value><xsl:value-of select="( *[local-name()='surname'] | *[local-name()='lastName'] | *[local-name()='lastname'] )[1]"/></ns1:value></ns1:item>
                                                            </ns1:cellItem>
                                                        </ns1:cell>
                                                        <ns1:cell xsi:type="ns1:cell" ns1:col_id="designation">
                                                            <ns1:cellItem xsi:type="ns1:cellItem">
                                                                <ns1:item xsi:type="ns1:simpleText"><ns1:value><xsl:value-of select="( *[local-name()='designation'] | *[local-name()='role'] | *[local-name()='title'] )[1]"/></ns1:value></ns1:item>
                                                            </ns1:cellItem>
                                                        </ns1:cell>
                                                        <ns1:cell xsi:type="ns1:cell" ns1:col_id="instruction">
                                                            <ns1:cellItem xsi:type="ns1:cellItem">
                                                                <ns1:item xsi:type="ns1:simpleText">
                                                                    <ns1:value>
                                                                        <xsl:variable name="instRaw" select="normalize-space(string((
                            *[local-name()='instructions' or local-name()='instruction' or local-name()='action'
                              or local-name()='change' or local-name()='changeType' or local-name()='status'][1]
                            | @instructions | @instruction | @action | @change | @changeType | @status
                          )))"/>
                                                                        <xsl:choose>
                                                                            <xsl:when test="string-length($instRaw) &gt; 0"><xsl:value-of select="$instRaw"/></xsl:when>
                                                                            <xsl:otherwise>Add</xsl:otherwise>
                                                                        </xsl:choose>
                                                                    </ns1:value>
                                                                </ns1:item>
                                                            </ns1:cellItem>
                                                        </ns1:cell>
                                                    </ns1:row>
                                                </xsl:for-each>

                                            </xsl:when>
                                            <xsl:otherwise>
                                                <ns1:row xsi:type="ns1:fullTableRow" ns1:groupId="dir_add_g">
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
                                                    <ns1:cell xsi:type="ns1:cell" ns1:col_id="instruction">
                                                        <ns1:cellItem xsi:type="ns1:cellItem">
                                                            <ns1:item xsi:type="ns1:simpleText"><ns1:value>-</ns1:value></ns1:item>
                                                        </ns1:cellItem>
                                                    </ns1:cell>
                                                </ns1:row>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </ns1:boxSymbol>

                                    <!-- Removed Directors -->
                                    <xsl:if test="count($REM_SET) &gt; 0">
                                        <ns1:boxSymbol xsi:type="ns1:textHeading" ns1:size="5">
                                            <ns1:value>Removed Directors</ns1:value>
                                        </ns1:boxSymbol>

                                        <ns1:boxSymbol xsi:type="ns1:fullTable" ns1:id="dirRemTbl"
                                                       ns1:heading="" ns1:showTotal="false" ns1:headingColor="black">
                                            <ns1:tableColumn ns1:align="left" ns1:fieldName="firstName"   ns1:heading="First Name"   ns1:disableSorting="true"/>
                                            <ns1:tableColumn ns1:align="left" ns1:fieldName="surname"     ns1:heading="Surname"      ns1:disableSorting="true"/>
                                            <ns1:tableColumn ns1:align="left" ns1:fieldName="designation" ns1:heading="Designation"  ns1:disableSorting="true"/>
                                            <ns1:tableColumn ns1:align="left" ns1:fieldName="instruction" ns1:heading="Instruction"  ns1:disableSorting="true"/>

                                            <ns1:rowGroup xsi:type="ns1:rowGroup" ns1:groupId="dir_rem_g" ns1:groupHeaderLabel="">
                                                <ns1:totalsRow ns1:category=" ">
                                                    <ns1:cell xsi:type="ns1:cell" ns1:col_id="firstName">
                                                        <ns1:cellItem xsi:type="ns1:cellItem">
                                                            <ns1:item xsi:type="ns1:simpleText"><ns1:value/></ns1:item>
                                                        </ns1:cellItem>
                                                    </ns1:cell>
                                                </ns1:totalsRow>
                                            </ns1:rowGroup>

                                            <xsl:for-each select="$REM_SET">
                                                <ns1:row xsi:type="ns1:fullTableRow" ns1:groupId="dir_rem_g">
                                                    <ns1:cell xsi:type="ns1:cell" ns1:col_id="firstName">
                                                        <ns1:cellItem xsi:type="ns1:cellItem">
                                                            <ns1:item xsi:type="ns1:simpleText"><ns1:value><xsl:value-of select="( *[local-name()='firstName'] | *[local-name()='firstname'] | *[local-name()='name'] )[1]"/></ns1:value></ns1:item>
                                                        </ns1:cellItem>
                                                    </ns1:cell>
                                                    <ns1:cell xsi:type="ns1:cell" ns1:col_id="surname">
                                                        <ns1:cellItem xsi:type="ns1:cellItem">
                                                            <ns1:item xsi:type="ns1:simpleText"><ns1:value><xsl:value-of select="( *[local-name()='surname'] | *[local-name()='lastName'] | *[local-name()='lastname'] )[1]"/></ns1:value></ns1:item>
                                                        </ns1:cellItem>
                                                    </ns1:cell>
                                                    <ns1:cell xsi:type="ns1:cell" ns1:col_id="designation">
                                                        <ns1:cellItem xsi:type="ns1:cellItem">
                                                            <ns1:item xsi:type="ns1:simpleText"><ns1:value><xsl:value-of select="( *[local-name()='designation'] | *[local-name()='role'] | *[local-name()='title'] )[1]"/></ns1:value></ns1:item>
                                                        </ns1:cellItem>
                                                    </ns1:cell>
                                                    <ns1:cell xsi:type="ns1:cell" ns1:col_id="instruction">
                                                        <ns1:cellItem xsi:type="ns1:cellItem">
                                                            <ns1:item xsi:type="ns1:simpleText">
                                                                <ns1:value>
                                                                    <xsl:variable name="instRaw" select="normalize-space(string((
                          *[local-name()='instructions' or local-name()='instruction' or local-name()='action'
                            or local-name()='change' or local-name()='changeType' or local-name()='status'][1]
                          | @instructions | @instruction | @action | @change | @changeType | @status
                        )))"/>
                                                                    <xsl:choose>
                                                                        <xsl:when test="string-length($instRaw) &gt; 0"><xsl:value-of select="$instRaw"/></xsl:when>
                                                                        <xsl:otherwise>Remove</xsl:otherwise>
                                                                    </xsl:choose>
                                                                </ns1:value>
                                                            </ns1:item>
                                                        </ns1:cellItem>
                                                    </ns1:cell>
                                                </ns1:row>
                                            </xsl:for-each>
                                        </ns1:boxSymbol>
                                    </xsl:if>

                                </ns1:box>
                            </ns1:symbol>
                        </ns1:sections>
                    </xsl:if>

                    <!-- ================= Combined Comments (working version) ================= -->
                    <xsl:variable name="APP_IN_REQ" select="$REQ/*[local-name()='approvedComments']/*"/>
                    <xsl:variable name="REJ_IN_REQ" select="$REQ/*[local-name()='rejectedComments']/*"/>

                    <xsl:variable name="APP_CMTS"
                                  select="$APP_IN_REQ
                          | (//*[local-name()='approvedComments']/*)[count($APP_IN_REQ)=0]"/>
                    <xsl:variable name="REJ_CMTS"
                                  select="$REJ_IN_REQ
                          | (//*[local-name()='rejectedComments']/*)[count($REJ_IN_REQ)=0]"/>
                    <xsl:variable name="ALL_CMTS" select="$APP_CMTS | $REJ_CMTS"/>

                    <xsl:if test="count($ALL_CMTS) &gt; 0">
                        <!-- Header -->
                        <ns1:sections ns1:align="left" ns1:width="full">
                            <ns1:symbol xsi:type="ns1:textHeading" ns1:align="left">
                                <ns1:value>Comments</ns1:value>
                            </ns1:symbol>
                        </ns1:sections>

                        <!-- Boxed table -->
                        <ns1:sections ns1:align="left" ns1:width="full">
                            <ns1:symbol xsi:type="ns1:boxContainer" ns1:id="commentsBox">
                                <ns1:box xsi:type="ns1:box">

                                    <ns1:boxSymbol xsi:type="ns1:fullTable"
                                                   ns1:id="commentsTbl"
                                                   ns1:heading=""
                                                   ns1:showTotal="false"
                                                   ns1:headingColor="black">

                                        <!-- Columns -->
                                        <ns1:tableColumn ns1:align="left" ns1:fieldName="creator"     ns1:heading="Creator"      ns1:disableSorting="true"/>
                                        <ns1:tableColumn ns1:align="left" ns1:fieldName="createdDate" ns1:heading="Created Date" ns1:disableSorting="true"/>
                                        <ns1:tableColumn ns1:align="left" ns1:fieldName="comment"     ns1:heading="Comment"      ns1:disableSorting="true"/>

                                        <ns1:rowGroup xsi:type="ns1:rowGroup" ns1:groupId="cmt_g" ns1:groupHeaderLabel="">
                                            <ns1:totalsRow ns1:category=" ">
                                                <ns1:cell xsi:type="ns1:cell" ns1:col_id="creator">
                                                    <ns1:cellItem xsi:type="ns1:cellItem">
                                                        <ns1:item xsi:type="ns1:simpleText"><ns1:value/></ns1:item>
                                                    </ns1:cellItem>
                                                </ns1:cell>
                                                <ns1:cell xsi:type="ns1:cell" ns1:col_id="createdDate">
                                                    <ns1:cellItem xsi:type="ns1:cellItem">
                                                        <ns1:item xsi:type="ns1:simpleText"><ns1:value/></ns1:item>
                                                    </ns1:cellItem>
                                                </ns1:cell>
                                                <ns1:cell xsi:type="ns1:cell" ns1:col_id="comment">
                                                    <ns1:cellItem xsi:type="ns1:cellItem">
                                                        <ns1:item xsi:type="ns1:simpleText"><ns1:value/></ns1:item>
                                                    </ns1:cellItem>
                                                </ns1:cell>
                                            </ns1:totalsRow>
                                        </ns1:rowGroup>

                                        <!-- Pick one node per comment "record" -->
                                        <xsl:variable name="RECORD_LIKE"
                                                      select="$ALL_CMTS[
                               @creator or @created or @createdDate or @date or @createdAt or @createdOn or @timestamp
                               or *[local-name()='creator' or local-name()='created' or local-name()='createdDate'
                                     or local-name()='date' or local-name()='createdAt' or local-name()='createdOn'
                                     or local-name()='timestamp' or local-name()='comment' or local-name()='commentText'
                                     or local-name()='text' or local-name()='message' or local-name()='body']
                             ]"/>

                                        <!-- If we don't have record-like nodes, dedupe parents of leaf fields to get one row per parent -->
                                        <xsl:variable name="LEAF_FIELDS"
                                                      select="$ALL_CMTS[local-name()='creator'
                                        or local-name()='created' or local-name()='createdDate'
                                        or local-name()='date' or local-name()='createdAt' or local-name()='createdOn'
                                        or local-name()='timestamp'
                                        or local-name()='comment' or local-name()='commentText'
                                        or local-name()='text' or local-name()='message' or local-name()='body']"/>

                                        <xsl:for-each select="
                        $RECORD_LIKE
                        | ( $LEAF_FIELDS
                              [ not(preceding::*
                                  [local-name()='creator' or local-name()='created' or local-name()='createdDate'
                                   or local-name()='date' or local-name()='createdAt' or local-name()='createdOn'
                                   or local-name()='timestamp'
                                   or local-name()='comment' or local-name()='commentText'
                                   or local-name()='text' or local-name()='message' or local-name()='body']
                                  [ generate-id(..) = generate-id(current()/..) ]
                                )
                              ]/..
                          )[count($RECORD_LIKE)=0]
                      ">
                                            <!-- Sort newest first -->
                                            <xsl:sort select="normalize-space(string(
                         ( @created | *[local-name()='created']
                         | @createdDate | *[local-name()='createdDate']
                         | @date | *[local-name()='date']
                         | @createdAt | *[local-name()='createdAt']
                         | @createdOn | *[local-name()='createdOn']
                         | @timestamp | *[local-name()='timestamp']
                         | *[local-name()='dateTime'] | *[local-name()='datetime'] )[1]))"
                                                      data-type="text" order="descending"/>

                                            <!-- Extract fields -->
                                            <xsl:variable name="vCreator" select="normalize-space(string(
                        ( @creator | *[local-name()='creator']
                        | @createdBy | *[local-name()='createdBy']
                        | @user | *[local-name()='user']
                        | @author | *[local-name()='author']
                        | @username | *[local-name()='username']
                        | *[local-name()='userName'] | *[local-name()='name']
                        | @owner | *[local-name()='owner'] )[1]))"/>

                                            <xsl:variable name="vCreated" select="normalize-space(string(
                        ( @created | *[local-name()='created']
                        | @createdDate | *[local-name()='createdDate']
                        | @date | *[local-name()='date']
                        | @createdAt | *[local-name()='createdAt']
                        | @createdOn | *[local-name()='createdOn']
                        | @timestamp | *[local-name()='timestamp']
                        | *[local-name()='dateTime'] | *[local-name()='datetime'] )[1]))"/>

                                            <xsl:variable name="vCommentGuess" select="normalize-space(string(
                        ( @comment | *[local-name()='comment'] | *[local-name()='commentText']
                        | @text | *[local-name()='text']
                        | @message | *[local-name()='message']
                        | *[local-name()='body'] | *[local-name()='content']
                        | *[local-name()='note'] | *[local-name()='description']
                        | text() )[1]))"/>

                                            <!-- Only fall back to empty if no explicit comment fields -->
                                            <xsl:variable name="vComment">
                                                <xsl:choose>
                                                    <xsl:when test="string-length($vCommentGuess) &gt; 0">
                                                        <xsl:value-of select="$vCommentGuess"/>
                                                    </xsl:when>
                                                    <xsl:otherwise/>
                                                </xsl:choose>
                                            </xsl:variable>

                                            <ns1:row xsi:type="ns1:fullTableRow" ns1:groupId="cmt_g">
                                                <ns1:cell xsi:type="ns1:cell" ns1:col_id="creator">
                                                    <ns1:cellItem xsi:type="ns1:cellItem">
                                                        <ns1:item xsi:type="ns1:simpleText"><ns1:value><xsl:value-of select="$vCreator"/></ns1:value></ns1:item>
                                                    </ns1:cellItem>
                                                </ns1:cell>
                                                <ns1:cell xsi:type="ns1:cell" ns1:col_id="createdDate">
                                                    <ns1:cellItem xsi:type="ns1:cellItem">
                                                        <ns1:item xsi:type="ns1:simpleText"><ns1:value><xsl:value-of select="$vCreated"/></ns1:value></ns1:item>
                                                    </ns1:cellItem>
                                                </ns1:cell>
                                                <ns1:cell xsi:type="ns1:cell" ns1:col_id="comment">
                                                    <ns1:cellItem xsi:type="ns1:cellItem">
                                                        <ns1:item xsi:type="ns1:simpleText"><ns1:value><xsl:value-of select="$vComment"/></ns1:value></ns1:item>
                                                    </ns1:cellItem>
                                                </ns1:cell>
                                            </ns1:row>

                                        </xsl:for-each>

                                    </ns1:boxSymbol>
                                </ns1:box>
                            </ns1:symbol>
                        </ns1:sections>
                    </xsl:if>
                    <!-- ================= /Combined Comments ================= -->

                </ns1:form>
            </symbol>

            <!-- ===== Dynamic action labels based on subStatus (and optional status) ===== -->
            <xsl:variable name="SUB_RAW" select="normalize-space($REQ/*[local-name()='subStatus'])"/>
            <xsl:variable name="SUB_UP"  select="translate($SUB_RAW, $LOWER, $UPPER)"/>
            <xsl:variable name="STATUS_UP" select="translate(normalize-space($REQ/*[local-name()='status']), $LOWER, $UPPER)"/>
            <xsl:variable name="BACK_URL">
                <xsl:choose>
                    <!-- sub-status specific bucket -->
                    <xsl:when test="contains($SUB_UP,'ADMIN APPROVAL PENDING')">app-domain/mandates-and-resolutions/adminApproval</xsl:when>

                    <!-- status buckets -->
                    <xsl:when test="contains($STATUS_UP,'COMPLETED')">app-domain/mandates-and-resolutions/adminCompleted</xsl:when>
                    <xsl:when test="contains($STATUS_UP,'ON HOLD')">app-domain/mandates-and-resolutions/adminOnHold</xsl:when>
                    <xsl:when test="contains($STATUS_UP,'BREACH') or contains($STATUS_UP,'BREACHED')">app-domain/mandates-and-resolutions/adminBreach</xsl:when>
                    <xsl:when test="contains($STATUS_UP,'IN') and contains($STATUS_UP,'PROGRESS')">app-domain/mandates-and-resolutions/adminInProgress</xsl:when>
                    <!-- default -->
                    <xsl:otherwise>app-domain/mandates-and-resolutions/adminAll</xsl:otherwise>
                </xsl:choose>
            </xsl:variable>


            <!-- If you want to gate by status=In Progress, set this and AND it into the whens -->
            <!-- <xsl:variable name="IS_INPROGRESS" select="contains($STATUS_UP,'IN') and contains($STATUS_UP,'PROGRESS')"/> -->

            <xsl:variable name="LAB_APPROVE">
                <xsl:choose>
                    <xsl:when test="contains($SUB_UP,'HOGAN VERIFICATION PENDING')">Verify for Hogan</xsl:when>
                    <xsl:when test="contains($SUB_UP,'WINDEED VERIFICATION PENDING')">Verify for Windeed</xsl:when>
                    <xsl:when test="contains($SUB_UP,'HANIS VERIFICATION PENDING')">Verify for Hanis</xsl:when>
                    <xsl:when test="contains($SUB_UP,'ADMIN APPROVAL PENDING')">Approve</xsl:when>
                    <xsl:when test="contains($SUB_UP,'HOGAN UPDATE PENDING')">Update for Hogan</xsl:when>
                    <xsl:when test="contains($SUB_UP,'DOCUMENTUM UPDATE PENDING')">Update for Documentum</xsl:when>
                    <xsl:when test="$SUB_UP='SUBMITTED'">Submit Approve</xsl:when>
                    <xsl:otherwise>Approve</xsl:otherwise>
                </xsl:choose>
            </xsl:variable>

            <xsl:variable name="LAB_REJECT">
                <xsl:choose>
                    <xsl:when test="$SUB_UP='SUBMITTED'">Submit Reject</xsl:when>
                    <xsl:otherwise>Reject</xsl:otherwise>
                </xsl:choose>
            </xsl:variable>


            <!--===== Footer (Admin) =====-->
            <symbol xsi:type="ns1:footer" ns1:text="" ns1:textAlign="left" ns1:buttonAlign="right">
                <xsl:choose>
                    <!-- Completed: only Back -->
                    <xsl:when test="contains($STATUS_UP,'COMPLETED')">
                        <ns1:baseButton ns1:id="back"
                                        ns1:url="{$BACK_URL}" ns1:target="main"
                                        ns1:formSubmit="false" ns1:label="Back"/>
                    </xsl:when>

                    <!-- Otherwise -->
                    <xsl:otherwise>
                        <ns1:baseButton ns1:id="reassignBtn"
                                        ns1:url="app-domain/mandates-and-resolutions/adminReassign"
                                        ns1:target="panel" ns1:formSubmit="false" ns1:label="Re Assign"/>

                        <ns1:baseButton ns1:id="editBtn"
                                        ns1:url="{concat('app-domain/mandates-and-resolutions/editRequest/', $REQ/*[local-name()='requestId'])}"
                                        ns1:target="main" ns1:formSubmit="false" ns1:label="Edit"/>

                        <!-- Only show Hold when status is NOT 'On Hold' -->
                        <xsl:if test="not(contains($STATUS_UP,'ON HOLD'))">
                            <ns1:baseButton ns1:id="hold"
                                            ns1:url="{concat('app-domain/mandates-and-resolutions/viewRequestHold?requestId=', $REQ/*[local-name()='requestId'], '&amp;origin=admin')}"
                                            ns1:target="main" ns1:formSubmit="false" ns1:label="Hold"/>
                        </xsl:if>

                        <!-- Only show UnHold when status IS 'On Hold' -->
                        <xsl:if test="contains($STATUS_UP,'ON HOLD')">
                            <ns1:baseButton ns1:id="unHold"
                                            ns1:url="{concat('app-domain/mandates-and-resolutions/viewRequestUnhold?requestId=', $REQ/*[local-name()='requestId'], '&amp;origin=admin')}"
                                            ns1:target="main" ns1:formSubmit="false" ns1:label="UnHold"/>
                        </xsl:if>

                        <ns1:baseButton ns1:id="approve"
                                        ns1:url="{concat('app-domain/mandates-and-resolutions/viewRequestApprovePage?requestId=', $REQ/*[local-name()='requestId'])}"
                                        ns1:target="panel" ns1:formSubmit="false" ns1:label="{$LAB_APPROVE}"/>
                        <ns1:baseButton ns1:id="reject"
                                        ns1:url="{concat('app-domain/mandates-and-resolutions/viewRequestReject?requestId=', $REQ/*[local-name()='requestId'])}"
                                        ns1:target="panel" ns1:formSubmit="false" ns1:label="{$LAB_REJECT}"/>
                        <ns1:baseButton ns1:id="back"
                                        ns1:url="{$BACK_URL}" ns1:target="main"
                                        ns1:formSubmit="false" ns1:label="Back"/>
                    </xsl:otherwise>
                </xsl:choose>
            </symbol>
        </page>
    </xsl:template>
</xsl:stylesheet>