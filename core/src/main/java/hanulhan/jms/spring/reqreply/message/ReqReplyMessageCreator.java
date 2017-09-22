/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hanulhan.jms.spring.reqreply.message;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.jms.core.MessageCreator;

/**
 *
 * @author uhansen
 */
public class ReqReplyMessageCreator implements MessageCreator {

    private Destination replyDestination= null;
    private String messageId, correlationId;
    private Message jmsMessage;
    private Map<String, String> propertyStringMap;
    private Map<String, Integer> propertyIntMap;
    private final Object object;
    private static final Logger LOGGER = Logger.getLogger(ReqReplyMessageCreator.class);

    /**
     *
     * @param aMsg
     * @param aReplyDestination
     */
    public ReqReplyMessageCreator(String aMsg, Destination aReplyDestination) {
        this.object = aMsg;
        this.replyDestination= aReplyDestination;
    }

    public ReqReplyMessageCreator(Object aMsg, Destination aReplyDestination) {
        this.object = aMsg;
        this.replyDestination= aReplyDestination;
    }    
    
    /**
     *
     * @param aMsg
     * @param aCorrelationId
     */
    public ReqReplyMessageCreator(String aMsg, String aCorrelationId) {
        this.correlationId = aCorrelationId;
        this.object = aMsg;
    }
//
//    public ReqReplyMessageCreator(String aMsg, Boolean aDoReply) {
//        this.messageText = aMsg;
//        doReply = aDoReply;
//    }

    @Override
    public Message createMessage(Session aSession) throws JMSException {
        Iterator myEntries;
        String myKey;

        LOGGER.log(Level.TRACE, "ReqMessageCreator:createMessage()");
        if (object instanceof String)   {
            jmsMessage = aSession.createTextMessage((String)object);
        } else if (object instanceof Serializable)  {
            jmsMessage = aSession.createObjectMessage((Serializable)object);
        }


        // Use MessageId-Pattern
        // The MessageId of the Req is set to the correlationId of the response
        if (replyDestination == null) {
            // This is a reply
            jmsMessage.setJMSCorrelationID(correlationId);
        } else {
            jmsMessage.setJMSReplyTo(replyDestination);
        }

        if (propertyStringMap != null && !propertyStringMap.isEmpty()) {
            myEntries = propertyStringMap.entrySet().iterator();
            while (myEntries.hasNext()) {
                Entry thisEntry = (Entry) myEntries.next();
                myKey = (String) thisEntry.getKey();
                String myStringValue = (String) thisEntry.getValue();
                jmsMessage.setStringProperty(myKey, myStringValue);
            }
        }
        if (propertyIntMap != null && !propertyIntMap.isEmpty()) {
            myEntries = propertyIntMap.entrySet().iterator();
            while (myEntries.hasNext()) {
                Entry thisEntry = (Entry) myEntries.next();
                myKey = (String) thisEntry.getKey();
                Integer myIntValue = (Integer) thisEntry.getValue();
                jmsMessage.setIntProperty(myKey, myIntValue);
            }
        }

        return jmsMessage;
    }

    /**
     *
     * @param aPropertyKey
     * @param aPropertyValue
     */
    public void setStringProperty(String aPropertyKey, String aPropertyValue) {
        if (propertyStringMap == null) {
            propertyStringMap = new HashMap<>();
        }
        propertyStringMap.put(aPropertyKey, aPropertyValue);
    }

    /**
     *
     * @param aPropertyKey
     * @param aPropertyValue
     */
    public void setIntProperty(String aPropertyKey, Integer aPropertyValue) {
        if (propertyIntMap == null) {
            propertyIntMap = new HashMap<>();
        }
        propertyIntMap.put(aPropertyKey, aPropertyValue);
    }


    /**
     *
     * @return
     * @throws JMSException
     */
    public String getMessageId() throws JMSException {
        return jmsMessage.getJMSMessageID();
    }

}
