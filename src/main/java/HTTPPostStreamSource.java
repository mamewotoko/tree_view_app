// HTTPPostStreamSource.java	Created      : Mon Oct 20 01:24:37 2003
//			Last modified: Mon Oct 27 04:01:54 2003
// Compile: javac HTTPPostStreamSource.java #
// Execute: java HTTPPostStreamSource #
// FTP Directory: sources/java #
//------------------------------------------------------------
//
import java.io.*;
import java.util.*;
import java.net.*;

public class HTTPPostStreamSource implements InputStreamSource {
    String _content;
    URL _url;
    HttpURLConnection _c = null;
    Hashtable _properties = null;
    
    public HTTPPostStreamSource(String url_string, String content) throws MalformedURLException
    {
	_url = new URL(url_string);
	_content = content;
    }

    public HTTPPostStreamSource(String url_string, String content, Hashtable properies) throws MalformedURLException
    {
	_url = new URL(url_string);
	_content = content;
	_properties = properies;
    }

    public InputStream getInputStream() 
    {
	try {
	    _c = (HttpURLConnection)_url.openConnection();
	    if(_properties != null) {
		Enumeration keys = _properties.keys();
		while(keys.hasMoreElements()) {
		    String key = (String)keys.nextElement();
		    _c.addRequestProperty(key, (String)_properties.get(key));
		}
	    }

	    _c.setRequestMethod("POST");
	    _c.setDoOutput(true);
	    OutputStream os = _c.getOutputStream();

	    //debug
	    System.out.println("URL: " + _url.toString());
	    System.out.println("CONTENT: " + _content);
	    
	    os.write(_content.getBytes());
	    os.flush();
	    _c.connect();
	    InputStream is = _c.getInputStream();

	    return is;
	}
	catch(MalformedURLException e) {
	    e.printStackTrace();
	}
	catch(Exception e) {
	    e.printStackTrace();
	}
	System.out.println();
	return null;
    }

    public void afterInput() 
    {
	_c.disconnect();
    }
}
