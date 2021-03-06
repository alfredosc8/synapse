package de.otto.synapse.configuration.aws;

import de.otto.synapse.compaction.s3.CompactionService;
import de.otto.synapse.compaction.s3.SnapshotWriteService;
import de.otto.synapse.endpoint.receiver.MessageLogReceiverEndpointFactory;
import de.otto.synapse.eventsource.EventSourceBuilder;
import de.otto.synapse.state.ConcurrentHashMapStateRepository;
import de.otto.synapse.state.StateRepository;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(CompactionProperties.class)
@ImportAutoConfiguration(S3SnapshotAutoConfiguration.class)
public class CompactionAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "compactionStateRepository")
    public StateRepository<String> compactionStateRepository() {
        return new ConcurrentHashMapStateRepository<>();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "synapse.compaction", name = "enabled", havingValue = "true")
    public CompactionService compactionService(final SnapshotWriteService snapshotWriteService,
                                               final StateRepository<String> compactionStateRepository,
                                               final EventSourceBuilder eventSourceBuilder,
                                               final MessageLogReceiverEndpointFactory messageLogReceiverEndpointFactory) {
        return new CompactionService(snapshotWriteService, compactionStateRepository, eventSourceBuilder, messageLogReceiverEndpointFactory);
    }
}
