package com.redbottledesign.bitcoin.rpc.stratum.message;

/**
 * <p>Interface abstraction for the types of data that can be included in the
 * result field of a {@link ResponseMessage}.</p>
 *
 * <p>© 2013 - 2014 RedBottle Design, LLC.</p>
 * <p>© 2020 Inveniem.</p>
 *
 * @author Guy Paddock (guy@inveniem.com)
 */
public interface Result {
  /**
   * <p>Converts this result into its equivalent representation in JSON.</p>
   *
   * @return The JSON representation of this Stratum result.
   */
  Object toJson();
}
