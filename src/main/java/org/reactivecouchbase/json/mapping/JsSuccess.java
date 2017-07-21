package org.reactivecouchbase.json.mapping;


import io.vavr.collection.Array;
import io.vavr.collection.Seq;
import io.vavr.control.Option;

import java.util.Iterator;
import java.util.function.Function;

public class JsSuccess<T> extends JsResult<T> {

    private final T value;

    public JsSuccess(T value) {
        this.value = value;
    }

    @Override
    public Option<JsError<T>> asError() {
        return Option.none();
    }

    @Override
    public Option<JsSuccess<T>> asSuccess() {
        return Option.some(this);
    }

    @Override
    public boolean hasErrors() {
        return false;
    }

    @Override
    public T getValueOrNull() {
        return value;
    }

    @Override
    public boolean isErrors() {
        return false;
    }

    @Override
    public boolean isSuccess() {
        return true;
    }

    @Override
    public int countErrors() {
        return 0;
    }

    @Override
    public T get() {
        return value;
    }

    @Override
    public T orError(Throwable t) {
        return get();
    }

    @Override
    public Option<T> getOpt() {
        return Option.some(value);
    }

    @Override
    public T getValueOrElse(T result) {
        return value;
    }

    @Override
    public T getValueOrElse(Throwable result) {
        return value;
    }

    @Override
    public JsResult<T> getOrElse(JsResult<T> result) {
        return new JsSuccess<>(value);
    }

    @Override
    public <B> JsResult<B> map(Function<T, B> map) {
        return new JsSuccess<>(map.apply(value));
    }

    @Override
    public <B> JsResult<B> flatMap(Function<T, JsResult<B>> map) {
        return map.apply(value);
    }

    @Override
    public JsResult<T> filter(final Function<T, Boolean> p) {
        return this.flatMap(a -> {
            if (p.apply(a)) {
                return new JsSuccess<>(a);
            }
            return new JsError<>(Array.empty());
        });
    }

    @Override
    public JsResult<T> filterNot(final Function<T, Boolean> p) {
        return this.flatMap(a -> {
            if (p.apply(a)) {
                return new JsError<>(Array.empty());
            }
            return new JsSuccess<>(a);
        });
    }

    @Override
    public JsResult<T> filter(final Function<T, Boolean> predicate, final Seq<Throwable> errors) {
        return this.flatMap(a -> {
            if (predicate.apply(a)) {
                return new JsSuccess<>(a);
            }
            return new JsError<>(errors);
        });
    }

    @Override
    public JsResult<T> filterNot(final Function<T, Boolean> predicate, final Seq<Throwable> errors) {
        return this.flatMap(a -> {
            if (predicate.apply(a)) {
                return new JsError<>(errors);
            }
            return new JsSuccess<>(a);
        });
    }

    @Override
    public JsResult<T> filter(final Function<T, Boolean> predicate, final Throwable error) {
        return this.flatMap(a -> {
            if (predicate.apply(a)) {
                return new JsSuccess<>(a);
            }
            return new JsError<>(Array.of(error));
        });
    }

    @Override
    public JsResult<T> filterNot(final Function<T, Boolean> predicate, final Throwable error) {
        return this.flatMap(a -> {
            if (predicate.apply(a)) {
                return new JsError<>(Array.of(error));
            }
            return new JsSuccess<>(a);
        });
    }

    @Override
    public Iterator<T> iterator() {
        return Array.of(value).iterator();
    }

    @Override
    public String toString() {
        return "JsSuccess(" + value +')';
    }
}