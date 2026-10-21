package io.github.teggr.teavmshowcase.site;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

class AssembleSiteTest {
    @TempDir
    Path directory;

    @Test
    void publishesEveryAppWithoutChangingStandalonePages() throws IOException {
        Map<String, String> modules = Map.of(
                "play", "platform-game", "create", "drawing-app",
                "connect", "currency-converter", "learn", "number-guessing");
        String original = "<html><head></head><body><main>Java application</main></body></html>";
        for (String module : modules.values()) {
            Path compiled = compiled(module);
            Files.createDirectories(compiled);
            Files.writeString(compiled.resolve("index.html"), original);
            Files.writeString(compiled.resolve("classes.js"), "function main() {}");
        }
        Path output = directory.resolve("output");
        Files.createDirectories(output);
        Files.writeString(output.resolve("index.html"), "Write Java.");

        AssembleSite.main(new String[]{directory.toString(), output.toString()});

        for (var module : modules.entrySet()) {
            Path published = output.resolve("examples").resolve(module.getKey());
            String page = Files.readString(published.resolve("index.html"));
            assertTrue(page.contains("../../assets/demo-theme.css"));
            assertTrue(page.contains("href=\"../../\""));
            assertTrue(page.contains("exhibition-nav"));
            assertEquals(module.getKey().equals("connect"), page.contains("data-static-fixtures=\"true\""));
            assertEquals(module.getKey().equals("create"), page.contains("data-drawing-colors=\"showcase\""));
            assertEquals(original, Files.readString(compiled(module.getValue()).resolve("index.html")));
            assertTrue(Files.isRegularFile(published.resolve("classes.js")));
        }
        assertTrue(Files.isRegularFile(output.resolve(".nojekyll")));
    }

    @Test
    void failsExplicitlyWhenTheTeaVmOutputIsMissing() {
        IOException failure = assertThrows(IOException.class,
                () -> AssembleSite.main(new String[]{directory.toString(), directory.resolve("output").toString()}));
        assertTrue(failure.getMessage().contains("Run mvn verify from the reactor root"));
    }

    private Path compiled(String module) {
        return directory.resolve("teavm-showcase-" + module).resolve("target")
                .resolve(module.equals("currency-converter") ? "classes/static" : "javascript");
    }
}
