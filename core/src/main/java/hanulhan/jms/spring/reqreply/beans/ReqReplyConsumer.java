/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hanulhan.jms.spring.reqreply.beans;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.jms.core.JmsTemplate;
import hanulhan.jms.spring.reqreply.util.ReqReplyFilterInterface;
import hanulhan.jms.spring.reqreply.util.ReqReplyMessageCreator;
import hanulhan.jms.spring.reqreply.util.ReqReplyMessageObject;
import hanulhan.jms.spring.reqreply.util.ReqReplySettings;
import javax.annotation.PostConstruct;
import javax.jms.MessageListener;

/**
 *
 * @author uhansen
 */
public class ReqReplyConsumer implements MessageListener {

    private ApplicationContext applicationContext;

    // injected stuff
    private JmsTemplate jmsTemplate;
    private Destination reqDestination;
    private String filterPropertyName;

    private ReqReplyFilterInterface filterPropertyDelegator;
    private String serverId;
    private Integer maxMessageLength;

    // internal
    private static final Logger LOGGER = Logger.getLogger(ReqReplyConsumer.class);
    private String filterPropertyValue;
//    private ReqReplyMessageObject myResponse= new ReqReplyMessageObject();

    @PostConstruct
    public void postConstruct() {
        LOGGER.log(Level.TRACE, "ReqReplyConsuer:postConstuct");
        LOGGER.log(Level.TRACE, "Req-Destination: " + reqDestination.toString());
        LOGGER.log(Level.TRACE, "jmsTemplate: " + jmsTemplate.toString());
        LOGGER.log(Level.TRACE, "filterPropertyName: " + filterPropertyName);
        LOGGER.log(Level.TRACE, "maxMessageLength: " + maxMessageLength);
    }

    public ReqReplyConsumer() {
        LOGGER.log(Level.TRACE, "ReqReplyConsumer::ReqReplyConsumer()");
    }

    @Override
    public void onMessage(Message aMessage) {

        LOGGER.log(Level.TRACE, "ReqReplyConsumer::onReceive()");
        try {
            if (aMessage instanceof TextMessage) {
                if (filterPropertyName != null && !filterPropertyName.trim().isEmpty()) {
                    if (aMessage.propertyExists(filterPropertyName)) {
                        LOGGER.log(Level.TRACE, "Filter property in Message: " + aMessage.getStringProperty(filterPropertyName));
                        filterPropertyValue = aMessage.getStringProperty(filterPropertyName);
                        // Check if the filter-property should be handled
                        if (filterPropertyDelegator.getPropertyFilterActive(filterPropertyValue)) {
                            handleMessage(aMessage);
                        } else {
                            LOGGER.log(Level.ERROR, "Message received, but fiter property does not fit");
                        }
                    } else {
                        LOGGER.log(Level.ERROR, "Message received, but no filter property set");
                    }
                } else {
                    LOGGER.log(Level.ERROR, "No Filter property set");
                }
            } else {
                LOGGER.log(Level.ERROR, "Message received, but not a TextMessage");
            }
        } catch (JMSException jMSException) {
            LOGGER.log(Level.ERROR, jMSException);
        }
    }

    private void handleMessage(Message aMessage) {

        LOGGER.log(Level.TRACE, "ReqReplyConsumer::handleMessage()");
        String myResponseText;
        // Handle the Filter property
        try {
            if (aMessage.getJMSReplyTo() != null) {
//                LOGGER.log(Level.INFO, "Server(" + serverId + ") send response for filter " + this.filterPropertyValue);
//                LOGGER.log(Level.DEBUG, "Server(" + serverId + ") take the msg and send ACK to " + aMessage.getJMSReplyTo().toString());

                // Use MessageId-Pattern
                // The MessageId of the Req is set to the correlationId of the response
                String correlationId = aMessage.getJMSMessageID();
                Destination myResponseDestination = aMessage.getJMSReplyTo();

                // Send an ACK first
                ReqReplyMessageCreator myResponseCreator = new ReqReplyMessageCreator("ACK", correlationId);
                myResponseCreator.setStringProperty(ReqReplySettings.PROPERTY_NAME_MSG_TYPE, ReqReplySettings.PROPERTY_VALUE_MSG_TYPE_ACK);
                myResponseCreator.setStringProperty(filterPropertyName, filterPropertyValue);
                jmsTemplate.send(myResponseDestination, myResponseCreator);
                LOGGER.log(Level.DEBUG, "Server send ACK"
                        + ", Ident: " + filterPropertyValue
                        + ", msgId: " + correlationId);

                myResponseText = filterPropertyDelegator.getPropertyFilterResult(filterPropertyValue);

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
                    myResponseCreator.setStringProperty(filterPropertyName, filterPropertyValue);

                    LOGGER.log(Level.DEBUG, "Server send response "
                            + (i + 1) + "/" + myMsgCount
                            + ", Ident: " + filterPropertyValue
                            + ", msgId: " + correlationId);

                    jmsTemplate.send(myResponseDestination, myResponseCreator);
                }
            }
        } catch (JMSException jMSException) {
            LOGGER.log(Level.ERROR, jMSException);
        }
    }

    public JmsTemplate getJmsTemplate() {
        return jmsTemplate;
    }

    public void setJmsTemplate(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public String getFilterPropertyName() {
        return filterPropertyName;
    }

    public void setFilterPropertyName(String filterPropertyName) {
        this.filterPropertyName = filterPropertyName;
    }

    public ReqReplyFilterInterface getFilterPropertyDelegator() {
        return filterPropertyDelegator;
    }

    public void setFilterPropertyDelegator(ReqReplyFilterInterface filterPropertyDelegator) {
        this.filterPropertyDelegator = filterPropertyDelegator;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public Integer getMaxMessageLength() {
        return maxMessageLength;
    }

    public void setMaxMessageLength(Integer maxMessageLength) {
        this.maxMessageLength = maxMessageLength;
    }

    public Destination getReqDestination() {
        return reqDestination;
    }

    public void setReqDestination(Destination reqDestination) {
        this.reqDestination = reqDestination;
    }

}
