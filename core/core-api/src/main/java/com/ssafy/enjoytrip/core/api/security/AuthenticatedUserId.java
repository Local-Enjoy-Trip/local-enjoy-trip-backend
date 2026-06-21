package com.ssafy.enjoytrip.core.api.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthenticatedUserId {
    Unauthenticated unauthenticated() default Unauthenticated.THROW;

    enum Unauthenticated {
        THROW,
        NULL
    }
}
