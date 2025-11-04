<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:comm="http://ws.online.fnb.co.za/v1/common/"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">

    <xsl:output method="xml" indent="yes"/>

    <xsl:variable name="LOWER" select="'abcdefghijklmnopqrstuvwxyz'"/>
    <xsl:variable name="UPPER" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ'"/>

    <xsl:template match="/requestWrapper">
        <xsl:variable name="SUB"    select="normalize-space(/requestWrapper/request/subStatus)"/>
        <xsl:variable name="SUB_UP" select="translate($SUB, $LOWER, $UPPER)"/>

        <!-- Primary label -->
        <xsl:variable name="LAB_SUBMIT">
            <xsl:choose>
                <xsl:when test="contains($SUB_UP,'HOGAN VERIFICATION PENDING')">Verify for Hogan</xsl:when>
                <xsl:when test="contains($SUB_UP,'WINDEED VERIFICATION PENDING')">Verify for Windeed</xsl:when>
                <xsl:when test="contains($SUB_UP,'HANNIS VERIFICATION PENDING') or contains($SUB_UP,'HANIS VERIFICATION PENDING')">Verify for Hannis</xsl:when>
                <xsl:when test="contains($SUB_UP,'ADMIN VERIFICATION PENDING') or contains($SUB_UP,'ADMIN APPROVAL PENDING')">Approve</xsl:when>
                <xsl:when test="contains($SUB_UP,'HOGAN UPDATE PENDING')">Update for Hogan</xsl:when>
                <xsl:when test="contains($SUB_UP,'DOCUMENTUM UPDATE PENDING')">Updated successfully</xsl:when>
                <xsl:when test="contains($SUB_UP,'REQUEST UPDATED SUCCESSFULLY')">Updated Successfully</xsl:when>
                <xsl:otherwise>Approve</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <!-- Documentum step? then secondary button should be Reject -->
        <xsl:variable name="IS_DOCU" select="contains($SUB_UP,'DOCUMENTUM UPDATE PENDING')"/>
        <xsl:variable name="SECOND_LABEL">
            <xsl:choose>
                <xsl:when test="$IS_DOCU">Reject</xsl:when>
                <xsl:otherwise>Cancel</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="SECOND_URL">
            <xsl:choose>
                <xsl:when test="$IS_DOCU">app-domain/mandates-and-resolutions/viewRequestReject</xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="concat('app-domain/mandates-and-resolutions/viewRequest/', /requestWrapper/request/requestId)"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="SECOND_SUBMIT">
            <xsl:choose>
                <xsl:when test="$IS_DOCU">true</xsl:when>
                <xsl:otherwise>false</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <page xmlns:comm="http://ws.online.fnb.co.za/v1/common/"
              xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
              id="approvePanel" title="Approve Request" template="main" layout="" version="1">

            <symbol xsi:type="comm:formLayout">
                <comm:form comm:action="app-domain/mandates-and-resolutions/comment/approve" comm:name="approveForm">

                    <comm:sections comm:align="left" comm:width="full">
                        <comm:symbol xsi:type="comm:textHeading">
                            <comm:value>Add a comment before approval (optional)</comm:value>
                        </comm:symbol>
                    </comm:sections>

                    <comm:sections comm:align="left" comm:width="full">
                        <comm:symbol xsi:type="comm:input" comm:name="requestId" comm:inputType="hidden">
                            <comm:value><xsl:value-of select="/requestWrapper/request/requestId"/></comm:value>
                        </comm:symbol>
                        <comm:symbol xsi:type="comm:input" comm:name="subStatus" comm:inputType="hidden">
                            <comm:value><xsl:value-of select="/requestWrapper/request/subStatus"/></comm:value>
                        </comm:symbol>

                        <comm:symbol xsi:type="comm:boxContainer" comm:id="boxDiv">
                            <comm:box xsi:type="comm:box">

<!--                                &lt;!&ndash; ===== Dynamic Instructions from wrapper.lovs.instructions ===== &ndash;&gt;-->
<!--                                <comm:boxSymbol xsi:type="comm:textList" comm:subHeading="Instructions">-->
<!--                                    <comm:value/>-->
<!--                                    <xsl:choose>-->
<!--                                        &lt;!&ndash;Render list items when present&ndash;&gt;-->
<!--                                        <xsl:when test="count(/requestWrapper/lovs/instructions/instruction) &gt; 0">-->
<!--                                            <xsl:for-each select="/requestWrapper/lovs/instructions/instruction">-->
<!--                                                <comm:textListItem><comm:value><xsl:value-of select="."/></comm:value></comm:textListItem>-->
<!--                                            </xsl:for-each>-->
<!--                                        </xsl:when>-->
<!--                                        &lt;!&ndash;Fallback&ndash;&gt;-->
<!--                                        <xsl:otherwise>-->
<!--                                            <comm:textListItem>-->
<!--                                                <comm:value>No specific instructions for this status.</comm:value>-->
<!--                                            </comm:textListItem>-->
<!--                                        </xsl:otherwise>-->
<!--                                    </xsl:choose>-->
<!--                                </comm:boxSymbol>-->
                                <comm:boxSymbol xsi:type="comm:commentbox"
                                                comm:name="commentbox"
                                                comm:label="Comment"
                                                comm:commentLimit="2000"
                                                comm:rowsNo="9">
                                    <comm:value/>
                                </comm:boxSymbol>

                            </comm:box>
                        </comm:symbol>
                    </comm:sections>

                    <comm:sections comm:align="left" comm:width="full">
                        <comm:symbol xsi:type="comm:button"
                                     comm:id="submitApprove"
                                     comm:target="main"
                                     comm:url="app-domain/mandates-and-resolutions/comment/approve"
                                     comm:label="{$LAB_SUBMIT}"
                                     comm:width="1"
                                     comm:formSubmit="true"
                                     comm:align="right"
                                     comm:type="primary"/>
                        <comm:symbol xsi:type="comm:button"
                                     comm:id="secondaryApprove"
                                     comm:target="main"
                                     comm:url="{$SECOND_URL}"
                                     comm:label="{$SECOND_LABEL}"
                                     comm:width="1"
                                     comm:formSubmit="{$SECOND_SUBMIT}"
                                     comm:align="right"
                                     comm:type="primary"/>
                    </comm:sections>

                </comm:form>
            </symbol>
        </page>
    </xsl:template>
</xsl:stylesheet>