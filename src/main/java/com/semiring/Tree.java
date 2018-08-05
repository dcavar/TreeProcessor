/*
 * Tree.java
 * 
 * Copyright 2018 by Damir Cavar <damir@semiring.com>
 *
 * This is the tree parsing and representation class
 * for TreeProcessor.
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

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.Map.Entry;

enum State {
	NONE, WAITFORLHS, LHS, WAITFORRHS, RHS;
}

/**
 * 
 * @author Damir Cavar
 *
 */
public class Tree {

	// static class variables and methods

	private static Map<String, Integer> symbol2int = new HashMap<>();
	private static Map<Integer, String> int2symbol = new HashMap<>();
	private static int countSymbols = 0;

	/**
	 * Return an integer ID for a string representation of a symbol. This
	 * replacement is used to reduce memory and increase processing speed when
	 * processing large grammars.
	 * 
	 * @param symbol
	 * @return
	 */
	private static int getID4Symbol(String symbol) {
		if (symbol2int.containsKey(symbol)) {
			return symbol2int.get(symbol);
		}
		countSymbols += 1;
		symbol2int.put(symbol, countSymbols);
		int2symbol.put(countSymbols, symbol);
		return countSymbols;
	}

	/**
	 * Return the string representation of a symbol with an integer ID. This
	 * replacement is used to reduce memory and increase processing speed when
	 * processing large grammars.
	 * 
	 * @param n
	 * @return
	 */
	private static String getSymbol4ID(int n) {
		if (int2symbol.containsKey(n)) {
			return int2symbol.get(n);
		}
		return null;
	}

	// instance level variables and methods

	// rules collected in Map, key is level
	// value is list of Lists with integers: Symbol-ID - List of Symbol-IDs
	private Map<Integer, List<List<Integer>>> rules;
	// for every symbol ID the value is its symbol string ID
	private Map<Integer, Integer> symbolref;
	// dominance relation
	private Map<Integer, Set<Integer>> dominates;
	// c-command relation
	private Map<Integer, Set<Integer>> ccommands;
	// precedes relation
	private Map<Integer, Set<Integer>> precedes;
	// is terminal
	private Set<Integer> terminals;
	
	Tree() {
		this.rules = new HashMap<>();
		this.symbolref = new HashMap<>();
		this.dominates = new HashMap<>();
		this.ccommands = new HashMap<>();
		this.precedes = new HashMap<>();
		this.terminals = new HashSet<>();
	}

	/**
	 * 
	 * @param tree
	 */
	public void parseTree(String tree) {
		State state = State.NONE;
		StringBuffer sb = new StringBuffer();
		int level = 0; // keep track of the embedding level or tree depth
		int n; // helper variable for integer ID of a symbol string
		String symbol; // helper variable for the symbol string
		List<List<Integer>> eList;
		
		CharacterIterator it = new StringCharacterIterator(tree);
		int treeID = 0;
		while (it.current() != CharacterIterator.DONE) {
			if (it.current() == '(') {
				state = State.WAITFORLHS;
				level += 1;
			} else if (it.current() == ')') {
				switch (state) {
				case RHS:
					symbol = sb.toString();
					treeID += 1;
					n = getID4Symbol(symbol);

					symbolref.put(treeID, n);
					eList = rules.get(level);
					List<Integer> lastList = eList.get(eList.size() - 1);
					lastList.add(treeID);
					// add to terminals
					terminals.add(treeID);

					sb.delete(0, sb.length());
					break;
				default:
					break;
				}
				state = State.NONE;
				level -= 1;
			} else if (Character.isWhitespace(it.current())) {
				switch (state) {
				case LHS:
					symbol = sb.toString(); // get the symbol from StringBuffer
					treeID += 1; // create a new unique node ID for the symbol
					n = getID4Symbol(symbol); // get a string ID for a symbol
					symbolref.put(treeID, n); // remember the unique node ID and the corresponding string ID

					// append the current LHS to current level
					ArrayList<Integer> newlhs = new ArrayList<Integer>(); // create a new LHS List
					newlhs.add(treeID); // append as the first unique ID the LHS symbol
					if (rules.containsKey(level)) { // if there is a rule collection for the level
						eList = rules.get(level);
						eList.add(newlhs); // append to it the new LHS list
						rules.put(level, eList); // push to HashMap
					} else { // there is no rule collection for the level yet
						eList = new ArrayList<List<Integer>>(); // create a new rule list
						eList.add(newlhs); // append to it the new LHS list
						rules.put(level, eList); // push to HashMap
					}
					if (level > 1) { // add symbolID to last LHS list of RHS symbols in previous level, not for ROOT
						eList = rules.get(level - 1);
						List<Integer> lastList = eList.get(eList.size() - 1);
						lastList.add(treeID);
						rules.put(level - 1, eList);
					}

					sb.delete(0, sb.length());
					state = State.WAITFORRHS;
					break;
				case RHS:
					symbol = sb.toString();
					treeID += 1;
					n = getID4Symbol(symbol);

					symbolref.put(treeID, n);
					eList = rules.get(level);
					List<Integer> lastList = eList.get(eList.size() - 1);
					lastList.add(treeID);
					// add to terminals
					terminals.add(treeID);

					sb.delete(0, sb.length());
					state = State.WAITFORRHS;
					break;
				default:
					break;
				}
			} else { // this must be a character of sorts
				sb.append(it.current());
				switch (state) {
				case WAITFORLHS:
					state = State.LHS;
					break;
				case WAITFORRHS:
					state = State.RHS;
					break;
				default:
					break;
				}
			}
			it.next();
		}
	}

	
	/**
	 * Returns true, if x is in the scope of y.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean isInScope(int x, int y) {
		return true;
	}
	
	/**
	 * Returns true, if x dominates y.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean dominates(int x, int y) {
		return true;
	}

	/**
	 * Returns true, if x c-commands y.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean cCommands(int x, int y) {
		return true;
	}

	/**
	 * Returns true, if x precedes y.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean precedes(int x, int y) {
		return true;
	}

	/**
	 * 
	 * @return
	 */
	public String[] getTerminals() {
		List<String> res = new ArrayList<String>();
		for (int x : terminals) {
			res.add(int2symbol.get(symbolref.get(x)));
		}
		String[] rArray = new String[ res.size() ];
		res.toArray(rArray);
		return rArray;
	}
	
	/**
	 * 
	 * @param skipTerminals
	 * @return
	 */
	public String getCFG(boolean skipTerminals) {
		StringBuffer sb = new StringBuffer();
		for (Entry<Integer, List<List<Integer>>> pair : rules.entrySet()) {
			List<List<Integer>> v = pair.getValue();
			for (int i = 0; i < v.size(); i++) {
				List<Integer> r = v.get(i);
				if (skipTerminals) { // skip terminal rules
					if (r.size() == 2) {
						if (terminals.contains(r.get(1))) {
							continue;
						}
					}
				}
				StringJoiner joiner = new StringJoiner(" ");
				for (int x = 1; x < r.size(); x++) {
					joiner.add(int2symbol.get(symbolref.get(r.get(x))));
				}
				sb.append(int2symbol.get(symbolref.get(r.get(0))) + " --> " + joiner.toString() + System.lineSeparator());
			}
		}
		return sb.toString();		
	}
	
	/**
	 * Returns a string with the Context-free Grammar extracted from the tree. Every
	 * line contains one rule.
	 * 
	 * @return
	 */
	public String getCFG() {
		return getCFG(false);
	}

	/**
	 * Returns a string with the Probabilistic Context-free Grammar extracted from
	 * the tree. Every line contains one rule.
	 * 
	 * @return
	 */
	public String getPCFG() {
		return "";
	}

	/**
	 * Returns an SVG graphic with the tree representation.
	 * 
	 * @return
	 */
	public String getSVGTree() {
		return "";
	}

}
