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
                <comm:form comm:action="app-domain/ui/searchCompanyDetails" comm:name="predictiveForm">

                    <!-- Company registration input -->
                    <comm:sections comm:align="left" comm:width="full">
                        <comm:symbol xsi:type="comm:boxContainer" comm:id="templateBox">
                            <comm:box xsi:type="comm:box">
                                <comm:boxSymbol xsi:type="comm:boxSplit" comm:width="50">
                                    <comm:boxSplitSymbol xsi:type="comm:input"
                                                         comm:predictive="true"
                                                         comm:name="companyRegNumber"
                                                         comm:label="Company Registration Number"
                                                         comm:inputType="text">
                                        <comm:value/>
                                    </comm:boxSplitSymbol>

                                    <xsl:if test="/requestWrapper/requestDTO/errorMessage">
                                        <comm:boxSplitSymbol xsi:type="comm:boxMessage"
                                                             comm:type="error"
                                                             comm:message="{/requestWrapper/requestDTO/errorMessage}"/>
                                    </xsl:if>
                                </comm:boxSymbol>
                                <comm:boxSymbol xsi:type="comm:button"
                                                comm:id="searchHogan"
                                                comm:target="main"
                                                comm:url="app-domain/ui/searchCompanyDetails"
                                                comm:label="Search In Hogan"
                                                comm:width="3"
                                                comm:formSubmit="true"
                                                comm:type="primary"/>
                                <comm:boxSymbol xsi:type="comm:button"
                                                comm:id="searchHogan"
                                                comm:target="main"
                                                comm:url="app-domain/ui/searchCompanyDetails"
                                                comm:label="Search In Hogan"
                                                comm:width="3"
                                                comm:formSubmit="true"
                                                comm:type="primary"/>
                            </comm:box>
                        </comm:symbol>
                    </comm:sections>

                    <!-- Action buttons -->
                    <!--                    <comm:sections comm:width="full">-->
                    <!--                        <comm:symbol xsi:type="comm:boxContainer" comm:id="boxDiv">-->

                    <!--                            &lt;!&ndash; Search In Hogan button &ndash;&gt;-->
                    <!--                            <comm:boxSymbol xsi:type="comm:boxSplit" comm:width="33">-->
                    <!--                                <comm:boxSplitSymbol xsi:type="comm:button"-->
                    <!--                                                     comm:id="searchHogan"-->
                    <!--                                                     comm:target="main"-->
                    <!--                                                     comm:url="app-domain/ui/searchCompanyDetails"-->
                    <!--                                                     comm:label="Search In Hogan"-->
                    <!--                                                     comm:width="5"-->
                    <!--                                                     comm:formSubmit="true"-->
                    <!--                                                     comm:type="primary"/>-->
                    <!--                            </comm:boxSymbol>-->

                    <!--                            &lt;!&ndash; Proceed without Hogan button &ndash;&gt;-->
                    <!--                            <comm:boxSymbol xsi:type="comm:boxSplit" comm:width="33">-->
                    <!--                                <comm:boxSplitSymbol xsi:type="comm:button"-->
                    <!--                                                     comm:id="proceedWithoutHogan"-->
                    <!--                                                     comm:target="main"-->
                    <!--                                                     comm:url="app-domain/ui/test"-->
                    <!--                                                     comm:label="Proceed without Hogan"-->
                    <!--                                                     comm:width="5"-->
                    <!--                                                     comm:formSubmit="false"-->
                    <!--                                                     comm:type="primary"/>-->
                    <!--                            </comm:boxSymbol>-->

                    <!--                            &lt;!&ndash;Empty split to balance layout &ndash;&gt;-->
                    <!--                            <comm:boxSymbol xsi:type="comm:boxSplit" comm:width="33">-->
                    <!--                            </comm:boxSymbol>-->

                    <!--                        </comm:symbol>-->
                    <!--                    </comm:sections>-->

                </comm:form>
            </symbol>

            <!-- Footer -->
            <symbol xsi:type="comm:footer"
                    comm:text=""
                    comm:textAlign="left"
                    comm:buttonAlign="right">
                <comm:baseButton comm:id="cancel"
                                 comm:type="action"
                                 comm:url="app-domain/ui/requestTable"
                                 comm:target="main"
                                 comm:formSubmit="true"
                                 comm:label="Cancel"/>
            </symbol>

        </page>
    </xsl:template>
</xsl:stylesheet>