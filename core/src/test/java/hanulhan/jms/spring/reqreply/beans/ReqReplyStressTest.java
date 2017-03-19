/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hanulhan.jms.spring.reqreply.beans;

import hanulhan.jms.spring.reqreply.util.ReqReplyReturnObject;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.annotation.PreDestroy;
import javax.jms.JMSException;
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
@ContextConfiguration(locations = {"/spring/springTest-jmsReqReplyStress.xml"})
public class ReqReplyStressTest implements ApplicationContextAware {

    private ApplicationContext applicationContext;
    static final Logger LOGGER = Logger.getLogger(ReqReplyStressTest.class);
    private List<ReqReplyStressTestHotel> HotelList = new ArrayList<>();
    private List<WebAction> WebActionList = new ArrayList<>();

    static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    static SecureRandom rnd = new SecureRandom();

    // Test Setup
    private static final int HOTEL_QUANTITY = 2;
    static final long THREAD_MAX_SLEEP_TIME_MS = 500;
    static final long THREAD_MIN_SLEEP_TIME_MS = 50;
    static final long RESPNSE_MAX_STRING_LENGTH = 200;
    static final long TEST_RUN_TIME_SECONDS = 30;


    public class WebAction extends Thread {

        private ReqReplyStressTestHotel hotel;
        private long sleepTime;
        private int webCallQuantity;
        private int webCallErrorCount;
        private int webCallErrorTimeoutCount;
        private int webCallErrorHeaderCount;

        @PreDestroy
        public void destroyIt() {
            this.interrupt();
        }

        public WebAction(ReqReplyStressTestHotel hotel) {
            super();
            this.hotel = hotel;
        }

        public void cancel() {
            interrupt();
        }

        @Override
        @SuppressWarnings("SleepWhileInLoop")
        public void run() {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    this.sleepTime = randomNumber(THREAD_MIN_SLEEP_TIME_MS, THREAD_MAX_SLEEP_TIME_MS);
                    Thread.sleep(sleepTime);
//                    LOGGER.log(Level.DEBUG, "WebAction [" + this.hotel.getSystemIdent() + "] run");
                    
                    ReqReplyPollingProducer myReqReply= (ReqReplyPollingProducer) applicationContext.getBean("bean_vmReqReplyProducer", 5000, 5000);
                    ReqReplyReturnObject myResponse;
                    try {
                        myResponse = myReqReply.sendAndAwaitingResponse("REQUEST", "SYSTEM_IDENT", hotel.getSystemIdent());
//                        Assert.assertTrue(myResponse.getStatus().toString(), myResponse.getStatusOK() == true);
                        if (myResponse.equals(hotel.getResponse()))  {
                            LOGGER.log(Level.ERROR, "NO RESPONSE MATCH");
                        }

                    } catch (JMSException jMSException) {
                        LOGGER.log(Level.ERROR, jMSException);
                    }
                    

                }

            } catch (InterruptedException e) {
                LOGGER.log(Level.TRACE, e);
            } catch (Exception e) {
                LOGGER.log(Level.ERROR, e);
            }
        }
    }

    public ReqReplyStressTest() {

        int i;
        ReqReplyStressTestHotel myHotel;
        int myResponseLength = (int) randomNumber(20, RESPNSE_MAX_STRING_LENGTH);

        // Initialize the Hotels and Threads
        for (i = 0; i < HOTEL_QUANTITY; i++) {

            myHotel = new ReqReplyStressTestHotel(randomString(6), randomString(myResponseLength));

            HotelList.add(myHotel);
            WebActionList.add(new WebAction(myHotel));
        }

        // initialize WebActions
    }

    @Override
    public void setApplicationContext(ApplicationContext ac) throws BeansException {
        this.applicationContext = ac;
    }

    @Test
    public void Test1() {

        int seconds;
        Date startTime = new Date();

        LOGGER.log(Level.DEBUG, "Test start");
        for (WebAction temp : WebActionList) {
            temp.start();
        }

        // Wait 
        LOGGER.log(Level.DEBUG, "Test running for " + TEST_RUN_TIME_SECONDS + " s");
        do {
            seconds = (int) ((new Date().getTime() - startTime.getTime()) / 1000);
        } while (seconds < TEST_RUN_TIME_SECONDS);

        // Stop Threads
        LOGGER.log(Level.INFO, "Stop Clients");
        for (WebAction temp : WebActionList) {
            temp.cancel();
        }
        
        try {
            // Wait to make sure everything is finished
            long waitTime= 10 * THREAD_MAX_SLEEP_TIME_MS;
            LOGGER.log(Level.INFO, "Wait " + waitTime + "s to finish");
            Thread.sleep(waitTime);
        } catch (InterruptedException ex) {
            LOGGER.log(Level.ERROR, ex);
        }

    }

    private long randomNumber(long aMinValue, long aMaxValue) {
        return aMinValue + (long) (Math.random() * (aMaxValue - aMinValue));
    }

    private String randomString(int len) {

        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        }
        return sb.toString();
    }
}
