/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hanulhan.jms.spring.reqreply.beans;

import static hanulhan.jms.spring.reqreply.beans.ReqReplyStressTest.TEST_WAIT_TO_FINISH_SECONDS;
import hanulhan.jms.spring.reqreply.util.ReqReplyReturnObject;
import java.util.Date;
import javax.jms.JMSException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
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

public class ReqReplyTest implements ApplicationContextAware {

    private ApplicationContext applicationContext;
    private static final Logger LOGGER = Logger.getLogger(ReqReplyTest.class);

    public ReqReplyTest() {

//        LOGGER.log(Level.INFO, "StartBroker()");
//        try {
//            BrokerService broker = new BrokerService();
//            broker.setPersistent(false);
//            broker.setUseJmx(false);
//            broker.addConnector("tcp://localhost:61616");
//            broker.start();
//        } catch (Exception exception) {
//            LOGGER.log(Level.ERROR, exception);
//        }
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

        LOGGER.log(Level.DEBUG, "Wait 15 seconds");
        Date startTime = new Date();
        long seconds;
        do {
            seconds = (int) ((new Date().getTime() - startTime.getTime()) / 1000);
        } while (seconds < 15);
        
    }


    @Override
    public void setApplicationContext(ApplicationContext ac) throws BeansException {
        this.applicationContext = ac;
    }

}


