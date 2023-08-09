package com.rogermiranda1000.helper;

/**
 * Expected exceptions, that shouldn't be reported
 */
public class SoftException extends RuntimeException {
    public SoftException(String err) {
        super(err);
    }

    public SoftException(Throwable ex) {
        super(ex);
    }
}
