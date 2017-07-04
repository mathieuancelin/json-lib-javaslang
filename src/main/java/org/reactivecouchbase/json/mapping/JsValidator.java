package org.reactivecouchbase.json.mapping;

import io.vavr.collection.Array;
import io.vavr.collection.Seq;
import org.reactivecouchbase.json.JsValue;

public class JsValidator<T> implements Reader<T> {

    private final Seq<Reader<T>> validators;
    private final boolean traverse;

    public static <T> JsValidator<T> validateWith(final Class<T> clazz) {
        return new JsValidator<T>(Array.of(DefaultReaders.getReader(clazz).getOrElse(() -> {
            throw new IllegalStateException("No reader found for class " + clazz.getName());
        })), true);
    }

    public static <T> JsValidator<T> validateWith(Reader<T> base) {
        return new JsValidator<T>(Array.of(base), true);
    }

    public static <T> JsValidator<T> of(final Class<T> clazz) {
        return new JsValidator<>(Array.empty(), true);
    }

    public JsValidator<T> traversable() {
        return new JsValidator<>(validators, true);
    }

    public JsValidator<T> failFast() {
        return new JsValidator<>(validators, false);
    }

    public JsValidator(Seq<Reader<T>> validators, boolean traverse) {
        this.validators = validators;
        this.traverse = traverse;
    }

    public JsValidator<T> and(Reader<T> reader) {
        return new JsValidator<>(validators.append(reader), traverse);
    }

    @Override
    public JsResult<T> read(JsValue value) {
        JsResult<T> lastRes = JsResult.error(new RuntimeException("No validators"));
        Seq<Throwable> throwables = Array.empty();
        for (Reader<T> reader : validators) {
            lastRes = reader.read(value);
            if (lastRes.isErrors()) {
                if (!traverse) {
                    return lastRes;
                } else {
                    throwables = throwables.appendAll(lastRes.asError().get().errors);
                }
            }
        }
        if (throwables.isEmpty()) {
            return lastRes;
        } else {
            return new JsError<>(throwables);
        }
    }
}