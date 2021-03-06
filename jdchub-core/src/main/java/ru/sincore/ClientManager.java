/*
 * ClientManager.java
 *
 * Created on 12 september 2011, 15:30
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

package ru.sincore;


import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.sincore.adc.ClientType;
import ru.sincore.adc.State;
import ru.sincore.client.*;
import ru.sincore.util.SubnetUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides user management control functionality
 * It is a Singleton. It's implementation describes in
 * http://www.javenue.info/post/83 (Russian)
 *
 * @author Alexey 'lh' Antonov
 * @since 2011-09-12
 */
public final class ClientManager
{
    private static final Logger log = LoggerFactory.getLogger(ClientManager.class);

    private static ConcurrentHashMap<String, AbstractClient> clientsBySID;
    private static ConcurrentHashMap<String, String>         sidByNick;
    private static ConcurrentHashMap<String, String>         sidByCID;

    private static ConcurrentHashMap<String, AbstractClient> uninitializedClients;

    // *********************** Singleton implementation start ********************************
    private static volatile Strategy strategy = new CreateAndReturnStrategy();
    private static ClientManager instance;

    private ConfigurationManager configurationManager = ConfigurationManager.getInstance();


    private static interface Strategy
    {
        ClientManager getInstance();
    }


    private static class ReturnStrategy implements Strategy
    {
        public final ClientManager getInstance()
        {
            return instance;
        }
    }


    private static class CreateAndReturnStrategy implements Strategy
    {
        public final ClientManager getInstance()
        {
            synchronized (ClientManager.class)
            {
                if (instance == null)
                {
                    instance = new ClientManager();
                    strategy = new ReturnStrategy();
                }
            }
            return instance;
        }
    }


    public static ClientManager getInstance()
    {
        return strategy.getInstance();
    }
// *********************** Singleton implementation end ********************************


    private ClientManager()
    {
        int initialCapacity =
                configurationManager.getInt(ConfigurationManager.USER_INITIAL_CAPACITY);
        float loadFactor =
                configurationManager.getFloat(ConfigurationManager.USER_LOAD_FACTOR);

        clientsBySID = new ConcurrentHashMap<String, AbstractClient>(initialCapacity, loadFactor);
        sidByNick = new ConcurrentHashMap<String, String>(initialCapacity, loadFactor);
        sidByCID = new ConcurrentHashMap<String, String>(initialCapacity, loadFactor);

        uninitializedClients = new ConcurrentHashMap<String, AbstractClient>(
                ConfigurationManager.getInstance()
                                    .getInt(ConfigurationManager.USER_CONNECTION_BUFFER_INITIAL_SIZE),
                loadFactor);

        addBots();
        addChatRooms();
    }


    private void addBots()
    {
        // Create new Hub Bot
        Bot bot = new HubBot();

        // add bot to client list
        addClient(bot);
    }


    private void addChatRooms()
    {
        ChatRoom opChat = new ChatRoom();
        opChat.setSid(configurationManager.getString(ConfigurationManager.OP_CHAT_SID));
        opChat.setCid(configurationManager.getString(ConfigurationManager.OP_CHAT_CID));
        opChat.setNick(configurationManager.getString(ConfigurationManager.OP_CHAT_NAME));
        opChat.setDescription(configurationManager.getString(ConfigurationManager.OP_CHAT_DESCRIPTION));
        opChat.setWeight(configurationManager.getInt(ConfigurationManager.OP_CHAT_WEIGHT));
        opChat.setClientType(ClientType.BOT | ClientType.OPERATOR);
        opChat.setValidated();
        opChat.loadInfo();
        addClient(opChat);

        ChatRoom vipChat = new ChatRoom();
        vipChat.setSid(configurationManager.getString(ConfigurationManager.VIP_CHAT_SID));
        vipChat.setCid(configurationManager.getString(ConfigurationManager.VIP_CHAT_CID));
        vipChat.setNick(configurationManager.getString(ConfigurationManager.VIP_CHAT_NAME));
        vipChat.setDescription(configurationManager.getString(ConfigurationManager.VIP_CHAT_DESCRIPTION));
        vipChat.setWeight(configurationManager.getInt(ConfigurationManager.VIP_CHAT_WEIGHT));
        vipChat.setClientType(ClientType.BOT | ClientType.OPERATOR);
        vipChat.setValidated();
        vipChat.loadInfo();
        addClient(vipChat);

    }


    public void addClient(AbstractClient client)
    {
        clientsBySID.put(client.getSid(), client);
        sidByNick.put(client.getNick(), client.getSid());
        sidByCID.put(client.getCid(), client.getSid());
    }


    public void addNewClient(AbstractClient client)
    {
        client.setState(State.PROTOCOL);
        uninitializedClients.put(client.getSid(), client);
    }


    synchronized public void moveClientToRegularMap(AbstractClient client)
    {
        if (uninitializedClients.remove(client.getSid()) == null)
        {
            log.error("Client was not found in uninitialized clients vector!");
        }

        addClient(client);
    }


    public void removeAllClients()
    {
        // For all clients
        for (AbstractClient client : clientsBySID.values())
        {
            if (client instanceof Client)
            {
                Client tempClient = (Client) client;
                // Close connection
                tempClient.removeSession(true);
            }
        }

        // Remove all clients from client lists
        clientsBySID.clear();
        sidByNick.clear();
        sidByCID.clear();
    }


    /**
     * Return collection of clients
     *
     * @return collection of clients
     */
    public Collection<AbstractClient> getClients()
    {
        return clientsBySID.values();
    }


    public Collection<AbstractClient> getUninitializedClients()
    {
        return uninitializedClients.values();
    }


    synchronized public AbstractClient getUninitializedClientByCID(String cid)
    {
        for (AbstractClient client : uninitializedClients.values())
        {
            if (client.getCid().equals(cid))
            {
                return client;
            }
        }

        return null;
    }


    /**
     * Return client with {@link AbstractClient#cid} equals to sid
     *
     * @param cid Client CID {@link AbstractClient#cid}
     *
     * @return {@link AbstractClient}
     */
    public AbstractClient getClientByCID(String cid)
    {
        String sid = sidByCID.get(cid);
        if (sid == null)
        {
            return null;
        }

        return clientsBySID.get(sid);
    }


    /**
     * Return client with {@link AbstractClient#nick} equals to nick
     *
     * @param nick Client nick (NI)
     * @return {@link AbstractClient}
     */
    public AbstractClient getClientByNick(String nick)
    {
        String sid = sidByNick.get(nick);
        if (sid == null)
        {
            return null;
        }

        return clientsBySID.get(sid);
    }


    /**
     * Return client with {@link AbstractClient#sid} equals to sid
     *
     * @param sid Client SID {@link AbstractClient#sid}
     * @return {@link AbstractClient}
     */
    public AbstractClient getClientBySID(String sid)
    {
        return clientsBySID.get(sid);
    }


    /**
     * Return client with {@link AbstractClient#ipAddressV4} equals to ip
     *
     * @param ip Client ip (v4)
     * @return {@link AbstractClient}
     */
    public AbstractClient getClientByIPv4(String ip)
    {
        for (AbstractClient client : clientsBySID.values())
        {
            if (ip.equals(client.getIpAddressV4()))
                return client;
        }

        return null;
    }


    /**
     * Return all clients which ips is in ip range described by subnetUtils
     *
     * @param subnetUtils describes ip range by address and mask
     * @return {@link AbstractClient}
     */
    public List<AbstractClient> getClientsByNetwork(SubnetUtils subnetUtils)
    {
        List<AbstractClient> clients =  new ArrayList<AbstractClient>();

        for (AbstractClient client : clientsBySID.values())
        {
            String ip = client.getRealIP();
            if (!StringUtils.isEmpty(ip) &&
                subnetUtils.getInfo().isInRange(client.getRealIP()))
                clients.add(client);
        }

        return clients;
    }


    public int getClientsCount()
    {
        return clientsBySID.size();
    }


    public int getValidatedClientsCount()
    {
        int count = 0;

        for (AbstractClient client : clientsBySID.values())
        {
            if (client.isValidated())
            {
                count++;
            }
        }

        return count;
    }


    public long getTotalShare()
    {
        long ret = 0;
        for (AbstractClient client : clientsBySID.values())
        {
            try
            {
                ret += client.getShareSize();
            }
            catch (ArithmeticException ae)
            {
                log.error("Exception in total share size calculation : " + ae);
            }
            catch (NullPointerException ex)
            {
                log.debug("Client " + client.getNick() + " doesn\'t have share.");
            }
        }
        return ret;
    }


    public long getTotalFileCount()
    {
        long ret = 0;
        for (AbstractClient client : clientsBySID.values())
        {
            try
            {
                ret += client.getSharedFiles();
            }
            catch (ArithmeticException ae)
            {
                log.error("Exception in total file count calculation : " + ae);
            }
            catch (NullPointerException ex)
            {
                log.debug("Client " + client.getNick() + " doesn\'t shared files.");
            }
        }
        return ret;
    }


    synchronized public boolean removeUninitializedClient(AbstractClient client)
    {
        if (uninitializedClients.remove(client.getSid()) != null)
        {
            log.debug("Uninitialized client with sid = \'" +
                      client.getSid() +
                      "\' was removed.");
            return true;
        }

        return false;
    }


    synchronized public boolean removeRegularClient(AbstractClient client)
    {
        AbstractClient removedClient = clientsBySID.remove(client.getSid());

        if (removedClient == null)
        {
            log.error("Client " + client.getNick() + " with SID [" + client.getSid() + "] not in clientsBySID.");
        }
        else
        {
            if (sidByNick.remove(removedClient.getNick()) == null)
            {
                log.error("Client " + client.getNick() + " with SID [" + client.getSid() + "] not in sidByNick.");
            }

            if (sidByCID.remove(removedClient.getCid()) == null)
            {
                log.error("Client " + client.getNick() + " with SID [" + client.getSid() + "] not in sidByCID.");
            }

            log.debug("Client " + removedClient.getNick() +
                      " with SID [" + removedClient.getSid() +
                      "] was removed.");
        }

        return removedClient != null;
    }


    public void sendClientsInfsToClient(AbstractClient client)
    {
        for (AbstractClient oldClient : clientsBySID.values())
        {
            if (oldClient.isValidated() && !oldClient.equals(client))
            {
                client.sendRawCommand(oldClient.getINF());
            }
        }
    }
}
