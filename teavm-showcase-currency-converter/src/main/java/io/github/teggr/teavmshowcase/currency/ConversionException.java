package io.github.teggr.teavmshowcase.currency;

public class ConversionException extends RuntimeException {
    private final String code;

    public ConversionException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
