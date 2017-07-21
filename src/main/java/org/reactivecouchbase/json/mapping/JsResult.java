package org.reactivecouchbase.json.mapping;

import io.vavr.collection.Array;
import io.vavr.collection.Seq;
import io.vavr.control.Option;

import java.util.function.Function;

public abstract class JsResult<T> implements Iterable<T> {
    public abstract T get();

    public abstract Option<T> getOpt();

    public Option<T> asOpt() {
        return getOpt();
    }

    public abstract JsResult<T> getOrElse(JsResult<T> result);

    public abstract T getValueOrNull();

    public abstract T getValueOrElse(T result);

    public abstract T getValueOrElse(Throwable result);

    public abstract <B> JsResult<B> map(Function<T, B> map);

    public abstract <B> JsResult<B> flatMap(Function<T, JsResult<B>> map);

    public abstract JsResult<T> filter(Function<T, Boolean> predicate);

    public abstract JsResult<T> filterNot(Function<T, Boolean> predicate);

    public abstract JsResult<T> filter(Function<T, Boolean> predicate, Seq<Throwable> errors);

    public abstract JsResult<T> filterNot(Function<T, Boolean> predicate, Seq<Throwable> errors);

    public abstract JsResult<T> filter(Function<T, Boolean> predicate, Throwable errors);

    public abstract JsResult<T> filterNot(Function<T, Boolean> predicate, Throwable errors);

    public abstract boolean hasErrors();

    public abstract boolean isErrors();

    public abstract boolean isSuccess();

    public abstract int countErrors();

    public abstract Option<JsError<T>> asError();

    public abstract Option<JsSuccess<T>> asSuccess();

    public JsError<T> toError() {
        return asError().get();
    }

    public JsSuccess<T> toSuccess() {
        return asSuccess().get();
    }

    public static <T> JsResult<T> error(Throwable t) {
        return new JsError<>(t);
    }

    public static <T> JsResult<T> success(T t) {
        return new JsSuccess<>(t);
    }

    public <X> X fold(Function<JsError<T>, X> onError, Function<JsSuccess<T>, X> onSuccess) {
        if (isErrors()) {
            return onError.apply(this.toError());
        }
        return onSuccess.apply(this.toSuccess());
    }

    public <X> X transform(Function<JsResult<T>, X> trans) {
        return trans.apply(this);
    }

    public Option<Throwable> onError() {
        if (isErrors()) {
            return Option.some(asError().get().firstError());
        }
        return Option.none();
    }

    public Seq<Throwable> onErrors() {
        if (isErrors()) {
            return asError().get().errors;
        }
        return Array.empty();
    }

    public Option<T> onSuccess() {
        if (isSuccess()) {
            return Option.some(get());
        }
        return Option.none();
    }

    public abstract T orError(Throwable t);

    public T recover(Function<JsError, T> block) {
        if (isSuccess()) {
            return get();
        } else {
            return block.apply(asError().get());
        }
    }

    private static <T> JsResult<T> populateErrs(JsResult<T> finalResult, JsResult<?>... results) {
        Seq<Throwable> throwables = Array.empty();
        for (JsResult<?> res : results) {
            if (res.isErrors()) {
                throwables = throwables.appendAll(res.asError().get().errors);
            }
        }
        if (throwables.isEmpty() && finalResult.isSuccess()) {
            return new JsSuccess<T>(finalResult.asSuccess().get().get());
        } else {
            // should never happens
        }
        return new JsError<T>(throwables);
    }
}