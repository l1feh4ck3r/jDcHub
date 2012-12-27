/*
* ChatBot.java
*
* Created on 03 02 2012, 15:06
*
* Copyright (C) 2012 Alexey 'lh' Antonov
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

package jdchub.module;

import com.rabbitmq.client.AlreadyClosedException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.tools.json.JSONWriter;
import lombok.extern.slf4j.Slf4j;
import ru.sincore.ClientManager;
import ru.sincore.Command;
import ru.sincore.ConfigurationManager;
import ru.sincore.TigerImpl.CIDGenerator;
import ru.sincore.adc.ClientType;
import ru.sincore.adc.MessageType;
import ru.sincore.adc.action.actions.MSG;
import ru.sincore.client.AbstractClient;
import ru.sincore.client.Bot;
import ru.sincore.util.AdcUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Rabbit chat bot class.
 * It sends broadcast chat messages to rabbit mq.
 *
 * @author Alexey 'lh' Antonov
 * @since 2012-02-03
 */
@Slf4j
public class RabbitBot extends Bot
{
    private final static String     EXCHANGE_NAME = "chat";
    private              Connection connection    = null;
    private              Channel    channel       = null;


    public RabbitBot()
    {
        ConfigurationManager configurationManager = ConfigurationManager.getInstance();

        this.setNick(configurationManager.getString("bot.nick"));
        this.setSid("RBOT");
        this.setCid(CIDGenerator.generate());
        this.setDescription(AdcUtils.toAdcString(configurationManager.getString("bot.description")));
        this.setEmail(configurationManager.getString("bot.email"));
        this.setWeight(10);
        this.setClientType(ClientType.BOT);
        this.setValidated();
        this.setActive(true);
        this.setMustBeDisconnected(false);
    }


    public void initRabbit()
    {
        ConfigurationManager configurationManager = ConfigurationManager.getInstance();

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(configurationManager.getString("rabbitmq.host"));
        factory.setPort(configurationManager.getInt("rabbitmq.port"));
        factory.setVirtualHost(configurationManager.getString("rabbitmq.vhost"));
        factory.setUsername(configurationManager.getString("rabbitmq.username"));
        factory.setPassword(configurationManager.getString("rabbitmq.password"));

        try
        {
            connection = factory.newConnection();
            channel = connection.createChannel();
            //channel.queueDeclare(EXCHANGE_NAME, false, false, false, null);
            channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
        }
        catch (IOException e)
        {
            log.error("Error in connection creation process : " + e.toString());
        }
    }


    public void deinitRabbit()
    {
        try
        {
            channel.close();
            connection.close();
        }
        catch (IOException e)
        {
            log.error("Error in rabbit deinit : " + e.toString());
        }
        catch (AlreadyClosedException e)
        {
            //ignore
        }
    }


    public void sendMessage(String message)
    {
        try
        {
            MSG outgoingMessage = new MSG();
            outgoingMessage.setMessage(message);
            outgoingMessage.setMessageType(MessageType.B);
            outgoingMessage.setSourceSID(this.getSid());

            Command.handle(this, outgoingMessage.getRawCommand());
        }
        catch (Exception ex)
        {
            log.error(ex.toString());
        }
    }


    @Override
    public void sendRawCommand(String rawCommand)
    {
        super.sendRawCommand(rawCommand);

        if (rawCommand.startsWith("BMSG"))
        {
            try
            {
                MSG message = new MSG(rawCommand);

                SimpleDateFormat
                        dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss z", Locale.US);
                AbstractClient clientFrom = ClientManager.getInstance().getClientBySID(message.getSourceSID());


                String[] messageInfo = new String[3];
                messageInfo[0] = dateFormat.format(new Date());
                messageInfo[1] = clientFrom.getNick();
                messageInfo[2] = message.getMessage();

                JSONWriter writer = new JSONWriter(false);
                String jsonMessage = writer.write(messageInfo);

                channel.basicPublish(EXCHANGE_NAME, "", null, jsonMessage.getBytes());
            }
            catch (Exception e)
            {
                log.error(e.toString());
            }
        }

    }


    public Object getEventHandler()
    {
        return null;
    }
}
