package com.github.mayconr.juoserver.network.session.i18n;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public class Utf8Control
        extends ResourceBundle.Control {

    @Override
    public ResourceBundle newBundle(
            String baseName,
            java.util.Locale locale,
            String format,
            ClassLoader loader,
            boolean reload
    ) throws IOException {

        String bundleName =
                toBundleName(baseName, locale);

        String resourceName =
                toResourceName(bundleName, "properties");

        URL url = loader.getResource(resourceName);

        if (url == null) {
            return null;
        }

        URLConnection connection =
                url.openConnection();

        if (reload) {
            connection.setUseCaches(false);
        }

        try (InputStream stream =
                     connection.getInputStream();

             InputStreamReader reader =
                     new InputStreamReader(
                             stream,
                             StandardCharsets.UTF_8
                     )) {

            return new PropertyResourceBundle(reader);
        }
    }
}
