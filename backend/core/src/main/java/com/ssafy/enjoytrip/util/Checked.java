package com.ssafy.enjoytrip.util;

import java.util.function.Function;
import java.util.function.Supplier;

public final class Checked {
    private Checked() {
    }

    public static <T> T getOrElse(ThrowingSupplier<T> supplier, Supplier<T> fallback) {
        try {
            return supplier.get();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return fallback.get();
        } catch (Exception ex) {
            return fallback.get();
        }
    }

    public static <T> T getOrThrow(ThrowingSupplier<T> supplier, Function<Exception, RuntimeException> mapper) {
        try {
            return supplier.get();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw mapper.apply(ex);
        } catch (Exception ex) {
            throw mapper.apply(ex);
        }
    }

    @FunctionalInterface
    public interface ThrowingSupplier<T> {
        T get() throws Exception;
    }
}
