/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hanulhan.jms.spring.websession.sim;

import hanulhan.jms.spring.reqreply.beans.ReqReplyProducer;
import hanulhan.jms.spring.util.RandomUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author uhansen
 */
public class ClientWebSession extends Thread {

    private long sleepTimeMsec;
    private int timeoutSec;
    private final String myRequest = "REQUEST-1";
    private final String ident;
    private ReqReplyProducer reqReplyProducer;
    private boolean active = false;
    private boolean terminated = false;
    private static final Logger LOGGER = Logger.getLogger(ClientWebSession.class);

    private int requestQuantity = 0;
    private int errorQuantity = 0;
    private int timeoutQuantity= 0;

    private static final int SLEEP_TIME_MIN = 45000;
    private static final int SLEEP_TIME_MAX = 60000;

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
        this.active = true;
        try {
            Thread.sleep(RandomUtils.getRandomLong(1000, 45000));
            while (active) {
                LOGGER.log(Level.TRACE, "Send Request: " + myRequest + ", Ident: " + ident + ", Timeout: " + timeoutSec + "s");
                requestQuantity++;
                myResponse = reqReplyProducer.getResponse(myRequest, ident, (long) (1000 * timeoutSec));
                if (myResponse != null) {
                    LOGGER.log(Level.TRACE, "Response length: " + myResponse.length());
                    String[] temp = myResponse.split(",");
                    String myFirstChar = temp[1].substring(0, 1);
                    String myLastChar = temp[1].substring(temp[1].length() - 2, temp[1].length() - 1);
                    if (Integer.parseInt(temp[0]) != temp[1].length()) {
                        errorQuantity++;
                        LOGGER.log(Level.ERROR, "ident: + " + ident + " --> Message size ERROR");
                    } else if (!myFirstChar.equals(myLastChar)) {
                        errorQuantity++;
                        LOGGER.log(Level.ERROR, "ident: " + ident + " --> Message content ERROR");
                    } else {
                        LOGGER.log(Level.TRACE, "ident: " + ident + " --> Message validation OK");
                    }
                } else {
                    LOGGER.log(Level.ERROR, "ident: " + ident + " --> Response TIMEOUT");
                    errorQuantity++;
                    timeoutQuantity++;
                }
                
                Thread.sleep(sleepTimeMsec);
            }
        } catch (InterruptedException e) {
            LOGGER.log(Level.ERROR, e);
        } finally {
            terminated = true;
        }
    }

    public int getRequestQuantity() {
        return requestQuantity;
    }

    public int getErrorQuantity() {
        return errorQuantity;
    }

    public int getTimeoutQuantity() {
        return timeoutQuantity;
    }
    
    

}
