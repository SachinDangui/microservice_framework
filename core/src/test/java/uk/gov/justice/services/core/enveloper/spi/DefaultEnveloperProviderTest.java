package uk.gov.justice.services.core.enveloper.spi;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.core.enveloper.Enveloper.envelop;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.justice.services.common.converter.ObjectToJsonValueConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.core.enveloper.DefaultEnveloper;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.extension.EventFoundEvent;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.justice.services.messaging.spi.DefaultEnvelope;

import java.util.UUID;
import java.util.function.Function;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.openejb.OpenEjbContainer;
import org.junit.Before;
import org.junit.Test;

public class DefaultEnveloperProviderTest {

    private static final String TEST_EVENT_NAME = "test.event.something-happened";

    private DefaultEnveloper enveloper;

    @Before
    public void setup() throws JsonProcessingException {
        OpenEjbContainer.createEJBContainer();
        enveloper = new DefaultEnveloper(
                new UtcClock(),
                new ObjectToJsonValueConverter(new ObjectMapperProducer().objectMapper()));

    }

    @Test
    public void shouldEnvelopWithDefaultEnvelope() {

        Metadata metadata = createMetadata();

        final TestPojo payload = new TestPojo(TEST_EVENT_NAME);

        Envelope<TestPojo> envelope = new DefaultEnvelope<>(metadata, payload);

        final Envelope<TestPojo> resultEnvelope = envelop(payload).withName(TEST_EVENT_NAME).withMetadataFrom(envelope);

        assertThat(resultEnvelope, instanceOf(DefaultEnvelope.class));
        assertThat(resultEnvelope.metadata().name(), is(metadata.name()));
        assertThat(resultEnvelope.payload(), is(payload));

    }

    @Test
    public void shouldProvideDefaultEnvelopeFromMetadataAndPayload() throws Exception {
        enveloper.register(new EventFoundEvent(DefaultEnveloperProviderTest.TestEvent.class, TEST_EVENT_NAME));
        final Metadata metadata = createMetadata();
        final TestPojo payload = new TestPojo(TEST_EVENT_NAME);

        Envelope<TestPojo> envelope = new LocalDefaultNewEnvelope<>(metadata, payload);

        final Function<Object, JsonEnvelope> envelope1 = Enveloper.toEnvelopeWithMetadataFrom(envelope);
        TestEvent testEvent = new TestEvent();

        assertThat(envelope1.apply(testEvent).metadata().name(), is(metadata.name()));
        assertThat(envelope.payload(), is(payload));
    }

    class LocalDefaultNewEnvelope<T> implements Envelope<T> {

        private final Metadata metadata;

        private final T payload;

        LocalDefaultNewEnvelope(final Metadata metadata, final T payload) {
            this.metadata = metadata;
            this.payload = payload;
        }

        @Override
        public Metadata metadata() {
            return metadata;
        }

        @Override
        public T payload() {
            return payload;
        }

    }

    @Event("Test-Event")
    public static class TestEvent {
        private String somePayloadKey;

        public TestEvent(final String somePayloadKey) {
            this.somePayloadKey = somePayloadKey;
        }

        public TestEvent() {
        }

        public String getSomePayloadKey() {
            return somePayloadKey;
        }
    }

    private Metadata createMetadata() {
        return JsonEnvelope.metadataBuilder()
                .withName(TEST_EVENT_NAME)
                .withId(UUID.randomUUID())
                .withClientCorrelationId("asdsfd")
                .build();
    }

    class TestPojo {
        String name = TEST_EVENT_NAME;

        public TestPojo(final String name) {
            this.name = name;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

}
