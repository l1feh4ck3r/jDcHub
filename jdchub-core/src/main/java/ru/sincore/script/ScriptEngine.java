/*
* ScriptEngine.java
*
* Created on 21 12 2011, 13:42
*
* Copyright (C) 2011 Alexey 'lh' Antonov
*
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation; either version 2
* of the License, or any later version.

* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.

* You should have received a copy of the GNU General Public License
* along with this program; if not, write to the Free Software
* Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/

package ru.sincore.script;

import org.python.core.PySystemState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.sincore.ConfigurationManager;
import ru.sincore.cmd.CommandEngine;
import ru.sincore.db.dao.ScriptInfoDAO;
import ru.sincore.db.dao.ScriptInfoDAOImpl;
import ru.sincore.db.pojo.ScriptInfoPOJO;
import ru.sincore.script.executor.PyScriptExecutor;

import java.io.File;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Alexey 'lh' Antonov
 * @since 2011-12-21
 */
public class ScriptEngine extends Thread
{
    private final static Logger log = LoggerFactory.getLogger(ScriptEngine.class);

    /**
     * ScriptExecutionPools for all types of script engines (jython, bsh etc).
     * If engines equals to null it means ScriptEngine was not initialized!
     */
    private ConcurrentHashMap<String, ScriptExecutionPool> engines = null;


    public ScriptEngine()
    {
        
    }


    public void initialize()
    {
        this.initialize(null);
    }


    public void initialize(CommandEngine commandEngine)
    {
        if (this.engines != null)
        {
            // already initialized
            return;
        }

        engines = new ConcurrentHashMap<String, ScriptExecutionPool>();

        initializePythonScriptEngine();

        // registering script engine management command
        if (commandEngine != null)
        {
            commandEngine.registerCommand("script", new ScriptCommandHandler(this));
        }
    }


    private void initializePythonScriptEngine()
    {
        ConfigurationManager configurationManager = ConfigurationManager.getInstance();

        String scriptsPath = configurationManager.getString(ConfigurationManager.SCRIPTS_LOCATION) + "/py/";
        int numberOfThreads = configurationManager.getInt(ConfigurationManager.NUMBER_OF_SCRIPTS_INTERPRETERS);

        Properties properties = PySystemState.getBaseProperties();

        PySystemState.initialize(properties, null, null);
        
        engines.put("py", new ScriptExecutionPool(PyScriptExecutor.class, scriptsPath, numberOfThreads, 100));
    }


    public void addTask(ScriptTask task)
    {
        ScriptExecutionPool executor = this.engines.get(task.getEngineType());

        if (executor == null)
        {
            log.debug("Engine for script \'" + task.getScriptName() + "\' not found!");
            return;
        }

        executor.execute(task);
    }


    @Override
    public void run()
    {
        if (engines == null)
        {
            log.error("Engine not initialized.");
            return;
        }

        // execute all scripts
        File scriptDirectory = new File(ConfigurationManager.getInstance()
                                                            .getString(ConfigurationManager.SCRIPTS_LOCATION));
        for (File enginesDir : scriptDirectory.listFiles())
        {
            if (!enginesDir.isDirectory())
            {
                continue;
            }

            for (File script : enginesDir.listFiles())
            {
                // skipping directories
                if (script.isDirectory())
                {
                    continue;
                }

                // skip files with wrong extentions
                if (!script.getName().endsWith("." + enginesDir.getName()))
                {
                    continue;
                }

                ScriptInfoDAO scriptInfoDAO = new ScriptInfoDAOImpl();
                ScriptInfoPOJO scriptInfoPOJO = scriptInfoDAO.getScriptInfo(script.getName());

                if (scriptInfoPOJO == null)
                {
                    scriptInfoPOJO = new ScriptInfoPOJO();
                    scriptInfoPOJO.setName(script.getName());

                    scriptInfoDAO.addScriptInfo(scriptInfoPOJO);
                }

                if (scriptInfoPOJO.getEnabled())
                {
                    ScriptTask task = new ScriptTask();
                    task.setEngineType(enginesDir.getName());
                    task.setScriptName(script.getName());
                    this.addTask(task);
                }
            }
        }
    }


    synchronized public void stopEngines()
    {
        for (ScriptExecutionPool engine: engines.values())
        {
            engine.stop();
        }

        engines.clear();
    }

    public void restart()
    {
        if (this.engines == null)
        {
            return;
        }

        stopEngines();
        initialize();
        start();
    }
}
