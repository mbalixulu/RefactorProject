<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:ns1="http://ws.online.fnb.co.za/v1/common/"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">

    <xsl:output method="xml" indent="yes"/>

    <xsl:template match="/">
        <!-- Safely compute counts to avoid BigInteger errors -->
        <xsl:variable name="directorCount" select="count(/requestWrapper/request/directors/director)" />
        <xsl:variable name="directorCountStr">
            <xsl:value-of select="string($directorCount)"/>
        </xsl:variable>
        <xsl:variable name="directorCountPlusOneStr">
            <xsl:value-of select="string($directorCount + 1)"/>
        </xsl:variable>

        <page xmlns:ns1="http://ws.online.fnb.co.za/v1/common/"
              id="resolutionAutoFillForm"
              title="Resolution Auto Fill Form"
              template="main"
              layout=""
              version="1">

            <!-- Heading -->
            <symbol xsi:type="ns1:subTabGroup" ns1:subTabGroupHeading="Resolutions"/>

            <symbol xsi:type="ns1:formLayout">
                <ns1:form ns1:action="http://localhost:8080/bifrost/"
                          ns1:name="salesForm">

                    <ns1:sections ns1:align="left" ns1:width="full">
                        <ns1:symbol xsi:type="ns1:textHeading">
                            <ns1:value>Auto fill</ns1:value>
                        </ns1:symbol>
                    </ns1:sections>

                    <!-- Directors Table -->
                    <ns1:sections ns1:align="left" ns1:width="full">
                        <ns1:symbol xsi:type="ns1:fullTable"
                                    ns1:id="DirectorsTable"
                                    ns1:heading="Add appointed directors"
                                    ns1:showTotal="false"
                                    ns1:headingColor="black">

                            <!-- Table Columns -->
                            <ns1:tableColumn ns1:align="left" ns1:fieldName="fullName" ns1:heading="Full Name (As per ID)" ns1:disableSorting="true"/>
                            <ns1:tableColumn ns1:align="left" ns1:fieldName="idNumber" ns1:heading="ID Number" ns1:disableSorting="true"/>
                            <ns1:tableColumn ns1:align="left" ns1:fieldName="instruction" ns1:heading="Instruction" ns1:disableSorting="true"/>
                            <ns1:tableColumn ns1:align="left" ns1:fieldName="remove" ns1:heading="Remove" ns1:disableSorting="true"/>

                            <!-- Row Group -->
                            <ns1:rowGroup xsi:type="ns1:rowGroup"
                                          ns1:groupId="directors"
                                          ns1:groupHeaderLabel="">

                                <ns1:totalsRow ns1:category=" ">
                                    <ns1:cell xsi:type="ns1:cell" ns1:col_id="fullName">
                                        <ns1:cellItem xsi:type="ns1:cellItem">
                                            <ns1:item xsi:type="ns1:simpleText">
                                                <ns1:value/>
                                            </ns1:item>
                                        </ns1:cellItem>
                                    </ns1:cell>
                                </ns1:totalsRow>

                                <!-- Add Director Button -->
                                <ns1:groupTableButton xsi:type="ns1:imageButton"
                                                      ns1:id="addDirectorBtn"
                                                      ns1:label="Add a director"
                                                      ns1:tip="Add a director"
                                                      ns1:url="{concat('app-domain/ui/resolutionsFill?directorCount=', $directorCountPlusOneStr)}"/>
                            </ns1:rowGroup>

                            <!-- Render Directors -->
                            <xsl:for-each select="/requestWrapper/request/directors/director">
                                <xsl:variable name="pos" select="position()" />
                                <ns1:row xsi:type="ns1:fullTableRow" ns1:groupId="directors">

                                    <ns1:cell xsi:type="ns1:cell" ns1:col_id="fullName">
                                        <ns1:cellItem xsi:type="ns1:cellItem">
                                            <ns1:item xsi:type="ns1:input"
                                                      ns1:name="{concat('fullName_', $pos)}">
                                                <ns1:value><xsl:value-of select="name"/></ns1:value>
                                            </ns1:item>
                                        </ns1:cellItem>
                                    </ns1:cell>

                                    <ns1:cell xsi:type="ns1:cell" ns1:col_id="idNumber">
                                        <ns1:cellItem xsi:type="ns1:cellItem">
                                            <ns1:item xsi:type="ns1:input"
                                                      ns1:name="{concat('idNumber_', $pos)}">
                                                <ns1:value><xsl:value-of select="surname"/></ns1:value>
                                            </ns1:item>
                                        </ns1:cellItem>
                                    </ns1:cell>

                                    <ns1:cell xsi:type="ns1:cell" ns1:col_id="instruction">
                                        <ns1:cellItem xsi:type="ns1:cellItem">
                                            <ns1:item xsi:type="ns1:input"
                                                      ns1:name="{concat('instruction_', $pos)}">
                                                <ns1:value><xsl:value-of select="designation"/></ns1:value>
                                            </ns1:item>
                                        </ns1:cellItem>
                                    </ns1:cell>

                                    <!-- Remove Button -->
                                    <ns1:cell xsi:type="ns1:cell" ns1:col_id="remove">
                                        <ns1:cellItem xsi:type="ns1:cellItem">
                                            <ns1:item xsi:type="ns1:button"
                                                      ns1:id="{concat('removeDirectorBtn_', position())}"
                                                      ns1:type="paper"
                                                      ns1:label="Remove"
                                                      ns1:formSubmit="false"
                                                      ns1:target="main"
                                                      ns1:width="2"
                                                      ns1:url="{concat('app-domain/ui/resolutionsFill?directorCount=', count(/requestWrapper/request/directors/director), '&amp;removeDirectorAt=', position())}"/>
                                        </ns1:cellItem>
                                    </ns1:cell>
                                </ns1:row>
                            </xsl:for-each>

                        </ns1:symbol>
                    </ns1:sections>
                </ns1:form>
            </symbol>

            <!-- Footer -->
            <symbol xsi:type="ns1:footer" ns1:buttonAlign="right">
                <ns1:baseButton ns1:id="submitBtn"
                                ns1:url="app-domain/ui/resolutionsSuccess"
                                ns1:label="Submit"
                                ns1:formSubmit="false"
                                ns1:target="main"/>
                <ns1:baseButton ns1:id="backBtn"
                                ns1:url="app-domain/ui/createRequest"
                                ns1:label="Back"
                                ns1:formSubmit="false"
                                ns1:target="main"/>
            </symbol>
        </page>
    </xsl:template>
</xsl:stylesheet>