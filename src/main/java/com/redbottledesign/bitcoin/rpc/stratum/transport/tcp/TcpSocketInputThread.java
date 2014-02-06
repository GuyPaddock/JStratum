package com.redbottledesign.bitcoin.rpc.stratum.transport.tcp;

import java.util.List;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redbottledesign.bitcoin.rpc.stratum.message.Message;
import com.redbottledesign.bitcoin.rpc.stratum.message.MessageMarshaller;
import com.redbottledesign.bitcoin.rpc.stratum.transport.ConnectionState;

/**
 * <p>A generic Stratum TCP socket input thread.</p>
 *
 * <p>© 2013 - 2014 RedBottle Design, LLC.</p>
 *
 * @author Guy Paddock (guy.paddock@redbottledesign.com)
 */
public class TcpSocketInputThread
extends Thread
{
    /**
     * The logger for this thread.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(TcpSocketInputThread.class);

    /**
     * The TCP message transport.
     */
    private final AbstractTcpMessageTransport transport;

    /**
     * Constructor for {@link TcpSocketInputThread} that initializes the new
     * input thread for the specified Stratum TCP message transport.
     *
     * @param   transport
     *          The transport to which the input thread pertains.
     */
    public TcpSocketInputThread(AbstractTcpMessageTransport transport)
    {
        this.setName(this.getClass().getSimpleName());
        this.setDaemon(true);

        this.transport = transport;
    }

    /**
     * Runs the input thread, continuously dispatching incoming messages
     * until the TCP connection is closed.
     */
    @Override
    public void run()
    {
        try (Scanner scan = new Scanner(this.transport.getSocket().getInputStream()))
        {
            while (this.transport.isOpen())
            {
                String jsonLine = scan.nextLine().trim();

                if (!jsonLine.isEmpty())
                {
                    synchronized (this.transport)
                    {
                        ConnectionState   currentState = this.transport.getConnectionState();
                        MessageMarshaller marshaller   = currentState.getMarshaller();
                        List<Message>     messages;

                        if (LOGGER.isTraceEnabled())
                            LOGGER.trace("Stratum [in]: " + jsonLine);

                        messages = marshaller.marshalMessages(jsonLine);

                        this.transport.receiveMessages(messages);
                    }
                }
            }
        }

        catch (Exception ex)
        {
            if (LOGGER.isErrorEnabled())
            {
                LOGGER.error(
                    String.format(
                        "Error on connection: %s",
                        ex.getMessage()),
                    ex);
            }
        }

        finally
        {
            this.transport.close();
        }

        if (LOGGER.isTraceEnabled())
            LOGGER.trace("Input thread exiting.");
    }
}