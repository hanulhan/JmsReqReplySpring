/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hanulhan.jms.spring.reqreply.util;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author uhansen
 */
public class ReqReplyTest3_IdentMap {

    private final Map<String, ReqReplyTest3_Object> identMap;
    private final Semaphore available = new Semaphore(1, true);
    static final Logger LOGGER = Logger.getLogger(ReqReplyTest3_IdentMap.class);

    /**
     *
     */
    public ReqReplyTest3_IdentMap() {
        identMap = Collections.synchronizedMap(new HashMap<String, ReqReplyTest3_Object>());
    }

    /**
     *
     * @param aIdent
     * @param aObj
     */
    public synchronized void put(String aIdent, ReqReplyTest3_Object aObj) {
        try {
            available.acquire();
            LOGGER.log(Level.TRACE, "Put ident to map");
            this.identMap.put(aIdent, aObj);
        } catch (InterruptedException interruptedException) {
            LOGGER.log(Level.ERROR, interruptedException);
        } finally {
            available.release();
        }
    }

    /**
     *
     * @param aIdent
     */
    public synchronized void delete(String aIdent) {
        try {
            available.acquire();
            LOGGER.log(Level.TRACE, "delete ident from map");
            this.identMap.remove(aIdent);
        } catch (InterruptedException ex) {
            LOGGER.log(Level.ERROR, ex);
        } finally {
            available.release();
        }
    }

    /**
     *
     * @param aIdent
     * @return
     */
    public synchronized boolean IsIdentInMap(String aIdent) {
        return this.identMap.containsKey(aIdent);
    }

    /**
     *
     * @param aIdent
     * @param aConsumerId
     * @param aRequest
     * @param aMessageId
     * @param aTimeout
     * @return
     */
    @SuppressWarnings("SleepWhileInLoop")
    public boolean addRequest(String aIdent, int aConsumerId, String aRequest, String aMessageId, long aTimeout) {

        boolean retValue = false;
        Date startTime = new Date();
        long myMilliSeconds;
        long myAquireTimeMs= aTimeout / 10;
        do {
            try {
                if (available.tryAcquire(myAquireTimeMs, TimeUnit.MILLISECONDS)) {
                    LOGGER.log(Level.TRACE, "Consumer: " + aConsumerId + " aquire identMap");
                    if (identMap.containsKey(aIdent) && !identMap.get(aIdent).isInProgress()) {
                        ReqReplyTest3_Object myObj = identMap.get(aIdent);
                        
                        synchronized (myObj) {
                            myObj.setNewRequest(aRequest, aMessageId, aConsumerId, startTime);
                            LOGGER.log(Level.TRACE, "Consumer: " + aConsumerId + " NOTIFY Object");
                            retValue = true;
                            identMap.remove(aIdent);
                            myObj.notify();
                        }
                    }
                } else {
                    Thread.sleep(myAquireTimeMs);
                }
            } catch (InterruptedException ex) {
                LOGGER.log(Level.ERROR, ex);
            } finally {
                available.release();
                LOGGER.log(Level.TRACE, "Consumer: " + aConsumerId + " release identMap");
            }
            myMilliSeconds = (new Date().getTime() - startTime.getTime());
        } while (retValue == false && myMilliSeconds < aTimeout);

        if (myMilliSeconds >= aTimeout)    {
            LOGGER.log(Level.ERROR, "TIMEOUT (" + myMilliSeconds + ") Consumer: " + aConsumerId + " adding Request ");
        }
        return retValue;
    }

//    public synchronized boolean ready(String aIdent) {
//        boolean retValue = false;
//        try {
//            available.acquire();
//            retValue = identMap.containsKey(aIdent) && !identMap.get(aIdent).isInProgress();
//        } catch (InterruptedException ex) {
//            LOGGER.log(Level.ERROR, ex);
//        } finally {
//            available.release();
//        }
//        return retValue;
//    }
//    public boolean addRequest(String aIdent, int aConsumerId, String aRequest, String aMessageId, long aTimeout) {
//
//        boolean retValue = false;
//        Date startTime= new Date();
//        try {
//                available.acquire();
//                LOGGER.log(Level.DEBUG, "Consumer: " + aConsumerId + " AQUIRE map in " + (new Date().getTime() - startTime.getTime()) + "ms");
//                if (identMap.containsKey(aIdent) && !identMap.get(aIdent).isInProgress()) {
//                    ReqReplyTest3_Object myObj = identMap.get(aIdent);
//                    synchronized (myObj) {
//                        myObj.setNewRequest(aRequest, aMessageId, aConsumerId, startTime);
//                        LOGGER.log(Level.TRACE, "Consumer: " + aConsumerId + " NOTIFY Object");
//                        myObj.notify();
//                        retValue = true;
//                        identMap.remove(aIdent);
//                    }
//                } else {
//                    LOGGER.log(Level.ERROR, "ident not in map or busy");
//                }
//        } catch (InterruptedException interruptedException) {
//            LOGGER.log(Level.ERROR, interruptedException);
//        } finally {
//            available.release();
//        }
//        return retValue;
//    }

    /**
     *
     * @return
     */
    public int size() {
        return identMap.size();
    }
}
