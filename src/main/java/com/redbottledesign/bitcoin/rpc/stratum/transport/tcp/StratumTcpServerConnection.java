package com.redbottledesign.bitcoin.rpc.stratum.transport.tcp;

import java.net.Socket;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.redbottledesign.bitcoin.rpc.stratum.message.Message;
import com.redbottledesign.bitcoin.rpc.stratum.transport.ConnectionState;

/**
 * <p>A Stratum server connection over TCP.</p>
 *
 * <p>© 2013 - 2014 RedBottle Design, LLC.</p>
 *
 * @author Guy Paddock (guy.paddock@redbottledesign.com)
 */
public abstract class StratumTcpServerConnection
extends AbstractTcpMessageTransport
{
    /**
     * The maximum amount of time that a Stratum connection can sit idle.
     */
    public static final long MAX_IDLE_TIME_MSECS = TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES);

    /**
     * The server to which this connection corresponds.
     */
    private final StratumTcpServer server;

    /**
     * The unique identifier for this connection.
     */
    private final String connectionId;

    /**
     * Whether or not this connection has been opened for servicing.
     */
    private volatile boolean isOpen;

    /**
     * Constructor for {@link StratumTcpServerConnection} that initializes the
     * connection to wrap the specified connected server-side socket.
     *
     * @param   server
     *          The server.
     *
     * @param   connectionSocket
     *          The server connection socket.
     */
    public StratumTcpServerConnection(StratumTcpServer server, Socket connectionSocket)
    {
        super();

        if (connectionSocket == null)
            throw new IllegalArgumentException("connectionSocket cannot be null.");

        this.server = server;

        this.connectionId   = UUID.randomUUID().toString();
        this.isOpen         = false;

        this.setSocket(connectionSocket);
    }

    /**
     * Gets the server to which this connection corresponds.
     *
     * @return  The server.
     */
    public StratumTcpServer getServer()
    {
        return this.server;
    }

    /**
     * Gets the unique identifier for this connection.
     *
     * @return  The unique identifier for this connection.
     */
    public String getConnectionId()
    {
        return this.connectionId;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Since the connection socket is opened before the TCP server
     * connection is constructed, this implementation also takes into account
     * whether the {@link #open()} method has been called on this
     * connection, rather than just examining the socket status.</p>
     */
    @Override
    public boolean isOpen()
    {
        return (this.isOpen && super.isOpen());
    }

    /**
     * Opens this connection and starts servicing it.
     */
    public void open()
    {
        ConnectionState postConnectState = this.createPostConnectState();

        if (this.isOpen())
            throw new IllegalStateException("The connection is already open.");

        this.setConnectionState(postConnectState);

        this.getOutputThread().start();
        this.getInputThread().start();

        this.isOpen = true;
    }

    /**
     * {@inheritDoc}
     *
     * <p>The timeout counter for this connection is also reset, in
     * acknowledgment of the fact that receiving a message makes this
     * connection active.</p>
     */
    @Override
    protected void receiveMessages(List<Message> messages)
    {
        super.receiveMessages(messages);

        // Mark this connection as active
        this.getServer().resetConnectionTimeout(this);
    }
}