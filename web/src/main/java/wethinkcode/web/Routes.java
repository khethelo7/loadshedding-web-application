package wethinkcode.web;

import io.javalin.Javalin;
import io.javalin.http.Handler;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.post;

import java.util.HashMap;
import java.util.Map;

public class Routes {
    public static final String LANDING_PAGE = "/";

    public static void configure(Javalin app) {
        app.routes(() -> {
            get(LANDING_PAGE, landing);
        });
    }

    public static Handler landing = ctx -> {
        Map<String, Object> model = new HashMap<>();
        model.put("message", "Hello World!!! - From Thymeleaf");
        ctx.render("index.html", model);
    };

}
