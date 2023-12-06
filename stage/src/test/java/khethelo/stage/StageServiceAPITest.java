package khethelo.stage;

import java.io.IOException;

import kong.unirest.HttpResponse;
import kong.unirest.HttpStatus;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONException;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * I contain functional tests of the Stage Service.
 */
@Tag( "expensive" )
public class StageServiceAPITest
{
    public static final int TEST_PORT = 7777;

    private static StageService server;

    @BeforeAll
    public static void startServer() throws IOException{
        server = new StageService().initialise();
        server.start( TEST_PORT );
    }

    @AfterAll
    public static void stopServer(){
        server.stop();
    }

    @Test
    public void getsCorrespondingState() {
        final int trueStage = Integer.parseInt(
                Unirest.get("https://loadshedding.eskom.co.za/LoadShedding/GetStatus")
                    .asString()
                    .getBody()
        );

        HttpResponse<JsonNode> response = Unirest.get( serverUrl() + "/stage" ).asJson();
        assertEquals( HttpStatus.OK, response.getStatus() );
        assertEquals( "application/json", response.getHeaders().getFirst( "Content-Type" ) );

        final int stage = getStageFromResponse( response );
        assertEquals( trueStage, stage );
    }

    private static int getStageFromResponse( HttpResponse<JsonNode> response ) throws JSONException{
        return response.getBody().getObject().getInt( "stage" );
    }

    private String serverUrl(){
        return "http://localhost:" + TEST_PORT;
    }
}
