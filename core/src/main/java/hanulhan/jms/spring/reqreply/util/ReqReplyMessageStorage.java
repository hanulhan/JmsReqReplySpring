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
import javax.jms.JMSException;
import javax.jms.Message;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author uhansen
 */
public class ReqReplyMessageStorage {

    private final Map<String, ReqReplyMessageObject> msgMap;
    private final Semaphore available = new Semaphore(1, true);
    static final Logger LOGGER = Logger.getLogger(ReqReplyMessageStorage.class);
    private String filterName;

    public ReqReplyMessageStorage(String aFilterName) {
        super();
        this.filterName = aFilterName;
        msgMap = Collections.synchronizedMap(new HashMap<String, ReqReplyMessageObject>());
    }

    public ReqReplyStatusCode add(Message aMessage) throws JMSException {
        String myMessageId;
        ReqReplyStatusCode myStatus = ReqReplyStatusCode.STATUS_ERROR;
        try {
            myMessageId = aMessage.getJMSCorrelationID();
            available.acquire();

            if (msgMap.containsKey(myMessageId)) {
                myStatus = msgMap.get(myMessageId).add(aMessage);
                String myBitMask= Integer.toHexString(msgMap.get(myMessageId).getMsgBitMask());
                LOGGER.log(Level.DEBUG, "Add part to message [" + myMessageId + "], msgMask: " + myBitMask);
            } else {
                myStatus = ReqReplyStatusCode.STATUS_CORRELATION_MISMATCH;
                LOGGER.log(Level.ERROR, "Message [" + myMessageId + "] does not in storage");                
            }
        } catch (InterruptedException interruptedException) {
            LOGGER.log(Level.ERROR, interruptedException);
        } finally {
            available.release();
        }
        return myStatus;
    }

    public ReqReplyStatusCode add(String aMessageId, String aFilterValue) throws JMSException {
        String myMessageId;
        ReqReplyStatusCode myStatus = ReqReplyStatusCode.STATUS_ERROR;
        try {
            available.acquire();

            if (msgMap.containsKey(aMessageId)) {
                myStatus = ReqReplyStatusCode.STATUS_CORRELATION_MISMATCH;
                LOGGER.log(Level.DEBUG, "Add new message [" + aMessageId + "] should not exist in storage");
            } else {
                msgMap.put(aMessageId, new ReqReplyMessageObject(aMessageId, this.filterName, aFilterValue));
                LOGGER.log(Level.DEBUG, "Add new message [" + aMessageId + "] to storage");
            }
        } catch (InterruptedException interruptedException) {
            LOGGER.log(Level.ERROR, interruptedException);
        } finally {
            available.release();
        }
        return myStatus;
    }

    public String getResponse(String myMessageId) {
        String myReturn = null;
        try {
            available.acquire();
            if (msgMap.containsKey(myMessageId)) {
                myReturn = "";
            }

        } catch (InterruptedException interruptedException) {
            LOGGER.log(Level.ERROR, interruptedException);
        } finally {
            available.release();
        }

        return myReturn;
    }

    public boolean isResponseReceived(String myMessageId) {
        boolean myReturn = false;
        try {
            available.acquire();
            myReturn = true;

        } catch (InterruptedException interruptedException) {
            LOGGER.log(Level.ERROR, interruptedException);
        } finally {
            available.release();
        }

        return myReturn;
    }

}
