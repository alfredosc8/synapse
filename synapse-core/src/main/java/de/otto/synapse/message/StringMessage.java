package de.otto.synapse.message;

/**
 * A Message is an atomic packet of data that can be transmitted on a channel.
 *
 * <p>
 * <img src="http://www.enterpriseintegrationpatterns.com/img/MessageSolution.gif" alt="Message">
 * </p>
 *
 * <p>Thus to transmit data, an application must break the data into one or more packets,
 * wrap each packet as a message, and then send the message on a channel. Likewise, a receiver
 * application receives a message and must extract the data from the message to process it.
 * </p>
 * <p>
 * The message system will try repeatedly to deliver the message (e.g., transmit it from the
 * sender to the receiver) until it succeeds.
 * </p>
 *
 * @see <a href="http://www.enterpriseintegrationpatterns.com/patterns/messaging/Message.html">EIP - Message</a>
 */
public final class StringMessage {

}
