// HTTPStreamSource.java	Created      : Sun Aug 24 09:55:07 2003
//			Last modified: Mon Oct 27 04:02:23 2003
// Compile: javac HTTPStreamSource.java #
// Execute: java HTTPStreamSource #
// FTP Directory: sources/java #
//------------------------------------------------------------
//
import java.io.*;
import java.util.*;
import java.net.*;

public class HTTPStreamSource implements InputStreamSource {
    URL _proxy_url = null;
    HttpURLConnection _c = null;
    Hashtable _properties = null;
    
    public HTTPStreamSource(String url_string) throws MalformedURLException
    {
	_proxy_url = new URL(url_string);
    }

    public HTTPStreamSource(String url_string, Hashtable properties) throws MalformedURLException
    {
	_proxy_url = new URL(url_string);
	_properties = properties;
    }
	
    public InputStream getInputStream() 
    {
	try {
	    _c = (HttpURLConnection)_proxy_url.openConnection();
	    if(_properties != null) {
		Enumeration keys = _properties.keys();
		while(keys.hasMoreElements()) {
		    String key = (String)keys.nextElement();
		    _c.addRequestProperty(key, (String)_properties.get(key));
		}
	    }
	    _c.connect();
	    return _c.getInputStream();
	}
	catch(Exception e) {
	    e.printStackTrace(System.err);
	}
	return null;
    }
	
    public void afterInput() 
    {
	_c.disconnect();
    }
}

