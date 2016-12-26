// MyHTMLEditorKit.java	Created      : Tue Sep  9 00:19:48 2003
//			Last modified: Tue Sep 09 00:22:52 2003
// Compile: javac MyHTMLEditorKit.java #
// Execute: java MyHTMLEditorKit #
// FTP Directory: sources/java #
//------------------------------------------------------------
//
import java.io.*;
import java.util.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import javax.swing.text.*;
import javax.swing.text.html.*;

public class MyHTMLEditorKit extends HTMLEditorKit {
    public Document createDefaultDocument ()
    {
	return new MyHTMLDocument();
    }
}
