import java.io.*;
import java.util.*;
import javax.swing.text.*;

public class TagInfo implements Serializable {
    public TagInfo (String tag)
    {
	this(tag, new SimpleAttributeSet());
    }

    public TagInfo (String tag, AttributeSet attr)
    {
	_tag = tag;
	_attr = attr == null ? new SimpleAttributeSet() : attr;
    }

    public String getTag()
    {
	return _tag;
    }
	
    public AttributeSet getAttr()
    {
	return _attr;
    }

    public String toString()
    {
	return _tag;
    }
    
    public String toXML()
    {
	StringBuffer sb = new StringBuffer();
	sb.append(_tag);
	Enumeration e = _attr.getAttributeNames();
	while(e.hasMoreElements()) {
	    Object key = e.nextElement();
	    Object value = _attr.getAttribute(key);
	    sb.append(" " + key.toString() + "=\"" + value.toString() + "\"");
	}
	return sb.toString();
    }

    public Object getValueByName(String name)
    {
	if(_attr == null) {
	    //System.out.println("getValueByName: _attr == null");
	    return null;
	}
	Enumeration e = _attr.getAttributeNames();

	while(e.hasMoreElements()) {
	    Object element = e.nextElement();
	    String elementName = element.toString();
	    if(elementName.equals(name)) {
		return _attr.getAttribute(element);
	    }
	}
	return null;
    }
    
    public String toOriginalString()
    {
	return "";
    }

    String _tag;
    AttributeSet _attr;
}
