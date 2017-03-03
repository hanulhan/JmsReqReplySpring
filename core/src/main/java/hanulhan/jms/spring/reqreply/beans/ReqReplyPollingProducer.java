/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hanulhan.jms.spring.reqreply.beans;

import hanulhan.jms.spring.reqreply.util.ReqMessageCreator;
import static java.lang.Thread.sleep;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;
import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.jms.core.MessagePostProcessor;

/**
 *
 * @author uhansen
 */
public class ReqReplyPollingProducer implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    // Injected stuff
    private JmsTemplate jmsTemplate;
    private Destination destination;

    // internal
    private static final Logger LOGGER = Logger.getLogger(ReqReplyPollingProducer.class);

    // Broker and JMS Settings
    private boolean transacted = false;

    // ReqReply Settings
    private final int WAIT_FOR_ACK_MILLI_SECONDS = 1000;
    private final int WAIT_FOR_RESPONSE_MILLI_SECONDS = 1000;

    public ReqReplyPollingProducer() {
        super();
    }

    public String SendReqAwaitingReply(String systemRequest, String ident) {

        TextMessage txtMessage;
        String correlationId;
        int myMilliSeconds;
        TextMessage receiveMessage;
        Date myStartTime;
        String messageText = null;

        Connection connection;
        int msgCount = 0;

        try {
            LOGGER.log(Level.DEBUG, "Start Client(" + clientId + "),  Broker: " + connectionFactory.getBrokerURL());
            connection = connectionFactory.createConnection();
            connection.start();
            session = connection.createSession(transacted, Settings.REP_ACK_MODE);
            Destination adminQueue = session.createTopic(Settings.MESSAGE_TOPIC_NAME);

            //Setup a message producer to send message to the queue the server is consuming from
            this.producer = session.createProducer(adminQueue);
            this.producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

            //Create a temporary queue that this client will listen for responses on then create a consumer
            //that consumes message from this temporary queue...for a real application a client should reuse
            //the same temp queue for each message to the server...one temp queue per client
            Destination tempDest = session.createTemporaryQueue();
            MessageConsumer responseConsumer = session.createConsumer(tempDest);

            //This class will handle the messages to the temp queue as well
            //responseConsumer.setMessageListener(this);
            // ##########################################
            Boolean terminate = false;
            Scanner keyboard = new Scanner(System.in);

            sleep(1000);
            LOGGER.log(Level.INFO, "Press any key + <Enter> to continue and x + <Enter> to exit");
            String input = keyboard.nextLine();

            msgCount++;
            //Now create the actual message you want to send

            ReqMessageCreator myMessagCreator;
            myMessagCreator = new ReqMessageCreator("My Message Text", tempDest);
            this.jmsTemplate.send(destination, myMessagCreator);
            LOGGER.log(Level.TRACE, "Send Message (" + myMessagCreator.getCorrelationId() + "): " + myMessagCreator.getMessageText());
            
            {
                // Wait for response
                myStartTime = new Date();
                Message myMessage1 = null;

                // Wait for first reply / Acknowledge
                myMessage1 = responseConsumer.receive(WAIT_FOR_ACK_MILLI_SECONDS);

                myMilliSeconds = (int) ((new Date().getTime() - myStartTime.getTime()));
                if (myMessage1 != null) {

                    // Receive first response, ACK
                    if (myMessage1 instanceof TextMessage) {
                        receiveMessage = (TextMessage) myMessage1;
                        messageText = receiveMessage.getText();
                        LOGGER.log(Level.DEBUG, "Client receive [" + messageText + "] in " + myMilliSeconds + "ms, awaiting more");
                    }

                    myStartTime = new Date();
                    Message myMessage2 = null;

                    myMilliSeconds = (int) ((new Date().getTime() - myStartTime.getTime()));

                    // Wait for second response
                    myMessage2 = responseConsumer.receive(WAIT_FOR_RESPONSE_MILLI_SECONDS);

                    if (myMessage2 != null) {
                        if (myMessage2 instanceof TextMessage) {

                            // Second response received
                            receiveMessage = (TextMessage) myMessage2;
                            messageText = receiveMessage.getText();
                            LOGGER.log(Level.DEBUG, "Client receive [" + messageText + "] in " + myMilliSeconds + " ms");
                            int myMsgCount = 1;

//                                        Enumeration<String> myProperties = myMessage2.getPropertyNames();
//                                        while (myProperties.hasMoreElements())  {
//                                            String propertyName = myProperties.nextElement();
//                                            LOGGER.log(Level.TRACE, "Property " + propertyName + ": " + myMessage2.getObjectProperty(propertyName));
//                                        }
                            // How may respnses are expected                                        
                            if (myMessage2.propertyExists("totalCount")) {
                                int myTotalMsgCount = myMessage2.getIntProperty("totalCount");
                                LOGGER.log(Level.TRACE, "Expecting " + myTotalMsgCount + " Messages");
                                if (myTotalMsgCount > 1) {
                                    // More responses are expected
                                    do {
                                        myStartTime = new Date();
                                        myMessage2 = null;
                                        myMilliSeconds = (int) ((new Date().getTime() - myStartTime.getTime()));
                                        // wati for respnse 3 and more
                                        myMessage2 = responseConsumer.receive(5000);

                                        if (myMessage2 != null) {
                                            if (myMessage2 instanceof TextMessage) {
                                                receiveMessage = (TextMessage) myMessage2;
                                                messageText = receiveMessage.getText();

                                                LOGGER.log(Level.DEBUG, "Client receive [" + messageText + "] in " + myMilliSeconds + " ms");
                                                myMsgCount++;
                                            } else {
                                                LOGGER.log(Level.DEBUG, "Received Message not a TextMessage");
                                            }
                                        } else {
                                            LOGGER.log(Level.DEBUG, "Messages Timeout. Count: " + myMsgCount + ", totalCount: " + myTotalMsgCount);
                                        }
                                    } while (myMessage2 != null && myMsgCount < 3);

                                    LOGGER.log(Level.DEBUG, "Cancel Receiving. Count: " + myMsgCount + ", totalCount: " + myTotalMsgCount);

                                } else {
                                    LOGGER.log(Level.DEBUG, "Total count: " + myTotalMsgCount);
                                }
                            } else {
                                LOGGER.log(Level.DEBUG, "Property tocalCount missing");
                            }
                        } else {
                            LOGGER.log(Level.DEBUG, "Received Messae not a TextMessage");
                        }
                    } else {
                        LOGGER.log(Level.DEBUG, "No Response received within " + WAIT_FOR_RESPONSE_MILLI_SECONDS + " ms");
                    }

                } else {
                    LOGGER.log(Level.DEBUG, "No ACK received within " + WAIT_FOR_ACK_MILLI_SECONDS + " ms");
                }

            }

            session.close();
            connection.close();

        } catch (JMSException e) {
            LOGGER.log(Level.ERROR, e);
        } catch (InterruptedException ex) {
            LOGGER.log(Level.ERROR, ex);
        }
    }



    @Override
    public void setApplicationContext(ApplicationContext ac) throws BeansException {
        this.applicationContext = ac;
    }

}
