package de.otto.edison.eventsourcing.example.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.otto.edison.eventsourcing.EventSourceBuilder;
import de.otto.edison.eventsourcing.MessageSenderFactory;
import de.otto.edison.eventsourcing.example.consumer.configuration.MyServiceProperties;
import de.otto.edison.eventsourcing.inmemory.InMemoryEventSource;
import de.otto.edison.eventsourcing.inmemory.InMemoryMessageSender;
import de.otto.edison.eventsourcing.translator.JsonStringMessageTranslator;
import de.otto.edison.eventsourcing.translator.MessageTranslator;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static de.otto.edison.eventsourcing.inmemory.InMemoryStreams.getChannel;

@Configuration
@EnableConfigurationProperties(MyServiceProperties.class)
public class TestEventSourcingConfiguration {

    @Bean
    public MessageSenderFactory eventSenderFactory(final ObjectMapper objectMapper,
                                                   final MyServiceProperties myServiceProperties) {
        final MessageTranslator<String> messageTranslator = new JsonStringMessageTranslator(objectMapper);
        return streamName -> {
            if (streamName.equals(myServiceProperties.getBananaChannel())) {
                return new InMemoryMessageSender(messageTranslator, getChannel(myServiceProperties.getBananaChannel()));
            } else if (streamName.equals(myServiceProperties.getProductChannel())) {
                return new InMemoryMessageSender(messageTranslator, getChannel(myServiceProperties.getProductChannel()));
            } else {
                throw new IllegalArgumentException("no stream for name " + streamName + " available.");
            }
        };
    }


    @Bean
    public EventSourceBuilder defaultEventSourceBuilder(final MyServiceProperties myServiceProperties,
                                                        final ApplicationEventPublisher eventPublisher,
                                                        final ObjectMapper objectMapper) {
        return (name, streamName) -> {
            if (streamName.equals(myServiceProperties.getBananaChannel())) {
                return new InMemoryEventSource(name, streamName, getChannel(myServiceProperties.getBananaChannel()), eventPublisher, objectMapper);
            } else if (streamName.equals(myServiceProperties.getProductChannel())) {
                return new InMemoryEventSource(name, streamName, getChannel(myServiceProperties.getProductChannel()), eventPublisher, objectMapper);
            } else {
                throw new IllegalArgumentException("no stream for name " + streamName + " available.");
            }
        };
    }

}
