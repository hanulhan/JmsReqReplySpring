/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hanulhan.jms.spring.reqreply.util;

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

    private ReqReply reqReply;
    private boolean busy = false;
    static final Logger LOGGER = Logger.getLogger(ReqReplyMessageObject.class);

    /**
     * Create a new object with the FilterValue (ident)
     *
     * @param aIdent
     */
    public RequestObject(String aIdent) {
        super();
        this.reqReply= new ReqReply();
        this.reqReply.setIdent(aIdent);

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
     * @param aMessageId
     * @param aConsumerId
     */
    public void setNewRequest(String aRequest, String aMessageId, String aConsumerId) {
        this.busy = true;
        this.reqReply.setRequest(aRequest);
        this.reqReply.setMessageid(aMessageId);

    }

    /**
     * Put a request to this filterValue (ident)
     *
     * @param aRequest
     * @param aMessageId
     * @param aConsumerId
     * @param aStartDateTime
     */
    public void setNewRequest(String aRequest, String aMessageId, String aConsumerId, XMLGregorianCalendar aStartDateTime) {
        this.busy = true;
        this.reqReply.setRequest(aRequest);
        this.reqReply.setMessageid(aMessageId);
        this.reqReply.setCreated(aStartDateTime);
//        GregorianCalendar gcal = (GregorianCalendar) GregorianCalendar.getInstance();
//        gcal.setTime(aStartTime);
//        try {
//            XMLGregorianCalendar xgcal = DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);
//            this.reqReply.setCreated(xgcal);
//        } catch (DatatypeConfigurationException e) {
//            LOGGER.log(Level.ERROR, e);
//        }
    }

    /**
     *
     * @return
     */
    public XMLGregorianCalendar getStartDateTime() {
        return reqReply.getCreated();
    }

    /**
     *
     * @param aStartDateTime
     */
    public void setStartDateTime(XMLGregorianCalendar aStartDateTime) {
        this.reqReply.setCreated(aStartDateTime);

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
        return this.reqReply.getIdent();
    }

    public String getMessageId() {
        return this.reqReply.getMessageid();
    }

    public void setMessageId(String aMessageId) {
        this.reqReply.setMessageid(aMessageId);
    }

    public String getRequest() {
        return this.reqReply.getRequest();
    }

    public ReqReply getReqReply() {
        return reqReply;
    }

    public void setReqReply(ReqReply reqReply) {
        this.reqReply = reqReply;
    }

    public void setConsumerId(String aId)   {
        this.reqReply.setConsumderid(aId);
    }
    
    
    @Override
    public String toString() {
        return "Object [ident:" + this.reqReply.getIdent() + ", request: " + this.reqReply.getRequest() + ", msgId: " + this.reqReply.getMessageid();
//        return "Object [ident:" + this.reqReply.getIdent() + ", consumer: " + this.reqReply.get + ", request: " + request + ", msgId: " + messageId;
    }
}
