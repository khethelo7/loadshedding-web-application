package khethelo.places.server;

import java.util.ArrayList;
import java.util.Collection;

import io.javalin.Javalin;
import io.javalin.http.Context;
import khethelo.places.model.Places;
import khethelo.places.model.Town;

public class Endpoints {

    public static void configureEndpoints(Javalin server, Places places) {
        server.get( "/provinces", ctx -> {
            ctx.json( places.provinces());
            ctx.contentType("application/json");
        });
        server.get( "/towns/{province}", ctx -> ctx.json(getTowns(ctx, places)));
    }

    private static Collection<Town> getTowns( Context ctx, Places places ){
        final String province = ctx.pathParam( "province" ).toUpperCase();
        return places.provinces().contains(province) ? places.townsIn( province ) : new ArrayList<>();
    }
    
}
