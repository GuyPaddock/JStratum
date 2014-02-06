package com.redbottledesign.bitcoin.rpc.stratum.message;

import org.json.JSONArray;

import com.redbottledesign.bitcoin.rpc.stratum.MalformedStratumMessageException;

/**
 * <p>Factory for transforming a Stratum result from a JSON object into the
 * appropriate {@link Result} object.</p>
 *
 * <p>© 2013 - 2014 RedBottle Design, LLC.</p>
 *
 * @author Guy Paddock (guy.paddock@redbottledesign.com)
 */
public class ResultFactory
{
    /**
     * The singleton instance of this factory.
     */
    private static ResultFactory INSTANCE = new ResultFactory();

    /**
     * Gets an instance of this factory.
     *
     * @return  The current factory instance.
     */
    public static ResultFactory getInstance()
    {
        return INSTANCE;
    }

    /**
     * Instantiates the appropriate {@link Result} object to parse the
     * provided JSON object and wrap its value.
     *
     * @param   jsonObject
     *          The JSON object to parse.
     *
     * @return  The appropriate result object.
     *
     * @throws  MalformedStratumMessageException
     *          If the data parsed into the result from the provided JSON
     *          was incorrect for the type of information that was expected.
     */
    public Result createResult(Object jsonObject) throws MalformedStratumMessageException
    {
        Result result;

        // Single array response
        if (jsonObject instanceof JSONArray)
            result = new ArrayResult((JSONArray)jsonObject);

        // Single-value responses (boolean, string, etc)
        else
            result = new ValueResult<Object>(jsonObject);

        return result;
    }
}