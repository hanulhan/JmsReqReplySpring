/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hanulhan.jms.spring.reqreply.beans;

import javax.jms.JMSException;
import junit.framework.Assert;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author uhansen
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/spring/springTest-jmsReqReply.xml"})
public class ReqReplyPollingProducerTest implements ApplicationContextAware {
    
    private ApplicationContext applicationContext;
    static final Logger LOGGER= Logger.getLogger(ReqReplyPollingProducerTest.class);
    public ReqReplyPollingProducerTest() {
    }


    @Test
    public void doTest() {
        ReqReplyPollingProducer myReqReply= (ReqReplyPollingProducer) applicationContext.getBean("bean_vmReqReplyProducer");
        String myResponse= null;
        try {
            myResponse = myReqReply.sendAndAwaitingResponse("My Message", "AAAA");
        } catch (JMSException jMSException) {
            LOGGER.log(Level.ERROR, jMSException);
        }
//        Assert.assertTrue("No Respnse", myResponse != null);
                
    }

    @Override
    public void setApplicationContext(ApplicationContext ac) throws BeansException {
        this.applicationContext= ac;
    }
    
}
