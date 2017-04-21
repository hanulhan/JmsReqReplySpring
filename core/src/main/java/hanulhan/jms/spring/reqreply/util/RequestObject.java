/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hanulhan.jms.spring.reqreply.util;

import java.util.Date;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * As soon as a System connects to a B2B REST call, it creates and object of this type and 
 * fall into sleep for a certain time waiting for notify.
 * The Object is added to the filterMap. 
 * If a Request for this "filterName" is received by the "ReqReplyConsumer", it is
 * added to this object and notify wakes up the REST call
 * @author uhansen
 */
public class RequestObject extends Object {
    private String messageId;
    private String consumerId;
    private final String filterValue;
    private String request;
    private Date startTime;
    boolean busy= false;
    private static final Logger LOGGER = Logger.getLogger(RequestObject.class);
    
    /**
     * Create a new object with the FilterValue (ident)
     * @param aFilterValue
     */
    public RequestObject(String aFilterValue) {
        super();
        this.filterValue = aFilterValue;
                
    }

    /**
     * As soon as a new Request stored for this filterValue (ident), the object is busy
     * @return
     */
    public boolean isBusy()  {
        return busy;
    }

    /**
     * Put a request to this filterValue (ident) 
     * @param aRequest
     * @param aMessageId
     * @param aConsumerId
     */
    public void setNewRequest(String aRequest, String aMessageId, String aConsumerId) {
        this.busy= true;
        this.request= aRequest;
        this.messageId= aMessageId;
        this.consumerId= aConsumerId;
    }

    /**
     * Put a request to this filterValue (ident) 
     * @param aRequest
     * @param aMessageId
     * @param aConsumerId
     * @param aStartTime
     */
    public void setNewRequest(String aRequest, String aMessageId, String aConsumerId, Date aStartTime)  {
        this.busy= true;
        this.request= aRequest;
        this.messageId= aMessageId;
        this.consumerId= aConsumerId;
        this.startTime= aStartTime;
        LOGGER.log(Level.DEBUG, "setNewRequest [consumer:" + aConsumerId + ", messageId: " + aMessageId);
    }
    
    /**
     *
     * @return
     */
    public Date getStartTime() {
        return startTime;
    }

    /**
     *
     * @param startTime
     */
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public String getFilterValue() {
        return filterValue;
    }
    
    
    
    @Override
    public String toString()  {
        return "Object [filter:" + filterValue + ", consumer: " + consumerId + ", request: " + request + ", msgId: " + messageId;
    }
}
