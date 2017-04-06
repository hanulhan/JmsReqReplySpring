/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hanulhan.jms.spring.reqreply.beans;

import hanulhan.jms.spring.reqreply.util.ReqReplyTest3_IdentMap;
import hanulhan.jms.spring.reqreply.util.ReqReplyTest3_Object;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
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
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations = {"/spring/springTest-3.xml"})
//public class ReqReplyTest3 implements ApplicationContextAware {
public class ReqReplyTest3 {

//    ApplicationContext applicationContext;
    static final Logger LOGGER = Logger.getLogger(ReqReplyTest3.class);
    
    private final long WAIT_SECONDS = 50;
    private final String SYSTEM_IDENT = "ABCDE";
    private final int REQUEST_QUANTITY = 20;
    
    ReqReplyTest3_IdentMap identMap = new ReqReplyTest3_IdentMap();

//    @Override
//    public void setApplicationContext(ApplicationContext ac) throws BeansException {
//        this.applicationContext = ac;
//    }
    @Test
    public void MyTest() {
        
        Consumer myConsumer1 = new Consumer(SYSTEM_IDENT, 1, REQUEST_QUANTITY, identMap);
        Consumer myConsumer2 = new Consumer(SYSTEM_IDENT, 2, REQUEST_QUANTITY, identMap);
        System mySystem = new System(SYSTEM_IDENT, identMap);
        Date startTime;
        long mySeconds;
        
        mySystem.start();
        myConsumer1.start();
        myConsumer2.start();
//        LOGGER.log(Level.DEBUG, "Test running for " + WAIT_SECONDS + " s");
//        startTime = new Date();
//        do {
//            mySeconds = (int) ((new Date().getTime() - startTime.getTime()) / 1000);
//        } while (mySeconds < WAIT_SECONDS);

        while (myConsumer1.isAlive() && myConsumer2.isAlive()) {}
        LOGGER.log(Level.DEBUG, "Wait additinal 5 s");
        startTime = new Date();
        do {
            mySeconds = (int) ((new Date().getTime() - startTime.getTime()) / 1000);
        } while (mySeconds < 5);        
        mySystem.cancel();
        Assert.assertTrue("Some Requests not processed, system received: " + mySystem.getRequestReceiveCount(), mySystem.getRequestReceiveCount() == 2 * REQUEST_QUANTITY);
    }
    
    public class System extends Thread {
        
        private final String ident;
        private boolean active = true;
        ReqReplyTest3_IdentMap identMap;
        private int requestReceiveCount = 0;
        
        @PreDestroy
        public void destroyIt() {
            this.interrupt();
        }
        
        public System(String ident, ReqReplyTest3_IdentMap aIdentMap) {
            this.ident = ident;
            this.identMap = aIdentMap;
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
            while (!Thread.currentThread().isInterrupted()) {
//            while (this.active) {
                LOGGER.log(Level.DEBUG, "System [" + ident + "] connected");
                
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
                            
                            if (myObj.isInProgress()) {
                                requestReceiveCount++;
                                LOGGER.log(Level.DEBUG, "System [" + ident + "] disconnect with Response: " + myObj.toString());
                            } else {
                                LOGGER.log(Level.DEBUG, "System [" + ident + "] disconnect");
                            }
                            if (identMap.IsIdentInMap(ident)) {
                                identMap.remove(ident);
                            }
                        }
                        Thread.sleep(200);
                    } catch (InterruptedException ex) {
                        LOGGER.log(Level.DEBUG, "System [" + ident + "] disconnect");
                    }
                }
            }
        }
        
        public int getRequestReceiveCount() {
            return requestReceiveCount;
        }
        
    }
    
    public class Consumer extends Thread {
        
        private final String ident;
        private boolean active;
        private final int consumerId;
        private int requestQuantity;
        
        ReqReplyTest3_IdentMap identMap;
        
        public Consumer(String ident, int aId, int aReqQuantity, ReqReplyTest3_IdentMap aIdentMap) {
            this.active = true;
            this.ident = ident;
            this.consumerId = aId;
            this.requestQuantity = aReqQuantity;
            this.identMap = aIdentMap;
        }
        
        private synchronized void stopMe() {
            this.active = false;
        }
        
        @Override
        @SuppressWarnings("SleepWhileInLoop")
        public void run() {
            int i = 0;
            while (i < requestQuantity) {
                try {
                    //while (!identMap.ready(ident)) {}
//                LOGGER.log(Level.TRACE, "Consumer: " + consumerId +" try to add Request to map");
                    if (identMap.addRequest(ident, consumerId, "HALLO", "MESSAGE-" + (i + 1), 1000))  {
                        i++;
                    }
                    Thread.sleep(200);
                } catch (InterruptedException ex) {
                    LOGGER.log(Level.ERROR, ex);
                }
            }
            this.active = false;
        }
        
        public boolean isActive() {
            return active;
        }
        
    }
}
