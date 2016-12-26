// OcamlFunctionXMLParser.java	Created      : Thu Oct 16 01:54:44 2003
//			Last modified: Sun Nov 09 04:51:09 2003
// Compile: javac OcamlFunctionXMLParser.java #
// Execute: java OcamlFunctionXMLParser #
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

public class OcamlFunctionXMLParser {
    public static DefaultMutableTreeNode translate(org.w3c.dom.Document doc, Hashtable methodname2pathes, HashSet filterwords)
    {
	org.w3c.dom.Element e = doc.getDocumentElement();
	DefaultMutableTreeNode parent = new DefaultMutableTreeNode("files");
	NodeList nl = e.getChildNodes();
	for(int i = 0; i < nl.getLength(); i++) {
	    Node newNode = nl.item(i);
	    visit(newNode, parent, methodname2pathes, filterwords);
	}

	if(! parent.isLeaf()) {
	    return parent;
	}
	else {
	    return null;
	}
    }

    public static AttributeSet nodeMap2AttributeSet(NamedNodeMap map)
    {
	SimpleAttributeSet hash = null;

	if(map != null) {
	    for(int i = 0; i < map.getLength(); i++) {
		Node n = map.item(i);
		switch(n.getNodeType()) {
		case Node.ATTRIBUTE_NODE:
		    if(hash == null) {
			hash = new SimpleAttributeSet();
		    }
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

    public static void addMethod(Hashtable methodname2pathes, String methodname, TreeNode[] path) 
    {
	Vector v = (Vector)methodname2pathes.get(methodname);
	if(v != null) {
	    v.add(path);
	}
	else {
	    v = new Vector();
	    v.add(path);
	    methodname2pathes.put(methodname, v);
	}
    }

    public static void visit(org.w3c.dom.Node e, DefaultMutableTreeNode parent, Hashtable methodname2pathes, HashSet filterwords)
    {
	String name = e.getNodeName();
	String content = e.getNodeValue();
	DefaultMutableTreeNode child = null;
	TagInfo ti = null;
	AttributeSet attr = null;
	boolean add_function_table = false;
	Object typestr = null;
	
	switch(e.getNodeType()) {
	case Node.TEXT_NODE:
	    content = content.trim();
	    
	    if(content.length() == 0) {
		break;
	    }
	    else {
		child = new DefaultMutableTreeNode(new EditableTagInfo(content));
	    }
	    break;
	case Node.ELEMENT_NODE:
	case Node.DOCUMENT_NODE:
	    attr = nodeMap2AttributeSet(e.getAttributes());
	    if(attr == null) {
		//System.out.println("attr == null");
		ti = new EditableTagInfo(name);
	    }
	    else {
		//System.out.println("attr != null");
		String nname = (String)attr.getAttribute("name");
		add_function_table = name.equals("function");
		if(nname == null) 
		    ti = new EditableTagInfo(name, attr);
		else if(!name.equals("call") || !filterwords.contains(nname)) 
		    ti = new EditableTagInfo(nname, attr);
	    }
	    if(ti != null) 
		child = new DefaultMutableTreeNode(ti);
	    break;
	case Node.COMMENT_NODE:
	    break;
	default:
	    break;
	}

	if(child != null) {
	    //model.insertNodeInto(child, parent, parent.getChildCount());
	    parent.add(child);

	    if(add_function_table) {
		addMethod(methodname2pathes, name, child.getPath());
	    }
	    
	    NodeList nl = e.getChildNodes();
	    for(int i = 0; i < nl.getLength(); i++) {
		Node newNode = nl.item(i);
		visit(newNode, child, methodname2pathes, filterwords);
	    }
	}
    }

    public static org.w3c.dom.Document parse(InputStream is) throws IOException, SAXException
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
