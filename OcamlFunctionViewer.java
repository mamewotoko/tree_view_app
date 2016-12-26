// OcamlFunctionViewer.java	Created      : Fri Sep  5 17:23:47 2003
//			Last modified: Mon Dec 26 08:42:52 2016
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
import javax.swing.plaf.basic.*;
import java.beans.*;

public class OcamlFunctionViewer {

    static final String[] FILTERWORDS = { "!", "^", "ref" };
    static final String DEMO_FILENAME = "ocaml_result.xml";

    static boolean _is_ok = false;
    static boolean _use_name = false;
    static final Runtime _runtime = Runtime.getRuntime();
    static final HashSet _filterwords_set = new HashSet();
    static DefaultMutableTreeNode _tree = null;
    static DefaultTreeModel _tree_model = null;
    static TreePath _rootpath = null;
    static SearchResult _previous_search = null;
    static boolean _emacs_mode = false;
    static boolean _nobar_mode = false;
    static TreePath _link_source = null;
    static Object _link_source_end = null;
    static int _number_of_tree = 2;

    static String _viewfilename = null;
    static String _xml_filename = "ocaml_result.xml";

    static Object _strong_node = null;
    static boolean _inverse_search_mode = false;
    
    static final JTextField _status_entry1 = new JTextField(" ");
    static final JTextField _status_entry2 = new JTextField(" ");

    static String _addfile_path = ".";

    static JTree[] _tree_guis = null;
    static JTextField[] _tree_names = null;

    static JScrollPane[] _scrolls = null;
    //static final JTree _tree_gui2 = new JTree();

    static JTree _current_tree = null;
    //static boolean _one_tree_mode = false;

    static JFrame _frame = null;
    static final JTextField _field = new JTextField();
    static final JTextField _current_path_entry = new JTextField("   ");
    //static final JTextField _url_field = new JTextField();
    //static final JTextPane _message_area = new JTextPane();
    static final DefaultListModel _link_model = new DefaultListModel();
    static final DefaultListModel _next_candidates_model = new DefaultListModel();
    static final DefaultListModel _inverse_candidates_model = new DefaultListModel();
    static final JList _candidates_list = new JList(_next_candidates_model);
    static final Hashtable _method_table = new Hashtable();
    private static StringBuffer _content_buffer = new StringBuffer();
    public static boolean loose_mode = true;

    public static void sendLispToEmacs(String lisp) 
    {
        String[] command = { "gnuclient", "-q", "-batch", "-eval",
                             lisp };
        try {
            Process p = _runtime.exec(command);
            p.waitFor();
            p.destroy();
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    public static JTree otherTree(JTree tree) 
    {
        int target_index = 0;
        for(target_index = 0; target_index < _tree_guis.length; target_index++) {
            if(_tree_guis[target_index] == tree)
                break;
        }
        System.out.println("Targetindex: " + target_index);
        return _tree_guis[(target_index+1)%_number_of_tree];
    }

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

    public static void moveSelection(JTree tree_gui, TreePath path)
    {
        _strong_node = path.getLastPathComponent();
        tree_gui.expandPath(path);
        tree_gui.clearSelection();
        tree_gui.addSelectionPath(path);
        tree_gui.scrollPathToVisible(path);
    }

    public static void searchAll(String keyword)
    {
        //files
        //System.out.println("searchAll called");
        _inverse_candidates_model.clear();
        _candidates_list.setModel(_inverse_candidates_model);
	
        DefaultMutableTreeNode filesnode = (DefaultMutableTreeNode)_tree;

        for(int i = 0; i < filesnode.getChildCount(); i++) {
            DefaultMutableTreeNode classnode = (DefaultMutableTreeNode)filesnode.getChildAt(i);
            for(int j = 0; j < classnode.getChildCount(); j++) {
                DefaultMutableTreeNode methodnode = (DefaultMutableTreeNode)classnode.getChildAt(j);
                for(int k = 0; k < methodnode.getChildCount(); k++) {
                    DefaultMutableTreeNode callnode = (DefaultMutableTreeNode)methodnode.getChildAt(k);
                    String callmethod = path2MethodName(((TagInfo)callnode.getUserObject()).getTag());
                    //		    System.out.println("key: " + keyword);		    //System.out.println("callmethod: " + callmethod);
                    if(callmethod.equals(keyword)) {
                        TagInfo ti = (TagInfo)methodnode.getUserObject();
                        //AttributeSet attr = ti.getAttr();
                        String typestr = "";
                        Object val = ti.getValueByName("type");
                        if(val != null) 
                            typestr = val.toString();
                        _inverse_candidates_model.addElement(new MethodPathInfo(methodnode.getPath(), typestr));
                        break;
                    }
                }
            }
        }
    }
    
    public static SearchResult nextSearch(SearchResult sr) 
    {
        String keyword = sr.getKeyword();
        Continuation _continuation = sr.getContinuation();
        return nextSearchOnContinuation(_continuation, keyword);
    }
    
    public static DefaultMutableTreeNode getSubTree(TreePath path)
    {
        DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode)(path.getLastPathComponent());
        return currentNode;
    }

    public static String path2MethodName(String pathstr)
    {
        int pos = pathstr.lastIndexOf('.');
        if(pos < 0) {
            return pathstr;
        }
        else {
            return pathstr.substring(pos+1, pathstr.length());
        }
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

    public static String patharr2string(Object patharray[])
    {
        String pathstr = "";
        for(int i = 1; i < patharray.length; i++) {
            pathstr += ((TagInfo)(((DefaultMutableTreeNode)patharray[i]).getUserObject())).getTag() + "/";
        }
        return pathstr;
    }

    //fst....
    public static String path2string(TreePath path)
    {
        String pathstr = "";
        Object[] patharray = path.getPath();
        return patharr2string(patharray);
    }

    public static TreePath string2path(String pathstr)
    {
        Vector v = new Vector();

        for(int i = 0; i < pathstr.length(); /* */) {
            int pos = pathstr.indexOf("/", i);
            if(pos < 0) 
                break;
            v.add(pathstr.substring(i, pos));
            i = pos + 1;
        }
	
        DefaultMutableTreeNode[] result = new DefaultMutableTreeNode[v.size()+1];

        result[0] = _tree;
        DefaultMutableTreeNode current_parent = _tree;

        top:
        for(int h = 0; h < v.size(); h++) {
            String current_str = (String)v.get(h);

            for(int i = 0; i < current_parent.getChildCount(); i++) {
                DefaultMutableTreeNode child = (DefaultMutableTreeNode)current_parent.getChildAt(i);
                if(current_str.equals(((TagInfo)child.getUserObject()).getTag())) {
                    result[h + 1] = child;
                    current_parent = child;
                    continue top;
                }
            }
            return null;
        }
	
        return new TreePath(result);
    }

    public static void spanLink(TreePath source, TreePath target)
    {
        //System.out.println("spanLink called: " + path2string(target));
        Object obj = ((DefaultMutableTreeNode)source.getLastPathComponent()).getUserObject();
        if(obj instanceof EditableTagInfo) {
            ((EditableTagInfo)obj).addAttribute("link", path2string(target));
        }
    }
    
    public static class TreeMouseListener extends MouseAdapter {
        JTree _tree_gui = null;
	
        public TreeMouseListener(JTree tree_gui) 
        {
            _tree_gui = tree_gui;
        }

        public void mouseClicked(MouseEvent e) 
        {
            TreePath path = _tree_gui.getSelectionPath();
            switch(e.getButton()) {
            case MouseEvent.BUTTON1:
                if(e.getClickCount() == 2) {
                    whenEnterPressed(_tree_gui);
                }
                break;
            case MouseEvent.BUTTON2:
                if(path != null) {
                    if(_link_source == null) {
                        //System.out.println("determin target");
                        //use as JTextField ....
                        _link_model.clear();
                        _link_model.addElement("Link from: " + path2string(path));
                        _candidates_list.setModel(_link_model);
                        _link_source = path;
                        _link_source_end = path.getLastPathComponent();
                    }
                    else {
                        //System.out.println("span link");
                        _link_model.addElement("to: " + path2string(path));
			
                        spanLink(_link_source, path);
                        _link_source = null;
                    }
                }
                break;
            case MouseEvent.BUTTON3:
                if(path != null) {
                    getComment((DefaultMutableTreeNode)path.getLastPathComponent());
                }
            default:
                break;
            }
        }
    }

    public static class TreeFocusListener extends FocusAdapter {
        JTree _tree_gui = null;
	
        public TreeFocusListener(JTree tree_gui) 
        {
            _tree_gui = tree_gui;
        }
	
        public void focusGained(FocusEvent e)
        {
            if(_emacs_mode && _current_tree != _tree_gui) {
                sendLispToEmacs("(other-window 1)");
            }
	    
            _current_tree = _tree_gui;
            //_previous_search = null;
        }
    }

    public static class MyTreeSelectionListener implements TreeSelectionListener 
    {
        public MyTreeSelectionListener(JTree tree_gui, JTextField status_entry)
        {
            _tree_gui = tree_gui;
            _status_entry = status_entry;
        }
	
        public void valueChanged(TreeSelectionEvent e)
        {
            _current_tree = _tree_gui;
            _next_candidates_model.clear();
			    
            TreePath path = e.getNewLeadSelectionPath();
	    
            if(path != null && path.getPathCount() > 2) {
                TagInfo ti = (TagInfo)((DefaultMutableTreeNode)path.getLastPathComponent()).getUserObject();
		
                Object linenum = ti.getValueByName("startline");
                Object filename = ti.getValueByName("filename");
                String status_str = "";
		
                if(filename != null) {
                    status_str = filename.toString();
                }
                if(linenum != null) {
                    status_str += ":" + linenum.toString() + ":";
                }
		    
                _status_entry.setText(status_str);
                _status_entry.selectAll();
                _current_path_entry.setText(path2string(path));
		
                if(_emacs_mode && filename != null && linenum != null) {
                    sendLispToEmacs("(find-file \""+ filename + "\") (goto-line " + linenum + ")");
                }
            }
        }
	
        JTree _tree_gui;
        JTextField _status_entry;
    }

    public static class TreeKeyListener extends KeyAdapter {
        JTree _tree_gui = null;
	
        public TreeKeyListener(JTree tree_gui)
        {
            _tree_gui = tree_gui;
        }

        public void keyPressed(KeyEvent e) 
        {
            TreePath path = _tree_gui.getSelectionPath();
            String keystr = KeyEvent.getKeyText(e.getKeyCode());
	    
            if(keystr.equals("Delete")) {
                if(path != null) {
                    DefaultMutableTreeNode current = (DefaultMutableTreeNode)path.getLastPathComponent();
                    DefaultMutableTreeNode next = (DefaultMutableTreeNode)current.getNextSibling();
                    if(next == null)
                        next = (DefaultMutableTreeNode)current.getPreviousSibling();
                    if(next == null)
                        next = (DefaultMutableTreeNode)current.getParent();
		    
                    if(next != null) {
                        deleteNode(current);
                        _tree_gui.addSelectionPath(new TreePath(next.getPath()));
                    }
                }
            }
            else if(keystr.equals("Escape")) {
                _link_source = null;
                _previous_search = null;
                _link_model.clear();
            }
            else if(keystr.equals("Enter")) {
                whenEnterPressed(_tree_gui);
            }
            else if(e.isControlDown() && keystr.equals("D")) {
                //System.out.println("Ctrl-D");
                if(path != null) {
                    DefaultMutableTreeNode current = (DefaultMutableTreeNode)path.getLastPathComponent();
                    Object o = current.getUserObject();
                    if(o != null && o instanceof EditableTagInfo) {
                        EditableTagInfo eti = (EditableTagInfo)o;

                        System.out.println("remove!!");
			
                        Object link = eti.getValueByName("link");
                        if(link != null) {
                            eti.removeValueByName("link");
                        }
                    }
                }
            }
            else if(e.isControlDown() && keystr.equals("L")) {
                if(_candidates_list.getModel().getSize() > 0) {
                    _candidates_list.requestFocus();
                    _candidates_list.setSelectedIndex(0);
                }
            }
            else if(e.isControlDown() && keystr.equals("C")) {
                if(path != null) {
                    getComment((DefaultMutableTreeNode)path.getLastPathComponent());
                }
            }
            else if(e.isControlDown() && keystr.equals("O")) {
                try {
                    FileOutputStream fos = new FileOutputStream(_xml_filename);
                    DefaultMutableTreeNode top = (DefaultMutableTreeNode)_tree.getChildAt(0);
                    outputXML(new PrintStream(fos));
                    outputGUI(_viewfilename);
                    fos.close();
                }
                catch(Exception ex) {
                    ex.printStackTrace();
                }
            }
            else if(e.isControlDown() && keystr.equals("I")) {
                if(path != null) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode)(path.getLastPathComponent());
                    TagInfo ti = (TagInfo)node.getUserObject();
                    String methodname = path2MethodName(ti.getTag());
                    _inverse_search_mode = true;
                    searchAll(methodname);
                }
                else {
                    System.out.println("null path");
                }
            }
            else if(e.isControlDown() && keystr.equals("E")) {
                if(path != null) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode)(path.getLastPathComponent());
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
                        TreePath result_path = tmp.getResult();
                        moveSelection(_tree_gui, result_path);
                        _previous_search = tmp;
                    }
                    else {
                        _status_entry1.setText("Not found (next): " + _previous_search.getKeyword());
                        _previous_search = null;
                    }
                }
                else {
                    String keyword = _field.getText();
                    if(keyword.length() > 0) {
                        TreePath startpath = path != null ? path : new TreePath(_tree.getPath());
                        DefaultMutableTreeNode startnode = getSubTree(startpath);
                        SearchResult tmp = 
                            iSearchLoosely(startnode, startpath, keyword);
                        if(tmp != null) {
                            TreePath result_path = tmp.getResult();
                            moveSelection(_tree_gui, result_path);
                            _previous_search = tmp;
                        }
                        else {
                            _status_entry1.setText("Not found (next): " + keyword);
                            _previous_search = null;
                        }
                    }
                    else {
                        _field.requestFocus();
                    }
                }
            }
            else if(e.isControlDown()) {
                try {
                    int num = Integer.parseInt(keystr);
                    if(num <= _number_of_tree) {
                        _tree_guis[num-1].requestFocus();
                    }
                }
                catch(NumberFormatException ex) {}
            }
        }
    }

    public static Color SELECTED_COLOR = new Color(61,247,234);
    public static Font NORMAL_FONT = new Font("DialogInput", Font.PLAIN, 12);
    public static Font LINK_SOURCE_FONT = new Font("DialogInput", Font.ITALIC, 12);
    public static Font STRONG_FONT = new Font("DialogInput", Font.BOLD, 12);

    public static class TreeRenderer extends DefaultTreeCellRenderer {
        //implements TreeCellRenderer {
        public TreeRenderer() {
            super();
        }
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) 
        {
            Component c = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

            Object obj = ((DefaultMutableTreeNode)value).getUserObject();
            if(obj instanceof EditableTagInfo) {
                Object link = ((EditableTagInfo)obj).getValueByName("link");
                if(link != null) {
                    c.setForeground(Color.RED);
                }
            }
            if(_strong_node == value) {
                c.setFont(STRONG_FONT);
            }
            else if(_link_source == value) {
                c.setFont(LINK_SOURCE_FONT);
            }
            else {
                c.setFont(NORMAL_FONT);
            }
            return c;
        }
    }
    
    public static boolean isTreeBinding(KeyEvent e)
    {
        String keystr = KeyEvent.getKeyText(e.getKeyCode());
        //!?!?!
        String[] bindstr = { "P", "O", "L", "D", "G", "F", "E", "S", "T", "1", "2", "3", "4" };
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

    public static DefaultMutableTreeNode readInputStream(InputStream is) throws Exception
    {
        return OcamlFunctionXMLParser.translate(OcamlFunctionXMLParser.parse(is), _method_table, _filterwords_set);	
    }

    private static void openFile(String filename) 
    {
        if(filename.length() == 0) 
            return;
	
        String suffix = getSuffix(filename);
        InputStream is = null;

        try {
            is = new FileInputStream(filename);
	    
            try {
                _tree = readInputStream(is);
            }
            catch(SAXParseException e) {
                is.close();
            }
            is.close();
	    
            _previous_search = null;
            _frame.setTitle(filename);
        }
        catch(Exception e) {
            _status_entry1.setText("show error: " + e.getMessage());
            e.printStackTrace();
        }

        if(_tree == null) {
            _tree = new DefaultMutableTreeNode("files");
        }
        _tree_model = new DefaultTreeModel(_tree);

        for(int i = 0; i < _tree_guis.length; i++) {
            if(_use_name)
                _tree_names[i].setText("???");
            _tree_guis[i].setModel(_tree_model);
        }

        //if(! _one_tree_mode) 
    }

    public static void deleteNode(MutableTreeNode node)
    {
        _tree_model.removeNodeFromParent(node);
    }

    private static void subOutputXML(DefaultMutableTreeNode tree, PrintStream ps, String space)
    {
        TagInfo ti = (TagInfo)tree.getUserObject();
        if(tree.isLeaf()) {
            ps.println(space + "<" + ti.toXML() + " />");
        }
        else {
            //for now no attributes
            ps.println(space + "<" + ti.toXML() + " >");
            Enumeration e = tree.children();
            while(e.hasMoreElements()) {
                DefaultMutableTreeNode child = (DefaultMutableTreeNode)e.nextElement();
                subOutputXML(child, ps, space+"  ");
            }
            ps.println(space + "</" + ti.getTag() + ">");
        }
    }

    public static void outputGUI(String filename)
    {
        ObjectOutputStream out = null;
        try {
            //Write Object
            out = new ObjectOutputStream(new FileOutputStream(filename));
            out.writeInt(_tree_guis.length);
            TreePath rootpath = new TreePath(_tree);

            out.writeBoolean(_use_name);
	    
            for(int i = 0; i < _tree_guis.length; i++) {
                Enumeration e = _tree_guis[i].getExpandedDescendants(rootpath);
                Vector v = new Vector();
                while(e.hasMoreElements()) {
                    v.add(e.nextElement());
                }

                if(_use_name)
                    out.writeUTF(_tree_names[i].getText());

                //out.writeInt(_scrolls[i].getVerticalScrollBar().getValue());
                out.writeUTF(path2string(_tree_guis[i].getSelectionPath()));

                out.writeInt(v.size());
                for(int j = 0; j < v.size(); j++) {
                    out.writeUTF(path2string((TreePath)v.get(j)));
                }
            }
            out.close();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void inputGUI(String filename)
    {
        ObjectInputStream in = null;
        try {
            //Write Object
            in = new ObjectInputStream(new FileInputStream(filename));
            _number_of_tree = in.readInt();
            boolean use_name = in.readBoolean();
            _use_name = use_name && _use_name;
	    
            for(int i = 0; i < _number_of_tree; i++) {
                if(_use_name) {
                    String name = in.readUTF();
                    _tree_names[i].setText(name);
                }
                // 		int pos = in.readInt();
                // 		_scrolls[i].getVerticalScrollBar().setValue(pos);

                String pathstr = in.readUTF();
                TreePath p = string2path(pathstr);
                int number_of_path = in.readInt();
                for(int j = 0; j < number_of_path; j++) {
                    _tree_guis[i].expandPath(string2path(in.readUTF()));
                }
                if(p != null) {
                    moveSelection(_tree_guis[i], p);
                }
            }
            in.close();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void outputXML(PrintStream ps)
    {
        ps.println("<?xml version=\"1.0\" encoding=\"euc-jp\"?>\n<files>");
        Enumeration e = _tree.children();
        while(e.hasMoreElements()) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode)e.nextElement();
            subOutputXML(child, ps, "  ");
        }
        ps.println("</files>");
    }

    public static void getComment(DefaultMutableTreeNode node) 
    {
        final DefaultMutableTreeNode target_node = node;
        final JDialog f = new JDialog(_frame, "comment", true);
        GridBagLayout gl = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
	
        c.gridy = 0;
        c.gridx = 0;
	
        final JTextPane text = new JTextPane();
	
        Object obj = (EditableTagInfo)target_node.getUserObject();
        if(obj instanceof EditableTagInfo) {
            Object comment = ((EditableTagInfo)obj).getValueByName("comment");
            if(comment != null) {
                text.setText(comment.toString());
            }
        }
	
        JScrollPane sc = new JScrollPane(text);
        JButton button = new JButton("OK");
        JButton cancel = new JButton("Cancel");
        Container cont = f.getContentPane();
	
        cont.setLayout(gl);
	
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.BOTH;
        gl.setConstraints(sc, c);
	
        cont.add(sc);
	
        c.weighty = 0.0;
        c.gridy++;
	
        c.gridwidth = 1;
        c.gridx = 0;
        c.weightx = 0.5;
        c.fill = GridBagConstraints.HORIZONTAL;
        gl.setConstraints(button, c);
        cont.add(button);

        c.gridwidth = 1;
        c.gridx = 1;
        c.weightx = 0.5;
        c.fill = GridBagConstraints.HORIZONTAL;
        gl.setConstraints(cancel, c);
        cont.add(cancel);


        text.addKeyListener(new KeyAdapter () {
                public void keyPressed(KeyEvent ke) 
                {
                    String keystr = KeyEvent.getKeyText(ke.getKeyCode());
                    if(keystr.equals("Escape")) {
                        f.hide();
                        _is_ok = false;			
                    }
                    else if(ke.isControlDown() && keystr.equals("Enter")) {
                        f.hide();
                        _is_ok = true;
                    }
                }
            });

        button.addActionListener(new ActionListener () {
                public void actionPerformed(ActionEvent ae) 
                {
                    f.hide();
                    _is_ok = true;
                }
            });

        cancel.addActionListener(new ActionListener () {
                public void actionPerformed(ActionEvent ae)
                {
                    f.hide();
                    _is_ok = false;
                }
            });

        f.pack();
        f.setSize(500, 300);
        f.setVisible(true);

        if(_is_ok) {
            String comment = text.getText();
            ((EditableTagInfo)target_node.getUserObject()).addAttribute("comment", comment);
        }
        f.dispose();
    }

    public static void whenEnterPressed(JTree tree_gui) 
    {
        _inverse_search_mode = false;
        _next_candidates_model.clear();
        _candidates_list.setModel(_next_candidates_model);
        //_current_tree = tree_gui;
	
        if(_current_tree == null)
            return;
	
        TreePath path = _current_tree.getSelectionPath();
        if(path != null) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)(path.getLastPathComponent());
            if(node.getUserObject() instanceof TagInfo) {
                TagInfo ti = (TagInfo)node.getUserObject();
                if(ti != null) {
		    
                    Object link = ti.getValueByName("link");
                    if(link != null) {
                        TreePath target_path = string2path(link.toString());
                        //System.out.println("LINKPATH: " + target_path);
                        if(target_path != null) {
                            moveSelection(otherTree(tree_gui), target_path);
                        }
                    }
		
                    String kind = ti.getTag();
		    
                    if(kind != null) {
                        if(kind.toString().equals("call")) { 
			    
                            String name = ti.getTag();
                            int pos = name.lastIndexOf('.');
                            if(pos > 0) {
                                name = name.substring(pos+1, name.length());
                            }
			    
                            Vector v = (Vector)_method_table.get(name);
			    
                            if(v != null) {
                                for(int i = 0; i < v.size(); i++) {
                                    TreeNode[] nextpath = (TreeNode[])v.get(i);
                                    _next_candidates_model.addElement(new TreePath(path));
                                }
                                // to be modified
                                if(v.size() == 1 && node.isLeaf()) {
                                    TreeNode[] nextpath = (TreeNode[])v.get(0);
                                    DefaultMutableTreeNode subtree = (DefaultMutableTreeNode)nextpath[nextpath.length-1];
                                    Enumeration e = subtree.children();
				    
                                    while(e.hasMoreElements()) {
                                        DefaultMutableTreeNode child = (DefaultMutableTreeNode)((DefaultMutableTreeNode)e.nextElement()).clone();
                                        _tree_model.insertNodeInto(child, node, node.getChildCount());
                                    }
                                }
                            }
                            else {
                                _status_entry1.setText("Not found (" + node.toString() + ")");
                            }
                        }
                    }
                }
            }
        }
    }
    
    public static void whenCandidateDetermined() 
    {
        System.out.println("Not implemented");
    }
    // 	TreePath path = ((MethodPathInfo)_candidates_list.getSelectedValue()).getPath();
    // 	if(path != null && _current_tree != null) {
	    
    // 	    if(_inverse_search_mode) {
    // 		moveSelection(_current_tree, path);
    // 	    }
    // 	    else {
    // 		//JTree
    // 		TreePath target_path = _current_tree.getSelectionPath();
    // 		if(target_path != null) {
    // 		    DefaultMutableTreeNode target_node = (DefaultMutableTreeNode)target_path.getLastPathComponent();
    // 		    if(target_node.isLeaf()) {
    // 			DefaultMutableTreeNode methodnode = (DefaultMutableTreeNode)path.getLastPathComponent();
						
    // 			Enumeration enum = methodnode.children();
    // 			while(enum.hasMoreElements()) {
    // 			    DefaultMutableTreeNode child = (DefaultMutableTreeNode)((DefaultMutableTreeNode)enum.nextElement()).clone();
    // 			    _tree_model.insertNodeInto(child, target_node, target_node.getChildCount());
    // 			}
    // 			if(target_node.isLeaf()) {
    // 			    EditableTagInfo eti = (EditableTagInfo)methodnode.getUserObject();
    // 			    DefaultMutableTreeNode child = new DefaultMutableTreeNode(new EditableTagInfo("NONE", eti.getAttr()));
    // 			    _tree_model.insertNodeInto(child, target_node, target_node.getChildCount());
    // 			}
    // 		    }
    // 		}
    // 	    }
    // 	    _current_tree.requestFocus();
    // 	}
    //     }

	public static void main(String[] args) 
    {
        int framewidth = 500;
        int frameheight = 700;
        int argindex = 0;
        boolean read_viewfile = false;
	
        for(argindex = 0; argindex < args.length; argindex++) {
            // 	    if(args[argindex].equals("-emacs")) {
            // 		_emacs_mode = true;
            // 	    }
            if(args[argindex].equals("-nobar")) {
                _nobar_mode = true;
            }
            else if(args[argindex].equals("-4")) {
                _number_of_tree = 4;
            }
            else if(args[argindex].equals("-name")) {
                _use_name = true;
            }
            else if(args[argindex].equals("-width")) {
                if(++argindex < args.length) {
                    framewidth = Integer.parseInt(args[argindex]);
                }
                else {
                    System.err.println("Option error!!!");
                    System.exit(1);
                }
            }
            else if(args[argindex].equals("-height")) {
                if(++argindex < args.length) {
                    frameheight = Integer.parseInt(args[argindex]);
                }
                else {
                    System.err.println("Option error!!!");
                    System.exit(1);
                }
            }
            else if(args[argindex].equals("-viewfile")) {
                //read_viewfile = true;
                if(++argindex < args.length) {
                    _viewfilename = args[argindex];
                }
                else {
                    System.err.println("Option error!!!");
                    System.exit(1);
                }
            }
            else {
                break;
            }
        }

        _tree_guis = new JTree[_number_of_tree];
        if(_use_name)
            _tree_names = new JTextField[_number_of_tree];
	
        _scrolls = new JScrollPane[_number_of_tree];
	
        for(int i = 0; i < FILTERWORDS.length; i++) {
            _filterwords_set.add(FILTERWORDS[i]);
        }
	
        try {
            //JFrame.setDefaultLookAndFeelDecorated(true);
            //JDialog.setDefaultLookAndFeelDecorated(true);

            _frame = new JFrame();


            //if(! _one_tree_mode)
            _frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            JScrollPane message_scroll = new JScrollPane(_candidates_list);

            Container cont = _frame.getContentPane();
            GridBagLayout gl = new GridBagLayout();
            GridBagConstraints c = new GridBagConstraints();
	    
            cont.setLayout(gl);

            int default_ipady = c.ipady;
            int default_gridheight = c.gridheight;
            c.gridy = 0;
            c.gridx = 0;

            c.gridwidth = 1;
            c.weightx = 1.0/_number_of_tree;

            int treey = _use_name ? 1 : 0;
	    
            for(int i = 0; i < _tree_guis.length; i++) {
                if(_use_name) {
                    _tree_names[i] = new JTextField("???");
                    _tree_names[i].setEditable(true);

                    c.gridy = 0;
                    c.weighty = 0.0;
                    c.fill = GridBagConstraints.HORIZONTAL;
                    c.ipady = _tree_names[i].getHeight();

                    gl.setConstraints(_tree_names[i], c);
                    cont.add(_tree_names[i]);
                    c.gridy = 1;
                }

                c.weighty = 1.0;
                c.gridy = treey;
                c.fill = GridBagConstraints.BOTH;
		
                _tree_guis[i] = new JTree();
                _tree_guis[i].getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
                _tree_guis[i].setCellRenderer(new TreeRenderer());
                _tree_guis[i].getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
                _tree_guis[i].addKeyListener(new TreeKeyListener(_tree_guis[i]));
                _tree_guis[i].addTreeSelectionListener(new MyTreeSelectionListener(_tree_guis[i], _status_entry1));
                _tree_guis[i].addMouseListener(new TreeMouseListener(_tree_guis[i]));
                _tree_guis[i].addFocusListener(new TreeFocusListener(_tree_guis[i]));
                _scrolls[i] = new JScrollPane(_tree_guis[i]);
                _scrolls[i].setWheelScrollingEnabled(true);
		
                gl.setConstraints(_scrolls[i], c);
                cont.add(_scrolls[i]);
                c.gridx++;
            }

            c.gridy = treey + 1;

            c.gridwidth = _number_of_tree;
	
            c.gridy++;
            c.gridx = 0;
            //c.weightx = 0.4;
            c.weighty = 0.0;
            c.gridheight = default_gridheight;

            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.SOUTH;
            c.ipady = _field.getHeight();
            gl.setConstraints(_field, c);
            cont.add(_field);

            c.gridy++;

            c.fill = GridBagConstraints.HORIZONTAL;
            c.ipady = _current_path_entry.getHeight();
            gl.setConstraints(_current_path_entry, c);
            cont.add(_current_path_entry);
            _current_path_entry.setEditable(false);
            _current_path_entry.setBackground(_frame.getBackground());

            c.gridy++;

            //JList
            DefaultListSelectionModel dlsm = new DefaultListSelectionModel();
            dlsm.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            _candidates_list.setSelectionModel(dlsm);
            _candidates_list.setVisibleRowCount(2);
            c.fill = GridBagConstraints.HORIZONTAL;
            //message_scroll.setSize(500, 20);
            c.ipady = _candidates_list.getHeight();

            gl.setConstraints(message_scroll, c);
            cont.add(message_scroll);

            c.gridy++;
            c.fill = GridBagConstraints.HORIZONTAL;

            if(! _nobar_mode) {
                //c.gridheight = GridBagConstraints.REMAINDER;
                //c.anchor = GridBagConstraints.LAST_LINE_START;
                c.ipady = _status_entry1.getHeight();
                _status_entry1.setEditable(false);
                _status_entry1.setBackground(Color.WHITE);
                gl.setConstraints(_status_entry1, c);
                cont.add(_status_entry1);
		
                c.gridy++;
                c.fill = GridBagConstraints.HORIZONTAL;
                //c.ipady = _status_entry.getHeight();
                _status_entry2.setEditable(false);
                _status_entry2.setBackground(Color.WHITE);
                gl.setConstraints(_status_entry2, c);
                cont.add(_status_entry2);
            }
		
            _field.addKeyListener(new KeyAdapter () {
                    public void keyPressed(KeyEvent e) 
                    {
                        String keystr = KeyEvent.getKeyText(e.getKeyCode());
                        if(keystr.equals("Enter") && _current_tree != null) {
                            //System.out.println("search");
                            String keyword = _field.getText();
                            TreePath path = _current_tree.getSelectionPath();

                            if(keyword != null) {
                                TreePath startpath = path != null ? path : new TreePath(_tree.getPath());
                                DefaultMutableTreeNode startnode = getSubTree(startpath);
                                SearchResult tmp = 
                                    iSearchLoosely(startnode, startpath, keyword);
                                if(tmp != null) {
                                    TreePath result_path = tmp.getResult();
                                    moveSelection(_current_tree, result_path);
                                    _previous_search = tmp;
                                }
                                else {
                                    _status_entry1.setText("Not found (new)" + keyword);
                                    _previous_search = null;
                                }
                            }
                        }
                        else if(isTreeBinding(e)) {
                            //tree にイベントをまわす
                            _current_tree.dispatchEvent(e);
                        }
                    }
                });

            //candidate list
            _candidates_list.addMouseListener(new MouseAdapter() {
                    public void mousePressed(MouseEvent e)
                    {
                        if(e.getClickCount() == 2) {
                            whenCandidateDetermined();
                        }
                    }
                });

            _candidates_list.addKeyListener(new KeyAdapter() {
                    public void keyPressed(KeyEvent e)
                    {
                        String keystr = KeyEvent.getKeyText(e.getKeyCode());
                        if(keystr.equals("Enter")) {
                            whenCandidateDetermined();
                        }
                        else if(isTreeBinding(e)) {
                            _current_tree.dispatchEvent(e);
                        }
                    }
                });

            JMenuBar menubar = new JMenuBar();
            final JMenu menu = new JMenu("File");
            menubar.add(menu);
            final JMenuItem openitem = menu.add("Open");
            final JMenuItem saveitem = menu.add("Save");
            final JMenuItem loaditem = menu.add("Load");
            final JMenuItem demoitem = menu.add("Demo");
	    
            openitem.addMouseListener(new MouseAdapter() {
                    public void mousePressed(MouseEvent e) 
                    {
                        //menu.setVisible(false);
                        JFileChooser filechooser = new JFileChooser(".");
                        //ファイルを選択してくれるまでまつ
                        filechooser.showOpenDialog(_frame);
			
                        File f = filechooser.getSelectedFile();

                        //filechooser.dispose();
			
                        if(f != null) 
                            openFile(f.getAbsolutePath());
                        if(_tree == null) {
                            _tree = new DefaultMutableTreeNode("files");
                        }
                        _tree_model = new DefaultTreeModel(_tree);
                    }
                });

            saveitem.addMouseListener(new MouseAdapter() {
                    public void mousePressed(MouseEvent e) 
                    {
                        outputGUI(_viewfilename);
                    }
                });

            loaditem.addMouseListener(new MouseAdapter() {
                    public void mousePressed(MouseEvent e) 
                    {
                        inputGUI(_viewfilename);
                    }
                });

            demoitem.addMouseListener(new MouseAdapter() {
                    public void mousePressed(MouseEvent e) 
                    {
                        String filename = DEMO_FILENAME;

                        try {
                            URL u = OcamlFunctionViewer.class.getClassLoader().getResource(filename);
                            URLConnection c = u.openConnection();
                            c.connect();
                            InputStream is = c.getInputStream();
                            _tree = readInputStream(is);
                            is.close();
                        }
                        catch(SAXParseException ex) {
                            ex.printStackTrace();
                        }
                        catch(Exception ex) {
                            ex.printStackTrace();
                        }

                        if(_tree == null) {
                            _tree = new DefaultMutableTreeNode("files");
                        }
                        _tree_model = new DefaultTreeModel(_tree);

                        for(int i = 0; i < _tree_guis.length; i++) {
                            _tree_guis[i].setModel(_tree_model);
                        }
                    }
                });
            // ClassLoader.getResource -> URL
            // open ... ?

            _frame.setJMenuBar(menubar);

            if(argindex < args.length && getSuffix(args[argindex]).equals(".xml"))
                _xml_filename = args[argindex];
            else
                _xml_filename = "RESULT.xml";
	       
            for( /* nothing */ ; argindex < args.length; argindex++) {
                System.out.println("parsing... " + args[argindex]);
                openFile(args[argindex]);
            }
	    
            if(_tree == null) {
                _tree = new DefaultMutableTreeNode("files");
                _tree_model = new DefaultTreeModel(_tree);
                for(int i = 0; i < _tree_guis.length; i++) {
                    _tree_guis[i].setModel(_tree_model);
                }
            }

            //JTree
            DefaultMutableTreeNode[] rootpatharr = { _tree };

            //dummy
            TreePath rootpath = new TreePath(rootpatharr);

            if(_viewfilename != null)
                inputGUI(_viewfilename);
            else
                _viewfilename = "tree.dat";

            for(int i = 0; i < _tree_guis.length; i++) {
                moveSelection(_tree_guis[i], rootpath);
            }
            _frame.pack();
            _frame.setSize(framewidth, frameheight);
	    
            _frame.setVisible(true);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
}
