// HTMLTree.java	Created      : Fri Sep  5 17:23:47 2003
//			Last modified: Mon Dec 26 08:29:37 2016
//------------------------------------------------------------

import java.io.*;
import java.util.*;
import java.net.*;
import java.awt.event.*;
import java.awt.*;
import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.text.html.*;
import javax.swing.text.html.HTML.*;
import javax.swing.*;
import javax.swing.tree.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import org.xml.sax.*;

public class HTMLTree {

    //     static Vector _tree_history = new Vector();
    //     static Vector _current_index = 0;

    static DefaultMutableTreeNode _tree = null;
    static DefaultTreeModel _treeModel = null;
    static TreePath _rootpath = null;
    static SearchResult _previous_search = null;
    static final JLabel status_label = new JLabel(" ");
    static final JTree _tree_gui = new TypeAheadTree();
    static JFrame _frame = null;
    static final JTextField field = new JTextField();
    static final JTextField _url_field = new JTextField();
    static String _url_string = null;
    static final JTextPane _message_area = new JTextPane();

    private static StringBuffer _content_buffer = new StringBuffer();
    //li を閉じる??
    public static boolean loose_mode = true;

    public static String getSuffix(String filename) 
    {
        int pos = filename.lastIndexOf('.');
        if(pos > 0) {
            int length = filename.length();
            return filename.substring(pos, length);
        }
        else {
            return "";
        }
    }

    public static class Continuation {
        public Continuation(TreePath path, Enumeration children)
        {
            _path = path;
            _children = children;
        }

        public Enumeration getChildren()
        {
            return _children;
        }
	
        public TreePath getPath()
        {
            return _path;
        }
	
        public void setNext(Continuation continuation)
        {
            _continuation = continuation;
        }

        public Continuation getNext()
        {
            return _continuation;
        }

        Enumeration _children = null;
        TreePath _path = null;
        Continuation _continuation = null;
        Continuation _last = null;
    }


    public static class SearchResult {
        public SearchResult(TreePath result, String keyword)
        {
            _result = result;
            _keyword = keyword;
        }

        public TreePath getResult()
        {
            return _result;
        }
	
        public void addContinuation(Continuation continuation)
        {
            if(_continuation == null) {
                _continuation = continuation;
                _last_continuation = continuation;
            }
            else {
                _last_continuation.setNext(continuation);
                _last_continuation = continuation;
            }
        }

        public Continuation getContinuation()
        {
            return _continuation;
        }

        public String getKeyword()
        {
            return _keyword;
        }

        TreePath _result = null;
        String _keyword = null;
        Continuation _continuation = null;
        Continuation _last_continuation = null;
    }
    
    public static TreePath searchLoosely(DefaultMutableTreeNode node, TreePath currentpath, String str) 
    {
        String node_str = node.toString();
        if(node_str.indexOf(str) != -1) {
            return currentpath;
        }
        else {
            if(node.isLeaf()) {
                return null;
            }
            else {
                Enumeration e = node.children();
                while(e.hasMoreElements()) {
                    DefaultMutableTreeNode child =
                        (DefaultMutableTreeNode)e.nextElement();
                    TreePath newpath = currentpath.pathByAddingChild(child);
                    TreePath resultpath = searchLoosely(child, newpath, str);
                    if(resultpath != null) {
                        return resultpath;
                    }
                }
                return null;
            }
        }
    }

    public static SearchResult iSearchLoosely(DefaultMutableTreeNode node, TreePath currentpath, String str) 
    {
        String node_str = node.toString();
        if(node_str.indexOf(str) != -1) {
            SearchResult result = new SearchResult(currentpath, str);
            if(! node.isLeaf()) {
                result.addContinuation(new Continuation(currentpath, node.children()));
            }
            return result;
        }
        else {
            if(node.isLeaf()) {
                return null;
            }
            else {
                Enumeration e = node.children();
                while(e.hasMoreElements()) {
                    DefaultMutableTreeNode child =
                        (DefaultMutableTreeNode)e.nextElement();
                    TreePath newpath = currentpath.pathByAddingChild(child);

                    SearchResult result = iSearchLoosely(child, newpath, str);

                    if(result != null) {
                        result.addContinuation(new Continuation(currentpath, e));
                        return result;
                    }
                }
                return null;
            }
        }
    }

    public static SearchResult nextSearchOnContinuation(Continuation continuation, String keyword)
    {
        if(continuation == null) {
            return null;
        }
        else {
            SearchResult result = null;
            Enumeration children = continuation.getChildren();
            while(children.hasMoreElements()) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)children.nextElement();
                TreePath path = continuation.getPath().pathByAddingChild(node);
                //System.out.println("Searching: " + path);
                result = iSearchLoosely(node, path, keyword);

                if(result != null) {
                    result.addContinuation(continuation);
                    return result;
                }
            }
            return nextSearchOnContinuation(continuation.getNext(), keyword);
        }
    }
    

    public static SearchResult nextSearch(SearchResult sr) 
    {
        String keyword = sr.getKeyword();
        Continuation _continuation = sr.getContinuation();
        return nextSearchOnContinuation(_continuation, keyword);
    }

    public static boolean printEasyRegexp(DefaultMutableTreeNode node, boolean isPreviousNode)
    {
        String tagname = node.toString();
        if(node.isLeaf()) {
            if(tagname.equals("hr")) {
                String data = "<hr[^>]*>";
                if(isPreviousNode) {
                    data = "[^<]*" + data;
                }
                System.out.print(data);
                return true;
            }
            else {
                //System.out.println("isPreviousNode: " + isPreviousNode);
                if(isPreviousNode) {
                    System.out.print("(.*?)");
                    return false;
                }
                else {
                    return false;
                }
            }
        }
        else {
            // TO BE MODIFIED
            //if(tagname.equals("tr") || tagname.equals("td")) {
            //open & close
            boolean display_tag = !tagname.equals("p-implied");
            boolean is_endopt = false;

            if(display_tag) {
                String data = "<"+tagname+"[^>]*>";
                if(isPreviousNode) {
                    //previous is tag!
                    data = "[^<]*"+data;
                }
                System.out.print(data);
            }

            boolean isPrevChildNode = true;
            Enumeration e = node.children();
            while(e.hasMoreElements()) {
                DefaultMutableTreeNode current =
                    (DefaultMutableTreeNode)e.nextElement();
                isPrevChildNode = printEasyRegexp(current, isPrevChildNode);
            }
	    
            if(display_tag) {
                String data = "</"+tagname+">";
                if(isPrevChildNode) {
                    // last children is tag!
                    data = "[^<]*" + data;
                }

                if(tagname.equals("li")) {
                    is_endopt = true;
                    //</li> のあとにはテキストはこないのである
                    data = "(" + data + "[^>]*)?";
                }
                System.out.print(data);
            }
            //display_tag <=> p-implied
            return (is_endopt || !display_tag) ? isPrevChildNode : true;
        }
    }
    
    public static DefaultMutableTreeNode getChildTree(TreePath path)
    {
        DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode)(path.getLastPathComponent());
        return currentNode;
    }

    public static String getPathRegexp(TreePath path)
    {
        Object[] objpath = path.getPath();
        int size = objpath.length;
        StringBuffer result = new StringBuffer();

        for(int i = 1; i < size-1; i++) {
            String tagname = objpath[i].toString();
            result.append("<"+tagname+"[^>]*>.*?");
        }
        result.append("(<"+objpath[size-1]+"[^>]*>.*)$");
        return result.toString();
    }

    public static void tree2string(StringBuffer buffer, DefaultMutableTreeNode node, boolean do_print_node)
    {
        if(do_print_node || node.isLeaf()) {
            buffer.append(node.toString() + "\n");
        }
        Enumeration e = node.children();
        while(e.hasMoreElements()) {
            DefaultMutableTreeNode current = (DefaultMutableTreeNode)e.nextElement();
            tree2string(buffer, current, do_print_node);
        }
    }

    public static void subGetForm(DefaultMutableTreeNode node, Hashtable hash) 
    {
        //木をたどる
        String nodestr = node.toString();

        if(nodestr.equals("select")) {
            TagInfo ti = (TagInfo)node.getUserObject();
            String select_name = (String)ti.getValueByName("name");

            if(select_name != null) {
                //value がなくてただの飾りってのもあるなぁ
                Vector options = new Vector();
                Enumeration option_enum = node.children();
                while(option_enum.hasMoreElements()) {
                    DefaultMutableTreeNode option_node = (DefaultMutableTreeNode)option_enum.nextElement();
                    TagInfo option_tag_info = (TagInfo)option_node.getUserObject();

                    String value = (String)option_tag_info.getValueByName("value");
                    if(option_node.toString().equals("option") && value != null) {
                        if(! option_node.isLeaf()) {
                            String representation = ((DefaultMutableTreeNode)option_node.children().nextElement()).toString();
                            //debug
                            System.out.println("representation: " + representation);
                            options.add(new FormFrame.SelectItem(value, representation));
                        }
                        else {
                            System.out.println("Option node is leaf");
                        }
                    }
                    else 
                        System.out.println();
                }
                //うーん汚い。   
                hash.put(select_name, new SelectInputInfo(select_name, options));
            }
            else 
                System.out.println("Selectedname == null");
        }
        else if(nodestr.equals("input") || nodestr.equals("textarea") || nodestr.equals("password")) {
            TagInfo ti = (TagInfo)node.getUserObject();
            String tag_name = ti.getTag();
            AttributeSet attr = ti.getAttr();
            Enumeration keys = attr.getAttributeNames();
            String name = null;
            String value = null;
            int type = InputInfo.NONE;

            if(nodestr.equals("textarea")) {
                type = InputInfo.typestr2type(nodestr);
            }

            while(keys.hasMoreElements()) {
                Object key = keys.nextElement();
                Object v = attr.getAttribute(key);

                if(key.toString().equals("name") && (! v.toString().equals(nodestr))) {
                    name = v.toString();
                }
                else if(key.toString().equals("value")) {
                    value = v.toString();
                }
                else if(key.toString().equals("type")) {
                    type = InputInfo.typestr2type(v);
                    if(v.toString().equals("submit") && name == null) {
                        name = "submit";
                    }
                }
            }

            if(name != null) {
                hash.put(name, new InputInfo(type, name, value));
            }
        }
        else if(! node.isLeaf()) {
            Enumeration e = node.children();
            while(e.hasMoreElements()) {
                DefaultMutableTreeNode child = (DefaultMutableTreeNode)e.nextElement();
                subGetForm(child, hash);
            }
        }
    }

    //トップノードは form
    public static FormInfo getForm(DefaultMutableTreeNode node)
    {
        //String -> String
        Hashtable hash = new Hashtable();
        if(node.toString().equals("form")) {
            //get
            TagInfo ti = (TagInfo)node.getUserObject();
            AttributeSet attr = ti.getAttr();
            Enumeration keys = attr.getAttributeNames();
            String method = "get";
            String action = null;
	    
            while(keys.hasMoreElements()) {
                Object key = keys.nextElement();
                Object value = attr.getAttribute(key);

                if(key.toString().equals("method")) {
                    method = value.toString();
                }
                else if(key.toString().equals("action")) {
                    action = value.toString();
                }
            }
	    
            subGetForm(node, hash);
            return new FormInfo(method, action, hash, _url_string);
        }
        return null;
    }

    public static boolean isTreeBinding(KeyEvent e)
    {
        String keystr = KeyEvent.getKeyText(e.getKeyCode());
        //!?!?!
        String[] bindstr = { "R", "P", "O", "L", "G", "F", "E", "S", "T" };
        if(e.isControlDown()) {
            for(int i = 0; i < bindstr.length; i++) {
                if(keystr.equals(bindstr[i]))
                    return true;
            }
            return false;
        }
        else {
            return false;
        }
    }

    //微妙なネーミングだ。
    public static void showHTML(InputStream is) 
    {
        _tree = new DefaultMutableTreeNode("ROOT");
        _rootpath = new TreePath(_tree);
        _treeModel = new DefaultTreeModel(_tree);

        try {
            HTMLParser.translate(HTMLParser.parse(is, "JISAutoDetect"), _tree, _treeModel);
        }
        catch(Throwable t) { }
        _previous_search = null;
        _tree_gui.setModel(_treeModel);
    }
    
    public static void show(String filename) 
    {
        if(filename.length() == 0) 
            return;
	
        String suffix = getSuffix(filename);

        _tree = new DefaultMutableTreeNode("ROOT");
        _rootpath = new TreePath(_tree);
        _treeModel = new DefaultTreeModel(_tree);
	
        InputStream is = null;
        HTTPStreamSource hss = null;
	
        try {
            try {
                hss = new HTTPStreamSource(filename);
                is = hss.getInputStream();
            }
            catch(MalformedURLException e) {
                is = new FileInputStream(filename);
            }
	    
            if(suffix.equals(".html") || suffix.equals(".htm")) {
                System.out.println("HTML mode");
                HTMLParser.translate(HTMLParser.parse(is, "JISAutoDetect"), _tree, _treeModel);
            }
            else {
                System.out.println("try XML mode");
                try {
                    XMLParser.translate(XMLParser.parse(is), _tree, _treeModel);
                }
                catch(SAXParseException e) {
                    is.close();
                    try {
                        hss = new HTTPStreamSource(filename);
                        is = hss.getInputStream();
                    }
                    catch(MalformedURLException ex) {
                        is = new FileInputStream(filename);
                    }
                    catch(Exception e1) {
                        e1.printStackTrace();
                        return;
                    }

                    try {
                        HTMLParser.translate(HTMLParser.parse(is, "JISAutoDetect"), _tree, _treeModel);
                    }
                    catch(Exception ex) { } //ignore
                }
            }
	    
            if(hss != null)
                hss.afterInput();
            is.close();
            _previous_search = null;
            _frame.setTitle(filename);
        }
        catch(Throwable e) {
            status_label.setText("show error: " + e.getMessage());
            // 	    e.printStackTrace();
        }
        _tree_gui.setModel(_treeModel);
    }
    
    public static void main(String[] args) 
    {
        String filename = args.length == 1 ? args[0] : "";
	
        try {
            JFrame.setDefaultLookAndFeelDecorated(true);
            _frame = new JFrame();
            _tree_gui.getSelectionModel().setSelectionMode(TreeSelectionModel.CONTIGUOUS_TREE_SELECTION);

            _url_field.setText(filename);
            _frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            _message_area.setEditable(false);

            _tree_gui.addKeyListener(new KeyAdapter () {
                    public void keyPressed(KeyEvent e) 
                    {
                        TreePath[] paths = _tree_gui.getSelectionPaths();
                        String keystr = KeyEvent.getKeyText(e.getKeyCode());
                        if(e.isControlDown() && keystr.equals("R")) {
                            //print regexp 
                            if(paths != null) {
                                for(int i = 0; i < paths.length; i++) {
                                    printEasyRegexp(getChildTree(paths[i]), true);	
                                }
                            }
                            System.out.println("");
                        }
                        else if (e.isControlDown() && keystr.equals("T")) {
                            //print path
                            if(paths != null) {
                                StringBuffer sb = new StringBuffer();
                                for(int i = 0; i < paths.length; i++) {
                                    TagInfo ti = (TagInfo)((DefaultMutableTreeNode)(paths[i].getLastPathComponent())).getUserObject();
                                    sb.append(ti.toOriginalString() + "\n");
                                }
                                _message_area.setText(sb.toString());
                            }
                        }
                        else if (e.isControlDown() && keystr.equals("P")) {
                            //print path
                            if(paths != null) {
                                for(int i = 0; i < paths.length; i++) {
                                    System.out.println(paths[i]);
                                    System.out.println("=================");
                                }
                            }
                        }
                        else if(e.isControlDown() && keystr.equals("O")) {
                            if(paths != null) {
                                StringBuffer sb = new StringBuffer();
                                for(int i = 0; i < paths.length; i++) {
                                    tree2string(sb, getChildTree(paths[i]), false);
                                }
                                _message_area.setText(sb.toString());
                            }
                        }
                        else if(e.isControlDown() && keystr.equals("L")) {
                            if(paths != null) {
                                for(int i = 0; i < paths.length; i++) {
                                    System.out.println(getPathRegexp(paths[i]));
                                }
                            }
                        }
                        else if(e.isControlDown() && keystr.equals("G")) {
                            if(paths != null) {
                                DefaultMutableTreeNode node = (DefaultMutableTreeNode)(paths[0].getLastPathComponent());			    
                                FormInfo fi = getForm(node);
                                if(fi != null) {
                                    FormFrame formframe = new FormFrame(fi, _url_string);
                                    formframe.setVisible(true);
                                }
                            }				
                        }
                        else if(e.isControlDown() && keystr.equals("F")) {
                            if(paths != null) {
                                DefaultMutableTreeNode node = (DefaultMutableTreeNode)(paths[0].getLastPathComponent());
                                FormInfo fi = getForm(node);
                                if(fi != null) {
                                    System.out.println(fi);
                                }
                            }
                        }
                        else if(e.isControlDown() && keystr.equals("E")) {
                            if(paths != null) {
                                DefaultMutableTreeNode node = (DefaultMutableTreeNode)(paths[0].getLastPathComponent());
                                TagInfo ti = (TagInfo)node.getUserObject();
                                String tag_name = ti.getTag();
                                AttributeSet attr = ti.getAttr();
                                if(attr != null) {
                                    Enumeration keys = attr.getAttributeNames();
                                    while(keys != null && keys.hasMoreElements()) {
                                        Object key = keys.nextElement();
                                        Object value = attr.getAttribute(key);
                                        if(! (key.toString().equals("name") && value.toString().equals(tag_name))) {
                                            System.out.println(key+"="+value);
                                        }
                                    }
                                    System.out.println("------------------");
                                }
                            }
                        }
                        else if(e.isControlDown() && keystr.equals("S")) {

                            if(_previous_search != null) {
                                SearchResult tmp = nextSearch(_previous_search);

                                if(tmp != null) {
                                    TreePath path = tmp.getResult();
                                    _tree_gui.expandPath(path);
                                    _tree_gui.clearSelection();
                                    _tree_gui.addSelectionPath(path);
                                    _tree_gui.scrollPathToVisible(path);
					
                                    _previous_search = tmp;
                                    System.out.println("Found: " + path);
                                }
                                else {
                                    status_label.setText("Not found (next): " + _previous_search.getKeyword());
                                    _previous_search = null;
                                }
                            }
                            else {
                                String keyword = field.getText();
                                if(keyword != null) {
                                    TreePath startpath = paths != null ? paths[0] : _rootpath;
                                    DefaultMutableTreeNode startnode = getChildTree(startpath);
                                    SearchResult tmp = 
                                        iSearchLoosely(startnode, startpath, keyword);
                                    if(tmp != null) {
                                        TreePath path = tmp.getResult();
                                        _tree_gui.expandPath(path);
                                        _tree_gui.clearSelection();
                                        _tree_gui.addSelectionPath(path);
                                        _tree_gui.scrollPathToVisible(path);

                                        _previous_search = tmp;
                                        System.out.println("Found: " + path);
                                    }
                                    else {
                                        status_label.setText("Not found (next): " + keyword);
                                        _previous_search = null;
                                    }
                                }
                            }
                        }
                    }
                });

            _tree_gui.addTreeSelectionListener(new TreeSelectionListener () {
                    public void valueChanged(TreeSelectionEvent e) {
                        _previous_search = null;
                    }
                });

            _tree_gui.addMouseListener(new MouseAdapter () {
                    public void mouseClicked(MouseEvent e) 
                    {
                        TreePath[] paths = _tree_gui.getSelectionPaths();
                        switch(e.getButton()) {
                            // 			case MouseEvent.BUTTON3:
                            // 			    if(paths != null) {
                            // 				System.out.println("-------------");
                            // 				for(int i = 0; i < paths.length; i++) {
                            // 				    printTree(getChildTree(paths[i]), true);	
                            // 				}
                            // 				System.out.println("-------------");
                            // 			    }
                            // 			    break;
                        case MouseEvent.BUTTON2:
                            if(paths != null) {
                                for(int i = 0; i < paths.length; i++) {
                                    printEasyRegexp(getChildTree(paths[i]), true);	
                                }
                            }
                            System.out.println("");
                            break;
                        default:
                            break;
                        }
                    }
                });
            JScrollPane scroll = new JScrollPane(_tree_gui);
            JScrollPane message_scroll = new JScrollPane(_message_area);

            scroll.setWheelScrollingEnabled(true);

            Container cont = _frame.getContentPane();
            GridBagLayout gl = new GridBagLayout();
            GridBagConstraints c = new GridBagConstraints();
	    
            cont.setLayout(gl);

            int default_ipady = c.ipady;
            int default_gridheight = c.gridheight;
            c.gridy = 0;
            c.gridx = 0;
            c.gridwidth = 1;
	    
            c.weightx = 1.0;
            c.weighty = 1.0;
            //c.ipady = 300;

            c.fill = GridBagConstraints.BOTH;
            gl.setConstraints(scroll, c);
            cont.add(scroll);
	    
            c.gridheight = default_gridheight;

            c.gridy++;

            c.weightx = 0.0;
            c.weighty = 0.0;

            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.SOUTH;
            c.ipady = field.getHeight();
            gl.setConstraints(field, c);
            cont.add(field);

            c.gridy++;

            c.anchor = GridBagConstraints.WEST;
            c.fill = GridBagConstraints.NONE;
            JLabel label = new JLabel("URL/FILENAME");
            c.ipady = label.getHeight();
            gl.setConstraints(label, c);
            cont.add(label);

            c.gridy++;

            c.fill = GridBagConstraints.HORIZONTAL;

            c.ipady = _url_field.getHeight();
            gl.setConstraints(_url_field, c);
            cont.add(_url_field);

            c.gridy++;

            c.fill = GridBagConstraints.HORIZONTAL;
            //c.gridheight = GridBagConstraints.REMAINDER;
            c.ipady = 100;
            gl.setConstraints(message_scroll, c);
            cont.add(message_scroll);

            c.gridy++;

            c.fill = GridBagConstraints.HORIZONTAL;

            c.gridheight = GridBagConstraints.REMAINDER;
            //c.anchor = GridBagConstraints.LAST_LINE_START;
            c.ipady = status_label.getHeight();
            gl.setConstraints(status_label, c);
            cont.add(status_label);

            field.addKeyListener(new KeyAdapter () {
                    public void keyPressed(KeyEvent e) 
                    {
                        String keystr = KeyEvent.getKeyText(e.getKeyCode());
                        if(keystr.equals("Enter")) {
                            String keyword = field.getText();
                            TreePath[] paths = _tree_gui.getSelectionPaths();

                            if(keyword != null) {
                                TreePath startpath = paths != null ? paths[0] : _rootpath;
                                DefaultMutableTreeNode startnode = getChildTree(startpath);
                                SearchResult tmp = 
                                    iSearchLoosely(startnode, startpath, keyword);
                                if(tmp != null) {
                                    TreePath path = tmp.getResult();
                                    _tree_gui.expandPath(path);
                                    _tree_gui.clearSelection();
                                    _tree_gui.addSelectionPath(path);
                                    _tree_gui.scrollPathToVisible(path);

                                    _previous_search = tmp;
                                    System.out.println("Found: " + path);
                                }
                                else {
                                    status_label.setText("Not found (new)" + keyword);
                                    _previous_search = null;
                                }
                            }
                        }
                        else if(isTreeBinding(e)) {
                            //tree にイベントをまわす
                            _tree_gui.dispatchEvent(e);
                        }
                    }
                });


            //URL
            _url_field.addKeyListener(new KeyAdapter () {
                    public void keyPressed(KeyEvent e) 
                    {
                        String keystr = KeyEvent.getKeyText(e.getKeyCode());
                        if(keystr.equals("Enter")) {
                            _url_string = _url_field.getText();

                            if(_url_string != null) {
                                show(_url_string);
                            }
                        }
                        else if(isTreeBinding(e)) {
                            //tree にイベントをまわす
                            _tree_gui.dispatchEvent(e);
                        }
                    }
                });

            _url_string = filename;
            show(filename);
            _frame.pack();
            //Dimension d = _frame.getSize();
            _frame.setSize(500, (int)_frame.getHeight());
            _frame.setVisible(true);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
}
