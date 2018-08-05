/* 
 * Grammar.java
 * 
 * Copyright 2018 by Damir Cavar <damir@semiring.com>
 *
 * This is the main grammar parsing and processing class.
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

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Damir Cavar
 */
public class Grammar {

	public List<Tree> trees;

	Grammar() {
		this.trees = new ArrayList<Tree>();
	}

	/**
	 * 
	 * @return
	 */
	public String getCFG() {
		String rules = "";
		return (rules);
	}

	/**
	 * 
	 * @return
	 */
	public String getPCFG() {
		String rules = "";
		return (rules);
	}

	/**
	 * 
	 */
	public void parseCFG() {
	}

	/**
	 * 
	 */
	public void parsePCFG() {
	}

	/**
	 * 
	 * @param tree
	 */
	public void processTree(String tree) {
		Tree mTree = new Tree();
		mTree.parseTree(tree);
		this.trees.add(mTree);
	}
}
