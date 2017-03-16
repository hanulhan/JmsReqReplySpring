/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hanulhan.jms.spring.reqreply.util;

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

    private Destination tempDest;
    private String messageId, correlationId;
    private TextMessage txtMessage;
    private Map<String, String> propertyStringMap;
    private Map<String, Integer> propertyIntMap;
    private final String messageText;
    private final Boolean doReply;
    private static final Logger LOGGER = Logger.getLogger(ReqReplyMessageCreator.class);

    public ReqReplyMessageCreator(String aMsg, String aCorrelationId, Boolean aDoReply) {
        this.correlationId = aCorrelationId;
        this.messageText = aMsg;
        doReply = aDoReply;
    }

    public ReqReplyMessageCreator(String aMsg, Boolean aDoReply) {
        this.messageText = aMsg;
        doReply = aDoReply;
    }

    @Override
    public Message createMessage(Session aSession) throws JMSException {
        Iterator myEntries;
        String myKey;

        LOGGER.log(Level.TRACE, "ReqMessageCreator:createMessage()");
        txtMessage = aSession.createTextMessage(messageText);

        // Use MessageId-Pattern
        // The MessageId of the Req is set to the correlationId of the response
        if (!doReply) {
            // This is a reply
            txtMessage.setJMSCorrelationID(correlationId);
        }

        if (propertyStringMap != null && !propertyStringMap.isEmpty()) {
            myEntries = propertyStringMap.entrySet().iterator();
            while (myEntries.hasNext()) {
                Entry thisEntry = (Entry) myEntries.next();
                myKey = (String) thisEntry.getKey();
                String myStringValue = (String) thisEntry.getValue();
                txtMessage.setStringProperty(myKey, myStringValue);
            }
        }
        if (propertyIntMap != null && !propertyIntMap.isEmpty()) {
            myEntries = propertyIntMap.entrySet().iterator();
            while (myEntries.hasNext()) {
                Entry thisEntry = (Entry) myEntries.next();
                myKey = (String) thisEntry.getKey();
                Integer myIntValue = (Integer) thisEntry.getValue();
                txtMessage.setIntProperty(myKey, myIntValue);
            }
        }

        if (doReply) {
            tempDest = aSession.createTemporaryQueue();
            LOGGER.log(Level.DEBUG, "Creating tempQueue [" + tempDest.toString() + "]");
            txtMessage.setJMSReplyTo(tempDest);
        }

        return txtMessage;
    }

    public void setStringProperty(String aPropertyKey, String aPropertyValue) {
        if (propertyStringMap == null) {
            propertyStringMap = new HashMap<>();
        }
        propertyStringMap.put(aPropertyKey, aPropertyValue);
    }

    public void setIntProperty(String aPropertyKey, Integer aPropertyValue) {
        if (propertyIntMap == null) {
            propertyIntMap = new HashMap<>();
        }
        propertyIntMap.put(aPropertyKey, aPropertyValue);
    }

    public Destination getTempDest() {
        return tempDest;
    }

    public String getMessageText() {
        return messageText;
    }

    public String getMessageId() throws JMSException {
        return txtMessage.getJMSMessageID();
    }

}
