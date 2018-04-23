package uk.gov.justice.services.messaging.spi;

import static javax.json.Json.createObjectBuilder;
import static javax.json.JsonValue.NULL;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.messaging.JsonEnvelope.METADATA;
import static uk.gov.justice.services.messaging.JsonMetadata.CAUSATION;
import static uk.gov.justice.services.messaging.JsonMetadata.CLIENT_ID;
import static uk.gov.justice.services.messaging.JsonMetadata.CONTEXT;
import static uk.gov.justice.services.messaging.JsonMetadata.CORRELATION;
import static uk.gov.justice.services.messaging.JsonMetadata.ID;
import static uk.gov.justice.services.messaging.JsonMetadata.NAME;
import static uk.gov.justice.services.messaging.JsonMetadata.SESSION_ID;
import static uk.gov.justice.services.messaging.JsonMetadata.STREAM;
import static uk.gov.justice.services.messaging.JsonMetadata.STREAM_ID;
import static uk.gov.justice.services.messaging.JsonMetadata.USER_ID;
import static uk.gov.justice.services.messaging.JsonMetadata.LEVEL_OF_ASSURANCE;
import static uk.gov.justice.services.messaging.JsonMetadata.VERSION;
import static uk.gov.justice.services.messaging.spi.DefaultJsonMetadata.metadataBuilderFrom;

import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.messaging.JsonMetadata;
import uk.gov.justice.services.messaging.Metadata;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonObject;

import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;
import com.google.common.testing.EqualsTester;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the {@link DefaultJsonMetadata} class.
 */
public class DefaultJsonMetadataTest {

    private static final String UUID_ID = "d04885b4-9652-4c2a-87c6-299bda0a87d4";
    private static final String UUID_CLIENT_CORRELATION = "8d67ed44-ecfb-43ce-867c-53077abf97a6";
    private static final String UUID_CAUSATION = "49ef76bc-df4f-4b91-8ca7-21972c30ee4c";
    private static final String UUID_USER_ID = "182a8f83-faa0-46d6-96d0-96999f05e3a2";
    private static final int LEVEL_OF_ASSURANCE_VALUE = 1;
    private static final String UUID_SESSION_ID = "f0132298-7b79-4397-bab6-f2f5e27915f0";
    private static final String UUID_STREAM_ID = "f29e0415-3a3b-48d8-b301-d34faa58662a";
    private static final String MESSAGE_NAME = "logical.message.name";
    private static final Long STREAM_VERSION = 99L;

    private JsonObject jsonObject;
    private Metadata metadata;

    @Before
    public void setup() {
        jsonObject = createObjectBuilder()
                .add(ID, UUID_ID)
                .add(NAME, MESSAGE_NAME)
                .add(CORRELATION, createObjectBuilder()
                        .add(CLIENT_ID, UUID_CLIENT_CORRELATION)
                )
                .add(CAUSATION, Json.createArrayBuilder()
                        .add(UUID_CAUSATION)
                )
                .add(CONTEXT, createObjectBuilder()
                        .add(USER_ID, UUID_USER_ID)
                        .add(LEVEL_OF_ASSURANCE, LEVEL_OF_ASSURANCE_VALUE)
                        .add(SESSION_ID, UUID_SESSION_ID)
                )
                .add(STREAM, createObjectBuilder()
                        .add(STREAM_ID, UUID_STREAM_ID)
                        .add(VERSION, STREAM_VERSION)
                )
                .build();
        metadata = metadataBuilderFrom(jsonObject).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionOnMissingId() throws Exception {
        final JsonObject joEnvelope = new StringToJsonObjectConverter().convert(jsonFromFile("json/envelope-missing-id.json"));
        metadataBuilderFrom(joEnvelope.getJsonObject(METADATA));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionOnMissingName() throws Exception {
        final JsonObject joEnvelope = new StringToJsonObjectConverter().convert(jsonFromFile("json/envelope-missing-name"));
        metadataBuilderFrom(joEnvelope.getJsonObject(METADATA));
    }

    @Test
    public void shouldReturnId() throws Exception {
        assertThat(metadata.id(), equalTo(UUID.fromString(UUID_ID)));
    }

    @Test
    public void shouldReturnName() throws Exception {
        assertThat(metadata.name(), equalTo(MESSAGE_NAME));
    }

    @Test
    public void shouldReturnClientCorrelationId() throws Exception {
        assertThat(metadata.clientCorrelationId().isPresent(), is(true));
        assertThat(metadata.clientCorrelationId().get(), equalTo(UUID_CLIENT_CORRELATION));
    }

    @Test
    public void shouldReturnCausation() throws Exception {
        assertThat(metadata.causation(), equalTo(ImmutableList.of(UUID.fromString(UUID_CAUSATION))));
    }

    @Test
    public void shouldReturnUserId() throws Exception {
        assertThat(metadata.userId().isPresent(), is(true));
        assertThat(metadata.userId().get(), equalTo(UUID_USER_ID));
    }

    @Test
    public void shouldReturnLevelOfAssurance() throws Exception {
        assertThat(metadata.levelOfAssurance().isPresent(), is(true));
        assertThat(metadata.levelOfAssurance().get(), is(LEVEL_OF_ASSURANCE_VALUE));
    }

    @Test
    public void shouldReturnSessionId() throws Exception {
        assertThat(metadata.sessionId().isPresent(), is(true));
        assertThat(metadata.sessionId().get(), equalTo(UUID_SESSION_ID));
    }

    @Test
    public void shouldReturnStreamId() throws Exception {
        assertThat(metadata.streamId().isPresent(), is(true));
        assertThat(metadata.streamId().get(), equalTo(UUID.fromString(UUID_STREAM_ID)));
    }

    @Test
    public void shouldReturnStreamVersion() throws Exception {
        assertThat(metadata.version().isPresent(), is(true));
        assertThat(metadata.version().get(), equalTo(STREAM_VERSION));
    }

    @Test
    public void shouldReturnJsonObject() throws Exception {
        assertThat(metadata.asJsonObject(), equalTo(jsonObject));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfIdIsMissing() throws Exception {
        metadataBuilderFrom(createObjectBuilder()
                .build()
        ).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfIdIsNotUUID() throws Exception {
        metadataBuilderFrom(createObjectBuilder()
                .add(ID, "blah")
                .build()
        ).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfIdIsNull() throws Exception {
        metadataBuilderFrom(createObjectBuilder()
                .add(ID, NULL)
                .build()
        ).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfNameIsMissing() throws Exception {
        metadataBuilderFrom(createObjectBuilder()
                .add(ID, UUID_ID)
                .build()
        ).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfNameIsEmpty() throws Exception {
        metadataBuilderFrom(createObjectBuilder()
                .add(ID, UUID_ID)
                .add(NAME, "")
                .build()
        ).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfNameIsNull() throws Exception {
        metadataBuilderFrom(createObjectBuilder()
                .add(ID, UUID_ID)
                .add(NAME, NULL)
                .build()
        ).build();
    }

    @SuppressWarnings({"squid:MethodCyclomaticComplexity", "squid:S1067", "squid:S00122"})
    @Test
    public void shouldTestEqualsAndHashCode() {

        final Metadata item1 = metadata(UUID_ID, UUID_CLIENT_CORRELATION, UUID_CAUSATION, UUID_USER_ID, 3, UUID_SESSION_ID, UUID_STREAM_ID, MESSAGE_NAME, STREAM_VERSION);
        final Metadata item2 = metadata(UUID_ID, UUID_CLIENT_CORRELATION, UUID_CAUSATION, UUID_USER_ID, 3, UUID_SESSION_ID, UUID_STREAM_ID, MESSAGE_NAME, STREAM_VERSION);
        final Metadata item3 = metadata(UUID.randomUUID().toString(), UUID_CLIENT_CORRELATION, UUID_CAUSATION, UUID_USER_ID, 3, UUID_SESSION_ID, UUID_STREAM_ID, MESSAGE_NAME, STREAM_VERSION);
        final Metadata item4 = metadata(UUID_ID, UUID.randomUUID().toString(), UUID_CAUSATION, UUID_USER_ID, 3, UUID_SESSION_ID, UUID_STREAM_ID, MESSAGE_NAME, STREAM_VERSION);
        final Metadata item5 = metadata(UUID_ID, UUID_CLIENT_CORRELATION, UUID.randomUUID().toString(), UUID_USER_ID, 3, UUID_SESSION_ID, UUID_STREAM_ID, MESSAGE_NAME, STREAM_VERSION);
        final Metadata item6 = metadata(UUID_ID, UUID_CLIENT_CORRELATION, UUID_CAUSATION, UUID.randomUUID().toString(), 3, UUID_SESSION_ID, UUID_STREAM_ID, MESSAGE_NAME, STREAM_VERSION);
        final Metadata item7 = metadata(UUID_ID, UUID_CLIENT_CORRELATION, UUID_CAUSATION, UUID_USER_ID, 3, UUID.randomUUID().toString(), UUID_STREAM_ID, MESSAGE_NAME, STREAM_VERSION);
        final Metadata item8 = metadata(UUID_ID, UUID_CLIENT_CORRELATION, UUID_CAUSATION, UUID_USER_ID, 3, UUID_SESSION_ID, UUID.randomUUID().toString(), MESSAGE_NAME, STREAM_VERSION);
        final Metadata item9 = metadata(UUID_ID, UUID_CLIENT_CORRELATION, UUID_CAUSATION, UUID_USER_ID, 3, UUID_SESSION_ID, UUID_STREAM_ID, "dummy name", STREAM_VERSION);
        final Metadata item10 = metadata(UUID_ID, UUID_CLIENT_CORRELATION, UUID_CAUSATION, UUID_USER_ID, 3, UUID_SESSION_ID, UUID_STREAM_ID, MESSAGE_NAME, 0L);

        new EqualsTester()
                .addEqualityGroup(item1, item2)
                .addEqualityGroup(item3)
                .addEqualityGroup(item4)
                .addEqualityGroup(item5)
                .addEqualityGroup(item6)
                .addEqualityGroup(item7)
                .addEqualityGroup(item8)
                .addEqualityGroup(item9)
                .addEqualityGroup(item10)
                .testEquals();
    }

    private Metadata metadata(String id, String uuidClientCorrelation, String uuidCausation, String uuidUserId, int levelOfAssurance,
                              String uuidSessionId, String uuidStreamId, String messageName, Long streamVersion) {
        return metadataBuilderFrom(
                createObjectBuilder()
                        .add(ID, id)
                        .add(NAME, messageName)
                        .add(CORRELATION, createObjectBuilder()
                                .add(CLIENT_ID, uuidClientCorrelation)
                        )
                        .add(CAUSATION, Json.createArrayBuilder()
                                .add(uuidCausation)
                        )
                        .add(CONTEXT, createObjectBuilder()
                                .add(USER_ID, uuidUserId)
                                .add(LEVEL_OF_ASSURANCE, levelOfAssurance)
                                .add(SESSION_ID, uuidSessionId)
                        )
                        .add(STREAM, createObjectBuilder()
                                .add(STREAM_ID, uuidStreamId)
                                .add(VERSION, streamVersion)
                        )
                        .build())
                .build();

    }

    private String jsonFromFile(final String name) throws IOException {
        return Resources.toString(Resources.getResource(String.format("json/%s.json", name)), Charset.defaultCharset());
    }
}
