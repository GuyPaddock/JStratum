package com.redbottledesign.bitcoin.rpc.stratum.transport;

import java.io.IOException;

import com.redbottledesign.bitcoin.rpc.stratum.message.Message;
import com.redbottledesign.bitcoin.rpc.stratum.message.RequestMessage;
import com.redbottledesign.bitcoin.rpc.stratum.message.ResponseMessage;

/**
 * <p>Common interface Stratum transports.</p>
 *
 * <p>A transport is responsible for sending and receiving {@link Message}s
 * over a protocol like HTTP or TCP sockets.</p>
 *
 * <p>© 2013 - 2014 RedBottle Design, LLC.</p>
 *
 * @author Guy Paddock (guy.paddock@redbottledesign.com)
 */
public interface MessageTransport
{
    /**
     * Sends the specified request over the transport.
     *
     * @param   message
     *          The request message to send.
     *
     * @throws  IOException
     *          If the transport connection fails for any reason, such as an
     *          interrupted connection or any other type of unexpected
     *          connection drop-out or failure.
     */
    public abstract void sendRequest(RequestMessage message)
    throws IOException;

    /**
     * Sends the specified request over the transport, and
     * associates it with a response of the specified type.
     *
     * @param   message
     *          The request message to send.
     *
     * @param   responseType
     *          The type of response expected for the request.
     *
     * @throws  IOException
     *          If the transport connection fails for any reason, such as an
     *          interrupted connection or any other type of unexpected
     *          connection drop-out or failure.
     */
    public void sendRequest(RequestMessage message, Class<? extends ResponseMessage> responseType)
    throws IOException;

    /**
     * Sends the specified response over the transport.
     *
     * @param   message
     *          The message to send.
     *
     * @throws  IOException
     *          If the transport connection fails for any reason, such as an
     *          interrupted connection or any other type of unexpected
     *          connection drop-out or failure.
     */
    public abstract void sendResponse(ResponseMessage message)
    throws IOException;

    /**
     * Polls the transport for any pending, unacknowledged messages.
     *
     * @throws  IOException
     *          If the transport connection fails for any reason, such as an
     *          interrupted connection or any other type of unexpected
     *          connection drop-out or failure.
     */
    public abstract void pollForMessages()
    throws IOException;

    /**
     * Closes the current transport connection, if it is open.
     */
    public abstract void close();

    /**
     * Registers the specified listener to be informed whenever requests are
     * received over the transport.
     *
     * @param   listener
     *          The listener that will be notified when requests are received.
     */
    public abstract void registerRequestListener(MessageListener<RequestMessage> listener);

    /**
     * Unregisters the specified listener from being informed whenever requests
     * are received over the transport.
     *
     * @param   listener
     *          The listener that will no longer be notified when requests are
     *          received.
     */
    public abstract void unregisterRequestListener(MessageListener<RequestMessage> listener);

    /**
     * Registers the specified listener to be informed whenever responses are
     * received over the transport.
     *
     * @param   listener
     *          The listener that will be notified when one or more responses
     *          are received.
     */
    public abstract void registerResponseListener(MessageListener<ResponseMessage> listener);

    /**
     * Unregisters the specified listener from being informed whenever
     * responses are received over the transport.
     *
     * @param   listener
     *          The listener that will no longer be notified when responses are
     *          received.
     */
    public abstract void unregisterResponseListener(MessageListener<ResponseMessage> listener);
}
