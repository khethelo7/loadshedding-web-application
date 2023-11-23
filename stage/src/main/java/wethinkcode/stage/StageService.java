package wethinkcode.stage;


import javax.jms.JMSException;

import com.google.common.annotations.VisibleForTesting;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import wethinkcode.loadshed.common.transfer.StageDO;
import wethinkcode.loadshed.spikes.TopicSender;

/**
 * I provide a REST API that reports the current loadshedding "stage". I provide two endpoints:
 * <dl>
 * <dt>GET /stage
 * <dd>report the current stage of loadshedding as a JSON serialisation of a {@code StageDO} data/transfer object
 * <dt>POST /stage
 * <dd>set a new loadshedding stage/level by POSTing a JSON-serialised {@code StageDO} instance as the body of the
 * request.
 * </ul>
 */
public class StageService
{
    public static final int DEFAULT_STAGE = 0; // no loadshedding. Ha!

    public static int DEFAULT_PORT = 7001;

    public static final String MQ_TOPIC_NAME = "stage";

    // private TopicSender topicSender = new TopicSender();

    public static void main( String[] args ){
        final StageService svc = new StageService().initialise();
        svc.start();
    }

    private int loadSheddingStage;

    private Javalin server;

    private int servicePort;

    private TopicSender topicSender = new TopicSender();

    @VisibleForTesting
    public
    StageService initialise(){
        return initialise( DEFAULT_STAGE );
    }

    @VisibleForTesting
    StageService initialise( int initialStage ){
        loadSheddingStage = initialStage;
        assert loadSheddingStage >= 0;
        topicSender.run();

        server = initHttpServer();
        return this;
    }
    
    public void start(){
        start( DEFAULT_PORT );
    }

    @VisibleForTesting
    void start( int networkPort ){
        DEFAULT_PORT = networkPort;
        run();
    }

    public void stop(){
        server.stop();
    }

    public void run(){
        server.start( DEFAULT_PORT );
    }

    private Javalin initHttpServer(){
        return Javalin.create()
            .before(ctx -> {
                ctx.header("Access-Control-Allow-Origin", "*");
                ctx.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
                ctx.header("Access-Control-Allow-Headers", "Content-Type");
            })
            .get( "/stage", this::getCurrentStage )
            .post( "/stage", this::setNewStage );
    }

    private Context getCurrentStage( Context ctx ){
        return ctx.json( new StageDO( loadSheddingStage ) );
    }

    private Context setNewStage( Context ctx ){
        final StageDO stageData = ctx.bodyAsClass( StageDO.class );
        final int newStage = stageData.getStage();
        if( newStage >= 0 && newStage <= 8 ){
            loadSheddingStage = newStage;
            broadcastStageChangeEvent( ctx );
            ctx.status( HttpStatus.OK );
        }else{
            ctx.status( HttpStatus.BAD_REQUEST );
        }
        return ctx.json( new StageDO( loadSheddingStage ) );
    }

    private void broadcastStageChangeEvent( Context ctx ){
        try {
            topicSender.sendMessages(ctx.body());
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

}
