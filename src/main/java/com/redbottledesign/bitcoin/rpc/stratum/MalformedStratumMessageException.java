package com.redbottledesign.bitcoin.rpc.stratum;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * <p>Exception thrown when JSON-encoded Stratum messages are either not recognized, malformed, or
 * otherwise invalid according to a specific Stratum protocol specification.</p>
 *
 * <p>© 2013 - 2014 RedBottle Design, LLC.</p>
 * <p>© 2020 Inveniem.</p>
 *
 * @author Guy Paddock (guy@inveniem.com)
 */
public class MalformedStratumMessageException
extends Exception {
  /**
   * Serial version ID.
   */
  private static final long serialVersionUID = -5169028037089628215L;

  /**
   * Initializes a new exception that indicates information in the specified JSON message is
   * malformed.
   *
   * @param jsonMessage
   *   The JSON message that is malformed.
   */
  public MalformedStratumMessageException(final JSONObject jsonMessage) {
    this(jsonMessage.toString());
  }

  /**
   * Initializes a new exception that indicates information in the specified JSON message is
   * malformed.
   *
   * @param jsonMessage
   *   The JSON message that is malformed, as a JSON {@code String}.
   */
  public MalformedStratumMessageException(final String jsonMessage) {
    super("Unknown or malformed Stratum JSON message received: " + jsonMessage);
  }

  /**
   * Initializes a new exception that indicates that information in the specified JSON message is
   * malformed in the specified way.
   *
   * @param jsonMessage
   *   The JSON message that is malformed.
   * @param error
   *   A description of the problem with the message.
   */
  public MalformedStratumMessageException(final JSONObject jsonMessage, final String error) {
    super(
      String.format(
        "Unknown or malformed Stratum JSON message received (%s): %s",
        error,
        jsonMessage.toString()
      )
    );
  }

  /**
   * Initializes a new exception that indicates that information in the specified JSON message is
   * malformed in a way that caused the specified exception.
   *
   * @param jsonMessage
   *   The JSON message that is malformed.
   * @param t
   *   The exception that occurred while processing the message.
   */
  public MalformedStratumMessageException(final JSONObject jsonMessage, final Throwable t) {
    this(jsonMessage.toString(), t);
  }

  /**
   * Initializes a new exception that indicates that information in the specified JSON message is
   * malformed in a way that caused the specified exception.
   *
   * @param jsonMessage
   *   The JSON message that is malformed, as a JSON {@code String}.
   * @param t
   *   The exception that occurred while processing the message.
   */
  public MalformedStratumMessageException(final String jsonMessage, final Throwable t) {
    super(
      String.format(
        "Unknown or malformed Stratum JSON message received (%s): %s",
        t.getMessage(),
        jsonMessage
      ),
      t
    );
  }

  /**
   * Initializes a new exception that indicates that the specified Stratum method was expected but
   * not properly interpreted from the specified message.
   *
   * @param method
   *   The name of the method that was being interpreted.
   * @param jsonMessage
   *   The JSON message that is malformed.
   */
  public MalformedStratumMessageException(final String method, final JSONObject jsonMessage) {
    super(
      String.format(
        "Unknown or malformed \"%s\" Stratum JSON message received: %s",
        method,
        jsonMessage.toString()
      )
    );
  }

  /**
   * Initializes a new exception that indicates that the specified Stratum method was expected but
   * not properly interpreted from the specified message.
   *
   * @param method
   *   The name of the method that was being interpreted.
   * @param error
   *   A description of the problem with the message.
   * @param jsonMessage
   *   The JSON message that is malformed.
   */
  public MalformedStratumMessageException(final String method, final String error,
                                          final JSONObject jsonMessage) {
    super(
      String.format(
        "Unknown or malformed \"%s\" Stratum JSON message received (%s): %s",
        method,
        error,
        jsonMessage.toString()
      )
    );
  }

  /**
   * Initializes a new exception that indicates that the specified Stratum method was expected but
   * not properly interpreted from the specified message, leading to the specified exception.
   *
   * @param method
   *   The name of the method that was being interpreted.
   * @param error
   *   A description of the problem with the message.
   * @param t
   *   The exception that occurred while processing the message.
   * @param jsonMessage
   *   The JSON message that is malformed.
   */
  public MalformedStratumMessageException(final String method, final String error,
                                          final Throwable t, final JSONObject jsonMessage) {
    super(
      String.format(
        "Unknown or malformed \"%s\" Stratum JSON message received (%s): %s",
        method,
        error,
        jsonMessage.toString()
      ),
      t
    );
  }

  /**
   * Initializes a new exception that indicates information in the specified JSON message is
   * malformed.
   *
   * @param jsonMessage
   *   The JSON message that is malformed.
   */
  public MalformedStratumMessageException(final JSONArray jsonMessage) {
    this(jsonMessage.toString());
  }

  /**
   * Initializes a new exception that indicates that information in the specified JSON message is
   * malformed in the specified way.
   *
   * @param jsonMessage
   *   The JSON message that is malformed.
   * @param error
   *   A description of the problem with the message.
   */
  public MalformedStratumMessageException(final JSONArray jsonMessage, final String error) {
    super(
      String.format(
        "Unknown or malformed Stratum JSON message received (%s): %s",
        error,
        jsonMessage.toString()
      )
    );
  }

  /**
   * Initializes a new exception that indicates that information in the specified JSON message is
   * malformed in a way that caused the specified exception.
   *
   * @param jsonMessage
   *   The JSON message that is malformed.
   * @param t
   *   The exception that occurred while processing the message.
   */
  public MalformedStratumMessageException(final JSONArray jsonMessage, final Throwable t) {
    super(
      String.format(
        "Unknown or malformed Stratum JSON message received (%s): %s",
        t.getMessage(),
        jsonMessage.toString()
      ),
      t
    );
  }

  /**
   * Initializes a new exception that indicates that the specified Stratum method was expected but
   * not properly interpreted from the specified message.
   *
   * @param method
   *   The name of the method that was being interpreted.
   * @param jsonMessage
   *   The JSON message that is malformed.
   */
  public MalformedStratumMessageException(final String method, final JSONArray jsonMessage) {
    super(
      String.format(
        "Unknown or malformed \"%s\" Stratum JSON message received: %s",
        method,
        jsonMessage.toString()
      )
    );
  }

  /**
   * Initializes a new exception that indicates that the specified Stratum method was expected but
   * not properly interpreted from the specified message.
   *
   * @param method
   *   The name of the method that was being interpreted.
   * @param error
   *   A description of the problem with the message.
   * @param jsonMessage
   *   The JSON message that is malformed.
   */
  public MalformedStratumMessageException(final String method, final String error,
                                          final JSONArray jsonMessage) {
    super(
      String.format(
        "Unknown or malformed \"%s\" Stratum JSON message received (%s): %s",
        method,
        error,
        jsonMessage.toString()
      )
    );
  }

  /**
   * Initializes a new exception that indicates that the specified Stratum method was expected but
   * not properly interpreted from the specified message, leading to the specified exception.
   *
   * @param method
   *   The name of the method that was being interpreted.
   * @param error
   *   A description of the problem with the message.
   * @param t
   *   The exception that occurred while processing the message.
   * @param jsonMessage
   *   The JSON message that is malformed.
   */
  public MalformedStratumMessageException(final String method, final String error,
                                          final Throwable t, final JSONArray jsonMessage) {
    super(
      String.format(
        "Unknown or malformed \"%s\" Stratum JSON message received (%s): %s",
        method,
        error,
        jsonMessage.toString()
      ),
      t
    );
  }
}
