package de.otto.synapse.edison.trace;

import de.otto.edison.navigation.NavBar;
import de.otto.synapse.endpoint.EndpointType;
import de.otto.synapse.endpoint.MessageEndpoint;
import de.otto.synapse.endpoint.MessageInterceptorRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.ManagementServerProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Optional;

import static de.otto.edison.navigation.NavBarItem.navBarItem;
import static de.otto.synapse.endpoint.MessageInterceptorRegistration.matchingChannelsWith;
import static java.lang.String.format;
import static java.util.Collections.emptyList;

@Configuration
@ConditionalOnProperty(
        prefix = "synapse.edison.trace",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
public class MessageTraceAutoConfiguration {

    @Value("${synapse.edison.trace.capacity:500}")
    private int capacity = 500;

    @Bean
    @ConditionalOnMissingBean
    public MessageTrace messageTrace(final MessageInterceptorRegistry interceptorRegistry,
                                     final NavBar rightNavBar,
                                     final ManagementServerProperties managementServerProperties,
                                     final Optional<List<? extends MessageEndpoint>> endpoints) {

        final String contextPath = managementServerProperties.getContextPath();
        rightNavBar.register(
                navBarItem(10, "Message Trace", format("%s/messagetrace", contextPath))
        );

        final MessageTrace messageTrace = new MessageTrace(capacity);
        endpoints.orElse(emptyList()).forEach(endpoint -> {
            final String channelName = endpoint.getChannelName();
            final EndpointType endpointType = endpoint.getEndpointType();
            interceptorRegistry.register(matchingChannelsWith(
                    channelName,
                    message -> {
                        messageTrace.add(new TraceEntry(channelName, endpointType, message));
                        return message;
                    }
            ));
            switch (endpointType) {
                case SENDER:
                    rightNavBar.register(navBarItem(11, "Sender: " + channelName, format("%s/messagetrace/sender/%s", contextPath, channelName)));
                    break;
                case RECEIVER:
                    rightNavBar.register(navBarItem(12, "Receiver: " + channelName, format("%s/messagetrace/receiver/%s", contextPath, channelName)));
                    break;
            }
        });
        return messageTrace;
    }

}
