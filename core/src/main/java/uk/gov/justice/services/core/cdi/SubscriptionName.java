package uk.gov.justice.services.core.cdi;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;


// @TODO: move this to framework-api
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({TYPE, FIELD, METHOD})
public @interface SubscriptionName {
    String value();
}


