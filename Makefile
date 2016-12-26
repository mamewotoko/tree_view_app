### Makefile		Created      : Fri Sep  5 23:43:11 2003
###			Last modified: Mon Dec 26 10:00:31 2016
### Execute: make #

SHELL=/bin/bash
ARCHIVE_DEST=/home/tak/lib/java
DEST=/home/tak/bin

all: classes/HTMLTree.jar classes2/MethodTableViewer.jar classes3/OcamlFunctionViewer.jar

GENERATED_SRC_DIR=src/main/java

############################################################
ARCHIVE=HTMLTree.jar
CLASSES=classes
MANIFEST=HTMLTree.MF
SOURCES= src/main/java/HTMLTree.java src/main/java/MyDialog.java src/main/java/HTMLParser.java src/main/java/MyHTMLDocument.java src/main/java/MyHTMLEditorKit.java src/main/java/TagInfo.java src/main/java/InputInfo.java src/main/java/FormInfo.java src/main/java/XMLParser.java src/main/java/HTTPStreamSource.java src/main/java/InputStreamSource.java src/main/java/TypeAheadTree.java src/main/java/FormFrame.java src/main/java/HTTPPostStreamSource.java src/main/java/SelectInputInfo.java

SOURCE_FILES=$(SOURCES) $(MANIFEST)
$(CLASSES)/$(ARCHIVE): $(SOURCE_FILES) 
	[ -e $(CLASSES) ] || mkdir $(CLASSES)
	rm -f $@
	javac -d $(CLASSES) $(SOURCES)
	cp $(SOURCE_FILES) $(CLASSES)
	(cd $(CLASSES) ; \
	if [ -z "$(MANIFEST)" ]; then \
		jar cvf $(ARCHIVE) * ; \
	else \
		jar cvmf $(MANIFEST) $(ARCHIVE) * ; \
	fi )

############################################################
MT_ARCHIVE=MethodTableViewer.jar
MT_CLASSES=classes2
MT_MANIFEST=MethodTableViewer.MF
MT_JAVACC_SOURCES=src/main/javacc/java_sequence.jj
MT_JAVACC_JAVA_SOURCES= \
$(GENERATED_SRC_DIR)/TokenMgrError.java \
$(GENERATED_SRC_DIR)/ParseException.java \
$(GENERATED_SRC_DIR)/Token.java \
$(GENERATED_SRC_DIR)/JavaCharStream.java \
$(GENERATED_SRC_DIR)/JavaParser.java \
$(GENERATED_SRC_DIR)/JavaParserConstants.java \
$(GENERATED_SRC_DIR)/JavaParserTokenManager.java

MT_JAVA_SOURCES=src/main/java/MethodTableViewer.java src/main/java/TagInfo.java src/main/java/EditableTagInfo.java src/main/java/MethodXMLParser.java src/main/java/HTTPStreamSource.java src/main/java/MethodPathInfo.java
MT_SOURCES=$(MT_JAVA_SOURCES) $(MT_JAVACC_JAVA_SOURCES)
MT_SOURCE_FILES=$(MT_SOURCES) $(MT_MANIFEST) xml/result.xml 

$(MT_CLASSES)/$(MT_ARCHIVE):  $(MT_JAVA_SOURCES)
	[ -e $(MT_CLASSES) ] || mkdir $(MT_CLASSES)
	rm -f $@ $(MT_CLASSES)/*.class $(MT_JAVACC_JAVA_SOURCES)
	mkdir -p $(GENERATED_SRC_DIR)
	javacc -OUTPUT_DIRECTORY=$(GENERATED_SRC_DIR) $(MT_JAVACC_SOURCES)
	javac -classpath src/main/java -sourcepath src/main/java -d $(MT_CLASSES) $(MT_SOURCES)
	cp $(MT_SOURCE_FILES) $(MT_CLASSES)
	(cd $(MT_CLASSES) ; \
	if [ -z "$(MT_MANIFEST)" ]; then \
		jar cvf $(MT_ARCHIVE) * ; \
	else \
		jar cvmf $(MT_MANIFEST) $(MT_ARCHIVE) * ; \
	fi )

############################################################
OF_ARCHIVE=OcamlFunctionViewer.jar
OF_CLASSES=classes3
OF_MANIFEST=OcamlFunctionViewer.MF
OF_JAVA_SOURCES=src/main/java/OcamlFunctionViewer.java src/main/java/MyDialog.java src/main/java/TagInfo.java src/main/java/EditableTagInfo.java src/main/java/HTTPStreamSource.java src/main/java/OcamlFunctionXMLParser.java src/main/java/MethodPathInfo.java src/main/java/InputStreamSource.java
OF_SOURCES=$(OF_JAVA_SOURCES)
OF_SOURCE_FILES=$(OF_SOURCES) $(OF_MANIFEST)

# cp $(OF_SOURCE_FILES) ocaml_result.xml $(OF_CLASSES) 
$(OF_CLASSES)/$(OF_ARCHIVE):  $(OF_JAVA_SOURCES)
	[ -e $(OF_CLASSES) ] || mkdir $(OF_CLASSES)
	rm -f $@ $(OF_CLASSES)/*.class 
	javac -d $(OF_CLASSES) $(OF_SOURCES)
	cp $(OF_SOURCE_FILES) $(OF_CLASSES) 
	(cd $(OF_CLASSES) ; \
	if [ -z "$(OF_MANIFEST)" ]; then \
		jar cvf $(OF_ARCHIVE) * ; \
	else \
		jar cvmf $(OF_MANIFEST) $(OF_ARCHIVE) * ; \
	fi )

# KEYSTORE=/home/tak/.keystore/mamewo
# KEY=mykey
# SIGN_TARGET=$(MT_CLASSES)/$(MT_ARCHIVE)

# $(KEYSTORE):
# 	keytool -genkey -keystore $(KEYSTORE) -alias $(KEY)
# 	keytool -selfcert -alias $(KEY) -keystore $(KEYSTORE)

# sign: $(KEYSTORE)
# 	jarsigner -keystore $(KEYSTORE) $(SIGN_TARGET) $(KEY)

clean:
	rm -rf $(CLASSES) *~ *.class classes $(MT_CLASSES) $(MT_JAVACC_JAVA_SOURCES) $(MT_CLASSES) $(OF_CLASSES)
