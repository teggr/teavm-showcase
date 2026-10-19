# Article support: TeaVM currency explorer

## Verified module structure

```text
teavm-showcase-currency-converter/
  src/main/java/io/github/teggr/teavmshowcase/currency/
    AmountValidator.java
    ApiError.java
    ApiExceptionHandler.java
    ChartInterop.java
    ConversionController.java
    ConversionError.java
    ConversionException.java
    ConversionRequest.java
    ConversionResponse.java
    ConversionResponseView.java
    ConversionService.java
    ExchangeRates.java
    RateHistoryPoint.java
    RateHistoryPointView.java
    StrictBigDecimalDeserializer.java
    CurrencyConverterServer.java
    CurrencyConverterApp.java
  src/main/resources/static/
    bootstrap.js
    favicon.svg
    index.html
    styles.css
    vendor/chart.umd.min.js
    vendor/CHARTJS-LICENSE.txt
  src/main/resources/application.properties
  src/test/java/io/github/teggr/teavmshowcase/currency/
    AmountValidatorTest.java
    ConversionApiTest.java
    ConversionServiceTest.java
```

The existing `teavm-showcase-number-guessing` module remains unchanged. All Java code for the currency demo lives in the root currency package to keep the example simple; Maven copies the static browser resources during `process-resources` and writes the generated TeaVM app directly into the module's `static/` classpath resources.

## Versions and generated output

- Java compiler release: **17** (the build was run with Maven 3.9.16 and JDK 25.0.1).
- TeaVM compiler and JSO APIs: **0.16.0**.
- Spring Boot: **4.0.8**, pinned and verified against Maven Central.
- Chart.js: **4.5.1**, pinned local UMD distribution; MIT license text is included at `teavm-showcase-currency-converter/src/main/resources/static/vendor/CHARTJS-LICENSE.txt`.
- TeaVM output: `teavm-showcase-currency-converter/target/classes/static/`.
- The packaged application serves those files from `/`, with the source map and copied source paths retained under the generated `src/` tree. Build output is not committed.

## API request and response

Request:

```http
POST /api/conversions
Content-Type: application/json
```

```json
{"amount":100,"from":"GBP","to":"EUR"}
```

Observed HTTP 200 response:

```json
{
  "amount": 100,
  "from": "GBP",
  "to": "EUR",
  "rate": 1.1700000000,
  "result": 117.00,
  "asOf": "2026-10-18",
  "history": [
    {"date": "2026-10-12", "rate": 1.1524500000},
    {"date": "2026-10-13", "rate": 1.1606400000},
    {"date": "2026-10-14", "rate": 1.1571300000},
    {"date": "2026-10-15", "rate": 1.1746800000},
    {"date": "2026-10-16", "rate": 1.1840400000},
    {"date": "2026-10-17", "rate": 1.1770200000},
    {"date": "2026-10-18", "rate": 1.1700000000}
  ]
}
```

The fixed units-per-GBP fixtures are GBP 1.00, EUR 1.17, USD 1.34 and JPY 201.50. The `history` array contains seven generated illustrative values for the selected pair, ending at the fixture rate on `asOf`. These are synthetic demo points and not historical market data. Amounts are limited to 1,000,000 and two input decimal places.

Invalid requests return HTTP 400 and a stable JSON error, for example:

```json
{"code":"UNSUPPORTED_CURRENCY","message":"Choose one of the supported currencies."}
```

## TeaVM client and request lifecycle

From `src/main/java/io/github/teggr/teavmshowcase/currency/CurrencyConverterApp.java`, the request layer uses the TeaVM 0.16.0 `org.teavm.jso.ajax.XMLHttpRequest` wrapper, opens the request asynchronously, and handles it when ready:

```java
request.open("POST", "/api/conversions", true);
request.setRequestHeader("Content-Type", "application/json");
request.setOnReadyStateChange(readyStateEvent -> {
    if (request.getReadyState() == XMLHttpRequest.DONE) {
        finishRequest(requestId, request);
    }
});
request.onError(errorEvent -> finishNetworkError(requestId));
request.send("{\"amount\":" + amount + ",\"from\":\"" + from + "\",\"to\":\"" + to + "\"}");
```

The TeaVM JSO `JSON.parse` overlay decodes the response into `ConversionResponse`. The client validates the fields, clears stale result text while loading, disables duplicate submits, reports network and API failures without rendering raw response bodies, and restores the form after completion.

`AmountValidator.normalize` rejects invalid syntax, non-positive values, amounts over 1,000,000, and more than two fractional digits before the client sends a request. It uses string comparisons rather than binary floating-point for those input checks; server-side validation remains authoritative.

## Server calculation and validation

From `src/main/java/io/github/teggr/teavmshowcase/currency/ConversionService.java`:

```java
BigDecimal fromRate = requireRate(request.getFrom());
BigDecimal toRate = requireRate(request.getTo());
BigDecimal rate = toRate.divide(fromRate, ExchangeRates.RATE_SCALE, RoundingMode.HALF_UP);
BigDecimal result = amount.multiply(toRate).divide(fromRate, 2, RoundingMode.HALF_UP);
```

The dated demo series is derived from the selected pair's rate using fixed factors held with the server fixtures; the final sample is the selected conversion rate. Same-currency history remains constant at one:

```java
BigDecimal pointRate = from.equals(to)
        ? BigDecimal.ONE.setScale(ExchangeRates.RATE_SCALE)
        : rate.multiply(new BigDecimal(ExchangeRates.DEMO_RATE_FACTORS.get(i)))
                .setScale(ExchangeRates.RATE_SCALE, RoundingMode.HALF_UP);
```

`src/main/java/io/github/teggr/teavmshowcase/currency/ConversionController.java` exposes the JSON endpoint:

```java
@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
public ConversionResponse convert(@RequestBody(required = false) JsonNode body) {
    if (body == null || body.isNull()) {
        return conversionService.convert(null);
    }
    if (!body.isObject()) {
        throw new ConversionException("INVALID_REQUEST", "A conversion request object is required.");
    }

    JsonNode amount = body.get("amount");
    JsonNode from = body.get("from");
    JsonNode to = body.get("to");
    return conversionService.convert(new ConversionRequest(
            amount != null && amount.isNumber() ? amount.decimalValue() : null,
            from != null && from.isString() ? from.asText() : null,
            to != null && to.isString() ? to.asText() : null));
}
```

`ExchangeRates.java` is the single domain source for demo rates, history factors, fixture date and request limits. `ApiExceptionHandler.java` maps expected validation and JSON parsing failures to stable 400 responses and logs unexpected API failures while returning a generic 500 body.

## Chart.js line-chart boundary

`ChartInterop.java` is the only Chart.js bridge. Its `@JSBody` method filters API-supplied history points, destroys the previous chart, and either creates the new line chart or gracefully leaves it clear if there are no usable points:

```javascript
if (history && Array.isArray(history)) {
    for (const item of history) {
        if (item && typeof item.date === "string"
                && Number.isFinite(item.rate) && item.rate > 0) {
            rows.push(item);
        }
    }
}
const chart = new Chart(canvas, {
    type: "line",
    data: {
        labels: rows.map(item => item.date),
        datasets: [{ label: "1 " + source + " to " + target,
                     data: rows.map(item => item.rate) }]
    }
});
```

The local script is loaded from `vendor/chart.umd.min.js`; no CDN request is required. Result rendering is independent of chart errors.

## Build and run evidence

From the repository root, both commands were executed successfully:

```sh
mvn -q clean verify
java -jar teavm-showcase-currency-converter/target/teavm-showcase-currency-converter-1.0-SNAPSHOT.jar
```

The clean reactor verification included the existing number-guessing module and the combined currency explorer build/tests: **16 tests passed, 0 failures, 0 errors** (6 existing game tests, 2 client validation tests, 3 API tests and 5 conversion-service tests). A live `curl` request returned HTTP 200 with GBP 100 → EUR 117.00; a CAD request returned HTTP 400 with `UNSUPPORTED_CURRENCY`. The executable JAR contained the static index, TeaVM output/source map, CSS, Chart.js and license.

## Browser verification and screenshot

The application was opened at `http://localhost:8080/` in Chrome **155.0.8059.39** (headless Chromium over the DevTools Protocol and the shared browser canvas). Verified scenarios:

- Page and local Chart.js assets loaded from the Spring Boot origin.
- GBP 100 → EUR returned and displayed **£100.00 = €117.00**; chart showed the API values for EUR, USD and JPY.
- EUR 100 → USD displayed **€100.00 = US$114.53** and updated the line chart with the API's EUR-to-USD demo history.
- The selected conversion pair's line chart used the response's dated sample points; all points were labeled as illustrative rather than historical market data.
- Pressing Enter in the amount field submitted the valid form once and completed a GBP 100 → EUR conversion.
- Empty, negative and over-precision amounts displayed validation feedback and did not send an API request.
- Unsupported currency returned a stable API error.
- A delayed response kept “Converting…” visible, disabled the submit button, and cleared the previous result until completion.
- A malformed success payload displayed a safe message; a response with no history left the conversion visible and cleared the chart.
- A simulated connection refusal showed a useful network message, re-enabled the form, and a following valid conversion succeeded.
- No uncaught JavaScript errors were observed during those scenarios.

The actual successful-conversion screenshot is `teavm-showcase-currency-converter/docs/currency-explorer.png`.

The implementation brief was supplied as an attachment; no separate Article 2 draft file was present in the repository to edit. This verified support document is ready to be used when updating that draft.
