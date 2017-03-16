/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hanulhan.jms.spring.reqreply.beans;

import hanulhan.jms.spring.reqreply.util.ReqReplyReturnObject;
import hanulhan.jms.spring.reqreply.util.ReqReplySettings;
import javax.jms.JMSException;
import org.apache.activemq.broker.BrokerService;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;
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
@ContextConfiguration(locations = {"/spring/springTest-jmsReqReplyConsumer.xml"})

public class ReqReplyConsumerTest implements ApplicationContextAware {

    private ApplicationContext applicationContext;
    private static final Logger LOGGER = Logger.getLogger(ReqReplyConsumerTest.class);

    public ReqReplyConsumerTest() {

        LOGGER.log(Level.INFO, "StartBroker()");
        try {
            BrokerService broker = new BrokerService();
            broker.setPersistent(false);
            broker.setUseJmx(false);
            broker.addConnector("tcp://localhost:61616");
            broker.start();
        } catch (Exception exception) {
            LOGGER.log(Level.ERROR, exception);
        }
    }

    /**
     * Test of onReceive method, of class ReqReplyConsumer.
     */
    @Test
    public void testMe() {
        ReqReplyPollingProducer myReqReply= (ReqReplyPollingProducer) applicationContext.getBean("bean_vmReqReplyProducer", 5000, 5000);
        ReqReplyReturnObject myResponse;
        try {
            myResponse = myReqReply.sendAndAwaitingResponse("My Message", "SYSTEM_IDENT", "AAAA");
            Assert.assertTrue(myResponse.getStatus().toString(), myResponse.getStatusOK() == true);
        
        } catch (JMSException jMSException) {
            LOGGER.log(Level.ERROR, jMSException);
        }

        
    }


    @Override
    public void setApplicationContext(ApplicationContext ac) throws BeansException {
        this.applicationContext = ac;
    }

}
