/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hanulhan.jms.spring.reqreply.util;

import hanulhan.jms.spring.reqreply.jaxb.generated.MessageObj;
import hanulhan.jms.spring.reqreply.message.ReqReplyMessageContainer;
import javax.xml.datatype.XMLGregorianCalendar;
import org.apache.log4j.Logger;

/**
 * As soon as a System connects to a B2B REST call, it creates and object of
 * this type and fall into sleep for a certain time waiting for notify. The
 * Object is added to the filterMap. If a Request for this "filterName" is
 * received by the "ReqReplyConsumer", it is added to this object and notify
 * wakes up the REST call
 *
 * @author uhansen
 */
public class RequestObject extends Object {

    private MessageObj messageObj;
    private boolean busy = false;
    static final Logger LOGGER = Logger.getLogger(ReqReplyMessageContainer.class);

    /**
     * Create a new object with the FilterValue (ident)
     *
     * @param aIdent
     */
    public RequestObject(String aIdent) {
        super();
        this.messageObj= new MessageObj();
        this.messageObj.setIdent(aIdent);

    }

    /**
     * As soon as a new Request stored for this filterValue (ident), the object
     * is busy
     *
     * @return
     */
    public boolean isBusy() {
        return busy;
    }

    /**
     * Put a request to this filterValue (ident)
     *
     * @param aRequest
     * @param aCommand
     * @param aPort
     * @param aMessageId
     * @param aConsumerId
     */
    public void setNewRequest(String aRequest, String aCommand, int aPort, String aMessageId, String aConsumerId) {
        this.busy = true;
        this.messageObj.setRequest(aRequest);
        this.messageObj.setMessageid(aMessageId);
        this.messageObj.setCommand(aCommand);
        this.messageObj.setPort(aPort);

    }

    /**
     * Put a request to this filterValue (ident)
     *
     * @param aRequest
     * @param aMessageId
     * @param aConsumerId
     * @param aStartDateTime
     */
    public void setNewRequest(String aRequest, String aCommand, int aPort, String aMessageId, String aConsumerId, XMLGregorianCalendar aStartDateTime) {
        this.busy = true;
        this.messageObj.setRequest(aRequest);
        this.messageObj.setMessageid(aMessageId);
        this.messageObj.setCreated(aStartDateTime);
        this.messageObj.setCommand(aCommand);
        this.messageObj.setPort(aPort);
    }

    /**
     *
     * @return
     */
    public XMLGregorianCalendar getStartDateTime() {
        return messageObj.getCreated();
    }

    /**
     *
     * @param aStartDateTime
     */
    public void setStartDateTime(XMLGregorianCalendar aStartDateTime) {
        this.messageObj.setCreated(aStartDateTime);

//        GregorianCalendar gcal = (GregorianCalendar) GregorianCalendar.getInstance();
//        gcal.setTime(aStartTime);
//        try {
//            XMLGregorianCalendar xgcal = DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);
//            this.reqReply.setCreated(xgcal);
//        } catch (DatatypeConfigurationException e) {
//            e.printStackTrace();
//        }
    }

    public String getIdent() {
        return this.messageObj.getIdent();
    }

    public String getMessageId() {
        return this.messageObj.getMessageid();
    }

    public void setMessageId(String aMessageId) {
        this.messageObj.setMessageid(aMessageId);
    }

    public String getRequest() {
        return this.messageObj.getRequest();
    }

    public MessageObj getMessageObj() {
        return messageObj;
    }

    public void setMessageObj(MessageObj messageObj) {
        this.messageObj = messageObj;
    }



    public void setConsumerId(String aId)   {
        this.messageObj.setConsumderid(aId);
    }
    
    
    @Override
    public String toString() {
        return "Object [ident:" + this.messageObj.getIdent() + ", request: " + this.messageObj.getRequest() + ", msgId: " + this.messageObj.getMessageid();
//        return "Object [ident:" + this.reqReply.getIdent() + ", consumer: " + this.reqReply.get + ", request: " + request + ", msgId: " + messageId;
    }
}
