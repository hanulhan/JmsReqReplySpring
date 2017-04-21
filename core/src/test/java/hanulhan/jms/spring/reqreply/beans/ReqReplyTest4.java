/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hanulhan.jms.spring.reqreply.beans;

import hanulhan.jms.spring.reqreply.util.ReqReplyTest3_IdentMap;
import hanulhan.jms.spring.reqreply.util.ReqReplyTest3_Object;
import java.util.Date;
import javax.annotation.PreDestroy;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test for ReqReplyConsumer and the System.
 * When the system connects, its ident is put to the identMap.
 * The system is waiting for an object, which will be notified as soon as 
 * a Request from the Consumer is available.
 * 
 * This test does not use any core-classes. It just show the way how to protect
 * the ident map for simultaneous access and how sleep and notify is working in 
 * principle.
 * 
 * @author uhansen
 */

public class ReqReplyTest4 {

//    ApplicationContext applicationContext;
    static final Logger LOGGER = Logger.getLogger(ReqReplyTest4.class);

    private final long WAIT_SECONDS = 50;
    private final String SYSTEM_IDENT = "ABCDE";
    private final int REQUEST_QUANTITY = 20;
    private final int CLIENT_QUANTITY = 2;
    private final long MAX_CONSUMER_SLEEP_TIME = 500;
    private final long MIN_CONSUMER_SLEEP_TIME = 100;
    private final long SYSTEM_RECONNECT_TIME = 100;
    private final long REQUEST_TIMEOUT_MS = 2500;
    private final long AQUIRE_TIME_MS = 250;

    ReqReplyTest3_IdentMap identMap = new ReqReplyTest3_IdentMap();

//    @Override
//    public void setApplicationContext(ApplicationContext ac) throws BeansException {
//        this.applicationContext = ac;
//    }
    @Test
    public void MyTest() {
        Consumer[] myConsumer = new Consumer[CLIENT_QUANTITY];
        int i;
        long maxRequestSendTime = 0;
        long minRequestSendTime = Long.MAX_VALUE;
        long avgRequestSendTime = 0;
        long maxSystemHoldTime = 0;
        long minSystemHoldTime = Long.MAX_VALUE;
        long avgSystemHoldTime = 0;

        for (i = 0; i < CLIENT_QUANTITY; i++) {
            myConsumer[i] = new Consumer(SYSTEM_IDENT, (i + 1), REQUEST_QUANTITY, identMap);
        }
        System mySystem = new System(SYSTEM_IDENT, identMap);
        Date startTime;
        long mySeconds;

        mySystem.start();
        for (i = 0; i < CLIENT_QUANTITY; i++) {
            myConsumer[i].start();
        }
//        LOGGER.log(Level.DEBUG, "Test running for " + WAIT_SECONDS + " s");
//        startTime = new Date();
//        do {
//            mySeconds = (int) ((new Date().getTime() - startTime.getTime()) / 1000);
//        } while (mySeconds < WAIT_SECONDS);

        boolean isAlive;
        do {
            isAlive = false;
            for (i = 0; i < CLIENT_QUANTITY; i++) {
                if (myConsumer[i].isActive()) {
                    isAlive = true;
                }
            }
        } while (isAlive);
        mySystem.stopMe();

        LOGGER.log(Level.DEBUG, "Wait additinal 5 s");
        startTime = new Date();
        do {
            mySeconds = (int) ((new Date().getTime() - startTime.getTime()) / 1000);
        } while (mySeconds < 5);

        for (i = 0; i < CLIENT_QUANTITY; i++) {
            LOGGER.log(Level.DEBUG, "Consumer " + i + " Timeouts: " + myConsumer[i].getRequestTimeoutQuantity());
        }
        long l;
        long [] x= mySystem.getRequestSendTimeStat();
        for (i=0; i < REQUEST_QUANTITY; i++) {
            l= x[i];
            avgRequestSendTime += l;
            if (minRequestSendTime > l) {
                minRequestSendTime = l;
            }
            if (maxRequestSendTime < l) {
                maxRequestSendTime = l;
            }
        }
        avgRequestSendTime = avgRequestSendTime / (long)(REQUEST_QUANTITY * CLIENT_QUANTITY);

        x= mySystem.getSystemHoldTimeStat();
        for (i=0; i < REQUEST_QUANTITY; i++) {
            l= x[i];
            avgSystemHoldTime += l;
            if (minSystemHoldTime > l) {
                minSystemHoldTime = l;
            }
            if (maxSystemHoldTime < l) {
                maxSystemHoldTime = l;
            }
        }
        avgSystemHoldTime = avgSystemHoldTime / (long)mySystem.getSystemConnectCounter();

        LOGGER.log(Level.DEBUG, "###### REQUEST SEND TIME STATISTIC #######");
        LOGGER.log(Level.DEBUG, "avg: " + avgRequestSendTime + " ms");
        LOGGER.log(Level.DEBUG, "max: " + maxRequestSendTime + " ms");
        LOGGER.log(Level.DEBUG, "min: " + minRequestSendTime + " ms");
        
        LOGGER.log(Level.DEBUG, "###### SYSTEM HOLD TIME STATISTIC #######");
        LOGGER.log(Level.DEBUG, "avg: " + avgSystemHoldTime + " ms");
        LOGGER.log(Level.DEBUG, "max: " + maxSystemHoldTime + " ms");
        LOGGER.log(Level.DEBUG, "min: " + minSystemHoldTime + " ms");

        Assert.assertTrue("Some Requests not processed, system received: " + mySystem.getRequestReceiveCount(), mySystem.getRequestReceiveCount() == CLIENT_QUANTITY * REQUEST_QUANTITY);
        Assert.assertTrue("No System should be connected", identMap.size() == 0);        
    }

    

    public class System extends Thread {

        private final String ident;
        private boolean active = true;
        ReqReplyTest3_IdentMap identMap;
        private int requestReceiveCount = 0;
        private int systemConnectCounter= 0;
        private final long[] requestSendTimeStat;
        private final long[] systemHoldTimeStat;

        @PreDestroy
        public void destroyIt() {
            this.interrupt();
        }

        public System(String ident, ReqReplyTest3_IdentMap aIdentMap) {
            this.ident = ident;
            this.identMap = aIdentMap;
            requestSendTimeStat = new long[CLIENT_QUANTITY * REQUEST_QUANTITY];
            systemHoldTimeStat = new long[2 * CLIENT_QUANTITY * REQUEST_QUANTITY];
        }

        private synchronized void stopMe() {
            this.active = false;
        }

        public void cancel() {
            interrupt();
        }

        @Override
        @SuppressWarnings("SleepWhileInLoop")
        public void run() {
            Date startTime;
            long myMilliSeconds;
            long now;

            while (active) {
                LOGGER.log(Level.DEBUG, "System [" + ident + "] connected");
                systemConnectCounter++;
                startTime = new Date();

                if (identMap.IsIdentInMap(ident)) {
                    LOGGER.log(Level.ERROR, "ERROR: Ident already in map");
                } else {
                    try {
                        ReqReplyTest3_Object myObj = new ReqReplyTest3_Object(ident);
                        synchronized (myObj) {
                            LOGGER.log(Level.TRACE, "System enter synchronized block");

                            identMap.put(ident, myObj);
                            LOGGER.log(Level.TRACE, "System waits for notify");
                            myObj.wait(2000);
                            systemHoldTimeStat[systemConnectCounter-1] = (new Date().getTime() - startTime.getTime());
                            if (myObj.isInProgress()) {
                                now= new Date().getTime();
                                long z= myObj.getStartTime().getTime();
                                myMilliSeconds= now - z;
                                requestSendTimeStat[requestReceiveCount] = myMilliSeconds;
                                requestReceiveCount++;

                                LOGGER.log(Level.DEBUG, "System [" + ident + "] disconnect with Response: " + myObj.toString());
                            } else {
                                LOGGER.log(Level.DEBUG, "System [" + ident + "] disconnect ");
                            }
                            if (identMap.IsIdentInMap(ident)) {
                                identMap.delete(ident);
                            }
                        }
                        Thread.sleep(SYSTEM_RECONNECT_TIME);
                    } catch (InterruptedException ex) {
                        LOGGER.log(Level.DEBUG, "System [" + ident + "] disconnect");
                    }
                }
            }
        }

        public int getRequestReceiveCount() {
            return requestReceiveCount;
        }

        public long[] getRequestSendTimeStat() {
            return requestSendTimeStat;
        }

        public long[] getSystemHoldTimeStat() {
            return systemHoldTimeStat;
        }

        public int getSystemConnectCounter() {
            return systemConnectCounter;
        }
        
        

    }

    public class WebSession extends Thread {


        @Override
        @SuppressWarnings("SleepWhileInLoop")
        public void run() {
        }


    }

    private long randomNumber(long aMinValue, long aMaxValue) {
        return aMinValue + (long) (Math.random() * (aMaxValue - aMinValue));
    }
}
