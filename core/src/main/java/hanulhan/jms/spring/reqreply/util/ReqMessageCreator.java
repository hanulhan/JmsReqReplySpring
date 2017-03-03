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
import org.springframework.jms.core.MessageCreator;

/**
 *
 * @author uhansen
 */
public class ReqMessageCreator implements MessageCreator {

    private final String correlationId;
    private final Destination replyTo;
    private TextMessage txtMessage;
    private String ident;
    private final String messageText;

    public ReqMessageCreator(String aMsg, Destination aReplyTo) {
        this.correlationId = createRandomString();
        this.messageText= aMsg;
        this.replyTo= aReplyTo;
        
    }

    private String createRandomString() {
        Random random;
        random = new Random(System.currentTimeMillis());
        long randomLong = random.nextLong();
        return Long.toHexString(randomLong);
    }

    @Override
    public Message createMessage(Session sn) throws JMSException {
        txtMessage= sn.createTextMessage(messageText);
        txtMessage.setJMSCorrelationID(correlationId);
        txtMessage.setStringProperty(ident, ident);
        txtMessage.setJMSReplyTo(replyTo);
        return txtMessage;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public String getMessageText() {
        return messageText;
    }

    
}
