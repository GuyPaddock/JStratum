package com.redbottledesign.bitcoin.rpc.stratum.transport.tcp;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import com.redbottledesign.bitcoin.rpc.stratum.transport.ConnectionState;

/**
 * <p>A TCP implementation of a Stratum client.</p>
 *
 * <p>A client can only be used for a single connection; after a connection is
 * closed, it cannot be used to connect again.</p>
 *
 * <p>© 2013 - 2014 RedBottle Design, LLC.</p>
 *
 * @author Guy Paddock (guy.paddock@redbottledesign.com)
 */
public abstract class StratumTcpClient
extends AbstractTcpMessageTransport
{
    /**
     * Default constructor for {@link StratumTcpClient}.
     */
    public StratumTcpClient()
    {
        super();
    }

    /**
     * Opens a socket to the specified address and port.
     *
     * @param   address
     *          The server address.
     *
     * @param   port
     *          The server port.
     *
     * @throws  UnknownHostException
     *          If the server address cannot be resolved.
     *
     * @throws  IOException
     *          If the connection to the server fails.
     */
    public void connect(String address, int port)
    throws UnknownHostException, IOException
    {
        this.connect(Inet4Address.getByName(address), port);
    }

    /**
     * Opens a socket to the specified address and port.
     *
     * @param   address
     *          The server address.
     *
     * @param   port
     *          The server port.
     *
     * @throws  UnknownHostException
     *          If the server address cannot be resolved.
     *
     * @throws  IOException
     *          If the connection to the server fails.
     */
    public void connect(InetAddress address, int port)
    throws IOException
    {
        ConnectionState postConnectState = this.createPostConnectState();

        if (postConnectState == null)
        {
            throw new IllegalStateException(
                "The post-connect state must be specified through the constructor or set with setPostConnectState() " +
                "before attempting to connect.");
        }

        if (this.isOpen())
            throw new IllegalStateException("The client is already connected.");

        this.setSocket(new Socket(address, port));
        this.setConnectionState(postConnectState);

        this.getOutputThread().start();
        this.getInputThread().start();
    }
}