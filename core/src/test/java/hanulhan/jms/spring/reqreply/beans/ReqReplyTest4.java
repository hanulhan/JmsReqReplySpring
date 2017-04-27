/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hanulhan.jms.spring.reqreply.beans;

import static hanulhan.jms.spring.reqreply.beans.ReqReplyTest2.AB;
import static hanulhan.jms.spring.reqreply.beans.ReqReplyTest3.LOGGER;
import hanulhan.jms.spring.reqreply.util.RequestObject;
import java.security.SecureRandom;
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
 *
 * @author uhansen
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/spring/springTest-4.xml"})
public class ReqReplyTest4 implements ApplicationContextAware {

    ApplicationContext applicationContext;
    static final Logger LOGGER = Logger.getLogger(ReqReplyTest4.class);
    ReqReplyProducer producer;

    static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    static SecureRandom rnd = new SecureRandom();

    private final long WAIT_SECONDS = 50;
    private final String SYSTEM_IDENT = "ABCDE";
    private final int REQUEST_QUANTITY = 20;
    private final int SYSTEM_QUANTITY = 1;
    private final long MAX_CONSUMER_SLEEP_TIME = 500;
    private final long MIN_CONSUMER_SLEEP_TIME = 100;
    private final long SYSTEM_RECONNECT_TIME = 100;
    private final long REQUEST_TIMEOUT_MS = 2500;
    private final long AQUIRE_TIME_MS = 250;

    @Test
    public void MyTest() {

        System mySystem;
        AcsWebSession mySession;

        RequestResponse[] myReqResponse = new RequestResponse[SYSTEM_QUANTITY];

        for (int i = 0; i < SYSTEM_QUANTITY; i++) {
            myReqResponse[i] = new RequestResponse(SYSTEM_IDENT);
            for (int j = 0; j < REQUEST_QUANTITY; j++) {
                int rndLength= (int)randomNumber(1, 200);
                myReqResponse[i].set(randomString(6), randomString(rndLength));
            }
        }

        mySession = new AcsWebSession(SYSTEM_IDENT, 10, myReqResponse[0]);
        mySystem = new System(SYSTEM_IDENT, myReqResponse[0]);

        producer = (ReqReplyProducer) applicationContext.getBean("bean_vmReqReplyProducer");
        mySystem.start();
        mySession.start();

        do {
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                LOGGER.log(Level.ERROR, ex);
            }
        } while (mySession.isBusy());

        mySystem.cancel();
        
        Assert.assertTrue("ERROR ocurs", mySession.getError() == true);
    }

    public class System extends Thread {

        private final String ident;
        private ReqReplyConsumer myConsumer;
        private RequestResponse requestResponse;

        @PreDestroy
        public void destroyIt() {
            this.interrupt();
        }

        public System(String ident, RequestResponse aRequestResponse) {
            super();
            this.ident = ident;
            this.requestResponse= aRequestResponse;
        }

        public void cancel() {
            interrupt();
        }

        @Override
        @SuppressWarnings("SleepWhileInLoop")
        public void run() {
            Date startTime;
            RequestObject myRequestObj;
            String myResponse;

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    myConsumer = (ReqReplyConsumer) applicationContext.getBean("bean_jmsReqReplyConsumer1");
                    myRequestObj = new RequestObject(this.ident);
                    if (myConsumer.ConnectSystem(myRequestObj)) {
                        synchronized (myRequestObj) {
                            LOGGER.log(Level.TRACE, "System waits for notify");
                            myRequestObj.wait(2000);
                            if (myRequestObj.isBusy()) {
                                LOGGER.log(Level.DEBUG, "System [" + ident + "] disconnect with Response: " + myRequestObj.toString());
                                myResponse= requestResponse.getReply(myRequestObj.getRequest());
                                myConsumer.sendResponse(ident, myResponse, myRequestObj.getMessageId());
                            } else {
                                LOGGER.log(Level.DEBUG, "System [" + ident + "] disconnect ");
                            }
                        }
                        myConsumer.DisconnectSystem(ident);
                    }
                    Thread.sleep(SYSTEM_RECONNECT_TIME);

                } catch (InterruptedException ex) {
                    LOGGER.log(Level.ERROR, ex);
                }
            }
        }

    }

    private class AcsWebSession extends Thread {

        private String ident;
        private boolean busy;
        private int quantity;
        private RequestResponse requestResponse;
        private int errorCounter= 0;

        @PreDestroy
        public void destroyIt() {
            this.interrupt();
        }

        public AcsWebSession(String aIdent, int aQuantity, RequestResponse aRequestResponse) {
            super();
            this.ident = aIdent;
            this.busy = false;
            this.quantity = aQuantity;
            this.requestResponse= aRequestResponse;

        }

        public void cancel() {
            interrupt();
        }

        @Override
        @SuppressWarnings("SleepWhileInLoop")
        public void run() {
//            Date startTime;
            String myResponse;
            this.busy = true;
            int myCount = 0;
            int rndIndex;
            String myRequest;
            while (myCount < quantity && !Thread.currentThread().isInterrupted()) {
                try {

                    rndIndex = (int)randomNumber(0, REQUEST_QUANTITY - 1);
                    myRequest= requestResponse.getRequest(rndIndex);
                    
                    myResponse = producer.getResponse(myRequest, SYSTEM_IDENT, 2000);
                    if (myResponse != null) {
                        LOGGER.log(Level.DEBUG, "Response received: " + myResponse);
                        if (!myResponse.equals(requestResponse.getResponse(rndIndex)))  {
                            errorCounter+= 1;
                        }
                    }
                    Thread.sleep(500);
                    myCount += 1;
                } catch (InterruptedException ex) {
                    LOGGER.log(Level.ERROR, ex);
                    this.busy = false;
                }
            }
            this.busy = false;
        }

        public boolean isBusy() {
            return busy;
        }

        private boolean getError()  {
            return errorCounter == 0;
        }
    }

    private class RequestResponse {

        private final String ident;
        private final String[] request = new String[REQUEST_QUANTITY];
        private final String[] response = new String[REQUEST_QUANTITY];
        private int currentIndex;

        public RequestResponse(String ident) {
            this.ident = ident;
            this.currentIndex = 0;
        }

        public void set(String aRequest, String aReply) {
            if (currentIndex < REQUEST_QUANTITY) {
                this.request[currentIndex] = aRequest;
                this.response[currentIndex] = aReply;
                currentIndex += 1;
            }

        }

        public String getReply(String aRequest) {

            for (int i=0; i < REQUEST_QUANTITY; i++)    {
                if (this.request[i].equals(aRequest))  {
                    return this.response[i];
                }
            }

            return null;
        }
        
        public String getRequest(int aIndex)  {
            return this.request[aIndex];
        }
        
        public String getResponse(int aIndex)   {
            return response[aIndex];
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

    @Override
    public void setApplicationContext(ApplicationContext ac) throws BeansException {
        this.applicationContext = ac;
    }
}
