package com.clientjourney.app;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.exceptions.HttpStatusException;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Controller
public class StaticEntryController {

    @Get(uri = "/", produces = MediaType.TEXT_HTML)
    public HttpResponse<String> root() {
        return htmlFromClasspath("/public/index.html");
    }

    @Get(uri = "/admin", produces = MediaType.TEXT_HTML)
    public HttpResponse<String> admin() {
        return htmlFromClasspath("/public/admin/index.html");
    }

    @Get(uri = "/chat", produces = MediaType.TEXT_HTML)
    public HttpResponse<String> chat() {
        return htmlFromClasspath("/public/chat/index.html");
    }

    @Get(uri = "/swagger-ui", produces = MediaType.TEXT_HTML)
    public HttpResponse<String> swaggerUi() {
        return HttpResponse.ok(swaggerUiHtml()).contentType(MediaType.TEXT_HTML_TYPE);
    }

    @Get(uri = "/swagger/{name}", produces = {"application/yaml", "text/yaml", MediaType.TEXT_PLAIN})
    public HttpResponse<String> swaggerFile(String name) {
        return textFromClasspath("/META-INF/swagger/" + name);
    }

    private HttpResponse<String> htmlFromClasspath(String path) {
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is == null) {
                throw new HttpStatusException(HttpStatus.NOT_FOUND, "Resource not found: " + path);
            }
            return HttpResponse.ok(new String(is.readAllBytes(), StandardCharsets.UTF_8)).contentType(MediaType.TEXT_HTML_TYPE);
        } catch (HttpStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to read resource: " + path);
        }
    }

    private HttpResponse<String> textFromClasspath(String path) {
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is == null) {
                throw new HttpStatusException(HttpStatus.NOT_FOUND, "Resource not found: " + path);
            }
            return HttpResponse.ok(new String(is.readAllBytes(), StandardCharsets.UTF_8));
        } catch (HttpStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to read resource: " + path);
        }
    }

    private String swaggerUiHtml() {
        return """
            <!doctype html>
            <html>
            <head>
              <meta charset="UTF-8">
              <title>Swagger UI</title>
              <link rel="stylesheet" href="https://unpkg.com/swagger-ui-dist@5/swagger-ui.css" />
            </head>
            <body>
              <div id="swagger-ui"></div>
              <script src="https://unpkg.com/swagger-ui-dist@5/swagger-ui-bundle.js"></script>
              <script>
                window.ui = SwaggerUIBundle({
                  url: '/swagger/client-journey-platform-1.0.0.yml',
                  dom_id: '#swagger-ui'
                });
              </script>
            </body>
            </html>
            """;
    }
}
