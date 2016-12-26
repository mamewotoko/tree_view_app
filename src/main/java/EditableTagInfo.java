// EditableTagInfo.java	Created      : Thu Sep 25 22:25:07 2003
//			Last modified: Sat Oct 04 17:08:50 2003
// Compile: javac EditableTagInfo.java #
// Execute: java EditableTagInfo #
// FTP Directory: sources/java #
//------------------------------------------------------------
// For MethodTableViewer only
import java.io.*;
import java.util.*;
import javax.swing.text.*;

public class EditableTagInfo extends TagInfo implements Serializable {
    public EditableTagInfo(String tag)
    {
	super(tag);
    }
    
    public EditableTagInfo(String tag, AttributeSet attr)
    {
	super(tag, attr);
    }

    public void addAttribute(String name, String value)
    {
	//System.out.println("EditableTagInfo.addAttribute called");
	((SimpleAttributeSet)_attr).addAttribute(name, value);
    }

    public String toString() 
    {
	String comment = "";
	if(_attr != null) {
	    Object val = _attr.getAttribute("comment");
	    if(val != null) {
		comment = " //" + val.toString();
	    }
	}

	return super.toString() + comment;
    }

    public void removeValueByName(String name)
    {
	if(_attr != null) {
	    ((SimpleAttributeSet)_attr).removeAttribute(name);
	}
    }
}
