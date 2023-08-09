package com.rogermiranda1000.helper;

/**
 * Expected exceptions, that shouldn't be reported, but enough to make the plugin crash
 */
public class SoftCriticalException extends SoftException {
    public SoftCriticalException(String err) {
        super(err);
    }

    public SoftCriticalException(Throwable ex) {
        super(ex);
    }
}
