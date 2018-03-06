package de.otto.synapse.sender;

import de.otto.synapse.channel.InMemoryChannel;
import de.otto.synapse.endpoint.AbstractMessageSenderEndpoint;
import de.otto.synapse.endpoint.MessageInterceptor;
import de.otto.synapse.message.Message;
import de.otto.synapse.translator.MessageTranslator;

import javax.annotation.Nonnull;

public class InMemoryMessageSender extends AbstractMessageSenderEndpoint {

    private final InMemoryChannel channel;

    public InMemoryMessageSender(final MessageTranslator<String> messageTranslator,
                                 final InMemoryChannel channel) {
        super(channel.getChannelName(), messageTranslator);
        this.channel = channel;
    }

    public InMemoryMessageSender(final MessageTranslator<String> messageTranslator,
                                 final InMemoryChannel channel,
                                 final MessageInterceptor messageInterceptor) {
        super(channel.getChannelName(), messageTranslator, messageInterceptor);
        this.channel = channel;
    }

    @Override
    protected void doSend(@Nonnull Message<String> message) {
        channel.send(message);
    }

}
