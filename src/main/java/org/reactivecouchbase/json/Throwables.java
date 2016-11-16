package org.reactivecouchbase.json;

public class Throwables {
    public static RuntimeException propagate(Throwable throwable) {
        if (throwable == null) {
            throw new RuntimeException("Exception can not be null");
        }
        throw new RuntimeException(throwable);
    }
}
