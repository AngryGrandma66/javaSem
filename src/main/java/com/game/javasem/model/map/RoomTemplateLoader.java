package com.game.javasem.model.map;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RoomTemplateLoader {

    /**
     * Load every JSON file from the "layouts" directory on the classpath.
     *
     * @return a List of RoomTemplate
     * @throws IOException if the layouts directory cannot be found or read
     */
    public static List<RoomTemplate> loadAll() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        // 1) Find the "layouts" folder on the classpath
        URL layoutsUrl = Thread.currentThread()
                .getContextClassLoader()
                .getResource("com/game/javasem/layouts");
        if (layoutsUrl == null) {
            throw new IOException("Could not find 'layouts' directory on classpath");
        }

        // 2) Resolve to a Path, handling both exploded‐dir and jar‐packed cases
        Path layoutsPath;
        try {
            URI uri = layoutsUrl.toURI();
            if (uri.getScheme().equals("jar")) {
                FileSystem fs = FileSystems.newFileSystem(uri, Collections.emptyMap());
                // in a jar, the path to entries is exactly "/layouts"
                layoutsPath = fs.getPath("/layouts");
            } else {
                // exploded: file://.../target/classes/layouts
                layoutsPath = Paths.get(uri);
            }
        } catch (Exception e) {
            throw new IOException("Failed to resolve 'layouts' directory", e);
        }

        // 3) Stream every .json under layoutsPath
        List<RoomTemplate> templates = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(layoutsPath, "*.json")) {
            for (Path p : stream) {
                templates.add(mapper.readValue(p.toFile(), RoomTemplate.class));
            }
        }

        if (templates.isEmpty()) {
            throw new IOException("No JSON files found in 'layouts' directory");
        }

        return templates;
    }
}
