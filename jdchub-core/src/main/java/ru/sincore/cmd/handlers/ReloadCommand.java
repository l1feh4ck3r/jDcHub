/*
* ReloadHandler.java
*
* Created on 16 november 2011, 12:08
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

package ru.sincore.cmd.handlers;

import ru.sincore.ConfigurationManager;
import ru.sincore.client.AbstractClient;
import ru.sincore.cmd.AbstractCommand;
import ru.sincore.util.ClientUtils;

/**
 * @author Alexey 'lh' Antonov
 * @since 2011-11-16
 */
public class ReloadCommand extends AbstractCommand
{
    private long timeout = 1000;


    @Override
    public String execute(String cmd, String args, AbstractClient commandOwner)
    {
        ClientUtils.sendMessageToOpChat(commandOwner.getNick() + " called !reload command.");

        ClientUtils.broadcastTextMessageFromHub("Hub will be freezed due to configs loading...");

        try
        {
            Thread.sleep(timeout);
        }
        catch (InterruptedException ex)
        {
            // ignored
        }

        String result;
        if (ConfigurationManager.getInstance().loadConfigs())
        {
            result = "Configs reloaded.";
            commandOwner.sendPrivateMessageFromHub(result);
        }
        else
        {
            result = "Configs doesn\'t reloaded. See logs for more information.";
            commandOwner.sendPrivateMessageFromHub(result);
        }

        return result;
    }
}
