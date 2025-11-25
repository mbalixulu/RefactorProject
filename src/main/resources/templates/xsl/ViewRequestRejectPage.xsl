<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:comm="http://ws.online.fnb.co.za/v1/common/"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">

    <xsl:output method="xml" indent="yes"/>

    <xsl:template match="/requestWrapper">
        <page xmlns:comm="http://ws.online.fnb.co.za/v1/common/"
              xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
              id="rejectPanel" title="Reject Request" template="main" layout="" version="1">

            <symbol xsi:type="comm:formLayout">
                <comm:form comm:action="app-domain/mandates-and-resolutions/comment/reject" comm:name="rejectForm">

                    <comm:sections comm:align="left" comm:width="full">
                        <comm:symbol xsi:type="comm:textHeading">
                            <comm:value>Reason for rejection (required)</comm:value>
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
<!--                                        <xsl:when test="count(/requestWrapper/lovs/instructions/instruction) &gt; 0">-->
<!--                                            <xsl:for-each select="/requestWrapper/lovs/instructions/instruction">-->
<!--                                                <comm:textListItem><comm:value><xsl:value-of select="."/></comm:value></comm:textListItem>-->
<!--                                            </xsl:for-each>-->
<!--                                        </xsl:when>-->
<!--                                        <xsl:otherwise>-->
<!--                                            <comm:textListItem>-->
<!--                                                <comm:value>No specific instructions for this status.</comm:value>-->
<!--                                            </comm:textListItem>-->
<!--                                        </xsl:otherwise>-->
<!--                                    </xsl:choose>-->
<!--                                </comm:boxSymbol>-->

<!--                                <comm:boxSymbol xsi:type="comm:input"-->
<!--                                                comm:name="confirmationCheckMandate"-->
<!--                                                comm:inputType="checkbox"-->
<!--                                                comm:unCheckedValue="No"-->
<!--                                                comm:selected="false"-->
<!--                                                comm:errorMessage="{/requestWrapper/approveRejectErrorModel/confirmationCheckMandate}">-->
<!--                                    <comm:value/>-->
<!--                                    <comm:inputItem comm:id="confirmationCheckMandate"-->
<!--                                                    comm:label="I confirm that the Instructions have been followed as mentioned above."-->
<!--                                                    comm:type="checkbox"-->
<!--                                                    comm:value="1"-->
<!--                                                    comm:unCheckedValue="No"-->
<!--                                                    comm:selected="false"/>-->
<!--                                </comm:boxSymbol>-->

                                <comm:boxSymbol xsi:type="comm:commentbox"
                                                comm:name="commentbox"
                                                comm:label="Reason"
                                                comm:commentLimit="2000"
                                                comm:rowsNo="9"
                                                comm:errorMessage="{/requestWrapper/approveRejectErrorModel/commentbox}">
                                    <comm:value>
                                        <xsl:value-of select="/requestWrapper/approveRejectErrorModel/commentboxValue"/>
                                    </comm:value>
                                </comm:boxSymbol>

                            </comm:box>
                        </comm:symbol>
                    </comm:sections>

                    <!--  <comm:sections comm:align="left" comm:width="full">
                          <comm:symbol xsi:type="comm:button"
                                       comm:id="submitReject"
                                       comm:target="main"
                                       comm:url="app-domain/mandates-and-resolutions/comment/reject"
                                       comm:label="Reject"
                                       comm:width="1"
                                       comm:formSubmit="true"
                                       comm:align="right"
                                       comm:type="primary"/>
                          <comm:symbol xsi:type="comm:button"
                                       comm:id="cancelReject"
                                       comm:target="main"
                                       comm:url="{concat('app-domain/mandates-and-resolutions/viewRequest/', /requestWrapper/request/requestId)}"
                                       comm:label="Cancel"
                                       comm:width="1"
                                       comm:formSubmit="false"
                                       comm:align="right"
                                       comm:type="primary"/>
                      </comm:sections>-->

                </comm:form>
            </symbol>
            <symbol xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                    xsi:type="comm:footer" comm:text="" comm:textAlign="left"
                    comm:buttonAlign="right">
                <comm:baseButton comm:id="submitReject"
                                 comm:target="main"
                                 comm:url="app-domain/mandates-and-resolutions/comment/reject"
                                 comm:label="Reject"
                                 comm:formSubmit="true"/>
                <comm:baseButton comm:id="cancelReject"
                                 comm:target="main"
                                 comm:url="{concat('app-domain/mandates-and-resolutions/viewRequest/', /requestWrapper/request/requestId)}"
                                 comm:label="Cancel"
                                 comm:formSubmit="false"/>
            </symbol>

        </page>
    </xsl:template>
</xsl:stylesheet>