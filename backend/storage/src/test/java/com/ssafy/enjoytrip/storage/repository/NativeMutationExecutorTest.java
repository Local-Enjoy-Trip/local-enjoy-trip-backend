package com.ssafy.enjoytrip.storage.repository;

import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NativeMutationExecutorTest {
    @Test
    void updateExecutesSqlWithBindingsAndReturnsAffectedRows() {
        AtomicBoolean providerCalled = new AtomicBoolean(false);
        AtomicReference<String> capturedSql = new AtomicReference<>();
        AtomicReference<Object[]> capturedBindings = new AtomicReference<>();
        DSLContext dsl = dslContext((sql, bindings) -> {
            providerCalled.set(true);
            capturedSql.set(sql);
            capturedBindings.set(bindings);
            return 3;
        });

        int affectedRows = new NativeMutationExecutor(dsl)
                .update("update members set name = ? where user_id = ?", "Renamed", "ssafy");

        assertEquals(3, affectedRows);
        assertEquals("update members set name = ? where user_id = ?", capturedSql.get());
        assertArrayEquals(new Object[]{"Renamed", "ssafy"}, capturedBindings.get());
        assertTrue(providerCalled.get());
    }

    @Test
    void updatePropagatesJooqExecutionFailures() {
        DSLContext dsl = dslContext((sql, bindings) -> {
            throw new DataAccessException("database rejected mutation");
        });

        DataAccessException thrown = assertThrows(
                DataAccessException.class,
                () -> new NativeMutationExecutor(dsl).update("broken sql")
        );
        assertTrue(thrown.getMessage().contains("database rejected mutation"));
    }

    private static DSLContext dslContext(Mutation mutation) {
        return (DSLContext) Proxy.newProxyInstance(
                DSLContext.class.getClassLoader(),
                new Class<?>[]{DSLContext.class},
                (proxy, method, args) -> {
                    if ("query".equals(method.getName())) {
                        String sql = (String) args[0];
                        Object[] bindings = (Object[]) args[1];
                        return Proxy.newProxyInstance(
                                method.getReturnType().getClassLoader(),
                                new Class<?>[]{method.getReturnType()},
                                query(sql, bindings, mutation)
                        );
                    }
                    return objectMethod(proxy, method, args);
                }
        );
    }

    private static InvocationHandler query(String sql, Object[] bindings, Mutation mutation) {
        return (proxy, method, args) -> {
            if ("execute".equals(method.getName())) {
                return mutation.execute(sql, bindings);
            }
            return objectMethod(proxy, method, args);
        };
    }

    private static Object objectMethod(Object proxy, java.lang.reflect.Method method, Object[] args) {
        return switch (method.getName()) {
            case "toString" -> "NativeMutationExecutorTestProxy";
            case "hashCode" -> System.identityHashCode(proxy);
            case "equals" -> proxy == args[0];
            default -> throw new UnsupportedOperationException(method.getName());
        };
    }

    @FunctionalInterface
    private interface Mutation {
        int execute(String sql, Object[] bindings);
    }
}
