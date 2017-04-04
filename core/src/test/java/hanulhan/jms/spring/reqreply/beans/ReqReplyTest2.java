/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hanulhan.jms.spring.reqreply.beans;

import hanulhan.jms.spring.reqreply.util.ReqReplyTest2_Hotel;
import hanulhan.jms.spring.reqreply.util.ReqReplyTest2_HotelList;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
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
@ContextConfiguration(locations = {"/spring/springTest-2.xml"})
public class ReqReplyTest2 implements ApplicationContextAware {

    private ApplicationContext applicationContext;
    static final Logger LOGGER = Logger.getLogger(ReqReplyTest2.class);
    private List<WebAction> WebActionList = new ArrayList<>();

    static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    static SecureRandom rnd = new SecureRandom();

    // Test Setup
    private static final int HOTEL_QUANTITY = 50;
    static final long THREAD_MAX_SLEEP_TIME_MS = 500;
    static final long THREAD_MIN_SLEEP_TIME_MS = 50;
    static final long RESPNSE_MAX_STRING_LENGTH = 200;
    static final long TEST_RUN_TIME_SECONDS = 30;
    static final long TEST_WAIT_TO_FINISH_SECONDS = 5;

    /**
     * The WebAction class represents a web-session. A user requests
     * data from a system. Multiple WebAction threads are uses in this test
     */
    public class WebAction extends Thread {

        private final ReqReplyTest2_Hotel hotel;
        private int webCallQuantity;
        private int webCallErrorCount;
        private int webCallErrorTimeoutCount;
        private Boolean run;

        /**
         *
         */
        @PreDestroy
        public void destroyIt() {
            this.interrupt();
        }

        /**
         *
         * @param hotel
         * In this test, the WebAction is linked to a hotel
         */
        public WebAction(ReqReplyTest2_Hotel hotel) {
            super();
            this.hotel = hotel;
            this.run = true;
            webCallQuantity = 0;
            webCallErrorCount = 0;
            webCallErrorTimeoutCount = 0;
        }

        /**
         *
         */
        public void stopSending() {
            this.run = false;
        }

        /**
         *
         */
        public void cancel() {
            interrupt();
        }

        @Override
        @SuppressWarnings("SleepWhileInLoop")
        public void run() {
            Date myStartTime;
            long sleepTime;
            long myMilliSeconds;
            while (this.run) {
                sleepTime = randomNumber(THREAD_MIN_SLEEP_TIME_MS, THREAD_MAX_SLEEP_TIME_MS);
                myStartTime = new Date();
                do {
                    myMilliSeconds = (int) ((new Date().getTime() - myStartTime.getTime()));
                } while (myMilliSeconds < sleepTime);

                webCallQuantity++;
                ReqReplyProducer myReqReply = (ReqReplyProducer) applicationContext.getBean("bean_vmReqReplyProducer");
                String myResponse = null;

                try {
                    myResponse = myReqReply.getResponse("REQUEST", hotel.getSystemIdent(), 2000);
                } catch (InterruptedException ex) {
                    LOGGER.log(Level.ERROR, ex);
                }

                if (myResponse != null) {
                    if (!hotel.compareResponse(myResponse)) {
                        LOGGER.log(Level.ERROR, "Hotel: " + hotel.getSystemIdent() + " --> NO RESPONSE MATCH");
                        LOGGER.log(Level.ERROR, "expected: " + hotel.getResponse());
                        LOGGER.log(Level.ERROR, "received: " + myResponse);
                        webCallErrorCount++;
                    }
                } else {
                    LOGGER.log(Level.ERROR, "Hotel: " + hotel.getSystemIdent() + " --> RESPONSE NULL");
                    webCallErrorTimeoutCount++;
                    webCallErrorCount++;
                }

            }

        }

        
        /**
         *
         * @return
         */
        public int getWebCallQuantity() {
            return webCallQuantity;
        }

        /**
         *
         * @return
         */
        public int getWebCallErrorCount() {
            return webCallErrorCount;
        }

        /**
         *
         * @return
         */
        public int getWebCallErrorTimeoutCount() {
            return webCallErrorTimeoutCount;
        }

        /**
         *
         * @return
         */
        public ReqReplyTest2_Hotel getHotel() {
            return hotel;
        }

    }

    /**
     * Constructor of the test-class
     */
    public ReqReplyTest2() {

    }

    /**
     *
     */
    @PostConstruct
    public void InitTest() {
        int i;

        int myResponseLength = (int) randomNumber(20, RESPNSE_MAX_STRING_LENGTH);
        ReqReplyTest2_HotelList myHotelList = (ReqReplyTest2_HotelList) applicationContext.getBean("bean_HotelList");
        ReqReplyTest2_Hotel myHotel;

        // Initialize the Hotels and Threads
        for (i = 0; i < HOTEL_QUANTITY; i++) {

            myHotel = new ReqReplyTest2_Hotel(randomString(6), randomString(myResponseLength));
            myHotelList.add(myHotel);
            WebActionList.add(new WebAction(myHotel));
        }

    }

    /**
     *
     * @param ac
     * @throws BeansException
     */
    @Override
    public void setApplicationContext(ApplicationContext ac) throws BeansException {
        this.applicationContext = ac;
    }

    /**
     * the test itself
     */
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
            temp.stopSending();
        }

        LOGGER.log(Level.INFO, "Wait " + TEST_WAIT_TO_FINISH_SECONDS + "s to finish");
        startTime = new Date();
        do {
            seconds = (int) ((new Date().getTime() - startTime.getTime()) / 1000);
        } while (seconds < TEST_WAIT_TO_FINISH_SECONDS);

        for (WebAction temp : WebActionList) {
            if (temp.getWebCallQuantity() != temp.getHotel().getResponseCount() || temp.getWebCallErrorCount() > 0) {
                LOGGER.log(Level.INFO, "Hotel {"
                        + temp.getHotel().getSystemIdent()
                        + "} "
                        + " Calls:" + temp.getWebCallQuantity()
                        + " Responses: " + temp.getHotel().getResponseCount()
                        + " Errors: " + temp.getWebCallErrorCount()
                        + " Timeouts: " + temp.getWebCallErrorTimeoutCount());
                Assert.assertTrue("Error in transmission", temp.getWebCallErrorCount() == 0);
                Assert.assertTrue("Req and response counts different", temp.getWebCallQuantity() == temp.getHotel().getResponseCount());
            }
        }

        
        ReqReplyProducer myReqReply = (ReqReplyProducer) applicationContext.getBean("bean_vmReqReplyProducer");
        if (myReqReply.getStorageSize() != 0)   {
            LOGGER.log(Level.ERROR, "Still MsgObj in storage");
            Map myMap= myReqReply.getMessageStorage().getMsgMap();
            Iterator it= myMap.entrySet().iterator();
            while (it.hasNext())    {
                Map.Entry temp= (Map.Entry)it.next();
                LOGGER.log(Level.ERROR, temp.getValue().toString());
            }
        }
        
        Assert.assertTrue("Storage size should be 0", myReqReply.getStorageSize() == 0);
        
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
