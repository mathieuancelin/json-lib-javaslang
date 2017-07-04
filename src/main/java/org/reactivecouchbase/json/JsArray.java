package org.reactivecouchbase.json;

import io.vavr.collection.Array;
import io.vavr.collection.Seq;
import org.reactivecouchbase.json.mapping.JsResult;
import org.reactivecouchbase.json.mapping.Reader;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;

public class JsArray extends JsValue implements Iterable<JsValue> {
    public final Seq<JsValue> values;

    public static JsArray apply(Seq<JsValue> values) {
        return new JsArray(values);
    }

    public static JsArray apply() {
        return new JsArray();
    }

    public JsArray(Seq<JsValue> values) {
        if (values == null) {
            throw new IllegalArgumentException("Values can't be null !");
        }
        this.values = values;
    }

    public JsArray() {
        this.values = Array.empty();
    }

    public boolean contains(JsValue value) {
        return values.contains(value);
    }

    @Override
    public Iterator<JsValue> iterator() {
        return values.iterator();
    }

    @Override
    public JsValue get(int idx) {
        try {
            return values.get(idx);
        } catch (Exception e) {
            return JsUndefined.JSUNDEFINED_INSTANCE;
        }
    }

    public JsArray append(JsArray arr) {
        if (arr == null) {
            return new JsArray(values);
        }
        return new JsArray(values.appendAll(arr.values));
    }

    public JsArray preprend(JsArray arr) {
        if (arr == null) {
            return new JsArray(values);
        }
        return new JsArray(values.prependAll(arr.values));
    }

    public JsArray addElement(JsValue arr) {
        if (arr == null) {
            return new JsArray(values);
        }
        return new JsArray(values.append(arr));
    }

    public JsArray preprendElement(JsValue arr) {
        if (arr == null) {
            return new JsArray(values);
        }
        return new JsArray(values.prepend(arr));
    }

    public JsArray map(Function<JsValue, JsValue> map) {
        return new JsArray(values.map(map));
    }

    public <T> Seq<T> mapWith(Reader<T> reader) {
        return values.map(i -> {
            JsResult<T> result = i.read(reader);
            if (result.hasErrors()) {
                throw Throwables.propagate(result.asError().get().firstError());
            }
            return result.get();
        });
    }

    public <T> Seq<T> mapWith(Reader<T> reader, Function<JsResult<T>, T> onError) {
        return values.map(i -> {
            JsResult<T> result = i.read(reader);
            if (result.hasErrors()) {
                return onError.apply(result);
            }
            return result.get();
        });
    }

    public JsArray filter(Predicate<JsValue> predicate) {
        return new JsArray(values.filter(predicate));
    }

    public JsArray filterNot(final Predicate<JsValue> predicate) {
        return new JsArray(values.filter(predicate.negate()));
    }

    @Override
    String toJsonString() {
        return "[" + values.map(JsValue::toJsonString).mkString(",") + "]";
    }

    @Override
    public String toString() {
        return "JsArray[" + values.map(JsValue::toJsonString).mkString(", ") + "]";
    }

    public int size() {
        return values == null ? 0 : values.size();
    }

    public boolean isEmpty() {
        return values == null || values.isEmpty();
    }

    public boolean notEmpty() {
        return !isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof JsArray)) {
            return false;
        }
        JsArray jsArray = (JsArray) o;
        if (!values.equals(jsArray.values)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean deepEquals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof JsArray)) {
            return false;
        }
        JsArray jsArray = (JsArray) o;
        for (int i = 0; i < size(); i++) {
            JsValue v1 = get(i);
            JsValue v2 = jsArray.get(i);
            if (v1 == null && v2 == null) {
                // we're good
            } else if (v1 != null && v2 == null) {
                return false;
            } else if (v1 == null && v2 != null) {
                return false;
            } else {
                if (!v1.deepEquals(v2)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return values.hashCode();
    }

    @Override
    public JsArray cloneNode() {
        return new JsArray(Array.ofAll(values));
    }
}