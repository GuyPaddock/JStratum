package com.redbottledesign.bitcoin.rpc.stratum.message;

import org.json.JSONException;
import org.json.JSONObject;

import com.redbottledesign.bitcoin.rpc.stratum.MalformedStratumMessageException;

/**
 * <p>Abstract parent class for both request and response Stratum messages.</p>
 *
 * <p>At a minimum, all Stratum messages have a numeric {@code id} field, but
 * it may be {@code null}.</li>
 *
 * <p>© 2013 - 2014 RedBottle Design, LLC.</p>
 *
 * @author Guy Paddock (gpaddock@redbottledesign.com)
 */
public abstract class Message
{
    /**
     * Constant for the name of the {@code id} field in the JSON object for
     * this message.
     */
    protected static final String JSON_STRATUM_KEY_ID = "id";

    /**
     * <p>The unique identifier for this message, relative to the side
     * of the connection that initiated the request.</p>
     *
     * <p>This may be {@code null} if the side of the connection that initiated
     * the request does not expect a response.</p>
     */
    private String id;

    /**
     * Constructor for {@link Message} that initializes a new instance
     * from information in the included JSON message.
     *
     * @param   jsonMessage
     *          The JSON message object.
     *
     * @throws  MalformedStratumMessageException
     *          If the provided JSON message object is not a properly-formed
     *          Stratum message or cannot be understood.
     */
    public Message(JSONObject jsonMessage)
    throws MalformedStratumMessageException
    {
        this.parseMessage(jsonMessage);
    }

    /**
     * Constructor for {@link Message} that initializes a new instance
     * having the specified ID.
     *
     * @param   id
     *          The unique identifier for the message. This may be
     *          {@code null}.
     */
    protected Message(String id)
    {
        this.setId(id);
    }

    /**
     * <p>Gets the unique identifier for this message, relative to the side
     * of the connection that initiated the request.</p>
     *
     * <p>This may be {@code null} if the side of the connection that initiated
     * the request does not expect a response.</p>
     *
     * @return  The unique identifier for the message; or {@code null} if none was
     *          provided.
     */
    public String getId()
    {
        return this.id;
    }

    /**
     * <p>Converts this message into its equivalent representation in JSON.</p>
     *
     * <p>Sub-classes should override this method to implement JSON
     * serialization of any additional fields that they expect in the JSON
     * message.</p>
     *
     * @return  The JSON representation of this Stratum message.
     */
    public JSONObject toJson()
    {
        JSONObject object = new JSONObject();

        try
        {
            object.put(JSON_STRATUM_KEY_ID, this.getId());
        }

        catch (JSONException ex)
        {
            // Should not happen
            throw new RuntimeException("Unexpected exception while contructing JSON object: " + ex.getMessage(), ex);
        }

        return object;
    }

    /**
     * <p>Sets the unique identifier for this message, relative to the side
     * of the connection that initiated the request.</p>
     *
     * <p>This may be {@code null} if the side of the connection that initiated
     * the request does not expect a response.</p>
     *
     * @param   id
     *          Sets the unique identifier for the message. This may be {@code null}.
     */
    protected void setId(String id)
    {
        this.id = id;
    }

    /**
     * <p>Parses the provided JSON message as a Stratum message and then
     * populates this message accordingly with its contents.</p>
     *
     * <p>Sub-classes should override this method to implement parsing for any
     * additional fields that they expect in the JSON message.</p>
     *
     * @param   jsonMessage
     *          The message to parse.
     *
     * @throws  MalformedStratumMessageException
     *          If the provided JSON message object is not a properly-formed
     *          Stratum message or cannot be understood.
     */
    protected void parseMessage(JSONObject jsonMessage)
    throws MalformedStratumMessageException
    {
        this.parseId(jsonMessage);

        this.validateParsedData(jsonMessage);
    }

    /**
     * <p>Validates that all of the information that was parsed into this
     * message is valid.</p>
     *
     * <p>This is automatically invoked by {@link #parseMessage(JSONObject)}. The
     * default behavior provides no validation logic, but sub-classes can
     * override this method to provide their own validation logic.</p>
     *
     * @param   jsonMessage
     *          The message that was parsed.
     *
     * @throws  MalformedStratumMessageException
     *          If the data parsed into this object from the provided message
     *          was incorrect for the type of message that was expected.
     */
    protected void validateParsedData(JSONObject jsonMessage)
    throws MalformedStratumMessageException
    {
        // Default: do nothing.
    }

    /**
     * <p>Parses the {@code id} field out of the provided JSON message.</p>
     *
     * <p>All Stratum messages are expected to have an {@code id} field present
     * in the message object, though it can be {@code null}.</p>
     *
     * @param   jsonMessage
     *          The message to parse.
     *
     * @throws  MalformedStratumMessageException
     *          If the data parsed into this object from the provided message
     *          was incorrect for the type of message that was expected.
     */
    protected void parseId(JSONObject jsonMessage)
    throws MalformedStratumMessageException
    {
        Object id;

        if (!jsonMessage.has(JSON_STRATUM_KEY_ID))
            throw new MalformedStratumMessageException(jsonMessage, String.format("missing '%s'", JSON_STRATUM_KEY_ID));

        try
        {
            id = jsonMessage.get(JSON_STRATUM_KEY_ID);
        }

        catch (JSONException ex)
        {
            throw new MalformedStratumMessageException(jsonMessage, ex);
        }

        if (id != null)
            this.setId(id.toString());

        else
            this.setId(null);
    }
}
