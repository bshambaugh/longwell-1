<?xml version='1.0'?>

<!--+
    | This stylesheet converts Harvard VIA data to RDF 
    |
    | Author: Stefano Mazzocchi  
    | Date: 18 June 2004
    +-->
    
<!DOCTYPE xsl:stylesheet [
    <!ENTITY xsl     'http://www.w3.org/1999/XSL/Transform'>	
    <!ENTITY xsd     'http://www.w3.org/2001/XMLSchema'>
    <!ENTITY rdf     'http://www.w3.org/1999/02/22-rdf-syntax-ns#'>
    <!ENTITY rdfs    'http://www.w3.org/2000/01/rdf-schema#'>
    <!ENTITY skos    'http://www.w3.org/2004/02/skos/core#'>
    <!ENTITY dc 		  'http://purl.org/dc/elements/1.1/'>
    <!ENTITY dcterms 'http://purl.org/dc/terms/'>
    <!ENTITY vb      'viaBatch.xsd'>
    <!ENTITY via     'http://hul.harvard.edu/ois/xml/xsd/via/newvia.xsd'>
    <!ENTITY svia    'http://simile.mit.edu/2004/06/ontologies/via#'>    
    <!ENTITY viadata 'http://simile.mit.edu/2004/06/via/'>
    <!ENTITY geo     'http://simile.mit.edu/2004/06/ontologies/geography#'>
    <!ENTITY places  'http://simile.mit.edu/2004/06/places#'>
    <!ENTITY viasource 'http://simile.mit.edu/2004/06/collections#via'>
    <!ENTITY str 	   'http://simile.mit.edu/2004/01/xslt/common'>	
    <!ENTITY wrong-xlink   'http://www.w3.org/TR/xlink'>
]>

<xsl:stylesheet version="1.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:wrong-xlink="&wrong-xlink;"
    xmlns:rdf="&rdf;" 
    xmlns:rdfs="&rdfs;" 
    xmlns:xsd="&xsd;"
    xmlns:dc="&dc;" 
    xmlns:dcterms="&dcterms;" 
    xmlns:vb="&vb;"
    xmlns:via="&via;" 
    xmlns:svia="&svia;" 
    xmlns:geo="&geo;" 
    xmlns:skos="&skos;"
    xmlns:str="&str;"
    exclude-result-prefixes="vb via str xsd wrong-xlink">
    
  <xsl:import href="../../../stylesheets/common.xsl"/>
  
  <xsl:output method="xml" version="1.0" encoding="iso-8859-1" indent="yes"/>
  
  <xsl:template match="vb:viaBatch">
    <rdf:RDF> 
      <xsl:apply-templates/>
    </rdf:RDF>
  </xsl:template>

  <xsl:template match="vb:header">
    <rdf:Description rdf:about="&viasource;">
      <rdf:type rdf:resource="http://simile.mit.edu/2003/10/ontologies/artstor#Collection"/>
      <rdfs:label>Harvard VIA</rdfs:label>
      <dc:contributor><xsl:value-of select="vb:contributor"/></dc:contributor>
      <dc:date><xsl:value-of select="vb:batchDate"/></dc:date>
    </rdf:Description>
  </xsl:template>

  <!-- ============================= Named Templates ========================= -->

  <xsl:template name="generateURI">
    <xsl:param name="controlledVocabularyType"/>
    <skos:Concept rdf:about="&viadata;{$controlledVocabularyType}#{str:urlencode(.)}">
      <xsl:choose>
        <xsl:when test="via:term">
         <rdfs:label><xsl:value-of select="via:term"/></rdfs:label>
         <skos:prefLabel><xsl:value-of select="via:term"/></skos:prefLabel>
        </xsl:when>
        <xsl:otherwise>
         <rdfs:label><xsl:value-of select="."/></rdfs:label>
         <skos:prefLabel><xsl:value-of select="."/></skos:prefLabel>
        </xsl:otherwise>
      </xsl:choose>
      <skos:inScheme rdf:resource="&svia;{$controlledVocabularyType}"/>
      <xsl:apply-templates select="via:source"/>
    </skos:Concept>
  </xsl:template>

  <!-- ============================= Complex Types ========================= -->

<!--+
    | viaRecord
    |  @images
    |  @originalAtHarvard
    |  recordId
    |  group|work
		|  admin?
		|
		| A record consisting of a group description (with subworks and optional surrogates) 
		| or a work description (with optional surrogates), plus a section of administrative 
		| information about the record.
		+-->
  <xsl:template match="via:viaRecord[@images='true']">
    <svia:Record rdf:about="&viadata;records/{str:urlencode(via:recordId)}">
      <dc:source rdf:resource="&viasource;"/>
      <xsl:apply-templates/>
    </svia:Record>
  </xsl:template>

<!--+
    | group
    |  image*
    |  title*
    |  itemIdentifier*
    |  classification*
    |  workType*
    |  creator*
    |  production*
    |  structuredDate*
    |  freeDate*
    |  description*
    |  physicalDescription*
    |  dimensions*
    |  associatedName*
    |  placeName*
    |  topic*
    |  style*
    |  culture*
    |  materials*
    |  notes*
    |  location*
    |  copyright*
    |  relatedWork*
    |  relatedInformation*
    |  useRestrictions*
    |  repository*
    |  surrogate*
    |  subwork+
		|
		| An aggregate description of multiple works, sites, or objects, some of which must 
		| also be individually described as subworks within this record. Where multiple 
		| works, site, or objects are described together, but none are described separately, 
		| use the work element fo the aggregate description. 
		+-->
  <xsl:template match="via:group">
    <xsl:apply-templates/>
  </xsl:template>

<!--+
    | work
    |  image*
    |  title+
    |  itemIdentifier*
    |  classification*
    |  workType*
    |  creator*
    |  production*
    |  structuredDate*
    |  freeDate*
    |  state*
    |  description*
    |  physicalDescription*
    |  dimensions*
    |  assocatedName*
    |  placeName*
    |  topic*
    |  style*
    |  culture*
    |  materials*
    |  notes*
    |  location*
    |  copyright*
    |  relatedwork*
    |  relatedInformation*
    |  useRestrictions*
    |  repository*
    |  surrogate*
    |
    |  A work element describes a single object, site, etc. It should also be used 
    |  for descriptions of multiple objects, sites, etc. when no individual 
    |  descriptions or the components are present.
    +-->
  <xsl:template match="via:work">
    <xsl:apply-templates/>
  </xsl:template>

<!--+
    | subwork
    |   @componentID?
    |  image*
    |  title*
    |  itemIdentifier*
    |  classification*
    |  workType*
    |  creator*
    |  production*
    |  structuredDate*
    |  freeDate*
    |  state*
    |  description*
    |  physicalDescription*
    |  dimensions*
    |  associatedName*
    |  placeName*
    |  topic*
    |  style*
    |  culture*
    |  materials*
    |  notes*
    |  location*
    |  copyright*
    |  relatedwork*
    |  relatedInformation*
    |  useRestrictions*
    |  repository*
    |  surrogate*
    |  
    | An individually described unit within a larger described group,
    |  e.g. a cup in a teaset, one photo in a folder, one building in a complex.
    +-->
  <xsl:template match="via:subwork">
    <svia:contains>
      <svia:Subwork rdf:about="&viadata;surrogates/{str:urlencode(@componentID)}">
        <dc:source rdf:resource="&viasource;"/>
        <xsl:apply-templates/>
      </svia:Subwork>
    </svia:contains>
  </xsl:template>

<!--+
    | surrogate
    |   @componentID?
    |  image*
    |  title*
    |  itemIdentifier*
    |  classification*
    |  workType*
    |  creator*
    |  production*
    |  structuredDate*
    |  freeDate*
    |  state*
    |  description*
    |  physicalDescription*
    |  dimensions*
    |  assocatedName*
    |  placeName*
    |  topic*
    |  style*
    |  culture*
    |  materials*
    |  notes*
    |  location*
    |  copyright*
    |  relatedwork*
    |  relatedInformation*
    |  useRestrictions*
    |  repository
    |
    | Description of a single image representing the described work, subwork, or group. 
    | Surrogate descriptions in VIA represent instances and must be identified as belonging 
    | to a particular repository, e.g., Countway's copy of a photograph, not the abstraction 
    | of a photograph which could exist in separate copies in different locations. 
    | However, a given surrogate can represent both a print photograph 
    | and its digital representation(s).
    |
    +-->
  <xsl:template match="via:surrogate[via:image/@restrictedImage='false']">
    <svia:contains>
      <svia:Surrogate rdf:about="&viadata;surrogates/{str:urlencode(@componentID)}">
        <dc:source rdf:resource="&viasource;"/>
        <xsl:apply-templates/>
      </svia:Surrogate>
    </svia:contains>
  </xsl:template>
  
  <xsl:template match="via:surrogate">
    <!-- ignore for now -->
  </xsl:template>

<!--+
    | associatedName
    |  nameElement
    |  (dates|place|nationality|role|source)*
    |
    | Use for names other than creators and producers/publishers. 
    | E.g., use for donors, patrons, former owners, sitters, etc.
    +-->
  <xsl:template match="via:associatedName">
    <xsl:choose>
      <xsl:when test="role">
        <rdf:Description rdf:type="&svia;{role}">
          <rdf:value><xsl:value-of select="nameElement"/></rdf:value>
          <xsl:apply-templates/>
        </rdf:Description>
      </xsl:when>
      <xsl:otherwise>
        <xsl:comment>ignored associatedName "<xsl:value-of select="nameElement"/>because it has no role</xsl:comment>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

<!--+    
    | place
    +-->
  <xsl:template match="via:associatedName/via:place">
    <!-- ignore for now -->
  </xsl:template>

<!--+    
    | dates
    +-->
  <xsl:template match="via:dates">
    <!-- ignore for now -->
  </xsl:template>
  
<!--+    
    | nationality
    +-->
  <xsl:template match="via:nationality">
    <!-- ignore for now -->
  </xsl:template>

<!--+
    | culture
    |  term
    |  source?
    +-->
  <xsl:template match="via:culture">
    <svia:culture>
      <xsl:call-template name="generateURI">
        <xsl:with-param name="controlledVocabularyType">Culture</xsl:with-param>
      </xsl:call-template>
    </svia:culture>
  </xsl:template>
  
<!--+
    | image
    |  @restrictedImage[xs:boolean]
    |  @wrong-xlink:href
    |  caption?
    |  thumbnail?
    +-->
  <xsl:template match="via:image">
    <!--svia:hasImage>
      <svia:Image rdf:about="{@wrong-xlink:href}">
        <svia:restricted><xsl:value-of select="@restrictedImage"/></svia:restricted>
        <svia:linkToImage><xsl:value-of select="@wrong-xlink:href"/></svia:linkToImage>
        <svia:linkToThumbnail><xsl:value-of select="via:thumbnail/@wrong-xlin:href"/></svia:linkToThumbnail>
      </svia:Image>
    </svia:hasImage-->
    <!-- FIXME(SM) this is a hack to make it work in longwell since it's not able to 
         recurse into nested RDF nodes (yet, hopefully) --> 
    <svia:hasImage rdf:resource="{@wrong-xlink:href}"/>
  </xsl:template>

<!--+
    | thumbnail
    |  @wrong-xlink:href
    |  @wrong-xlink:show
    |  @wrong-xlink:actuate
    +-->
  <xsl:template match="via:thumbnail">
    <!-- apparently the system is screwed and the thumbnail value is not connected to the thumbnails at all! -->
    <!--svia:linkToThumbnail><xsl:value-of select="@wrong-xlink:href"/></svia:linkToThumbnail-->
  </xsl:template>

<!--+    
    | caption
    +-->
  <xsl:template match="via:caption">
    <svia:caption><xsl:value-of select="."/></svia:caption>
  </xsl:template>

<!--+
    | itemIdentifier
    |  type?
    |  number
    +-->
  <xsl:template match="via:itemIdentifier">
  </xsl:template>

<!--+
    | link
    |  @wrong-xlink:href
    |  @wrong-xlink:show
    |  @wrong-xlink:actuate
    +-->
  <xsl:template match="via:link">
  </xsl:template>

<!--+
    | location
    |  type?
    |  place
    |  source?
    |  geodata?
    +-->
  <xsl:template match="via:location">
    <svia:location>
      <geo:Place rdf:about="&places;{str:urlencode(via:place)}">
        <geo:name><xsl:value-of select="via:place"/></geo:name>
        <xsl:apply-templates select="via:geodata"/>
        <!-- NOTE(SM): ignoring type and source for now -->
      </geo:Place>
    </svia:location>
  </xsl:template>

<!--+
    | placeName
    |  place
    |  source?
    |  geodata?
    +-->
  <xsl:template match="via:placeName">
    <svia:location>
      <geo:Place rdf:about="&places;{str:urlencode(via:place)}">
        <geo:name><xsl:value-of select="via:place"/></geo:name>
        <xsl:apply-templates select="via:geodata"/>
        <!-- NOTE(SM): ignoring source for now -->
      </geo:Place>
    </svia:location>
  </xsl:template>

<!--+
    | production
    |  placeOfProduction*
    |  producer?
    |  role?
    +-->
  <xsl:template match="via:production">
    <svia:producedBy>
      <svia:Production>
       <xsl:apply-templates/>
      </svia:Production>
    </svia:producedBy>
  </xsl:template>

<!--+
    | placeOfProduction
    |  place
    |  geodata?
    +-->
  <xsl:template match="via:placeOfProduction">
    <svia:placeOfProduction>
      <geo:Place rdf:about="&places;{str:urlencode(via:place)}">
        <geo:name><xsl:value-of select="via:place"/></geo:name>
        <xsl:apply-templates/>
      </geo:Place>
    </svia:placeOfProduction>
  </xsl:template>

<!--+    
    | place
    +-->
  <xsl:template match="via:placeOfProduction/via:place">
    <!-- ignore for now -->
  </xsl:template>

<!--+
    | geodata
    |  wordRegion?
    |  country?
    |  stateProvince?
    |  siteCity*
    |  coordinates?
    +-->
  <xsl:template match="via:geodata">
    <xsl:apply-templates/>
  </xsl:template>
  
<!--+
    | relatedInformation[xs:string]
    |  @wrong-xlink:href
    |  @wrong-xlink:show
    |  @wrong-xlink:actuate
    +-->
  <xsl:template match="via:relatedInformation">
  </xsl:template>

<!--+
    | relatedWork
    |  relationship? 
    |   A string which displays in front of the remaining subelements, 
    |   e.g., a phrase such as "Is part of".
    |  textElement
    |   This is the title of the related work.
    |  (creator|production|freeDate|structuredDate)*
    |  link?
    +-->
  <xsl:template match="via:relatedWork">
    <!-- ignore for now -->
  </xsl:template>

<!--+
    | repository
    |  repositoryName 
    |  (note|number)*
    +-->
  <xsl:template match="via:repository">
    <!-- ignore for now -->
  </xsl:template>

<!--+
    | structuredDate
    |  beginDate
    |  endDate?
    +-->
  <xsl:template match="via:structuredDate">
    <xsl:apply-templates/>
  </xsl:template>
  
<!--+
    | style
    |  term
    |  source?
    +-->
  <xsl:template match="via:style">
    <svia:style>
      <xsl:call-template name="generateURI">
        <xsl:with-param name="controlledVocabularyType">Style</xsl:with-param>
      </xsl:call-template>
    </svia:style>
  </xsl:template>
    
<!--+
    | title
    |  type?
    |  textElement
    +-->
  <xsl:template match="via:title">
    <xsl:choose>
      <xsl:when test="via:type = 'Alternate'">
        <dcterms:alternate><xsl:value-of select="via:textElement"/></dcterms:alternate>
      </xsl:when>
      <xsl:otherwise>
        <dc:title><xsl:value-of select="via:textElement"/></dc:title>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
<!--+
    | topic
    |  term
    |  source?
    +-->
  <xsl:template match="via:topic">
    <svia:topic>
      <xsl:call-template name="generateURI">
        <xsl:with-param name="controlledVocabularyType">Topic</xsl:with-param>
      </xsl:call-template>
    </svia:topic>
  </xsl:template>
  
  <!-- =========================== Integers ======================================= -->

<!--+
    | beginDate[xs:integer]
    | endDate[xs:integer]
    +-->
  <xsl:template match="via:beginDate">
    <svia:beginDate><xsl:value-of select="."/></svia:beginDate>
  </xsl:template>
  
  <xsl:template match="via:endDate">
    <svia:endDate><xsl:value-of select="."/></svia:endDate>
  </xsl:template>

  <!-- =========================== Strings ======================================= -->

  <!--+
      |  coordinates[xs:string]
      |
      |  Coordinates is a non-displaying, non-searched value to be used 
      |  prospectively in any mapping services developed for VIA. 
      |  Information that is intended to be searched and displayed about 
      |  a place should be supplied in the "place" element under the 
      |  placeName, production, or location elements. The VIA coordinates 
      |  element is a copy of the OpenGIS Geography Markup Language 
      |  Specification 3.0 CoordinatesType.
      +-->
  <xsl:template match="via:coordinates">
    <!-- FIXME(SM) this should be parsed into something like
    
         <wgs84:lat>...</wgs84:lat>
         <wgs84:long>...</wgs84:long>
         
         but let's leave it like this for now.
     -->
     <geo:coordinates><xsl:value-of select="."/></geo:coordinates>
  </xsl:template>
  
  <!--+
      | country[xs:string]
      |
      | Use country for the name of a country or other geographic 
      | unit that is the next geographic unit smaller than a 
      | continent or worldRegion. country is a non-displaying, 
      | non-searched value to be used prospectively in any 
      | mapping services developed for VIA. Information that is 
      | intended to be searched and displayed about a place 
      | should be supplied in the "place" element under the 
      | placeName, production, or location elements. 
      +-->
  <xsl:template match="via:country">
    <geo:country><xsl:value-of select="."/></geo:country>
  </xsl:template>
  
  <!--+
	    | creator[xs:string]
	    | 
	    | Use for people and organizations that have a 
	    | creative responsibility for the object(s), site(s), etc. described.
	    +-->
  <xsl:template match="via:creator">
    <dc:creator><xsl:value-of select="."/></dc:creator>
  </xsl:template>
  
  <!--+
	    | producer[xs:string]
	    |
	    | Use for names of people or organizations that have a non-creative role 
	    | in the production of the object(s), site(s), etc. described. 
	    | Manufacturers and publishers belong in this element.
	    +-->
  <xsl:template match="via:producer">
    <svia:producer><xsl:value-of select="."/></svia:producer>
  </xsl:template>
  
  <!--+
      | siteCity[xs:string]
      | 
      | Use siteCity for the name of a site, city, etc.  
      | siteCity is repeatable within the geodata element, 
      | and can be used for all geographic units smaller 
      | than a state or province. siteCity is a non-displaying, 
      | non-searched value to be used prospectively in any 
      | mapping services developed for VIA. Information that is 
      | intended to be searched and displayed about a place 
      | should be supplied in the "place" element under the 
      | placeName, production, or location elements.
      +-->
  <xsl:template match="via:siteCity">
    <geo:site><xsl:value-of select="."/></geo:site>
  </xsl:template>
  
  <!--+
      | stateProvince[xs:string]
      | 
      | Use stateProvince for the name of a state, province, 
      | district, or region that is the next geographic unit 
      | smaller than a country. stateProvince is a non-displaying, 
      | non-searched value to be used prospectively in any mapping 
      | services developed for VIA. Information that is intended 
      | to be searched and displayed about a place should be supplied 
      | in the "place" element under the placeName, production, or 
      | location elements.
      +-->
  <xsl:template match="via:stateProvince">
    <geo:province><xsl:value-of select="."/></geo:province>
  </xsl:template>
  
  <!--+
      | worldRegion[xs:string]
      | 
      | Use worldRegion for the name of a continent or other geographic 
      | unit larger than a country. worldRegion is a non-displaying, 
      | non-searched value to be used prospectively in any mapping 
      | services developed for VIA. Information that is intended 
      | to be searched and displayed about a place should be supplied 
      | in the "place" element under the placeName, production, 
      | or location elements. 
      +-->
  <xsl:template match="via:worldRegion">
    <geo:region><xsl:value-of select="."/></geo:region>
  </xsl:template>
  
<!--+
    | copyright
    +-->
  <xsl:template match="via:copyright">
    <!-- ignore for now -->
  </xsl:template>

<!--+    
    | description
    +-->
  <xsl:template match="via:description">
    <dc:description><xsl:value-of select="."/></dc:description>
  </xsl:template>

<!--+    
    | physicalDescriptions
    +-->
  <xsl:template match="via:physicalDescriptions">
    <!-- ignore for now -->
  </xsl:template>
  
<!--+    
    | dimensions
    +-->
  <xsl:template match="via:dimensions">
    <!-- ignore for now -->
  </xsl:template>

<!--+    
    | freeDate
    +-->
  <xsl:template match="via:freeDate">
    <!-- ignore for now -->
  </xsl:template>

<!--+    
    | materials
    +-->
  <xsl:template match="via:materials">
    <!-- ignore for now -->
  </xsl:template>

<!--+    
    | note
    +-->
  <xsl:template match="via:note">
    <!-- ignore for now -->
  </xsl:template>
  
<!--+    
    | notes
    +-->
  <xsl:template match="via:notes">
    <!-- ignore for now -->
  </xsl:template>
  
<!--+    
    | number
    +-->
  <xsl:template match="via:number">
    <!-- ignore for now -->
  </xsl:template>

<!--+    
    | relationship
    +-->
  <xsl:template match="via:relationship">
    <!-- ignore for now -->
  </xsl:template>

<!--+    
    | repositoryName
    +-->
  <xsl:template match="via:repositoryName">
    <!-- ignore for now -->
  </xsl:template>

<!--+    
    | state
    +-->
  <xsl:template match="via:state">
    <!-- ignore for now -->
  </xsl:template>

<!--+    
    | term
    +-->
  <xsl:template match="via:term">
    <!-- ignore for now -->
  </xsl:template>

<!--+    
    | useRestrictions
    +-->
  <xsl:template match="via:useRestrictions">
    <!-- ignore for now -->
  </xsl:template>

<!--+
    | workType
    +-->
  <xsl:template match="via:workType">
    <svia:workType>
      <xsl:call-template name="generateURI">
        <xsl:with-param name="controlledVocabularyType">WorkType</xsl:with-param>
      </xsl:call-template>
    </svia:workType>
  </xsl:template>
  
  <!-- ============== Ignored because used directly  ============ -->

<!--+    
    | recordID
    +-->
  <xsl:template match="via:recordID"/>

<!--+    
    | type
    +-->
  <xsl:template match="via:type"/>

<!--+    
    | textElement
    +-->
  <xsl:template match="via:textElement"/>

<!--+
    | nameElement[xs:string]
    |
    | Used in the creator and associatedName elements, 
    | and by extension in the relatedWork element. 
    +-->
  <xsl:template match="via:nameElement"/>
  
<!--+    
    | role
    +-->
  <xsl:template match="via:role"/>

<!--+    
    | source
    +-->
  <xsl:template match="via:source"/>

  <!-- ============== Ignored because not currently important  ============ -->

<!--+
    | classification
    |  type*
    |  number
    |
    +-->
  <xsl:template match="via:classification">
    <!-- ignore for now -->
  </xsl:template>

<!--+
    | admin
    |  term
    |  source?
    +-->
  <xsl:template match="via:admin">
    <!-- NOTE (SM): probably this information it's non for public consuption anyway -->
  </xsl:template>
  
<!--+    
    | everything that is not processed is removed (this helped to avoid whitespace problems)
    +-->
  <xsl:template match="*|@*|text()"/>
  
</xsl:stylesheet>
