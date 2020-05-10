package com.redbottledesign.bitcoin.rpc.stratum.message;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalCause;
import com.google.common.cache.RemovalListener;
import com.redbottledesign.bitcoin.rpc.stratum.MalformedStratumMessageException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * <p>Class for marshalling Stratum messages into and out of JSON format.</p>
 *
 * <p>© 2013 - 2014 RedBottle Design, LLC.</p>
 * <p>© 2020 Inveniem.</p>
 *
 * @author Guy Paddock (guy@inveniem.com)
 */
public class MessageMarshaller {
  /**
   * The logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(MessageMarshaller.class);

  /**
   * The number of minutes that a request can be pending in a marshaller instance before it is
   * considered "ignored" (i.e. will not receive a response).
   */
  protected static int IGNORED_REQUEST_TIMEOUT_MINUTES = 10;

  /**
   * <p>The name of the key in a Stratum message object that holds the result
   * of an operation.</p>
   *
   * <p>Messages that do not have this key are assumed to be request messages,
   * while messages that do have it are assumed to be responses.</p>
   */
  protected static final String JSON_MESSAGE_KEY_RESULT = "result";

  /**
   * The map of method names to request message types.
   */
  protected Map<String, Class<? extends RequestMessage>> requestMethodMap;

  /**
   * The map of waiting requests to response message types.
   *
   * <p>This map is purged as requests are answered or time-out.</p>
   */
  @SuppressWarnings("UnstableApiUsage")
  protected Cache<String, Class<? extends ResponseMessage>> requestResponseMap;

  /**
   * The constructor for {@link MessageMarshaller}.
   */
  public MessageMarshaller() {
    this.requestMethodMap   = new HashMap<>();
    this.requestResponseMap = this.createRequestResponseMap();
  }

  /**
   * Registers a concrete {@link RequestMessage} type as the appropriate message for un-marshalling
   * requests for the specified Stratum method.
   *
   * @param methodName
   *   The name of the method, as it appears in requests.
   * @param messageType
   *   The type of concrete message type for the method.
   */
  public void registerRequestHandler(final String methodName,
                                     final Class<? extends RequestMessage> messageType) {
    this.requestMethodMap.put(methodName, messageType);
  }

  /**
   * Gets the appropriate concrete message type to handle the specified Stratum method.
   *
   * @param methodName
   *   The name of the method for which a message type is desired.
   *
   * @return The concrete message type that is used to handle the specified method; or {@code null}
   *   if this instance does not have any registered handlers for the specified method.
   */
  public Class<? extends Message> getRequestHandlerForMethod(final String methodName) {
    return this.requestMethodMap.get(methodName);
  }

  /**
   * Registers the request having the specified ID as pending a response message of the specified
   * type.
   *
   * @param requestId
   *   The unique identifier for the request.
   * @param responseType
   *   The type of response message expected from the request.
   *
   * @throws IllegalArgumentException
   *   If {@code requestId} matches a request that is already registered as pending with this
   *   instance.
   */
  @SuppressWarnings("UnstableApiUsage")
  public void registerPendingRequest(final String requestId,
                                     final Class<? extends ResponseMessage> responseType)
  throws IllegalArgumentException {
    if (this.requestResponseMap.getIfPresent(requestId) != null) {
      throw new IllegalArgumentException(
        String.format(
          "A request with ID #%s is already pending.",
          requestId
        )
      );
    }

    this.requestResponseMap.put(requestId, responseType);
  }

  /**
   * Marshals the specified string of strictly-formatted JSON data into one or more concrete Stratum
   * messages.
   *
   * @param jsonString
   *   The string of JSON data.
   *
   * @return The list of one or more concrete {@link Message}s that represent the information from
   *   the provided JSON message.
   *
   * @throws MalformedStratumMessageException
   *   If any JSON information is invalid or doesn't match the messages that this marshaller was
   *   expecting or is capable of marshalling.
   */
  public List<Message> marshalMessages(final String jsonString)
  throws MalformedStratumMessageException {
    final List<Message> results;

    try {
      if (jsonString.startsWith("[")) {
        results = this.marshalMessages(new JSONArray(jsonString));
      }
      else {
        results = Collections.singletonList(this.marshalMessage(new JSONObject(jsonString)));
      }
    } catch (JSONException ex) {
      throw new MalformedStratumMessageException(jsonString, ex);
    }

    return results;
  }

  /**
   * Marshals the specified array of JSON messages into concrete Stratum messages.
   *
   * @param jsonMessages
   *   The JSON array containing each message as a JSONObject.
   *
   * @return The list of concrete {@link Message}s that represent the information from the provided
   * JSON message.
   *
   * @throws MalformedStratumMessageException
   *   If any JSON information is invalid or doesn't match the messages that this marshaller was
   *   expecting or is capable of marshalling.
   */
  public List<Message> marshalMessages(final JSONArray jsonMessages)
  throws MalformedStratumMessageException {
    final List<Message> results = new LinkedList<>();

    for (int messageIndex = 0; messageIndex < jsonMessages.length(); ++messageIndex) {
      try {
        final JSONObject jsonMessage = jsonMessages.getJSONObject(messageIndex);

        results.add(this.marshalMessage(jsonMessage));
      } catch (JSONException ex) {
        throw new MalformedStratumMessageException(jsonMessages, ex);
      }
    }

    return results;
  }

  /**
   * Marshals the specified JSON message into a concrete Stratum message.
   *
   * @param jsonMessage
   *   The JSON object containing the full message.
   *
   * @return A concrete {@link Message} that represents the information from the provided JSON
   *   message.
   *
   * @throws MalformedStratumMessageException
   *   If the JSON information is invalid or doesn't match any of the messages that this marshaller
   *   was expecting or is capable of marshalling.
   */
  public Message marshalMessage(final JSONObject jsonMessage)
  throws MalformedStratumMessageException {
    final Message result;

    if (jsonMessage.has(JSON_MESSAGE_KEY_RESULT)) {
      result = marshalResponseMessage(jsonMessage);
    }
    else {
      result = marshalRequestMessage(jsonMessage);
    }

    return result;
  }

  /**
   * Un-marshals the specified Stratum message to a String of JSON data.
   *
   * @param message
   *   The Stratum message to unmarshal.
   *
   * @return A String that represents the message in strict JSON.
   */
  public String unmarshalMessage(final Message message) {
    return message.toJson().toString();
  }

  /**
   * Marshals the specified JSON message into a concrete Stratum request message.
   *
   * @param jsonMessage
   *   The JSON object containing the full request message.
   *
   * @return A concrete {@link Message} that represents the request information from the provided
   *   JSON message.
   *
   * @throws MalformedStratumMessageException
   *   If the JSON information is invalid or doesn't match any of the requests that this marshaller
   *   was expecting or is capable of marshalling.
   */
  protected Message marshalRequestMessage(final JSONObject jsonMessage)
  throws MalformedStratumMessageException {
    final Message                  result;
    final RequestMessage           request     = new RequestMessage(jsonMessage);
    final String                   methodName  = request.getMethodName();
    final Class<? extends Message> requestType = this.requestMethodMap.get(methodName);

    if (requestType == null) {
      throw new MalformedStratumMessageException(methodName, jsonMessage);
    }
    else {
      result = this.marshalMessage(jsonMessage, requestType);
    }

    return result;
  }

  /**
   * Marshals the specified JSON message into a concrete Stratum response message.
   *
   * @param jsonMessage
   *   The JSON object containing the full response message.
   *
   * @return A concrete {@link Message} that represents the response information from the provided
   *   JSON message.
   *
   * @throws MalformedStratumMessageException
   *   If the JSON information is invalid or doesn't match any of the responses that this marshaller
   *   was expecting or is capable of marshalling.
   */
  @SuppressWarnings("UnstableApiUsage")
  protected Message marshalResponseMessage(final JSONObject jsonMessage)
  throws MalformedStratumMessageException {
    final Message                  result;
    final ResponseMessage          response     = new ResponseMessage(jsonMessage);
    final String                   messageId    = response.getId(); // Must always be set in responses
    final Class<? extends Message> responseType = this.requestResponseMap.getIfPresent(messageId);

    if (responseType == null) {
      throw new MalformedStratumMessageException(jsonMessage);
    } else {
      result = this.marshalMessage(jsonMessage, responseType);

      this.requestResponseMap.invalidate(messageId);
    }

    return result;
  }

  /**
   * Marshals the specified JSON message into the specified type of Stratum message.
   *
   * @param jsonMessage
   *   The JSON object containing the message.
   * @param messageType
   *   The type of message into which the JSON data will be marshalled.
   *
   * @return A concrete {@link Message} that represents the information from the provided JSON
   *   message.
   *
   * @throws MalformedStratumMessageException
   *   If the JSON information is invalid.
   */
  protected Message marshalMessage(final JSONObject jsonMessage,
                                   final Class<? extends Message> messageType)
  throws MalformedStratumMessageException {
    final Message result;

    try {
      // NOTE: This can throw MalformedStratumMessageException
      result = messageType.getConstructor(JSONObject.class).newInstance(jsonMessage);
    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
             | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
      throw new RuntimeException(
        String.format(
          "Stratum message class '%s' is missing a constructor that takes a JSONObject.",
          messageType.getName()
        )
      );
    }

    return result;
  }

  /**
   * <p>Callback invoked when a request expires without a reply.</p>
   *
   * <p>The default behavior is only to log the expiration as an error.</p>
   *
   * @param messageId
   *   The unique identifier for the message.
   * @param expectedResponseType
   *   The type of response that was expected for the message.
   */
  protected void onRequestExpired(final String messageId,
                                  final Class<? extends ResponseMessage> expectedResponseType) {
    if (LOGGER.isErrorEnabled()) {
      LOGGER.error(
        String.format(
          "Request #%s (expecting a response of type '%s') was expired after %d minutes of being " +
          "ignored without receiving a reply.",
          messageId,
          expectedResponseType.getName(),
          IGNORED_REQUEST_TIMEOUT_MINUTES
        )
      );
    }
  }

  /**
   * Creates a new request response map, which holds requests that are waiting for responses.
   *
   * @return A new cache for waiting request responses.
   */
  @SuppressWarnings("UnstableApiUsage")
  protected Cache<String, Class<? extends ResponseMessage>> createRequestResponseMap() {
    return CacheBuilder
      .newBuilder()
      .expireAfterWrite(IGNORED_REQUEST_TIMEOUT_MINUTES, TimeUnit.MINUTES)
      .removalListener(
        (RemovalListener<String, Class<? extends ResponseMessage>>)((notification) -> {
          if (notification.getCause() == RemovalCause.EXPIRED) {
            MessageMarshaller.this.onRequestExpired(
              notification.getKey(),
              notification.getValue()
            );
          }
        })
      ).build();
  }
}
