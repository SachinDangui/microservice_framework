package uk.gov.justice.services.example.cakeshop.command.handler;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.matchers.EventStreamMatcher.eventStreamAppendedWith;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerMatcher.isHandler;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerMethodMatcher.method;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.withMetadataEnvelopedFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeStreamMatcher.streamContaining;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataOf;

import uk.gov.justice.services.common.converter.ObjectToJsonValueConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.Tolerance;
import uk.gov.justice.services.example.cakeshop.domain.aggregate.Recipe;
import uk.gov.justice.services.example.cakeshop.domain.event.RecipeAdded;
import uk.gov.justice.services.example.cakeshop.domain.event.RecipePhotographAdded;
import uk.gov.justice.services.example.cakeshop.domain.event.RecipeRemoved;
import uk.gov.justice.services.example.cakeshop.domain.event.RecipeRenamed;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RecipeCommandHandlerTest {

    private static final String ADD_RECIPE_COMMAND_NAME = "example.command.add-recipe";
    private static final String ADD_RECIPE_EVENT_NAME = "example.recipe-added";
    private static final String RENAME_RECIPE_COMMAND_NAME = "example.command.rename-recipe";
    private static final String RENAME_RECIPE_EVENT_NAME = "example.recipe-renamed";
    private static final String REMOVE_RECIPE_COMMAND_NAME = "example.command.remove-recipe";
    private static final String REMOVE_RECIPE_EVENT_NAME = "example.recipe-removed";
    private static final String RECIPE_PHOTOGRAPH_ADDED_EVENT_NAME = "example.recipe-photograph-added";

    private static final UUID RECIPE_ID = randomUUID();
    private static final UUID PHOTO_ID = randomUUID();
    private static final String RECIPE_NAME = "Test Recipe";
    private static final Boolean GULTEN_FREE = true;

    @Mock
    private EventStream eventStream;

    @Mock
    private EventSource eventSource;

    @Mock
    private AggregateService aggregateService;

    @Mock
    ObjectToJsonValueConverter objectToJsonValueConverter = new ObjectToJsonValueConverter(new ObjectMapperProducer().objectMapper());

    @Spy
    private Enveloper enveloper = createEnveloperWithEvents(RecipeAdded.class, RecipeRenamed.class, RecipeRemoved.class, RecipePhotographAdded.class);

    @InjectMocks
    private RecipeCommandHandler recipeCommandHandler;

    @Test
    public void shouldHaveCorrectHandlesAnnotation() throws Exception {
        assertThat(recipeCommandHandler, isHandler(COMMAND_HANDLER)
                .with(method("addRecipe").thatHandles("example.command.add-recipe")));
    }

    @Test
    public void shouldHandleAddRecipeCommand() throws Exception {
        final Recipe recipe = new Recipe();
        final UUID commandId = randomUUID();

        final JsonEnvelope command = envelopeFrom(
                metadataOf(commandId, ADD_RECIPE_COMMAND_NAME),
                createObjectBuilder()
                        .add("recipeId", RECIPE_ID.toString())
                        .add("name", RECIPE_NAME)
                        .add("glutenFree", GULTEN_FREE)
                        .add("ingredients", createArrayBuilder()
                                .add(createObjectBuilder()
                                        .add("name", "Flour")
                                        .add("quantity", 200)))
                        .build());

        when(eventSource.getStreamById(RECIPE_ID)).thenReturn(eventStream);
        when(aggregateService.get(eventStream, Recipe.class)).thenReturn(recipe);

        recipeCommandHandler.addRecipe(command);

        assertThat(eventStream, eventStreamAppendedWith(
                streamContaining(
                        jsonEnvelope(
                                withMetadataEnvelopedFrom(command)
                                        .withName(ADD_RECIPE_EVENT_NAME),
                                payloadIsJson(allOf(
                                        withJsonPath("$.recipeId", equalTo(RECIPE_ID.toString())),
                                        withJsonPath("$.name", equalTo(RECIPE_NAME)),
                                        withJsonPath("$.glutenFree", equalTo(GULTEN_FREE)),
                                        withJsonPath("$.ingredients.length()", equalTo(1)),
                                        withJsonPath("$.ingredients[0].name", equalTo("Flour")),
                                        withJsonPath("$.ingredients[0].quantity", equalTo(200))
                                )))
                                .thatMatchesSchema()
                )));
    }

    @Test
    public void shouldHandleRenameRecipeCommand() throws Exception {

        RenameRecipe renameRecipe = new RenameRecipe(RECIPE_ID.toString(), RECIPE_NAME);

        Metadata metadata = JsonEnvelope.metadataBuilder()
                .withName("Name")
                .withId(UUID.randomUUID())
                .withClientCorrelationId("asdsfd")
                .build();

        Envelope<RenameRecipe> command = new LocalDefaultNewEnvelope<>(metadata, renameRecipe);

        when(eventSource.getStreamById(RECIPE_ID)).thenReturn(eventStream);
        when(aggregateService.get(eventStream, Recipe.class)).thenReturn(existingRecipe());
        System.out.println("Before Command: "+ command.payload().getName());
        recipeCommandHandler.renameRecipe(command);

        System.out.println("After  Command: "+ command.payload().getName());

        assertThat(command.payload().getName(), is(RECIPE_NAME));

        verify(eventStream).append(
                argThat(streamContaining(
                        jsonEnvelope(
                                withMetadataEnvelopedFrom(command)
                                        .withName(RENAME_RECIPE_EVENT_NAME),
                                payloadIsJson(allOf(
                                        withJsonPath("$.recipeId", equalTo(RECIPE_ID.toString())),
                                        withJsonPath("$.name", equalTo(RECIPE_NAME))
                                ))).thatMatchesSchema()
                )), eq(Tolerance.NON_CONSECUTIVE));

    }

    @Test
    public void shouldHandleRemoveRecipeCommand() throws Exception {
        final UUID commandId = randomUUID();

        final JsonEnvelope command = envelopeFrom(
                metadataOf(commandId, REMOVE_RECIPE_COMMAND_NAME),
                createObjectBuilder()
                        .add("recipeId", RECIPE_ID.toString())
                        .build());

        when(eventSource.getStreamById(RECIPE_ID)).thenReturn(eventStream);
        when(aggregateService.get(eventStream, Recipe.class)).thenReturn(existingRecipe());

        recipeCommandHandler.removeRecipe(command);

        assertThat(eventStream, eventStreamAppendedWith(
                streamContaining(
                        jsonEnvelope(
                                withMetadataEnvelopedFrom(command)
                                        .withName(REMOVE_RECIPE_EVENT_NAME),
                                payloadIsJson(
                                        withJsonPath("$.recipeId", equalTo(RECIPE_ID.toString()))
                                ))
                                .thatMatchesSchema()
                )));

    }

    @Test
    public void shouldHandleUploadPhotographCommand() throws Exception {
        final UUID commandId = randomUUID();

        final JsonEnvelope command = envelopeFrom(
                metadataOf(commandId, "example.upload-photograph"),
                createObjectBuilder()
                        .add("recipeId", RECIPE_ID.toString())
                        .add("photoId", PHOTO_ID.toString())
                        .build());

        when(eventSource.getStreamById(RECIPE_ID)).thenReturn(eventStream);
        when(aggregateService.get(eventStream, Recipe.class)).thenReturn(existingRecipe());

        recipeCommandHandler.uploadPhotograph(command);

        verify(eventStream).append(
                argThat(streamContaining(
                        jsonEnvelope(
                                withMetadataEnvelopedFrom(command)
                                        .withName(RECIPE_PHOTOGRAPH_ADDED_EVENT_NAME),
                                payloadIsJson(allOf(
                                        withJsonPath("$.recipeId", equalTo(RECIPE_ID.toString())),
                                        withJsonPath("$.photoId", equalTo(PHOTO_ID.toString()))
                                )))
                                .thatMatchesSchema()
                )), eq(Tolerance.NON_CONSECUTIVE));

    }

    private Recipe existingRecipe() {
        final Recipe recipe = new Recipe();
        recipe.apply(new RecipeAdded(RECIPE_ID, RECIPE_NAME, GULTEN_FREE, emptyList()));
        return recipe;
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

}
