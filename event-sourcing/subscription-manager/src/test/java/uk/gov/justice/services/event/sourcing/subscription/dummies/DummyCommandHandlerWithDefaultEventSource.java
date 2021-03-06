package uk.gov.justice.services.event.sourcing.subscription.dummies;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.cdi.EventSourceName;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;

@ServiceComponent(COMMAND_HANDLER)
@EventSourceName
public class DummyCommandHandlerWithDefaultEventSource {

    @Inject
    EventSource eventSource;

    @Handles("my-context.do-something")
    public void doSomething(final JsonEnvelope command) throws EventStreamException {
    }

}
