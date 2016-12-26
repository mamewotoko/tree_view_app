// HTTPPost.java	Created      : Fri Sep 12 23:47:32 2003
//			Last modified: Sun Sep 14 03:12:06 2003
// Compile: javac HTTPPost.java #
// Execute: java HTTPPost #
// FTP Directory: sources/java #
//------------------------------------------------------------
// う、自虐的だ。
// これをつかって teacup 荒しちゃだめ。
import java.io.*;
import java.util.*;
import java.net.*;

public class HTTPPost {

    public static String post(URL url, Hashtable hash, Hashtable property, String charset) 
    {
	Enumeration keys = hash.keys();
	StringBuffer parameter = new StringBuffer();
	
	try {
	    Object key = null;
	    Object value = null;
	    if(keys.hasMoreElements()) {
		key = keys.nextElement();
		value = hash.get(key);
		parameter.append(URLEncoder.encode(key.toString(), charset) + "=" 
				 + URLEncoder.encode(value.toString(), charset));
		while(keys.hasMoreElements()) {
		    key = keys.nextElement();
		    value = hash.get(key);
		    parameter.append("&" + URLEncoder.encode(key.toString(), charset) + "=" 
				     + URLEncoder.encode(value.toString(), charset));
		}
	    }
	    
	    HttpURLConnection connection = (HttpURLConnection)url.openConnection();
	    connection.setRequestMethod("POST");

	    keys = property.keys();
	    while(keys.hasMoreElements()) {
		key = keys.nextElement();
		value = property.get(key);
		connection.addRequestProperty(key.toString(), value.toString());
	    }

	    connection.setDoOutput(true);
	    OutputStream os = connection.getOutputStream();
	    os.write(parameter.toString().getBytes());
	    os.flush();
	    connection.connect();
	    InputStream is = connection.getInputStream();
	    
	    StringBuffer result = new StringBuffer();
	    BufferedReader br = new BufferedReader(new InputStreamReader(is, "Shift_JIS"));
	    String data = null;
	    while((data=br.readLine()) != null) {
		result.append(data + "\n");
	    }
	    connection.disconnect();
	    br.close();

	    return result.toString();
	}
	catch(Exception e) {
	    e.printStackTrace();
	    return null;
	}
    }

    public static void main(String argv[]) 
    {
	String url_string = "http://kaju.net/cgi-bin/sample_bbs/mbbs/sub.cgi";
	Hashtable hash = new Hashtable();
	Hashtable property = new Hashtable();

	//tstamp=1063447039&m=&cmd=reg_01_g841227
	try {
	    URL url = new URL(url_string);
 	    property.put("REFERER", url_string);
// 	    hash.put("tstamp", "1063447039");
// 	    hash.put("m", "");
// 	    hash.put("subject", "ビビデバビデブー");
// 	    hash.put("name", "まめをとこ");
// 	    hash.put("cmd", "reg_01_g841227");
// 	    hash.put("email", "mamewo@okuiaki.com");
// 	    hash.put("page", "http://www002.upp.so-net.ne.jp/mamewo/");
// 	    hash.put("value", "> のさま\r\nちょっとまってよぉ。とりあえずちゃんと寝るのだよぉ。");
	    hash.put("name", "とおりすがり");
	    hash.put("kao", "(-_- )");
	    hash.put("comment", "てすとです。");
	    hash.put("submit", "完成");
	    hash.put("password", "taka");
	    hash.put("mode", "regist");
	    
	    System.out.println(post(url, hash, property, "Shift_JIS"));
	}
	catch(Exception e) {
	    e.printStackTrace();
	}
    }
}
