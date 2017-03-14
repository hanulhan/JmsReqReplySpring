/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hanulhan.jms.spring.reqreply.util;

import java.util.Random;
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
public class ReqMessageCreator implements MessageCreator {

    private Destination tempDest;
    
    private final String correlationId;
    private TextMessage txtMessage;
    private final String ident;
    private final String messageText;
    private static final Logger LOGGER = Logger.getLogger(ReqMessageCreator.class);

    public ReqMessageCreator(String aMsg, String aCorrelationId, String aSystemIdent) {
        this.correlationId = aCorrelationId;
        this.messageText= aMsg;
        this.ident= aSystemIdent;

    }

    @Override
    public Message createMessage(Session aSession) throws JMSException {
        LOGGER.log(Level.TRACE, "ReqMessageCreator:createMessage()");
        tempDest= aSession.createTemporaryQueue();
        LOGGER.log(Level.DEBUG, "Creating tempQueue [" + tempDest.toString() + "]");
        txtMessage= aSession.createTextMessage(messageText);
        txtMessage.setJMSCorrelationID(correlationId);
        txtMessage.setStringProperty(ReqReplySettings.PROPERTY_NAME_IDENT, ident);
        txtMessage.setJMSReplyTo(tempDest);
        return txtMessage;
    }

    public Destination getTempDest() {
        return tempDest;
    }

    public String getMessageText() {
        return messageText;
    }


    
}
