/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hanulhan.jms.spring.reqreply.util;

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
public class ReqReplyMessageObject {

    // vars static for the object
    private String messageId = null;
    private String filterValue = null;
    private String filterName = null;
    private Date startTime = new Date();
    private long responseTime = 0;

    // vars modified by a new message
    private String[] payload = null;
    private boolean ackReceived = false;
    private boolean msgTypeAck = false;
    private int msgBitMask = 0;
    private int msgCount = 0;
    private int totalCount = 0;

    private ReqReplyStatusCode statusCode = ReqReplyStatusCode.STATUS_ERROR;
    static final Logger LOGGER = Logger.getLogger(ReqReplyMessageObject.class);

    public ReqReplyMessageObject(String aMessageId, String aFilterName, String aFilterValue) {
        super();
        this.messageId = aMessageId;
        this.filterName = aFilterName;
        this.filterValue = aFilterValue;
        this.msgBitMask= 0;
    }

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
                    LOGGER.log(Level.DEBUG, "ReqReplyMessageObj::add(), create new array with size " + this.totalCount);
                } else {
                    this.msgCount++;
                }

                if (this.msgCount == this.totalCount) {
                    this.responseTime = ((new Date().getTime() - this.startTime.getTime()));
                }

                this.payload[lfdMsg] = ((TextMessage) aMessage).getText();
                LOGGER.log(Level.DEBUG, "ReqReplyMessageObj::add(), store payload to array[" + lfdMsg + "]");
            }
        }

        return this.statusCode;
    }

    public boolean isFinished() {
        return this.ackReceived && this.msgCount > 0 && this.msgCount == this.totalCount;
    }

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

        if (!(aMessage instanceof TextMessage)) {
            LOGGER.log(Level.ERROR, "Message is not a TextMessage");
            return ReqReplyStatusCode.STATUS_MESSAGE_ERROR;
        }

        // Check if Property: <FilterName>  exists
        if (!aMessage.propertyExists(this.filterName)) {
            LOGGER.log(Level.ERROR, "PropertyName: " + this.filterName + " missing in response");
            return ReqReplyStatusCode.STATUS_HEADER_ERROR;
        }

        // Check content of Property: <FilterName>
        if (!aMessage.getStringProperty(this.filterName).equals(this.filterValue)) {
            LOGGER.log(Level.ERROR, "PropertyName: " + this.filterName + " missmatch");
            return ReqReplyStatusCode.STATUS_HEADER_ERROR;
        }

        // Check if Property: "MsgType"  exists
        if (!aMessage.propertyExists(ReqReplySettings.PROPERTY_NAME_MSG_TYPE)) {
            LOGGER.log(Level.ERROR, "PropertyName: MsgType missing in response");
            return ReqReplyStatusCode.STATUS_HEADER_ERROR;
        }

        // MsgType ACK or Payload check
        if (aMessage.getStringProperty(ReqReplySettings.PROPERTY_NAME_MSG_TYPE).equals(ReqReplySettings.PROPERTY_VALUE_MSG_TYPE_PAYLOAD)) {
            if (!aMessage.propertyExists(ReqReplySettings.PROPERTY_NAME_COUNT)) {
                LOGGER.log(Level.ERROR, "PropertyName: " + ReqReplySettings.PROPERTY_NAME_COUNT + "missing in response");
                return ReqReplyStatusCode.STATUS_HEADER_ERROR;
            }
            if (!aMessage.propertyExists(ReqReplySettings.PROPERTY_NAME_TOTAL_COUNT)) {
                LOGGER.log(Level.ERROR, "PropertyName: " + ReqReplySettings.PROPERTY_NAME_TOTAL_COUNT + "missing in reponse");
                return ReqReplyStatusCode.STATUS_HEADER_ERROR;
            }

            // if it's not the first message, check the total count
            if (this.payload != null && aMessage.getIntProperty(ReqReplySettings.PROPERTY_NAME_TOTAL_COUNT) != this.totalCount) {
                LOGGER.log(Level.ERROR, "PropertyName: " + ReqReplySettings.PROPERTY_NAME_TOTAL_COUNT + "missmatch");
                return ReqReplyStatusCode.STATUS_HEADER_ERROR;
            }

            this.msgTypeAck = false;

        } else if (aMessage.getStringProperty(ReqReplySettings.PROPERTY_NAME_MSG_TYPE).equals(ReqReplySettings.PROPERTY_VALUE_MSG_TYPE_ACK)) {
            this.msgTypeAck = true;
        } else {
            LOGGER.log(Level.ERROR, "PropertyName: " + ReqReplySettings.PROPERTY_NAME_MSG_TYPE + "not valid");
            return ReqReplyStatusCode.STATUS_HEADER_ERROR;
        }

        // Check if Properties fit to the new Message
        if (!aMessage.getJMSCorrelationID().equals(this.messageId)) {
            LOGGER.log(Level.ERROR, "MessageId missmatch ");
            return ReqReplyStatusCode.STATUS_CORRELATION_MISMATCH;
        }

        return ReqReplyStatusCode.STATUS_OK;
    }

    public boolean getStatusOk() {
        return statusCode == ReqReplyStatusCode.STATUS_OK;
    }

    public String getMessageId() {
        return messageId;
    }

    public int getMsgBitMask() {
        return msgBitMask;
    }

}
