package org.reactivecouchbase.json.mapping;

import io.vavr.collection.Array;
import io.vavr.collection.Seq;
import io.vavr.control.Option;
import org.reactivecouchbase.json.JsArray;
import org.reactivecouchbase.json.Json;
import org.reactivecouchbase.json.Throwables;

import java.util.Iterator;
import java.util.function.Function;

public class JsError<T> extends JsResult<T> {

    public final Seq<Throwable> errors;

    @Override
    public T getValueOrElse(T result) {
        return result;
    }

    @Override
    public T getValueOrNull() {
        return null;
    }

    @Override
    public T getValueOrElse(Throwable result) {
        throw Throwables.propagate(result);
    }

    @Override
    public Option<JsError<T>> asError() {
        return Option.some(this);
    }

    @Override
    public Option<JsSuccess<T>> asSuccess() {
        return Option.none();
    }

    @Override
    public boolean hasErrors() {
        return true;
    }

    @Override
    public boolean isErrors() {
        return true;
    }

    @Override
    public boolean isSuccess() {
        return false;
    }

    @Override
    public int countErrors() {
        return errors.size();
    }

    @Override
    public T orError(Throwable t) {
        throw Throwables.propagate(t);
    }

    @Override
    public T get() {
        throw new IllegalStateException("No value");
    }

    @Override
    public Option<T> getOpt() {
        return Option.none();
    }

    @Override
    public JsResult<T> getOrElse(JsResult<T> result) {
        return result;
    }

    @Override
    public <B> JsResult<B> map(Function<T, B> map) {
        return new JsError<>(errors);
    }

    @Override
    public <B> JsResult<B> flatMap(Function<T, JsResult<B>> map) {
        return new JsError<>(errors);
    }

    @Override
    public JsResult<T> filter(Function<T, Boolean> predicate) {
        return new JsError<>(errors);
    }

    @Override
    public JsResult<T> filterNot(Function<T, Boolean> predicate) {
        return new JsError<>(errors);
    }

    @Override
    public JsResult<T> filter(Function<T, Boolean> predicate, Seq<Throwable> errs) {
        JsResult<T> val = this;
        if (val.isSuccess() && predicate.apply(val.get())) {
            return new JsError<>(this.errors.appendAll(errs));
        }
        return new JsError<T>(this.errors);
    }

    @Override
    public JsResult<T> filterNot(Function<T, Boolean> predicate, Seq<Throwable> errs) {
        JsResult<T> val = this;
        if (val.isSuccess() && !predicate.apply(val.get())) {
            return new JsError<>(this.errors.appendAll(errs));
        }
        return new JsError<T>(this.errors);
    }

    @Override
    public JsResult<T> filter(Function<T, Boolean> predicate, Throwable error) {
        JsResult<T> val = this;
        if (val.isSuccess() && predicate.apply(val.get())) {
            return new JsError<>(this.errors.append(error));
        }
        return new JsError<T>(this.errors);
    }

    @Override
    public JsResult<T> filterNot(Function<T, Boolean> predicate, Throwable error) {
        JsResult<T> val = this;
        if (val.isSuccess() && !predicate.apply(val.get())) {
            return new JsError<>(this.errors.append(error));
        }
        return new JsError<T>(this.errors);
    }

    public JsError(Seq<Throwable> errors) {
        this.errors = errors;
    }

    public JsError(Throwable errors) {
        this.errors = Array.of(errors);
    }

    public Throwable firstError() {
        if (errors.isEmpty()) {
            return new IllegalAccessError("No error, that's weird !!!");
        }
        return errors.iterator().next();
    }

    @Override
    public Iterator<T> iterator() {
        return Array.<T>empty().iterator();
    }

    public JsArray errors() {
        return Json.arr(errorsAsString());
    }

    public Seq<String> errorsAsString() {
        return errors.map(Throwable::getMessage);
    }

    @Override
    public String toString() {
        return "JsError(" + errors + ')';
    }
}