/*
 * Client.java
 *
 * Created on 07 october 2011, 12:05
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

package ru.sincore.client;

import com.adamtaft.eb.EventBusService;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.compression.CompressionFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.sincore.BigTextManager;
import ru.sincore.Broadcast;
import ru.sincore.ClientManager;
import ru.sincore.ConfigurationManager;
import ru.sincore.Exceptions.CommandException;
import ru.sincore.Exceptions.STAException;
import ru.sincore.adc.Features;
import ru.sincore.adc.State;
import ru.sincore.adc.action.actions.AbstractAction;
import ru.sincore.adc.action.actions.ZON;
import ru.sincore.db.dao.ChatLogDAO;
import ru.sincore.db.dao.ChatLogDAOImpl;
import ru.sincore.db.dao.ClientListDAO;
import ru.sincore.db.dao.ClientListDAOImpl;
import ru.sincore.db.pojo.ChatLogPOJO;
import ru.sincore.db.pojo.ClientListPOJO;
import ru.sincore.events.ClientConnected;
import ru.sincore.i18n.Messages;
import ru.sincore.signals.ZonAdcActionSignal;
import ru.sincore.signalservice.Signal;
import ru.sincore.util.AdcUtils;
import ru.sincore.util.ClientUtils;
import ru.sincore.util.Constants;
import ru.sincore.util.STAError;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author Alexey 'lh' Antonov
 * @since 2011-10-07
 */
public class Client extends AbstractClient
{
    private static final Logger log = LoggerFactory.getLogger(Client.class);

    /**
     * Client NIO session.
     */
    private IoSession session;

    /**
     * Client message buffer
     */
    private StringBuffer messageBuffer = new StringBuffer(ConfigurationManager.getInstance()
                                                                              .getInt(ConfigurationManager.MESSAGE_BUFFER_SIZE));

    @Override
    public synchronized void flushBuffer()
    {
        if (messageBuffer.length() != 0)
        {
            session.write(messageBuffer.toString());
            messageBuffer = new StringBuffer(ConfigurationManager.getInstance()
                                                                 .getInt(ConfigurationManager.MESSAGE_BUFFER_SIZE));
            lastBufferFlushTime = System.currentTimeMillis();
        }
    }


    @Override
    public synchronized void sendRawCommand(String rawCommand)
    {
        if (messageBuffer.length() + rawCommand.length() + 1 >= messageBuffer.capacity())
        {
            flushBuffer();
        }
        messageBuffer.append(rawCommand + "\n");
    }


    @Override
    public synchronized void sendAdcAction(AbstractAction action)
    {
        try
        {
            sendRawCommand(action.getRawCommand());
        }
        catch (CommandException e)
        {
            e.printStackTrace();
        }
    }


    @Override
    public void setSession(IoSession session)
    {
        this.session = session;
    }


    @Override
    public IoSession getSession()
    {
        return this.session;
    }


    @Override
    public void removeSession(boolean immediately)
    {
        session.close(immediately);
    }


    /**
     *  Store data about client if client begin registration procedure, or update statistic data.
     *
     *
     *  @see ru.sincore.db.dao.ClientListDAOImpl
     *  @see ru.sincore.db.pojo.ClientListPOJO
     */
    public void storeInfo() throws STAException
    {
        if (this.getState() != State.NORMAL)
        {
            return;
        }

        ClientListDAO clientListDAO = new ClientListDAOImpl();

        ClientListPOJO clientInfo = clientListDAO.getClientByNick(this.getNick());

        if (clientInfo == null)
        {
            clientInfo = new ClientListPOJO();
        }

        // set CID
        clientInfo.setCid(this.getCid());
        // set IP
        clientInfo.setCurrentIp(this.getIpAddressV4());
        // set Nickname
        clientInfo.setNickName(this.getNick());
        // set password
        clientInfo.setPassword(this.getPassword());

        if (!clientInfo.getRegistred())
        {
            clientInfo.setRegDate(this.getRegistrationDate());
            clientInfo.setRegOwner(this.getRegistratorNick());
        }

        clientInfo.setSharedFilesCount(this.getSharedFiles());
        clientInfo.setShareSize(this.getShareSize());
        clientInfo.setWeight(this.getWeight());
        clientInfo.setLastNick(this.getLastNick());

        clientInfo.setRegistred(this.isRegistred());
        clientInfo.setOverrideShare(this.isOverrideShare());
        clientInfo.setOverrideSpam(this.isOverrideSpam());
        clientInfo.setOverrideFull(this.isOverrideFull());
        clientInfo.setIsKickable(this.isKickable());
        clientInfo.setRenameable(this.isRenameable());
        clientInfo.setTxBytes(this.getTxBytes());
        clientInfo.setRxBytes(this.getRxBytes());
        clientInfo.setLastMessage(this.getLastRawMSG());
        clientInfo.setLoginCount(this.getLoginCount());
        clientInfo.setTimeOnline(this.getTimeOnline());
        clientInfo.setLastLogIn(this.getLoggedIn());


        long timeOnline = System.currentTimeMillis() - this.getLoggedIn().getTime();
        if (clientInfo.getMaximumTimeOnline() < timeOnline)
        {
            clientInfo.setMaximumTimeOnline(timeOnline);
        }


        clientListDAO.addClient(clientInfo);
    }


    public boolean loadInfo()
    {
        ClientListPOJO clientInfo;
        ClientListDAO clientListDAO = new ClientListDAOImpl();
        clientInfo = clientListDAO.getClientByNick(this.getNick());
        if (clientInfo == null)
            return false;

        this.setWeight(clientInfo.getWeight());
        this.setPassword(clientInfo.getPassword());
        this.setRegistred(clientInfo.getRegistred());
        this.setClientTypeByWeight(clientInfo.getWeight());
        this.setLastNick(clientInfo.getLastNick());
        this.setLastLogin(clientInfo.getLastLogIn());
        this.setRegistrationDate(clientInfo.getRegDate());
        this.setRegistratorNick(clientInfo.getRegOwner());
        this.setLastIP(clientInfo.getCurrentIp());
        this.setOverrideShare(clientInfo.getOverrideShare());
        this.setOverrideSpam(clientInfo.getOverrideSpam());
        this.setOverrideFull(clientInfo.getOverrideFull());
        this.setKickable(clientInfo.getIsKickable());
        this.setRenameable(clientInfo.getRenameable());
        this.setTxBytes(clientInfo.getTxBytes());
        this.setRxBytes(clientInfo.getRxBytes());
        this.setLastRawMSG(clientInfo.getLastMessage());
        this.setLoginCount(clientInfo.getLoginCount() + 1);
        this.setTimeOnline(clientInfo.getTimeOnline());
        this.setMaximumTimeOnline(clientInfo.getMaximumTimeOnline());

        return true;
    }


    @Override
    public void onConnected()
    {
        // make client active
        this.setActive(true);

        ClientManager.getInstance().moveClientToRegularMap(this);

        //ok now sending infs of all others to the handler
        ClientManager.getInstance().sendClientsInfsToClient(this);

        //ok now must send INF to all clients
        Broadcast.getInstance().broadcast(this.getINF(), null);

        if (isFeature(Features.UCMD) || isFeature(Features.UCM0))
        {
            //ok, he is ucmd ok, so
            this.sendRawCommand("ICMD Test CT1 TTTest");
        }

        sendMessageFromHub(ClientUtils.getHubInfo(this));

        // send MOTD
        this.sendMOTD();

        // send info client's stats
        sendMessageFromHub(Messages.get("core.client_info_header",
                                        this.getExtendedField("LC")) +
                           ClientUtils.getClientStats(this));

        // send N last messages from main chat
        this.sendNLastMessages();

        this.setAdditionalClientStats();

        // TODO [lh] enable back compressing outgoing messages
//        if (isFeature(Features.ZLIF))
//        {
//            this.enableCompression();
//        }

        EventBusService.publish(new ClientConnected(this));
    }


    private void enableCompression()
    {
        this.flushBuffer();
        this.sendRawCommand("IZON");
        this.flushBuffer();
        session.getFilterChain().addFirst(Constants.ZLIB_FILTER,
                                          new CompressionFilter(false,
                                                                true,
                                                                CompressionFilter.COMPRESSION_DEFAULT));
        Signal.emit(new ZonAdcActionSignal(this, new ZON()));
    }


    private void setAdditionalClientStats()
    {
        this.setAdditionalStat(Constants.BANNED_BY_SHARE_MESSAGE_SENT, new Boolean(false));
    }


    private void sendMOTD()
    {
        BigTextManager bigTextManager = new BigTextManager();
        this.sendMessageFromHub(
                AdcUtils.fromAdcString("\n" +
                        bigTextManager.getText(
                                BigTextManager.MOTD,
                                (String)getExtendedField("LC"))));
    }


    private void sendNLastMessages()
    {
        ChatLogDAO chatLogDAO = new ChatLogDAOImpl();
        int lastMessageCount = ConfigurationManager.getInstance().getInt(ConfigurationManager.LAST_MESSAGES_COUNT);
        List<ChatLogPOJO> chatLog = chatLogDAO.getLast(lastMessageCount);

        // reverse message list (from older to newer)
        Collections.reverse(chatLog);

        StringBuilder message = new StringBuilder();

        message.append(Messages.get("core.last_chat_messages_header",
                                    lastMessageCount,
                                    (String)getExtendedField("LC")));

        for (ChatLogPOJO chatLogEntry : chatLog)
        {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss: ");
            message.append(df.format(chatLogEntry.getSendDate()));
            message.append("<");
            message.append(chatLogEntry.getNickName());
            message.append("> ");
            message.append(AdcUtils.fromAdcString(chatLogEntry.getMessage()));
            message.append("\n");
        }

        this.sendMessageFromHub(message.toString());
    }


    @Override
    public void onLoggedIn()
    {
        try
        {
            new STAError(this, Constants.STA_SEVERITY_SUCCESS, Messages.AUTHENTICATED).send();
        }
        catch (STAException e)
        {
            e.printStackTrace();
        }

        this.setLastNick(this.getNick());
        this.setLastIP(this.getRealIP());

        //user is OK, logged in and cool
        this.setValidated();
        this.setState(State.NORMAL);

        this.setLoggedIn(new Date());
    }
}
