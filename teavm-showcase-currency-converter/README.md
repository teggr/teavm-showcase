# TeaVM Showcase: Currency Converter

This single Maven module contains the Java/TeaVM browser client and Spring Boot **4.0.8** API, packaged together and served on one origin. The rate fixtures are illustrative and fixed; no live exchange-rate service is used.

## Build and run

From the repository root, build and test the server and its TeaVM client dependency:

```sh
mvn -pl teavm-showcase-currency-converter clean verify
```

Run the executable application produced by that build:

```sh
java -jar teavm-showcase-currency-converter/target/teavm-showcase-currency-converter-1.0-SNAPSHOT.jar
```

Open <http://localhost:8080/>. The app serves `index.html`, TeaVM's generated JavaScript and source map, CSS, the source files used by the source map, and Chart.js from the same origin. Maven copies the static web resources and compiles the TeaVM client into the same module's packaged `static/` directory.

## Conversion API

`POST /api/conversions` accepts JSON:

```json
{"amount":100,"from":"GBP","to":"EUR"}
```

A successful response uses the fixture rates **GBP 1.00, EUR 1.17, USD 1.34, and JPY 201.50 units per GBP**. Its history is a generated illustrative trend around the selected pair's fixed rate:

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

The rate is units of target currency per source unit. The fixed `asOf` date identifies the demonstration fixture; the history entries are generated sample points, not historical market quotes. `BigDecimal` is used for the conversion and the result is rounded to two decimal places with `HALF_UP`. The history is returned by the API so the chart does not carry a second copy of its data.

Amounts must be positive, no greater than **1,000,000**, and have at most **2 decimal places**. The API returns HTTP 400 with a stable `{ "code": "...", "message": "..." }` body for malformed JSON, invalid amounts, and unsupported currency codes. Unexpected API errors return a generic HTTP 500 response without exception details.

Inspect the API directly:

```sh
curl -i http://localhost:8080/api/conversions \
  -H 'Content-Type: application/json' \
  -d '{"amount":100,"from":"GBP","to":"EUR"}'
```

Run only the currency explorer tests with `mvn -pl teavm-showcase-currency-converter test`. The complete repository suite is `mvn clean verify`.

The module combines both browser and server sources under `src/main/java`; browser assets are in `src/main/resources/static`. Chart.js **4.5.1** is served locally, and its MIT license is included at `src/main/resources/static/vendor/CHARTJS-LICENSE.txt`. The client and server use separate model types where their JSON representations differ.

The standalone page always submits conversions to `/api/conversions`. A static host can opt into the same Java conversion fixtures in the browser by setting `data-static-fixtures="true"` on `<body>`; the showcase site applies that override only to its copied example page.
