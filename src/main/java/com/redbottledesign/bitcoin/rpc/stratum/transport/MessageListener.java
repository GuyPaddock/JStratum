package com.redbottledesign.bitcoin.rpc.stratum.transport;

import com.redbottledesign.bitcoin.rpc.stratum.message.Message;

/**
 * <p>Abstract interface for objects interested in being notified when one or
 * more messages of the specified type are received over a Stratum
 * {@link MessageTransport}.</p>
 *
 * <p>© 2013 - 2014 RedBottle Design, LLC.</p>
 *
 * @param   <T>
 *          The type of message the listener is handling.
 *
 * @author  Guy Paddock (guy.paddock@redbottledesign.com)
 */
public interface MessageListener<T extends Message>
{
    /**
     * Method invoked when a message is received over a message transport.
     *
     * @param   message
     *          The message that was received.
     */
    public abstract void onMessageReceived(T message);
}
