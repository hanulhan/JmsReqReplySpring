/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hanulhan.jms.spring.websession.sim;

import hanulhan.jms.spring.reqreply.beans.ReqReplyProducer;
import hanulhan.jms.spring.util.RandomUtils;
import java.util.Date;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author uhansen
 */
public class ClientWebSession extends Thread {

    private long sleepTimeMsec;
    private int timeoutSec;
    private final String ident;
    private ReqReplyProducer reqReplyProducer;
    private boolean active = false;
    private boolean terminated = false;
    private static final Logger LOGGER = Logger.getLogger(ClientWebSession.class);

    private long requestQuantity = 0;
    private int errorQuantity = 0;
    private int timeoutQuantity= 0;
    private long requestGoodQuantity= 0;
    
    private long minResponseTime= 100000;
    private long maxResponseTime= 0;
    private long avgResponseTime= 0;

    private static final int SLEEP_TIME_MIN = 5000;
    private static final int SLEEP_TIME_MAX = 10000;

    /**
     *
     * @param aMessageProducer
     * @param aIdent
     */
    public ClientWebSession(ReqReplyProducer aMessageProducer, String aIdent) {
        this.ident = aIdent;
        this.reqReplyProducer = aMessageProducer;
        this.sleepTimeMsec = 2000;
    }

    public ClientWebSession(ReqReplyProducer aMessageProducer, String aIdent, int aTimeoutSec) {
        this.ident = aIdent;
        this.reqReplyProducer = aMessageProducer;
        this.timeoutSec = aTimeoutSec;
        this.sleepTimeMsec = RandomUtils.getRandomLong(SLEEP_TIME_MIN, SLEEP_TIME_MAX);

    }

    public ClientWebSession(String aIdent) {
        this.ident = aIdent;
    }

    public void terminate() {
        this.active = false;
    }

    public boolean isTerminated() {
        return this.terminated;
    }

    @Override
    @SuppressWarnings("SleepWhileInLoop")
    public void run() {
        String myResponse;
        String myRequest;
        this.active = true;
        long milliSeconds, now;
        try {
            Thread.sleep(RandomUtils.getRandomLong(1000, 45000));
            while (active) {
                myRequest= "REQ-" + ident + "-" + requestQuantity;
                LOGGER.log(Level.TRACE, "Send Request: " + myRequest + ", Ident: " + ident + ", Timeout: " + timeoutSec + "s");
                requestQuantity++;
                now= new Date().getTime();
                myResponse = reqReplyProducer.getResponse(myRequest, ident, (long) (1000 * timeoutSec));
                if (myResponse != null) {
                    milliSeconds= new Date().getTime() - now;
                    if (milliSeconds < minResponseTime) {
                        minResponseTime= milliSeconds;
                    }
                    if (milliSeconds > maxResponseTime) {
                        maxResponseTime= milliSeconds;
                    }
                    avgResponseTime= ((avgResponseTime * requestGoodQuantity) + milliSeconds) / (requestGoodQuantity + 1);
                    requestGoodQuantity++;
                    
                    LOGGER.log(Level.TRACE, "Response length: " + myResponse.length());
                    String[] temp = myResponse.split(",");
                    
                    if (temp[1] != null & temp[1].length() > 0)   {
                        String myFirstChar = temp[1].substring(0, 1);
                        int strLen2= temp[1].length();

                        String myLastChar = temp[1].substring((strLen2 - 2), strLen2 - 1);

                        if (Integer.parseInt(temp[0]) != strLen2) {
                            errorQuantity++;
                            LOGGER.log(Level.ERROR, "ident: + " + ident + " --> Message size ERROR");
                        } else if (!myFirstChar.equals(myLastChar)) {
                            errorQuantity++;
                            LOGGER.log(Level.ERROR, "ident: " + ident + " --> Message content ERROR");
                        } else {
                            LOGGER.log(Level.TRACE, "ident: " + ident + " --> Message validation OK");
                        }
                    } else {
                        errorQuantity++;
                        LOGGER.log(Level.ERROR, "ident: " + ident + " --> Message content ERROR");
                    }
                } else {
                    LOGGER.log(Level.ERROR, "Response TIMEOUT: " + myRequest);
                    errorQuantity++;
                    timeoutQuantity++;
                }
                
                Thread.sleep(sleepTimeMsec);
            }
        } catch (InterruptedException | NumberFormatException e) {
            LOGGER.log(Level.ERROR, e);
        } finally {
            terminated = true;
        }
    }

    public long getRequestQuantity() {
        return requestQuantity;
    }

    public int getErrorQuantity() {
        return errorQuantity;
    }

    public int getTimeoutQuantity() {
        return timeoutQuantity;
    }

    public long getAvgResponseTime() {
        return avgResponseTime;
    }
    
    
    

}
