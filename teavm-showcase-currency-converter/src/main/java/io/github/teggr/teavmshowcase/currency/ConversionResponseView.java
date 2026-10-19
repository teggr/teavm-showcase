package io.github.teggr.teavmshowcase.currency;

import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;
import org.teavm.jso.core.JSArrayReader;

public interface ConversionResponseView extends JSObject {
    @JSProperty
    double getAmount();

    @JSProperty
    String getFrom();

    @JSProperty
    String getTo();

    @JSProperty
    double getRate();

    @JSProperty
    double getResult();

    @JSProperty
    String getAsOf();

    @JSProperty
    JSArrayReader<RateHistoryPointView> getHistory();
}
