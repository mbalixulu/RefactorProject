<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:comm="http://ws.online.fnb.co.za/v1/common/"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <xsl:output method="xml" indent="yes"/>

    <xsl:template match="/">
        <page id="createRequestForm"
              title="Search Bar"
              template="main"
              layout="main"
              version="1"
              xmlns:comm="http://ws.online.fnb.co.za/v1/common/"
              xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">

            <!-- Subtab heading -->
            <symbol xsi:type="comm:subTabGroup" comm:subTabGroupHeading="Mandates and resolutions"/>

            <symbol xsi:type="comm:formLayout">
                <comm:form comm:action="app-domain/mandates-and-resolutions/searchCompanyDetails" comm:name="predictiveForm">

                    <!-- Hidden origin flag so server can tell this POST came from CreateRequest -->
                    <comm:sections comm:align="left" comm:width="full">
                        <comm:symbol xsi:type="comm:input" comm:name="origin" comm:inputType="hidden">
                            <comm:value>create</comm:value>
                        </comm:symbol>
                    </comm:sections>

                    <!-- Company registration input -->
                    <comm:sections comm:align="left" comm:width="full">
                        <comm:symbol xsi:type="comm:boxContainer" comm:id="templateBox">
                            <comm:box xsi:type="comm:box">

                                <comm:boxSymbol xsi:type="comm:boxSplit" comm:width="50">
                                    <comm:boxSplitSymbol xsi:type="comm:input"
                                                         comm:predictive="true"
                                                         comm:predictiveTextNav="app-domain/mandates-and-resolutions/predictive/companyRegNumbers"
                                                         comm:name="companyRegNumber"
                                                         comm:label="Company Registration Number"
                                                         comm:inputType="text">
                                        <!-- Only include message attribute if we have an error -->
                                        <xsl:if test="string-length(normalize-space(/requestWrapper/request/errorMessage)) &gt; 0">
                                            <xsl:attribute name="comm:message">
                                                <xsl:value-of select="/requestWrapper/request/errorMessage"/>
                                            </xsl:attribute>
                                        </xsl:if>
                                        <!-- Prepopulate if returning to this page -->
                                        <comm:value>
                                            <xsl:value-of select="/requestWrapper/request/registrationNumber"/>
                                        </comm:value>
                                    </comm:boxSplitSymbol>
                                </comm:boxSymbol>

                                <!-- Invisible spacer to keep Search aligned to the top -->
                                <comm:boxSymbol xsi:type="comm:button"
                                                comm:id="searchSpacer"
                                                comm:target="main"
                                                comm:url="app-domain/mandates-and-resolutions/searchCompanyDetails"
                                                comm:label="Spacer"
                                                comm:width="3"
                                                comm:formSubmit="true"
                                                comm:type="primary"/>

                                <!-- Search -->
                                <comm:boxSymbol xsi:type="comm:button"
                                                comm:id="searchBtn"
                                                comm:target="main"
                                                comm:url="app-domain/mandates-and-resolutions/searchCompanyDetails"
                                                comm:label="Search"
                                                comm:width="3"
                                                comm:formSubmit="true"
                                                comm:type="primary"/>

                                <!-- When DAO lookup fails, show Create Request next to Search -->
                                <xsl:if test="string-length(normalize-space(/requestWrapper/request/errorMessage)) &gt; 0">
                                    <comm:boxSymbol xsi:type="comm:button"
                                                    comm:id="createRequestBtn"
                                                    comm:target="main"
                                                    comm:url="{concat('app-domain/mandates-and-resolutions/searchResults?registrationNumber=', normalize-space(/requestWrapper/request/registrationNumber))}"
                                                    comm:label="Create Request"
                                                    comm:width="3"
                                                    comm:formSubmit="false"
                                                    comm:type="primary"/>
                                </xsl:if>

                            </comm:box>
                        </comm:symbol>
                    </comm:sections>
                </comm:form>
            </symbol>

            <!-- Footer -->
            <symbol xsi:type="comm:footer" comm:text="" comm:textAlign="left" comm:buttonAlign="right">
                <comm:baseButton
                        comm:id="cancel"
                        comm:type="action"
                        comm:url="app-domain/mandates-and-resolutions/cancelCreateRequest"
                        comm:target="main"
                        comm:formSubmit="false"
                        comm:label="Cancel"/>
            </symbol>


        </page>
    </xsl:template>
</xsl:stylesheet>