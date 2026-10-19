package io.github.teggr.teavmshowcase.currency;
import org.teavm.jso.JSBody;
import org.teavm.jso.dom.events.Event;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLElement;
import org.teavm.jso.dom.html.HTMLInputElement;
import org.teavm.jso.dom.html.HTMLSelectElement;
import org.teavm.jso.ajax.XMLHttpRequest;
import org.teavm.jso.json.JSON;

public final class CurrencyConverterApp {
    private final HTMLInputElement amountInput;
    private final HTMLSelectElement fromInput;
    private final HTMLSelectElement toInput;
    private final HTMLElement submitButton;
    private final HTMLElement result;
    private final HTMLElement status;
    private long requestSequence;
    private boolean requestInProgress;

    private CurrencyConverterApp() {
        HTMLDocument document = HTMLDocument.current();
        HTMLElement form = requireElement(document, "conversion-form");
        amountInput = requireInput(document, "amount");
        fromInput = requireSelect(document, "from");
        toInput = requireSelect(document, "to");
        submitButton = requireElement(document, "submit-button");
        result = requireElement(document, "result");
        status = requireElement(document, "status");
        form.addEventListener("submit", event -> submit((Event) event));
    }

    public static void main(String[] args) {
        new CurrencyConverterApp();
    }

    private static HTMLElement requireElement(HTMLDocument document, String id) {
        HTMLElement element = document.getElementById(id);
        if (element == null) {
            throw new IllegalStateException("Required page element is missing: " + id);
        }
        return element;
    }

    private static HTMLInputElement requireInput(HTMLDocument document, String id) {
        HTMLElement element = requireElement(document, id);
        if (!(element instanceof HTMLInputElement)) {
            throw new IllegalStateException("Page element must be an input: " + id);
        }
        return (HTMLInputElement) element;
    }

    private static HTMLSelectElement requireSelect(HTMLDocument document, String id) {
        HTMLElement element = requireElement(document, id);
        if (!(element instanceof HTMLSelectElement)) {
            throw new IllegalStateException("Page element must be a select: " + id);
        }
        return (HTMLSelectElement) element;
    }

    private void submit(Event event) {
        event.preventDefault();
        if (requestInProgress) {
            return;
        }

        String amount = AmountValidator.normalize(amountInput.getValue());
        if (amount == null) {
            result.setTextContent("");
            status.setTextContent("Enter an amount greater than 0, up to 1,000,000, with no more than 2 decimal places.");
            return;
        }
        String from = fromInput.getValue();
        String to = toInput.getValue();
        long requestId = ++requestSequence;
        requestInProgress = true;
        submitButton.setAttribute("disabled", "");
        result.setTextContent("");
        status.setTextContent("Converting…");
        try {
            ChartInterop.clear();
        } catch (RuntimeException e) {
            status.setTextContent("Converting…");
        }

        XMLHttpRequest request = new XMLHttpRequest();
        request.open("POST", "/api/conversions", true);
        request.setRequestHeader("Content-Type", "application/json");
        request.setOnReadyStateChange(readyStateEvent -> {
            if (request.getReadyState() == XMLHttpRequest.DONE) {
                finishRequest(requestId, request);
            }
        });
        request.onError(errorEvent -> finishNetworkError(requestId));
        request.send("{\"amount\":" + amount + ",\"from\":\"" + from + "\",\"to\":\"" + to + "\"}");
    }

    private void finishRequest(long requestId, XMLHttpRequest request) {
        if (requestId != requestSequence || !requestInProgress) {
            return;
        }
        if (request.getStatus() == 0) {
            finishNetworkError(requestId);
            return;
        }
        requestInProgress = false;
        submitButton.removeAttribute("disabled");
        if (request.getStatus() < 200 || request.getStatus() >= 300) {
            status.setTextContent(readApiError(request.getResponseText()));
            return;
        }

        try {
            ConversionResponseView response = (ConversionResponseView) JSON.parse(request.getResponseText());
            if (!isValid(response)) {
                throw new IllegalArgumentException("Invalid conversion response");
            }
            result.setTextContent(formatMoney(response.getAmount(), response.getFrom())
                    + " = " + formatMoney(response.getResult(), response.getTo()));
            status.setTextContent("Rate: 1 " + response.getFrom() + " = "
                    + response.getRate() + " " + response.getTo()
                    + " · Fixture rates as of " + response.getAsOf() + ".");
            try {
                ChartInterop.update(response.getHistory(), response.getFrom(), response.getTo());
            } catch (RuntimeException e) {
                status.setTextContent(status.getTextContent() + " The rate history chart could not be updated.");
            }
        } catch (RuntimeException e) {
            result.setTextContent("");
            status.setTextContent("The server returned an invalid response. Please try again.");
        }
    }

    private void finishNetworkError(long requestId) {
        if (requestId != requestSequence || !requestInProgress) {
            return;
        }
        requestInProgress = false;
        submitButton.removeAttribute("disabled");
        status.setTextContent("Unable to reach the conversion service. Check your connection and try again.");
    }

    private static String readApiError(String body) {
        try {
            ApiError error = (ApiError) JSON.parse(body);
            String message = error.getMessage();
            if (message != null && !message.isEmpty()) {
                return message;
            }
        } catch (RuntimeException e) {
            return "The conversion service returned an error. Please try again.";
        }
        return "The conversion service returned an error. Please try again.";
    }

    private static boolean isValid(ConversionResponseView response) {
        return response != null
                && Double.isFinite(response.getAmount()) && response.getAmount() > 0
                && Double.isFinite(response.getRate()) && response.getRate() > 0
                && Double.isFinite(response.getResult()) && response.getResult() >= 0
                && supportedCurrency(response.getFrom())
                && supportedCurrency(response.getTo())
                && response.getAsOf() != null && !response.getAsOf().isEmpty();
    }

    private static boolean supportedCurrency(String currency) {
        return "GBP".equals(currency) || "EUR".equals(currency)
                || "USD".equals(currency) || "JPY".equals(currency);
    }

    private static String formatMoney(double amount, String currency) {
        return formatCurrency(amount, currency);
    }

    @JSBody(params = { "amount", "currency" }, script = """
            return new Intl.NumberFormat("en-GB", {
                style: "currency",
                currency: currency,
                minimumFractionDigits: 2,
                maximumFractionDigits: 2
            }).format(amount);
            """)
    private static native String formatCurrency(double amount, String currency);
}
