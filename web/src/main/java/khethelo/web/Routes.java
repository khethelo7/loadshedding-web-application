package khethelo.web;

import io.javalin.Javalin;
import io.javalin.http.Handler;
import khethelo.loadshed.common.transfer.ScheduleDO;
import khethelo.loadshed.common.transfer.StageDO;
import khethelo.places.model.Town;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONException;
import kong.unirest.json.JSONObject;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.post;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Routes {
    public static final String LANDING_PAGE = "/";
    public static final String STAGE_PAGE = "/stage";
    public static final String PROVINCES_PAGE = "/provinces";
    public static final String TOWNS_PAGE = "/towns";
    public static final String SCHEDULE_PAGE = "/town_schedule";

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
            get(SCHEDULE_PAGE, schedule);

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
        ArrayList<String> provincesList = getListFromResponse(response);

        Map<String, Object> model = Map.of("provinces", provincesList);
        ctx.render("provinces.html", model);
    };

    public static Handler towns = ctx -> {
        ctx.render("towns.html");
    };

    public static Handler view_towns = ctx -> {
        String selectedProvince = ctx.formParamAsClass("province", String.class)
                .check(Objects::nonNull, "Province was not specified")
                .get();
        
        HttpResponse<JsonNode> response = Unirest.get(places_url+"towns/"+selectedProvince).asJson();
        ArrayList<Town> towns = getJsonListFromResponse(response);
        Collections.sort(towns, Comparator.comparing(Town::getName));

        // need towns as 'towns', province as 'province'
        Map<String, Object> model = Map.of("towns", towns, "province", selectedProvince);

        System.out.println(selectedProvince);
        ctx.render("towns_in_province.html", model);
    };

    public static Handler schedule = ctx -> {
        String searchedTown = ctx.queryParam("selectedTown");
        String searchedProvince = ctx.queryParam("selectedProvince");
        int currentStage = getStageFromResponse(Unirest.get(stage_url+"stage").asJson());

        HttpResponse<JsonNode> response = Unirest
                .get(schedule_url+searchedProvince+"/"+searchedTown+"/"+currentStage)
                .asJson();

        // JSONObject scheduleObject = new JSONObject(response.getBody());

        Map<String, Object> model = Map.of("town", searchedTown, "schedule", response.getBody());
        ctx.render("schedule.html", model);
    };


    // HELPER FUNCTIONS
    private static int getStageFromResponse( HttpResponse<JsonNode> response ) throws JSONException{
        return response.getBody().getObject().getInt( "stage" );
    }
    // private static ScheduleDO getScheduleFromResponse( HttpResponse<JsonNode> response ) throws JSONException{
        
    // }
    private static ArrayList<String> getListFromResponse(HttpResponse<JsonNode> response) throws JSONException {
        JSONArray jsonArray = new JSONArray(response.getBody().toString());
        ArrayList<String> resultList = new ArrayList<>();
    
        for (int i = 0; i < jsonArray.length(); i++) {
            Object element = jsonArray.get(i);
            resultList.add((String) element);
        }
    
        return resultList;
    }
    private static ArrayList<Town> getJsonListFromResponse(HttpResponse<JsonNode> response) throws JSONException {
        JSONArray jsonArray = new JSONArray(response.getBody().toString());
        ArrayList<Town> resultList = new ArrayList<>();
    
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject element = jsonArray.getJSONObject(i);

            resultList.add(new Town(element.get("name").toString(), element.get("province").toString()));
        }
    
        return resultList;
    }

}
