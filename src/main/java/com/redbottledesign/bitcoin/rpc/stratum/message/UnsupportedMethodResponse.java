package com.redbottledesign.bitcoin.rpc.stratum.message;

/**
 * A response message that indicates that a method being requested is not
 * supported.
 */
public class UnsupportedMethodResponse
extends ResponseMessage {
  /**
   * Constructor for {@link UnsupportedMethodResponse} that initializes a new instance having the
   * specified ID for the specified method that was not supported.
   *
   * @param id
   *   The unique identifier for the message. This may be {@code null}.
   * @param methodName
   *   The name of the method that was not supported.
   */
  public UnsupportedMethodResponse(final String id, final String methodName) {
    super(id, "Method not supported: " + methodName);
  }
}
