package com.redbottledesign.bitcoin.rpc.stratum.transport;

import com.redbottledesign.bitcoin.rpc.stratum.message.Message;
import com.redbottledesign.bitcoin.rpc.stratum.message.RequestMessage;
import com.redbottledesign.bitcoin.rpc.stratum.message.ResponseMessage;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * <p>Abstract base class for {@link MessageTransport} implementations.</p>
 *
 * <p>This base implementation is optional. {@link MessageTransport} consumers should only refer to
 * the {@link MessageTransport} interface and never make a direct reference to this class.</p>
 *
 * <p>© 2013 - 2014 RedBottle Design, LLC.</p>
 * <p>© 2020 Inveniem.</p>
 *
 * @author Guy Paddock (guy@inveniem.com)
 */
public abstract class AbstractMessageTransport
implements MessageTransport {
  /**
   * The set of request listeners to notify when requests are received.
   */
  protected Set<MessageListener<RequestMessage>> requestListeners;

  /**
   * The set of response listeners to notify when responses are received.
   */
  protected Set<MessageListener<ResponseMessage>> responseListeners;

  /**
   * Default constructor for {@link AbstractMessageTransport}.
   */
  public AbstractMessageTransport() {
    this.requestListeners   = new LinkedHashSet<>();
    this.responseListeners  = new LinkedHashSet<>();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public synchronized void registerRequestListener(final MessageListener<RequestMessage> listener) {
    this.requestListeners.add(listener);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public synchronized void unregisterRequestListener(
                                                  final MessageListener<RequestMessage> listener) {
    this.requestListeners.remove(listener);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public synchronized void registerResponseListener(
                                                  final MessageListener<ResponseMessage> listener) {
    this.responseListeners.add(listener);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public synchronized void unregisterResponseListener(
                                                  final MessageListener<ResponseMessage> listener) {
    this.responseListeners.remove(listener);
  }

  /**
   * Notifies listeners about the provided messages.
   *
   * @param messages
   *   The messages to notify listeners about.
   */
  protected synchronized void notifyMessageListeners(final List<Message> messages) {
    for (final Message message : messages) {
      this.notifyMessageListeners(message);
    }
  }

  /**
   * Notifies listeners about the provided message(s).
   *
   * @param messages
   *   The message(s) to notify listeners about.
   */
  protected synchronized void notifyMessageListeners(final Message... messages) {
    for (final Message message : messages) {
      if (message instanceof RequestMessage) {
        this.notifyMessageListenersRequestReceived((RequestMessage)message);
      } else if (message instanceof ResponseMessage) {
        this.notifyMessageListenersResponseReceived((ResponseMessage)message);
      } else {
        throw new IllegalArgumentException(
          "Unknown message type (expected either RequestMessage or ResponseMessage): " +
          message.getClass().getName()
        );
      }
    }
  }

  /**
   * Notifies request listeners about the provided request.
   *
   * @param request
   *   The request to notify listeners about.
   */
  protected synchronized void notifyMessageListenersRequestReceived(final RequestMessage request) {
    for (final MessageListener<RequestMessage> listener : this.requestListeners) {
      listener.onMessageReceived(request);
    }
  }

  /**
   * Notifies response listeners about the provided response.
   *
   * @param response
   *   The response to notify listeners about.
   */
  protected synchronized void notifyMessageListenersResponseReceived(
                                                                  final ResponseMessage response) {
    for (final MessageListener<ResponseMessage> listener : this.responseListeners) {
      listener.onMessageReceived(response);
    }
  }
}
