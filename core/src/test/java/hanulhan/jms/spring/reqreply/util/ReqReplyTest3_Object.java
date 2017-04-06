/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hanulhan.jms.spring.reqreply.util;

/**
 *
 * @author uhansen
 */
public class ReqReplyTest3_Object extends Object{
    private String messageId;
    private int consumerId;
    private String ident;
    private String request;
    boolean inProgress= false;

    public ReqReplyTest3_Object(String ident) {
        super();
        this.ident = ident;
                
    }

    public boolean isInProgress() {
        return inProgress;
    }

    

    public void setNewRequest(String aRequest, String aMessageId, int aConsumerId)  {
        this.inProgress= true;
        this.request= aRequest;
        this.messageId= aMessageId;
        this.consumerId= aConsumerId;
    }
    
    @Override
    public String toString()  {
        return "Object [ident:" + ident + ", consumer: " + consumerId + ", request: " + request + ", msgId: " + messageId;
    }
}
