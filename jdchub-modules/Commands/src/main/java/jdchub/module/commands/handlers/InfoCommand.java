 /*
 * InfoCommand.java
 *
 * Created on 28 october 2011, 13:52
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

package jdchub.module.commands.handlers;

import ru.sincore.ClientManager;
import ru.sincore.client.AbstractClient;
import ru.sincore.cmd.AbstractCommand;
import ru.sincore.util.ClientUtils;

 /**
 * @author Alexey 'lh' Antonov
 * @since 2011-10-28
 */
public class InfoCommand extends AbstractCommand
{
    private AbstractClient  client;
    private String          cmd;
    private String          args;


    @Override
    public String execute(String cmd, String args, AbstractClient commandOwner)
    {
        this.client = commandOwner;
        this.cmd	= cmd;
        this.args	= args;

        StringBuilder info = new StringBuilder();
        info.append("\n >> Information about client:");

        if (args == null || args.isEmpty())
        {
            // send info client's stats
            info.append(ClientUtils.getClientStats(commandOwner));
            commandOwner.sendPrivateMessageFromHub(info.toString());
        }
        else if ((args.equals("?")))
        {
            showHelp();
            return null;
        }
        else
        {
            AbstractClient clientAbout = ClientManager.getInstance().getClientByNick(args);

            if (clientAbout == null)
            {
                String result = "Client with nick \'" + args + "\' not found!";
                commandOwner.sendPrivateMessageFromHub(result);
                return result;
            }

            info.append(ClientUtils.getClientStats(clientAbout));
            commandOwner.sendPrivateMessageFromHub(info.toString());
        }

        return info.toString();
    }


    private void showHelp()
    {
        StringBuilder result = new StringBuilder();

        result.append("\nShows information about client.\n");
        result.append("Usage: +info [<nick>]\n");
        result.append("\tWhere <nick> - client nick.\n");
        result.append("\tIf use without <nick>, actionName show info about yourself.\n");

        client.sendPrivateMessageFromHub(result.toString());
    }
}
