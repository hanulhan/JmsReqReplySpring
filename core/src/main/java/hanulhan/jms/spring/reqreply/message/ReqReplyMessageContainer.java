/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hanulhan.jms.spring.reqreply.message;

import hanulhan.jms.spring.reqreply.jaxb.generated.MessageObj;
import hanulhan.jms.spring.reqreply.util.ReqReplySettings;
import hanulhan.jms.spring.reqreply.util.ReqReplyStatusCode;
import java.util.Date;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author uhansen
 */
public class ReqReplyMessageContainer {

    MessageObj messageObj;

    // vars static for the object
//    private String messageId = null;
    private String filterName = null;
    private final Date startTime = new Date();
    private long responseTime = 0;

    // vars modified by a new message
    private String[] payload = null;
    private boolean ackReceived = false;
    private boolean msgTypeAck = false;
    private int msgBitMask = 0;
    private int msgCount = 0;
    private int totalCount = 0;

    private ReqReplyStatusCode statusCode = ReqReplyStatusCode.STATUS_ERROR;
    static final Logger LOGGER = Logger.getLogger(ReqReplyMessageContainer.class);

    /**
     *
     * @param aMessageId
     * @param aFilterName
     * @param aFilterValue
     */
    public ReqReplyMessageContainer(String aMessageId, String aFilterName, String aFilterValue) {
        super();
        this.filterName = aFilterName;
        this.msgBitMask= 0;

        this.messageObj= new MessageObj();
        this.messageObj.setMessageid(aMessageId);
        this.messageObj.setIdent(aFilterValue);
        this.messageObj.setMessageid(aMessageId);
        
    }

    public ReqReplyMessageContainer(String aFilterName, MessageObj aMessage) {    
        super();
        this.filterName = aFilterName;
        this.msgBitMask= 0;
        
        this.messageObj= aMessage;
    }
    
    /**
     *
     * @param aMessage
     * @return
     * @throws JMSException
     */
    public ReqReplyStatusCode add(Message aMessage) throws JMSException {

        int lfdMsg;
        int myTotalCount;

        this.statusCode = validate(aMessage);
        if (this.statusCode == ReqReplyStatusCode.STATUS_OK) {
            if (this.msgTypeAck) {
                this.ackReceived = true;
            } else {
                lfdMsg = aMessage.getIntProperty(ReqReplySettings.PROPERTY_NAME_COUNT) - 1;
                msgBitMask |= 1 << lfdMsg;
                myTotalCount = aMessage.getIntProperty(ReqReplySettings.PROPERTY_NAME_TOTAL_COUNT);

                // First Message?
                if (this.payload == null) {
                    this.totalCount = myTotalCount;
                    this.msgCount = 1;
                    this.payload = new String[this.totalCount];
//                    LOGGER.log(Level.DEBUG, "ReqReplyMessageObj::add(), create new array with size " + this.totalCount);
                } else {
                    this.msgCount++;
                }

                if (this.msgCount == this.totalCount) {
                    this.responseTime = ((new Date().getTime() - this.startTime.getTime()));
                }

                this.payload[lfdMsg] = ((TextMessage) aMessage).getText();
//                LOGGER.log(Level.DEBUG, "ReqReplyMessageObj::add(), store payload to array[" + lfdMsg + "]");
            }
        }

        return this.statusCode;
    }

    /**
     *
     * @return
     */
    public synchronized boolean isFinished() {
        return this.ackReceived && this.msgCount > 0 && this.msgCount == this.totalCount;
    }

    /**
     *
     * @return
     */
    public String getResponse() {
        String myResponse = null;

        if (isFinished()) {
            myResponse= new String();
            for (String temp : payload) {
                myResponse += temp;
            }
        }

        return myResponse;
    }

    private ReqReplyStatusCode validate(Message aMessage) throws JMSException {

        if (aMessage == null) {
            LOGGER.log(Level.ERROR, "Message is null");
            return ReqReplyStatusCode.STATUS_TIMEOUT;
        }

//        if (!(aMessage instanceof TextMessage)) {
//            LOGGER.log(Level.ERROR, "Message is not a TextMessage");
//            return ReqReplyStatusCode.STATUS_MESSAGE_ERROR;
//        }

        // Check if Property: <FilterName>  exists
        if (!aMessage.propertyExists(this.filterName)) {
            LOGGER.log(Level.ERROR, "PropertyName: " + this.filterName + " missing in response");
            return ReqReplyStatusCode.STATUS_HEADER_ERROR;
        }

        // Check content of Property: <FilterName>
        if (!aMessage.getStringProperty(this.filterName).equals(this.messageObj.getIdent())) {
            LOGGER.log(Level.ERROR, "PropertyName: " + this.filterName + " missmatch");
            return ReqReplyStatusCode.STATUS_HEADER_ERROR;
        }

        // Check if Property: "MsgType"  exists
        if (!aMessage.propertyExists(ReqReplySettings.PROPERTY_NAME_MSG_TYPE)) {
            LOGGER.log(Level.ERROR, "PropertyName: MsgType missing in response");
            return ReqReplyStatusCode.STATUS_HEADER_ERROR;
        }

        // MsgType ACK or Payload check
        switch (aMessage.getStringProperty(ReqReplySettings.PROPERTY_NAME_MSG_TYPE)) {
            case ReqReplySettings.PROPERTY_VALUE_MSG_TYPE_PAYLOAD:
                if (!aMessage.propertyExists(ReqReplySettings.PROPERTY_NAME_COUNT)) {
                    LOGGER.log(Level.ERROR, "PropertyName: " + ReqReplySettings.PROPERTY_NAME_COUNT + "missing in response");
                    return ReqReplyStatusCode.STATUS_HEADER_ERROR;
                }   if (!aMessage.propertyExists(ReqReplySettings.PROPERTY_NAME_TOTAL_COUNT)) {
                    LOGGER.log(Level.ERROR, "PropertyName: " + ReqReplySettings.PROPERTY_NAME_TOTAL_COUNT + "missing in reponse");
                    return ReqReplyStatusCode.STATUS_HEADER_ERROR;
                }   // if it's not the first message, check the total count
                if (this.payload != null && aMessage.getIntProperty(ReqReplySettings.PROPERTY_NAME_TOTAL_COUNT) != this.totalCount) {
                    LOGGER.log(Level.ERROR, "PropertyName: " + ReqReplySettings.PROPERTY_NAME_TOTAL_COUNT + "missmatch");
                    return ReqReplyStatusCode.STATUS_HEADER_ERROR;
                }   this.msgTypeAck = false;
                break;
            case ReqReplySettings.PROPERTY_VALUE_MSG_TYPE_ACK:
                this.msgTypeAck = true;
                break;
            default:
                LOGGER.log(Level.ERROR, "PropertyName: " + ReqReplySettings.PROPERTY_NAME_MSG_TYPE + "not valid");
                return ReqReplyStatusCode.STATUS_HEADER_ERROR;
        }

        // Check if Properties fit to the new Message
        if (!aMessage.getJMSCorrelationID().equals(this.messageObj.getMessageid())) {
            LOGGER.log(Level.ERROR, "MessageId missmatch ");
            return ReqReplyStatusCode.STATUS_CORRELATION_MISMATCH;
        }

        return ReqReplyStatusCode.STATUS_OK;
    }

    /**
     *
     * @return
     */
    public boolean getStatusOk() {
        return statusCode == ReqReplyStatusCode.STATUS_OK;
    }


    /**
     *
     * @return
     */
    public int getMsgBitMask() {
        return msgBitMask;
    }

    /**
     *
     * @return
     */
    public String getFilterName() {
        return filterName;
    }

    /**
     *
     * @return
     */
    public long getResponseTime() {
        return responseTime;
    }

    /**
     *
     * @return
     */
    public boolean isAckReceived() {
        return ackReceived;
    }

    /**
     *
     * @return
     */
    public int getMsgCount() {
        return msgCount;
    }

    /**
     *
     * @return
     */
    public int getTotalCount() {
        return totalCount;
    }

    /**
     *
     * @return
     */
    public ReqReplyStatusCode getStatusCode() {
        return statusCode;
    }

    public String getIdent()    {
        return messageObj.getIdent();
    }
    
    @Override
    public String toString()    {
        return  "MsgObj: {id: " + this.messageObj.getMessageid()
                        + ", Ident: " + this.messageObj.getIdent()
                        + ", count: " + this.msgCount
                        + ", totalCount: " + this.totalCount
                        + ", bitMask: " + this.msgBitMask
                        + ", ackReceived: " + this.ackReceived
                        + ", status: " + this.statusCode;
    }
    
}
