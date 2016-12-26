// XMLParser.java	Created      : Sun Sep 14 17:00:24 2003
//			Last modified: Fri Sep 26 00:40:50 2003
// Compile: javac XMLParser.java #
// Execute: java XMLParser #
// FTP Directory: sources/java #
//------------------------------------------------------------
//
import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import javax.swing.tree.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import javax.swing.text.*;

public class XMLParser {
    public static void translate(org.w3c.dom.Document doc, DefaultMutableTreeNode parent, DefaultTreeModel model)
    {
	org.w3c.dom.Element e = doc.getDocumentElement();
	visit(e, parent, model);
    }

    public static SimpleAttributeSet nodeMap2AttributeSet(NamedNodeMap map)
    {
	SimpleAttributeSet hash = new SimpleAttributeSet();

	if(map != null) {
	    for(int i = 0; i < map.getLength(); i++) {
		Node n = map.item(i);
		switch(n.getNodeType()) {
		case Node.ATTRIBUTE_NODE:
		    Attr a = (Attr)n;
		    hash.addAttribute(a.getName(), a.getValue());
		    break;
		default:
		    System.out.println("???" + n.toString());
		    break;
		}
	    }
	}

	return hash;
    }

    public static void visit(org.w3c.dom.Node e, DefaultMutableTreeNode parent, DefaultTreeModel model)
    {
	//String name = e.getTagName();
	String name = e.getNodeName();
	String content = e.getNodeValue();
	DefaultMutableTreeNode child = null;
	
	switch(e.getNodeType()) {
	case Node.TEXT_NODE:
	    content = content.trim();
	    
	    if(content.length() == 0) {
		break;
	    }
	    else {
		child = new DefaultMutableTreeNode(new TagInfo(content));
	    }
	    break;
	case Node.ELEMENT_NODE:
	case Node.DOCUMENT_NODE:
	    child = new DefaultMutableTreeNode(new TagInfo(name, nodeMap2AttributeSet(e.getAttributes())));
	    break;
	case Node.COMMENT_NODE:
	    break;
	default:
	    break;
	}

	if(child != null) {
	    model.insertNodeInto(child, parent, parent.getChildCount());
	    
	    NodeList nl = e.getChildNodes();
	    
	    for(int i = 0; i < nl.getLength(); i++) {
		Node newNode = nl.item(i);
		visit(newNode, child, model);
	    }
	}
    }

    public static org.w3c.dom.Document parse(InputStream is)  throws FileNotFoundException, IOException, SAXException
    {
	org.w3c.dom.Document doc = null;
	try {
	    DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
	    doc = db.parse(is);
	}
	catch(ParserConfigurationException e) {
	    e.printStackTrace();
	}

	return doc;
    }
}
