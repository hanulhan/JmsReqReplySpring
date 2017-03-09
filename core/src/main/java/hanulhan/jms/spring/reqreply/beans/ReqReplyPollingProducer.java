/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hanulhan.jms.spring.reqreply.beans;

import hanulhan.jms.spring.reqreply.util.ReqReplyProducerCallback;
import javax.jms.Destination;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jms.core.JmsTemplate;

/**
 *
 * @author uhansen
 */
public class ReqReplyPollingProducer implements ApplicationContextAware {

    private ApplicationContext applicationContext;
    private JmsTemplate jmsTemplate;
    private Destination destination;
    static final Logger LOGGER= Logger.getLogger(ReqReplyPollingProducer.class);

    public ReqReplyPollingProducer() {
        LOGGER.log(Level.TRACE, "ReqReplyPollingProducer:ReqReplyPollingProducer()");
    }
    
    
    
    public String sendAndAwaitingResponse(String aMessageText, String aSystemIdent) {
        String myResponse;
        LOGGER.log(Level.TRACE, "ReqReplyPollingProducer:sendAndAwaitingResponse()");
        
        ReqReplyProducerCallback  myReqReplyProducer= new ReqReplyProducerCallback(destination, aMessageText, aSystemIdent);
        
        myResponse= (String)jmsTemplate.execute(myReqReplyProducer);
        return myResponse;
    }

    public void setJmsTemplate(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public void setDestination(Destination destination) {
        LOGGER.log(Level.TRACE, "ReqReplyPollingProducer:setDestination()");
        this.destination = destination;
    }
    
    
    
    @Override
    public void setApplicationContext(ApplicationContext ac) throws BeansException {
        this.applicationContext= ac;
    }
    
}
