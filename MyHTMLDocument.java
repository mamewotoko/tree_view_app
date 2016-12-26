// MyHTMLDocument.java	Created      : Tue Sep  9 00:10:20 2003
//			Last modified: Sun Sep 21 20:16:29 2003
// Compile: javac MyHTMLDocument.java #
// Execute: java MyHTMLDocument #
// FTP Directory: sources/java #
//------------------------------------------------------------
//
import java.io.*;
import java.util.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import javax.swing.text.html.*;
// import javax.swing.text.html.HTMLDocument;

public class MyHTMLDocument extends HTMLDocument {
    
    public class MyHTMLReader extends HTMLDocument.HTMLReader {
	public MyHTMLReader(int offset)
	{
	    super(offset);
	    this.registerTag(HTML.Tag.A, new BlockAction());
	    this.registerTag(HTML.Tag.SELECT, new BlockAction());
	    this.registerTag(HTML.Tag.OPTION, new BlockAction());
	    this.registerTag(HTML.Tag.FORM, new BlockAction());
	    this.registerTag(HTML.Tag.TEXTAREA, new BlockAction());
	    this.registerTag(HTML.Tag.P, new BlockAction());
	}
    }

    public MyHTMLDocument() 
    {
	super();
    }

    public HTMLEditorKit.ParserCallback getReader(int offset)
    {
	return new MyHTMLReader(offset);
    }
}
