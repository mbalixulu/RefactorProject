<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:ns1="http://ws.online.fnb.co.za/v1/common/"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">

    <xsl:output method="xml" indent="yes"/>

    <!-- Expecting root <requestWrapper> with <request><requestId> -->
    <xsl:template match="/requestWrapper">
        <page xmlns:ns1="http://ws.online.fnb.co.za/v1/common/"
              id="ViewRequestSuccessRejectPageAdmin"
              title="View Request Reject Success (Admin)"
              template="main" layout="" version="1">

            <symbol xsi:type="ns1:formLayout">
                <ns1:form ns1:action="" ns1:name="ViewRequestSuccessRejectPageAdmin">
                    <!-- Left: tick illustration -->
                    <ns1:sections ns1:align="left" ns1:width="half">
                        <ns1:symbol xsi:type="ns1:textParagraph" ns1:type="sub" ns1:bold="true">
                            <ns1:value/>
                        </ns1:symbol>
                        <ns1:symbol xsi:type="ns1:imagelink" ns1:lazy="false"
                                    ns1:url="/_assets/images/mm/fnb/15/pc/illustrations/tick.results.illustration.svg"
                                    ns1:align="center" ns1:illustration="true"
                                    ns1:height="75" ns1:width="75" ns1:sizeMetric="px"/>
                    </ns1:sections>

                    <!-- Right: message -->
                    <ns1:sections ns1:align="right" ns1:width="half">
                        <ns1:symbol xsi:type="ns1:textHeading" ns1:size="3" ns1:color="ghostdark">
                            <ns1:value>Thank you</ns1:value>
                        </ns1:symbol>
                        <ns1:symbol xsi:type="ns1:textParagraph" ns1:type="sub" ns1:bold="true">
                            <ns1:value>This request has been successfully rejected.</ns1:value>
                        </ns1:symbol>
                    </ns1:sections>
                </ns1:form>
            </symbol>

            <!-- Finish -> back to the exact ADMIN view of the request -->
            <symbol xsi:type="ns1:footer" ns1:text="" ns1:textAlign="left" ns1:buttonAlign="right">
                <ns1:baseButton ns1:id="finishBtn"
                                ns1:url="{concat('app-domain/mandates-and-resolutions/adminView/', /requestWrapper/request/requestId)}"
                                ns1:target="main"
                                ns1:formSubmit="false"
                                ns1:label="Finish"/>
            </symbol>
        </page>
    </xsl:template>
</xsl:stylesheet>
