/*
 * SessionManager.java
 *
 * Created on 06 noiembrie 2007, 14:34
 *
 * DSHub ADC HubSoft
 * Copyright (C) 2007,2008  Eugen Hristev

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

import org.apache.mina.core.buffer.BufferDataException;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.sincore.Exceptions.CommandException;
import ru.sincore.Exceptions.STAException;
import ru.sincore.TigerImpl.SIDGenerator;
import ru.sincore.client.AbstractClient;
import ru.sincore.client.Client;
import ru.sincore.i18n.Messages;
import ru.sincore.signals.ClientQuitSignal;
import ru.sincore.signalservice.Signal;
import ru.sincore.util.Constants;
import ru.sincore.util.STAError;

import java.util.Date;
import java.util.StringTokenizer;

/**
 * @author Pietricica
 * @author Valor
 * @author Alexey 'lh' Antonov
 * @since 2011-09-06
 */
public class SessionManager extends IoHandlerAdapter
{
    private static final Logger log = LoggerFactory.getLogger(SessionManager.class);

    /**
     * Creates a new instance of SessionManager
     */
    public SessionManager()
    {
    }


    public void Disconnect(IoSession session)
    {
        session.close(false);
    }


    public void exceptionCaught(IoSession session, Throwable t)
            throws Exception
    {
        super.exceptionCaught(session, t);

        AbstractClient client = (AbstractClient) session.getAttribute(Constants.SESSION_ATTRIBUTE_CLIENT);
        try
        {
            if (t instanceof java.io.IOException)
            {
                log.error("[SESSION ERROR] client [" +
                          client.getNick() +
                          ", " +
                          session.getRemoteAddress() +
                          "] IOException error : " +
                          t.toString());

                client.disconnect();
                return;
            }

            if (t instanceof BufferDataException)
            {
                log.error("[SESSION ERROR] client [" +
                          client.getNick() +
                          ", " +
                          session.getRemoteAddress() +
                          "] BufferDataException error : " +
                          t.toString());

                new STAError(client,
                             Constants.STA_SEVERITY_RECOVERABLE,
                             Messages.MESSAGE_TOO_LONG,
                             t.toString());
                return;
            }

            String throwableMessage = t.getMessage();
            if (throwableMessage != null)
            {
                if ((throwableMessage.contains("java.nio.charset.MalformedInputException")))
                {
//                ((ClientHandler) (session.getAttribute("client")))
//                        .sendFromBot("Unicode Exception. Your client sent non-Unicode chars. Ignored.");
                }
                else
                {
                    log.error("[SESSION ERROR] client [" +
                              client.getNick() +
                              ", " +
                              session.getRemoteAddress() +
                              "] error : " +
                              t.toString());

                    client.disconnect();
                }
            }
        }
        catch (Exception e)
        {
            log.error("[SESSION ERROR] client [" +
                      client.getNick() +
                      ", " +
                      session.getRemoteAddress() +
                      "] error : " +
                      "Funny exception in exceptionCaught(), here is the stack trace:", e);
        }
    }


    public void messageReceived(IoSession session, Object msg)
            throws Exception, STAException
    {
        String rawMessage = (String) msg;
		log.debug("Incoming message : "+ rawMessage);

        ((Client) session.getAttribute(Constants.SESSION_ATTRIBUTE_CLIENT)).setLastKeepAlive(System.currentTimeMillis());

        try
        {
            Command.handle((Client) (session.getAttribute(Constants.SESSION_ATTRIBUTE_CLIENT)), rawMessage);
        }
        catch (STAException stex)
        {
            if (stex.getStaCode() < 200)
            {
                return;
            }
			log.error(stex.toString());
        }
        catch (CommandException cfex)
        {
			log.error(cfex.toString());
        }

    }

    public void messageSent(IoSession session, Object message)
            throws Exception
    {
        log.debug("Outgoing message from hub : \'" + message.toString() + "\'");
    }

    public void sessionIdle(IoSession session, IdleStatus status)
            throws Exception
    {
        ((Client)session.getAttribute(Constants.SESSION_ATTRIBUTE_CLIENT)).setLastKeepAlive(System.currentTimeMillis());
    }


    public void sessionClosed(IoSession session)
            throws Exception
    {
        Client currentClient = (Client)(session.getAttribute(Constants.SESSION_ATTRIBUTE_CLIENT));
        session.removeAttribute(Constants.SESSION_ATTRIBUTE_CLIENT);

        if (ClientManager.getInstance().removeRegularClient(currentClient))
        {
            // broadcast client quited message
            Broadcast.getInstance().broadcast("IQUI " + currentClient.getSid(), currentClient);
        }
        else if (ClientManager.getInstance().removeUninitializedClient(currentClient))
        {
            //remove uninitialized client and nobody needs to know about that client
        }
        else
        {
            log.error("[SESSION ERROR] client [" +
                      currentClient.getNick() +
                      ", " +
                      session.getRemoteAddress() +
                      "]  not found.");
        }


        /** calling plugins...*/
        // Publish async event
        // TODO
        // Emit sync signal
        Signal.emit(new ClientQuitSignal(currentClient));

        currentClient.increaseTimeOnline(System.currentTimeMillis() -
                                         currentClient.getLoggedIn().getTime());

        log.info("[DISCONNECTED] Client " +
                 currentClient.getNick() +
                 " with SID [" +
                 currentClient.getSid() +
                 "] and ip [" +
                 session.getRemoteAddress() +
                 "] disconnected.");

        currentClient.storeInfo();
    }


    public void sessionOpened(IoSession session)
            throws Exception
    {
		Client newClient = new Client();

        session.setAttribute(Constants.SESSION_ATTRIBUTE_CLIENT, newClient);

        newClient.setSession(session);
        StringTokenizer ST = new StringTokenizer(session.getRemoteAddress().toString(), "/:");

        String realIp = ST.nextToken();

		newClient.setRealIP(realIp);
        newClient.setSid(SIDGenerator.generateUnique());
        newClient.setLoggedIn(new Date());

        log.info("[CONNECTED] Client with SID [" +
                 newClient.getSid() +
                 "] and real IP[" +
                 realIp +
                 "] connected.");

        /**
         * Client will be moved from uninitialized to regular map after
         * handshacke will be done.
         * See {@link ru.sincore.adc.action.handlers.INFHandler#handle()}
         */
        ClientManager.getInstance().addNewClient(newClient);
    }
}
