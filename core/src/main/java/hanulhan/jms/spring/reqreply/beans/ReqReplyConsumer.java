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

/**
 *
 * @author uhansen
 */
public class ReqReplyConsumer implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    // injected stuff
    private Destination destination;
    private JmsTemplate jmsTemplate;
    private String filterPropertyName;
    private ReqReplyFilterInterface filterPropertyInstance;
    private String serverId;

    // internal
    private static final Logger LOGGER = Logger.getLogger(ReqReplyConsumer.class);

    public ReqReplyConsumer() {
    }

    public void onReceive(Message aMessage) {

        LOGGER.log(Level.TRACE, "ReqReplyConsumer::onReceive()");
        try {
            if (aMessage instanceof TextMessage) {
                if (filterPropertyName != null && !filterPropertyName.trim().isEmpty()) {
                    if (aMessage.propertyExists(filterPropertyName)) {
                        LOGGER.log(Level.TRACE, "Filter property in Message: " + aMessage.getStringProperty(filterPropertyName));
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

        if (aMessage.getJMSReplyTo() != null) {
            LOGGER.log(Level.INFO, "Server(" + serverId + ") take the msg and send ACK to " + aMessage.getJMSReplyTo().toString());
            TextMessage response = this.session.createTextMessage();
            response.setText("Server(" + serverId + ") ACK to msg: [" + messageText + "], Id: " + aMessage.getJMSCorrelationID());
            response.setJMSCorrelationID(aMessage.getJMSCorrelationID());
            this.replyProducer.send(aMessage.getJMSReplyTo(), response);

            sleep(500);

            for (i = 1; i < 4; i++) {
                response = this.session.createTextMessage();
                response.setIntProperty(Settings.PROPERTY_NAME_COUNT, i);
                response.setIntProperty(Settings.PROPERTY_NAME_TOTAL_COUNT, 3);
                response.setText("Server(" + serverId + ") Response " + i + " von 3 to msg: [" + messageText + "], Id: " + aMessage.getJMSCorrelationID());
                response.setJMSCorrelationID(aMessage.getJMSCorrelationID());
                LOGGER.log(Level.INFO, "Server(" + serverId + ") send response "
                        + response.getStringProperty(Settings.PROPERTY_NAME_COUNT)
                        + "/"
                        + response.getStringProperty(Settings.PROPERTY_NAME_TOTAL_COUNT)
                        + " to " + aMessage.getJMSReplyTo().toString());

                this.replyProducer.send(aMessage.getJMSReplyTo(), response);
            }
        }
        LOGGER.log(Level.INFO, "Press x + <Enter> to terminate the Server  \n");
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

}
