/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hanulhan.jms.spring.reqreply.util;

import hanulhan.jms.spring.reqreply.beans.ReqReplyProducer;
import java.util.Arrays;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

/**
 *
 * @author uhansen
 */
public class ReqReplyMessageObject {

    private String[] payload;
    private boolean ackReceived;
    private int msgCount;
    private int totalCount;
    private String messageId;
    private String filterValue;
    private String filterName;
    private ReqReplyStatusCode statusCode;
    static final Logger LOGGER = Logger.getLogger(ReqReplyMessageObject.class);

    public ReqReplyMessageObject() {
        super();
        this.ackReceived = false;
        this.totalCount = 0;
        this.msgCount = 0;
    }

    public ReqReplyMessageObject(Message aMessage, String aFilterName) {
        this.filterName = aFilterName;
        try {
            this.add(aMessage);
        } catch (JMSException jMSException) {
            LOGGER.log(Level.ERROR, jMSException);
        }

    }

    public void add(Message aMessage) throws JMSException   {
        if (aMessage == null) {
            this.statusCode = ReqReplyStatusCode.STATUS_TIMEOUT;
            LOGGER.log(Level.ERROR, "Message is null");
            return;
        }

        if (!(aMessage instanceof TextMessage)) {
            this.statusCode = ReqReplyStatusCode.STATUS_MESSAGE_ERROR;
            LOGGER.log(Level.ERROR, "Message is not a TextMessage");
            return;
        }

        
        
        // Check if Property: <FilterName>  exists
        if (!aMessage.propertyExists(this.filterName)) {
            LOGGER.log(Level.ERROR, "FilterProperty: " + this.filterName + " missing in response");
            this.statusCode = ReqReplyStatusCode.STATUS_HEADER_ERROR;
            return;
        }

        // Check if Filter-Property match
        this.filterValue = aMessage.getStringProperty(this.filterName);

        // Check if Property: "MsgType"  exists
        if (!aMessage.propertyExists(ReqReplySettings.PROPERTY_NAME_MSG_TYPE)) {
            LOGGER.log(Level.ERROR, "PropertyName: MsgType missing in message");
            this.statusCode = ReqReplyStatusCode.STATUS_HEADER_ERROR;
            return;
        }

        // More checks for MsgType: payload
        if (aMessage.getStringProperty(ReqReplySettings.PROPERTY_NAME_MSG_TYPE).equals(ReqReplySettings.PROPERTY_VALUE_MSG_TYPE_ACK)) {
            this.ackReceived = true;
            this.messageId= aMessage.getJMSCorrelationID();
        } else if (aMessage.getStringProperty(ReqReplySettings.PROPERTY_NAME_MSG_TYPE).equals(ReqReplySettings.PROPERTY_VALUE_MSG_TYPE_PAYLOAD)) {

            if (!aMessage.propertyExists(ReqReplySettings.PROPERTY_NAME_TOTAL_COUNT)) {
                LOGGER.log(Level.ERROR, "Property TOTAL_COUNT missing");
                this.statusCode=  ReqReplyStatusCode.STATUS_HEADER_ERROR;
                return;
            }
            if (!aMessage.propertyExists(ReqReplySettings.PROPERTY_NAME_COUNT)) {
                LOGGER.log(Level.ERROR, "Property COUNT missing");
                this.statusCode = ReqReplyStatusCode.STATUS_HEADER_ERROR;
                return;
            }

            if (!aMessage.getJMSCorrelationID().equals(this.messageId)) {
                LOGGER.log(Level.ERROR, "Correlation missmatch");
                this.statusCode = ReqReplyStatusCode.STATUS_HEADER_ERROR;
                return;
            }
            
            TextMessage myTextMessage= (TextMessage)aMessage;
            int myMsgCount;
            
            if (payload == null)   {
                this.totalCount= aMessage.getIntProperty(ReqReplySettings.PROPERTY_NAME_TOTAL_COUNT);
                myMsgCount= aMessage.getIntProperty(ReqReplySettings.PROPERTY_NAME_COUNT);
                payload= new String[this.totalCount];
                payload[myMsgCount]= myTextMessage.getText();
                this.msgCount= 1;
            } else if (this.totalCount != aMessage.getIntProperty(ReqReplySettings.PROPERTY_NAME_TOTAL_COUNT))  {
                LOGGER.log(Level.ERROR, "Property TOTAL-COUNT with different value");
                this.statusCode = ReqReplyStatusCode.STATUS_HEADER_ERROR;
                return;
            } else {
                myMsgCount= aMessage.getIntProperty(ReqReplySettings.PROPERTY_NAME_COUNT);
                payload[myMsgCount]= myTextMessage.getText();
                this.msgCount++;
            }
        }        
    }
    
}


