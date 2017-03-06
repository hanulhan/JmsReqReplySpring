/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hanulhan.jms.spring.reqreply.beans;

import hanulhan.jms.spring.reqreply.util.ReqReplyProducerCallback;
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
        ReqReplyProducerCallback myReqReply= (ReqReplyProducerCallback) applicationContext.getBean("bean_vmReqReplyProducer");
    }

    @Override
    public void setApplicationContext(ApplicationContext ac) throws BeansException {
        this.applicationContext= ac;
    }
    
}
