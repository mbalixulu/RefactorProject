<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="xml"/>
    <xsl:template match="/">
        <page xmlns:comm="http://ws.online.fnb.co.za/v1/common/"
              xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" id="onboardForm"
              title="Logout Page" template="main" layout="" version="1">
            <symbol xsi:type="comm:formLayout">
                <comm:form comm:name="logoutPageForm">
                    <comm:sections comm:align="center" comm:width="full">
                        <comm:symbol xsi:type="comm:textHeading" comm:size="3"
                                     comm:boldTableCell="false" comm:align="left">
                            <comm:value>You have logged out</comm:value>
                        </comm:symbol>
                    </comm:sections>
                </comm:form>
            </symbol>
        </page>
    </xsl:template>
</xsl:stylesheet>