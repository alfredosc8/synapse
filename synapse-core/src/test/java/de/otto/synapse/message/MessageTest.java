package de.otto.synapse.message;

import org.junit.Test;

import java.time.Instant;

import static de.otto.synapse.channel.ShardPosition.fromPosition;
import static de.otto.synapse.message.Header.responseHeader;
import static de.otto.synapse.message.Message.message;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class MessageTest {

    @Test
    public void shouldBuildMessageWithHeader() {
        final Instant now = Instant.now();
        final Message<String> message = message(
                "42",
                responseHeader(fromPosition("some-channel", "00001"), now),
                "ßome dätä"
        );
        assertThat(message.getKey(), is("42"));
        assertThat(message.getPayload(), is("ßome dätä"));
        assertThat(message.getHeader().getArrivalTimestamp(), is(now));
        assertThat(message.getHeader().getShardPosition().get().shardName(), is("some-channel"));
        assertThat(message.getHeader().getShardPosition().get().position(), is("00001"));
    }

    @Test
    public void shouldBuildMessageWithoutHeader() {
        final Instant now = Instant.now();
        final Message<String> message = message(
                "42",
                "ßome dätä"
        );
        assertThat(message.getKey(), is("42"));
        assertThat(message.getPayload(), is("ßome dätä"));
        assertThat(message.getHeader().getArrivalTimestamp().isBefore(now), is(false));
        assertThat(message.getHeader().getShardPosition().isPresent(), is(false));
    }
}
