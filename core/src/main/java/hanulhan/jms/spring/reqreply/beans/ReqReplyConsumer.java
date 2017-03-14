/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hanulhan.jms.spring.reqreply.beans;

import static java.lang.Thread.sleep;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jms.core.JmsTemplate;
import hanulhan.jms.spring.reqreply.util.ReqReplyFilterInterface;
import hanulhan.jms.spring.reqreply.util.ReqReplyMessageCreator;
import hanulhan.jms.spring.reqreply.util.ReqReplySettings;
import javax.annotation.PostConstruct;

/**
 *
 * @author uhansen
 */
public class ReqReplyConsumer implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    // injected stuff
    private JmsTemplate jmsTemplate;
    private Destination destination;
    private String filterPropertyName;
    private ReqReplyFilterInterface filterPropertyInstance;
    private String serverId;
    private Integer maxMessageLength;

    // internal
    private static final Logger LOGGER = Logger.getLogger(ReqReplyConsumer.class);

    @PostConstruct
    public void postConstruct() {
        LOGGER.log(Level.TRACE, "ReqReplyConsuer:postConstuct");
        LOGGER.log(Level.TRACE, "destination: " + destination.toString());
        LOGGER.log(Level.TRACE, "jmsTemplate: " + jmsTemplate.toString());
        LOGGER.log(Level.TRACE, "filterPropertyName: " + filterPropertyName);
        LOGGER.log(Level.TRACE, "maxMessageLength: " + maxMessageLength);
    }
    
    public ReqReplyConsumer() {
        LOGGER.log(Level.TRACE, "ReqReplyConsumer::ReqReplyConsumer()");
    }
    
    public void receive(String stringMsg)   {
        LOGGER.log(Level.TRACE, "Received String Message");
    }

    public void receive(Message aMessage) {

        LOGGER.log(Level.TRACE, "ReqReplyConsumer::onReceive()");
        try {
            if (aMessage instanceof TextMessage) {
                if (filterPropertyName != null && !filterPropertyName.trim().isEmpty()) {
                    if (aMessage.propertyExists(filterPropertyName)) {
                        LOGGER.log(Level.TRACE, "Filter property in Message: " + aMessage.getStringProperty(filterPropertyName));
                        // Check if the filter-property should be handled
                        if (filterPropertyInstance.getPropertyFilterActive(filterPropertyName)) {
                            handleMessage(aMessage);
                        } else {
                            LOGGER.log(Level.TRACE, "Message received, but fiter property does not fit");
                        }
                    } else {
                        LOGGER.log(Level.TRACE, "Message received, but no filter property set");
                    }
                } else {
                    LOGGER.log(Level.TRACE, "No Filter property set");
                }
            } else {
                LOGGER.log(Level.TRACE, "Message received, but not a TextMessage");
            }
        } catch (JMSException jMSException) {
            LOGGER.log(Level.ERROR, jMSException);
        }
    }

    private void handleMessage(Message aMessage) {

        LOGGER.log(Level.TRACE, "ReqReplyConsumer::handleMessage()");
        // Handle the Filter property
        try {
            if (aMessage.getJMSReplyTo() != null) {
                LOGGER.log(Level.INFO, "Server(" + serverId + ") take the msg and send ACK to " + aMessage.getJMSReplyTo().toString());

                String correlationId = aMessage.getJMSCorrelationID();
                Destination myResponseDestination= aMessage.getJMSReplyTo();

                // Send an ACK first
                ReqReplyMessageCreator myResponseCreator = new ReqReplyMessageCreator("ACK", correlationId, false);
                jmsTemplate.send(myResponseDestination, myResponseCreator);

                String myResponse = filterPropertyInstance.getPropertyFilterResult(filterPropertyName);

                int myMsgCount;
                myMsgCount = (int) Math.ceil(myResponse.length() / maxMessageLength);

                LOGGER.log(Level.TRACE, "Split Response into " + myMsgCount + " pieces");

                int myStartIndex;
                int myEndIndex;
                for (int i = 0; i < myMsgCount; i++) {
                    myStartIndex = i * maxMessageLength;
                    myEndIndex   = ((i + 1) * maxMessageLength) -1;
                    if (myEndIndex > myResponse.length())   {
                        myEndIndex = myResponse.length();
                    }
                    String myMessagePart = myResponse.substring(myStartIndex, myEndIndex);
                    
                    myResponseCreator = new ReqReplyMessageCreator(myMessagePart, correlationId, false);
                    myResponseCreator.setIntProperty(ReqReplySettings.PROPERTY_NAME_TOTAL_COUNT, myMsgCount);
                    myResponseCreator.setIntProperty(ReqReplySettings.PROPERTY_NAME_COUNT, i+1);
                   
                    LOGGER.log(Level.INFO, "Server(" + serverId + ") send response ("
                            + i+1
                            + "/"
                            + myMsgCount
                            + ") index: "
                            + myStartIndex 
                            + "-"
                            + myEndIndex
                            + " to " + myResponseDestination.toString());

                    jmsTemplate.send(myResponseDestination, myResponseCreator);
                }
            }
        } catch (JMSException jMSException) {
            LOGGER.log(Level.ERROR, jMSException);
        }
    }

    public Destination getDestination() {
        return destination;
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
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

    public ReqReplyFilterInterface getFilterPropertyInstance() {
        return filterPropertyInstance;
    }

    public void setFilterPropertyInstance(ReqReplyFilterInterface filterPropertyInstance) {
        this.filterPropertyInstance = filterPropertyInstance;
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

    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

}
