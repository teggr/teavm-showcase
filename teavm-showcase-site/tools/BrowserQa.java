//usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 21+
//DEPS com.microsoft.playwright:playwright:1.58.0
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.*;
import java.nio.file.*;
import java.util.*;

class BrowserQa {
    public static void main(String[] args) throws Exception {
        String base = args[0].replaceAll("/$", "");
        Path artifacts = Path.of(args[1]);
        Path previews = args.length > 2 ? Path.of(args[2]) : null;
        Files.createDirectories(artifacts);
        List<String> errors = new ArrayList<>();
        BrowserType.LaunchOptions launch = new BrowserType.LaunchOptions().setHeadless(true);
        Path edge = Path.of("C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe");
        if (Files.isRegularFile(edge)) launch.setExecutablePath(edge);
        try (Playwright playwright = Playwright.create();
             Browser browser = playwright.chromium().launch(launch)) {
            BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                    .setViewportSize(1100, 950).setDeviceScaleFactor(1));
            Page page = context.newPage();
            page.onPageError(errors::add);
            page.onRequest(request -> {
                if (request.url().contains("/api/conversions")) errors.add("Static Pages app called API: " + request.url());
            });
            page.onResponse(response -> {
                if (response.status() >= 400) errors.add(response.status() + " " + response.url());
            });
            page.navigate(base + "/examples/play/");
            page.waitForFunction("document.querySelector('#score').textContent.includes('stars')");
            page.locator("#game-canvas").click();
            page.keyboard().down("ArrowRight");
            page.keyboard().press("Space");
            page.waitForTimeout(450);
            page.keyboard().up("ArrowRight");
            page.locator("#restart").click();
            check(page.locator("#score").innerText().contains("0 / 5"), "Platform restart");
            if (previews != null) capture(page.locator(".game-card"), previews.resolve("play.png"));

            page.navigate(base + "/examples/create/");
            page.waitForFunction("typeof main === 'function'");
            page.locator("#tool-rectangle").click();
            var canvas = page.locator("#drawing-canvas").boundingBox();
            page.mouse().move(canvas.x + canvas.width * .15, canvas.y + canvas.height * .2);
            page.mouse().down();
            page.mouse().move(canvas.x + canvas.width * .45, canvas.y + canvas.height * .55, new Mouse.MoveOptions().setSteps(15));
            page.mouse().up();
            page.locator("#color-purple").click();
            page.locator("#tool-ellipse").click();
            page.mouse().move(canvas.x + canvas.width * .5, canvas.y + canvas.height * .25);
            page.mouse().down();
            page.mouse().move(canvas.x + canvas.width * .8, canvas.y + canvas.height * .7, new Mouse.MoveOptions().setSteps(15));
            page.mouse().up();
            check(page.locator("#tool-ellipse").getAttribute("aria-pressed").equals("true"), "Drawing tool selection");
            page.locator("#color-amber").click();
            page.locator("#tool-rectangle").click();
            page.mouse().move(canvas.x + canvas.width * .4, canvas.y + canvas.height * .6);
            page.mouse().down();
            page.mouse().move(canvas.x + canvas.width * .65, canvas.y + canvas.height * .87, new Mouse.MoveOptions().setSteps(15));
            page.mouse().up();
            page.locator("#color-red").click();
            page.locator("#tool-arrow").click();
            page.mouse().move(canvas.x + canvas.width * .2, canvas.y + canvas.height * .75);
            page.mouse().down();
            page.mouse().move(canvas.x + canvas.width * .75, canvas.y + canvas.height * .3, new Mouse.MoveOptions().setSteps(15));
            page.mouse().up();
            page.locator("#color-blue").click();
            if (previews != null) capture(page.locator(".workspace"), previews.resolve("create.png"));

            page.navigate(base + "/examples/connect/");
            page.locator("#amount").fill("100");
            page.locator("#to").selectOption("USD");
            page.locator("#submit-button").click();
            page.waitForFunction("document.querySelector('#result').textContent.includes('134.00')");
            check(page.locator("#status").innerText().contains("Static demo fixtures"), "Explicit fixture mode");
            check((Boolean) page.evaluate("!!window.currencyRateChart && window.currencyRateChart.data.labels.length === 7"), "Currency chart");
            if (previews != null) capture(page.locator(".explorer"), previews.resolve("connect.png"));
            for (String from : List.of("GBP", "EUR", "USD", "JPY")) {
                for (String to : List.of("GBP", "EUR", "USD", "JPY")) {
                    page.locator("#from").selectOption(from);
                    page.locator("#to").selectOption(to);
                    page.locator("#submit-button").click();
                    check(!page.locator("#result").innerText().isBlank(), "Currency pair " + from + "/" + to);
                    Map<String, Double> rates = Map.of("GBP", 1.0, "EUR", 1.17, "USD", 1.34, "JPY", 201.5);
                    double expected = rates.get(to) / rates.get(from);
                    double actual = ((Number) page.evaluate("window.currencyRateChart.data.datasets[0].data.at(-1)")).doubleValue();
                    check(Math.abs(expected - actual) < 0.00000001, "Exact fixture chart rate " + from + "/" + to);
                }
            }
            page.locator("#amount").fill("0");
            page.locator("#submit-button").click();
            check(page.locator("#status").innerText().contains("Enter an amount"), "Invalid currency amount");

            page.navigate(base + "/examples/learn/");
            page.locator("#guess").fill("5");
            page.locator("#guess-button").click();
            check(page.locator("#guess-count").innerText().equals("1"), "Number guessing");
            if (previews != null) capture(page.locator(".game-card"), previews.resolve("learn.png"));
            for (int number = 1; number <= 10 && !page.locator("#new-game-button").isVisible(); number++) {
                page.locator("#guess").fill(Integer.toString(number));
                page.locator("#guess-button").click();
            }
            check(page.locator("#new-game-button").isVisible(), "Number game win");
            page.locator("#new-game-button").click();
            check(page.locator("#guess-count").innerText().equals("0"), "Number game reset");

            page.setViewportSize(1672, 941);
            page.navigate(base + "/");
            page.evaluate("document.fonts.ready");
            page.screenshot(new Page.ScreenshotOptions().setPath(artifacts.resolve("desktop-1672.png")));
            var strip = page.locator(".feature-strip").boundingBox();
            check(strip.y >= 830 && strip.y <= 850 && strip.y + strip.height <= 941, "Reference first-screen composition");
            for (Locator link : page.locator("a[href^='#']").all()) {
                String id = link.getAttribute("href").substring(1);
                check(page.locator("[id='" + id + "']").count() == 1, "Anchor target " + id);
            }
            check(page.locator(".showcase a[href^='examples/']").count() == 8, "All preview and demo links");
            System.out.println("Desktop geometry: " + page.evaluate("""
                    Object.fromEntries(['.site-header','.hero','h1','.showcase','.feature-strip'].map(s=>{
                      const r=document.querySelector(s).getBoundingClientRect();return [s,{x:r.x,y:r.y,w:r.width,h:r.height}]}))
                    """));
            for (int width : new int[]{1024, 820, 390}) {
                page.setViewportSize(width, 950);
                page.screenshot(new Page.ScreenshotOptions().setPath(artifacts.resolve("layout-" + width + ".png")).setFullPage(true));
                check((Boolean) page.evaluate("document.documentElement.scrollWidth <= innerWidth"), "No overflow at " + width);
            }
            page.emulateMedia(new Page.EmulateMediaOptions().setReducedMotion(ReducedMotion.REDUCE));
            check(page.evaluate("getComputedStyle(document.documentElement).scrollBehavior").equals("auto"), "Reduced motion");
            for (String name : List.of("play", "create", "connect", "learn")) {
                page.navigate(base + "/examples/" + name + "/");
                check((Boolean) page.evaluate("document.documentElement.scrollWidth <= innerWidth"), "Mobile app " + name);
                page.screenshot(new Page.ScreenshotOptions().setPath(artifacts.resolve("mobile-" + name + ".png")).setFullPage(true));
            }
            if (!errors.isEmpty()) throw new AssertionError(String.join("\n", errors));
            System.out.println("PASS: four real apps, 16 currency pairs, invalid input, win/reset, desktop/tablet/mobile, reduced motion, no browser/network errors.");
        }
    }

    static void capture(Locator locator, Path file) throws Exception {
        Files.createDirectories(file.getParent());
        locator.screenshot(new Locator.ScreenshotOptions().setPath(file));
    }

    static void check(boolean condition, String label) {
        if (!condition) throw new AssertionError(label);
    }
}
