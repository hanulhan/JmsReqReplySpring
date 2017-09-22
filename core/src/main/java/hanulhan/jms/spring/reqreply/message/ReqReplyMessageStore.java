/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hanulhan.jms.spring.reqreply.message;

import hanulhan.jms.spring.reqreply.jaxb.generated.ReqReply;
import hanulhan.jms.spring.reqreply.util.ReqReplySettings;
import hanulhan.jms.spring.reqreply.util.ReqReplyStatusCode;
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
public class ReqReplyMessageStore {

    private final Map<String, ReqReplyMessageContainer> msgMap;
    private final Semaphore available = new Semaphore(1, true);
    static final Logger LOGGER = Logger.getLogger(ReqReplyMessageStore.class);
    private final String filterName;

    /**
     *
     * @param aFilterName
     */
    public ReqReplyMessageStore(String aFilterName) {
        super();
        this.filterName = aFilterName;
        msgMap = Collections.synchronizedMap(new HashMap<String, ReqReplyMessageContainer>());
    }


    /**
     * Add the response from the Response-Topic to the MessageStore. 
     * @param aJmsMessage
     * @return ReqReplyStatusCode
     * @throws JMSException
     */
    public synchronized ReqReplyStatusCode add(Message aJmsMessage) throws JMSException {
        String myMessageId;
        ReqReplyStatusCode myStatus = ReqReplyStatusCode.STATUS_ERROR;
        ReqReplyMessageContainer myMessageContainer;
        try {
            myMessageId = aJmsMessage.getJMSCorrelationID();
            available.acquire();

            if (msgMap.containsKey(myMessageId)) {
            // Get the MsgObj from the map
                myMessageContainer = msgMap.get(myMessageId);
                
            } else {
                myMessageContainer= new ReqReplyMessageContainer(aJmsMessage.getJMSCorrelationID(), this.filterName, aJmsMessage.getStringProperty(filterName));
            }
            
            myStatus = myMessageContainer.add(aJmsMessage);
            // Add the new Message to the ReqReplyect


            if (myStatus == ReqReplyStatusCode.STATUS_OK) {
                // put it back to the map
                msgMap.put(myMessageId, myMessageContainer);

                String myFilterValue = myMessageContainer.getIdent();
                if (aJmsMessage.getStringProperty(ReqReplySettings.PROPERTY_NAME_MSG_TYPE).equals(ReqReplySettings.PROPERTY_VALUE_MSG_TYPE_PAYLOAD)) {
//                    String myBitMask = Integer.toHexString(msgMap.get(myMessageId).getMsgBitMask());
                    int count = aJmsMessage.getIntProperty(ReqReplySettings.PROPERTY_NAME_COUNT);
                    int totalCount = aJmsMessage.getIntProperty(ReqReplySettings.PROPERTY_NAME_TOTAL_COUNT);
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
     * @param aMessage
     * @param aFilterValue
     * @return
     * @throws JMSException
     */
//    public synchronized ReqReplyStatusCode add(String aMessageId, String aFilterValue) throws JMSException {
//        String myMessageId;
//        ReqReplyStatusCode myStatus = ReqReplyStatusCode.STATUS_ERROR;
//        try {
//            available.acquire();
//
//            if (msgMap.containsKey(aMessageId)) {
//                myStatus = ReqReplyStatusCode.STATUS_CORRELATION_MISMATCH;
//                LOGGER.log(Level.TRACE, "Add new message [" + aMessageId + "] should not exist in storage");
//            } else {
//                msgMap.put(aMessageId, new ReqReplyMessageContainer(aMessageId, this.filterName, aFilterValue));
//                LOGGER.log(Level.TRACE, "Add new message [" + aMessageId + "] to storage");
//            }
//        } catch (InterruptedException interruptedException) {
//            LOGGER.log(Level.ERROR, interruptedException);
//        } finally {
//            available.release();
//        }
//        return myStatus;
//    }

    public synchronized ReqReplyStatusCode add(String aMessageId, ReqReply aMessage) throws JMSException {
        ReqReplyStatusCode myStatus = ReqReplyStatusCode.STATUS_ERROR;
        try {
            available.acquire();

            if (msgMap.containsKey(aMessageId)) {
                myStatus = ReqReplyStatusCode.STATUS_CORRELATION_MISMATCH;
                LOGGER.log(Level.TRACE, "Add new message [" + aMessageId + "] should not exist in storage");
            } else {
                msgMap.put(aMessageId, new ReqReplyMessageContainer(aMessageId, this.filterName, aMessage.getIdent()));
                //msgMap.put(aMessageId, aMessage);
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
        ReqReplyMessageContainer myMsgObj;
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

    
    public synchronized ReqReplyMessageContainer getResponseObj(String myMessageId) {
        ReqReplyMessageContainer myReturn = null;
        try {
            available.acquire();
            if (msgMap.containsKey(myMessageId)) {
                myReturn = msgMap.get(myMessageId);
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
    public synchronized ReqReplyMessageContainer getMsgObj(String aMessageId) {
        ReqReplyMessageContainer myMsgObj = null;

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
    public synchronized boolean isResponseReceived(String aMessageId) {
        boolean myReturn = false;
        try {
            available.acquire();
            myReturn = msgMap.get(aMessageId).isFinished();

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
    public Map<String, ReqReplyMessageContainer> getMsgMap() {
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
