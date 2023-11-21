package wethinkcode.web;

import io.javalin.Javalin;
import io.javalin.http.Handler;
import wethinkcode.loadshed.common.transfer.StageDO;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.post;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Routes {
    public static final String LANDING_PAGE = "/";
    public static final String STAGE_PAGE = "/stage";

    public static void configure(Javalin app) {
        app.routes(() -> {
            get(LANDING_PAGE, landing);
            get(STAGE_PAGE, stage);
        });
    }

    public static Handler landing = ctx -> {
        Map<String, Object> model = new HashMap<>();
        model.put("message", "Hello World!!! - From Thymeleaf");
        ctx.render("index.html", model);
    };

    public static Handler stage = ctx -> {
        Map<String, Object> model = new HashMap<>();
        ArrayList<StageDO> stages = new ArrayList<>();

        for (int i = 0; i < 9; i++) {
            StageDO stage = new StageDO(i);
            stages.add(stage);
        }

        model.put("stages", stages);
        ctx.render("stage.html", model);
    };

}
