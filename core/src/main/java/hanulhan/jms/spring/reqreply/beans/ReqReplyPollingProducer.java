/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hanulhan.jms.spring.reqreply.beans;

import hanulhan.jms.spring.reqreply.util.ReqReplyProducerCallback;
import javax.jms.Destination;
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

    public ReqReplyPollingProducer() {
    }
    
    
    
    public String sendAndAwaitingResponse(String aMessageText, String aSystemIdent) {
        String myResponse;
        ReqReplyProducerCallback  myReqReplyProducer= new ReqReplyProducerCallback(destination, aMessageText, aSystemIdent);
        
        myResponse= (String)jmsTemplate.execute(myReqReplyProducer);
        return myResponse;
    }

    public void setJmsTemplate(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
    }
    
    
    
    @Override
    public void setApplicationContext(ApplicationContext ac) throws BeansException {
        this.applicationContext= ac;
    }
    
}
