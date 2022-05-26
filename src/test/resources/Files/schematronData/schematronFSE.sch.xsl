<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<xsl:stylesheet xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                xmlns:saxon="http://saxon.sf.net/"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:schold="http://www.ascc.net/xml/schematron"
                xmlns:iso="http://purl.oclc.org/dsdl/schematron"
                xmlns:xhtml="http://www.w3.org/1999/xhtml"
                xmlns:hl7="urn:hl7-org:v3"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                version="2.0"><!--Implementers: please note that overriding process-prolog or process-root is 
    the preferred method for meta-stylesheets to use where possible. -->
   <xsl:param name="archiveDirParameter"/>
   <xsl:param name="archiveNameParameter"/>
   <xsl:param name="fileNameParameter"/>
   <xsl:param name="fileDirParameter"/>
   <xsl:variable name="document-uri">
      <xsl:value-of select="document-uri(/)"/>
   </xsl:variable>

   <!--PHASES-->


   <!--PROLOG-->
   <xsl:output xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
               method="xml"
               omit-xml-declaration="no"
               standalone="yes"
               indent="yes"/>

   <!--XSD TYPES FOR XSLT2-->


   <!--KEYS AND FUNCTIONS-->


   <!--DEFAULT RULES-->


   <!--MODE: SCHEMATRON-SELECT-FULL-PATH-->
   <!--This mode can be used to generate an ugly though full XPath for locators-->
   <xsl:template match="*" mode="schematron-select-full-path">
      <xsl:apply-templates select="." mode="schematron-get-full-path"/>
   </xsl:template>

   <!--MODE: SCHEMATRON-FULL-PATH-->
   <!--This mode can be used to generate an ugly though full XPath for locators-->
   <xsl:template match="*" mode="schematron-get-full-path">
      <xsl:apply-templates select="parent::*" mode="schematron-get-full-path"/>
      <xsl:text>/</xsl:text>
      <xsl:choose>
         <xsl:when test="namespace-uri()=''">
            <xsl:value-of select="name()"/>
         </xsl:when>
         <xsl:otherwise>
            <xsl:text>*:</xsl:text>
            <xsl:value-of select="local-name()"/>
            <xsl:text>[namespace-uri()='</xsl:text>
            <xsl:value-of select="namespace-uri()"/>
            <xsl:text>']</xsl:text>
         </xsl:otherwise>
      </xsl:choose>
      <xsl:variable name="preceding"
                    select="count(preceding-sibling::*[local-name()=local-name(current())                                   and namespace-uri() = namespace-uri(current())])"/>
      <xsl:text>[</xsl:text>
      <xsl:value-of select="1+ $preceding"/>
      <xsl:text>]</xsl:text>
   </xsl:template>
   <xsl:template match="@*" mode="schematron-get-full-path">
      <xsl:apply-templates select="parent::*" mode="schematron-get-full-path"/>
      <xsl:text>/</xsl:text>
      <xsl:choose>
         <xsl:when test="namespace-uri()=''">@<xsl:value-of select="name()"/>
         </xsl:when>
         <xsl:otherwise>
            <xsl:text>@*[local-name()='</xsl:text>
            <xsl:value-of select="local-name()"/>
            <xsl:text>' and namespace-uri()='</xsl:text>
            <xsl:value-of select="namespace-uri()"/>
            <xsl:text>']</xsl:text>
         </xsl:otherwise>
      </xsl:choose>
   </xsl:template>

   <!--MODE: SCHEMATRON-FULL-PATH-2-->
   <!--This mode can be used to generate prefixed XPath for humans-->
   <xsl:template match="node() | @*" mode="schematron-get-full-path-2">
      <xsl:for-each select="ancestor-or-self::*">
         <xsl:text>/</xsl:text>
         <xsl:value-of select="name(.)"/>
         <xsl:if test="preceding-sibling::*[name(.)=name(current())]">
            <xsl:text>[</xsl:text>
            <xsl:value-of select="count(preceding-sibling::*[name(.)=name(current())])+1"/>
            <xsl:text>]</xsl:text>
         </xsl:if>
      </xsl:for-each>
      <xsl:if test="not(self::*)">
         <xsl:text/>/@<xsl:value-of select="name(.)"/>
      </xsl:if>
   </xsl:template>
   <!--MODE: SCHEMATRON-FULL-PATH-3-->
   <!--This mode can be used to generate prefixed XPath for humans 
	(Top-level element has index)-->
   <xsl:template match="node() | @*" mode="schematron-get-full-path-3">
      <xsl:for-each select="ancestor-or-self::*">
         <xsl:text>/</xsl:text>
         <xsl:value-of select="name(.)"/>
         <xsl:if test="parent::*">
            <xsl:text>[</xsl:text>
            <xsl:value-of select="count(preceding-sibling::*[name(.)=name(current())])+1"/>
            <xsl:text>]</xsl:text>
         </xsl:if>
      </xsl:for-each>
      <xsl:if test="not(self::*)">
         <xsl:text/>/@<xsl:value-of select="name(.)"/>
      </xsl:if>
   </xsl:template>

   <!--MODE: GENERATE-ID-FROM-PATH -->
   <xsl:template match="/" mode="generate-id-from-path"/>
   <xsl:template match="text()" mode="generate-id-from-path">
      <xsl:apply-templates select="parent::*" mode="generate-id-from-path"/>
      <xsl:value-of select="concat('.text-', 1+count(preceding-sibling::text()), '-')"/>
   </xsl:template>
   <xsl:template match="comment()" mode="generate-id-from-path">
      <xsl:apply-templates select="parent::*" mode="generate-id-from-path"/>
      <xsl:value-of select="concat('.comment-', 1+count(preceding-sibling::comment()), '-')"/>
   </xsl:template>
   <xsl:template match="processing-instruction()" mode="generate-id-from-path">
      <xsl:apply-templates select="parent::*" mode="generate-id-from-path"/>
      <xsl:value-of select="concat('.processing-instruction-', 1+count(preceding-sibling::processing-instruction()), '-')"/>
   </xsl:template>
   <xsl:template match="@*" mode="generate-id-from-path">
      <xsl:apply-templates select="parent::*" mode="generate-id-from-path"/>
      <xsl:value-of select="concat('.@', name())"/>
   </xsl:template>
   <xsl:template match="*" mode="generate-id-from-path" priority="-0.5">
      <xsl:apply-templates select="parent::*" mode="generate-id-from-path"/>
      <xsl:text>.</xsl:text>
      <xsl:value-of select="concat('.',name(),'-',1+count(preceding-sibling::*[name()=name(current())]),'-')"/>
   </xsl:template>

   <!--MODE: GENERATE-ID-2 -->
   <xsl:template match="/" mode="generate-id-2">U</xsl:template>
   <xsl:template match="*" mode="generate-id-2" priority="2">
      <xsl:text>U</xsl:text>
      <xsl:number level="multiple" count="*"/>
   </xsl:template>
   <xsl:template match="node()" mode="generate-id-2">
      <xsl:text>U.</xsl:text>
      <xsl:number level="multiple" count="*"/>
      <xsl:text>n</xsl:text>
      <xsl:number count="node()"/>
   </xsl:template>
   <xsl:template match="@*" mode="generate-id-2">
      <xsl:text>U.</xsl:text>
      <xsl:number level="multiple" count="*"/>
      <xsl:text>_</xsl:text>
      <xsl:value-of select="string-length(local-name(.))"/>
      <xsl:text>_</xsl:text>
      <xsl:value-of select="translate(name(),':','.')"/>
   </xsl:template>
   <!--Strip characters-->
   <xsl:template match="text()" priority="-1"/>

   <!--SCHEMA SETUP-->
   <xsl:template match="/">
      <svrl:schematron-output xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                              title="Schematron Laboratorio Analisi 1.3"
                              schemaVersion="">
         <xsl:comment>
            <xsl:value-of select="$archiveDirParameter"/>   
		 <xsl:value-of select="$archiveNameParameter"/>  
		 <xsl:value-of select="$fileNameParameter"/>  
		 <xsl:value-of select="$fileDirParameter"/>
         </xsl:comment>
         <svrl:ns-prefix-in-attribute-values uri="urn:hl7-org:v3" prefix="hl7"/>
         <svrl:ns-prefix-in-attribute-values uri="http://www.w3.org/2001/XMLSchema-instance" prefix="xsi"/>
         <svrl:active-pattern>
            <xsl:attribute name="document">
               <xsl:value-of select="document-uri(/)"/>
            </xsl:attribute>
            <xsl:attribute name="id">all</xsl:attribute>
            <xsl:attribute name="name">all</xsl:attribute>
            <xsl:apply-templates/>
         </svrl:active-pattern>
         <xsl:apply-templates select="/" mode="M3"/>
      </svrl:schematron-output>
   </xsl:template>

   <!--SCHEMATRON PATTERNS-->
   <svrl:text xmlns:svrl="http://purl.oclc.org/dsdl/svrl">Schematron Laboratorio Analisi 1.3</svrl:text>

   <!--PATTERN all-->


	  <!--RULE -->
   <xsl:template match="hl7:ClinicalDocument" priority="1008" mode="M3">
      <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="hl7:ClinicalDocument"/>

		    <!--ASSERT -->
      <xsl:choose>
         <xsl:when test="count(hl7:realmCode) &gt;= 1"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="count(hl7:realmCode) &gt;= 1">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>ERRORE-1| L'elemento <xsl:text/>
                  <xsl:value-of select="name(.)"/>
                  <xsl:text/> DEVE avere un elemento 'realmCode'</svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--ASSERT -->
      <xsl:choose>
         <xsl:when test="count(hl7:realmCode[@code='IT'])= 1"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="count(hl7:realmCode[@code='IT'])= 1">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>ERRORE-2| L'elemento 'realmCode' DEVE avere l'attributo @root valorizzato come 'IT'</svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--ASSERT -->
      <xsl:choose>
         <xsl:when test="count(hl7:typeId[@root='2.16.840.1.113883.1.3']) = 1"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="count(hl7:typeId[@root='2.16.840.1.113883.1.3']) = 1">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>ERRORE-3| L'elemento 'typeId' DEVE avere l'attributo @root valorizzato come '2.16.840.1.113883.1.3' </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--ASSERT -->
      <xsl:choose>
         <xsl:when test="count(hl7:templateId)&gt;=1"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="count(hl7:templateId)&gt;=1">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>ERRORE-4| L'elemento <xsl:text/>
                  <xsl:value-of select="name(.)"/>
                  <xsl:text/> DEVE avere almeno un elemento di tipo 'templateId'</svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--ASSERT -->
      <xsl:choose>
         <xsl:when test="count(hl7:templateId[@root='2.16.840.1.113883.2.9.10.1.1'])= 1 and  count(hl7:templateId/@extension)= 1"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="count(hl7:templateId[@root='2.16.840.1.113883.2.9.10.1.1'])= 1 and count(hl7:templateId/@extension)= 1">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>ERRORE-5| Almeno un elemento 'templateId' DEVE essere valorizzato attraverso l'attributo  @root='2.16.840.1.113883.2.9.10.1.1' (id del template nazionale)  associato all'attributo extensione che  indica la versione a cui il templateId fa riferimento</svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--ASSERT -->
      <xsl:choose>
         <xsl:when test="count(hl7:code[@code='11502-2'][@codeSystem='2.16.840.1.113883.6.1']) = 1"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="count(hl7:code[@code='11502-2'][@codeSystem='2.16.840.1.113883.6.1']) = 1">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>ERRORE-6| L'elemento <xsl:text/>
                  <xsl:value-of select="name(.)"/>
                  <xsl:text/>/code deve essere valorizzato con l'attributo @code='11502-2' e il @codeSystem='2.16.840.1.113883.6.1'</svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>
      <xsl:variable name="code_codeSystemName" select="hl7:code/@codeSystemName"/>
      <xsl:variable name="code_displayName" select="hl7:code/@displayName"/>

		    <!--REPORT -->
      <xsl:if test="($code_codeSystemName !='LOINC') or ($code_displayName!= 'Referto di Laboratorio')">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                 test="($code_codeSystemName !='LOINC') or ($code_displayName!= 'Referto di Laboratorio')">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-select-full-path"/>
            </xsl:attribute>
            <svrl:text>W001| Si raccomanda di valorizzare gli attributi dell'elemento <xsl:text/>
               <xsl:value-of select="name(.)"/>
               <xsl:text/>/code nel seguente modo: @codeSystemName ='LOINC' e @displayName ='Referto di laboratorio'.--&gt; </svrl:text>
         </svrl:successful-report>
      </xsl:if>

		    <!--ASSERT -->
      <xsl:choose>
         <xsl:when test="(count(hl7:confidentialityCode[@code='N'][@codeSystem='2.16.840.1.113883.5.25'])= 1) or (count(hl7:confidentialityCode[@code='V'][@codeSystem='2.16.840.1.113883.5.25'])= 1)"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="(count(hl7:confidentialityCode[@code='N'][@codeSystem='2.16.840.1.113883.5.25'])= 1) or (count(hl7:confidentialityCode[@code='V'][@codeSystem='2.16.840.1.113883.5.25'])= 1)">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>ERRORE-7| L'elemento  'confidentialityCode' di <xsl:text/>
                  <xsl:value-of select="name(.)"/>
                  <xsl:text/> DEVE essere valorizzato con l'attributo @code 'N' o 'V' e il suo @codeSystem  nel seguente modo '2.16.840.1.113883.5.25'</svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>
      <xsl:variable name="versionNumber" select="hl7:versionNumber/@value"/>

		    <!--ASSERT -->
      <xsl:choose>
         <xsl:when test="(string(number($versionNumber)) = 'NaN') or      ($versionNumber= 1 and hl7:id/@root = hl7:setId/@root and hl7:id/@extension = hl7:setId/@extension) or      ($versionNumber!= '1' and hl7:id/@root = hl7:setId/@root and hl7:id/@extension != hl7:setId/@extension) or      (hl7:id/@root != hl7:setId/@root)"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="(string(number($versionNumber)) = 'NaN') or ($versionNumber= 1 and hl7:id/@root = hl7:setId/@root and hl7:id/@extension = hl7:setId/@extension) or ($versionNumber!= '1' and hl7:id/@root = hl7:setId/@root and hl7:id/@extension != hl7:setId/@extension) or (hl7:id/@root != hl7:setId/@root)">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>ERRORE 8: Se ClinicalDocument.id e ClinicalDocument.setId usano lo stesso dominio di identificazione (@root identico) allora l’attributo @extension del
					ClinicalDocument.id deve essere diverso da quello del ClinicalDocument.setId a meno che ClinicalDocument.versionNumber non sia uguale ad 1; cioè i valori del setId ed id per un documento clinico possono coincidere solo per la prima versione di un documento.</svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--ASSERT -->
      <xsl:choose>
         <xsl:when test="(string(number($versionNumber)) ='NaN') or         ($versionNumber=1) or          (($versionNumber &gt;1) and count(hl7:relatedDocument)=1)"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="(string(number($versionNumber)) ='NaN') or ($versionNumber=1) or (($versionNumber &gt;1) and count(hl7:relatedDocument)=1)">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>ERRORE-9| Se l'attributo <xsl:text/>
                  <xsl:value-of select="name(.)"/>
                  <xsl:text/>/versionNumber/@value maggiore di  1 l'elemento <xsl:text/>
                  <xsl:value-of select="name(.)"/>
                  <xsl:text/>  deve contenere un elemento di tipo 'relatedDocument'.</svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--REPORT -->
      <xsl:if test="(count(hl7:recordTarget/hl7:patientRole/hl7:id) &gt;0) or (count(hl7:recordTarget/hl7:patientRole/hl7:id[@root='2.16.840.1.113883.2.9.4.3.2']) &gt; 1) or     (count(hl7:recordTarget/hl7:patientRole/hl7:id[@root='2.16.840.1.113883.2.9.4.3.7'])&gt;1) or (count(hl7:recordTarget/hl7:patientRole/hl7:id[@root='2.16.840.1.113883.2.9.4.3.3'])&gt;1) or     (count(hl7:recordTarget/hl7:patientRole/hl7:id[@root='2.16.840.1.113883.2.9.4.3.18']) &gt; 1)or (count(hl7:recordTarget/hl7:patientRole/hl7:id[@root='2.16.840.1.113883.2.9.4.3.17'])&gt;1)or    (count(hl7:recordTarget/hl7:patientRole/hl7:id[@root='2.16.840.1.113883.2.9.4.3.15'])&gt;1)">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                 test="(count(hl7:recordTarget/hl7:patientRole/hl7:id) &gt;0) or (count(hl7:recordTarget/hl7:patientRole/hl7:id[@root='2.16.840.1.113883.2.9.4.3.2']) &gt; 1) or (count(hl7:recordTarget/hl7:patientRole/hl7:id[@root='2.16.840.1.113883.2.9.4.3.7'])&gt;1) or (count(hl7:recordTarget/hl7:patientRole/hl7:id[@root='2.16.840.1.113883.2.9.4.3.3'])&gt;1) or (count(hl7:recordTarget/hl7:patientRole/hl7:id[@root='2.16.840.1.113883.2.9.4.3.18']) &gt; 1)or (count(hl7:recordTarget/hl7:patientRole/hl7:id[@root='2.16.840.1.113883.2.9.4.3.17'])&gt;1)or (count(hl7:recordTarget/hl7:patientRole/hl7:id[@root='2.16.840.1.113883.2.9.4.3.15'])&gt;1)">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-select-full-path"/>
            </xsl:attribute>
            <svrl:text>W00| Si si consiglia di valorizzare l'elemento recordTarget/patientRole/id  con una  delle seguenti informazioni:
			CF:2.16.840.1.113883.2.9.4.3.2
			TEAM: 2.16.840.1.113883.2.9.4.3.7 o 2.16.840.1.113883.2.9.4.3.3
			ENI:2.16.840.1.113883.2.9.4.3.18
			STP:2.16.840.1.113883.2.9.4.3.17
			ANA: 2.16.840.1.113883.2.9.4.3.15.--&gt; 
			</svrl:text>
         </svrl:successful-report>
      </xsl:if>

		    <!--ASSERT -->
      <xsl:choose>
         <xsl:when test="count(hl7:recordTarget/hl7:patientRole/hl7:addr)=0 or (count(hl7:recordTarget/hl7:patientRole/hl7:addr/hl7:country)&gt;=1 and count(hl7:recordTarget/hl7:patientRole/hl7:addr/hl7:city)&gt;=1 and count(hl7:recordTarget/hl7:patientRole/hl7:addr/hl7:streetAddressLine)&gt;=1)"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="count(hl7:recordTarget/hl7:patientRole/hl7:addr)=0 or (count(hl7:recordTarget/hl7:patientRole/hl7:addr/hl7:country)&gt;=1 and count(hl7:recordTarget/hl7:patientRole/hl7:addr/hl7:city)&gt;=1 and count(hl7:recordTarget/hl7:patientRole/hl7:addr/hl7:streetAddressLine)&gt;=1)">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text> ERRORE 10: Se presente l'elemento recordTarget/patientRole/addr DEVONO essere presenti i suoi sotto-elementi country, city, streetAddressLine   </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>
      <xsl:variable name="patient" select="hl7:recordTarget/hl7:patientRole/hl7:patient"/>

		    <!--ASSERT -->
      <xsl:choose>
         <xsl:when test="count($patient)=0 or count($patient/hl7:name)=1"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="count($patient)=0 or count($patient/hl7:name)=1">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>ERRORE-11| L'elemento <xsl:text/>
                  <xsl:value-of select="name(.)"/>
                  <xsl:text/>/recordTaget/patientRole/patient DEVE contenere l'elemento 'name'</svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--ASSERT -->
      <xsl:choose>
         <xsl:when test="count($patient)=0 or (count($patient/hl7:name/hl7:given)=1 and count($patient/hl7:name/hl7:family)=1)"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="count($patient)=0 or (count($patient/hl7:name/hl7:given)=1 and count($patient/hl7:name/hl7:family)=1)">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>ERRORE 12| L'elemento ClinicalDocument/recordTaget/patientRole/patient/name DEVE riportare gli elementi 'given' e 'family'</svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>
      <xsl:variable name="genderCode"
                    select="hl7:recordTarget/hl7:patientRole/hl7:patient/hl7:administrativeGenderCode/@code"/>
      <xsl:variable name="genderOID"
                    select="hl7:recordTarget/hl7:patientRole/hl7:patient/hl7:administrativeGenderCode/@codeSystem"/>

		    <!--ASSERT -->
      <xsl:choose>
         <xsl:when test="count(hl7:recordTarget/hl7:patientRole/hl7:patient)=0 or ($genderCode='M' or $genderCode='F' or $genderCode='UN')"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="count(hl7:recordTarget/hl7:patientRole/hl7:patient)=0 or ($genderCode='M' or $genderCode='F' or $genderCode='UN')">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text> ERRORE 13| L'attributo <xsl:text/>
                  <xsl:value-of select="name(.)"/>
                  <xsl:text/>/recordTarget/patientRole/patient/administrativeGenderCode/@code='<xsl:text/>
                  <xsl:value-of select="$genderCode"/>
                  <xsl:text/>' non è valorizzato correttamente. Deve assumere uno dei seguenti valori:'M', 'F', 'UN', e l'attributo @codeSystem con '2.16.840.1.113883.5.1' </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--ASSERT -->
      <xsl:choose>
         <xsl:when test="count(hl7:recordTarget/hl7:patientRole/hl7:patient)=0 or $genderOID='2.16.840.1.113883.5.1'"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="count(hl7:recordTarget/hl7:patientRole/hl7:patient)=0 or $genderOID='2.16.840.1.113883.5.1'">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>ERRORE-14| L'OID assegnato all'attributo <xsl:text/>
                  <xsl:value-of select="name(.)"/>
                  <xsl:text/>/recordTarget/patientRole/patient/administrativeGenderCode/@codeSystem='<xsl:text/>
                  <xsl:value-of select="$genderOID"/>
                  <xsl:text/>' non è corretto. L'attributo DEVE essere valorizzato con '2.16.840.1.113883.5.1' </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--ASSERT -->
      <xsl:choose>
         <xsl:when test="count(hl7:recordTarget/hl7:patientRole/hl7:patient)=0 or count(hl7:recordTarget/hl7:patientRole/hl7:patient/hl7:birthTime)=1"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="count(hl7:recordTarget/hl7:patientRole/hl7:patient)=0 or count(hl7:recordTarget/hl7:patientRole/hl7:patient/hl7:birthTime)=1">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>ERRORE 15| L'elemento ClinicalDocument/recordTaget/patientRole/patient DEVE avere un elemento 'birthTime'</svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--ASSERT -->
      <xsl:choose>
         <xsl:when test="count(hl7:author/hl7:assignedAuthor/hl7:id[@root='2.16.840.1.113883.2.9.4.3.2'])= 1"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="count(hl7:author/hl7:assignedAuthor/hl7:id[@root='2.16.840.1.113883.2.9.4.3.2'])= 1">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>ERRORE-16| L'elemento <xsl:text/>
                  <xsl:value-of select="name(.)"/>
                  <xsl:text/>/author/assignedAuthor DEVE contenere almeno un elemento id valorizzato con l'attributo @root '2.16.840.1.113883.2.9.4.3.2'</svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--ASSERT -->
      <xsl:choose>
         <xsl:when test="count(hl7:author/hl7:assignedAuthor/hl7:addr)=0 or (count(hl7:author/hl7:assignedAuthor/hl7:addr/hl7:country)&gt;=1 and count(hl7:author/hl7:assignedAuthor/hl7:addr/hl7:city)&gt;=1 and count(hl7:author/hl7:assignedAuthor/hl7:addr/hl7:streetAddressLine)&gt;=1)"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="count(hl7:author/hl7:assignedAuthor/hl7:addr)=0 or (count(hl7:author/hl7:assignedAuthor/hl7:addr/hl7:country)&gt;=1 and count(hl7:author/hl7:assignedAuthor/hl7:addr/hl7:city)&gt;=1 and count(hl7:author/hl7:assignedAuthor/hl7:addr/hl7:streetAddressLine)&gt;=1)">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text> ERRORE 17: Se presente l'elemento author/assignedAuthor/addr DEVONO essere presenti i suoi sotto-elementi country, city, streetAddressLine </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>
      <xsl:variable name="name_author"
                    select="hl7:author/hl7:assignedAuthor/hl7:assignedPerson"/>

		    <!--ASSERT -->
      <xsl:choose>
         <xsl:when test="count($name_author)=0 or count($name_author/hl7:name)=1"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="count($name_author)=0 or count($name_author/hl7:name)=1">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text> ERRORE-18| L'elemento ClinicalDocument/author/assignedAuthor/assignedPerson DEVE avere l'elemento 'name' </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--ASSERT -->
      <xsl:choose>
         <xsl:when test="count($name_author)=0 or (count($name_author/hl7:name/hl7:given)=1 and count($name_author/hl7:name/hl7:family)=1)"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="count($name_author)=0 or (count($name_author/hl7:name/hl7:given)=1 and count($name_author/hl7:name/hl7:family)=1)">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>ERRORE-19| L'elemento ClinicalDocument/author/assignedAuthor/assignedPerson/name DEVE avere gli elementi 'given' e 'family'</svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--ASSERT -->
      <xsl:choose>
         <xsl:when test="count(hl7:author/hl7:assignedAuthor/hl7:telecom)&gt;=1"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="count(hl7:author/hl7:assignedAuthor/hl7:telecom)&gt;=1">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text> ERRORE-20 | In ClinicalDocument/author/assignedAuthor DEVE essere presente almeno un elemento 'telecom' </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--ASSERT -->
      <xsl:choose>
         <xsl:when test="count(hl7:dataEnterer)=0 or count(hl7:dataEnterer/hl7:time)"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="count(hl7:dataEnterer)=0 or count(hl7:dataEnterer/hl7:time)">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text> ERRORE-21 | L'elemento <xsl:text/>
                  <xsl:value-of select="name(.)"/>
                  <xsl:text/>/dataEnterer DEVE avere un elemento 'time'</svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>
      <xsl:variable name="id_dataEnterer"
                    select="hl7:dataEnterer/hl7:assignedEntity/hl7:id"/>

		    <!--ASSERT -->
      <xsl:choose>
         <xsl:when test="count(hl7:dataEnterer)=0 or count($id_dataEnterer[@root='2.16.840.1.113883.2.9.4.3.2'])=1"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="count(hl7:dataEnterer)=0 or count($id_dataEnterer[@root='2.16.840.1.113883.2.9.4.3.2'])=1">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text> ERRORE-22 | L'elemento ClinicalDocument/dataEnterer DEVE avere almeno un elemento 'id' <xsl:text/>
                  <xsl:value-of select="$id_dataEnterer"/>
                  <xsl:text/> con l'attributo @root valorizzato con '2.16.840.1.113883.2.9.4.3.2'</svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>
      <xsl:variable name="nome"
                    select="hl7:dataEnterer/hl7:assignedEntity/hl7:assignedPerson/hl7:name"/>

		    <!--ASSERT -->
      <xsl:choose>
         <xsl:when test="count(hl7:dataEnterer)=0 or (count(hl7:dataEnterer/hl7:assignedEntity/hl7:assignedPerson)=1 and count(hl7:dataEnterer/hl7:assignedEntity/hl7:assignedPerson/hl7:name)=1)"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="count(hl7:dataEnterer)=0 or (count(hl7:dataEnterer/hl7:assignedEntity/hl7:assignedPerson)=1 and count(hl7:dataEnterer/hl7:assignedEntity/hl7:assignedPerson/hl7:name)=1)">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text> ERRORE-23| DEVE essere presente l'elemento assignedPerson e il sotto elemento name </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--ASSERT -->
      <xsl:choose>
         <xsl:when test="count(hl7:dataEnterer)=0 or (count($nome/hl7:family)=1 and count($nome/hl7:given)=1)"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="count(hl7:dataEnterer)=0 or (count($nome/hl7:family)=1 and count($nome/hl7:given)=1)">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text> ERRORE-24 | L'elemento name del dataEnterer DEVE riportare gli elementi 'given' e 'family' </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--ASSERT -->
      <xsl:choose>
         <xsl:when test="count(hl7:custodian/hl7:assignedCustodian/hl7:representedCustodianOrganization/hl7:id)&gt;= 1 and count(hl7:custodian/hl7:assignedCustodian/hl7:representedCustodianOrganization/hl7:name)=1"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="count(hl7:custodian/hl7:assignedCustodian/hl7:representedCustodianOrganization/hl7:id)&gt;= 1 and count(hl7:custodian/hl7:assignedCustodian/hl7:representedCustodianOrganization/hl7:name)=1">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>ERRORE-25| ClinicalDocument/custodian/assignedCustodian/representedCustodianOrganization deve contenere almeno un elemento 'id' e un solo elemento 'name'</svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--ASSERT -->
      <xsl:choose>
         <xsl:when test="count(hl7:custodian/hl7:assignedCustodian/hl7:representedCustodianOrganization/hl7:addr)=0 or (count(hl7:custodian/hl7:assignedCustodian/hl7:representedCustodianOrganization/hl7:addr/hl7:country)&gt;=1 and count(hl7:custodian/hl7:assignedCustodian/hl7:representedCustodianOrganization/hl7:addr/hl7:city)&gt;=1 and count(hl7:custodian/hl7:assignedCustodian/hl7:representedCustodianOrganization/hl7:addr/hl7:streetAddressLine)&gt;=1)"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="count(hl7:custodian/hl7:assignedCustodian/hl7:representedCustodianOrganization/hl7:addr)=0 or (count(hl7:custodian/hl7:assignedCustodian/hl7:representedCustodianOrganization/hl7:addr/hl7:country)&gt;=1 and count(hl7:custodian/hl7:assignedCustodian/hl7:representedCustodianOrganization/hl7:addr/hl7:city)&gt;=1 and count(hl7:custodian/hl7:assignedCustodian/hl7:representedCustodianOrganization/hl7:addr/hl7:streetAddressLine)&gt;=1)">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text> ERRORE 26: Se presente l'elemento custodian/assignedCustodian/representedCustodianOrganization/addr DEVONO essere presenti i suoi sotto-elementi country, city, streetAddressLine </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--ASSERT -->
      <xsl:choose>
         <xsl:when test="count(hl7:legalAuthenticator)= 0 or count(hl7:legalAuthenticator/hl7:signatureCode[@code='S'])= 1"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="count(hl7:legalAuthenticator)= 0 or count(hl7:legalAuthenticator/hl7:signatureCode[@code='S'])= 1">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>ERRORE-27| L'elemento legalAuthenticator/signatureCode deve essere valorizzato con il codice "S"  </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--ASSERT -->
      <xsl:choose>
         <xsl:when test="count(hl7:legalAuthenticator)= 0 or count(hl7:legalAuthenticator/hl7:assignedEntity/hl7:id)&gt;= 1"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="count(hl7:legalAuthenticator)= 0 or count(hl7:legalAuthenticator/hl7:assignedEntity/hl7:id)&gt;= 1">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>ERRORE-28| L'elemento legalAuthenticator/assignedEntity DEVE contenere almeno  un elemento id </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--ASSERT -->
      <xsl:choose>
         <xsl:when test="count(hl7:legalAuthenticator)= 0 or count(hl7:legalAuthenticator/hl7:assignedEntity/hl7:id[@root='2.16.840.1.113883.2.9.4.3.2'])= 1"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="count(hl7:legalAuthenticator)= 0 or count(hl7:legalAuthenticator/hl7:assignedEntity/hl7:id[@root='2.16.840.1.113883.2.9.4.3.2'])= 1">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>ERRORE-29| L'elemento legalAuthenticator/assignedEntity DEVE contenere almeno un elemento id valorizzato con l'attributo @root '2.16.840.1.113883.2.9.4.3.2'</svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--ASSERT -->
      <xsl:choose>
         <xsl:when test="count(hl7:legalAuthenticator)= 0 or count(hl7:legalAuthenticator/hl7:assignedEntity/hl7:assignedPerson/hl7:name)=1"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="count(hl7:legalAuthenticator)= 0 or count(hl7:legalAuthenticator/hl7:assignedEntity/hl7:assignedPerson/hl7:name)=1">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text> ERRORE-30 | ClinicalDocument/legalAuthenticator/assignedEntity/assignedPerson DEVE contenere l'elemento name </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--ASSERT -->
      <xsl:choose>
         <xsl:when test="count(hl7:legalAuthenticator)= 0 or (count(hl7:legalAuthenticator/hl7:assignedEntity/hl7:assignedPerson/hl7:name/hl7:family)=1 and count(hl7:legalAuthenticator/hl7:assignedEntity/hl7:assignedPerson/hl7:name/hl7:given)=1)"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="count(hl7:legalAuthenticator)= 0 or (count(hl7:legalAuthenticator/hl7:assignedEntity/hl7:assignedPerson/hl7:name/hl7:family)=1 and count(hl7:legalAuthenticator/hl7:assignedEntity/hl7:assignedPerson/hl7:name/hl7:given)=1)">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>ERRORE-31 | ClinicalDocument/legalAuthenticator/assignedEntity/assignedPerson/name DEVE riportare gli elementi 'given' e 'family'</svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>
      <xsl:variable name="prioriry"
                    select="hl7:inFulfillmentOf/hl7:order/hl7:priorityCode/@code"/>

		    <!--ASSERT -->
      <xsl:choose>
         <xsl:when test="count(hl7:inFulfillmentOf)&gt;=1"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="count(hl7:inFulfillmentOf)&gt;=1">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text> ERRORE-32 | in ClinicalDocuemntDEVE essere presente l'elemento inFulfillmentOf </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--ASSERT -->
      <xsl:choose>
         <xsl:when test="count(hl7:inFulfillmentOf/hl7:order/hl7:priorityCode)=0 or ($prioriry='R' or $prioriry='P' or $prioriry='UR' or $prioriry='EM')"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="count(hl7:inFulfillmentOf/hl7:order/hl7:priorityCode)=0 or ($prioriry='R' or $prioriry='P' or $prioriry='UR' or $prioriry='EM')">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text> ERRORE-32 | ClinicalDocument/infulfillmentOf/order/priorityCode DEVE avere l'attributo code valorizzato con uno dei seguenti valori: 'R'|'P'|'UR'|'EM' </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--REPORT -->
      <xsl:if test="count (hl7:documentationOf/hl7:serviceEvent/hl7:performer)!=0 or (count (hl7:documentationOf/hl7:serviceEvent/hl7:performer/hl7:assignedEntity/hl7:addr)!=1 and count (hl7:documentationOf/hl7:serviceEvent/hl7:performer/hl7:assignedEntity/hl7:telecom)!=1 )">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                 test="count (hl7:documentationOf/hl7:serviceEvent/hl7:performer)!=0 or (count (hl7:documentationOf/hl7:serviceEvent/hl7:performer/hl7:assignedEntity/hl7:addr)!=1 and count (hl7:documentationOf/hl7:serviceEvent/hl7:performer/hl7:assignedEntity/hl7:telecom)!=1 )">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-select-full-path"/>
            </xsl:attribute>
            <svrl:text> W003 | L'elemento ClinicalDocument/documentationOf/serviceEvent/performer/assignedEntity dovrebbe contenere address e telecom </svrl:text>
         </svrl:successful-report>
      </xsl:if>

		    <!--ASSERT -->
      <xsl:choose>
         <xsl:when test="count (hl7:documentationOf/hl7:serviceEvent/hl7:performer/hl7:assignedEntity/hl7:assignedPerson)=0 or count (hl7:documentationOf/hl7:serviceEvent/hl7:performer/hl7:assignedEntity/hl7:assignedPerson/hl7:name)=1"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="count (hl7:documentationOf/hl7:serviceEvent/hl7:performer/hl7:assignedEntity/hl7:assignedPerson)=0 or count (hl7:documentationOf/hl7:serviceEvent/hl7:performer/hl7:assignedEntity/hl7:assignedPerson/hl7:name)=1">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text> ERRORE-33 | L'elemento ClinicalDocument/documentationOf/serviceEvent/performer/assignedEntity/assignedPerson se presente, deve contenere l'elemento name </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--ASSERT -->
      <xsl:choose>
         <xsl:when test="count (hl7:documentationOf/hl7:serviceEvent/hl7:performer/hl7:assignedEntity/hl7:assignedPerson)=0 or (count (hl7:documentationOf/hl7:serviceEvent/hl7:performer/hl7:assignedEntity/hl7:assignedPerson/hl7:name/hl7:given)=1 and count (hl7:documentationOf/hl7:serviceEvent/hl7:performer/hl7:assignedEntity/hl7:assignedPerson/hl7:name/hl7:family)=1)"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="count (hl7:documentationOf/hl7:serviceEvent/hl7:performer/hl7:assignedEntity/hl7:assignedPerson)=0 or (count (hl7:documentationOf/hl7:serviceEvent/hl7:performer/hl7:assignedEntity/hl7:assignedPerson/hl7:name/hl7:given)=1 and count (hl7:documentationOf/hl7:serviceEvent/hl7:performer/hl7:assignedEntity/hl7:assignedPerson/hl7:name/hl7:family)=1)">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>ERRORE-34 |l'elemento ClinicalDocument/documentationOf/serviceEvent/performer/assignedEntity/assignedPerson/name deve contenere gli attributi given e family </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>
      <xsl:variable name="path_name"
                    select="hl7:componentOf/hl7:encompassingEncounter/hl7:responsibleParty/hl7:assignedEntity/hl7:assignedPerson/hl7:name"/>

		    <!--ASSERT -->
      <xsl:choose>
         <xsl:when test="count(hl7:componentOf/hl7:encompassingEncounter/hl7:responsibleParty/hl7:assignedEntity/hl7:assignedPerson)=0 or count (hl7:componentOf/hl7:encompassingEncounter/hl7:responsibleParty/hl7:assignedEntity/hl7:assignedPerson/hl7:name)=1 "/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="count(hl7:componentOf/hl7:encompassingEncounter/hl7:responsibleParty/hl7:assignedEntity/hl7:assignedPerson)=0 or count (hl7:componentOf/hl7:encompassingEncounter/hl7:responsibleParty/hl7:assignedEntity/hl7:assignedPerson/hl7:name)=1">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text> ERROR-35 | deve essere presente l'elemento ClinicalDocument/componentOf/encompassingEncounter/responsibleParty/assignedentity/assignedPerson/name </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--ASSERT -->
      <xsl:choose>
         <xsl:when test="count(hl7:componentOf/hl7:encompassingEncounter/hl7:responsibleParty/hl7:assignedEntity/hl7:assignedPerson)=0 or (count($path_name/hl7:given)=1 and count($path_name/hl7:family)=1)"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="count(hl7:componentOf/hl7:encompassingEncounter/hl7:responsibleParty/hl7:assignedEntity/hl7:assignedPerson)=0 or (count($path_name/hl7:given)=1 and count($path_name/hl7:family)=1)">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text> ERRORE-36 | L'elemento ClinicalDocument/componentOf/encompassingEncounter/responsibleParty/assignedentity/assignedPerson/name deve contenere gli attributi given e family </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--ASSERT -->
      <xsl:choose>
         <xsl:when test="count(hl7:componentOf/hl7:encompassingEncounter/hl7:location/hl7:healthCareFacility/hl7:serviceProviderOrganization)=0 or count (hl7:componentOf/hl7:encompassingEncounter/hl7:location/hl7:healthCareFacility/hl7:serviceProviderOrganization/hl7:id)=1"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="count(hl7:componentOf/hl7:encompassingEncounter/hl7:location/hl7:healthCareFacility/hl7:serviceProviderOrganization)=0 or count (hl7:componentOf/hl7:encompassingEncounter/hl7:location/hl7:healthCareFacility/hl7:serviceProviderOrganization/hl7:id)=1">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text> ERRORE-37 | L'elemento ClinicalDocument/componentOf/encompassingEncounter/location/healthcareFacility/serviceProviderOrganization deve contenere l'elemento 'id' </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates select="*|comment()|processing-instruction()" mode="M3"/>
   </xsl:template>

	  <!--RULE -->
   <xsl:template match="hl7:ClinicalDocument/hl7:component/hl7:structuredBody/hl7:component/hl7:section"
                 priority="1007"
                 mode="M3">
      <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                       context="hl7:ClinicalDocument/hl7:component/hl7:structuredBody/hl7:component/hl7:section"/>

		    <!--ASSERT -->
      <xsl:choose>
         <xsl:when test="count(hl7:code[@code='18717-9'])&gt;= 1 or count(hl7:code[@code='18718-7'])&gt;= 1 or       count(hl7:code[@code='18719-5'])&gt;= 1 or count(hl7:code[@code='18720-3'])&gt;= 1 or       count(hl7:code[@code='18721-1'])&gt;= 1 or count(hl7:code[@code='18722-9'])&gt;= 1 or       count(hl7:code[@code='18723-7'])&gt;= 1 or count(hl7:code[@code='18724-5'])&gt;= 1 or       count(hl7:code[@code='18725-2'])&gt;= 1 or count(hl7:code[@code='18727-8'])&gt;= 1 or       count(hl7:code[@code='18729-6'])&gt;= 1 or count(hl7:code[@code='18729-4'])&gt;= 1 or       count(hl7:code[@code='18767-4'])&gt;= 1 or count(hl7:code[@code='18768-2'])&gt;= 1 or       count(hl7:code[@code='18769-0'])&gt;= 1 or count(hl7:code[@code='26435-8'])&gt;= 1 or       count(hl7:code[@code='26436-6'])&gt;= 1 or count(hl7:code[@code='26437-4'])&gt;= 1 or       count(hl7:code[@code='26438-2'])&gt;= 1 or count(hl7:code[@code='18716-1'])&gt;= 1 or       count(hl7:code[@code='26439-0'])&gt;= 1 "/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="count(hl7:code[@code='18717-9'])&gt;= 1 or count(hl7:code[@code='18718-7'])&gt;= 1 or count(hl7:code[@code='18719-5'])&gt;= 1 or count(hl7:code[@code='18720-3'])&gt;= 1 or count(hl7:code[@code='18721-1'])&gt;= 1 or count(hl7:code[@code='18722-9'])&gt;= 1 or count(hl7:code[@code='18723-7'])&gt;= 1 or count(hl7:code[@code='18724-5'])&gt;= 1 or count(hl7:code[@code='18725-2'])&gt;= 1 or count(hl7:code[@code='18727-8'])&gt;= 1 or count(hl7:code[@code='18729-6'])&gt;= 1 or count(hl7:code[@code='18729-4'])&gt;= 1 or count(hl7:code[@code='18767-4'])&gt;= 1 or count(hl7:code[@code='18768-2'])&gt;= 1 or count(hl7:code[@code='18769-0'])&gt;= 1 or count(hl7:code[@code='26435-8'])&gt;= 1 or count(hl7:code[@code='26436-6'])&gt;= 1 or count(hl7:code[@code='26437-4'])&gt;= 1 or count(hl7:code[@code='26438-2'])&gt;= 1 or count(hl7:code[@code='18716-1'])&gt;= 1 or count(hl7:code[@code='26439-0'])&gt;= 1">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>
						ERRORE-38|L'elemento code della section deve essere valorizzato con dei seguenti codici LOINC individuati:
						18717-9
						18718-7
						18719-5
						18720-3
						18721-1
						18722-9
						18723-7
						18724-5
						18725-2
						18727-8
						18729-6
						18729-4
						18767-4
						18768-2
						18769-0
						26435-8
						26436-6 
						26437-4
						26438-2
						18716-1
						26439-0 </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--ASSERT -->
      <xsl:choose>
         <xsl:when test="(count(hl7:component/hl7:section)&gt;=1 or count(hl7:text)=1) or (count(hl7:component/hl7:section)&gt;=1 and count (hl7:text)=1)"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="(count(hl7:component/hl7:section)&gt;=1 or count(hl7:text)=1) or (count(hl7:component/hl7:section)&gt;=1 and count (hl7:text)=1)">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>ERORE 39| L'elemento component/structuredBody/component/section/text DEVE essere presente nel caso in cui non è riportata la sezione foglia. </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--ASSERT -->
      <xsl:choose>
         <xsl:when test="count(hl7:entry/hl7:act/hl7:specimen)=0 or ((count(hl7:entry/hl7:act/hl7:specimen) &lt;=1) or count(hl7:entry/hl7:act/hl7:specimen/hl7:specimenRole/hl7:id)=1)"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="count(hl7:entry/hl7:act/hl7:specimen)=0 or ((count(hl7:entry/hl7:act/hl7:specimen) &lt;=1) or count(hl7:entry/hl7:act/hl7:specimen/hl7:specimenRole/hl7:id)=1)">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text> ERRORE 40 | Gli elementi <xsl:text/>
                  <xsl:value-of select="name(.)"/>
                  <xsl:text/>/entry/act/specimen/specimenRole DEVONO avere un elemento 'id'. </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--ASSERT -->
      <xsl:choose>
         <xsl:when test="count(hl7:entry/hl7:act/hl7:specimen)=0 or count(hl7:entry/hl7:act/hl7:specimen/hl7:specimenRole/hl7:specimenPlayingEntity)=1"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="count(hl7:entry/hl7:act/hl7:specimen)=0 or count(hl7:entry/hl7:act/hl7:specimen/hl7:specimenRole/hl7:specimenPlayingEntity)=1">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text> ERROR 41 | L'elemento component/struturedBody/component/section/entry/act/specimen deve contenere l'elemento specimenRole/specimenPlayingEntity </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--ASSERT -->
      <xsl:choose>
         <xsl:when test="count(hl7:entry/hl7:act/hl7:specimen)=0 or count(hl7:entry/hl7:act/hl7:specimen/hl7:specimenRole/hl7:specimenPlayingEntity/hl7:code)=1"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="count(hl7:entry/hl7:act/hl7:specimen)=0 or count(hl7:entry/hl7:act/hl7:specimen/hl7:specimenRole/hl7:specimenPlayingEntity/hl7:code)=1">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text> ERROR 42 | L'elemento <xsl:text/>
                  <xsl:value-of select="name(.)"/>
                  <xsl:text/> /entry/act/specimen/specimenRole/specimenPlayingEntity deve contenere l'elemento code</svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--ASSERT -->
      <xsl:choose>
         <xsl:when test="count(hl7:entry/hl7:act)=0 or (count(hl7:entry/hl7:act/hl7:entryRelationship[@typeCode='SUBJ'])=1 and count(hl7:entry/hl7:act/hl7:entryRelationship[@inversionInd='true'])=1)"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="count(hl7:entry/hl7:act)=0 or (count(hl7:entry/hl7:act/hl7:entryRelationship[@typeCode='SUBJ'])=1 and count(hl7:entry/hl7:act/hl7:entryRelationship[@inversionInd='true'])=1)">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text> ERRORE 43 | L'elemento <xsl:text/>
                  <xsl:value-of select="name(.)"/>
                  <xsl:text/> /entry/act/entryRelationship deve avere gli attributi @typeCode="SUBJ"e @inversionInd="true" </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--ASSERT -->
      <xsl:choose>
         <xsl:when test="count(hl7:entry/hl7:act/hl7:entryRelationship)=0 or (count(hl7:entry/hl7:act/hl7:entryRelationship/hl7:act[@code='48767-8'])=1 and count(hl7:act[@codeSystem='2.16.840.1.113883.6.1'])=1)"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="count(hl7:entry/hl7:act/hl7:entryRelationship)=0 or (count(hl7:entry/hl7:act/hl7:entryRelationship/hl7:act[@code='48767-8'])=1 and count(hl7:act[@codeSystem='2.16.840.1.113883.6.1'])=1)">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text> ERROR 44 | L'elemento <xsl:text/>
                  <xsl:value-of select="name(.)"/>
                  <xsl:text/> /entry/act/entryRelationship/act/code deve avere gli attributi @code="48767-8" e @codeSystem="2.16.840.1.113883.6.1" </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--ASSERT -->
      <xsl:choose>
         <xsl:when test="count(hl7:entry/hl7:act/hl7:entryRelationship)=0 or (count(hl7:entry/hl7:act/hl7:entryRelationship/hl7:act/hl7:text/hl7:reference)=1)"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="count(hl7:entry/hl7:act/hl7:entryRelationship)=0 or (count(hl7:entry/hl7:act/hl7:entryRelationship/hl7:act/hl7:text/hl7:reference)=1)">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text> ERROR 45 | L'elemento <xsl:text/>
                  <xsl:value-of select="name(.)"/>
                  <xsl:text/> /entry/act/entryRelationship/act/text deve contenere l'elemento reference </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates select="*|comment()|processing-instruction()" mode="M3"/>
   </xsl:template>

	  <!--RULE -->
   <xsl:template match="hl7:ClinicalDocument/hl7:component/hl7:structuredBody/hl7:component/hl7:section/hl7:component/hl7:section"
                 priority="1006"
                 mode="M3">
      <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                       context="hl7:ClinicalDocument/hl7:component/hl7:structuredBody/hl7:component/hl7:section/hl7:component/hl7:section"/>

		    <!--ASSERT -->
      <xsl:choose>
         <xsl:when test="count(hl7:component/hl7:section)=0"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="count(hl7:component/hl7:section)=0">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text> ERRORE 46 | <xsl:text/>
                  <xsl:value-of select="name(.)"/>
                  <xsl:text/> non deve includere ulteriori component</svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>
      <xsl:variable name="stCode" select="hl7:entry/hl7:act/hl7:statusCode/@code"/>

		    <!--ASSERT -->
      <xsl:choose>
         <xsl:when test="count(hl7:entry/hl7:act/hl7:statusCode)=1 and ($stCode='completed' or $stCode='active' or $stCode='aborted')"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="count(hl7:entry/hl7:act/hl7:statusCode)=1 and ($stCode='completed' or $stCode='active' or $stCode='aborted')">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text> ERRORE-47 | <xsl:text/>
                  <xsl:value-of select="name(.)"/>
                  <xsl:text/> /entry/act/statusCode deve contenere l'attibuto @code = 'completed' or 'active' or 'aborted' </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--REPORT -->
      <xsl:if test="count(hl7:entry/hl7:act/hl7:code[@codeSystem='2.16.840.1.113883.6.1'])=0">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                 test="count(hl7:entry/hl7:act/hl7:code[@codeSystem='2.16.840.1.113883.6.1'])=0">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-select-full-path"/>
            </xsl:attribute>
            <svrl:text> W005 | si consiglia di utilizzare il sistema di codifica LOINC per la valorizzazione dell'elemento code di  <xsl:text/>
               <xsl:value-of select="name(.)"/>
               <xsl:text/> /entry </svrl:text>
         </svrl:successful-report>
      </xsl:if>
      <xsl:apply-templates select="*|comment()|processing-instruction()" mode="M3"/>
   </xsl:template>

	  <!--RULE -->
   <xsl:template match="hl7:ClinicalDocument/hl7:component/hl7:structuredBody/hl7:component/hl7:section/hl7:component/hl7:section/hl7:entry/hl7:act/hl7:entryRelationship/hl7:observation"
                 priority="1005"
                 mode="M3">
      <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                       context="hl7:ClinicalDocument/hl7:component/hl7:structuredBody/hl7:component/hl7:section/hl7:component/hl7:section/hl7:entry/hl7:act/hl7:entryRelationship/hl7:observation"/>

		    <!--REPORT -->
      <xsl:if test="count(hl7:code[@codeSystem='2.16.840.1.113883.6.1'])=0">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                 test="count(hl7:code[@codeSystem='2.16.840.1.113883.6.1'])=0">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-select-full-path"/>
            </xsl:attribute>
            <svrl:text> W006 | si consiglia di utilizzare il sistema di codifica LOINC per la valorizzazione dell'elemento code di observation </svrl:text>
         </svrl:successful-report>
      </xsl:if>
      <xsl:variable name="obs_int" select="hl7:interpretationCode/@codeSystem"/>

		    <!--ASSERT -->
      <xsl:choose>
         <xsl:when test="count(hl7:interpretationCode)=0 or $obs_int='2.16.840.1.113883.5.83'"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="count(hl7:interpretationCode)=0 or $obs_int='2.16.840.1.113883.5.83'">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text> ERRORE-48| L'elemento observation/interpretationCode DEVE essere valorizzato secondo il value set HL7 Observation Interpretation (2.16.840.1.113883.5.83)</svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates select="*|comment()|processing-instruction()" mode="M3"/>
   </xsl:template>

	  <!--RULE -->
   <xsl:template match="//*[@codeSystem='2.16.840.1.113883.6.1']"
                 priority="1004"
                 mode="M3">
      <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                       context="//*[@codeSystem='2.16.840.1.113883.6.1']"/>
      <xsl:variable name="val_LOINC" select="@code"/>

		    <!--ASSERT -->
      <xsl:choose>
         <xsl:when test="doc('DIZ/XML_LAB_FSE_v1/2.16.840.1.113883.6.1.xml')//el[@code=$val_LOINC]"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="doc('DIZ/XML_LAB_FSE_v1/2.16.840.1.113883.6.1.xml')//el[@code=$val_LOINC]">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>ERRORE-48| Codice LOINC <xsl:text/>
                  <xsl:value-of select="$val_LOINC"/>
                  <xsl:text/> errato
			</svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates select="*|comment()|processing-instruction()" mode="M3"/>
   </xsl:template>

	  <!--RULE -->
   <xsl:template match="//*[@classCode='BATTERY']" priority="1003" mode="M3">
      <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                       context="//*[@classCode='BATTERY']"/>

		    <!--ASSERT -->
      <xsl:choose>
         <xsl:when test="(count(hl7:code)=1)"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="(count(hl7:code)=1)">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>
						ERRORE-49| L’elemento organizer di tipo 'BATTERY' (@classCode='BATTERY') DEVE contenere l’elemento organizer/code.
			</svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates select="*|comment()|processing-instruction()" mode="M3"/>
   </xsl:template>

	  <!--RULE -->
   <xsl:template match="//hl7:addr" priority="1002" mode="M3">
      <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="//hl7:addr"/>

		    <!--ASSERT -->
      <xsl:choose>
         <xsl:when test="count(@use)=0 or (count(@use)=1)"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="count(@use)=0 or (count(@use)=1)">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>
						ERRORE-50| L’elemento 'addr' DEVE contenere l'attributo @use.
			</svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates select="*|comment()|processing-instruction()" mode="M3"/>
   </xsl:template>

	  <!--RULE -->
   <xsl:template match="//hl7:telecom" priority="1001" mode="M3">
      <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="//hl7:telecom"/>

		    <!--ASSERT -->
      <xsl:choose>
         <xsl:when test="(count(@use)=1)"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="(count(@use)=1)">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>
						ERRORE-51| L’elemento 'telecom' DEVE contenere l'attributo @use.
			</svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates select="*|comment()|processing-instruction()" mode="M3"/>
   </xsl:template>

	  <!--RULE -->
   <xsl:template match="//hl7:id[@root='2.16.840.1.113883.2.9.4.3.2']"
                 priority="1000"
                 mode="M3">
      <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                       context="//hl7:id[@root='2.16.840.1.113883.2.9.4.3.2']"/>
      <xsl:variable name="CF" select="@extension"/>

		    <!--ASSERT -->
      <xsl:choose>
         <xsl:when test="matches(@extension, '[A-Z0-9]{16}')"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="matches(@extension, '[A-Z0-9]{16}')">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text> ERRORE-52| codice fiscale '<xsl:text/>
                  <xsl:value-of select="$CF"/>
                  <xsl:text/>' cittadino ed operatore: 16 cifre [A-Z0-9]{16}</svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates select="*|comment()|processing-instruction()" mode="M3"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M3"/>
   <xsl:template match="@*|node()" priority="-2" mode="M3">
      <xsl:apply-templates select="*|comment()|processing-instruction()" mode="M3"/>
   </xsl:template>
</xsl:stylesheet>
