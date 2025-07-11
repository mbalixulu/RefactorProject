<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml" indent="yes"/>
    <xsl:template match="/">

        <page xmlns:comm="http://ws.online.fnb.co.za/v1/common/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" id="onboardForm" title="Comment Box Version 1" template="main" layout="" version="1">
            <symbol xsi:type="comm:formLayout">
                <comm:form comm:action="http://localhost:8080/bifrost/" comm:name="salesForm">
                    <comm:sections comm:align="left" comm:width="full">
                        <comm:symbol xsi:type="comm:textHeading">
                            <comm:value>Reason for rejection</comm:value>
                        </comm:symbol>
                    </comm:sections>

                    <comm:sections comm:align="left" comm:width="full">
                        <comm:symbol xsi:type="comm:boxContainer" comm:id="boxDiv">
                            <comm:box xsi:type="comm:box">
                                <comm:boxSymbol xsi:type="comm:commentbox" comm:name="commentbox" comm:label="Reason" comm:commentLimit="2000" comm:rowsNo="9">
                                    <comm:value/>
                                </comm:boxSymbol>
                            </comm:box>
                        </comm:symbol>
                    </comm:sections>

                    <comm:sections comm:align="left" comm:width="full">
                        <comm:symbol xsi:type="comm:button" comm:id="editBtn" comm:target="main" comm:url="app-domain/ui/viewRequest"
                                     comm:label="Submit" comm:width="1" comm:formSubmit="false" comm:align="right" comm:type="primary"/>

                        <comm:symbol xsi:type="comm:button" comm:id="overrideBtn" comm:target="main" comm:url="app-domain/ui/viewRequest"
                                     comm:label="Cancel" comm:width="1" comm:formSubmit="false" comm:align="right" comm:type="primary"/>
                    </comm:sections>
                </comm:form>
            </symbol>
        </page>
    </xsl:template>
</xsl:stylesheet>