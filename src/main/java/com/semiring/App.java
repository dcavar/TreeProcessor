/* App.java
 *
 * Copyright 2018 by Damir Cavar <damir@semiring.com>
 *
 * This application processes syntactic trees from parser outputs
 * or tree banks. It extracts rules, their frequencies, as well
 * as tree internal properties like dominance, c-command,
 * government, linear precedence, etc.
 * 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @version 0.1, 08/04/18
 * @author Damir Cavar
 */

package com.semiring;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.StringJoiner;
import java.util.Map.Entry;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.ini4j.Ini;
import org.ini4j.IniPreferences;

/**
 * Main App
 * 
 * @author Damir Cavar
 *
 */
public class App {
	public static final String MODNAME = "TreeProcessor";
	private static final Logger log = Logger.getLogger(MODNAME);
	private static final String CONFIGFILE = "config.ini";
	private static final String DEFAULTREES = "trees.txt";
	private Preferences prefs = null;
	private int port = 9099;
	private Grammar grammar = null;

	/**
	 * Read the config.ini file.
	 *
	 * @param filename
	 */
	public void readInit(String filename) {
		try {
			prefs = new IniPreferences(new Ini(new File(filename)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (prefs != null) {
			try {
				if (prefs.nodeExists(MODNAME)) {
					port = Integer.valueOf(prefs.node(MODNAME).get("port", "9099"));
				}
			} catch (BackingStoreException ex) {
				ex.printStackTrace();
			}
		}
	}

	/**
	 *
	 */
	public void loadDefaultTrees() {
		loadTrees(App.class.getClassLoader().getResourceAsStream(DEFAULTREES));
	}

	public void loadTrees(InputStream stream) {
		try {
			LineIterator it = IOUtils.lineIterator(stream, StandardCharsets.UTF_8);
			while (it.hasNext()) {
				String line = it.nextLine().trim();
				if (line.length() > 0) {
					if (grammar == null)
						grammar = new Grammar();
					grammar.processTree(line);
				}
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			IOUtils.closeQuietly(stream);
		}
	}

	public static void main(String[] args) {
		App myApp = new App();
		Comparse myCLI = new Comparse(args);
		myCLI.parse();
		// is there a command line argument for a specific trees file?
		if (myCLI.treesFile.length() > 0) {
			File treesFile = new File(myCLI.treesFile);
			if (treesFile.exists()) {
				try {
					InputStream targetStream = FileUtils.openInputStream(treesFile);
					myApp.loadTrees(targetStream);
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}
		// running in test mode?
		if (myCLI.testMode) {
			if (myCLI.treesFile.length() == 0)
				myApp.loadDefaultTrees();
			for (Tree t : myApp.grammar.trees) {
				System.out.println("---------- Tree ----------");
				System.out.println(t.getTreeString());
				System.out.println("---------- Terminals ----------");
				StringJoiner joiner = new StringJoiner(", ");
				for (String x : t.getTerminals()) {
					joiner.add(x);
				}
				System.out.println(joiner.toString());
				System.out.println("---------- Dominates Relations ----------");
				System.out.println("Number of Dominance relations: " + t.getDominators().length);
				System.out.println("---------- C-Command Relations ----------");
				for (int x : t.getCCommanders()) {
					joiner = new StringJoiner(", ");
					for (int s : t.getCCommended(x)) {
						joiner.add(t.getSymbol4NodeID(s, true));
					}
					System.out.println(t.getSymbol4NodeID(x, true) + " c-commands: " + joiner.toString());
				}
				System.out.println("---------- CFG ----------");
				System.out.println(t.getCFG(true));
			}
		}
	}

}
