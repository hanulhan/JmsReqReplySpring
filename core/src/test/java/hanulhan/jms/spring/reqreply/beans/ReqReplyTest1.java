/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hanulhan.jms.spring.reqreply.beans;

import static hanulhan.jms.spring.reqreply.beans.ReqReplyTest2.TEST_WAIT_TO_FINISH_SECONDS;
import hanulhan.jms.spring.reqreply.util.ReqReplyMessageObject;
import hanulhan.jms.spring.reqreply.util.ReqReplyTest1_FilterDelegator;
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
@ContextConfiguration(locations = {"/spring/springTest-1.xml"})

public class ReqReplyTest1 implements ApplicationContextAware {

    private ApplicationContext applicationContext;
    private static final Logger LOGGER = Logger.getLogger(ReqReplyTest1.class);

    public ReqReplyTest1() {

    }

    /**
     * Test of onReceive method, of class ReqReplyConsumer.
     */
    @Test
    public void testMe() {
        try {
            ReqReplyProducer myReqReply = (ReqReplyProducer) applicationContext.getBean("bean_vmReqReplyProducer");
            ReqReplyTest1_FilterDelegator myFilterDelegator = new ReqReplyTest1_FilterDelegator();
            String myResponse = null;
            myResponse = myReqReply.getResponse("My Message", "SY", 1234, "AAAA", 2000);


            LOGGER.log(Level.DEBUG, "Wait 2 seconds");
            Date startTime = new Date();
            long seconds;
            do {
                seconds = (int) ((new Date().getTime() - startTime.getTime()) / 1000);
            } while (seconds < 2 );
            Assert.assertTrue("Response TIMEOUT", myResponse != null);
            Assert.assertTrue("NO response match", myResponse.equals(myFilterDelegator.getPropertyFilterResult("AAAA")));
        } catch (InterruptedException ex) {
            LOGGER.log(Level.ERROR, ex);
        }

    }

    @Override
    public void setApplicationContext(ApplicationContext ac) throws BeansException {
        this.applicationContext = ac;
    }

}
