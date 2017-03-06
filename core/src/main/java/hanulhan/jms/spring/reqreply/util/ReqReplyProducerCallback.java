/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hanulhan.jms.spring.reqreply.util;

import java.lang.reflect.Array;
import java.util.Date;
import java.util.Random;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.ProducerCallback;

/**
 *
 * @author uhansen
 */
public class ReqReplyProducerCallback implements ProducerCallback {

    // Constructor stuff
    private Destination destination;
    private final String messageText;
    private String systemIdent;

    // internal Stuff
    private static final Logger LOGGER = Logger.getLogger(ReqReplyProducerCallback.class);
    private final String correlationId;
    private final Destination replyTo;
    private TextMessage txtMessage;

    // Settings
    private int waitForAckMilliSec;
    private int waitForResponseMilliSec;

    public ReqReplyProducerCallback(Destination aDestination, String aMessageText, String aSystemIdent) {
        this.systemIdent = aSystemIdent;
        this.messageText = aMessageText;
        this.destination = aDestination;
        this.correlationId = createRandomString();
        this.replyTo = null;

        waitForAckMilliSec = 1000;
        waitForResponseMilliSec = 1000;
    }

    @Override
    public Object doInJms(Session session, MessageProducer producer) throws JMSException {
        Date myStartTime;
        Destination tempDest = session.createTemporaryQueue();
        MessageConsumer responseConsumer = session.createConsumer(tempDest);
        Message myAckResponse, myMsgResponse;
        String myAckResponseText;
        String myResponse;
        String[] myMsgRespnseText;
        int myMilliSeconds;
        int myTotalMsgCount, myCurrentMsgCount;

        txtMessage = session.createTextMessage(messageText);
        txtMessage.setJMSCorrelationID(correlationId);
        txtMessage.setStringProperty("systemIdent", systemIdent);
        txtMessage.setJMSReplyTo(replyTo);

        producer.send(this.destination, txtMessage);
        LOGGER.log(Level.TRACE, "Send Message (" + this.correlationId + "): " + messageText);

        // Wait for response
        {
            myStartTime = new Date();

            // Wait for first reply / Acknowledge
            myAckResponse = responseConsumer.receive(waitForAckMilliSec);

            myMilliSeconds = (int) ((new Date().getTime() - myStartTime.getTime()));
            if (myAckResponse != null) {

                // Receive first response, ACK
                if (myAckResponse instanceof TextMessage) {
                    myAckResponseText = ((TextMessage) myAckResponse).getText();
                    LOGGER.log(Level.DEBUG, "Client receive [" + myAckResponseText + "] in " + myMilliSeconds + "ms, awaiting more");
                }

                myStartTime = new Date();
                myMsgResponse = null;

                myMilliSeconds = (int) ((new Date().getTime() - myStartTime.getTime()));

                // Wait for second response
                myMsgResponse = responseConsumer.receive(waitForResponseMilliSec);

                if (myMsgResponse != null) {
                    if (myMsgResponse instanceof TextMessage) {

                        // Second response received
                        myResponse = ((TextMessage) myMsgResponse).getText();
                        LOGGER.log(Level.DEBUG, "Client receive [" + myResponse + "] in " + myMilliSeconds + " ms");
                        int myReceivedMsgCount = 1;

//                                        Enumeration<String> myProperties = myMessage2.getPropertyNames();
//                                        while (myProperties.hasMoreElements())  {
//                                            String propertyName = myProperties.nextElement();
//                                            LOGGER.log(Level.TRACE, "Property " + propertyName + ": " + myMessage2.getObjectProperty(propertyName));
//                                        }
                        // How may respnses are expected                                        
                        if (myMsgResponse.propertyExists("totalCount") && myMsgResponse.propertyExists("currentCount")) {
                            myTotalMsgCount = myMsgResponse.getIntProperty("totalCount");
                            myCurrentMsgCount = myMsgResponse.getIntProperty("currentCount");
                            LOGGER.log(Level.TRACE, "Expecting " + myTotalMsgCount + " Messages");
                            if (myTotalMsgCount > 1) {

                                myMsgRespnseText = new String[myTotalMsgCount];
                                myMsgRespnseText[0] = myResponse;

                                // More responses are expected
                                do {
                                    myStartTime = new Date();
                                    myMsgResponse = null;
                                    myMilliSeconds = (int) ((new Date().getTime() - myStartTime.getTime()));

                                    // wati for respnse 3 and more
                                    myMsgResponse = responseConsumer.receive(5000);

                                    if (myMsgResponse != null) {
                                        if (myMsgResponse instanceof TextMessage) {
                                            if (myMsgResponse.propertyExists("totalCount") && myMsgResponse.propertyExists("currentCount")) {
                                                if (myTotalMsgCount == myMsgResponse.getIntProperty("totalCount")) {
                                                    myCurrentMsgCount = myMsgResponse.getIntProperty("currentCount");
                                                    myResponse = ((TextMessage) myMsgResponse).getText();
                                                    myMsgRespnseText[myCurrentMsgCount] = myResponse;

                                                    LOGGER.log(Level.DEBUG, "Client receive [" + myResponse + "] in " + myMilliSeconds + " ms");
                                                    myReceivedMsgCount++;
                                                } else {
                                                    LOGGER.log(Level.ERROR, "Total Msg-count error");
                                                }
                                            } else {
                                                LOGGER.log(Level.DEBUG, "Property missing in response");
                                            }

                                        } else {
                                            LOGGER.log(Level.DEBUG, "Received Message not a TextMessage");
                                        }
                                    } else {
                                        LOGGER.log(Level.DEBUG, "Messages Timeout. Count: " + myReceivedMsgCount + ", totalCount: " + myTotalMsgCount);
                                    }
                                } while (myMsgResponse != null && myReceivedMsgCount < 3);

                                if (myReceivedMsgCount == myTotalMsgCount) {
                                    LOGGER.log(Level.DEBUG, "Finished. All Msgs received");
                                    String myMessageString = "";
                                    for (int i = 0; i < myTotalMsgCount; i++) {
                                        myMessageString += myMsgRespnseText[i];
                                    }

                                    return (myMessageString);
                                } else {
                                    LOGGER.log(Level.ERROR, "Not all Messages received" + myReceivedMsgCount + ", totalCount: " + myTotalMsgCount);
                                }

                            } else {
                                LOGGER.log(Level.DEBUG, "Total count: " + myTotalMsgCount);
                            }
                        } else {
                            LOGGER.log(Level.DEBUG, "Property missing in response");
                        }
                    } else {
                        LOGGER.log(Level.DEBUG, "Received Message not a TextMessage");
                    }
                } else {
                    LOGGER.log(Level.DEBUG, "No Response received within " + waitForAckMilliSec + " ms");
                }

            } else {
                LOGGER.log(Level.DEBUG, "No ACK received within " + waitForResponseMilliSec + " ms");
            }
        }

        return (null);
    }

    private String createRandomString() {
        Random random;
        random = new Random(System.currentTimeMillis());
        long randomLong = random.nextLong();
        return Long.toHexString(randomLong);
    }

    public void setWaitForAckMilliSec(int waitForAckMilliSec) {
        this.waitForAckMilliSec = waitForAckMilliSec;
    }

    public void setWaitForResponseMilliSec(int waitForResponseMilliSec) {
        this.waitForResponseMilliSec = waitForResponseMilliSec;
    }
}
