// FormFrame.java	Created      : Sun Sep 14 01:39:34 2003
//			Last modified: Mon Dec 26 08:25:38 2016
// Compile: javac FormFrame.java #
// Execute: java FormFrame #
// FTP Directory: sources/java #
//------------------------------------------------------------
// InputInfo, SelectInputInfo のことを知っている

import java.io.*;
import java.util.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import javax.swing.text.*;

public class FormFrame extends JFrame {
    private FormInfo _forminfo = null;
    private Hashtable _gui_table = new Hashtable();
    private Hashtable _list_table = new Hashtable();

    public static class SelectItem {
	String _name;
	String _repr;

	public SelectItem(String name, String repr) 
	{
	    _name = name;
	    _repr = repr;
	}

	public String toString()
	{
	    return _repr;
	}

	public String getName() 
	{
	    return _name;
	}
    }

    public FormFrame (FormInfo forminfo, String url_string) 
    {
	super();
	_forminfo = forminfo;

	Container cont = this.getContentPane();
	GridBagLayout gl = new GridBagLayout();
	GridBagConstraints c = new GridBagConstraints();
	cont.setLayout(gl);
	int default_ipady = c.ipady;
	Vector submits = new Vector();
	Vector select = new Vector();
	Vector hiddens = new Vector();
	
	Hashtable inputinfo = forminfo.getInput();
	Enumeration keys = inputinfo.keys();
	JLabel label = null;
	JTextField field = null;
	JScrollPane scroll = null;
	JButton button = null;

	c.weightx = 0.9;
	c.fill = GridBagConstraints.HORIZONTAL;
	
	while(keys.hasMoreElements()) {
	    Object key = keys.nextElement();
	    InputInfo value = (InputInfo)inputinfo.get(key);

	    switch(value.getType()) {
	    case InputInfo.HIDDEN:
		hiddens.add(value);
		break;
	    case InputInfo.SUBMIT:
	    case InputInfo.BUTTON:
		submits.add(value);
		break;
	    case InputInfo.SELECT:
		select.add(value);
		break;
	    default:
		break;
	    }
	}

	c.gridy = 0;

//------------------------------------------------------

	//action & method
	String method = forminfo.getMethod();
	if(method == null)
		method = "GET";
    c.gridx = 0;
    c.gridwidth = 1;
    label = new JLabel("method");
    gl.setConstraints(label, c);
    cont.add(label);

    c.gridx = 1;
    c.gridwidth = 1;
    field = new JTextField(method);
    gl.setConstraints(field, c);
    cont.add(field);
    c.gridy++;

//------------------------------------------------------
	String action = forminfo.getAction();
	if(method == null)
		action = "";
    c.gridx = 0;
    c.gridwidth = 1;
    label = new JLabel("action");
    gl.setConstraints(label, c);
    cont.add(label);

    c.gridx = 1;
    c.gridwidth = 1;
    field = new JTextField(action);
    gl.setConstraints(field, c);
    cont.add(field);
    c.gridy++;
	
//------------------------------------------------------

	for(int i = 0; i < hiddens.size(); i++) {
	    InputInfo ii = (InputInfo)hiddens.get(i);
	    c.gridx = 0;
	    c.gridwidth = 1;
	    label = new JLabel(ii.getID());
	    gl.setConstraints(label, c);
	    cont.add(label);

	    c.gridx = 1;
	    c.gridwidth = 1;
	    label = new JLabel((String)ii.getValue());
	    gl.setConstraints(label, c);
	    cont.add(label);
	    c.gridy++;
	}

	keys = inputinfo.keys();
	String id = null;
	
	while(keys.hasMoreElements()) {
	    Object key = keys.nextElement();
	    InputInfo value = (InputInfo)inputinfo.get(key);
	    switch(value.getType()) {
	    case InputInfo.TEXT:
		id = value.getID();
		label = new JLabel(id);
		field = new JTextField(value.getValue());
		_gui_table.put(id, field);

		c.gridx = 0;
		gl.setConstraints(label, c);
		cont.add(label);
		c.gridx = 1;
		gl.setConstraints(field, c);
		cont.add(field);
		c.gridy++;		
		break;
	    case InputInfo.TEXTAREA:
		id = value.getID();
		label = new JLabel(id);
		c.gridx = 0;
		c.gridwidth = 1;
		gl.setConstraints(label, c);
		cont.add(label);
		c.gridy++;
		c.gridwidth = 2;
		c.ipady = 150;
		JEditorPane txt = new JEditorPane();
		scroll = new JScrollPane(txt);
		_gui_table.put(id, txt);
		gl.setConstraints(scroll, c);
		cont.add(scroll);
		c.gridy++;
		c.ipady = default_ipady;
		break;
	    case InputInfo.SELECT:
		id = value.getID();
		Vector option_item = new Vector();
		Vector option_item_vector = ((SelectInputInfo)value).getOptionValue();
		JComboBox cb = new JComboBox(option_item_vector);
		_list_table.put(id, cb);

		label = new JLabel(id);
		c.gridx = 0;
		gl.setConstraints(label, c);
		cont.add(label);
		c.gridx = 1;
		gl.setConstraints(cb, c);
		cont.add(cb);
		c.gridy++;		
		break;
	    default:
		break;
	    }
	}

	//--
	//select

	ActionListener kl = new ActionListener() {
		public void actionPerformed(ActionEvent e)
		{
		    Enumeration keys = _gui_table.keys();
		    while(keys.hasMoreElements()) {
			String key = (String)keys.nextElement();
			JTextComponent tc = (JTextComponent)_gui_table.get(key);
			_forminfo.setValue(key, tc.getText());
		    }
		    
		    keys = _list_table.keys();
		    while(keys.hasMoreElements()) {
			String key = (String)keys.nextElement();
			JComboBox cb = (JComboBox)_list_table.get(key);
			_forminfo.setValue(key, ((SelectItem)cb.getSelectedItem()).getName());
		    }
		    
		    InputStreamSource iss = _forminfo.getInputStreamSource();
		    if(iss != null) {
			InputStream is = iss.getInputStream();
			if(is != null) {
			    HTMLTree.showHTML(is);
			    iss.afterInput();
			    
			    //debug
			    is = iss.getInputStream();
			    try {
				BufferedReader br = new BufferedReader(new InputStreamReader(is, "JISAutoDetect"));
				String line = null;
				System.out.println("Get Contents");
				while((line = br.readLine()) != null) {
				    System.out.println(line);
				}
			    }
			    catch(IOException ex) { }
			    iss.afterInput();
			}
			else 
			    System.out.println("InputStream is NULL");
			
		    }
		}
	    };

	for(int i = 0; i < submits.size(); i++) {
	    InputInfo ii = (InputInfo)submits.get(i);
	    c.gridx = 0;
	    c.gridwidth = 2;
	    button = new JButton(ii.getValue());
	    button.addActionListener(kl);
	    //
	    gl.setConstraints(button, c);
	    cont.add(button);
	    c.gridy++;
	}
	this.pack();
    }
}
