import java.io.*;
import java.util.*;
public class SelectInputInfo extends InputInfo {
    Vector _option_value = null;
    
    SelectInputInfo(String id, Vector option_value) 
    {
	super(InputInfo.SELECT, id, "");
	_option_value = option_value;
    }

    public Vector getOptionValue()
    {
	return _option_value;
    }
}
