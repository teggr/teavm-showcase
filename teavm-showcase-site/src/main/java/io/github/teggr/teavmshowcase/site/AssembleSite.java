package io.github.teggr.teavmshowcase.site;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;

public final class AssembleSite {
    private AssembleSite() {
    }

    public static void main(String[] args) throws IOException {
        Path root = Path.of(args[0]).toAbsolutePath().normalize();
        Path output = Path.of(args[1]);
        Map<String, String> apps = Map.of(
                "play", "platform-game",
                "create", "drawing-app",
                "connect", "currency-converter",
                "learn", "number-guessing");
        for (var app : apps.entrySet()) {
            String name = app.getValue();
            Path source = root.resolve("teavm-showcase-" + name).resolve("target")
                    .resolve(name.equals("currency-converter") ? "classes/static" : "javascript");
            if (!Files.isRegularFile(source.resolve("classes.js"))) {
                throw new IOException("Missing compiled " + name + ". Run mvn verify from the reactor root first.");
            }
            Path destination = output.resolve("examples").resolve(app.getKey());
            try (var files = Files.walk(source)) {
                for (Path file : files.filter(Files::isRegularFile).toList()) {
                    Path target = destination.resolve(source.relativize(file));
                    Files.createDirectories(target.getParent());
                    Files.copy(file, target, StandardCopyOption.REPLACE_EXISTING);
                }
            }
            Path index = destination.resolve("index.html");
            String page = Files.readString(index)
                    .replace("<body>", "<body class=\"exhibition " + app.getKey() + "\">\n"
                            + "<nav class=\"exhibition-nav\" aria-label=\"Showcase\">"
                            + "<a href=\"../../\">Tea<span>VM</span><b>.</b> <small>SHOWCASE</small></a>"
                            + "<a href=\"../../#examples\">All experiments &rarr;</a></nav>")
                    .replace("</head>", "<link rel=\"stylesheet\" href=\"../../assets/demo-theme.css\">\n</head>");
            if (app.getKey().equals("connect")) {
                page = page.replace("<body class=", "<body data-static-fixtures=\"true\" class=")
                        .replace("API demo history", "Static fixture history");
            }
            Files.writeString(index, page);
        }
        Files.writeString(output.resolve(".nojekyll"), "");
        if (!Files.readString(output.resolve("index.html")).contains("Write Java.")) {
            throw new IOException("Electrostatic did not publish the exhibition homepage.");
        }
    }
}
