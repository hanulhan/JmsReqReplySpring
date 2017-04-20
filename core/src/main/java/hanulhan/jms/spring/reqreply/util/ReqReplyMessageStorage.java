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

    /**
     *
     * @param aFilterName
     */
    public ReqReplyMessageStorage(String aFilterName) {
        super();
        this.filterName = aFilterName;
        msgMap = Collections.synchronizedMap(new HashMap<String, ReqReplyMessageObject>());
    }

//    public synchronized ReqReplyStatusCode add(Message aMessage) throws JMSException {
//        String myMessageId;
//        ReqReplyStatusCode myStatus = ReqReplyStatusCode.STATUS_ERROR;
//        ReqReplyMessageObject myMsgObj;
//        try {
//            myMessageId = aMessage.getJMSCorrelationID();
//            available.acquire();
//
//            if (msgMap.containsKey(myMessageId)) {
//
//                // Get the MsgObj from the map
//                myMsgObj = msgMap.get(myMessageId);
//
//                // Add the new Message to the MessageObject
//                myStatus = myMsgObj.add(aMessage);
//
//                if (myStatus == ReqReplyStatusCode.STATUS_OK) {
//                    // put it back to the map
//                    msgMap.put(myMessageId, myMsgObj);
//
//                    String myFilterValue= myMsgObj.getFilterValue();
//                    if (aMessage.getStringProperty(ReqReplySettings.PROPERTY_NAME_MSG_TYPE).equals(ReqReplySettings.PROPERTY_VALUE_MSG_TYPE_PAYLOAD)) {
//                        String myBitMask = Integer.toHexString(msgMap.get(myMessageId).getMsgBitMask());
//                        int count = aMessage.getIntProperty(ReqReplySettings.PROPERTY_NAME_COUNT);
//                        int totalCount = aMessage.getIntProperty(ReqReplySettings.PROPERTY_NAME_TOTAL_COUNT);
//                        LOGGER.log(Level.DEBUG, "Add part " + count + "/" + totalCount + " to message [" + myMessageId + "], Filter: " + myFilterValue);
//                    } else {
//                        LOGGER.log(Level.DEBUG, "Add ACK to message [" + myMessageId + "] for Filter: " + myFilterValue);
//                    }
//                } else {
//                    LOGGER.log(Level.DEBUG, "ERROR adding Message to map");
//                }
//            } else {
//                myStatus = ReqReplyStatusCode.STATUS_CORRELATION_MISMATCH;
//                LOGGER.log(Level.ERROR, "Message [" + myMessageId + "] is not in storage");
//            }
//        } catch (InterruptedException interruptedException) {
//            LOGGER.log(Level.ERROR, interruptedException);
//        } finally {
//            available.release();
//        }
//        return myStatus;
//    }

    /**
     *
     * @param aMessage
     * @return
     * @throws JMSException
     */
    public synchronized ReqReplyStatusCode add(Message aMessage) throws JMSException {
        String myMessageId;
        ReqReplyStatusCode myStatus = ReqReplyStatusCode.STATUS_ERROR;
        ReqReplyMessageObject myMsgObj;
        try {
            myMessageId = aMessage.getJMSCorrelationID();
            available.acquire();

            if (msgMap.containsKey(myMessageId)) {
            // Get the MsgObj from the map
                myMsgObj = msgMap.get(myMessageId);
                
            } else {
                myMsgObj= new ReqReplyMessageObject(aMessage.getJMSCorrelationID(), this.filterName, aMessage.getStringProperty(filterName));
            }
            
            myStatus = myMsgObj.add(aMessage);
            // Add the new Message to the MessageObject


            if (myStatus == ReqReplyStatusCode.STATUS_OK) {
                // put it back to the map
                msgMap.put(myMessageId, myMsgObj);

                String myFilterValue = myMsgObj.getFilterValue();
                if (aMessage.getStringProperty(ReqReplySettings.PROPERTY_NAME_MSG_TYPE).equals(ReqReplySettings.PROPERTY_VALUE_MSG_TYPE_PAYLOAD)) {
                    String myBitMask = Integer.toHexString(msgMap.get(myMessageId).getMsgBitMask());
                    int count = aMessage.getIntProperty(ReqReplySettings.PROPERTY_NAME_COUNT);
                    int totalCount = aMessage.getIntProperty(ReqReplySettings.PROPERTY_NAME_TOTAL_COUNT);
                    LOGGER.log(Level.DEBUG, "Add part " + count + "/" + totalCount + " to message [" + myMessageId + "], Filter: " + myFilterValue);
                } else {
                    LOGGER.log(Level.DEBUG, "Add ACK to message [" + myMessageId + "] for Filter: " + myFilterValue);
                }
            } else {
                LOGGER.log(Level.DEBUG, "ERROR adding Message to map");
            }

        } catch (InterruptedException interruptedException) {
            LOGGER.log(Level.ERROR, interruptedException);
        } finally {
            available.release();
        }
        return myStatus;
    }

    /**
     *
     * @param aMessageId
     * @param aFilterValue
     * @return
     * @throws JMSException
     */
    public synchronized ReqReplyStatusCode add(String aMessageId, String aFilterValue) throws JMSException {
        String myMessageId;
        ReqReplyStatusCode myStatus = ReqReplyStatusCode.STATUS_ERROR;
        try {
            available.acquire();

            if (msgMap.containsKey(aMessageId)) {
                myStatus = ReqReplyStatusCode.STATUS_CORRELATION_MISMATCH;
                LOGGER.log(Level.TRACE, "Add new message [" + aMessageId + "] should not exist in storage");
            } else {
                msgMap.put(aMessageId, new ReqReplyMessageObject(aMessageId, this.filterName, aFilterValue));
                LOGGER.log(Level.TRACE, "Add new message [" + aMessageId + "] to storage");
            }
        } catch (InterruptedException interruptedException) {
            LOGGER.log(Level.ERROR, interruptedException);
        } finally {
            available.release();
        }
        return myStatus;
    }

    /**
     *
     * @param myMessageId
     * @return
     */
    public synchronized String getResponse(String myMessageId) {
        String myReturn = null;
        ReqReplyMessageObject myMsgObj;
        try {
            available.acquire();
            if (msgMap.containsKey(myMessageId)) {
                myReturn = msgMap.get(myMessageId).getResponse();
                msgMap.remove(myMessageId);
            }

        } catch (InterruptedException interruptedException) {
            LOGGER.log(Level.ERROR, interruptedException);
        } finally {
            available.release();
        }

        return myReturn;
    }

    /**
     *
     * @param aMessageId
     * @return
     */
    public synchronized ReqReplyMessageObject getMsgObj(String aMessageId) {
        ReqReplyMessageObject myMsgObj = null;

        try {
            available.acquire();
        } catch (InterruptedException ex) {
            LOGGER.log(Level.ERROR, ex);
        } finally {
            available.release();
        }

        return myMsgObj;
    }

    /**
     *
     * @param myMessageId
     * @return
     */
    public synchronized boolean isResponseReceived(String myMessageId) {
        boolean myReturn = false;
        try {
            available.acquire();
            myReturn = msgMap.get(myMessageId).isFinished();

        } catch (InterruptedException interruptedException) {
            LOGGER.log(Level.ERROR, interruptedException);
        } finally {
            available.release();
        }

        return myReturn;
    }

    /**
     *
     * @return
     */
    public Map<String, ReqReplyMessageObject> getMsgMap() {
        return msgMap;
    }

    /**
     *
     * @return
     */
    public int size() {
        return msgMap.size();
    }
}
