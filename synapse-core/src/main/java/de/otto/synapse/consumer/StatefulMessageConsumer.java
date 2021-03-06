package de.otto.synapse.consumer;

import de.otto.synapse.message.Message;
import de.otto.synapse.state.StateRepository;

import javax.annotation.Nonnull;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * A {@code MessageConsumer} that is updating a {@link StateRepository}.
 *
 * @param <P> the type of the {@link Message} payload
 * @param <S> the type of the {@link StateRepository} entries
 */
public class StatefulMessageConsumer<P, S> implements MessageConsumer<P> {

    private final Pattern keyPattern;
    private final StateRepository<S> stateRepository;
    private final Class<P> payloadType;
    private final Function<Message<P>, String> keyMapper;
    private final Function<Message<P>, S> payloadToStateMapper;

    /**
     * Creates a StatefulMessageConsumer.
     *
     * @param keyPattern the key-pattern of {@link de.otto.synapse.message.Message#getKey() message keys} accepted by this consumer.
     * @param payloadType the payload type of the messages accepted by this consumer
     * @param stateRepository the StateRepository that is holding the State
     * @param payloadToStateMapper the mapper function used to map message payload to state entities
     */
    public StatefulMessageConsumer(final String keyPattern,
                                   final Class<P> payloadType,
                                   final StateRepository<S> stateRepository,
                                   final Function<Message<P>, S> payloadToStateMapper) {
        this(keyPattern, payloadType, stateRepository, payloadToStateMapper, Message::getKey);
    }

    /**
     * Creates a StatefulMessageConsumer.
     *
     * @param keyPattern the key-pattern of {@link de.otto.synapse.message.Message#getKey() message keys} accepted by this consumer.
     * @param payloadType the payload type of the messages accepted by this consumer
     * @param stateRepository the StateRepository that is holding the State
     * @param payloadToStateMapper the mapper function used to map message payload to state entities
     * @param keyMapper the mapper function used to map message keys to StateRepository keys.
     */
    public StatefulMessageConsumer(final String keyPattern,
                                   final Class<P> payloadType,
                                   final StateRepository<S> stateRepository,
                                   final Function<Message<P>, S> payloadToStateMapper,
                                   final Function<Message<P>,String> keyMapper) {
        this.keyPattern = Pattern.compile(keyPattern);
        this.payloadType = payloadType;
        this.stateRepository = stateRepository;
        this.payloadToStateMapper = payloadToStateMapper;
        this.keyMapper = keyMapper;
    }

    /**
     * Returns the expected payload type of {@link Message events} consumed by this EventConsumer.
     *
     * @return payload type
     */
    @Nonnull
    @Override
    public Class<P> payloadType() {
        return payloadType;
    }

    /**
     * Returns the pattern of {@link de.otto.synapse.message.Message#getKey() message keys} accepted by this consumer.
     *
     * @return Pattern
     */
    @Nonnull
    @Override
    public Pattern keyPattern() {
        return keyPattern;
    }

    @Override
    public void accept(final Message<P> message) {
        if (message.getPayload() == null) {
            stateRepository.remove(keyMapper.apply(message));
        } else {
            stateRepository.put(keyMapper.apply(message), payloadToStateMapper.apply(message));
        }
    }

}
