// InputStreamSource.java	Created      : Sun Aug 24 09:51:53 2003
//			Last modified: Sun Aug 24 09:52:17 2003
// Compile: javac InputStreamSource.java #
// Execute: java InputStreamSource #
// FTP Directory: sources/java #
//------------------------------------------------------------
//
import java.io.*;
import java.util.*;

public interface InputStreamSource {
    public InputStream getInputStream();
    public void afterInput();
}
