package de.otto.synapse.sender.aws;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.otto.synapse.endpoint.MessageSenderEndpoint;
import de.otto.synapse.sender.MessageSenderFactory;
import de.otto.synapse.translator.JsonStringMessageTranslator;
import de.otto.synapse.translator.MessageTranslator;
import software.amazon.awssdk.services.kinesis.KinesisClient;

public class KinesisMessageSenderFactory implements MessageSenderFactory {

    private final MessageTranslator<String> messageTranslator;
    private final KinesisClient kinesisClient;

    public KinesisMessageSenderFactory(final ObjectMapper objectMapper,
                                       final KinesisClient kinesisClient) {
        this.messageTranslator = new JsonStringMessageTranslator(objectMapper);
        this.kinesisClient = kinesisClient;
    }

    public MessageSenderEndpoint createSenderForStream(final String streamName) {
        return new KinesisMessageSender(streamName, messageTranslator, kinesisClient);
    }

}
