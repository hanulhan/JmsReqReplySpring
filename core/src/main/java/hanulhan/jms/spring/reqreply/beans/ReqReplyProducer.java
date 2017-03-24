/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hanulhan.jms.spring.reqreply.beans;

import hanulhan.jms.spring.reqreply.util.ReqReplyMessageCreator;
import hanulhan.jms.spring.reqreply.util.ReqReplyMessageObject;
import hanulhan.jms.spring.reqreply.util.ReqReplySettings;
import hanulhan.jms.spring.reqreply.util.ReqReplyStatusCode;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
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
public class ReqReplyProducer implements MessageListener {

    // injected stuff
    private ApplicationContext applicationContext;
    private JmsTemplate jmsTemplate;
    private Destination requestDestination;
    private Destination replyDestination;

    // internal
    private Map<String, String> activeRequestMap = new HashMap<>();
    
    private final long waitForResponseMilliSec;
    static final Logger LOGGER = Logger.getLogger(ReqReplyProducer.class);
    private String filterName;
    private String filterValue;
    private String messageId;

    public ReqReplyProducer() {
        LOGGER.log(Level.TRACE, "ReqReplyPollingProducer:ReqReplyPollingProducer()");
        waitForResponseMilliSec = 2000;
    }

    public ReqReplyProducer(int aWaitForResponse) {
        LOGGER.log(Level.TRACE, "ReqReplyPollingProducer:ReqReplyPollingProducer()");
    }

    
    public void sendRequest(String aMessageText, String aFilterName, String aFilterValue)    {
        
    }
    
    
    public ReqReplyMessageObject sendAndAwaitingResponse(String aMessageText, String aFilterName, String aFilterValue) throws JMSException {
        TextMessage myResponseMsg;

        int myReceivedMsgCount, myTotalMsgCount;
        String[] myResponseArray = null;
        ReqReplyMessageObject myReturnObj = new ReqReplyMessageObject();
        this.filterName = aFilterName;
        this.filterValue = aFilterValue;

        LOGGER.log(Level.TRACE, "ReqReplyPollingProducer:sendAndAwaitingResponse()");

        {   // SEND MESSAGE
            ReqReplyMessageCreator myReqMessage = new ReqReplyMessageCreator(aMessageText, replyDestination);
            myReqMessage.setStringProperty(filterName, filterValue);

            jmsTemplate.send(requestDestination, myReqMessage);

            messageId = myReqMessage.getMessageId();
            LOGGER.log(Level.INFO, "Sending MessageId: " + messageId);
            LOGGER.log(Level.DEBUG, "Send Message [id:" + messageId + "] to " + requestDestination.toString());
        }

        {   // RECEIVING
            LOGGER.log(Level.DEBUG, "WAITING FOR ACK on: " + replyDestination.toString());

            // Wait for ACK
            if (!AwaitingAck()) {
                LOGGER.log(Level.DEBUG, "No ACK received within " + waitForAckMilliSec + " ms");
                myReturnObj.setStatus(ReqReplyStatusCode.STATUS_RESPONSE_TIMEOUT);
                return myReturnObj;
            } else {
                LOGGER.log(Level.DEBUG, "ACK received ");
            }

            LOGGER.log(Level.DEBUG, "WAITING FOR RESPONSE on: " + replyDestination.toString());
            myReturnObj = AwaitingResponse();
        }
        return myReturnObj;
    }

    private ReqReplyMessageObject AwaitingResponse() {
        long myReceiveTimeout;
        long myMilliSeconds = 0;
        Date myStartTime = new Date();
        TextMessage myMessage;
        int myReceivedMsgCount = 0, myTotalMsgCount = 0, myMsgCount;

        String[] myResponseArray = null;
        ReqReplyMessageObject myReturnObj = new ReqReplyMessageObject();

        do {
            try {
                myReceiveTimeout = waitForResponseMilliSec - myMilliSeconds;
                LOGGER.log(Level.DEBUG, "Start over receiving. timeout: " + myReceiveTimeout + " ms");
                myMessage = ReceiveTextMessage(myReceiveTimeout);

                myReturnObj.setStatus(validate(myMessage));
                if (myReturnObj.getStatusOK()) {
                    myReceivedMsgCount++;

                    myMsgCount = myMessage.getIntProperty(ReqReplySettings.PROPERTY_NAME_COUNT);
                    if (myResponseArray == null) {
                        // Its the first Message
                        myTotalMsgCount = myMessage.getIntProperty(ReqReplySettings.PROPERTY_NAME_TOTAL_COUNT);
                        myResponseArray = new String[myTotalMsgCount];
                    }
                    LOGGER.log(Level.DEBUG, "Received Message (" + myMsgCount + "/" + myTotalMsgCount + ")");
                    myResponseArray[myMsgCount - 1] = myMessage.getText();
                    if (myMsgCount == 8) {
                        LOGGER.log(Level.DEBUG, "Last Packet");
                    }
                } else {
                    LOGGER.log(Level.DEBUG, "validate not ok");
                }
            } catch (JMSException jMSException) {
                LOGGER.log(Level.ERROR, jMSException);
            }

            myMilliSeconds = (int) ((new Date().getTime() - myStartTime.getTime()));
            if (waitForResponseMilliSec - myMilliSeconds < 2) {
                myMilliSeconds = waitForResponseMilliSec;
            }

            LOGGER.log(Level.DEBUG, "Receive runs since " + myMilliSeconds);
        } while ((myMilliSeconds < waitForResponseMilliSec) && (myReturnObj.getStatusError() || (myReceivedMsgCount < myTotalMsgCount)));

        if (myReturnObj.getStatusOK() && myReceivedMsgCount == myTotalMsgCount) {
            if (myResponseArray != null && myResponseArray.length > 0) {
                for (String temp : myResponseArray) {
                    if (temp != null) {
                        myReturnObj.concatPayload(temp);
                    }
                }
            }
        }
        return myReturnObj;
    }

    private TextMessage ReceiveTextMessage(long aTimeout) {

        long myMilliSeconds;
        Date myStartTime = new Date();
        Message myResponseMsg = null;
        TextMessage myTextMsg;
        String myMsgPayload;

        jmsTemplate.setReceiveTimeout((int) aTimeout);
        myResponseMsg = jmsTemplate.receive(replyDestination);
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
        } else {
            myTextMsg = (TextMessage) myResponseMsg;
            try {
                myMsgPayload = myTextMsg.getText();
                myMilliSeconds = (int) ((new Date().getTime() - myStartTime.getTime()));
                LOGGER.log(Level.DEBUG, "Client receive TextMessage: [" + ((TextMessage) myResponseMsg).getText() + "] in " + myMilliSeconds + "ms from " + replyDestination.toString());
            } catch (JMSException jMSException) {
                LOGGER.log(Level.ERROR, jMSException);
            }
        }

//            LOGGER.log(Level.DEBUG, "Client receive TextMessage in " + myMilliSeconds + "ms from " + aDestination.toString());
        return (TextMessage) myResponseMsg;
    }

    private Boolean AwaitingAck() {
        long myMilliSeconds;
        Date myStartTime = new Date();
        TextMessage myResponseMsg;
//        ReqReplyStatusCode myStatus;
        long myReceiveTimeout;
        ReqReplyMessageObject myReturnObj = new ReqReplyMessageObject();

        myMilliSeconds = 0;
        do {
            try {
                myReceiveTimeout = waitForAckMilliSec - myMilliSeconds;
                if (myReceiveTimeout < 0) {
                    myReceiveTimeout = 10;
                }
                LOGGER.log(Level.DEBUG, "Set ReceiveTimeout to " + myReceiveTimeout + " ms");

                myResponseMsg = ReceiveTextMessage((int) myReceiveTimeout);

                myReturnObj.setStatus(validate(myResponseMsg));

                myMilliSeconds = (new Date().getTime() - myStartTime.getTime());
            } catch (JMSException ex) {
                LOGGER.log(Level.ERROR, ex);
            }

        } while (myMilliSeconds < waitForAckMilliSec && myReturnObj.getStatusError());

        return myReturnObj.getStatusOK();
    }

    private ReqReplyStatusCode validate(TextMessage aMessage) throws JMSException {

        if (aMessage == null) {
            LOGGER.log(Level.ERROR, "Timeout");
            return ReqReplyStatusCode.STATUS_TIMEOUT;
        }

        // Check if Property: "MsgType"  exists
        if (!aMessage.propertyExists(ReqReplySettings.PROPERTY_NAME_MSG_TYPE)) {
            LOGGER.log(Level.ERROR, "PropertyName: MsgType missing in response");
            return ReqReplyStatusCode.STATUS_HEADER_ERROR;
        }

        // Check if Property: <FilterName>  exists
        if (!aMessage.propertyExists(this.filterName)) {
            LOGGER.log(Level.ERROR, "FilterProperty: " + this.filterName + " missing in response");
            return ReqReplyStatusCode.STATUS_HEADER_ERROR;
        }

        // Check if Filter-Property match
        if (!aMessage.getStringProperty(this.filterName).equals(this.filterValue)) {
            LOGGER.log(Level.ERROR, "FilterProperty mismatch in response");
            return ReqReplyStatusCode.STATUS_FILTER_MISMATCH;
        }

        // Check for Correlation match
        if (!aMessage.getJMSCorrelationID().equals(this.messageId)) {
            LOGGER.log(Level.ERROR, "MessageId mismatch in response");
            return ReqReplyStatusCode.STATUS_CORRELATION_MISMATCH;
        }

//        String msgTypeReceived = aMessage.getStringProperty(ReqReplySettings.PROPERTY_NAME_MSG_TYPE);
//        String msgType = ReqReplySettings.PROPERTY_VALUE_MSG_TYPE_ACK;
        // More checks for MsgType: payload
        if (aMessage.getStringProperty(ReqReplySettings.PROPERTY_NAME_MSG_TYPE).equals(ReqReplySettings.PROPERTY_VALUE_MSG_TYPE_PAYLOAD)) {

            if (!aMessage.propertyExists(ReqReplySettings.PROPERTY_NAME_COUNT)) {
                LOGGER.log(Level.ERROR, "Property COUNT missing");
                return ReqReplyStatusCode.STATUS_HEADER_ERROR;
            }

            if (!aMessage.propertyExists(ReqReplySettings.PROPERTY_NAME_TOTAL_COUNT)) {
                LOGGER.log(Level.ERROR, "Property TOTAL_COUNT missing");
                return ReqReplyStatusCode.STATUS_HEADER_ERROR;
            }
        } else if (!aMessage.getStringProperty(ReqReplySettings.PROPERTY_NAME_MSG_TYPE).equals(ReqReplySettings.PROPERTY_VALUE_MSG_TYPE_ACK)) {
            LOGGER.log(Level.ERROR, "PropertyName: MsgType not set properly");
            return ReqReplyStatusCode.STATUS_HEADER_ERROR;
        }

        return ReqReplyStatusCode.STATUS_OK;
    }


    public JmsTemplate getJmsTemplate() {
        return jmsTemplate;
    }

    public void setJmsTemplate(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public Destination getRequestDestination() {
        return requestDestination;
    }

    public void setRequestDestination(Destination requestDestination) {
        this.requestDestination = requestDestination;
    }

    public Destination getReplyDestination() {
        return replyDestination;
    }

    public void setReplyDestination(Destination replyDestination) {
        this.replyDestination = replyDestination;
    }

    public void setApplicationContext(ApplicationContext ac) throws BeansException {
        this.applicationContext = ac;
    }

    @Override
    public void onMessage(Message message) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
