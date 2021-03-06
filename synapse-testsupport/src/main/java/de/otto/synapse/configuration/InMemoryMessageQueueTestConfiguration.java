package de.otto.synapse.configuration;

import de.otto.synapse.channel.InMemoryChannels;
import de.otto.synapse.channel.selector.MessageQueue;
import de.otto.synapse.endpoint.MessageInterceptorRegistry;
import de.otto.synapse.endpoint.receiver.InMemoryMessageQueueReceiverEndpointFactory;
import de.otto.synapse.endpoint.receiver.MessageQueueReceiverEndpointFactory;
import de.otto.synapse.endpoint.sender.InMemoryMessageSenderFactory;
import de.otto.synapse.endpoint.sender.MessageSenderEndpointFactory;
import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Bean;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Configuration used to implement tests. Use this, if you want to bypass SQS and other hard-to-test
 * infrastructures and replace them by in-memory implementations.
 * <p>
 * {@code InMemoryMessageQueueTestConfiguration} can be activated by {@link org.springframework.boot.autoconfigure.ImportAutoConfiguration importing} it
 * into some other {@code Configuration} class:
 * </p>
 * <pre><code>
 * &#64;Configuration
 * &#64;ImportAutoConfiguration(InMemoryMessageQueueTestConfiguration.class)
 * public class MyTestConfig {
 *     // ...
 * }
 * </code></pre>
 */
@ImportAutoConfiguration(InMemoryChannelTestConfiguration.class)
public class InMemoryMessageQueueTestConfiguration {

    private static final Logger LOG = getLogger(InMemoryMessageQueueTestConfiguration.class);

    @Bean
    public MessageSenderEndpointFactory messageQueueSenderEndpointFactory(final MessageInterceptorRegistry interceptorRegistry,
                                                                        final InMemoryChannels inMemoryChannels) {
        LOG.warn("Creating InMemoryMessageSenderEndpointFactory. This should only be used in tests");
        return new InMemoryMessageSenderFactory(interceptorRegistry, inMemoryChannels, MessageQueue.class);
    }

    @Bean
    public MessageQueueReceiverEndpointFactory messageQueueReceiverEndpointFactory(final InMemoryChannels inMemoryChannels) {
        LOG.warn("Creating InMemoryMessageLogReceiverEndpointFactory. This should only be used in tests");
        return new InMemoryMessageQueueReceiverEndpointFactory(inMemoryChannels);
    }

}
