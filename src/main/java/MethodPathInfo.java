// MethodPathInfo.java	Created      : Mon Sep 29 00:32:49 2003
//			Last modified: Mon Dec 26 09:45:54 2016
import java.io.*;
import java.util.*;
import javax.swing.tree.*;

public class MethodPathInfo {
    public MethodPathInfo(TreeNode[] path, String typestr)
    {
// 	for(int i = 0; i < path.length; i++) {
// 	    System.out.println(path[i]);
// 	}
	_path = path;
	_typestr = typestr;
    }

    public String toString()
    {
	String path_string = "";
	if(_path.length > CLASS_LEVEL) {
	    for(int j = CLASS_LEVEL; j < _path.length-1; j++) {
		path_string += _path[j].toString()+".";
	    }
	    path_string += _path[_path.length-1].toString();
	}
	return path_string + "(" + _typestr + ")";
    }

    public String getMethodName()
    {
	return _path[_path.length-1].toString();
    }

    public TreePath getPath()
    {
	return new TreePath(_path);
    }
	
    private String _typestr;
    private TreeNode[] _path = null;
    public static final int CLASS_LEVEL = 1;
}
