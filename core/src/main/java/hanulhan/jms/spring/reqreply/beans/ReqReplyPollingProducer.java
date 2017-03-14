/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hanulhan.jms.spring.reqreply.beans;

import hanulhan.jms.spring.reqreply.util.ReqMessageCreator;
import hanulhan.jms.spring.reqreply.util.ReqReplyProducerCallback;
import hanulhan.jms.spring.reqreply.util.ReqReplySettings;
import java.util.Date;
import java.util.Random;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jms.core.JmsTemplate;

/**
 *
 * @author uhansen
 */
public class ReqReplyPollingProducer implements ApplicationContextAware {

    // injected stuff
    private ApplicationContext applicationContext;
    private JmsTemplate jmsTemplate;
    private Destination destination;

    // internal
    private int waitForAckMilliSec;
    private int waitForResponseMilliSec;

    static final Logger LOGGER = Logger.getLogger(ReqReplyPollingProducer.class);

    public ReqReplyPollingProducer() {
        LOGGER.log(Level.TRACE, "ReqReplyPollingProducer:ReqReplyPollingProducer()");
        waitForAckMilliSec = 2000;
        waitForResponseMilliSec = 2000;
    }
    
    public ReqReplyPollingProducer(int aWaitForAck, int aWaitForResponse) {
        LOGGER.log(Level.TRACE, "ReqReplyPollingProducer:ReqReplyPollingProducer()");
        waitForAckMilliSec = aWaitForAck;
        waitForResponseMilliSec = aWaitForResponse;
    }

    public String sendAndAwaitingResponse(String aMessageText, String aSystemIdent) throws JMSException {
        String myResponseText;
        Message myResponseMsg;
        Destination tempDest;
        int myReceivedMsgCount;
        String myCorrelationId = createRandomString();
        String[] myResponseArray = null;
        String myResponse="";

        LOGGER.log(Level.TRACE, "ReqReplyPollingProducer:sendAndAwaitingResponse()");
        
        if (jmsTemplate == null)    {
            LOGGER.log(Level.ERROR, "jmsTemplate is null");
        }
        if (destination == null)    {
            LOGGER.log(Level.ERROR, "destination is null");
        }
        if (applicationContext == null)    {
            LOGGER.log(Level.ERROR, "applicationContext is null");
        }
        
        ReqMessageCreator myReqMessage = new ReqMessageCreator(aMessageText, myCorrelationId, aSystemIdent);
        

        
        {   // SEND MESSAGE
            jmsTemplate.send(destination, myReqMessage);
            tempDest = myReqMessage.getTempDest();
            LOGGER.log(Level.DEBUG, "Send Message [" + myReqMessage.getMessageText() + "] to " + destination.toString());
        }

        {   // RECEIVING
            LOGGER.log(Level.DEBUG, "Waiting for Respionse on: " + tempDest.toString());
            
            // Wait for ACK
            myResponseMsg = ReceiveTextMessage(tempDest, waitForAckMilliSec);
            if (myResponseMsg != null) {

                // Wait for first data
                myResponseMsg = ReceiveTextMessage(tempDest, waitForResponseMilliSec);
                if (myResponseMsg != null) {
                    
                    // First data received. Check quantity of total messages
                    myReceivedMsgCount= 1;
                    if (myResponseMsg.propertyExists(ReqReplySettings.PROPERTY_NAME_TOTAL_COUNT)) {

                        int myTotalMsgCount = myResponseMsg.getIntProperty(ReqReplySettings.PROPERTY_NAME_TOTAL_COUNT);
                        LOGGER.log(Level.TRACE, "Expecting " + myTotalMsgCount + " Messages");
                        if (myTotalMsgCount > 1) {
                            // More responses are expected
                            myResponseArray = new String[myTotalMsgCount];
                            myResponseArray[0] = ((TextMessage)myResponseMsg).getText();
                            
                            do {
                                // wait for respnse 3 and more
                                myResponseMsg = ReceiveTextMessage(tempDest, waitForResponseMilliSec);
                                if (myResponseMsg != null)  {
                                    myResponseArray[myReceivedMsgCount]= ((TextMessage)myResponseMsg).getText();
                                    myReceivedMsgCount++;
                                }
                            } while (myResponseMsg != null && myReceivedMsgCount < myTotalMsgCount);

                            LOGGER.log(Level.DEBUG, "Stop Receiving. Count: " + myReceivedMsgCount + ", totalCount: " + myTotalMsgCount);

                        } else {
                            LOGGER.log(Level.DEBUG, "Total count: " + myTotalMsgCount);
                        }
                    } else {
                        LOGGER.log(Level.DEBUG, "Property " + ReqReplySettings.PROPERTY_NAME_TOTAL_COUNT + "missing");
                    }
                }
            } else {
                LOGGER.log(Level.DEBUG, "No ACK received within " + waitForAckMilliSec + " ms");
            }
        }
        
        if (myResponseArray != null)    {
            for (String temp : myResponseArray) {
                if (temp != null)   {
                    myResponse+= temp;
                }
            }
            return myResponse;
        } else {
            return null;
        }
        
        
    }

    private Message ReceiveTextMessage(Destination aDestination, int aTimeout) {

        int myMilliSeconds;
        Date myStartTime = new Date();
        Message myResponseMsg= null;

        jmsTemplate.setReceiveTimeout(aTimeout);
        myResponseMsg = jmsTemplate.receive(aDestination);
        if (myResponseMsg != null) {
            if (myResponseMsg instanceof TextMessage) {
                try {
                    myMilliSeconds = (int) ((new Date().getTime() - myStartTime.getTime()));
                    LOGGER.log(Level.DEBUG, "Client receive [" + ((TextMessage) myResponseMsg).getText() + "] in " + myMilliSeconds + "ms from " + aDestination.toString());
                } catch (JMSException ex) {
                    LOGGER.log(Level.ERROR, ex);
                }
            } else {
                LOGGER.log(Level.ERROR, "Response is not a TextMessage");
            }
        } else {
            LOGGER.log(Level.ERROR, "No ACK received within " + waitForAckMilliSec + " ms");
        }

        return myResponseMsg;
    }

    private String createRandomString() {
        Random random;
        random = new Random(System.currentTimeMillis());
        long randomLong = random.nextLong();
        return Long.toHexString(randomLong);
    }

    public JmsTemplate getJmsTemplate() {
        return jmsTemplate;
    }

    public void setJmsTemplate(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public Destination getDestination() {
        return destination;
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
    }



    @Override
    public void setApplicationContext(ApplicationContext ac) throws BeansException {
        this.applicationContext = ac;
    }

}
