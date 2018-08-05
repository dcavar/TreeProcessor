/*
 * Comparse.java
 *
 * Copyright 2018 by Damir Cavar <damir@semiring.com>
 *
 * This is a command line processing class for TreeProcessor.
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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * 
 * @author Damir Cavar
 *
 */
public class Comparse {

    private static final Logger log = Logger.getLogger(App.MODNAME);

    private String[] args;
    private Options options = new Options();

    public String treesFile = "";
    public boolean testMode = false;
    
    /**
     * 
     * @param args
     */
    public Comparse(String[] args) {
        this.args = args;
        options.addOption("h", "help", false, "Show help.");
        options.addOption("r", "read", true, "Read trees from file.");
        options.addOption("t", "test", false, "Run in test mode.");
    }

    /**
     * 
     */
    public void parse() {
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
            if (cmd.hasOption("h")) {
                help();
            }
            if (cmd.hasOption("r")) {
                log.log(Level.INFO, "Reading file " + cmd.getOptionValue("r"));
                // read trees from file
                this.treesFile = cmd.getOptionValue("r");
            }
            if (cmd.hasOption("t")) {
                log.log(Level.INFO, "Testmode selected...");
            	this.testMode = true;
            }
        } catch (ParseException e) {
            log.log(Level.SEVERE, "Error parsing command line arguments", e);
            help();
        }
    }

    /**
     * 
     */
    private void help() {
        HelpFormatter formater = new HelpFormatter();
        formater.printHelp("Main", options);
        System.exit(0);
    }
}
