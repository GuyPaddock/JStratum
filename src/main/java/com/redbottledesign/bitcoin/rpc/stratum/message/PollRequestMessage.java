package com.redbottledesign.bitcoin.rpc.stratum.message;

import org.json.JSONObject;

/**
 * <p>Special type of request message used by non-TCP transports to poll the
 * remote end for pending requests or responses.</p>
 *
 * <p>© 2013 - 2014 RedBottle Design, LLC.</p>
 *
 * @author Guy Paddock (guy.paddock@redbottledesign.com)
 */
public class PollRequestMessage
extends RequestMessage
{
    /**
     * Default constructor for {@link PollRequestMessage}.
     */
    public PollRequestMessage()
    {
        super(null, null);
    }

    /**
     * {@inheritDoc}
     *
     * <p>This override does nothing, and serves only to defeat the exception
     * that is otherwise produced by having a {@code null} method name in the
     * request message.</p>
     */
    @Override
    protected void setMethodName(String methodName)
    {
        // Do nothing; defeat exception in base class
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JSONObject toJson()
    {
        // Empty object
        return new JSONObject();
    }
}
