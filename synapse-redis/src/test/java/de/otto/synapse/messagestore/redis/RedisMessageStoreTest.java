package de.otto.synapse.messagestore.redis;

import de.otto.synapse.message.Message;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Collections;

import static de.otto.synapse.channel.ChannelPosition.fromHorizon;
import static de.otto.synapse.messagestore.redis.RedisMessageStore.messageOf;
import static de.otto.synapse.messagestore.redis.RedisMessageStore.toRedisValue;
import static java.util.Collections.emptyMap;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class RedisMessageStoreTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;
    @Mock
    BoundHashOperations<String, Object, Object> ops;
    private RedisMessageStore testee;

    @Before
    public void before() {
        initMocks(this);
        when(redisTemplate.boundHashOps(anyString())).thenReturn(ops);
        testee = new RedisMessageStore("some channel", redisTemplate, 10, 20);
    }

    @Test
    public void shouldReturnChannelName() {
        assertThat(testee.getChannelName(), is("some channel"));
    }

    @Test
    public void shouldReturnFromHorizonFromEmptyMessageStore() {
        when(ops.entries()).thenReturn(emptyMap());
        assertThat(testee.getLatestChannelPosition(), is(fromHorizon()));
    }

    @Test
    public void shouldConvertMessageToRedisValueAndViceVersa() {
        final Message<String> message = Message.message("some key","{}");
        final String redisValue = toRedisValue(message);
        assertThat(redisValue, is("{\"key\":\"some key\",\"header\":{},\"payload\":{}}"));
        final Message<String> transformed = messageOf(redisValue);
        assertThat(transformed.getKey(), is(message.getKey()));
        assertThat(transformed.getPayload(), is(message.getPayload()));
    }

    @Test
    public void shouldConvertMessageWithNullPayloadToRedisValueAndViceVersa() {
        final Message<String> message = Message.message("some key", null);
        final String redisValue = toRedisValue(message);
        assertThat(redisValue, is("{\"key\":\"some key\",\"header\":{},\"payload\":null}"));
        final Message<String> transformed = messageOf(redisValue);
        assertThat(transformed.getKey(), is(message.getKey()));
        assertThat(transformed.getPayload(), is(message.getPayload()));
    }

}