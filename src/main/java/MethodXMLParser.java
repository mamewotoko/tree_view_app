// MethodXMLParser.java	Created      : Sun Sep 14 17:00:24 2003
//			Last modified: Mon Nov 10 01:32:54 2003
// Compile: javac MethodXMLParser.java #
// Execute: java MethodXMLParser #
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

public class MethodXMLParser {
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

    /**
     * @param methodname
     * @param methodname2pathes methodname -> MethodPathInfo
     */
    public static void addMethod(Hashtable methodname2pathes, String methodname, MethodPathInfo mpi) 
    {
	//System.out.println("MethodXMLInfo.addMethod: methodname = " + methodname);

	Vector v = (Vector)methodname2pathes.get(methodname);
	if(v != null) {
	    v.add(mpi);
	}
	else {
	    v = new Vector();
	    v.add(mpi);
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
	boolean add_method_table = false;
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
	    if(! filterwords.contains(name)) {
		ti = new EditableTagInfo(name, nodeMap2AttributeSet(e.getAttributes()));
		child = new DefaultMutableTreeNode(new EditableTagInfo(name, nodeMap2AttributeSet(e.getAttributes())));
		Object value = ti.getValueByName("kind");
		typestr = ti.getValueByName("type");
		//System.out.println("value = " + value);
		add_method_table = (value != null) && value.toString().equals("method");
	    }
	    else {
		child = null;
	    }
	    break;
	case Node.COMMENT_NODE:
	    break;
	default:
	    break;
	}

	if(child != null) {
	    //model.insertNodeInto(child, parent, parent.getChildCount());
	    parent.add(child);

	    if(add_method_table) {
		String type = typestr == null ? "" : typestr.toString();
		addMethod(methodname2pathes, name, new MethodPathInfo(child.getPath(), type));
	    }
	    
	    NodeList nl = e.getChildNodes();
	    
	    for(int i = 0; i < nl.getLength(); i++) {
		Node newNode = nl.item(i);
		visit(newNode, child, methodname2pathes, filterwords);
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
