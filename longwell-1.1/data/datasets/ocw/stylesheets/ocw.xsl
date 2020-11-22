<?xml version="1.0"?>

<!--+
    | This stylesheet converts OCW data to RDF 
    |
    |  Author: Mark Butler mark-h.butler@hp.com 
    |  Date: 08 October 2003
    +-->

<!DOCTYPE xsl:stylesheet [
        <!ENTITY rdf      'http://www.w3.org/1999/02/22-rdf-syntax-ns#'>
        <!ENTITY rdfs     'http://www.w3.org/2000/01/rdf-schema#'>
        <!ENTITY xsd      'http://www.w3.org/2001/XMLSchema'>
        <!ENTITY skos     'http://www.w3.org/2004/02/skos/core#'>
        <!ENTITY vc       'http://www.w3.org/2001/vcard-rdf/3.0#'>
        <!ENTITY dc       'http://purl.org/dc/elements/1.1/'>
        <!ENTITY dcterms  'http://purl.org/dc/terms/'>
        <!ENTITY lom	     'http://www.imsproject.org/rdf/imsmd_rootv1p2#' >
        <!ENTITY lom-tech 'http://www.imsproject.org/rdf/imsmd_technicalv1p2#'>
        <!ENTITY lom-edu  'http://www.imsproject.org/rdf/imsmd_educationalv1p2#'>
        <!ENTITY lom-gen  'http://www.imsproject.org/rdf/imsmd_generalv1p2#'>
        <!ENTITY lom-life 'http://www.imsproject.org/rdf/imsmd_lifecyclev1p2#'>
        <!ENTITY ocwroot  'http://ocw.mit.edu/'>
        <!ENTITY str 	     'http://simile.mit.edu/2004/01/xslt/common'>	
        <!ENTITY person   'http://simile.mit.edu/2003/10/ontologies/person#'>
        <!ENTITY ocw	     'http://simile.mit.edu/2004/01/ontologies/ocw#'>
        <!ENTITY ocwdata  'http://simile.mit.edu/2004/01/ocw/'>
        <!ENTITY ocwsource 'http://simile.mit.edu/2004/06/collections#ocw'>
]>
   
<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:rdf="&rdf;" 
  xmlns:rdfs="&rdfs;" 
  xmlns:dc="&dc;"
  xmlns:ocw="&ocw;" 
  xmlns:str="&str;" 
  xmlns:xsd="&xsd;"
  xmlns:lom="&lom;" 
  xmlns:lom-life="&lom-life;"
  xmlns:lom-edu="&lom-edu;" 
  xmlns:lom-tech="&lom-tech;"
  xmlns:lom-gen="&lom-gen;" 
  xmlns:dcterms="&dcterms;" 
  xmlns:skos="&skos;">
  
  <xsl:import href="../../../stylesheets/common.xsl"/>
  
  <xsl:output method="xml" indent="yes"/>
  
  <xsl:template match="SearchMetadata">
    <rdf:RDF 
        xmlns:rdf="&rdf;" 
        xmlns:rdfs="&rdfs;" 
        xmlns:dc="&dc;"
        xmlns:ocw="&ocw;" 
        xmlns:str="&str;" 
        xmlns:xsd="&xsd;"
        xmlns:lom="&lom;" 
        xmlns:lom-life="&lom-life;"
        xmlns:lom-edu="&lom-edu;" 
        xmlns:lom-tech="&lom-tech;"
        xmlns:lom-gen="&lom-gen;" 
        xmlns:dcterms="&dcterms;" 
        xmlns:skos="&skos;"
        xmlns:person="&person;" 
        xmlns:vc="&vc;">
        
      <xsl:apply-templates/>
      
      <rdf:Description rdf:about="&ocwsource;">
        <rdf:type rdf:resource="http://simile.mit.edu/2003/10/ontologies/artstor#Collection"/>
        <rdfs:label>MIT OpenCourseWare</rdfs:label>
      </rdf:Description>
    </rdf:RDF>
  </xsl:template>

  <!-- ================ named templates ============ -->
  
  <xsl:template name="generateURI">
    <xsl:param name="controlledVocabularyType"/>
    <skos:Concept>
      <xsl:attribute name="rdf:about">&ocwdata;<xsl:value-of select="$controlledVocabularyType"/>#<xsl:value-of select="str:urlencode(.)"/></xsl:attribute>
      <!-- comment out rdfs:label for now, this will require some changes to Knowle and Longwell 
      <rdfs:label><xsl:value-of select="."/></rdfs:label> -->
      <skos:prefLabel>
        <xsl:value-of select="."/>
      </skos:prefLabel>
      <skos:inScheme>
        <xsl:attribute name="rdf:resource">&ocw;<xsl:value-of select="$controlledVocabularyType"/></xsl:attribute>
      </skos:inScheme>
    </skos:Concept>
  </xsl:template>

  <xsl:template name="contributor">
    <xsl:choose>
      <xsl:when test="Role='Author'">
        <lom-life:instructionaldesigner>
          <xsl:call-template name="contrib"/>
        </lom-life:instructionaldesigner>
      </xsl:when>
      <xsl:when test="Role='Contributor'">
        <dc:contributor>
          <xsl:call-template name="contrib"/>
        </dc:contributor>
      </xsl:when>
      <xsl:otherwise>
        <dc:contributor>
          <xsl:call-template name="contrib"/>
        </dc:contributor>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- ================ matched templates ============ -->

  <xsl:template match="CourseMetadata">
    <rdf:Description>
      <xsl:attribute name="rdf:about">&ocwroot;<xsl:value-of select="substring-after(Technical/Location, '/')"/></xsl:attribute>
      <rdf:type rdf:resource="&ocw;Lecture"/>
      <rdf:type rdf:resource="&lom-edu;LearningResourceType"/>
      <dc:source rdf:resource="http://simile.mit.edu/2004/06/collections#ocw"/>
      <xsl:for-each select="Classification/Keyword">
        <dc:subject>
          <xsl:call-template name="generateURI">
            <xsl:with-param name="controlledVocabularyType">Keyword</xsl:with-param>
          </xsl:call-template>
        </dc:subject>
      </xsl:for-each>
      <dc:title>
        <xsl:value-of select="General/Title"/>
      </dc:title>
      <dc:description>
        <xsl:value-of select="normalize-space(General/Description)"/>
      </dc:description>
      <lom-life:version>
        <xsl:value-of select="LifeCycle/Version"/>
      </lom-life:version>
      <xsl:for-each select="LifeCycle/Contribute">
        <xsl:call-template name="contributor"/>
      </xsl:for-each>
      <xsl:call-template name="aggregation"/>
    </rdf:Description>
  </xsl:template>
  
  <xsl:template name="location">
    <lom-tech:location>
      <xsl:value-of select="Technical/Location"/>
    </lom-tech:location>
  </xsl:template>
  
  <xsl:template name="aggregation">
    <xsl:if test="General/AggregationLevel=3">
      <!-- this is a course, so give it a label -->
      <rdfs:label>
        <xsl:value-of select="General/Title"/>
      </rdfs:label>
    </xsl:if>
    <lom-gen:aggregationlevel>
      <xsl:attribute name="rdf:resource">&lom-gen;Level<xsl:value-of select="General/AggregationLevel"/>
      </xsl:attribute>
    </lom-gen:aggregationlevel>
  </xsl:template>
  
  <xsl:template name="contrib">
    <xsl:variable name="tempname" select="normalize-space(Entity)"/>
    <xsl:variable name="name">
      <xsl:choose>
        <xsl:when test="$tempname='Dower, John W.'">
          <xsl:text>John Dower</xsl:text>
        </xsl:when>
        <xsl:when test="$tempname='Peters, W.T.'">
          <xsl:text>Peters, W. T.</xsl:text>
        </xsl:when>
        <xsl:when test="$tempname='Miyagawa, Shigeru'">
          <xsl:text>Shigeru Miyagawa</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$tempname"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <rdf:Description>
      <!-- lom:Entity -->
      <xsl:choose>
        <xsl:when test="$name='United States of America'">
          <xsl:attribute name="rdf:about">urn:iso:3166-1:US</xsl:attribute>
        </xsl:when>
        <xsl:when test="$name='Unknown'">
          <xsl:attribute name="rdf:about">urn:foo</xsl:attribute>
        </xsl:when>
        <xsl:when test="$name='Smithsonian Institution'">
          <xsl:attribute name="rdf:about">http://www.si.edu</xsl:attribute>
        </xsl:when>
        <xsl:when test="$name='Test Faculty'">
          <xsl:attribute name="rdf:about">urn:foo</xsl:attribute>
        </xsl:when>
        <xsl:when test="$name='Eye Wire Collection'">
          <xsl:attribute name="rdf:about">http://www.eyewire.com</xsl:attribute>
        </xsl:when>
        <xsl:when test="$name='Gleason''s Pictorial'">
          <xsl:attribute name="rdf:about">&ocwdata;Contributor#gleasons-pictorial</xsl:attribute>
        </xsl:when>
        <xsl:when test="$name='Getty Images'">
          <xsl:attribute name="rdf:about">http://www.gettyimages.com</xsl:attribute>
        </xsl:when>
        <xsl:when test="$name='Ryosenji Museum Collection'">
          <xsl:attribute name="rdf:about">http://www1.ocn.ne.jp/~ryosenji</xsl:attribute>
        </xsl:when>
        <xsl:otherwise>
          <xsl:attribute name="rdf:about">&ocwdata;Contributor#<xsl:value-of select="str:urlencode($name)"/>
          </xsl:attribute>
        </xsl:otherwise>
      </xsl:choose>
      <rdfs:label>
        <xsl:value-of select="$name"/>
      </rdfs:label>
      <rdf:type rdf:resource="&lom;Entity"/>
      <xsl:call-template name="canonicalizeName">
        <xsl:with-param name="name">
          <xsl:value-of select="$name"/>
        </xsl:with-param>
      </xsl:call-template>
    </rdf:Description>
  </xsl:template>
  
  <xsl:template match="MetaMetadata"/>
  
  <xsl:template match="ResourceMetadata">
    <rdf:Description>
      <!-- 
  <xsl:choose>
	    <xsl:when test="not(Technical/Location)">
	    	<xsl:attribute name="rdf:about">&ocwroot;<xsl:value-of select="str:urlencode(General/Title)"/></xsl:attribute>
	    </xsl:when>
	    <xsl:when test="substring(Technical/Location, 1, 1)='/'">
		<xsl:attribute name="rdf:about">&ocwroot;<xsl:value-of select="substring-after(Technical/Location, '/')"/></xsl:attribute>
	    </xsl:when>
	    <xsl:otherwise>
		<xsl:attribute name="rdf:about"><xsl:value-of select="Technical/Location"/></xsl:attribute>
	    </xsl:otherwise>
	</xsl:choose> 
  -->
      <!-- always make up URLs to avoid loops -->
      <xsl:attribute name="rdf:about">&ocwdata;LearningResource#<xsl:value-of select="str:urlencode(General/Title)"/></xsl:attribute>
      <xsl:choose>
        <xsl:when test="Educational/LearningResourceType='Diagram'">
          <rdf:type rdf:resource="&lom-edu;Diagram"/>
          <rdf:type rdf:resource="&lom-edu;LearningResourceType"/>
        </xsl:when>
        <xsl:when test="Educational/LearningResourceType='Exam'">
          <rdf:type rdf:resource="&lom-edu;Exam"/>
          <rdf:type rdf:resource="&lom-edu;LearningResourceType"/>
        </xsl:when>
        <xsl:when test="Educational/LearningResourceType='Exercise'">
          <rdf:type rdf:resource="&lom-edu;Exercise"/>
          <rdf:type rdf:resource="&lom-edu;LearningResourceType"/>
        </xsl:when>
        <xsl:when test="Educational/LearningResourceType='Figure'">
          <rdf:type rdf:resource="&lom-edu;Figure"/>
          <rdf:type rdf:resource="&lom-edu;LearningResourceType"/>
        </xsl:when>
        <xsl:when test="Educational/LearningResourceType='Index'">
          <rdf:type rdf:resource="&lom-edu;Index"/>
          <rdf:type rdf:resource="&lom-edu;LearningResourceType"/>
        </xsl:when>
        <xsl:when test="Educational/LearningResourceType='Narrative Text'">
          <rdf:type rdf:resource="&lom-edu;NarrativeText"/>
          <rdf:type rdf:resource="&lom-edu;LearningResourceType"/>
        </xsl:when>
        <xsl:when test="Educational/LearningResourceType='Questionnaire'">
          <rdf:type rdf:resource="&lom-edu;Questionnaire"/>
          <rdf:type rdf:resource="&lom-edu;LearningResourceType"/>
        </xsl:when>
        <xsl:when test="Educational/LearningResourceType='Simulation'">
          <rdf:type rdf:resource="&lom-edu;Simulation"/>
          <rdf:type rdf:resource="&lom-edu;LearningResourceType"/>
        </xsl:when>
        <xsl:when test="Educational/LearningResourceType='Slide'">
          <rdf:type rdf:resource="&lom-edu;Slide"/>
          <rdf:type rdf:resource="&lom-edu;LearningResourceType"/>
        </xsl:when>
        <xsl:when test="Educational/LearningResourceType='Table'">
          <rdf:type rdf:resource="&lom-edu;Table"/>
          <rdf:type rdf:resource="&lom-edu;LearningResourceType"/>
        </xsl:when>
        <!-- these types are not in the base lom ontology -->
        <xsl:when test="Educational/LearningResourceType='Laboratory'">
          <rdf:type rdf:resource="&ocw;Laboratory"/>
          <rdf:type rdf:resource="&lom-edu;LearningResourceType"/>
        </xsl:when>
        <xsl:when test="Educational/LearningResourceType='Bibliography'">
          <rdf:type rdf:resource="&ocw;Bibliography"/>
          <rdf:type rdf:resource="&lom-edu;LearningResourceType"/>
        </xsl:when>
        <xsl:when test="Educational/LearningResourceType='Calendar'">
          <rdf:type rdf:resource="&ocw;Calendar"/>
          <rdf:type rdf:resource="&lom-edu;LearningResourceType"/>
        </xsl:when>
        <xsl:when test="Educational/LearningResourceType='Lecture Notes'">
          <rdf:type rdf:resource="&ocw;LectureNotes"/>
          <rdf:type rdf:resource="&lom-edu;LearningResourceType"/>
        </xsl:when>
        <xsl:when test="Educational/LearningResourceType='Syllabus'">
          <rdf:type rdf:resource="&ocw;Syllabus"/>
          <rdf:type rdf:resource="&lom-edu;LearningResourceType"/>
        </xsl:when>
        <xsl:when test="Educational/LearningResourceType='Select Resource'">
          <rdf:type rdf:resource="&ocw;SelectResource"/>
          <rdf:type rdf:resource="&lom-edu;LearningResourceType"/>
        </xsl:when>
        <xsl:when test="Educational/LearningResourceType='Problem set'">
          <rdf:type rdf:resource="&ocw;ProblemSet"/>
          <rdf:type rdf:resource="&lom-edu;LearningResourceType"/>
        </xsl:when>
      </xsl:choose>
      <dc:title>
        <xsl:value-of select="General/Title"/>
      </dc:title>
      <dc:source rdf:resource="&ocwsource;"/>
      <dc:description>
        <xsl:value-of select="normalize-space(General/Description)"/>
      </dc:description>
      <xsl:for-each select="LifeCycle/Contribute">
        <xsl:call-template name="contributor"/>
      </xsl:for-each>
      <xsl:call-template name="aggregation"/>
      <xsl:if test="Technical/Location">
        <ocw:linkToImage>
          <xsl:attribute name="rdf:resource">
            <xsl:choose>
              <xsl:when test="matches(Technical/Location,'http://.*')">
                <xsl:value-of select="Technical/Location"/>
              </xsl:when>
              <xsl:otherwise>&ocwroot;<xsl:value-of select="substring-after(Technical/Location, '/')"/></xsl:otherwise>
            </xsl:choose>
          </xsl:attribute>
        </ocw:linkToImage>
      </xsl:if>
      <xsl:if test="Relation/Kind = 'Is Part Of'">
        <dcterms:isPartOf>
          <rdf:Description>
            <xsl:attribute name="rdf:about">&ocwroot;<xsl:value-of select="substring-after(//CourseMetadata/Technical/Location, '/')"/></xsl:attribute>
          </rdf:Description>
        </dcterms:isPartOf>
      </xsl:if>
      <!-- this is a hack, will fail if we have more than 3 contributors -->
      <xsl:variable name="contributor1" select="LifeCycle/Contribute[1]/Entity"/>
      <xsl:variable name="contributor2" select="LifeCycle/Contribute[2]/Entity"/>
      <xsl:variable name="contributor3" select="LifeCycle/Contribute[3]/Entity"/>
      <xsl:for-each select="Classification/Keyword">
        <xsl:choose>
          <xsl:when test="(.=$contributor1) or (.=$contributor2) or (.=contributor3)">
            <!-- don't create a dc:subject that duplicates dc:creator-->
          </xsl:when>
          <xsl:otherwise>
            <dc:subject>
              <xsl:call-template name="generateURI">
                <xsl:with-param name="controlledVocabularyType">Keyword</xsl:with-param>
              </xsl:call-template>
            </dc:subject>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:for-each>
    </rdf:Description>
  </xsl:template>
  
  <xsl:template match="SectionMetadata"/>
  
</xsl:stylesheet>
