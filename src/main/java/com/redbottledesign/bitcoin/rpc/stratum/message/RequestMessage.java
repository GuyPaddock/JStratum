package com.redbottledesign.bitcoin.rpc.stratum.message;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.redbottledesign.bitcoin.rpc.stratum.MalformedStratumMessageException;

/**
 * <p>Java representation of a Stratum request message.</p>
 *
 * <p>Request messages must include the following:</p>
 *
 * <ul>
 *  <li>an {@code id} field, but it may be {@code null} if a response to the
 *      request is not expected or required.</li>
 *
 *  <li>a {@code method} field, which must not be {@code null} and must specify
 *      the name of the method being invoked.</li>
 *
 *  <li>a {@code params} field, which can be an empty array if the method being
 *      invoked takes no parameters.</li>
 * </ul>
 *
 * <p>© 2013 - 2014 RedBottle Design, LLC.</p>
 *
 * @author Guy Paddock (gpaddock@redbottledesign.com)
 */
public class RequestMessage
extends Message
{
    /**
     * Constant for the name of the {@code method} field in the JSON object for
     * this message.
     */
    protected static final String JSON_STRATUM_KEY_METHOD = "method";

    /**
     * Constant for the name of the {@code params} field in the JSON object for
     * this message.
     */
    protected static final String JSON_STRATUM_KEY_PARAMS = "params";

    /**
     * Static counter for generating unique Stratum request IDs.
     */
    private static AtomicLong nextRequestId;

    /**
     * The name of the method being invoked.
     */
    private String methodName;

    /**
     * The list of parameters being supplied to the method.
     */
    private List<Object> params;

    static
    {
        nextRequestId = new AtomicLong(1);
    }

    /**
     * Gets a unique identifier than can be used to identify the next Stratum
     * request.
     *
     * @return  A unique identifier for the next Stratum request.
     */
    public static String getNextRequestId()
    {
        return Long.toString(nextRequestId.getAndIncrement());
    }

    /**
     * Constructor for {@link RequestMessage} that initializes a new
     * instance from information in the included JSON message.
     *
     * @param   jsonMessage
     *          The JSON message object.
     *
     * @throws  MalformedStratumMessageException
     *          If the provided JSON message object is not a properly-formed
     *          Stratum message or cannot be understood.
     */
    public RequestMessage(JSONObject jsonMessage)
    throws MalformedStratumMessageException
    {
        super(jsonMessage);
    }

    /**
     * Constructor for {@link RequestMessage} that initializes a new
     * instance having the specified ID, method, and parameters.
     *
     * @param   id
     *          The unique identifier for the message. This may be
     *          {@code null}.
     *
     * @param   methodName
     *          The name of the method being invoked on the remote side.
     *          This cannot be {@code null}.
     *
     * @param   params
     *          The parameters being passed to the method.
     *
     * @throws  IllegalArgumentException
     *          If {@code methodName} is {@code null}.
     */
    public RequestMessage(String id, String methodName, Object... params)
    throws IllegalArgumentException
    {
        super(id);

        this.setMethodName(methodName);
        this.setParams(Arrays.asList(params));
    }

    /**
     * Gets the name of the method being invoked.
     *
     * @return  The name of the method.
     */
    public String getMethodName()
    {
        return this.methodName;
    }

    /**
     * Gets the list of parameters being passed to the remote method.
     *
     * @return  An unmodifiable copy of the parameters being passed to the
     *          remote method.
     */
    public List<Object> getParams()
    {
        return Collections.unmodifiableList(this.params);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JSONObject toJson()
    {
        JSONObject object = super.toJson();

        try
        {
            object.put(JSON_STRATUM_KEY_METHOD, this.getMethodName());
            object.put(JSON_STRATUM_KEY_PARAMS, new JSONArray());

            for (Object param : this.getParams())
            {
                object.append(JSON_STRATUM_KEY_PARAMS, param);
            }
        }

        catch (JSONException ex)
        {
            // Should not happen
            throw new RuntimeException("Unexpected exception while contructing JSON object: " + ex.getMessage(), ex);
        }

        return object;
    }

    /**
     * Sets the name of the method being invoked.
     *
     * @param   methodName
     *          The name of the method.
     */
    protected void setMethodName(String methodName)
    {
        if (methodName == null)
            throw new IllegalArgumentException("methodName cannot be null.");

        if (methodName.isEmpty())
            throw new IllegalArgumentException("methodName cannot be an empty string.");

        this.methodName = methodName;
    }

    /**
     * Sets the list of parameters being passed to the remote method.
     *
     * @param   params
     *          The list of parameters. The list is used live, in-place.
     */
    protected void setParams(List<Object> params)
    {
        this.params = params;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void parseMessage(JSONObject jsonMessage)
    throws MalformedStratumMessageException
    {
        this.parseMethodName(jsonMessage);
        this.parseParams(jsonMessage);

        // Call superclass last, since it calls validateParsedData()
        super.parseMessage(jsonMessage);
    }

    /**
     * Parses-out the {@code method} field from the message.
     *
     * @param   jsonMessage
     *          The message to parse.
     *
     * @throws  MalformedStratumMessageException
     *          If the provided JSON message object is not a properly-formed
     *          Stratum message or cannot be understood.
     */
    protected void parseMethodName(JSONObject jsonMessage)
    throws MalformedStratumMessageException
    {
        String methodName;

        if (!jsonMessage.has(JSON_STRATUM_KEY_METHOD))
        {
            throw new MalformedStratumMessageException(
                jsonMessage, String.format("missing '%s'", JSON_STRATUM_KEY_METHOD));
        }

        try
        {
            methodName = jsonMessage.getString(JSON_STRATUM_KEY_METHOD);
        }

        catch (JSONException ex)
        {
            throw new MalformedStratumMessageException(jsonMessage, ex);
        }

        if (methodName.isEmpty())
        {
            throw new MalformedStratumMessageException(
                jsonMessage, String.format("empty '%s'", JSON_STRATUM_KEY_METHOD));
        }

        this.setMethodName(methodName);
    }

    /**
     * Parses-out the {@code params} field from the message.
     *
     * @param   jsonMessage
     *          The message to parse.
     *
     * @throws  MalformedStratumMessageException
     *          If the provided JSON message object is not a properly-formed
     *          Stratum message or cannot be understood.
     */
    protected void parseParams(JSONObject jsonMessage)
    throws MalformedStratumMessageException
    {
        List<Object>    params      = new ArrayList<>();
        JSONArray       jsonParams;

        if (!jsonMessage.has(JSON_STRATUM_KEY_PARAMS))
        {
            throw new MalformedStratumMessageException(
                jsonMessage, String.format("missing '%s'", JSON_STRATUM_KEY_PARAMS));
        }

        try
        {
            jsonParams = jsonMessage.getJSONArray(JSON_STRATUM_KEY_PARAMS);

            for (int paramIndex = 0; paramIndex < jsonParams.length(); ++paramIndex)
            {
                params.add(jsonParams.get(paramIndex));
            }
        }

        catch (JSONException ex)
        {
            throw new MalformedStratumMessageException(jsonMessage, ex);
        }

        this.setParams(params);
    }
}