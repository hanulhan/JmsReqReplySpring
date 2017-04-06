/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hanulhan.jms.spring.reqreply.util;

import java.util.Collections;
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

    public ReqReplyTest3_IdentMap() {
        identMap = Collections.synchronizedMap(new HashMap<String, ReqReplyTest3_Object>());
    }

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

    public synchronized void remove(String aIdent) {
        try {
            available.acquire();
            LOGGER.log(Level.TRACE, "remove ident from map");
            this.identMap.remove(aIdent);
            available.release();
            LOGGER.log(Level.TRACE, "RELEASE map");
        } catch (InterruptedException ex) {
            LOGGER.log(Level.ERROR, ex);
        } finally {
            available.release();
        }
    }

    public synchronized boolean IsIdentInMap(String aIdent) {
        return this.identMap.containsKey(aIdent);
    }

    public synchronized boolean ready(String aIdent) {
        boolean retValue = false;
        try {
            available.acquire();
            retValue = identMap.containsKey(aIdent) && !identMap.get(aIdent).isInProgress();
        } catch (InterruptedException ex) {
            LOGGER.log(Level.ERROR, ex);
        } finally {
            available.release();
        }
        return retValue;
    }

    public boolean addRequest(String aIdent, int aConsumerId, String aRequest, String aMessageId, long aTimeout) {

        boolean retValue = false;
        try {
            if (available.tryAcquire(aTimeout, TimeUnit.MILLISECONDS)) {
                LOGGER.log(Level.TRACE, "Consumer: " + aConsumerId + " AQUIRE map");
                if (identMap.containsKey(aIdent) && !identMap.get(aIdent).isInProgress()) {
//                if (identMap.containsKey(aIdent)) {
                    ReqReplyTest3_Object myObj = identMap.get(aIdent);
                    synchronized (myObj) {
                        myObj.setNewRequest(aRequest, aMessageId, aConsumerId);
                        LOGGER.log(Level.TRACE, "Consumer: " + aConsumerId + " NOTIFY Object");
                        myObj.notify();
                        retValue = true;
                        //identMap.remove(aIdent);
                    }
                } else {
                    LOGGER.log(Level.TRACE, "ident not in map or busy");
                }
            } else {
                LOGGER.log(Level.TRACE, "Consumer: " + aConsumerId + "could not aquire map");
            }
        } catch (InterruptedException interruptedException) {
            LOGGER.log(Level.ERROR, interruptedException);
        } finally {
            available.release();
        }
        return retValue;
    }
}
