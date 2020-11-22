<?xml version='1.0'?>

<!--+ 
    |  This stylesheet converts artstor.xml files to RDF 
    |  Author: Mark Butler mark-h.butler@hp.com 
    |  08 October 2003
    |
    |  It requires Saxon 7.7 to run as it uses XSLT 2.0 and XPath 2.0
    |  functionality
    +-->
<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:person="http://simile.mit.edu/2003/10/ontologies/person#"
  xmlns:str="http://simile.mit.edu/2004/01/xslt/common" 
  xmlns:vc="http://www.w3.org/2001/vcard-rdf/3.0#"
  xmlns:foaf="http://xmlns.com/foaf/0.1/">
  
  <xsl:output method="xml" version="1.0" encoding="iso-8859-1" indent="yes"/>
  
  <xsl:function name="str:urlencode">
    <xsl:param name="url"/>
    <xsl:value-of select="replace(replace(replace(replace(lower-case(normalize-space($url)),'#','_'),': ','_'),' ','_'),'&quot;','_')"/>
  </xsl:function>
  
  <xsl:template name="canonicalizeName">
    <xsl:param name="name"/>
    <xsl:variable name="namevalue" select="normalize-space($name)"/>
    <xsl:choose>
      <!-- no commas, do not reorder -->
      <xsl:when test="not(contains($namevalue,','))">
        <xsl:variable name="nametemp" select="$namevalue"/>
        <xsl:variable name="name">
          <xsl:choose>
            <xsl:when test="starts-with($nametemp,'Sir')">
              <xsl:value-of select="substring-after($nametemp,'Sir ')"/>
            </xsl:when>
            <xsl:when test="starts-with($nametemp,'Prof.')">
              <xsl:value-of select="substring-after($nametemp,'Prof. ')"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="$nametemp"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <xsl:choose>
          <xsl:when test="starts-with($nametemp,'Sir')">
            <vc:prefix>Sir</vc:prefix>
          </xsl:when>
          <xsl:when test="starts-with($nametemp,'Prof.')">
            <vc:prefix>Professor</vc:prefix>
          </xsl:when>
        </xsl:choose>
        <xsl:if test="not($nametemp='')">
          <vc:FN>
            <xsl:value-of select="$nametemp"/>
          </vc:FN>
        </xsl:if>
      </xsl:when>
      <xsl:when test="not(matches(namevalue,'.*,.*,.*,'))">
        <xsl:variable name="tokenizedPersonalName" select="tokenize($namevalue,',|-')"/>
        <xsl:variable name="surname" select="normalize-space($tokenizedPersonalName[1])"/>
        <xsl:variable name="forenametemp" select="normalize-space($tokenizedPersonalName[2])"/>
        <xsl:variable name="forename" select="normalize-space($tokenizedPersonalName[2])"/>
        <xsl:variable name="forename">
          <xsl:choose>
            <xsl:when test="starts-with($forenametemp,'Sir')">
              <xsl:value-of select="substring-after($forenametemp,'Sir ')"/>
            </xsl:when>
            <xsl:when test="starts-with($forenametemp,'Prof.')">
              <xsl:value-of select="substring-after($forenametemp,'Prof. ')"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="$forenametemp"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <xsl:choose>
          <xsl:when test="starts-with($forenametemp,'Sir')">
            <vc:prefix>Sir</vc:prefix>
          </xsl:when>
          <xsl:when test="starts-with($forenametemp,'Prof.')">
            <vc:prefix>Professor</vc:prefix>
          </xsl:when>
        </xsl:choose>
        <xsl:variable name="birth" select="normalize-space($tokenizedPersonalName[3])"/>
        <xsl:variable name="death" select="normalize-space($tokenizedPersonalName[4])"/>
        <xsl:if test="not($surname='')">
          <vc:Family>
            <xsl:value-of select="$surname"/>
          </vc:Family>
        </xsl:if>
        <xsl:if test="not($forename='')">
          <vc:Given>
            <xsl:value-of select="$forename"/>
          </vc:Given>
        </xsl:if>
        <xsl:if test="(not($forenametemp=''))and (not($surname=''))">
          <vc:FN>
            <xsl:value-of select="$forenametemp"/>
            <xsl:text/>
            <xsl:value-of select="$surname"/>
          </vc:FN>
        </xsl:if>
        <xsl:if test="(matches($namevalue,'.*,.*,.*\d+-')) and (not($birth='')) and (matches($birth,'.*\d+'))">
          <person:birth>
            <xsl:value-of select="$birth"/>
          </person:birth>
          <xsl:if test="(not($death='')) and (matches($death,'\d+'))">
            <person:death>
              <xsl:value-of select="$death"/>
            </person:death>
          </xsl:if>
        </xsl:if>
      </xsl:when>
      <xsl:otherwise>
        <failedOnName>
          <xsl:value-of select="namevalue"/>
        </failedOnName>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
</xsl:stylesheet>
