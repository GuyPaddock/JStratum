package com.redbottledesign.bitcoin.rpc.stratum.transport.tcp;

import java.io.PrintStream;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redbottledesign.bitcoin.rpc.stratum.message.Message;
import com.redbottledesign.bitcoin.rpc.stratum.message.MessageMarshaller;
import com.redbottledesign.bitcoin.rpc.stratum.transport.ConnectionState;

/**
 * <p>A generic Stratum TCP socket output thread.</p>
 *
 * <p>© 2013 - 2014 RedBottle Design, LLC.</p>
 *
 * @author Guy Paddock (guy.paddock@redbottledesign.com)
 */
public class TcpSocketOutputThread
extends Thread
{
    /**
     * The number of seconds that the output queue will remain blocked
     * before checking the state of the client connection.
     */
    private static final int QUEUE_POLL_TIMEOUT_SECONDS = 30;

    /**
     * The logger for this thread.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(TcpSocketOutputThread.class);

    /**
     * The TCP message transport.
     */
    private final AbstractTcpMessageTransport transport;

    /**
     * The queue of outgoing messages.
     */
    private final LinkedBlockingQueue<Message> queue;

    /**
     * Constructor for {@link TcpSocketOutputThread} that initializes the new
     * output thread for the specified Stratum TCP message transport.
     *
     * @param   transport
     *          The transport to which the output thread pertains.
     */
    public TcpSocketOutputThread(AbstractTcpMessageTransport transport)
    {
        this.setName(this.getClass().getSimpleName());
        this.setDaemon(true);

        this.transport  = transport;
        this.queue      = new LinkedBlockingQueue<Message>();
    }

    /**
     * Queues the specified message to be sent out over the connection
     * socket.
     *
     * @param   message
     *          The message to queue.
     */
    public void queueMessage(Message message)
    {
        this.queue.add(message);
    }

    /**
     * Runs the output thread, continuously dispatching queued messages
     * until the TCP connection is closed.
     */
    @Override
    public void run()
    {
        try (PrintStream outputStream = new PrintStream(this.transport.getSocket().getOutputStream()))
        {
            while (this.transport.isOpen())
            {
                /* Using poll rather than take so this thread will exit if
                 * the connection is closed.  Otherwise, it would wait
                 * forever on this queue.
                 */
                Message nextMessage = this.queue.poll(QUEUE_POLL_TIMEOUT_SECONDS, TimeUnit.SECONDS);

                if (nextMessage != null)
                {
                    synchronized (this.transport)
                    {
                        ConnectionState     currentState = this.transport.getConnectionState();
                        MessageMarshaller   marshaller   = currentState.getMarshaller();
                        String              jsonMessage  = marshaller.unmarshalMessage(nextMessage);

                        if (LOGGER.isTraceEnabled())
                            LOGGER.trace("Stratum [out]: " + jsonMessage);

                        outputStream.println(jsonMessage);
                        outputStream.flush();
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
            LOGGER.trace("Output thread exiting.");
    }
}