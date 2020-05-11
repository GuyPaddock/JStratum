package com.redbottledesign.bitcoin.rpc.stratum;

import com.redbottledesign.bitcoin.rpc.stratum.message.RequestMessage;
import org.json.JSONObject;

/**
 * Exception thrown when there is no handler registered for a specific type of Stratum message.
 *
 * <p>© 2020 Inveniem.</p>
 *
 * @author Guy Paddock (guy@inveniem.com)
 */
public class UnhandledStratumMessageException
extends Exception {
  /**
   * Serial version ID.
   */
  private static final long serialVersionUID = -5169028037089628215L;

  /**
   * The Stratum request that went unhandled.
   */
  private final RequestMessage unhandledRequest;

  /**
   * Initializes a new exception that indicates that the specified Stratum request was not handled.
   *
   * @param request
   *   The Stratum request that went unhandled.
   */
  public UnhandledStratumMessageException(final RequestMessage request) {
    super(
      String.format(
        "No handler registered for method \"%s\", specified by Stratum JSON message: %s",
        request.getMethodName(),
        request.toJson().toString()
      )
    );

    this.unhandledRequest = request;
  }

  /**
   * @return
   *   The Stratum request that went unhandled.
   */
  public RequestMessage getUnhandledRequest() {
    return this.unhandledRequest;
  }
}
