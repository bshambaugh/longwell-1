<?xml version='1.0'?>
<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:dc="http://purl.org/dc/elements/1.1/"
  xmlns:doc="http://www.w3.org/2000/10/swap/pim/doc#"
  xmlns:rec="http://www.w3.org/2001/02pd/rec54#"
  xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
  xmlns:contact="http://www.w3.org/2000/10/swap/pim/contact#"
  xmlns:skos="http://www.w3.org/2004/02/skos/core#"
  xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
  xmlns:n5="http://www.w3.org/2001/02pd/rec54#"
  xmlns:n4="http://www.w3.org/2001/02pd/rec54#"
  xmlns:n6="http://www.w3.org/2000/10/swap/pim/doc#"
  xmlns:n7="http://purl.org/dc/elements/1.1/">

  <xsl:output method="xml" version="1.0" encoding="iso-8859-1" indent="yes"/>

  <xsl:template match="/rdf:RDF">
   <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <xsl:apply-templates select="@*"/>
    <xsl:apply-templates select="*"/>
   </rdf:RDF>
  </xsl:template>

  <xsl:template match="*">
   <xsl:element name="{name()}">
    <xsl:apply-templates select="@*"/>
    <xsl:apply-templates select="text()"/>
    <xsl:apply-templates select="*"/>
   </xsl:element>
  </xsl:template>

  <xsl:template match="@*">
   <xsl:attribute name="{name()}">
    <xsl:value-of select="."/>
   </xsl:attribute>
  </xsl:template>

  <xsl:template match="text()">
    <xsl:value-of select="normalize-space()"/>
  </xsl:template>

  <xsl:template match="*[@rdf:parseType='Literal']">
   <xsl:element name="{name()}">
    <xsl:for-each select="@*">
     <xsl:if test="local-name()!='parseType'">
      <xsl:attribute name="{name()}">
       <xsl:value-of select="."/>
      </xsl:attribute>
     </xsl:if>
    </xsl:for-each>
    <xsl:value-of select="normalize-space()"/>
   </xsl:element>
  </xsl:template>

</xsl:stylesheet>
