package com.redbottledesign.bitcoin.rpc.stratum.message;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import com.redbottledesign.bitcoin.rpc.stratum.MalformedStratumMessageException;

/**
 * <p>{@link Result} handler for a result that's an array of data,
 * including an optional <em>subject tuple</em>.</p>
 *
 * <p>The subject tuple consists of a string that identifies the context for
 * the message, along with an optional <em>subject key</em> that the recipient
 * can use to refer to the context at a later time in the Stratum
 * conversation.</p>
 *
 * <p>In JSON, with the subject tuple present, this type of result can look
 * like either of the following:</p>
 * {@code [["subject", "subject key"], "first datum", 2, "third datum"]}<br />
 * {@code [["subject"], "first datum", 2, "third datum"]}
 *
 * <p>As shown above, the subject key is optional. If the subject is omitted,
 * the subject key must also be omitted.</p>
 *
 * <p>For example, in the mining protocol implementation of Stratum, a valid
 * result in response to the {@code mining.subscribe} method call can look like
 * the following:</p>
 * {@code [["mining.notify", "ae6812eb4cd7735a302a8a9dd95cf71f"], "08000002", 4]}
 *
 * <p>Results of other Stratum operations can omit the subject tuple entirely.
 * For example, in the Electrum implementation of Stratum, a valid result in
 * response to the {@code blockchain.address.get_history} method call can look
 * like the following:</p>
 * {@code ["1DiiVSnksihdpdP1Pex7jghMAZffZiBY9q"]}
 *
 * <p>© 2013 - 2014 RedBottle Design, LLC.</p>
 *
 * @author Guy Paddock (guy.paddock@redbottledesign.com)
 */
public class ArrayResult
implements Result
{
    /**
     * An optional subject that can provide the receiving party with additional
     * context about this result.
     */
    private String subject;

    /**
     * An optional unique identifier for the specific subject of this result,
     * which the recipient can use to refer to the context at a later time in
     * the Stratum conversation.</p>
     */
    private String subjectKey;

    /**
     * A list of the data contained in this result (after the subject tuple).
     */
    private List<Object> resultData;

    /**
     * <p>Constructor for {@link ArrayResult} that initializes a new
     * instance having no subject and the specified result data.</p>
     *
     * @param   resultData
     *          Data, if any, that should be contained in the result (after
     *          the subject tuple).
     */
    public ArrayResult(Object... resultData)
    {
        this(null, null, resultData);
    }

    /**
     * <p>Constructor for {@link ArrayResult} that initializes a new
     * instance having the specified subject, subject key, and result data.</p>
     *
     * <p>All arguments may be {@code null}.</p>
     *
     * @param   subject
     *          An optional subject that can provide the receiving party with
     *          additional context about the result.
     *
     * @param   subjectKey
     *          An optional unique identifier for the specific subject of the
     *          result, which the recipient can use to refer to the context at
     *          a later time in the Stratum conversation. This must be
     *          {@code null} if {@code subject} is {@code null}.
     *
     * @param   resultData
     *          Data, if any, that should be contained in the result (after
     *          the subject tuple).
     *
     * @throws  IllegalArgumentException
     *          If a non-{@code null} value is provided for {@code subjectKey}
     *          when {@code subject} is {@code null}.
     */
    public ArrayResult(String subject, String subjectKey, Object... resultData)
    throws IllegalArgumentException
    {
        this.setSubject(subject);
        this.setSubjectKey(subjectKey);
        this.setResultData(Arrays.asList(resultData));
    }

    /**
     * Constructor for {@link ArrayResult} that initializes a new
     * instance from information in the included JSON array.
     *
     * @param   jsonResultData
     *          The JSON result data.
     *
     * @throws  MalformedStratumMessageException
     *          If the provided JSON message object is not a properly-formed
     *          Stratum message or cannot be understood.
     */
    public ArrayResult(JSONArray jsonResultData)
    throws MalformedStratumMessageException
    {
        this.parseResult(jsonResultData);
    }

    /**
     * Gets the optional subject that can provide the receiving party with additional
     * context about this result.
     *
     * @return  Either the subject of this message; or {@code null} if no
     *          subject was specified.
     */
    public String getSubject()
    {
        return this.subject;
    }

    /**
     * Gets the optional unique identifier for the specific subject of this result,
     * which the recipient can use to refer to the context at a later time in
     * the Stratum conversation.
     *
     * @return  Either the subject of this message; or {@code null} if no
     *          subject was specified.
     */
    public String getSubjectKey()
    {
        return this.subjectKey;
    }

    /**
     * Gets a copy of the data contained in this result (after the subject tuple).
     *
     * @return  An unmodifiable copy of the data contained in this result, if any.
     */
    public List<Object> getResultData()
    {
        return Collections.unmodifiableList(this.resultData);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JSONArray toJson()
    {
        JSONArray   result      = new JSONArray();
        String      subject     = this.getSubject();
        Object      subjectKey  = this.getSubjectKey();

        if (subject != null)
        {
            JSONArray subjectTuple = new JSONArray();

            subjectTuple.put(this.getSubject());

            if (subjectKey != null)
                subjectTuple.put(subjectKey);

            result.put(subjectTuple);
        }

        for (Object datum : this.getResultData())
        {
            result.put(datum);
        }

        return result;
    }

    /**
     * <p>Sets the optional subject that can provide the receiving party with
     * additional context about this result.</p>
     *
     * <p>If the subject is being set to {@code null}, the subject key must not
     * be set to any value other than {@code null}.</p>
     *
     * @param   subject
     *          The new subject for this message. This can be {@code null}.
     *
     * @throws  IllegalArgumentException
     *          If a {@code null} value is provided for {@code subject} when
     *          the subject key of this result is not {@code null}.
     */
    protected void setSubject(String subject)
    throws IllegalArgumentException
    {
        if ((subject == null) && (this.getSubjectKey() != null))
            throw new IllegalArgumentException("Subject cannot be set to null if subject key is not null.");

        this.subject = subject;
    }

    /**
     * <p>Sets the optional unique identifier for the specific subject of this
     * result, which the recipient can use to refer to the context at a later
     * time in the Stratum conversation.</p>
     *
     * <p>If the subject is {@code null}, the subject key cannot be set to any
     * value other than {@code null}.</p>
     *
     * @param   subjectKey
     *          The new subject key for this message. This can be {@code null}.
     *
     * @throws  IllegalArgumentException
     *          If a non-{@code null} value is provided for {@code subjectKey}
     *          when the subject of this result is {@code null}.
     */
    protected void setSubjectKey(String subjectKey)
    throws IllegalArgumentException
    {
        if ((subjectKey != null) && (this.getSubject() == null))
            throw new IllegalArgumentException("Subject key cannot be set if subject is null.");

        this.subjectKey = subjectKey;
    }

    /**
     * Sets the list of the data contained in this result (after the subject
     * tuple).
     *
     * @param   resultData
     *          The new list of result data.
     */
    protected void setResultData(List<Object> resultData)
    {
        this.resultData = resultData;
    }

    /**
     * Parses the provided JSON array as a Stratum array result and then
     * populates this result accordingly with its contents.
     *
     * @param   jsonResultData
     *          The array of result data to parse.
     *
     * @throws  MalformedStratumMessageException
     *          If the provided JSON array is not a properly-formed Stratum
     *          result or cannot be understood.
     */
    protected void parseResult(JSONArray jsonResultData)
    throws MalformedStratumMessageException
    {
        List<Object> resultData     = new ArrayList<>();
        int          startingIndex  = 0;

        // Skip the subject tuple if its present.
        if (this.parseOptionalSubjectTuple(jsonResultData))
            startingIndex = 1;

        for (int arrayIndex = startingIndex; arrayIndex < jsonResultData.length(); ++arrayIndex)
        {
            Object datum;

            try
            {
                datum = jsonResultData.get(arrayIndex);
            }

            catch (JSONException ex)
            {
                // Should not happen
                throw new RuntimeException(
                    "Unexpected exception while retrieving JSON element: " + ex.getMessage(), ex);
            }

            resultData.add(datum);
        }

        this.setResultData(resultData);
    }

    /**
     * Determines whether or not the provided JSON result data array contains
     * a subject tuple, parses it if it is present, and then populates the
     * subject information of this result accordingly with its contents.
     *
     * @param   jsonResultData
     *          The array of result data to parse.
     *
     * @throws  MalformedStratumMessageException
     *          If the provided JSON array does not contain a properly-formed
     *          Stratum result subject tuple, or cannot be understood.
     *
     * @return  {@code true} if a subject tuple was present and parsed;
     *          {@code false}, otherwise.
     */
    protected boolean parseOptionalSubjectTuple(JSONArray jsonResultData)
    throws MalformedStratumMessageException
    {
        boolean wasParsed = false;

        if ((jsonResultData.length() >= 1))
        {
            JSONArray subjectTuple = jsonResultData.optJSONArray(0);

            if (subjectTuple != null)
            {
                if (subjectTuple.length() > 0)
                {
                    try
                    {
                        this.setSubject(subjectTuple.getString(0));
                        this.setSubjectKey(subjectTuple.getString(1));
                    }

                    catch (JSONException ex)
                    {
                        throw new MalformedStratumMessageException(jsonResultData, "malformed subject tuple");
                    }
                }

                wasParsed = true;
            }
        }
        return wasParsed;
    }
}
