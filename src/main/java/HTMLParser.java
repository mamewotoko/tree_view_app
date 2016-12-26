// HTMLParser.java	Created      : Mon Sep  8 23:42:39 2003
//			Last modified: Mon Oct 20 01:46:46 2003
// Compile: javac HTMLParser.java #
// Execute: java HTMLParser #
// FTP Directory: sources/java #
//------------------------------------------------------------
//
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.text.html.*;
import java.awt.event.*;
import java.awt.*;
import javax.swing.text.*;
import javax.swing.tree.*;

public class HTMLParser {

    private static StringBuffer _content_buffer = new StringBuffer();

    //synchronized
    public static void translate(Document doc, DefaultMutableTreeNode parent, DefaultTreeModel model)
    {
	_content_buffer.delete(0, _content_buffer.length());
	Element e = doc.getRootElements()[0];
	visit(doc, e, parent, model);
    }

    private static void visit(Document doc, Element e, DefaultMutableTreeNode parent, DefaultTreeModel model) 
    {
	String name = e.getName();

	if(name.equals("content")) {
	    try {
		_content_buffer.append(doc.getText(e.getStartOffset(), e.getEndOffset() - e.getStartOffset()));
	    }
 	    catch(Exception ex) { }
	}
	else if(! name.equals("comment")) {
	    // StringBuffer
	    int size = _content_buffer.length();
	    String content = _content_buffer.toString();
	    _content_buffer.delete(0, size);
	    //System.out.println("Parsing: (open)" + content);

	    if(size != 0 && ! content.equals("\n") && !content.equals("\r\n")) {
		DefaultMutableTreeNode child = 
		    new DefaultMutableTreeNode(new TagInfo(content));
 		model.insertNodeInto(child, parent, parent.getChildCount());
	    }

	    AttributeSet attr = e.getAttributes();
	    if(attr == null)
		attr = new SimpleAttributeSet();

	    //to be modified
	    DefaultMutableTreeNode new_parent = null;

 	    if(name.equals("p-implied")) {
 		new_parent = parent;
 	    }
 	    else {
		new_parent = new DefaultMutableTreeNode(new TagInfo(name, attr));
		model.insertNodeInto(new_parent, parent, parent.getChildCount());
 	    }

	    if(! e.isLeaf()) {
		for(int i = 0; i < e.getElementCount(); i++) {
		    visit(doc, e.getElement(i), new_parent, model);
		}
	    }

	    size = _content_buffer.length();
	    content = _content_buffer.toString();
	    _content_buffer.delete(0, size);
	    //System.out.println("Parsing: (close)" + content);

	    if(size != 0 && ! content.equals("\n") && !content.equals("\r\n")) {
		DefaultMutableTreeNode child = new DefaultMutableTreeNode(new TagInfo(content));
 		model.insertNodeInto(child, parent, parent.getChildCount());
	    }
	}
    }

    public static Document parse(InputStream is, String encode) throws FileNotFoundException, IOException
    {
	EditorKit kit = new MyHTMLEditorKit();
	MyHTMLDocument doc = (MyHTMLDocument)kit.createDefaultDocument();
	
	doc.putProperty("IgnoreCharsetDirective", Boolean.TRUE);
	try {
	    kit.read(new InputStreamReader(is, encode), doc, 0);
	}
	catch(BadLocationException e) {
	    e.printStackTrace(System.err);
	}
	return doc;
    }

    public static Document parse(InputStream is) throws FileNotFoundException, IOException
    {
	return parse(is, "EUC-JP");
    }
}
