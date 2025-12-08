<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="xml"/>
    <!-- start from the root of the xml document "/" -->
    <xsl:template match="/">
        <page xmlns:comm="http://ws.online.fnb.co.za/v1/common/"
              xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
              id="boxConatinerID"
              title="Box Container" template="main" layout="" version="1">
            <symbol xsi:type="comm:subTabGroup" comm:subTabGroupHeading="Re-Assign User"/>
            <symbol xsi:type="comm:formLayout">
                <comm:form comm:name="selectRequirementsForm"
                           comm:action="app-domain/mandates-and-resolutions/adminReassignSubmit">
                    <comm:sections comm:align="left" comm:width="full">
                        <comm:symbol
                                xsi:type="comm:input"
                                comm:name="requestId"
                                comm:inputType="hidden">
                            <comm:value>
                                <xsl:value-of select="/requestWrapper/request/requestId"/>
                            </comm:value>
                        </comm:symbol>
                    </comm:sections>
                    <comm:sections comm:width="full">
                        <comm:symbol xsi:type="comm:boxContainer"
                                     comm:id="boxDiv">
                            <comm:box xsi:type="comm:box">
                                <comm:boxSymbol
                                        xsi:type="comm:dropdown"
                                        comm:selectedValue="{requestWrapper/request/userOptions/userOption}"
                                        comm:id="selectUser"
                                        comm:label="Assignee">
                                    <xsl:for-each
                                            select="/requestWrapper/request/userOptions/userOption">
                                        <comm:label>
                                            <xsl:value-of
                                                    select="username"/>
                                        </comm:label>
                                        <comm:value xsi:type="comm:eventValue">
                                            <comm:value>
                                                <xsl:value-of select="username"/>
                                            </comm:value>
                                        </comm:value>
                                    </xsl:for-each>
                                </comm:boxSymbol>
                            </comm:box>
                        </comm:symbol>
                    </comm:sections>
                </comm:form>
            </symbol>
            <symbol xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                    xsi:type="comm:footer" comm:text="" comm:textAlign="left"
                    comm:buttonAlign="right">
                <comm:baseButton comm:id="cancel"
                                 comm:url="app-domain/mandates-and-resolutions/adminViewBack"
                                 comm:target="main" comm:label="Cancel"
                                 comm:formSubmit="false"/>
                <comm:baseButton
                        comm:id="reAssign"
                        comm:url="app-domain/mandates-and-resolutions/adminReassignSubmit"
                        comm:target="main" comm:label="Reassign" comm:formSubmit="true"/>
            </symbol>
        </page>
    </xsl:template>
</xsl:stylesheet>



