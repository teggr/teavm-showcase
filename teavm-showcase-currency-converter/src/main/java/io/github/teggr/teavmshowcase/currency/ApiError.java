package io.github.teggr.teavmshowcase.currency;

import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;

public interface ApiError extends JSObject {
    @JSProperty
    String getCode();

    @JSProperty
    String getMessage();
}
