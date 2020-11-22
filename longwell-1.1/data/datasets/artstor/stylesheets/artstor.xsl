<?xml version='1.0'?>

<!--+
    | This stylesheet converts ARTStor data to RDF 
    |
    |  Author: Mark Butler mark-h.butler@hp.com 
    |  Date: 08 October 2003
    +-->
    
<!DOCTYPE xsl:stylesheet [
    <!ENTITY xsd     'http://www.w3.org/2001/XMLSchema'>
    <!ENTITY rdf     'http://www.w3.org/1999/02/22-rdf-syntax-ns#'>
    <!ENTITY rdfs    'http://www.w3.org/2000/01/rdf-schema#'>
    <!ENTITY vc      'http://www.w3.org/2001/vcard-rdf/3.0#'>
    <!ENTITY xsl     'http://www.w3.org/1999/XSL/Transform'>	
    <!ENTITY skos    'http://www.w3.org/2004/02/skos/core#'>
    <!ENTITY dc      'http://purl.org/dc/elements/1.1/'>
    <!ENTITY str     'http://simile.mit.edu/2004/01/xslt/common'>
    <!ENTITY art     'http://simile.mit.edu/2003/10/ontologies/artstor#'>  
    <!ENTITY vra     'http://simile.mit.edu/2003/10/ontologies/vraCore3#'>
    <!ENTITY person	  'http://simile.mit.edu/2003/10/ontologies/person#'>
    <!ENTITY artstordata 	  'http://simile.mit.edu/2003/10/artstor/'>
    <!ENTITY artstorsource 	'http://simile.mit.edu/2004/06/collections#artstor'>
    
]>

<xsl:stylesheet version="1.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:rdf="&rdf;" 
    xmlns:rdfs="&rdfs;" 
    xmlns:person="&person;"
    xmlns:str="&str;" 
    xmlns:xsd="&xsd;" 
    xmlns:art="&art;"
    xmlns:dc="&dc;" 
    xmlns:vc="&vc;"
    xmlns:vra="&vra;" 
    xmlns:skos="&skos;">
    
  <xsl:import href="../../../stylesheets/common.xsl"/>

  <xsl:output method="xml" version="1.0" encoding="iso-8859-1" indent="yes"/>

  <!-- Functions and templates called by name in stylesheet -->

  <xsl:template name="generateUntypedURI">
    <xsl:param name="value"/>
    <xsl:param name="type"/>
    <xsl:attribute name="rdf:about">&artstordata;<xsl:value-of select="$type"/>#<xsl:value-of select="str:urlencode($value)"/></xsl:attribute>
    <skos:prefLabel>
      <xsl:value-of select="$value"/>
    </skos:prefLabel>
  </xsl:template>

  <xsl:template name="generateURI">
    <xsl:param name="controlledVocabularyType"/>
    <skos:Concept>
      <xsl:call-template name="generateUntypedURI">
        <xsl:with-param name="value" select="."/>
        <xsl:with-param name="type" select="$controlledVocabularyType"/>
      </xsl:call-template>
      <skos:inScheme>
        <xsl:attribute name="rdf:resource">&art;<xsl:value-of select="$controlledVocabularyType"/>
        </xsl:attribute>
      </skos:inScheme>
    </skos:Concept>
  </xsl:template>

  <xsl:template name="generateSiteURI">
    <rdf:Description>
      <xsl:variable name="value" select="."/>
      <xsl:variable name="type">Site</xsl:variable>
      <xsl:attribute name="rdf:about">&artstordata;<xsl:value-of
          select="$type"/>#<xsl:value-of select="str:urlencode($value)"/>
      </xsl:attribute>
      <rdfs:label>
        <xsl:value-of select="$value"/>
      </rdfs:label>
      <rdf:type>
        <xsl:attribute name="rdf:resource">&art;<xsl:value-of select="$type"/>
        </xsl:attribute>
      </rdf:type>
    </rdf:Description>
  </xsl:template>

  <xsl:template name="splitTerm">
    <xsl:param name="property"/>
    <xsl:param name="propertyNamespace"/>
    <xsl:param name="class"/>
    <xsl:param name="classNamespace"/>
    <xsl:param name="value"/>
    <xsl:param name="token"/>
    <xsl:element name="{$property}" namespace="{$propertyNamespace}">
      <skos:Concept>
        <xsl:call-template name="generateUntypedURI">
          <xsl:with-param name="value" select="$value"/>
          <xsl:with-param name="type" select="$class"/>
        </xsl:call-template>
        <skos:inScheme>
          <xsl:attribute name="rdf:resource">
            <xsl:value-of select="$propertyNamespace"/>
            <xsl:value-of select="$property"/>
          </xsl:attribute>
        </skos:inScheme>
        <xsl:if test="contains($value,$token)">
          <xsl:variable name="tokenized" select="tokenize($value,$token)"/>
          <skos:broader>
            <skos:Concept>
              <xsl:call-template name="generateUntypedURI">
                <xsl:with-param name="value" select="normalize-space($tokenized[1])"/>
                <xsl:with-param name="type" select="$class"/>
              </xsl:call-template>
              <skos:inScheme>
                <xsl:attribute name="rdf:resource">
                  <xsl:value-of select="$propertyNamespace"/>
                  <xsl:value-of select="$property"/>
                </xsl:attribute>
              </skos:inScheme>
            </skos:Concept>
          </skos:broader>
        </xsl:if>
      </skos:Concept>
    </xsl:element>
  </xsl:template>

  <!-- Main stylesheet -->

  <xsl:template match="DOC">
    <rdf:RDF 
        xmlns:rdf="&rdf;" 
        xmlns:rdfs="&rdfs;"
        xmlns:xsd="&xsd;" 
        xmlns:art="&art;"
        xmlns:dc="&dc;" 
        xmlns:person="&person;" 
        xmlns:vc="&vc;" 
        xmlns:vra="&vra;" 
        xmlns:skos="&skos;">
        
      <xsl:apply-templates/>
      
      <rdf:Description rdf:about="&artstorsource;">
        <rdf:type rdf:resource="http://simile.mit.edu/2003/10/ontologies/artstor#Collection"/>
        <rdfs:label>ARTstor</rdfs:label>
      </rdf:Description>
    </rdf:RDF>
  </xsl:template>
  
  <xsl:template match="Image">
    <vra:Image>
      <xsl:attribute name="rdf:about">&artstordata;Id#<xsl:value-of select="./ID"/></xsl:attribute>
      <xsl:apply-templates select="ID"/>
      <xsl:apply-templates select="Creation_Date"/>
      <xsl:apply-templates select="Update_Date"/>
      <xsl:if test="not(normalize-space(./Source)='')">
        <art:imageSource>
          <rdf:Description>
            <xsl:attribute name="rdf:about">&artstordata;Source#<xsl:value-of select="./Source"/></xsl:attribute>
            <rdfs:label>
              <xsl:value-of select="./Source"/>
            </rdfs:label>
            <rdf:type rdf:resource="&art;Source"/>
          </rdf:Description>
        </art:imageSource>
      </xsl:if>
      <xsl:apply-templates select="Image_Id"/>
      <xsl:apply-templates select="Object_Id"/>
      <xsl:apply-templates select="Collection"/>
      <xsl:apply-templates select="MediaFiles"/>
      <xsl:apply-templates select="MetaData"/>
      <art:thumbnail>
        <xsl:attribute name="rdf:resource">http://simile.mit.edu/data/artstor/d<xsl:value-of select="MediaFiles/MediaFile/@lpsid"/>/<xsl:value-of select="Image_Id"/>.jpg</xsl:attribute>
      </art:thumbnail>
    </vra:Image>
  </xsl:template>
  <xsl:template match="ID"/>
  <xsl:template match="Image_Id">
    <art:imageId>
      <xsl:attribute name="rdf:resource">&artstordata;Image_id#<xsl:value-of select="."/>
      </xsl:attribute>
    </art:imageId>
  </xsl:template>
  <xsl:template match="Object_Id">
    <art:objectId>
      <xsl:attribute
          name="rdf:resource">&artstordata;Object_id#<xsl:value-of select="."/>
      </xsl:attribute>
    </art:objectId>
  </xsl:template>
  <xsl:template match="MediaFiles">
    <!-- Don't process mediafiles for now - save space for more data!
	<xsl:apply-templates/>
  -->
  </xsl:template>
  <xsl:template match="MediaFile">
    <art:hasMediaFile>
      <!-- We can have multiple media files, so they need to be resources -->
      <art:MediaFile>
        <!--+
            | Create a URI for the media file 
            | Instead of using the media file URL, create a new URI  
            | using the artstor data namespace, the resolution and the filename. 
            +-->
        <xsl:attribute name="rdf:about">&artstordata;Mediafile#<xsl:value-of select="@resolution"/>-<xsl:value-of select="@filename"/></xsl:attribute>
        <art:resolution>
          <rdf:Description>
            <xsl:attribute name="rdf:about">&artstordata;Resolution#<xsl:value-of select="@resolution"/></xsl:attribute>
            <rdfs:label>
              <xsl:value-of select="@resolution"/>
            </rdfs:label>
            <rdf:type rdf:resource="&art;Resolution"/>
          </rdf:Description>
        </art:resolution>
        <art:mediafileFormat>
          <rdf:Description>
            <xsl:attribute name="rdf:about">&artstordata;Mediafileformat#<xsl:value-of select="@format"/></xsl:attribute>
            <rdfs:label>
              <xsl:value-of select="@format"/>
            </rdfs:label>
            <rdf:type rdf:resource="&art;Mediafileformat"/>
          </rdf:Description>
        </art:mediafileFormat>
        <art:lpsid>
          <xsl:value-of select="@lpsid"/>
        </art:lpsid>
        <art:height>
          <xsl:value-of select="@height"/>
        </art:height>
        <art:width>
          <xsl:value-of select="@width"/>
        </art:width>
        <art:filename>
          <xsl:value-of select="@filename"/>
        </art:filename>
        <art:lps>
          <xsl:value-of select="@lps"/>
        </art:lps>
        <art:serverurl>
          <xsl:attribute name="rdf:resource">
            <xsl:value-of select="@serverurl"/>
          </xsl:attribute>
        </art:serverurl>
        <art:url>
          <xsl:attribute name="rdf:resource">
            <xsl:value-of select="@url"/>
          </xsl:attribute>
        </art:url>
      </art:MediaFile>
    </art:hasMediaFile>
  </xsl:template>

  <xsl:template match="Collection">
    <art:inCollection>
      <rdf:Description>
        <xsl:attribute
            name="rdf:about">&artstordata;Collection#<xsl:value-of select="@id"/>
        </xsl:attribute>
        <rdfs:label>
          <xsl:value-of select="."/>
        </rdfs:label>
        <rdf:type rdf:resource="&art;Collection"/>
      </rdf:Description>
    </art:inCollection>
    <dc:source rdf:resource="&artstorsource;"/>
  </xsl:template>
  
  <xsl:template match="MetaData">
    <xsl:apply-templates select="Record_Type"/>
    <xsl:apply-templates select="Creator"/>
    <xsl:apply-templates select="Culture"/>
    <xsl:apply-templates select="Date"/>
    <xsl:apply-templates select="Description"/>
    <xsl:apply-templates select="ID_Number"/>
    <xsl:apply-templates select="Measurements"/>
    <xsl:apply-templates select="Location"/>
    <xsl:apply-templates select="Title"/>
    <xsl:apply-templates select="Material"/>
    <xsl:apply-templates select="Rights"/>
    <xsl:apply-templates select="Source"/>
    <xsl:apply-templates select="Style_Period"/>
    <xsl:apply-templates select="Subject"/>
    <xsl:apply-templates select="Technique"/>
    <xsl:if test="not(normalize-space(./Type)='')">
      <xsl:for-each select="./Type">
        <vra:typeAAT>
          <xsl:value-of select="."/>
        </vra:typeAAT>
      </xsl:for-each>
    </xsl:if>
  </xsl:template>

  <xsl:template match="Relation">
    <!-- There are no examples of relations in the artstor corpus, don't worry about this
	for now
	<vra:relation>
		<vra:Relation>
			<xsl:if test="not(normalize-space(text())='')">
				<vra:relation><xsl:value-of select="normalize-space(text())"/></vra:relation>
			</xsl:if>
			<xsl:apply-templates select="Identity"/>
			<xsl:apply-templates select="Type"/>
		</vra:Relation>
	</vra:relation> -->
  </xsl:template>

  <xsl:template match="Source">
    <!-- Source is mixed mode -->
    <xsl:if test="not(normalize-space(text())='')">
      <vra:source>
        <xsl:attribute name="rdf:resource">&artstordata;source#<xsl:for-each select="text()">
            <xsl:value-of select="."/>
          </xsl:for-each>
        </xsl:attribute>
      </vra:source>
    </xsl:if>
    <!-- Don't call location template, need to change the name of this property to sourceLocation
	<xsl:apply-templates select="Location"/>
	-->
    <xsl:if test="not(normalize-space(./Location)='')">
      <xsl:for-each select="./Location">
        <art:sourceLocation>
          <xsl:value-of select="."/>
        </art:sourceLocation>
      </xsl:for-each>
    </xsl:if>
  </xsl:template>

  <xsl:template match="Style_Period">
    <!-- Style_Period is mixed mode -->
    <xsl:if test="not(normalize-space(text())='')">
      <vra:stylePeriod>
        <xsl:value-of select="normalize-space(text())"/>
      </vra:stylePeriod>
    </xsl:if>
    <xsl:apply-templates select="Dynasty"/>
    <xsl:apply-templates select="Group"/>
    <xsl:apply-templates select="Movement"/>
    <xsl:apply-templates select="Period"/>
    <xsl:apply-templates select="School"/>
    <xsl:apply-templates select="Style"/>
  </xsl:template>

  <xsl:template match="Creator">
    <!-- Creator is a first class object so create a resource -->
    <vra:creator>
      <vra:Entity>
        <xsl:choose>
          <xsl:when test="not(normalize-space(./Personal_Name)='')">
            <xsl:attribute
                name="rdf:about">&artstordata;Creator#<xsl:value-of select="str:urlencode(./Personal_Name)"/>
            </xsl:attribute>
            <rdfs:label>
              <xsl:value-of select="./Personal_Name"/>
            </rdfs:label>
            <rdf:type rdf:resource="&person;Person"/>
          </xsl:when>
          <xsl:when test="not(normalize-space(./Corporate_Name)='')">
            <xsl:attribute
                name="rdf:about">&artstordata;Creator#<xsl:value-of select="str:urlencode(./Corporate_Name)"/>
            </xsl:attribute>
            <rdfs:label>
              <xsl:value-of select="./Corporate_Name"/>
            </rdfs:label>
            <rdf:type rdf:resource="&art;Creator"/>
            <rdf:type rdf:resource="&vra;Corporation"/>
          </xsl:when>
          <xsl:when test="not(normalize-space(./Attribution)='')">
            <xsl:attribute
                name="rdf:about">&artstordata;Creator#<xsl:value-of select="str:urlencode(./Attribution)"/>
            </xsl:attribute>
            <rdfs:label>
              <xsl:value-of select="./Attribution"/>
            </rdfs:label>
            <rdf:type rdf:resource="&art;Creator"/>
          </xsl:when>
        </xsl:choose>
        <!-- Creator is mixed mode -->
        <xsl:if test="not(normalize-space(text())='')">
          <creator>
            <xsl:value-of select="normalize-space(text())"/>
          </creator>
        </xsl:if>
        <xsl:if test="(not(normalize-space(./Personal_Name)=''))">
          <xsl:call-template name="canonicalizeName">
            <xsl:with-param name="name" select="./Personal_Name"/>
          </xsl:call-template>
        </xsl:if>
        <xsl:apply-templates select="Attribution"/>
        <!-- <xsl:apply-templates select="Corporate_Name"/>
			<xsl:apply-templates select="Personal_Name"/> -->
        <xsl:apply-templates select="Role"/>
        <xsl:apply-templates select="Nationality"/>
        <xsl:apply-templates select="Vital_Dates"/>
      </vra:Entity>
    </vra:creator>
  </xsl:template>

  <xsl:template match="Date">
    <!-- Date is mixed mode -->
    <xsl:if test="not(normalize-space(text())='')">
      <vra:date>
        <xsl:value-of select="normalize-space(text())"/>
      </vra:date>
    </xsl:if>
    <xsl:apply-templates select="Creation"/>
    <xsl:apply-templates select="Alteration"/>
    <xsl:apply-templates select="Beginning"/>
    <xsl:apply-templates select="Completion"/>
    <xsl:apply-templates select="Design"/>
    <xsl:apply-templates select="Restoration"/>
  </xsl:template>

  <xsl:template match="Description">
    <!-- Description is mixed mode -->
    <xsl:if test="not(normalize-space(text())='')">
      <vra:description>
        <xsl:value-of select="normalize-space(text())"/>
      </vra:description>
    </xsl:if>
    <xsl:apply-templates select="Work"/>
    <xsl:apply-templates select="Group"/>
  </xsl:template>

  <xsl:template match="ID_Number">
    <!-- ID_Number is mixed mode -->
    <xsl:if test="not(normalize-space(text())='')">
      <vra:id>
        <xsl:value-of select="normalize-space(text())"/>
      </vra:id>
    </xsl:if>
    <xsl:if test="not(normalize-space(./Former_Repository)='')">
      <vra:idFormerRepository>
        <rdf:Description>
          <xsl:attribute name="rdf:about">&artstordata;Site#<xsl:value-of select="str:urlencode(.)"/>
          </xsl:attribute>
          <rdfs:label>
            <xsl:value-of select="."/>
          </rdfs:label>
          <rdf:type rdf:resource="&art;Site"/>
        </rdf:Description>
      </vra:idFormerRepository>
    </xsl:if>
    <xsl:if test="not(normalize-space(./Current_Repository)='')">
      <vra:idCurrentRepository>
        <rdf:Description>
          <xsl:attribute name="rdf:about">&artstordata;Site#<xsl:value-of select="str:urlencode(.)"/>
          </xsl:attribute>
          <rdfs:label>
            <xsl:value-of select="."/>
          </rdfs:label>
          <rdf:type rdf:resource="&art;Site"/>
        </rdf:Description>
      </vra:idCurrentRepository>
    </xsl:if>
    <xsl:apply-templates select="Current_Accession"/>
    <xsl:apply-templates select="Former_Accession"/>
  </xsl:template>

  <xsl:template match="Location">
    <!-- Location is mixed mode -->
    <xsl:if test="not(normalize-space(text())='')">
      <vra:location>
        <xsl:value-of select="normalize-space(text())"/>
      </vra:location>
    </xsl:if>
    <xsl:apply-templates select="Current_Site"/>
    <xsl:apply-templates select="Former_Site"/>
    <xsl:apply-templates select="Current_Repository"/>
    <xsl:apply-templates select="Former_Repository"/>
    <xsl:apply-templates select="Creation_Site"/>
    <xsl:apply-templates select="Discovery_Site"/>
  </xsl:template>

  <xsl:template match="Title">
    <!-- Description is mixed mode -->
    <xsl:if test="not(normalize-space(text())='')">
      <vra:title>
        <xsl:value-of select="normalize-space(text())"/>
      </vra:title>
    </xsl:if>
    <xsl:apply-templates select="Larger_Entity"/>
    <xsl:apply-templates select="Series"/>
    <xsl:apply-templates select="Translation"/>
    <xsl:apply-templates select="Variant"/>
    <xsl:apply-templates select="Collection"/>
  </xsl:template>

  <xsl:template match="Material">
    <!-- Material is mixed mode -->
    <xsl:if test="not(normalize-space(text())='')">
      <vra:material>
        <rdf:Description>
          <xsl:attribute
              name="rdf:about">&artstordata;Material#<xsl:value-of select="str:urlencode(text())"/>
          </xsl:attribute>
          <rdfs:label>
            <xsl:value-of select="normalize-space(text())"/>
          </rdfs:label>
          <rdf:type rdf:resource="&art;Material"/>
        </rdf:Description>
      </vra:material>
    </xsl:if>
    <xsl:apply-templates select="Medium"/>
    <xsl:apply-templates select="Support"/>
  </xsl:template>

  <xsl:template match="Measurements">
    <!-- Measurements is mixed mode -->
    <xsl:if test="not(normalize-space(text())='')">
      <vra:measurements>
        <xsl:value-of select="normalize-space(text())"/>
      </vra:measurements>
    </xsl:if>
    <xsl:apply-templates select="Dimensions"/>
    <xsl:apply-templates select="Format"/>
    <xsl:apply-templates select="Resolution"/>
  </xsl:template>

  <xsl:template match="Subject">
    <!-- Subject is mixed mode -->
    <xsl:if test="not(normalize-space(text())='')">
      <xsl:for-each select="text()">
        <xsl:if test="not(normalize-space(.)='')">
          <xsl:variable name="subject" select="normalize-space(.)"/>
          <xsl:call-template name="splitTerm">
            <xsl:with-param name="property">subject</xsl:with-param>
            <xsl:with-param name="propertyNamespace">&vra;</xsl:with-param>
            <xsl:with-param name="class">Subject</xsl:with-param>
            <xsl:with-param name="classNamespace">&art;</xsl:with-param>
            <xsl:with-param name="value" select="$subject"/>
            <xsl:with-param name="token">--</xsl:with-param>
          </xsl:call-template>
        </xsl:if>
      </xsl:for-each>
    </xsl:if>
    <xsl:apply-templates select="Topic"/>
    <xsl:apply-templates select="Geographic"/>
    <xsl:apply-templates select="Personal_Name"/>
    <xsl:apply-templates select="Corporate_Name"/>
    <xsl:apply-templates select="Authority"/>
  </xsl:template>

  <!-- no mixed mode beyond here -->

  <xsl:template match="Creation_Date">
    <art:metadataCreationDate>
      <xsl:value-of select="."/>
    </art:metadataCreationDate>
  </xsl:template>

  <xsl:template match="Update_Date">
    <art:metadataUpdateDate>
      <xsl:value-of select="."/>
    </art:metadataUpdateDate>
  </xsl:template>

  <xsl:template match="Culture">
    <vra:culture>
      <xsl:value-of select="."/>
    </vra:culture>
  </xsl:template>

  <xsl:template match="Identity">
    <!-- There are no examples of relations in the artstor corpus
	don't worry about this for now -->
  </xsl:template>

  <xsl:template match="Rights">
    <vra:rights>
      <xsl:value-of select="."/>
    </vra:rights>
  </xsl:template>

  <xsl:template match="Dynasty">
    <vra:dynasty>
      <xsl:value-of select="."/>
    </vra:dynasty>
  </xsl:template>

  <xsl:template match="Group">
    <vra:group>
      <xsl:value-of select="."/>
    </vra:group>
  </xsl:template>

  <xsl:template match="Movement">
    <vra:movement>
      <xsl:value-of select="."/>
    </vra:movement>
  </xsl:template>

  <xsl:template match="School">
    <vra:school>
      <xsl:value-of select="."/>
    </vra:school>
  </xsl:template>

  <xsl:template match="Style">
    <vra:style>
      <xsl:value-of select="."/>
    </vra:style>
  </xsl:template>

  <xsl:template match="Nationality">
    <art:nationality>
      <xsl:value-of select="."/>
    </art:nationality>
  </xsl:template>

  <xsl:template match="Attribution">
    <vra:attribution>
      <xsl:value-of select="."/>
    </vra:attribution>
  </xsl:template>

  <xsl:template match="Corporate_Name">
    <vra:corporateName>
      <xsl:value-of select="."/>
    </vra:corporateName>
  </xsl:template>

  <xsl:template match="Role">
    <vra:role>
      <xsl:value-of select="."/>
    </vra:role>
  </xsl:template>

  <xsl:template match="Creation">
    <vra:creation>
      <xsl:value-of select="."/>
    </vra:creation>
  </xsl:template>

  <xsl:template match="Alteration">
    <vra:alteration>
      <xsl:value-of select="."/>
    </vra:alteration>
  </xsl:template>

  <xsl:template match="Beginning">
    <vra:beginning>
      <xsl:value-of select="."/>
    </vra:beginning>
  </xsl:template>

  <xsl:template match="Completion">
    <vra:completion>
      <xsl:value-of select="."/>
    </vra:completion>
  </xsl:template>

  <xsl:template match="Design">
    <vra:design>
      <xsl:value-of select="."/>
    </vra:design>
  </xsl:template>

  <xsl:template match="Restoration">
    <vra:restoration>
      <xsl:value-of select="."/>
    </vra:restoration>
  </xsl:template>

  <xsl:template match="Work">
    <!-- There are no examples of this in the artstor corpus don't worry about this for now -->
  </xsl:template>

  <xsl:template match="Dimensions">
    <vra:measurementsDimensions>
      <xsl:value-of select="."/>
    </vra:measurementsDimensions>
  </xsl:template>

  <xsl:template match="Current_Accession">
    <vra:idCurrentAccession>
      <xsl:value-of select="."/>
    </vra:idCurrentAccession>
  </xsl:template>

  <xsl:template match="Former_Accession">
    <vra:idFormerAccession>
      <xsl:value-of select="."/>
    </vra:idFormerAccession>
  </xsl:template>

  <xsl:template match="Support">
    <vra:support>
      <xsl:value-of select="."/>
    </vra:support>
  </xsl:template>

  <xsl:template match="Resolution">
    <vra:measurementsResolution>
      <xsl:value-of select="."/>
    </vra:measurementsResolution>
  </xsl:template>

  <xsl:template match="Medium">
    <vra:medium>
      <xsl:value-of select="."/>
    </vra:medium>
  </xsl:template>

  <xsl:template match="Period">
    <vra:period>
      <xsl:value-of select="."/>
    </vra:period>
  </xsl:template>

  <xsl:template match="Personal_Name">
    <vra:personalName>
      <xsl:value-of select="."/>
    </vra:personalName>
  </xsl:template>

  <xsl:template match="Authority">
    <art:authority>
      <xsl:value-of select="."/>
    </art:authority>
  </xsl:template>

  <xsl:template match="Technique">
    <vra:technique>
      <xsl:value-of select="."/>
    </vra:technique>
  </xsl:template>

  <xsl:template match="Larger_Entity">
    <dc:partOf>
      <vra:LargerEntity>
        <xsl:attribute
            name="rdf:about">&artstordata;LargerEntity#<xsl:value-of select="str:urlencode(.)"/>
        </xsl:attribute>
        <rdfs:label>
          <xsl:value-of select="."/>
        </rdfs:label>
        <rdf:type rdf:resource="&art;LargerEntity"/>
      </vra:LargerEntity>
    </dc:partOf>
  </xsl:template>

  <xsl:template match="Series">
    <dc:partOf>
      <vra:Series>
        <xsl:attribute name="rdf:about">&artstordata;Series#<xsl:value-of select="str:urlencode(.)"/>
        </xsl:attribute>
        <rdfs:label>
          <xsl:value-of select="."/>
        </rdfs:label>
        <rdf:type rdf:resource="&art;Series"/>
      </vra:Series>
    </dc:partOf>
  </xsl:template>

  <xsl:template match="Translation">
    <vra:titleTranslation>
      <xsl:value-of select="."/>
    </vra:titleTranslation>
  </xsl:template>

  <xsl:template match="Variant">
    <vra:titleVariant>
      <xsl:value-of select="."/>
    </vra:titleVariant>
  </xsl:template>

  <xsl:template match="Type">
    <!-- There are no examples of this in the artstor corpus don't worry about this for now -->
  </xsl:template>

  <xsl:template match="Vital_Dates">
    <art:vitalDates>
      <xsl:value-of select="."/>
    </art:vitalDates>
  </xsl:template>

  <!-- Properties using controlled terms -->

  <xsl:template match="Record_Type">
    <vra:type>
      <xsl:call-template name="generateURI">
        <xsl:with-param name="controlledVocabularyType">RecordType</xsl:with-param>
      </xsl:call-template>
    </vra:type>
  </xsl:template>

  <xsl:template match="Topic">
    <xsl:variable name="topic">
      <!-- Fix up some common topic inconsistencies -->
      <xsl:choose>
        <xsl:when test="(.='Architecture: site') or (.='Architecture:Site') or (.='Architecture:site')">
          <xsl:text>Architecture: Site</xsl:text>
        </xsl:when>
        <xsl:when test=".='Architecture:Artist'">
          <xsl:text>Architecture: Artist</xsl:text>
        </xsl:when>
        <xsl:when test=".='Religion:  Judaism: Texts'">
          <xsl:text>Religion: Judaism: Texts</xsl:text>
        </xsl:when>
        <xsl:when test="matches(.,'.*:[A-Za-z].*')">
          <xsl:value-of select="substring-before(.,':')"/>
          <xsl:text>: </xsl:text>
          <xsl:value-of select="substring-after(.,':')"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="."/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:call-template name="splitTerm">
      <xsl:with-param name="property">topic</xsl:with-param>
      <xsl:with-param name="propertyNamespace">&art;</xsl:with-param>
      <xsl:with-param name="class">Topic</xsl:with-param>
      <xsl:with-param name="classNamespace">&art;</xsl:with-param>
      <xsl:with-param name="value" select="$topic"/>
      <xsl:with-param name="token">:</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="Format">
    <vra:measurementsFormat>
      <xsl:call-template name="generateURI">
        <xsl:with-param name="controlledVocabularyType">Format</xsl:with-param>
      </xsl:call-template>
    </vra:measurementsFormat>
  </xsl:template>

  <xsl:template match="Geographic">
    <xsl:variable name="geographic" select="normalize-space(.)"/>
    <xsl:call-template name="splitTerm">
      <xsl:with-param name="property">geographic</xsl:with-param>
      <xsl:with-param name="propertyNamespace">&art;</xsl:with-param>
      <xsl:with-param name="class">Geographic</xsl:with-param>
      <xsl:with-param name="classNamespace">&art;</xsl:with-param>
      <xsl:with-param name="value" select="$geographic"/>
      <xsl:with-param name="token">:</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="Former_Site">
    <vra:locationFormerSite>
      <xsl:call-template name="generateSiteURI"/>
    </vra:locationFormerSite>
  </xsl:template>

  <xsl:template match="Creation_Site">
    <vra:locationCreationSite>
      <xsl:call-template name="generateSiteURI"/>
    </vra:locationCreationSite>
  </xsl:template>
 
  <xsl:template match="Discovery_Site">
    <vra:locationDiscoverySite>
      <xsl:call-template name="generateSiteURI"/>
    </vra:locationDiscoverySite>
  </xsl:template>
 
  <xsl:template match="Former_Repository">
    <vra:locationFormerRepository>
      <xsl:call-template name="generateSiteURI"/>
    </vra:locationFormerRepository>
  </xsl:template>
 
  <xsl:template match="Current_Repository">
    <vra:locationCurrentRepository>
      <xsl:call-template name="generateSiteURI"/>
    </vra:locationCurrentRepository>
  </xsl:template>
 
  <xsl:template match="Current_Site">
    <vra:locationCurrentSite>
      <xsl:call-template name="generateSiteURI"/>
    </vra:locationCurrentSite>
  </xsl:template>

</xsl:stylesheet>
