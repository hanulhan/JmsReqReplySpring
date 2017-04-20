/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hanulhan.jms.spring.reqreply.util;

import static hanulhan.jms.spring.reqreply.util.ReqReplyTest3_IdentMap.LOGGER;
import java.util.Date;
import org.apache.log4j.Level;

/**
 *
 * @author uhansen
 */
public class ReqReplyTest3_Object extends Object{
    private String messageId;
    private int consumerId;
    private String ident;
    private String request;
    private Date startTime;
    boolean inProgress= false;

    /**
     *
     * @param ident
     */
    public ReqReplyTest3_Object(String ident) {
        super();
        this.ident = ident;
                
    }

    /**
     *
     * @return
     */
    public boolean isInProgress() {
        return inProgress;
    }

    /**
     *
     * @param aRequest
     * @param aMessageId
     * @param aConsumerId
     */
    public void setNewRequest(String aRequest, String aMessageId, int aConsumerId)  {
        this.inProgress= true;
        this.request= aRequest;
        this.messageId= aMessageId;
        this.consumerId= aConsumerId;
    }

    /**
     *
     * @param aRequest
     * @param aMessageId
     * @param aConsumerId
     * @param aStartTime
     */
    public void setNewRequest(String aRequest, String aMessageId, int aConsumerId, Date aStartTime)  {
        this.inProgress= true;
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
    
    
    @Override
    public String toString()  {
        return "Object [ident:" + ident + ", consumer: " + consumerId + ", request: " + request + ", msgId: " + messageId;
    }
}
