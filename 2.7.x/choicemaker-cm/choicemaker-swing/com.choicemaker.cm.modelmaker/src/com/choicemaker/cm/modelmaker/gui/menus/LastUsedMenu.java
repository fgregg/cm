/*
 * Copyright (c) 2001, 2009 ChoiceMaker Technologies, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License
 * v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     ChoiceMaker Technologies, Inc. - initial API and implementation
 */
package com.choicemaker.cm.modelmaker.gui.menus;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.apache.log4j.Logger;

import com.choicemaker.cm.core.util.LoggingObject;
import com.choicemaker.cm.core.xmlconf.XmlConfigurator;
import com.choicemaker.cm.modelmaker.gui.ModelMaker;

/**
 * Provides a menu that displays at the end a list of least recently
 * used files.
 * 
 * @author  Martin Buechi
 * @version $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:03:09 $
 */
public abstract class LastUsedMenu extends JMenu {
	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(LastUsedMenu.class);

	private int maxNumItems;
	private List items;
	private ActionListener listener;
	private String key;

	/**
	 * Creates an instance.
	 * Usage:
	 * <ul>
	 *   <li>Call <code>addAutoItems()</code> after adding all other menu items.</li>
	 *   <li>Call <code>opened()</code> for each file that has been opened.</li>
	 *   <li>Implement <code>open()</code> to open files selected from auto-generated
	 *     menu items.</li>
	 * </ul>
	 * 
	 * @param   name  The name of the menu.
	 * @param   key The key for storing the state between program invocations in the preferences.
	 * @param   maxNumItems  The maximum number of items to display.
	 */
	public LastUsedMenu(String name, String key, int maxNumItems) {
		super(name);
		this.key = XmlConfigurator.getFileName() + ": " + key;
		if(this.key.length() > Preferences.MAX_KEY_LENGTH) {
			this.key = this.key.substring(this.key.length() - Preferences.MAX_KEY_LENGTH);
		}
		this.maxNumItems = maxNumItems;
		items = new ArrayList(maxNumItems);
		listener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MenuItem m = (MenuItem) e.getSource();
				open(m.fileName);
			}
		};
	}

	/**
	 * Adds the auto generated menu items. To be called after adding all other
	 * menu items manually.
	 */
	public void addAutoItems() {
		addSeparator();
		loadItems();
		addItems();
	}

	private void addItems() {
		int i = 0;
		int numItems = items.size();
		while (i < numItems) {
			add((MenuItem) items.get(i));
			++i;
		}
	}

	private void removeItems() {
		int i = 0;
		int numItems = items.size();
		while (i < numItems) {
			remove((MenuItem) items.get(i));
			++i;
		}
	}

	/**
	 * Removes an auto-generated menu item.
	 *
	 * @param   fileName  The item to remove.
	 */
	public void remove(String fileName) {
		int i = 0;
		int numItems = items.size();
		while (i < numItems && fileName != ((MenuItem) items.get(i)).fileName) {
			++i;
		}
		if (i < numItems) {
			saveItems();
			removeItems();
			items.remove(i);
			addItems();
		}
	}

	/**
	 * Notifies the menu that this file has been opened and should
	 * be placed on top of the least recently used files.
	 *
	 * @param   fileName  The name of the file opened.
	 */
	public void opened(String fileName) {
		fileName = fileName.intern();
		int i = 0;
		int numItems = items.size();
		while (i < numItems && fileName != ((MenuItem) items.get(i)).fileName) {
			++i;
		}
		if (i < numItems) {
			if (i != 0) {
				removeItems();
				items.add(0, items.remove(i));
				addItems();
				saveItems();
			}
		} else {
			removeItems();
			if (items.size() == maxNumItems) {
				items.remove(maxNumItems - 1);
			}
			MenuItem m = new MenuItem(fileName);
			m.addActionListener(listener);
			items.add(0, m);
			addItems();
			saveItems();
		}
	}

	/**
	 * Called when a auto-generated menu items for a recently
	 * used file is selected.
	 *
	 * @param   fileName  The name of the file.
	 */
	public abstract void open(String fileName);

	private static class MenuItem extends JMenuItem {
		private static final long serialVersionUID = 1L;
		String fileName;

		MenuItem(String fileName) {
			super(fileName);
			this.fileName = fileName.intern();
		}
	}

	private void saveItems() {
		try {
			Preferences prefs = ModelMaker.getPreferences();
			int numItems = items.size();
			String buf[] = new String[numItems];
			for (int i = 0; i < numItems; ++i) {
				buf[i] = ((MenuItem) items.get(i)).fileName;
			}
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(buf);
			oos.flush();
			prefs.putByteArray(key, bos.toByteArray());
		} catch (IOException ex) {
			logger.error(new LoggingObject("CM-100201"), ex);
		}
	}

	private void loadItems() {
		try {
			Preferences prefs = ModelMaker.getPreferences();
			byte[] res = prefs.getByteArray(key, new byte[0]);
			if (res.length > 0) {
				ByteArrayInputStream bis = new ByteArrayInputStream(res);
				String[] buf = (String[]) new ObjectInputStream(bis).readObject();
				for (int i = 0; i < buf.length; ++i) {
					MenuItem m = new MenuItem(buf[i]);
					m.addActionListener(listener);
					items.add(m);
				}
			}
		} catch (IOException ex) {
			logger.error(new LoggingObject("CM-100202"), ex);			
		} catch (ClassNotFoundException ex) {
			logger.error(new LoggingObject("CM-100202"), ex);
		}
	}
}
