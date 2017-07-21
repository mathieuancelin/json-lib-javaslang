package org.reactivecouchbase.json.mapping;

import io.vavr.collection.Array;
import io.vavr.collection.Seq;
import org.reactivecouchbase.json.JsObject;
import org.reactivecouchbase.json.JsValue;
import org.reactivecouchbase.json.Json;

import static org.reactivecouchbase.json.Syntax.$;

public class ThrowableWriter implements Writer<Throwable> {

    private final boolean printStacks;

    public ThrowableWriter(boolean printStacks) {
        this.printStacks = printStacks;
    }

    @Override
    public JsValue write(Throwable value) {
        StackTraceElement[] els = value.getStackTrace();
        Seq<StackTraceElement> elements = Array.empty();
        if (els != null && els.length != 0) {
            elements = elements.appendAll(Array.of(els));
        }
        Seq<String> elementsAsStr = elements.map(StackTraceElement::toString);
        JsObject base = Json.obj(
                $("message", value.getMessage()),
                $("type", value.getClass().getName())
        );
        if (printStacks) {
            base = base.add($("stack", Json.arr(elementsAsStr)));
        }
        if (value.getCause() != null) {
            base = base.add(Json.obj(
                    $("cause", Json.toJson(value.getCause(), new ThrowableWriter(printStacks)))
            ));
        }
        return base;
    }
}