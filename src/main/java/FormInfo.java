// FormInfo.java	Created      : Sun Sep 14 01:41:04 2003
//			Last modified: Mon Dec 26 08:26:10 2016
// Compile: javac FormInfo.java #
// Execute: java FormInfo #
// FTP Directory: sources/java #
//------------------------------------------------------------
//
import java.io.*;
import java.util.*;
import java.net.*;

public class FormInfo {

    public FormInfo(String method, String action, Hashtable input, String url_string) 
    {
	_method = method;
	_action = action;
	_input = input;
	_url_string = url_string;
    }
	
    public String getMethod()
    {
	return _method;
    }

    public String getAction()
    {
	return _action;
    }

    public Hashtable getInput()
    {
	return _input;
    }

    public void setValue(String name, String value) 
    {
	InputInfo ii = (InputInfo)_input.get(name);
	if(ii != null) {
	    ii.setValue(value);
	}
    }

    public String toString()
    {
	return this.toURLString();
    }
    
    public String toURLString()
    {
	StringBuffer buffer = new StringBuffer();
	String charset = "Shift_JIS"; // TODO: config
	Enumeration keys = _input.keys();

	try {
	    while(keys.hasMoreElements()) {
		Object key = keys.nextElement();
		InputInfo ii = (InputInfo)_input.get(key);
		if(ii.isValueMutable()) {
		    buffer.append("&" + URLEncoder.encode(ii.getID(), charset) + "=" + URLEncoder.encode(ii.getValue(), charset));
		}
		else {
		    buffer.append("&" +  URLEncoder.encode(ii.getID(), charset) + "=" + URLEncoder.encode(ii.getValue(), charset));
		}
	    }
	}
	catch(UnsupportedEncodingException e) {
	    e.printStackTrace(System.err);
	}
	
	String result = buffer.toString();
	return result.substring(1, result.length());
    }
    
    String concatPath(String p, String f) 
    {
	if(p.charAt(p.length()-1) == '/') {
	    if(f.charAt(0) == '/') {
		return p+f.substring(1, f.length());
	    }
	    else {
		return p + f;
	    }
	}
	else {
	    if(f.charAt(0) == '/') {
		return p+f;
	    }
	    else {
		return p+'/' + f;
	    }
	}
    }
    
    public InputStreamSource getInputStreamSource() 
    {
	try {
	    String action = "";
	    Hashtable properies = new Hashtable();
	    properies.put("User-Agent", "Mozilla/5.0");
		
	    if(_action.length() > 7 &&_action.toString().substring(0, 7).equals("http://")) {
		System.out.println("URL: (without base) "+ _action);
		action = _action;
	    }
	    else {
		try {
		    URL prevurl = new URL(_url_string);
		    String host = prevurl.getAuthority();
		    String path = prevurl.getPath();
		    int pos = path.lastIndexOf('/');
		    if(pos > 0) {
			path = path.substring(0, pos);
		    }
		    System.out.println("HOST: " + host);
		    action = "http://" + host + concatPath(path ,_action);
		}
		catch(MalformedURLException e) {
		    return null;
		}
	    }
	    if(_method.compareToIgnoreCase("GET") == 0) {
		System.out.println("GET method: " + action + "?" + this.toURLString());
		return new HTTPStreamSource(action+"?"+this.toURLString(), properies);
	    }
	    else if(_method.compareToIgnoreCase("POST") == 0) {
		System.out.println("POST method");
		return new HTTPPostStreamSource(action, this.toURLString(), properies);
	    }
	    else {
		System.out.println("What? : " + _method);
		return null;
	    }
	}
	catch(Exception e) {
	    e.printStackTrace();
	    return null;
	}
    }

    String _method;
    String _action;
    String _url_string;
    Hashtable _input;
}

