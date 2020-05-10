package com.redbottledesign.bitcoin.rpc.stratum.transport.tcp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalCause;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

/**
 * <p>A TCP implementation of a Stratum server.</p>
 *
 * <p>Each connection maintained by this implementation is stateful, allowing
 * it to gracefully progress through conversations of varying complexity with
 * a Stratum client.</p>
 *
 * <p>This implementation also automatically closes and expires connections
 * that sit idle for longer than
 * {@link StratumTcpServerConnection#MAX_IDLE_TIME_MSECS}.
 * The {@link #onConnectionTimeout(StratumTcpServerConnection)} method is
 * invoked each time that a connection expires.</p>
 *
 * <p>© 2013 - 2014 RedBottle Design, LLC.</p>
 * <p>© 2020 Inveniem.</p>
 *
 * @author Guy Paddock (guy.paddock@redbottledesign.com)
 */
public abstract class StratumTcpServer
{
    /**
     * The logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(StratumTcpServer.class);

    /**
     * The server socket.
     */
    private ServerSocket serverSocket;

    /**
     * The cache of existing TCP connections.
     */
    private final Cache<String, StratumTcpServerConnection> connections;

    /**
     * Default constructor for {@link StratumTcpServer}.
     */
    public StratumTcpServer()
    {
        this.connections = this.createConnectionMap();
    }

    /**
     * Returns whether or not the server is listening for connections.
     *
     * @return  {@code true} if the server is listening; {@code false}
     *          otherwise.
     */
    public boolean isListening()
    {
        final ServerSocket  socket = this.getServerSocket();
        final boolean       result = (socket != null) && !socket.isClosed();

        if (LOGGER.isTraceEnabled())
            LOGGER.trace("isListening(): " + result);

        return result;
    }

    /**
     * Starts listening for connections on the specified port.
     *
     * @param   port
     *          The port on which to listen for connections.
     *
     * @throws  IOException
     *          If the socket cannot be opened.
     */
    public void startListening(final int port)
    throws IOException
    {
        if (this.isListening())
            throw new IllegalStateException("The server is already listening for connections.");

        this.setServerSocket(new ServerSocket(port));

        while (this.isListening())
        {
            final Socket                     connectionSocket = this.serverSocket.accept();
            final StratumTcpServerConnection connection       = this.createConnection(connectionSocket);

            this.acceptConnection(connection);

            connection.open();
        }
    }

    /**
     * Stops the server from listening for additional connections, if it is
     * currently listening.
     */
    public void stopListening()
    {
        if (this.isListening())
        {
            try
            {
                this.getServerSocket().close();
            }

            catch (IOException ex)
            {
                if (LOGGER.isDebugEnabled())
                    LOGGER.debug("Exception encountered while closing server socket: " + ex.getMessage(), ex);
            }
        }
    }

    /**
     * Gets the connections that are currently active on this server.
     *
     * @return  The active server connections.
     */
    protected Cache<String, StratumTcpServerConnection> getConnections()
    {
        return this.connections;
    }

    /**
     * Gets the server socket.
     *
     * @return  The server socket.
     */
    protected ServerSocket getServerSocket()
    {
        return this.serverSocket;
    }

    /**
     * Sets the server socket.
     *
     * @param   serverSocket
     *          The new server socket.
     */
    protected void setServerSocket(final ServerSocket serverSocket)
    {
        this.serverSocket = serverSocket;
    }

    /**
     * Creates a Stratum TCP server connection for the provided active socket.
     *
     * @param   connectionSocket
     *          The socket for which a connection is desired.
     *
     * @return  The Stratum TCP server connection for the socket.
     */
    protected abstract StratumTcpServerConnection createConnection(final Socket connectionSocket);

    /**
     * <p>Performs any necessary logic to accept the provided connection.</p>
     *
     * @param   connection
     *          The connection being accepted.
     */
    protected void acceptConnection(final StratumTcpServerConnection connection)
    {
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("Connection accepted: " + connection.getConnectionId());

        //noinspection UnstableApiUsage
        this.connections.put(connection.getConnectionId(), connection);
    }

    /**
     * Resets the timeout counter for the provided connection, preventing the
     * connection from expiring until
     * another {@link StratumTcpServerConnection#MAX_IDLE_TIME_MSECS} have
     * passed without this method being called again.
     *
     * @param   connection
     *          The connection for which the timer is being reset.
     *
     * @throws  IllegalArgumentException
     *          If the connection is not known to this server.
     */
    protected void resetConnectionTimeout(final StratumTcpServerConnection connection)
    throws IllegalArgumentException
    {
        this.resetConnectionTimeout(connection.getConnectionId());
    }

    /**
     * Resets the timeout counter for the provided connection, preventing the
     * connection from expiring until
     * another {@link StratumTcpServerConnection#MAX_IDLE_TIME_MSECS} have
     * passed without this method being called again.
     *
     * @param   connectionId
     *          The unique identifier for the connection for which the timer is
     *          being reset.
     *
     * @throws  IllegalArgumentException
     *          If the connection is not known to this server.
     */
    protected void resetConnectionTimeout(String connectionId)
    throws IllegalArgumentException
    {
        // This is enough to refresh the connection, due to the expireAfterAccess() rule.
        final StratumTcpServerConnection connection = this.connections.getIfPresent(connectionId);

        if (connection == null)
            throw new IllegalArgumentException("No such connection is known to this server: " + connectionId);
    }

    /**
     * Method invoked when a connection times-out due to inactivity.
     *
     * @param   connection
     *          The connection that has timed-out.
     */
    protected void onConnectionTimeout(final StratumTcpServerConnection connection)
    {
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("Idle connection timed-out: " + connection.getConnectionId());

        connection.close();
    }

    /**
     * <p>Creates the map that contains the connections maintained by this
     * server.</p>
     *
     * <p>Sub-classes can override this method to customize the behavior of the
     * connection map, including its rules for expiring stale connections
     * and the limit on how many connections can be active at a single time.</p>
     *
     * @return  The connection map.
     */
    protected Cache<String, StratumTcpServerConnection> createConnectionMap()
    {
        return CacheBuilder
            .newBuilder()
            .expireAfterAccess(StratumTcpServerConnection.MAX_IDLE_TIME_MSECS, TimeUnit.MILLISECONDS)
            .removalListener(new ConnectionExpirationListener())
            .build();
    }

    /**
     * <p>The listener that handles receiving notifications about when
     * connections expire.</p>
     *
     * <p>© 2013 - 2014 RedBottle Design, LLC.</p>
     *
     * @author Guy Paddock (guy.paddock@redbottledesign.com)
     */
    protected class ConnectionExpirationListener
    implements RemovalListener<String, StratumTcpServerConnection>
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public void onRemoval(final RemovalNotification<String, StratumTcpServerConnection> notification)
        {
            if (notification.getCause() == RemovalCause.EXPIRED)
                StratumTcpServer.this.onConnectionTimeout(notification.getValue());
        }
    }
}
