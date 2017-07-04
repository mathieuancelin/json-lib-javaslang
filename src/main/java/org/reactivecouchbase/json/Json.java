package org.reactivecouchbase.json;

import com.fasterxml.jackson.databind.JsonNode;
import io.vavr.collection.Array;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import io.vavr.control.Validation;
import org.reactivecouchbase.json.mapping.*;

import java.util.function.Function;

public class Json {

    public static JsObject obj(Map<String, ?> objects) {
        return new JsObject(objects.bimap(k -> k, Json::wrap));
    }

    public static <T> Format<T> format(final Class<T> clazz) {
        final Writer<T> writer = Json.writes(clazz);
        final Reader<T> reader = Json.reads(clazz);
        return new Format<T>() {
            @Override
            public JsResult<T> read(JsValue value) {
                return reader.read(value);
            }

            @Override
            public JsValue write(T value) {
                return writer.write(value);
            }
        };
    }

    public static <T> Reader<T> reads(final Class<T> clazz) {
        if (DefaultReaders.readers.containsKey(clazz)) {
            return (Reader<T>) DefaultReaders.readers.get(clazz).get();
        }
        return value -> {
            try {
                return new JsSuccess<T>(Jackson.fromJson(Jackson.jsValueToJsonNode(value), clazz));
            } catch (Exception e) {
                return new JsError<T>(Array.of(e));
            }
        };
    }

    public static <T> Writer<T> writes(final Class<T> clazz) {
        return value -> Jackson.jsonNodeToJsValue(Jackson.toJson(value));
    }

    public static JsValue toJson(Object o) {
        return Jackson.jsonNodeToJsValue(Jackson.toJson(o));
    }

    public static JsValue parse(String json) {
        return Jackson.parseJsValue(json);
    }

    public static <T> Reader<T> safeReader(final Reader<T> reader) {
        return value -> {
            try {
                return reader.read(value);
            } catch (Exception e) {
                return JsResult.error(e);
            }
        };
    }

    public static JsObject obj(Iterable<? extends JsObject> objects) {
        JsObject root = new JsObject();
        for (JsObject object : objects) {
            root = root.add(object);
        }
        return root;
    }

    public static JsObject obj(JsObject... objects) {
        return obj(Array.of(objects));
    }

    public static JsObject obj() {
        return new JsObject();
    }

    public static <T extends Object> JsArray array(Seq<T> objects) {
        return new JsArray(objects.map(Json::wrap));
    }

    @SuppressWarnings("unchecked")
    public static JsArray arr(Object... objects) {
        if (objects != null && objects.length == 1 && Seq.class.isAssignableFrom(objects[0].getClass())) {
            return array((Seq<Object>) objects[0]);
        }
        return array(Array.of(objects));
    }

    public static <T> JsArray arr(Seq<T> collection, final Writer<T> writer) {
        return Json.arr(collection.map(writer::write));
    }

    public static String stringify(JsValue value) {
        return value.toJsonString();
    }

    public static String stringify(JsValue value, boolean pretty) {
        if (pretty) {
            return prettyPrint(value);
        }
        return stringify(value);
    }

    @SuppressWarnings("unchecked")
    public static JsValue wrap(Object o) {
        return Jackson.jsonNodeToJsValue(Jackson.toJson(o));
    }

    public static <T> JsResult<T> fromJson(JsValue value, Reader<T> reader) {
        return reader.read(value);
    }

    public static <T> JsResult<T> fromJson(String value, Reader<T> reader) {
        return reader.read(Json.parse(value));
    }

    public static <E, T> Validation<E, T> fromJson(JsValue value, Function<JsValue, Validation<E, T>> reader) {
        return reader.apply(value);
    }

    public static <E, T> Validation<E, T> fromJson(String value, Function<JsValue, Validation<E, T>> reader) {
        return fromJson(Json.parse(value), reader);
    }

    public static <T, V extends T> JsValue toJson(V o, Writer<T> writer) {
        return writer.write(o);
    }

    public static <T> JsonNode toJackson(JsValue value) {
        return Jackson.toJson(value);
    }

    public static String prettyPrint(JsValue value) {
        return Jackson.prettify(value);
    }

    public static JsValue fromJsonNode(JsonNode node) {
        return Jackson.jsonNodeToJsValue(node);
    }
}