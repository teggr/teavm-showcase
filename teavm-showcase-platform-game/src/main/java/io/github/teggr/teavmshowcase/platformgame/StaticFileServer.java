package io.github.teggr.teavmshowcase.platformgame;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.SimpleWebServer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;

public final class StaticFileServer {
    private static final int PORT = 8001;

    private StaticFileServer() {
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length != 1) {
            throw new IllegalArgumentException("Expected the directory to serve");
        }
        Path directory = Path.of(args[0]).toRealPath();
        if (!Files.isDirectory(directory)) {
            throw new IllegalArgumentException("Not a directory: " + directory);
        }
        SimpleWebServer server = new SimpleWebServer("127.0.0.1", PORT, directory.toFile(), false);
        server.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop, "static-server-shutdown"));
        System.out.println("Serving " + directory + " at http://localhost:" + PORT + "/");
        new CountDownLatch(1).await();
    }
}
