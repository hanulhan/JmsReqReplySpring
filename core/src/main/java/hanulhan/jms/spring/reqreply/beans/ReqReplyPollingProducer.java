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
    private String messageId;

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
        TextMessage myResponseMsg;
        Destination tempDest;
        int myReceivedMsgCount, myTotalMsgCount;
//        correlationId = createRandomString();
        String[] myResponseArray = null;
        ReqReplyReturnObject myReturnObj = new ReqReplyReturnObject();
        this.filterName = aFilterName;
        this.filterValue = aFilterValue;

        LOGGER.log(Level.TRACE, "ReqReplyPollingProducer:sendAndAwaitingResponse()");

        {   // SEND MESSAGE
            ReqReplyMessageCreator myReqMessage = new ReqReplyMessageCreator(aMessageText, true);
            myReqMessage.setStringProperty(filterName, filterValue);
            
            jmsTemplate.send(destination, myReqMessage);
            
            messageId = myReqMessage.getMessageId();
            LOGGER.log(Level.DEBUG, "Sending MessageId: " + messageId);
            tempDest = myReqMessage.getTempDest();
            LOGGER.log(Level.DEBUG, "Send Message [id:" + messageId + "] to " + destination.toString());
        }

        {   // RECEIVING
            LOGGER.log(Level.DEBUG, "Waiting for Respionse on: " + tempDest.toString());

            // Wait for ACK
            myResponseMsg = ReceiveTextMessage(tempDest, waitForAckMilliSec);
            myReturnObj.setStatus(validate(myResponseMsg));
            if (!myReturnObj.getStatusOK()) {
                LOGGER.log(Level.DEBUG, "No ACK received within " + waitForAckMilliSec + " ms");
                return myReturnObj;
            }

            // Wait for first data
            myResponseMsg = ReceiveTextMessage(tempDest, waitForResponseMilliSec);
            myReturnObj.setStatus(validate(myResponseMsg));
            if (!myReturnObj.getStatusOK()) {
                LOGGER.log(Level.DEBUG, "No payload received within " + waitForAckMilliSec + " ms");
                return myReturnObj;
            }

            // First data received. Check quantity of total messages
            myReceivedMsgCount = 1;

            myTotalMsgCount = myResponseMsg.getIntProperty(ReqReplySettings.PROPERTY_NAME_TOTAL_COUNT);
            myResponseArray = new String[myTotalMsgCount];
            myResponseArray[0] = myResponseMsg.getText();

            LOGGER.log(Level.TRACE, "Expecting " + myTotalMsgCount + " Messages");
            if (myTotalMsgCount > 1) {
                // More responses are expected

                do {
                    // wait for respnse 3 and more
                    myResponseMsg = ReceiveTextMessage(tempDest, waitForResponseMilliSec);
                    myReturnObj.setStatus(validate(myResponseMsg));
                    if (myReturnObj.getStatusOK()) {
                        myResponseArray[myReceivedMsgCount] = myResponseMsg.getText();
                        myReceivedMsgCount++;
                    }
                } while (myReturnObj.getStatusOK() && myReceivedMsgCount < myTotalMsgCount);

                if (!myReturnObj.getStatusOK()) {
                    LOGGER.log(Level.DEBUG, "Payload incomplete, timeout");
                    return myReturnObj;
                }
            }
        }

        LOGGER.log(Level.DEBUG, "Finished Receiving");

        if (myResponseArray != null && myResponseArray.length > 0) {
            for (String temp : myResponseArray) {
                if (temp != null) {
                    myReturnObj.concat(temp);
                }
            }
        }

        return myReturnObj;
    }

    private TextMessage ReceiveTextMessage(Destination aDestination, int aTimeout) {

        int myMilliSeconds;
        Date myStartTime = new Date();
        Message myResponseMsg = null;

        try {
            jmsTemplate.setReceiveTimeout(aTimeout);
            myResponseMsg = jmsTemplate.receive(aDestination);
//            String resSelectorId = "JMSCorrelationID='"+messageId+"'";
//            LOGGER.log(Level.DEBUG, "Selector: " + resSelectorId);
//            myResponseMsg = jmsTemplate.receiveSelected(destination, resSelectorId);
            if (myResponseMsg == null) {
                return null;
            }

            // Check if Msg is a TextMessage
            if (!(myResponseMsg instanceof TextMessage)) {
                LOGGER.log(Level.ERROR, "Response is not a TextMessage");
                return null;
            }

            myMilliSeconds = (int) ((new Date().getTime() - myStartTime.getTime()));
            LOGGER.log(Level.INFO, "Client receive TextMessage in " + myMilliSeconds + "ms from " + aDestination.toString());

        } catch (JmsException jmsException) {
            LOGGER.log(Level.ERROR, jmsException);
            return null;
        }

        return (TextMessage) myResponseMsg;
    }

    private ReqReplyStatusCode validate(TextMessage aMessage) throws JMSException {

        if (aMessage == null) {
            LOGGER.log(Level.ERROR, "Timeout");
            return ReqReplyStatusCode.STATUS_RESPONSE_TIMEOUT;
        }

        // Check if Property: "MsgType"  exists
        if (!aMessage.propertyExists(ReqReplySettings.PROPERTY_NAME_MSG_TYPE)) {
            LOGGER.log(Level.ERROR, "PropertyName: MsgType missing in response");
            return ReqReplyStatusCode.STATUS_RESPONSE_HEADER_ERROR;
        }

        // Check if Property: <FilterName>  exists
        if (!aMessage.propertyExists(this.filterName)) {
            LOGGER.log(Level.ERROR, "FilterProperty: " + this.filterName + " missing in response");
            return ReqReplyStatusCode.STATUS_RESPONSE_HEADER_ERROR;
        }

        // Check if Filter-Property match
        if (!aMessage.getStringProperty(this.filterName).equals(this.filterValue)) {
            LOGGER.log(Level.ERROR, "FilterProperty mismatch in response");
            return ReqReplyStatusCode.STATUS_RESPONSE_FILTER_MISMATCH;
        }

        // Check for Correlation match
        if (!aMessage.getJMSCorrelationID().equals(this.messageId)) {
            LOGGER.log(Level.ERROR, "MessageId mismatch in response");
            return ReqReplyStatusCode.STATUS_CORRELATION_MISMATCH;
        }

        // More checks for MsgType: payload
        if (aMessage.getStringProperty(ReqReplySettings.PROPERTY_NAME_MSG_TYPE).equals(ReqReplySettings.PROPERTY_VALUE_MSG_TYPE_PAYLOAD)) {

            if (!aMessage.propertyExists(ReqReplySettings.PROPERTY_NAME_COUNT)) {
                LOGGER.log(Level.ERROR, "Property COUNT missing");
                return ReqReplyStatusCode.STATUS_RESPONSE_HEADER_ERROR;
            }

            if (!aMessage.propertyExists(ReqReplySettings.PROPERTY_NAME_TOTAL_COUNT)) {
                LOGGER.log(Level.ERROR, "Property TOTAL_COUNT missing");
                return ReqReplyStatusCode.STATUS_RESPONSE_HEADER_ERROR;
            }

        }

        return ReqReplyStatusCode.STATUS_OK;
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
