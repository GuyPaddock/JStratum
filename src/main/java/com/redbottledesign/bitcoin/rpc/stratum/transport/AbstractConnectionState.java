package com.redbottledesign.bitcoin.rpc.stratum.transport;

import com.redbottledesign.bitcoin.rpc.stratum.message.MessageMarshaller;
import com.redbottledesign.bitcoin.rpc.stratum.message.RequestMessage;
import com.redbottledesign.bitcoin.rpc.stratum.message.ResponseMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>Abstract base implementation for Stratum {@link ConnectionState}
 * implementations.</p>
 *
 * <p>This base implementation is optional. {@link ConnectionState} consumers
 * should only refer to the {@link ConnectionState} interface and never make a direct reference to
 * this class.</p>
 *
 * <p>© 2013 - 2014 RedBottle Design, LLC.</p>
 * <p>© 2020 Inveniem.</p>
 *
 * @author Guy Paddock (guy@inveniem.com)
 */
public abstract class AbstractConnectionState
implements ConnectionState {
  /**
   * The logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractConnectionState.class);

  /**
   * The Stratum message transport to which this state corresponds.
   */
  private final StatefulMessageTransport transport;

  /**
   * The marshaller that will be used in this state to receive and send messages.
   */
  private final MessageMarshaller marshaller;

  /**
   * The listener that this state will use to receive incoming request messages from the transport.
   */
  private final RequestListener requestListener;

  /**
   * The listener that this state will use to receive incoming response messages from the
   * transport.
   */
  private final ResponseListener responseListener;

  /**
   * The map from request message types to appropriate handlers.
   */
  private final Map<Class<? extends RequestMessage>, MessageListener<RequestMessage>> requestHandlers;

  /**
   * The map from response message types to appropriate handlers.
   */
  private final Map<Class<? extends ResponseMessage>, MessageListener<ResponseMessage>> responseHandlers;

  /**
   * <p>Constructor for {@link AbstractConnectionState}.</p>
   *
   * <p>Initializes a new instance that corresponds to the specified connection.</p>
   *
   * @param transport
   *   The Stratum message transport to which the state corresponds.
   */
  public AbstractConnectionState(StatefulMessageTransport transport) {
    this.transport  = transport;
    this.marshaller = this.createMarshaller();

    this.requestListener  = new RequestListener();
    this.responseListener = new ResponseListener();
    this.requestHandlers  = new HashMap<>();
    this.responseHandlers = new HashMap<>();

    this.initializeHandlers();
  }

  /**
   * <p>Populates the maps of request and response handlers to appropriately handle the types of
   * messages that will be received while in this state.</p>
   *
   * <p>This method is automatically invoked by the constructor.</p>
   *
   * <p>All states that expect to receive messages should override this method with an
   * implementation that makes appropriate calls to
   * {@link #registerRequestHandler(String, Class, MessageListener)} and
   * {@link #registerResponseHandler(Class, MessageListener)}.</p>
   */
  protected void initializeHandlers() {
  }

  /**
   * Gets the Stratum message transport to which this state corresponds.
   *
   * @return The message transport.
   */
  public StatefulMessageTransport getTransport() {
    return this.transport;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public MessageMarshaller getMarshaller() {
    return this.marshaller;
  }

  @Override
  public void start() {
    final StatefulMessageTransport transport = this.getTransport();

    // Inform this state of any messages received
    transport.registerRequestListener(this.requestListener);
    transport.registerResponseListener(this.responseListener);
  }

  @Override
  public void end() {
    final StatefulMessageTransport transport = this.getTransport();

    // No longer inform this state of any messages received
    transport.unregisterRequestListener(this.requestListener);
    transport.unregisterResponseListener(this.responseListener);
  }

  /**
   * Registers a handler in this state for the specified type of request message.
   *
   * @param methodName
   *   The name of the method, as it appears in requests.
   * @param messageType
   *   The type of message for which a handler is being registered.
   * @param handler
   *   The handler to invoke for the message.
   *
   * @throws IllegalArgumentException
   *   If a handler for the method specified in {@code methodName} is already registered.
   */
  protected <T extends RequestMessage> void registerRequestHandler(final String methodName,
                                                                   final Class<T> messageType,
                                                                   final MessageListener<T> handler)
  throws IllegalArgumentException {
    this.registerRequestHandler(methodName, messageType, handler, false);
  }

  /**
   * <p>Registers a handler in this state for the specified type of request message, optionally
   * replacing any existing handler already registered for that type of request message.</p>
   *
   * @param methodName
   *   The name of the method, as it appears in requests.
   * @param messageType
   *   The type of message for which a handler is being registered.
   * @param handler
   *   The handler to invoke for the message.
   * @param replace
   *   Whether or not to replace any existing handlers for the method specified in
   *   {@code methodName}.
   *
   * @throws IllegalArgumentException
   *   If a handler for the method specified in {@code methodName} is already registered and
   *   {@code replace} is {@code false}.
   */
  @SuppressWarnings("unchecked")
  protected <T extends RequestMessage> void registerRequestHandler(final String methodName,
                                                                   final Class<T> messageType,
                                                                   final MessageListener<T> handler,
                                                                   final boolean replace)
  throws IllegalArgumentException {
    final MessageMarshaller marshaller = this.getMarshaller();

    if (!replace && this.requestHandlers.containsKey(messageType)) {
      throw new IllegalArgumentException(
        "A handler is already registered for this request message type: " + messageType.getName()
      );
    }

    marshaller.registerRequestHandler(methodName, messageType);
    this.requestHandlers.put(messageType, (MessageListener<RequestMessage>)handler);
  }

  /**
   * Registers a handler in this state for the specified type of response message.
   *
   * @param messageType
   *   The type of message for which a handler is being registered.
   * @param handler
   *   The handler to invoke for the message.
   */
  @SuppressWarnings("unchecked")
  protected <T extends ResponseMessage> void registerResponseHandler(
                                                                final Class<T> messageType,
                                                                final MessageListener<T> handler) {
    if (this.responseHandlers.containsKey(messageType)) {
      throw new IllegalArgumentException(
        "A handler is already registered for this response message type: " + messageType.getName());
    }

    this.responseHandlers.put(messageType, (MessageListener<ResponseMessage>)handler);
  }

  /**
   * Factory method invoked to give sub-classes a chance to customize the marshaller.
   *
   * @return The Stratum message marshaller that should be used while in this connection state.
   */
  protected MessageMarshaller createMarshaller() {
    return new MessageMarshaller();
  }

  /**
   * <p>Default request handler for connection states.</p>
   *
   * <p>This implementation dispatches the incoming request to the appropriate handler, as
   * registered by a prior call to
   * {@link #registerRequestHandler(String, Class, MessageListener)}.</p>
   *
   * @param message
   *   The message to dispatch.
   */
  @Override
  public boolean processRequest(final RequestMessage message) {
    boolean                               handled = false;
    final MessageListener<RequestMessage> handler = this.requestHandlers.get(message.getClass());

    if (handler == null) {
      if (LOGGER.isErrorEnabled()) {
        LOGGER.error(
          "Request message '{}' was ignored by state '{}'.",
          message.getClass().getName(),
          this.getClass().getName()
        );
      }
    } else {
      handler.onMessageReceived(message);

      handled = true;
    }

    return handled;
  }

  /**
   * <p>Default response handler for connection states.</p>
   *
   * <p>This implementation dispatches the incoming response to the appropriate handler, as
   * registered by a prior call to {@link #registerResponseHandler(Class,MessageListener)}.</p>
   *
   * @param message
   *   The message to dispatch.
   */
  @Override
  public boolean processResponse(final ResponseMessage message) {
    boolean                                 handled = false;
    final MessageListener<ResponseMessage>  handler = this.responseHandlers.get(message.getClass());

    if (handler == null) {
      if (LOGGER.isErrorEnabled()) {
        LOGGER.error(
          "Response message '{}' was ignored by state '{}'.",
          message.getClass().getName(),
          this.getClass().getName()
        );
      }
    }
    else {
      handler.onMessageReceived(message);

      handled = true;
    }

    return handled;
  }

  /**
   * <p>Convenience method for changing the state of the current connection.</p>
   *
   * <p>After this method is called, this state will no longer handle marshalling of messages.</p>
   *
   * @param newState
   *   The new state for the connection.
   */
  protected void moveToState(final ConnectionState newState) {
    this.getTransport().setConnectionState(newState);
  }

  /**
   * <p>The listener that this state will use to receive incoming request
   * messages from the transport.</p>
   *
   * <p>© 2013 - 2014 RedBottle Design, LLC.</p>
   * <p>© 2020 Inveniem.</p>
   *
   * @author Guy Paddock (guy@inveniem.com)
   */
  protected class RequestListener
  implements MessageListener<RequestMessage> {
    @Override
    public void onMessageReceived(final RequestMessage message) {
      AbstractConnectionState.this.processRequest(message);
    }
  }

  /**
   * <p>The listener that this state will use to receive incoming response
   * messages from the transport.</p>
   *
   * <p>© 2013 - 2014 RedBottle Design, LLC.</p>
   * <p>© 2020 Inveniem.</p>
   *
   * @author Guy Paddock (guy@inveniem.com)
   */
  protected class ResponseListener
  implements MessageListener<ResponseMessage> {
    @Override
    public void onMessageReceived(final ResponseMessage message) {
      AbstractConnectionState.this.processResponse(message);
    }
  }
}
