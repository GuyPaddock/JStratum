package com.redbottledesign.bitcoin.rpc.stratum.message;

import com.redbottledesign.bitcoin.rpc.stratum.MalformedStratumMessageException;
import org.json.JSONArray;

/**
 * <p>Factory for transforming a Stratum result from a JSON object into the
 * appropriate {@link Result} object.</p>
 *
 * <p>© 2013 - 2014 RedBottle Design, LLC.</p>
 * <p>© 2020 Inveniem.</p>
 *
 * @author Guy Paddock (guy@inveniem.com)
 */
public class ResultFactory {
  /**
   * The singleton instance of this factory.
   */
  private static final ResultFactory INSTANCE = new ResultFactory();

  /**
   * Gets an instance of this factory.
   *
   * @return The current factory instance.
   */
  public static ResultFactory getInstance() {
    return INSTANCE;
  }

  /**
   * Instantiates the appropriate {@link Result} object to parse the provided JSON object and wrap
   * its value.
   *
   * @param jsonObject
   *   The JSON object to parse.
   *
   * @return The appropriate result object.
   *
   * @throws MalformedStratumMessageException
   *   If the data parsed into the result from the provided JSON was incorrect for the type of
   *   information that was expected.
   */
  public Result createResult(final Object jsonObject)
  throws MalformedStratumMessageException {
    final Result result;

    if (jsonObject instanceof JSONArray) {
      // Single array response
      result = new ArrayResult((JSONArray)jsonObject);
    }
    else {
      // Single-value responses (boolean, string, etc)
      result = new ValueResult<>(jsonObject);
    }

    return result;
  }
}
