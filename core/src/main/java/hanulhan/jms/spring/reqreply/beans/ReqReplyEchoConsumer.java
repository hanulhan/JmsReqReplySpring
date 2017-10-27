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
import hanulhan.jms.spring.reqreply.util.ReqReplyFilterMap;
import hanulhan.jms.spring.reqreply.util.ReqReplyMessageCreator;
import hanulhan.jms.spring.reqreply.util.ReqReplySettings;
import hanulhan.jms.spring.reqreply.util.RequestObject;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.jms.JMSException;
import javax.jms.MessageListener;
import org.springframework.context.ApplicationContextAware;

/**
 * This class has a MessageListener to the jms REQUEST-Topic.
 *
 * The request will be validated
 *
 * @author uhansen
 */
public class ReqReplyEchoConsumer implements MessageListener, ApplicationContextAware {

    private ApplicationContext applicationContext;

    // injected stuff
    private JmsTemplate jmsTemplate;
    private Destination reqDestination;
    private Destination replyDestination;
    private String filterPropertyName;
    private String clientId;
    private Integer maxMessageLength;
    private long awaitingConnectionTimeoutMsec;

    // internal
    private static final Logger LOGGER = Logger.getLogger(ReqReplyEchoConsumer.class);

    /**
     *
     */
    @PostConstruct
    public void postConstructIt() {
        LOGGER.log(Level.TRACE, "ReqReplyConsuer:postConstuct");
        LOGGER.log(Level.TRACE, "Req-Destination: " + reqDestination.toString());
        LOGGER.log(Level.TRACE, "jmsTemplate: " + jmsTemplate.toString());
        LOGGER.log(Level.TRACE, "filterPropertyName: " + filterPropertyName);
        LOGGER.log(Level.TRACE, "maxMessageLength: " + maxMessageLength);
        LOGGER.log(Level.TRACE, "awaitingConnectionTimeoutMsec: " + awaitingConnectionTimeoutMsec);
    }

    public ReqReplyEchoConsumer() {
        super();
    }

    /**
     *
     */
    public ReqReplyEchoConsumer(long aAwaitingConnectionTimeoutMsec) {
        super();
        awaitingConnectionTimeoutMsec = aAwaitingConnectionTimeoutMsec;
        LOGGER.log(Level.TRACE, "ReqReplyConsumer::ReqReplyConsumer()");
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
                    + ", Ident: " + aIdent
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
        String myResponse;
        String correlationId;
        String myCommand;
        int myPort;
        long milliSeconds = 0;
        long startTime = 0;
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

            myIdent = aMessage.getStringProperty(filterPropertyName);
            myCommand = aMessage.getStringProperty(ReqReplySettings.PROPERTY_VALUE_COMMAND);
            myPort = aMessage.getIntProperty(ReqReplySettings.PROPERTY_VALUE_PORT);
            myRequest = ((TextMessage) aMessage).getText();

            LOGGER.log(Level.TRACE, "onMessage(" + myRequest + ")");

            startTime = new Date().getTime();

            // Block the system and send ACK
            correlationId = aMessage.getJMSMessageID();

            // Send an ACK
            myResponseDestination = aMessage.getJMSReplyTo();

            myResponseCreator = new ReqReplyMessageCreator("ACK", correlationId);
            myResponseCreator.setStringProperty(ReqReplySettings.PROPERTY_NAME_MSG_TYPE, ReqReplySettings.PROPERTY_VALUE_MSG_TYPE_ACK);
            myResponseCreator.setStringProperty(filterPropertyName, myIdent);
            jmsTemplate.send(myResponseDestination, myResponseCreator);
            LOGGER.log(Level.DEBUG, "Consumer send ACK"
                    + ", Ident: " + myIdent
                    + ", msgId: " + correlationId
                    + ", request: " + myRequest);

            
            myResponse= "Response to: " + myRequest;
            sendResponse(myIdent, myResponse, correlationId);

        } catch (JMSException jMSException) {
            LOGGER.log(Level.ERROR, jMSException);
        }
    }

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

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
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

    public Integer getMaxMessageLength() {
        return maxMessageLength;
    }

    public void setMaxMessageLength(Integer maxMessageLength) {
        this.maxMessageLength = maxMessageLength;
    }

    public long getAwaitingConnectionTimeoutMsec() {
        return awaitingConnectionTimeoutMsec;
    }

    public void setAwaitingConnectionTimeoutMsec(long awaitingConnectionTimeoutMsec) {
        this.awaitingConnectionTimeoutMsec = awaitingConnectionTimeoutMsec;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

}
