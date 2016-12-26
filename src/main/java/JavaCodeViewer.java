// JavaCodeViewer.java	Created      : Sat Sep 27 02:49:20 2003
//			Last modified: Sun Sep 28 14:06:24 2003
// Compile: javac JavaCodeViewer.java #
// Execute: java JavaCodeViewer #
// FTP Directory: sources/java #
//------------------------------------------------------------
//
import java.io.*;
import java.util.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

public class JavaCodeViewer extends JEditorPane {

    static int digit(int num, int digit) 
    {
	if(num < 10) {
	    return digit;
	}
	else {
	    return digit(num/10, digit+1);
	}
    }

    static String space(int num) 
    {
	StringBuffer sb = new StringBuffer();
	for(int i = 0; i < num; i++) {
	    sb.append(" ");
	}
	return sb.toString();
    }

    public void openFile(String filename)
    {
	final Vector line2position = new Vector();
	CodeViewer cv = new CodeViewer();

	try {
	    InputStream is = new FileInputStream(filename);
	    InputStreamReader isr = new InputStreamReader(is);
	    BufferedReader br = new BufferedReader(isr);
	    String line = null;
	    StringBuffer sb = new StringBuffer();
	    String header = "<html><head></head><body><pre>";

	    int linenum = 1;
	    int digit = 0;

	    while((line = br.readLine()) != null) {
		String content = "<b><font color=\"#aabbff\">"+ space(5-digit(linenum, 1)) + linenum + 
		    "</font></b>  " + cv.syntaxHighlight(line) + "<br>";
		sb.append(content);
		linenum++;
	    }
	    this.setText(header + sb.toString() + "</pre></body></html>");
	}
	catch(Exception e) {
	    e.printStackTrace();
	}

    }

    public JavaCodeViewer (String filename) 
    {
	super("text/html", "");
	this.openFile(filename);
    }
    
    public JavaCodeViewer ()
    {
	super("text/html", "");
    }
}
