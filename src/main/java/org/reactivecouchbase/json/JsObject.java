package org.reactivecouchbase.json;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.*;
import io.vavr.control.Option;
import io.vavr.control.Try;
import org.reactivecouchbase.json.mapping.JsResult;
import org.reactivecouchbase.json.mapping.Reader;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.function.Function;

import static org.reactivecouchbase.json.Syntax.$;
import static org.reactivecouchbase.json.Syntax.nill;

public class JsObject extends JsValue implements Iterable<Tuple2<String, JsValue>> {
    public final Map<String, JsValue> values;

    public static JsObject apply(Map<String, JsValue> values) {
        return new JsObject(values);
    }

    public static JsObject apply() {
        return new JsObject();
    }

    public JsObject(Map<String, JsValue> values) {
        if (values == null) {
            throw new IllegalArgumentException("Values can't be null !");
        }
        this.values = values;
    }

    public JsObject() {
        this.values = HashMap.empty();
    }

    public JsObject merge(JsObject with) {
        if (with == null) {
            throw new IllegalArgumentException("Value can't be null !");
        }
        // return new JsObject(values.merge(with.values));
        return new JsObject(with.values.merge(values));
    }

    @Override
    public Iterator<Tuple2<String, JsValue>> iterator() {
        return values.toList().iterator();
    }

    public JsObject deepMerge(JsObject with) {
        if (with == null) {
            throw new IllegalArgumentException("Value can't be null !");
        }
        Map<String, JsValue> newValues = with.values;
        for (Tuple2<String, JsValue> entry : values.toList()) {
            if (with.values.containsKey(entry._1()) && entry._2().is(JsObject.class)) {
                newValues = newValues.put(entry._1(), entry._2().as(JsObject.class).deepMerge(with.values.get(entry._1()).get().as(JsObject.class)));
            } else {
                newValues = newValues.put(entry._1(), entry._2());
            }
        }
        return new JsObject(newValues);
    }

    public Set<String> fieldsSet() {
        return values.keySet();
    }

    public Seq<JsValue> values() {
        return values.values();
    }

    public JsObject add(JsObject jsObject) {
        if (jsObject == null) {
            return new JsObject(values);
        }
        // return new JsObject(values.merge(jsObject.values));
        return new JsObject(jsObject.values.merge(values));
    }

    public JsObject add(String key, Option<JsValue> optVal) {
        if (optVal.isDefined()) {
            return add($(key, optVal.get()));
        }
        return new JsObject(values);
    }

    public JsObject addOrNull(String key, Option<JsValue> optVal) {
        if (optVal.isDefined()) {
            return add($(key, optVal.get()));
        } else {
            return add($(key, nill()));
        }
    }

    // update only if key is present
    public JsObject update(String key, Function<JsValue, JsValue> value) {
        Option<JsValue> field = this.fieldAsOpt(key);
        for (JsValue val : field) {
            return this.add(key, Option.of(value.apply(val)));
        }
        return this;
    }
    // update only if key is present and option is some
    public JsObject updateOpt(String key, Function<JsValue, Option<JsValue>> value) {
        Option<JsValue> field = this.fieldAsOpt(key);
        for (JsValue val : field) {
            return this.add(key, value.apply(val));
        }
        return this;
    }
    // update or insert at key
    public JsObject upsert(String key, Function<Option<JsValue>, JsValue> value) {
        Option<JsValue> field = this.fieldAsOpt(key);
        JsValue ret = value.apply(field);
        return this.add(key, Option.of(ret));
    }
    // update or insert at key only if returned option is Some
    public JsObject upsertOpt(String key, Function<Option<JsValue>, Option<JsValue>> value) {
        Option<JsValue> field = this.fieldAsOpt(key);
        Option<JsValue> ret = value.apply(field);
        if (ret == null) {
            return this;
        }
        return this.add(key, ret);
    }

    public JsObject remove(String field) {
        if (field == null) {
            return new JsObject(values);
        }
        return new JsObject(values.remove(field));
    }

    @Override
    public JsValue field(String field) {
        if (field == null) {
            return JsUndefined.JSUNDEFINED_INSTANCE;
        }
        return values.get(field).getOrElse(JsUndefined.JSUNDEFINED_INSTANCE);
    }

    @Override
    public Option<JsValue> fieldAsOpt(String field) {
        if (field == null) {
            return Option.none();
        }
        return  values.get(field);
    }

    @Override
    public Seq<JsValue> fields(String fieldName) {
        if (fieldName == null) {
            return Array.empty();
        }
        return values.toList().flatMap(tuple -> {
            String key = tuple._1;
            JsValue value = tuple._2;
            if (key.equals(fieldName)) {
                return Array.of(value);
            }
            for (JsObject obj : value.asOpt(JsObject.class)) {
                return obj.fields(fieldName);
            }
            for (JsObject obj : value.asOpt(JsPair.class)) {
                return obj.fields(fieldName);
            }
            return Array.empty();
        });
    }

    @Override
    String toJsonString() {
        return "{" + toJsonPairString() + "}";
    }

    @Override
    public String toString() {
        return "JsObject(" + toJsonPairString() + ")";
    }

    private String toJsonPairString() {
        return values.toList().map(tuple -> "\"" + tuple._1 + "\":" + tuple._2.toJsonString()).mkString(",");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof JsObject)) {
            return false;
        }
        JsObject object = (JsObject) o;
        if (!values.equals(object.values)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean deepEquals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof JsObject)) {
            return false;
        }
        JsObject object = (JsObject) o;
        for (Tuple2<String, JsValue> value : values.toList()) {
            JsValue field = object.field(value._1());
            if (!field.deepEquals(value._2())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return values.hashCode();
    }

    @Override
    public Boolean exists(String field) {
        return values.containsKey(field);
    }

    public JsObject mapProperties(Function<Tuple2<String, JsValue>, JsValue> block) {
        return new JsObject(values.map(t -> Tuple.of(t._1, block.apply(t))).toMap(t -> t));
    }

    public <T> Map<String, T> mapPropertiesWith(Reader<T> reader) {
        return values.map(t -> {
            JsResult<T> result = reader.read(t._2);
            if (result.isErrors()) {
                throw Throwables.propagate(result.asError().get().firstError());
            } else {
                return Tuple.of(t._1, result.get());
            }
        }).toMap(t -> t);
    }

    public <T> Map<String, T> mapPropertiesWith(Reader<T> reader, Function<JsResult<T>, T> onError) {
        return values.map(t -> {
            JsResult<T> result = reader.read(t._2);
            if (result.isErrors()) {
                return Tuple.of(t._1, onError.apply(result));
            } else {
                return Tuple.of(t._1, result.get());
            }
        }).toMap(t -> t);
    }

    public int nbrOfElements() {
        return size();
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
    public JsObject cloneNode() {
        return new JsObject(HashMap.ofEntries(values));
    }

    public JsObject with(String key) {
        return add(new JsPair(key, JsNull.JSNULL_INSTANCE));
    }

    public JsObject withNull(String key) {
        return add(new JsPair(key, JsNull.JSNULL_INSTANCE));
    }
    public JsObject withUndefined(String key) {
        return add(new JsPair(key, JsUndefined.JSUNDEFINED_INSTANCE));
    }
    public <T extends JsValue> JsObject with(String key, T value) {
        return add(new JsPair(key, value));
    }

    public <T extends JsValue> JsObject with(String key, Option<T> value) {
        return add(key, value.flatMap(v ->
            Try.of(() -> JsValue.class.cast(v)).toOption()
        ));
    }

    public JsObject with(String key, Integer value) {
        return add(new JsPair(key, value));
    }

    public JsObject with(String key, Long value) {
        return add(new JsPair(key, value));
    }

    public JsObject with(String key, Double value) {
        return add(new JsPair(key, value));
    }

    public JsObject with(String key, BigInteger value) {
        return add(new JsPair(key, value));
    }

    public JsObject with(String key, BigDecimal value) {
        return add(new JsPair(key, value));
    }

    public JsObject with(String key, Boolean value) {
        return add(new JsPair(key, value));
    }

    public JsObject with(String key, String value) {
        return add(new JsPair(key, value));
    }

    public JsObject withInt(String key, Option<Integer> value) {
        return add(key, value.map(JsNumber::new));
    }

    public JsObject withLong(String key, Option<Long> value) {
        return add(key, value.map(JsNumber::new));
    }

    public JsObject withDouble(String key, Option<Double> value) {
        return add(key, value.map(JsNumber::new));
    }

    public JsObject withBigInt(String key, Option<BigInteger> value) {
        return add(key, value.map(JsNumber::new));
    }

    public JsObject withBigDec(String key, Option<BigDecimal> value) {
        return add(key, value.map(JsNumber::new));
    }

    public JsObject withBoolean(String key, Option<Boolean> value) {
        return add(key, value.map(JsBoolean::new));
    }

    public JsObject withString(String key, Option<String> value) {
        return add(key, value.map(JsString::new));
    }
}
