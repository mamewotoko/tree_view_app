// InputInfo.java	Created      : Sun Sep 14 01:42:22 2003
//			Last modified: Mon Oct 27 00:27:19 2003
// Compile: javac InputInfo.java #
// Execute: java InputInfo #
// FTP Directory: sources/java #
//------------------------------------------------------------
//
import java.io.*;
import java.util.*;

public class InputInfo {
    public InputInfo (int type, String id, String value)
    {
	_type = type;
	_id = id;
	_value = value;
    }

    public int getType()
    {
	return _type;
    }

    public String getID()
    {
	return _id;
    }
	
    public boolean isValueMutable()
    {
	return _type == TEXT || _type == TEXTAREA;
    }

    public String getValue()
    {
	return _value == null ? "" : _value;
    }

    public void setValue(String value)
    {
	_value = value;
    }

    public String toString()
    {
	return _value == null ? "" : _value.toString();
    }

    int _type;
    String _id;
    String _value;

    public static final int HIDDEN = 0;
    public static final int TEXT = 1;
    public static final int TEXTAREA = 2;
    public static final int SUBMIT = 3;
    public static final int RESET = 4;
    public static final int BUTTON = 5;
    public static final int SELECT = 6;

    public static final int NONE = -1;

    public static int typestr2type(Object obj) 
    {
	String typestr = obj.toString();
	if(typestr.equals("hidden"))
	    return HIDDEN;
	else if (typestr.equals("text") || typestr.equals("password")) 
	    return TEXT;
	else if (typestr.equals("textarea"))
	    return TEXTAREA;
	else if (typestr.equals("submit"))
	    return SUBMIT;
	else if (typestr.equals("reset"))
	    return RESET;
	else if (typestr.equals("button"))
	    return BUTTON;
	else if (typestr.equals("select"))
	    return SELECT;
	else {
	    System.out.println("Unsupported type: " + typestr);
	    return NONE;
	}
    }
}

