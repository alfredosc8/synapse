package de.otto.synapse.configuration.aws;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.otto.edison.aws.configuration.AwsProperties;
import de.otto.synapse.endpoint.MessageInterceptorRegistry;
import de.otto.synapse.endpoint.receiver.MessageQueueReceiverEndpointFactory;
import de.otto.synapse.endpoint.receiver.aws.SqsMessageQueueReceiverEndpoint;
import de.otto.synapse.endpoint.sender.MessageSenderEndpointFactory;
import de.otto.synapse.endpoint.sender.aws.SqsMessageSenderEndpointFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

@Configuration
@EnableConfigurationProperties(AwsProperties.class)
public class SqsAutoConfiguration {

    private final AwsProperties awsProperties;

    @Autowired
    public SqsAutoConfiguration(final AwsProperties awsProperties) {
        this.awsProperties = awsProperties;
    }

    @Bean
    @ConditionalOnMissingBean(SqsAsyncClient.class)
    public SqsAsyncClient SqsAsyncClient(final AwsCredentialsProvider credentialsProvider) {
            return SqsAsyncClient.builder()
                    .credentialsProvider(credentialsProvider)
                    .region(Region.of(awsProperties.getRegion()))
                    .build();
    }

    @Bean
    @ConditionalOnMissingBean(name = "messageQueueSenderEndpointFactory")
    public MessageSenderEndpointFactory messageQueueSenderEndpointFactory(final MessageInterceptorRegistry registry,
                                                                          final ObjectMapper objectMapper,
                                                                          final SqsAsyncClient SqsAsyncClient,
                                                                          final @Value("${spring.application.name:Synapse Service}") String messageSenderName) {
        return new SqsMessageSenderEndpointFactory(registry, objectMapper, SqsAsyncClient, messageSenderName);
    }

    @Bean
    @ConditionalOnMissingBean(name = "messageQueueReceiverEndpointFactory")
    public MessageQueueReceiverEndpointFactory messageQueueReceiverEndpointFactory(final MessageInterceptorRegistry registry,
                                                                                   final ObjectMapper objectMapper,
                                                                                   final SqsAsyncClient SqsAsyncClient,
                                                                                   final ApplicationEventPublisher eventPublisher) {

        return (String channelName) -> {
            final SqsMessageQueueReceiverEndpoint endpoint = new SqsMessageQueueReceiverEndpoint(channelName, SqsAsyncClient, objectMapper, eventPublisher);
            endpoint.registerInterceptorsFrom(registry);
            return endpoint;
        };
    }

}
