package io.github.teggr.teavmshowcase.currency;
import org.teavm.jso.JSBody;
import org.teavm.jso.core.JSArrayReader;

final class ChartInterop {
    private ChartInterop() {
    }

    @JSBody(params = { "history", "source", "target" }, script = """
            const canvas = document.getElementById("rate-chart");
            if (!canvas || !window.Chart) {
                throw new Error("Chart.js or its canvas is unavailable");
            }
            const rows = [];
            if (history && Array.isArray(history)) {
                for (const item of history) {
                    if (item && typeof item.date === "string"
                            && Number.isFinite(item.rate) && item.rate > 0) {
                        rows.push(item);
                    }
                }
            }
            if (window.currencyRateChart) {
                window.currencyRateChart.destroy();
                window.currencyRateChart = undefined;
            }
            if (rows.length === 0) {
                return;
            }
            const chart = new Chart(canvas, {
                type: "line",
                data: {
                    labels: rows.map(item => item.date),
                    datasets: [{
                        label: "1 " + source + " to " + target,
                        data: rows.map(item => item.rate),
                        borderColor: "#1689ef",
                        backgroundColor: "rgb(22 137 239 / 12%)",
                        fill: true,
                        tension: 0.25,
                        pointRadius: 3
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: { legend: { display: true } },
                    scales: {
                        x: { title: { display: true, text: "Demo date" } },
                        y: { title: { display: true, text: "Target units per source unit" } }
                    }
                }
            });
            window.currencyRateChart = chart;
            """)
    static native void update(JSArrayReader<RateHistoryPointView> history, String source, String target);

    @JSBody(script = """
            if (window.currencyRateChart) {
                window.currencyRateChart.destroy();
                window.currencyRateChart = undefined;
            }
            """)
    static native void clear();
}
