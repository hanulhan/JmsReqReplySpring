/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hanulhan.jms.spring.reqreply.beans;

import hanulhan.jms.spring.reqreply.util.ReqReplyMessageCreator;
import hanulhan.jms.spring.reqreply.util.ReqReplyReturnObject;
import hanulhan.jms.spring.reqreply.util.ReqReplySettings;
import hanulhan.jms.spring.reqreply.util.ReqReplyStatusCode;
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
import org.springframework.jms.JmsException;
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
    private final int waitForAckMilliSec;
    private final int waitForResponseMilliSec;
    static final Logger LOGGER = Logger.getLogger(ReqReplyPollingProducer.class);
    private String filterName;
    private String filterValue;

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

    public ReqReplyReturnObject sendAndAwaitingResponse(String aMessageText, String aFilterName, String aFilterValue) throws JMSException {
        Message myResponseMsg;
        Destination tempDest;
        int myReceivedMsgCount;
        String myCorrelationId = createRandomString();
        String[] myResponseArray = null;
        ReqReplyReturnObject myResponse;
        ReqReplyStatusCode myStatus;
        this.filterName = aFilterName;
        this.filterValue = aFilterValue;

        LOGGER.log(Level.TRACE, "ReqReplyPollingProducer:sendAndAwaitingResponse()");

        ReqReplyMessageCreator myReqMessage = new ReqReplyMessageCreator(aMessageText, myCorrelationId, true);

        myReqMessage.setStringProperty(filterName, filterValue);

        {   // SEND MESSAGE
            jmsTemplate.send(destination, myReqMessage);
            tempDest = myReqMessage.getTempDest();
            LOGGER.log(Level.DEBUG, "Send Message [" + myReqMessage.getMessageText() + "] to " + destination.toString());
        }

        {   // RECEIVING
            LOGGER.log(Level.DEBUG, "Waiting for Respionse on: " + tempDest.toString());

            // Wait for ACK
            myResponse = ReceiveTextMessage(tempDest, waitForAckMilliSec);
            if (!myResponse.getStatusOK()) {
                LOGGER.log(Level.DEBUG, "No ACK received within " + waitForAckMilliSec + " ms");
                return myResponse;
            }

            // Wait for first data
            myResponseMsg = ReceiveTextMessage(tempDest, waitForResponseMilliSec);
            if (myResponseMsg == null) {
                myResponse;.
                setStatus(ReqReplyReturnObject.Status.STATUS_NO_PAYLOAD);
                LOGGER.log(Level.DEBUG, "No payload received within " + waitForAckMilliSec + " ms");
                return myResponse;;
            }
            myStatus = validate(myResponseMsg);

            // First data received. Check quantity of total messages
            myReceivedMsgCount = 1;
            if (!myResponseMsg.propertyExists(ReqReplySettings.PROPERTY_NAME_TOTAL_COUNT)) {
                myResponse;.
                setStatus(ReqReplyReturnObject.Status.STATUS_RESPONSE_HEADER_ERROR);
                LOGGER.log(Level.DEBUG, "Property " + ReqReplySettings.PROPERTY_NAME_TOTAL_COUNT + "missing");
            }

            int myTotalMsgCount = myResponseMsg.getIntProperty(ReqReplySettings.PROPERTY_NAME_TOTAL_COUNT);
            LOGGER.log(Level.TRACE, "Expecting " + myTotalMsgCount + " Messages");
            if (myTotalMsgCount > 1) {
                // More responses are expected
                myResponseArray = new String[myTotalMsgCount];
                myResponseArray[0] = ((TextMessage) myResponseMsg).getText();

                do {
                    // wait for respnse 3 and more
                    myResponseMsg = ReceiveTextMessage(tempDest, waitForResponseMilliSec);
                    if (myResponseMsg != null) {
                        myResponseArray[myReceivedMsgCount] = ((TextMessage) myResponseMsg).getText();
                        myReceivedMsgCount++;
                    }
                } while (myResponseMsg != null && myReceivedMsgCount < myTotalMsgCount);

                if (myResponseMsg == null) {
                    myResponse;.
                    setStatus(ReqReplyReturnObject.Status.STATUS_PAYLOAD_INCOMPLETE);
                    LOGGER.log(Level.DEBUG, "Payload incomplete, timeout");
                } else {
                    LOGGER.log(Level.DEBUG, "Finished Receiving. Count: " + myReceivedMsgCount + ", totalCount: " + myTotalMsgCount);
                    myResponse;.
                    setStatus(ReqReplyReturnObject.Status.STATUS_OK);
                }
            }
        }

        if (myResponseArray != null && myResponseArray.length > 0) {
            for (String temp : myResponseArray) {
                if (temp != null) {
                    myResponse;.
                    concat(temp);
                }
            }
        }

        return myResponse;;
    }

    private ReqReplyReturnObject ReceiveTextMessage(Destination aDestination, int aTimeout) {

        int myMilliSeconds;
        Date myStartTime = new Date();
        Message myResponseMsg = null;
        ReqReplyReturnObject myReturnObj = new ReqReplyReturnObject();

        try {
            jmsTemplate.setReceiveTimeout(aTimeout);
            myResponseMsg = jmsTemplate.receive(aDestination);

            if (myResponseMsg == null) {
                myReturnObj.setStatus(ReqReplyStatusCode.STATUS_ERROR);
                return myReturnObj;
            }

            if (!(myResponseMsg instanceof TextMessage)) {
                myReturnObj.setStatus(ReqReplyStatusCode.STATUS_RESPONSE_HEADER_ERROR);
                LOGGER.log(Level.ERROR, "Response is not a TextMessage");
            }

            if (!myResponseMsg.propertyExists(ReqReplySettings.PROPERTY_NAME_MSG_TYPE)) {
                myReturnObj.setStatus(ReqReplyStatusCode.STATUS_RESPONSE_HEADER_ERROR);
                return myReturnObj;
            }

            if (myResponseMsg.getStringProperty(ReqReplySettings.PROPERTY_NAME_MSG_TYPE).equals(ReqReplySettings.PROPERTY_VALUE_MSG_TYPE_NACK)) {
                myReturnObj.setStatus(ReqReplyStatusCode.STATUS_SERVER_SEND_NACK);
                LOGGER.log(Level.ERROR, "Server send NACK");
                return myReturnObj;

            } else if (myResponseMsg.getStringProperty(ReqReplySettings.PROPERTY_NAME_MSG_TYPE).equals(ReqReplySettings.PROPERTY_VALUE_MSG_TYPE_PAYLOAD)) {
                if (!myResponseMsg.propertyExists(ReqReplySettings.PROPERTY_NAME_COUNT)) {
                    myReturnObj.setStatus(ReqReplyStatusCode.STATUS_RESPONSE_HEADER_ERROR);
                    LOGGER.log(Level.ERROR, "Property COUNT missing");
                    return myReturnObj;
                }

                if (!myResponseMsg.propertyExists(ReqReplySettings.PROPERTY_NAME_TOTAL_COUNT)) {
                    myReturnObj.setStatus(ReqReplyStatusCode.STATUS_RESPONSE_HEADER_ERROR);
                    LOGGER.log(Level.ERROR, "Property TOTAL_COUNT missing");
                    return myReturnObj;
                }

            } else {
                myReturnObj.setStatus(ReqReplyStatusCode.STATUS_RESPONSE_HEADER_ERROR);
                return myReturnObj;
            }

            myMilliSeconds = (int) ((new Date().getTime() - myStartTime.getTime()));
            LOGGER.log(Level.INFO, "Client receive TextMessage in " + myMilliSeconds + "ms from " + aDestination.toString());

        } catch (JmsException jmsException) {
        } catch (JMSException jMSException) {
        }

        return myReturnObj;
    }

    private ReqReplyReturnObject validate(TextMessage aMessage) {

        if (aMessage == null) {
            return new ReqReplyReturnObj(ReqReplyReturnObject.Status.STATUS_NO_PAYLOAD);
        }

        if (!aMessage.propertyExists(filterName)) {
            return ReqReplyReturnObject.Status.STATUS_RESPONSE_HEADER_ERROR;
        }

        if (!aMessage.getStringProperty(filterName).equals(filterValue)) {
            return ReqReplyReturnObject.Status.STATUS_FILTER_MISMATCH;

        }

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
