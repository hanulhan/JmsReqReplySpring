/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hanulhan.jms.spring.reqreply.util;

import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * A synchronized map to store the systemIdents as soon as the system is
 * connected.
 *
 * @author uhansen
 */
public class ReqReplyFilterMap {

    private final Map<String, RequestObject> filterMap;
    private final Semaphore available = new Semaphore(1, true);
    static final Logger LOGGER = Logger.getLogger(ReqReplyFilterMap.class);

    /**
     *
     */
    public ReqReplyFilterMap() {
        filterMap = Collections.synchronizedMap(new HashMap<String, RequestObject>());
    }

    /**
     *
     * @param aIdent
     * @param aObj
     */
    public synchronized void put(String aIdent, RequestObject aObj) {
        try {
            available.acquire();
            LOGGER.log(Level.TRACE, "Put ident to map");
            this.filterMap.put(aIdent, aObj);
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
            this.filterMap.remove(aIdent);
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
    public synchronized boolean IsFilterInMap(String aIdent) {
        return this.filterMap.containsKey(aIdent);
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
    public boolean addRequest(String aIdent, String aConsumerId, String aRequest, String aMessageId, long aTimeout) {

        boolean retValue = false;
        GregorianCalendar startDateTime = new GregorianCalendar();
        //GregorianCalendar gregory = new GregorianCalendar();
        XMLGregorianCalendar xmlGregory;
        long myMilliSeconds;
        long myAquireTimeMs = aTimeout / 10;
        do {
            try {
                startDateTime.setTime(new Date());
                if (available.tryAcquire(myAquireTimeMs, TimeUnit.MILLISECONDS)) {
                    LOGGER.log(Level.TRACE, "Consumer: " + aConsumerId + " aquire identMap for request: " + aRequest);
                    if (filterMap.containsKey(aIdent) && !filterMap.get(aIdent).isBusy()) {
                        RequestObject myObj = filterMap.get(aIdent);

                        synchronized (myObj) {
                            xmlGregory= DateConverter.createXmlGregorianCalendar(startDateTime);
                            myObj.setNewRequest(aRequest, aMessageId, aConsumerId, xmlGregory);
                            LOGGER.log(Level.TRACE, "Consumer: " + aConsumerId + " NOTIFY Object for request: " + aRequest);
                            retValue = true;
                            filterMap.remove(aIdent);
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
                myMilliSeconds = DateConverter.elapsedMilliSeconds(startDateTime);
            }

        } while (retValue == false && myMilliSeconds < aTimeout);

        if (myMilliSeconds >= aTimeout) {
            LOGGER.log(Level.ERROR, "TIMEOUT (" + myMilliSeconds + ") Consumer: " + aConsumerId + " adding Request ");
        }
        return retValue;
    }

    /**
     *
     * @return
     */
    public int size() {
        return filterMap.size();
    }
}
