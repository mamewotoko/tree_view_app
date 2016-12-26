// InputStreamSource.java	Created      : Sun Aug 24 09:51:53 2003
//			Last modified: Mon Dec 26 09:38:53 2016
import java.io.*;
import java.util.*;

public interface InputStreamSource {
    public InputStream getInputStream();
    public void afterInput();
}
