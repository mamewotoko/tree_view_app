// HTMLTreeView2.java	Created      : Thu Sep  4 23:12:31 2003
//			Last modified: Fri Sep 05 02:58:24 2003
// Compile: javac HTMLTreeView2.java #
// Execute: java HTMLTreeView2 #
// FTP Directory: sources/java #
//------------------------------------------------------------
//

import java.io.*;
import java.net.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import javax.swing.text.html.HTML.*;
import javax.swing.*;
import javax.swing.tree.*;

class HTMLTreeView2 {
    
    static DefaultMutableTreeNode _tree = null;
	
    public static void visit(Document doc, Element e, DefaultMutableTreeNode parent) 
    {
	String name = e.getName();

	if(name.equals("content")) {
	    try {
		parent.add(new DefaultMutableTreeNode("content: " + doc.getText(e.getStartOffset(), e.getEndOffset() - e.getStartOffset())));
	    }
	    catch(Exception ex) {
		ex.printStackTrace();
	    }
	}
	else if(! name.equals("comment")) {
	    DefaultMutableTreeNode new_parent = new DefaultMutableTreeNode(e.getName());
	    parent.add(new_parent);
	    if(! e.isLeaf()) {
		for(int i = 0; i < e.getElementCount(); i++) {
		    visit(doc, e.getElement(i), new_parent);
		}
	    }
	}
    }
    
    public static void main(String[] args) 
    {
	String filename = args.length == 1 ? args[0] : "/home/tak/homepage/index.html";
	try {
	    _tree = new DefaultMutableTreeNode();

	    EditorKit kit = new HTMLEditorKit();
	    Document doc = kit.createDefaultDocument();

	    doc.putProperty("IgnoreCharsetDirective", Boolean.TRUE);
	    kit.read(new FileReader(filename), doc, 0);
	    Element[] el = doc.getRootElements();

	    for(int i = 0; i < el.length; i++) {
		Element current = el[i];
		visit(doc, current, _tree);
	    }
	    
	    JTree tree = new JTree(_tree);
	    JFrame f = new JFrame();
	    JScrollPane scroll = new JScrollPane(tree);
	    f.getContentPane().add(scroll);
	    f.pack();
	    f.setVisible(true);
	}
	catch(Exception e) {
	    e.printStackTrace();
	}
    }
}
