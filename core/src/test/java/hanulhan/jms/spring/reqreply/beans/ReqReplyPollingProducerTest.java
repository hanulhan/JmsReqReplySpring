/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hanulhan.jms.spring.reqreply.beans;

import hanulhan.jms.spring.reqreply.util.ReqReplyReturnObject;
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
@ContextConfiguration(locations = {"/spring/springTest-jmsReqReply.xml"})
public class ReqReplyPollingProducerTest implements ApplicationContextAware {

    private ApplicationContext applicationContext;
    static final Logger LOGGER = Logger.getLogger(ReqReplyPollingProducerTest.class);

    public ReqReplyPollingProducerTest() {
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

    @Test
    public void doTest() {
        ReqReplyPollingProducer myReqReply = (ReqReplyPollingProducer) applicationContext.getBean("bean_vmReqReplyProducer");
        ReqReplyReturnObject myResponse= null;
        try {
            myResponse = myReqReply.sendAndAwaitingResponse("My Message", "AAAA");
            Assert.assertTrue("Error", myResponse.getStatusOK() == true);
            
        } catch (JMSException jMSException) {
            LOGGER.log(Level.ERROR, jMSException);
        }

    }

//    @Test
    public void Test2() {
        Assert.assertTrue("", true);
    }

    @Override
    public void setApplicationContext(ApplicationContext ac) throws BeansException {
        this.applicationContext = ac;
    }

}
