/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hanulhan.jms.spring.reqreply.beans;

import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.TextMessage;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.jms.core.JmsTemplate;
import hanulhan.jms.spring.reqreply.util.ReqReplyFilterInterface;
import hanulhan.jms.spring.reqreply.util.ReqReplyFilterMap;
import hanulhan.jms.spring.reqreply.util.ReqReplyMessageCreator;
import hanulhan.jms.spring.reqreply.util.ReqReplySettings;
import hanulhan.jms.spring.reqreply.util.RequestObject;
import javax.annotation.PostConstruct;
import javax.jms.JMSException;
import javax.jms.MessageListener;

/**
 * This class has a MessageListener to the jms REQUEST-Topic.
 *
 * The request will be validated
 *
 * @author uhansen
 */
public class ReqReplyConsumer implements MessageListener {

    private ApplicationContext applicationContext;

    // injected stuff
    private JmsTemplate jmsTemplate;
    private Destination reqDestination;
    private Destination replyDestination;
    private String filterPropertyName;

    private String consumerId;
    private Integer maxMessageLength;

    // internal
    private static final Logger LOGGER = Logger.getLogger(ReqReplyConsumer.class);
    private ReqReplyFilterMap filterMap = new ReqReplyFilterMap();

    /**
     *
     */
    @PostConstruct
    public void postConstruct() {
        LOGGER.log(Level.TRACE, "ReqReplyConsuer:postConstuct");
        LOGGER.log(Level.TRACE, "Req-Destination: " + reqDestination.toString());
        LOGGER.log(Level.TRACE, "jmsTemplate: " + jmsTemplate.toString());
        LOGGER.log(Level.TRACE, "filterPropertyName: " + filterPropertyName);
        LOGGER.log(Level.TRACE, "maxMessageLength: " + maxMessageLength);
    }

    /**
     *
     */
    public ReqReplyConsumer() {
        LOGGER.log(Level.TRACE, "ReqReplyConsumer::ReqReplyConsumer()");
    }

    /**
     *
     * @param aReqObj
     * @return
     */
    public boolean ConnectSystem(RequestObject aReqObj) {
        String myFilterValue = aReqObj.getIdent();

        if (filterMap.IsFilterInMap(myFilterValue)) {
            return false;
        }
        LOGGER.log(Level.DEBUG, "ReqReplyConsumer::ConnectSystem()");
        filterMap.put(myFilterValue, aReqObj);
        return true;
    }

    private boolean IsSystemConnected(String aFilterValue) {
        return filterMap.IsFilterInMap(aFilterValue);
    }

    public void DisconnectSystem(String aFilterValue) {
        if (filterMap.IsFilterInMap(aFilterValue)) {
            LOGGER.log(Level.DEBUG, "ReqReplyConsumer::DisconnectSystem()");
            filterMap.delete(aFilterValue);
            
        }
    }

    public void sendResponse(String aIdent, String aResponse, String aMessageId) {
        int myMsgCount;
        ReqReplyMessageCreator myResponseCreator;

        LOGGER.log(Level.TRACE, "SendResponse()");
        myMsgCount = (int) Math.ceil((double) aResponse.length() / maxMessageLength);

        LOGGER.log(Level.TRACE, "Split Response into " + myMsgCount + " pieces");

        int myStartIndex;
        int myEndIndex;
        String myMessagePart;

        for (int i = 0; i < myMsgCount; i++) {
            myStartIndex = i * maxMessageLength;
            myEndIndex = ((i + 1) * maxMessageLength) - 1;
            if (myEndIndex >= aResponse.length()) {
                myEndIndex = aResponse.length() - 1;
            }
            myMessagePart = aResponse.substring(myStartIndex, myEndIndex + 1);

            myResponseCreator = new ReqReplyMessageCreator(myMessagePart, aMessageId);
            myResponseCreator.setIntProperty(ReqReplySettings.PROPERTY_NAME_TOTAL_COUNT, myMsgCount);
            myResponseCreator.setIntProperty(ReqReplySettings.PROPERTY_NAME_COUNT, i + 1);
            myResponseCreator.setStringProperty(ReqReplySettings.PROPERTY_NAME_MSG_TYPE, ReqReplySettings.PROPERTY_VALUE_MSG_TYPE_PAYLOAD);
            myResponseCreator.setStringProperty(filterPropertyName, aIdent);

            LOGGER.log(Level.DEBUG, "Server send response "
                    + (i + 1) + "/" + myMsgCount
                    + ", Ident: " + aResponse
                    + ", msgId: " + aMessageId);

            jmsTemplate.send(replyDestination, myResponseCreator);
        }
    }

    /**
     * Spring default MessageHandler callback function Receive the Request from
     * the client. Check the Header and the properties of the message and handle
     * the reply if the message is valid
     *
     * @param aMessage
     *
     */
    @Override
    public void onMessage(Message aMessage) {

        String myIdent;
        String myRequest;
        String correlationId;
        Destination myResponseDestination;
        ReqReplyMessageCreator myResponseCreator;
        LOGGER.log(Level.TRACE, "ReqReplyConsumer::onReceive()");
        try {
            if (!(aMessage instanceof TextMessage)) {
                LOGGER.log(Level.ERROR, "Message received, but not a TextMessage");
                return;
            }

            if (filterPropertyName == null || filterPropertyName.trim().isEmpty()) {
                LOGGER.log(Level.ERROR, "No Filter property set");
                return;
            }
            if (!aMessage.propertyExists(filterPropertyName)) {
                LOGGER.log(Level.ERROR, "Message received, but no filter property set");
                return;
            }

            LOGGER.log(Level.TRACE, "Filter property in Message: " + aMessage.getStringProperty(filterPropertyName));
            myIdent = aMessage.getStringProperty(filterPropertyName);


            if (IsSystemConnected(myIdent)) {

                // Block the system and send ACK
                myRequest = ((TextMessage) aMessage).getText();
                correlationId = aMessage.getJMSMessageID();

                if (filterMap.addRequest(myIdent, consumerId, myRequest, correlationId, 2000)) {

                    // Send an ACK
                    myResponseDestination = aMessage.getJMSReplyTo();

                    myResponseCreator = new ReqReplyMessageCreator("ACK", correlationId);
                    myResponseCreator.setStringProperty(ReqReplySettings.PROPERTY_NAME_MSG_TYPE, ReqReplySettings.PROPERTY_VALUE_MSG_TYPE_ACK);
                    myResponseCreator.setStringProperty(filterPropertyName, myIdent);
                    jmsTemplate.send(myResponseDestination, myResponseCreator);
                    LOGGER.log(Level.DEBUG, "Consumer send ACK"
                            + ", Ident: " + myIdent
                            + ", msgId: " + correlationId);
                }
            }

        } catch (JMSException jMSException) {
            LOGGER.log(Level.ERROR, jMSException);
        }
    }

    /*
    private void handleMessage(Message aMessage) {

        LOGGER.log(Level.TRACE, "ReqReplyConsumer::handleMessage()");
        String myResponseText;
        String myIdent = null;
        try {
            if (aMessage.getJMSReplyTo() != null) {

                String correlationId = aMessage.getJMSMessageID();
                Destination myResponseDestination = aMessage.getJMSReplyTo();

                ReqReplyMessageCreator myResponseCreator = new ReqReplyMessageCreator("ACK", correlationId);
                myResponseCreator.setStringProperty(ReqReplySettings.PROPERTY_NAME_MSG_TYPE, ReqReplySettings.PROPERTY_VALUE_MSG_TYPE_ACK);
                myResponseCreator.setStringProperty(filterPropertyName, myIdent);
                jmsTemplate.send(myResponseDestination, myResponseCreator);
                LOGGER.log(Level.DEBUG, "Server send ACK"
                        + ", Ident: " + myIdent
                        + ", msgId: " + correlationId);

                myResponseText = filterPropertyDelegator.getPropertyFilterResult(myIdent);
                int myMsgCount;
                myMsgCount = (int) Math.ceil((double) myResponseText.length() / maxMessageLength);

                LOGGER.log(Level.TRACE, "Split Response into " + myMsgCount + " pieces");

                int myStartIndex;
                int myEndIndex;
                String myMessagePart;

                for (int i = 0; i < myMsgCount; i++) {
                    myStartIndex = i * maxMessageLength;
                    myEndIndex = ((i + 1) * maxMessageLength) - 1;
                    if (myEndIndex >= myResponseText.length()) {
                        myEndIndex = myResponseText.length() - 1;
                    }
                    myMessagePart = myResponseText.substring(myStartIndex, myEndIndex + 1);

                    myResponseCreator = new ReqReplyMessageCreator(myMessagePart, correlationId);
                    myResponseCreator.setIntProperty(ReqReplySettings.PROPERTY_NAME_TOTAL_COUNT, myMsgCount);
                    myResponseCreator.setIntProperty(ReqReplySettings.PROPERTY_NAME_COUNT, i + 1);
                    myResponseCreator.setStringProperty(ReqReplySettings.PROPERTY_NAME_MSG_TYPE, ReqReplySettings.PROPERTY_VALUE_MSG_TYPE_PAYLOAD);
                    myResponseCreator.setStringProperty(filterPropertyName, myIdent);

                    LOGGER.log(Level.DEBUG, "Server send response "
                            + (i + 1) + "/" + myMsgCount
                            + ", Ident: " + myIdent
                            + ", msgId: " + correlationId);

                    jmsTemplate.send(myResponseDestination, myResponseCreator);
                }
            }
        } catch (JMSException jMSException) {
            LOGGER.log(Level.ERROR, jMSException);
        }
    }
    */

    /**
     *
     * @return
     */
    public JmsTemplate getJmsTemplate() {
        return jmsTemplate;
    }

    /**
     *
     * @param jmsTemplate
     */
    public void setJmsTemplate(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    /**
     *
     * @return
     */
    public String getFilterPropertyName() {
        return filterPropertyName;
    }

    /**
     *
     * @param filterPropertyName
     */
    public void setFilterPropertyName(String filterPropertyName) {
        this.filterPropertyName = filterPropertyName;
    }

    /**
     *
     * @return
     */
    public String getServerId() {
        return consumerId;
    }

    /**
     *
     * @param serverId
     */
    public void setServerId(String serverId) {
        this.consumerId = serverId;
    }

    /**
     *
     * @return
     */
    public Integer getMaxMessageLength() {
        return maxMessageLength;
    }

    /**
     *
     * @param maxMessageLength
     */
    public void setMaxMessageLength(Integer maxMessageLength) {
        this.maxMessageLength = maxMessageLength;
    }

    /**
     *
     * @return
     */
    public Destination getReqDestination() {
        return reqDestination;
    }

    /**
     *
     * @param reqDestination
     */
    public void setReqDestination(Destination reqDestination) {
        this.reqDestination = reqDestination;
    }

    public Destination getReplyDestination() {
        return replyDestination;
    }

    public void setReplyDestination(Destination replyDestination) {
        this.replyDestination = replyDestination;
    }

    
}
