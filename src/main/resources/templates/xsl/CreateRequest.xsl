<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:comm="http://ws.online.fnb.co.za/v1/common/"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" version="1.0">
    <xsl:output method="xml" indent="yes"/>
    <xsl:template match="/">
        <page xmlns:comm="http://ws.online.fnb.co.za/v1/common/"
              xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" id="createRequestForm"
              title="Search Bar" template="main" layout="main" version="1">
            <!--  Subtab heading  -->
            <symbol xsi:type="comm:subTabGroup" comm:subTabGroupHeading="Mandates and resolutions"/>
            <symbol xsi:type="comm:formLayout">
                <comm:form comm:action="app-domain/mandates-and-resolutions/searchCompanyDetails"
                           comm:name="predictiveForm">
                    <comm:sections comm:align="left" comm:width="full">
                        <comm:symbol xsi:type="comm:input" comm:name="origin"
                                     comm:inputType="hidden">
                            <comm:value>create</comm:value>
                        </comm:symbol>
                    </comm:sections>
                    <!--  Company registration input  -->
                    <comm:sections comm:align="left" comm:width="full">
                        <comm:symbol xsi:type="comm:boxContainer" comm:id="templateBox">
                            <comm:box xsi:type="comm:box">
                                <comm:boxSymbol xsi:type="comm:boxSplit" comm:width="50">
                                    <comm:boxSplitSymbol xsi:type="comm:input"
                                                         comm:predictive="true"
                                                         comm:predictiveTextNav="app-domain/mandates-and-resolutions/predictive/companyRegNumbers"
                                                         comm:name="companyRegNumber"
                                                         comm:label="Company Registration Number *"
                                                         comm:errorMessage="{requestWrapper/searchResultsErrorModel/regiNumber}"
                                                         comm:inputType="text">
                                        <xsl:if test="string-length(normalize-space(/requestWrapper/request/errorMessage)) > 0">
                                            <xsl:attribute name="comm:message">
                                                <xsl:value-of
                                                        select="/requestWrapper/request/errorMessage"/>
                                            </xsl:attribute>
                                        </xsl:if>
                                        <comm:value>
                                            <xsl:value-of
                                                    select="/requestWrapper/request/registrationNumber"/>
                                        </comm:value>
                                    </comm:boxSplitSymbol>
                                </comm:boxSymbol>
                            </comm:box>
                        </comm:symbol>
                    </comm:sections>
                </comm:form>
            </symbol>
            <!--  Footer  -->
            <symbol xsi:type="comm:footer" comm:text="" comm:textAlign="left"
                    comm:buttonAlign="right">
                <comm:baseButton comm:id="cancel" comm:type="action"
                                 comm:url="app-domain/mandates-and-resolutions/cancelCreateRequest"
                                 comm:target="main" comm:formSubmit="false" comm:label="Cancel"/>
                <comm:baseButton comm:id="search" comm:type="action"
                                 comm:url="app-domain/mandates-and-resolutions/searchCompanyDetails"
                                 comm:target="main" comm:formSubmit="true" comm:label="Search"/>
                <xsl:if test="string-length(normalize-space(/requestWrapper/request/errorMessage)) > 0">
                    <comm:baseButton comm:id="regi" comm:type="action"
                                     comm:url="{concat('app-domain/mandates-and-resolutions/searchResults?registrationNumber=', normalize-space(/requestWrapper/request/registrationNumber))}"
                                     comm:target="main" comm:formSubmit="false"
                                     comm:label="Create Request +"/>
                </xsl:if>
            </symbol>
        </page>
    </xsl:template>
</xsl:stylesheet>