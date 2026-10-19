package io.github.teggr.teavmshowcase.currency;

public final class AmountValidator {
    public static final String MAX_AMOUNT = "1000000";

    private AmountValidator() {
    }

    public static String normalize(String input) {
        if (input == null) {
            return null;
        }
        String value = input.trim();
        if (value.isEmpty()) {
            return null;
        }

        int decimalPoint = -1;
        for (int i = 0; i < value.length(); ++i) {
            char c = value.charAt(i);
            if (c == '.') {
                if (decimalPoint >= 0) {
                    return null;
                }
                decimalPoint = i;
            } else if (c < '0' || c > '9') {
                return null;
            }
        }
        if (decimalPoint == 0 || decimalPoint == value.length() - 1 && decimalPoint >= 0) {
            return null;
        }
        if (decimalPoint >= 0 && value.length() - decimalPoint - 1 > 2) {
            return null;
        }

        String whole = decimalPoint >= 0 ? value.substring(0, decimalPoint) : value;
        String fraction = decimalPoint >= 0 ? value.substring(decimalPoint + 1) : "";
        int firstNonZero = 0;
        while (firstNonZero < whole.length() - 1 && whole.charAt(firstNonZero) == '0') {
            firstNonZero++;
        }
        whole = whole.substring(firstNonZero);

        if (whole.length() > MAX_AMOUNT.length()
                || whole.length() == MAX_AMOUNT.length() && whole.compareTo(MAX_AMOUNT) > 0
                || whole.equals(MAX_AMOUNT) && !isZero(fraction)) {
            return null;
        }
        if (whole.equals("0") && isZero(fraction)) {
            return null;
        }
        return fraction.isEmpty() ? whole : whole + "." + fraction;
    }

    private static boolean isZero(String value) {
        for (int i = 0; i < value.length(); ++i) {
            if (value.charAt(i) != '0') {
                return false;
            }
        }
        return true;
    }
}
