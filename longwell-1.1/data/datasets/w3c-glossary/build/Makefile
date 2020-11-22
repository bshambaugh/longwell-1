CWM=python /Users/ryanlee/Development/cwm/cwm.py
SAXON=saxon

all: fix

fix: quick xslt
	$(CWM) *.rdf assignType.n3 --think --data --rdf > glossary.rdf
	@echo "Final RDF in glossary.rdf"

quick: setup
	perl -p -i.orig -e 's/ rdf:parseType=\"Literal\"//g' perlfix/*.rdf
	mv perlfix/*.rdf .

xslt: setup fix.xslt
	find xsltfix -name "*.rdf" -exec cp \{} \{}.orig \;
	find xsltfix -name "*.rdf" -exec $(SAXON) -o ./\{} ./\{}.orig fix.xslt \;
	mv xsltfix/*.rdf .

#setupinit:
setup:
	@echo "You need to set the value of CWM and SAXON to run this Makefile"
	@echo "Then change the target setupactual to setup and this target to something else"
	exit 1

#setup:
setupactual:
	cp ../data/original.zip .
	unzip original.zip
	mkdir xsltfix
	mkdir perlfix
	mv CCPP-struct-vocab.rdf PNG.rdf Process.rdf hypertext-terms.rdf owl-guide.rdf rdf-mt.rdf ruby.rdf voicexml20.rdf w3c-jargon.rdf weaving.rdf ws-gloss.rdf xforms.rdf xsltfix
	mv DOM-Level-3-Events.rdf REC-xml-names.rdf REC-xml.rdf xlink.rdf xml-names11.rdf xpath.rdf xptr-framework.rdf perlfix

clean:
	rm -rf xsltfix perlfix *.rdf original.zip
