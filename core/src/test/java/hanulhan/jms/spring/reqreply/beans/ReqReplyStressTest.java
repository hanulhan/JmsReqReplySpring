/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hanulhan.jms.spring.reqreply.beans;

import hanulhan.jms.spring.reqreply.util.ReqReplyStressTestHotel;
import hanulhan.jms.spring.reqreply.util.ReqReplyReturnObject;
import hanulhan.jms.spring.reqreply.util.ReqReplyStatusCode;
import hanulhan.jms.spring.reqreply.util.ReqReplyStressTestHotelList;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
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
    private List<WebAction> WebActionList = new ArrayList<>();

    static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    static SecureRandom rnd = new SecureRandom();

    // Test Setup
    private static final int HOTEL_QUANTITY = 20;
    static final long THREAD_MAX_SLEEP_TIME_MS = 500;
    static final long THREAD_MIN_SLEEP_TIME_MS = 50;
    static final long RESPNSE_MAX_STRING_LENGTH = 200;
    static final long TEST_RUN_TIME_SECONDS = 10;

    public class WebAction extends Thread {

        private final ReqReplyStressTestHotel hotel;
        private long sleepTime;
        private int webCallQuantity;
        private int webCallErrorCount;
        private int webCallErrorTimeoutCount;
        private int webCallErrorHeaderCount;
        private Boolean run;

        @PreDestroy
        public void destroyIt() {
            this.interrupt();
        }

        public WebAction(ReqReplyStressTestHotel hotel) {
            super();
            this.hotel = hotel;
            this.run = true;
            webCallQuantity = 0;
            webCallErrorCount = 0;
            webCallErrorTimeoutCount = 0;
            webCallErrorHeaderCount = 0;
        }

        public void stopSending() {
            this.run = false;
        }

        public void cancel() {
            interrupt();
        }

        @Override
        @SuppressWarnings("SleepWhileInLoop")
        public void run() {
            try {
                while (this.run) {
                    this.sleepTime = randomNumber(THREAD_MIN_SLEEP_TIME_MS, THREAD_MAX_SLEEP_TIME_MS);
                    Thread.sleep(sleepTime);
                    LOGGER.log(Level.INFO, "WebAction [" + this.hotel.getSystemIdent() + "] run");

                    webCallQuantity++;
                    ReqReplyPollingProducer myReqReply = (ReqReplyPollingProducer) applicationContext.getBean("bean_vmReqReplyProducer", 1000, 1000);
                    ReqReplyReturnObject myResponse;
                    try {
                        myResponse = myReqReply.sendAndAwaitingResponse("REQUEST", "SYSTEM_IDENT", hotel.getSystemIdent());

                        if (!hotel.compareResponse(myResponse.getPayload())) {
                            LOGGER.log(Level.ERROR, "NO RESPONSE MATCH");
                            webCallErrorCount++;
                            if (myResponse.getStatus() == ReqReplyStatusCode.STATUS_RESPONSE_TIMEOUT) {
                                webCallErrorTimeoutCount++;
                            } else if (myResponse.getStatus() == ReqReplyStatusCode.STATUS_RESPONSE_HEADER_ERROR) {
                                webCallErrorHeaderCount++;
                            }
                        }

                    } catch (JMSException jMSException) {
                        LOGGER.log(Level.ERROR, jMSException);
                    }
                    if (Thread.currentThread().isInterrupted()) {
                        this.run = false;
                    }

                }

            } catch (InterruptedException e) {
                LOGGER.log(Level.TRACE, e);
            } catch (BeansException e) {
                LOGGER.log(Level.ERROR, e);
            }
        }

        public int getWebCallQuantity() {
            return webCallQuantity;
        }

        public int getWebCallErrorCount() {
            return webCallErrorCount;
        }

        public int getWebCallErrorTimeoutCount() {
            return webCallErrorTimeoutCount;
        }

        public int getWebCallErrorHeaderCount() {
            return webCallErrorHeaderCount;
        }

        public ReqReplyStressTestHotel getHotel() {
            return hotel;
        }

    }

    public ReqReplyStressTest() {

    }

    @PostConstruct
    public void InitTest() {
        int i;

        int myResponseLength = (int) randomNumber(20, RESPNSE_MAX_STRING_LENGTH);
        ReqReplyStressTestHotelList myHotelList = (ReqReplyStressTestHotelList) applicationContext.getBean("bean_HotelList");
        ReqReplyStressTestHotel myHotel;

        // Initialize the Hotels and Threads
        for (i = 0; i < HOTEL_QUANTITY; i++) {

            myHotel = new ReqReplyStressTestHotel(randomString(6), randomString(myResponseLength));
//            myHotel = new ReqReplyStressTestHotel(randomString(6), "ABCDEFG");
            myHotelList.add(myHotel);
            WebActionList.add(new WebAction(myHotel));
        }

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
            long waitTime = 10 * THREAD_MAX_SLEEP_TIME_MS;
            LOGGER.log(Level.INFO, "Wait " + waitTime + "s to finish");
            Thread.sleep(waitTime);
        } catch (InterruptedException ex) {
            LOGGER.log(Level.ERROR, ex);
        }

        for (WebAction temp : WebActionList) {
            LOGGER.log(Level.INFO, "Hotel {"
                    + temp.getHotel().getSystemIdent()
                    + "} "
                    + " Calls:" + temp.getWebCallQuantity()
                    + " Responses: " + temp.getHotel().getResponseCount()
                    + " Errors: " + temp.getWebCallErrorCount());
            Assert.assertTrue("Error in transmission", temp.getWebCallErrorCount() == 0);
            Assert.assertTrue("Req and response counts different", temp.getWebCallQuantity() == temp.getHotel().getResponseCount());
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
