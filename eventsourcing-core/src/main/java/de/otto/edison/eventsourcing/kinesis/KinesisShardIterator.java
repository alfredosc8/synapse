package de.otto.edison.eventsourcing.kinesis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.model.GetRecordsRequest;
import software.amazon.awssdk.services.kinesis.model.GetRecordsResponse;

public class KinesisShardIterator {

    private static final Logger LOG = LoggerFactory.getLogger(KinesisShardIterator.class);

    static final int FETCH_RECORDS_LIMIT = 10000;
    static final int RETRY_MAX_ATTEMPTS = 16;
    static final int RETRY_BACK_OFF_POLICY_INITIAL_INTERVAL = 1000;
    static final int RETRY_BACK_OFF_POLICY_MAX_INTERVAL = 60000;
    static final double RETRY_BACK_OFF_POLICY_MULTIPLIER = 2.0;

    private final KinesisClient kinesisClient;
    private String id;
    private final RetryTemplate retryTemplate;

    public KinesisShardIterator(KinesisClient kinesisClient, String firstId) {
        this.kinesisClient = kinesisClient;
        this.id = firstId;
        this.retryTemplate = createRetryTemplate();
    }

    public String getId() {
        return this.id;
    }

    public GetRecordsResponse next() {
        try {
            return retryTemplate.execute((RetryCallback<GetRecordsResponse, Throwable>) context -> {
                try {
                    return tryNext();
                } catch (Exception e) {
                    LOG.info("failed to iterate on shard: {}", e.getMessage());
                    throw e;
                }
            });
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    private GetRecordsResponse tryNext() {
        GetRecordsResponse response = kinesisClient.getRecords(GetRecordsRequest.builder()
                .shardIterator(id)
                .limit(FETCH_RECORDS_LIMIT)
                .build());
        this.id = response.nextShardIterator();
        return response;
    }

    private RetryTemplate createRetryTemplate() {
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(RETRY_MAX_ATTEMPTS);

        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(RETRY_BACK_OFF_POLICY_INITIAL_INTERVAL);
        backOffPolicy.setMaxInterval(RETRY_BACK_OFF_POLICY_MAX_INTERVAL);
        backOffPolicy.setMultiplier(RETRY_BACK_OFF_POLICY_MULTIPLIER);

        RetryTemplate template = new RetryTemplate();
        template.setRetryPolicy(retryPolicy);
        template.setBackOffPolicy(backOffPolicy);

        return template;
    }

}
