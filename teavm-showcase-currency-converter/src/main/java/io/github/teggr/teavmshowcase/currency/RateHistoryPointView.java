package io.github.teggr.teavmshowcase.currency;

import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;

public interface RateHistoryPointView extends JSObject {
    @JSProperty
    String getDate();

    @JSProperty
    double getRate();
}
