package uk.gov.justice.services.core.interceptor;

import static java.lang.String.format;
import static uk.gov.justice.services.core.interceptor.InterceptorContext.interceptorContextWithInput;
import static uk.gov.justice.services.messaging.logging.LoggerUtils.trace;

import uk.gov.justice.services.core.dispatcher.DispatcherCache;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

import org.slf4j.Logger;

@ApplicationScoped
public class InterceptorChainProcessorProducer {

    @Inject
    Logger logger;

    @Inject
    DispatcherCache dispatcherCache;

    @Inject
    InterceptorCache interceptorCache;

    /**
     * Produces an interceptor chain processor for the provided injection point.
     *
     * @param injectionPoint class where the {@link InterceptorChainProcessor} is being injected
     * @return the interceptor chain processor
     */
    @Produces
    public InterceptorChainProcessor produceProcessor(final InjectionPoint injectionPoint) {
        final Function<JsonEnvelope, JsonEnvelope> synchronousDispatch = dispatcherCache.dispatcherFor(injectionPoint)::dispatch;

        trace(logger, () -> format("Interceptor Chain Processor provided for %s", injectionPoint.getClass().getName()));

        return createProcessor(synchronousDispatch, injectionPoint);
    }

    /**
     * Constructs the {@link InterceptorChainProcessor} for the given dispatcher interceptor target
     * and injection point.
     *
     * @param dispatcher     the dispatcher target method
     * @param injectionPoint the injection point of the {@link InterceptorChainProcessor}
     * @return the interceptor chain processor function
     */
    private InterceptorChainProcessor createProcessor(final Function<JsonEnvelope, JsonEnvelope> dispatcher, final InjectionPoint injectionPoint) {

        return jsonEnvelope -> {
            final InterceptorChain interceptorChain = new InterceptorChain(interceptorCache.getInterceptors(), new DispatcherTarget(dispatcher));

            return interceptorChain.processNext(interceptorContextWithInput(jsonEnvelope, injectionPoint))
                    .outputEnvelope()
                    .orElse(null);
        };
    }
}
