package wethinkcode.web;

import io.javalin.Javalin;
import io.javalin.http.Handler;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONException;
import wethinkcode.loadshed.common.transfer.StageDO;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.post;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Routes {
    public static final String LANDING_PAGE = "/";
    public static final String STAGE_PAGE = "/stage";
    public static final String PROVINCES_PAGE = "/provinces";
    public static final String TOWNS_PAGE = "/towns";

    public static final String FETCH_TOWNS = "/provinces.action";

    private static String places_url = "http://localhost:7000/";
    private static String stage_url = "http://localhost:7001/";
    private static String schedule_url = "http://localhost:7002/";

    public static void configure(Javalin app) {
        app.routes(() -> {
            get(LANDING_PAGE, landing);
            get(STAGE_PAGE, stage);
            get(PROVINCES_PAGE, provinces);
            get(TOWNS_PAGE, towns);

            post(FETCH_TOWNS, view_towns);
        });
    }

    // HANDLERS
    public static Handler landing = ctx -> {
        Map<String, Object> model = Map.of("message", "Hello World!!! - From Thymeleaf");
        ctx.render("index.html", model);
    };

    public static Handler stage = ctx -> {
        ArrayList<StageDO> stages = new ArrayList<>();
        
        for (int i = 0; i < 9; i++) {stages.add(new StageDO(i));}
        
        HttpResponse<JsonNode> response = Unirest.get(stage_url+"stage").asJson();
        StageDO currentStage = new StageDO(getStageFromResponse(response));
        
        Map<String, Object> model = Map.of("stages", stages, "current_stage", currentStage);
        ctx.render("stage.html", model);
    };

    public static Handler provinces = ctx -> {
        HttpResponse<JsonNode> response = Unirest.get(places_url+"provinces").asJson();
        ArrayList<String> provincesList = getProvincesFromResponse(response);

        Map<String, Object> model = Map.of("provinces", provincesList);
        ctx.render("provinces.html", model);
    };

    public static Handler towns = ctx -> {

        HttpResponse<JsonNode> response = Unirest.get(places_url+"provinces").asJson();

        ctx.render("towns.html");
    };

    public static Handler view_towns = ctx -> {
        String selectedProvince = ctx.formParamAsClass("province", String.class)
                .check(Objects::nonNull, "Province was not specified")
                .get();
        
        HttpResponse<JsonNode> response = Unirest.get(places_url+"towns/"+selectedProvince).asJson();
        System.out.println(selectedProvince);
        ctx.render("towns.html");
    };


    // HELPER FUNCTIONS
    private static int getStageFromResponse( HttpResponse<JsonNode> response ) throws JSONException{
        return response.getBody().getObject().getInt( "stage" );
    }
    private static ArrayList<String> getProvincesFromResponse( HttpResponse<JsonNode> response ) throws JSONException{
        JSONArray jsonArray = new JSONArray(response.getBody().toString());
        ArrayList<String> provinces = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            provinces.add(jsonArray.getString(i));
        }

        return provinces;
    }

}
