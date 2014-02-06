package com.redbottledesign.bitcoin.rpc.stratum.transport.tcp;

import java.io.IOException;
import java.net.Socket;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redbottledesign.bitcoin.rpc.stratum.message.Message;
import com.redbottledesign.bitcoin.rpc.stratum.message.RequestMessage;
import com.redbottledesign.bitcoin.rpc.stratum.message.ResponseMessage;
import com.redbottledesign.bitcoin.rpc.stratum.transport.ConnectionState;
import com.redbottledesign.bitcoin.rpc.stratum.transport.StatefulMessageTransport;

/**
 * <p>Abstract base class for Stratum TCP message transports.</p>
 *
 * <p>© 2013 - 2014 RedBottle Design, LLC.</p>
 *
 * @author Guy Paddock (guy.paddock@redbottledesign.com)
 */
public abstract class AbstractTcpMessageTransport
extends StatefulMessageTransport
{
    /**
     * The logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTcpMessageTransport.class);

    /**
     * The thread responsible for sending messages to the remote end.
     */
    private final TcpSocketOutputThread outThread;

    /**
     * The thread responsible for receiving messages from the remote end.
     */
    private final TcpSocketInputThread inThread;

    /**
     * The state that each connection will enter upon receiving a connection from a client.
     */
    private ConnectionState postConnectState;

    /**
     * The connection socket.
     */
    private volatile Socket socket;

    /**
     * Default constructor for {@link AbstractTcpMessageTransport}.
     */
    public AbstractTcpMessageTransport()
    {
        this(null);
    }

    /**
     * Constructor for {@link AbstractTcpMessageTransport} that configures the
     * transport with the specified post-connection state.
     *
     * @param   postConnectState
     *          The state that the connection should enter when the client
     *          connects.
     */
    public AbstractTcpMessageTransport(ConnectionState postConnectState)
    {
        super();

        this.setPostConnectState(postConnectState);

        this.outThread = new TcpSocketOutputThread(this);
        this.inThread  = new TcpSocketInputThread(this);
    }

    /**
     * Sets the state that the connection should enter when the client
     * connects.
     *
     * @param   postConnectState
     *          The new post-connect state.
     */
    public void setPostConnectState(ConnectionState postConnectState)
    {
        if (this.isOpen())
            throw new IllegalStateException("The post-connect state cannot be set once the connection is open.");

        this.postConnectState = postConnectState;
    }

    /**
     * Gets the state that the connection will enter when this client connects.
     *
     * @return  The post-connection state.
     */
    public ConnectionState getPostConnectState()
    {
        return this.postConnectState;
    }

    /**
     * Returns whether or not the connection is open.
     *
     * @return  {@code true} if the connection is open; {@code false}
     *          otherwise.
     */
    public boolean isOpen()
    {
        final Socket  socket = this.getSocket();
        final boolean result = (socket != null) && !socket.isClosed();

        if (LOGGER.isTraceEnabled())
            LOGGER.trace("isOpen(): " + result);

        return result;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Due to the direct nature of TCP, this method does nothing on this
     * type of transport. The remote end of the connection sends messages as
     * soon as they are ready, rather than relying on polling.</p>
     */
    @Override
    public void pollForMessages()
    {
        /* No polling is needed with this transport. Messages are sent whenever
         * they are available.
         */
    }

    /**
     * Queues the specified request to go out on the current connection.
     *
     * @param   message
     *          The request message to send.
     *
     * @throws  IllegalStateException
     *          If the client is not currently connected.
     */
    @Override
    public void sendRequest(RequestMessage message)
    throws IllegalStateException
    {
        this.sendRequest(message, null);
    }

    /**
     * Queues the specified request to go out on the current connection, and
     * associates it with a response of the specified type.
     *
     * @param   message
     *          The request message to send.
     *
     * @param   responseType
     *          The type of response expected for the request.
     *          This may be {@code null}.
     *
     * @throws  IllegalStateException
     *          If the connection is not currently open.
     */
    @Override
    public void sendRequest(RequestMessage message, Class<? extends ResponseMessage> responseType)
    throws IllegalStateException
    {
        assertConnected();

        if (responseType != null)
            this.getConnectionState().getMarshaller().registerPendingRequest(message.getId(), responseType);

        this.getOutputThread().queueMessage(message);
    }

    /**
     * Queues the specified response to go out on the current connection.
     *
     * @param   message
     *          The response message to send.
     *
     * @throws  IllegalStateException
     *          If the client is not currently connected.
     */
    @Override
    public void sendResponse(ResponseMessage message)
    throws IllegalStateException
    {
        assertConnected();

        this.getOutputThread().queueMessage(message);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close()
    {
        if (this.isOpen())
        {
            try
            {
                this.socket.close();
            }

            catch (IOException ex)
            {
                if (LOGGER.isDebugEnabled())
                    LOGGER.debug("Exception encountered while closing socket: " + ex.getMessage(), ex);
            }
        }
    }

    /**
     * Gets the thread responsible for receiving messages from the remote end.
     *
     * @return  The output thread.
     */
    protected TcpSocketOutputThread getOutputThread()
    {
        return this.outThread;
    }

    /**
     * Gets the thread responsible for receiving messages from the remote end.
     *
     * @return  The input thread.
     */
    protected TcpSocketInputThread getInputThread()
    {
        return this.inThread;
    }

    /**
     * Gets the connection socket.
     *
     * @return  The socket.
     */
    protected Socket getSocket()
    {
        return this.socket;
    }

    /**
     * Sets the connection socket.
     *
     * @param   socket
     *          The new socket.
     */
    protected void setSocket(Socket socket)
    {
        this.socket = socket;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected synchronized ConnectionState getConnectionState()
    {
        return super.getConnectionState();
    }

    /**
     * Notifies the transport to receive the specified messages, which
     * typically involves notifying any listeners registered with the
     * transport.
     *
     * @param   messages
     *          The messages to receive.
     */
    protected void receiveMessages(List<Message> messages)
    {
        this.notifyMessageListeners(messages);
    }

    /**
     * Convenience method for asserting that the connection is open.
     *
     * @throws  IllegalStateException
     *          If the connection is not currently open.
     */
    protected void assertConnected()
    {
        if (!this.isOpen())
            throw new IllegalStateException("The connection is not open.");
    }
}
