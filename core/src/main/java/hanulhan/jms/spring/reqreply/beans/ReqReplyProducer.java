/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hanulhan.jms.spring.reqreply.beans;

import hanulhan.jms.spring.reqreply.util.ReqReplyMessageCreator;
import hanulhan.jms.spring.reqreply.util.ReqReplyMessageObject;
import hanulhan.jms.spring.reqreply.util.ReqReplyMessageStorage;
import hanulhan.jms.spring.reqreply.util.ReqReplySettings;
import hanulhan.jms.spring.reqreply.util.ReqReplyStatusCode;
import java.util.Date;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;

/**
 *  Provides functions to write a Request to a Queue/Topic and receive the response
 *  to the request.<p>
 *  A Message Listener writes received responses to a "messageStorage". 
 *  The receive function check the message storage wether the response is received.
 */
public class ReqReplyProducer implements MessageListener {

    // injected stuff
    @Autowired
    private ApplicationContext applicationContext;
    private JmsTemplate jmsTemplate;
    private Destination requestDestination;
    private Destination replyDestination;
    private String filterName;

    // internal
    private ReqReplyMessageStorage messageStorage;
    static final Logger LOGGER = Logger.getLogger(ReqReplyProducer.class);

    public ReqReplyProducer() {
        LOGGER.log(Level.TRACE, "ReqReplyProducer:ReqReplyProducer()");
    }

    
    /**
     * Check the "messageStorage wether a response is received within "aTimeoutMilliSec" 
     * for the given Filter (aFilterValue)
     * @param aRequest
     * @param aCommand
     * @param aPort
     * @param aFilterValue
     * @param aTimeoutMilliSec
     * @return the JMS-messageId or null
     * @throws InterruptedException 
     */
    @SuppressWarnings("SleepWhileInLoop")
    public String getResponse(String aRequest, String aCommand, int aPort, String aFilterValue, long aTimeoutMilliSec) throws InterruptedException {
        Date startTime = new Date();
        int myMilliSeconds;
        String myMessageId;
        
        myMessageId= sendRequest(aRequest, aCommand, aPort, aFilterValue);

        // Wait 
        do {
            myMilliSeconds = (int) ((new Date().getTime() - startTime.getTime()));
            Thread.sleep(10);
        } while (myMilliSeconds < aTimeoutMilliSec && !messageStorage.isResponseReceived(myMessageId));

        if (messageStorage.isResponseReceived(myMessageId))  {
            return messageStorage.getResponse(myMessageId);
        } else {
            //ReqReplyMessageObject myMsgObj= messageStorage.getMsgObj(myMessageId);
//            LOGGER.log(Level.ERROR, "################ RESPONSE is null #####################");
            //LOGGER.log(Level.ERROR, myMsgObj.toString());
        }
        return null;
    }

    @SuppressWarnings("SleepWhileInLoop")
    public ReqReplyMessageObject getResponseObj(String aRequest, String aCommand, int aPort, String aFilterValue, long aTimeoutMilliSec) throws InterruptedException {
        Date startTime = new Date();
        int myMilliSeconds;
        String myMessageId;
        
        myMessageId= sendRequest(aRequest, aCommand, aPort, aFilterValue);

        // Wait 
        do {
            myMilliSeconds = (int) ((new Date().getTime() - startTime.getTime()));
            Thread.sleep(10);
        } while (myMilliSeconds < aTimeoutMilliSec && !messageStorage.isResponseReceived(myMessageId));

        if (messageStorage.isResponseReceived(myMessageId))  {
            return messageStorage.getResponseObj(myMessageId);
        } else {
            //ReqReplyMessageObject myMsgObj= messageStorage.getMsgObj(myMessageId);
//            LOGGER.log(Level.ERROR, "################ RESPONSE is null #####################");
            //LOGGER.log(Level.ERROR, myMsgObj.toString());
        }
        return null;
    }
    
    /**
     * Send the Request to a Queue/Topic, add Property information for FilterValue
     * and return the JMS-messageId
     * @param aMessageText
     * @param aFilterValue
     * @return the JMS-MessageId or null
     */
    public String sendRequest(String aMessageText, String aCommand, int aPort, String aFilterValue) {
        String myMessageId;
        ReqReplyMessageCreator myReqMessage = new ReqReplyMessageCreator(aMessageText, replyDestination);
        myReqMessage.setStringProperty(filterName, aFilterValue);
        myReqMessage.setStringProperty(ReqReplySettings.PROPERTY_VALUE_COMMAND, aCommand);
        myReqMessage.setIntProperty(ReqReplySettings.PROPERTY_VALUE_PORT, aPort);

        try {
            jmsTemplate.send(requestDestination, myReqMessage);
            myMessageId = myReqMessage.getMessageId();
            if (myMessageId != null) {
                messageStorage.add(myMessageId, aFilterValue);
            } else {
                LOGGER.log(Level.ERROR, "messageId is null! ");
            }
            LOGGER.log(Level.DEBUG, "Sending Request [" + myMessageId + "] for Ident: " + aFilterValue);

        } catch (JmsException | JMSException jmsException) {
            LOGGER.log(Level.ERROR, "Error sending msg! " + jmsException);
            return null;
        }
        return myMessageId;
    }


    /**
     * Spring/JMS Default Message Listener Container.
     * Receive the response from the Reply TOPIC and put it to the "messageStorage"
     * @param aMessage 
     */
    @Override
    public void onMessage(Message aMessage) {
        ReqReplyStatusCode myStatus;
        LOGGER.log(Level.DEBUG, "onMessage()");
        try {
            myStatus= messageStorage.add(aMessage);
            if (myStatus.isError()) {
                LOGGER.log(Level.ERROR, "Error adding message to storage");
            }
        } catch (JMSException ex) {
            LOGGER.log(Level.ERROR, ex);
        }

    }
    
    public int getStorageSize() {
        return messageStorage.size();
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
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

    public String getFilterName() {
        return filterName;
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
        messageStorage = new ReqReplyMessageStorage(filterName);
    }

    public ReqReplyMessageStorage getMessageStorage() {
        return messageStorage;
    }
}
