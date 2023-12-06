package khethelo.stage;


import java.util.concurrent.SynchronousQueue;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;

import kong.unirest.HttpResponse;
import kong.unirest.HttpStatus;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONException;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.jupiter.api.*;

import khethelo.loadshed.common.transfer.StageDO;
import khethelo.stage.StageService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * I test StageService message sending.
 */
// @Disabled
@Tag( "expensive" )
public class StageServiceMQTest
{
    public static final int TEST_PORT = 7777;

    private static StageService server;

    private static ActiveMQConnectionFactory factory;

    private static Connection mqConnection;

    @BeforeAll
    public static void startInfrastructure() throws JMSException {
        startMsgQueue();
        startStageSvc();
    }

    @AfterAll
    public static void cleanup() throws JMSException {
        server.stop();
        mqConnection.close();
    }

    public void connectMqListener( MessageListener listener ) throws JMSException {
        mqConnection = factory.createConnection();
        final Session session = mqConnection.createSession( false, Session.AUTO_ACKNOWLEDGE );
        final Destination dest = session.createTopic( StageService.MQ_TOPIC_NAME );

        final MessageConsumer receiver = session.createConsumer( dest );
        receiver.setMessageListener( listener );

        mqConnection.start();
    }

    @AfterEach
    public void closeMqConnection() throws JMSException {
        mqConnection.close();
        mqConnection = null;
    }

    @Test
    public void sendMqEventWhenStageChanges() throws JSONException, InterruptedException{
        final SynchronousQueue<StageDO> resultCatcher = new SynchronousQueue<>();

        final MessageListener mqListener = new MessageListener(){ // Happens in the background and waits
            @Override
            public void onMessage( Message message ){
                StageDO resultStage = (StageDO) ((TextMessage) message).getClass().cast(StageDO.class);
                
                resultCatcher.add(resultStage);
                assertEquals(new StageDO(1), resultStage);
            }
        };
        try {
            connectMqListener(mqListener);
        } catch (JMSException e) {
            fail("Could not connect MQListener");
        }


        final HttpResponse<StageDO> startStage = Unirest.get( serverUrl() + "/stage" ).asObject( StageDO.class );
        assertEquals( HttpStatus.OK, startStage.getStatus() );

        final StageDO data = startStage.getBody();
        final int newStage = data.getStage() + 1;

        final HttpResponse<JsonNode> changeStage = Unirest.post( serverUrl() + "/stage" )
            .header( "Content-Type", "application/json" )
            .body( new StageDO( newStage ))
            .asJson();
        assertEquals( HttpStatus.OK, changeStage.getStatus() );

        assertNotEquals(new StageDO(getStageFromResponse(changeStage)), resultCatcher.take());


    }

    private static int getStageFromResponse( HttpResponse<JsonNode> response ) throws JSONException{
        return response.getBody().getObject().getInt( "stage" );
    }

    private static void startMsgQueue() throws JMSException {
        factory = new ActiveMQConnectionFactory("vm://localhost?broker.persistent=false");
    }

    private static void startStageSvc(){
        server = new StageService().initialise();
        server.start( TEST_PORT );
    }

    private String serverUrl(){
        return "http://localhost:" + TEST_PORT;
    }
}
