package com.id.px3.utils;

import java.util.function.Function;

@FunctionalInterface
public interface ThrowingFn<T, R, E extends Exception> {
    R apply(T t) throws E;

    static <T, R> ThrowingFn<T, R, RuntimeException> from(Function<T, R> function) {
        return function::apply;
    }

    default Function<T, R> mayThrow() throws RuntimeException {
        return (t) -> {
            try {
                return apply(t);
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    static <T, R, E extends Exception> Function<T, R> mayThrow(final ThrowingFn<T, R, E> function) throws RuntimeException {
        return function.mayThrow();
    }
}
