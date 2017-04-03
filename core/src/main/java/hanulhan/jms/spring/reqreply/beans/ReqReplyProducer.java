/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hanulhan.jms.spring.reqreply.beans;

import hanulhan.jms.spring.reqreply.util.ReqReplyMessageCreator;
import hanulhan.jms.spring.reqreply.util.ReqReplyMessageObject;
import hanulhan.jms.spring.reqreply.util.ReqReplyMessageStorage;
import hanulhan.jms.spring.reqreply.util.ReqReplyStatusCode;
import java.util.Date;
import javax.annotation.PostConstruct;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;

/**
 *
 * @author uhansen
 */
public class ReqReplyProducer implements MessageListener {

    // injected stuff
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

    @PostConstruct
    void InitMe()   {
        messageStorage = new ReqReplyMessageStorage(filterName);
    }
    
    
    public String getResponse(String aRequest, String aFilterValue, long aTimeoutMilliSec) throws InterruptedException {
        Date startTime = new Date();
        int myMilliSeconds;
        String myMessageId;
        
        myMessageId= sendRequest(aRequest, aFilterValue);

        // Wait 
        do {
            myMilliSeconds = (int) ((new Date().getTime() - startTime.getTime()));
            Thread.sleep(10);
        } while (myMilliSeconds < aTimeoutMilliSec && !messageStorage.isResponseReceived(myMessageId));

        if (messageStorage.isResponseReceived(myMessageId))  {
            return messageStorage.getResponse(myMessageId);
        } else {
            ReqReplyMessageObject myMsgObj= messageStorage.getMsgObj(myMessageId);
            LOGGER.log(Level.ERROR, "################ RESPONSE is null #####################");
            LOGGER.log(Level.ERROR, myMsgObj.toString());
        }
        return null;
    }
    
    
    public String sendRequest(String aMessageText, String aFilterValue) {
        String myMessageId= null;
        ReqReplyMessageCreator myReqMessage = new ReqReplyMessageCreator(aMessageText, replyDestination);
        myReqMessage.setStringProperty(filterName, aFilterValue);

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

    @Override
    public void onMessage(Message aMessage) {
        ReqReplyStatusCode myStatus= ReqReplyStatusCode.STATUS_ERROR;
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
    }

    public ReqReplyMessageStorage getMessageStorage() {
        return messageStorage;
    }
    
    

}
