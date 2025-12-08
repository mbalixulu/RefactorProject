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
                                 comm:url="app-domain/mandates-and-resolutions/adminViewBack"
                                 comm:label="Cancel"
                                 comm:formSubmit="false"/>
            </symbol>

        </page>
    </xsl:template>
</xsl:stylesheet>