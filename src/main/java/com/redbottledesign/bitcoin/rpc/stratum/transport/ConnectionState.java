package com.redbottledesign.bitcoin.rpc.stratum.transport;

import com.redbottledesign.bitcoin.rpc.stratum.message.MessageMarshaller;
import com.redbottledesign.bitcoin.rpc.stratum.message.RequestMessage;
import com.redbottledesign.bitcoin.rpc.stratum.message.ResponseMessage;

/**
 * <p>Interface for the various states of a {@link StatefulMessageTransport}.</p>
 *
 * <p>© 2013 - 2014 RedBottle Design, LLC.</p>
 * <p>© 2020 Inveniem.</p>
 *
 * @author Guy Paddock (guy@inveniem.com)
 */
public interface ConnectionState {
  /**
   * Sets in motion any of the actions that should be performed when the connection enters this
   * state.
   */
  void start();

  /**
   * Notifies this connection state that the connection is about to transition to a different
   * state.
   */
  void end();

  /**
   * <p>Gets the message marshaller that should be used to marshal and unmarshal messages while in
   * this state.</p>
   *
   * <p>The configuration of the marshaller will determine which messages are accepted while the
   * transport is in this state.</p>
   *
   * @return The message marshaller to use while in this state.
   */
  MessageMarshaller getMarshaller();

  /**
   * <p>Notifies this connection state to process the provided request message.</p>
   *
   * <p>This method returns a result that indicates whether or not the provided message was handled.
   * This enables a "chain of responsibility"-style inheritance behavior, whereby states define a
   * common set of messages that are always handled the same way and then define custom behaviors
   * for other messages.</p>
   *
   * @param message
   *   The request message to process.
   *
   * @return {@code true} if the request was handled; {@code false} if it was ignored.
   */
  boolean processRequest(RequestMessage message);

  /**
   * <p>Notifies this connection state to process the provided response message.</p>
   *
   * <p>This method returns a result that indicates whether or not the provided message was handled.
   * This enables a "chain of responsibility"-style inheritance behavior, whereby states define a
   * common set of messages that are always handled the same way and then define custom behaviors
   * for other messages.</p>
   *
   * @param message
   *   The response message to process.
   *
   * @return {@code true} if the response was handled; {@code false} if it was ignored.
   */
  boolean processResponse(ResponseMessage message);
}
