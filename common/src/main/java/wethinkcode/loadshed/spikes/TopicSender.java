package wethinkcode.loadshed.spikes;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

/**
 * I am a small "maker" app for receiving MQ messages from the Stage Service.
 */
public class TopicSender implements Runnable
{
    private static long NAP_TIME = 2000; //ms

    public static final String MQ_URL = "tcp://localhost:61616";

    public static final String MQ_USER = "admin";

    public static final String MQ_PASSWD = "admin";

    public static final String MQ_TOPIC_NAME = "stage";

    public static void main( String[] args ){
        final TopicSender app = new TopicSender();
        app.cmdLineMsgs = args;
        app.run();
    }

    private boolean running = true;

    private String[] cmdLineMsgs;

    private Connection connection;

    private Session session;

    @Override
    public void run(){
        try{
            final ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory( MQ_URL );
            connection = factory.createConnection( MQ_USER, MQ_PASSWD );
            connection.start();
            session = connection.createSession( false, Session.AUTO_ACKNOWLEDGE );
            sendMessages( cmdLineMsgs.length == 0
                ? new String[]{ "{ \"stage\":17 }" }
                : cmdLineMsgs );
        }catch( JMSException erk ){
            throw new RuntimeException( erk );
        }finally{
            closeResources();
            System.out.println( "Bye..." );
        }
    }

    private void sendMessages( String[] messages ) throws JMSException {
        MessageProducer producer = session.createProducer(session.createQueue(MQ_TOPIC_NAME));

        for (String string : messages) {
            TextMessage msg = session.createTextMessage(string);
            System.out.println("Sent msg: "+msg.getText());
            producer.send(msg);
        }
    }

    private void closeResources(){
        try{
            if( session != null ) session.close();
            if( connection != null ) connection.close();
        }catch( JMSException ex ){
            // wut?
        }
        session = null;
        connection = null;
    }

    public void setArgs(String[] args) {
        this.cmdLineMsgs = args;
    }

}
