/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hanulhan.jms.spring.reqreply.util;

import javax.jms.Destination;

/**
 *
 * @author uhansen
 */
public class ReqReplySettings {
    public static final String PROPERTY_NAME_COUNT = "MSG_COUNT";
    public static final String PROPERTY_NAME_TOTAL_COUNT = "TOTAL_COUNT";
    public static final String PROPERTY_NAME_MSG_TYPE = "MSG_TYPE";
    public static final String PROPERTY_VALUE_MSG_TYPE_ACK = "ACK";
    public static final String PROPERTY_VALUE_MSG_TYPE_PAYLOAD = "PAYLOAD";
    
    private Destination reqDestination;
    private String      reqDestinationName;

    private Destination replyDestination;
    private String      replyDestinationName;

    private int         maxMessageLength;
    private String      serverId;
    private String      clientId;
    private String      filterPropertyName;
    
    private String      reqSubscriberName;
    private String      replySubscriberName;

    public Destination getReqDestination() {
        return reqDestination;
    }

    public void setReqDestination(Destination reqDestination) {
        this.reqDestination = reqDestination;
    }

    public int getMaxMessageLength() {
        return maxMessageLength;
    }

    public void setMaxMessageLength(int maxMessageLength) {
        this.maxMessageLength = maxMessageLength;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getFilterPropertyName() {
        return filterPropertyName;
    }

    public void setFilterPropertyName(String filterPropertyName) {
        this.filterPropertyName = filterPropertyName;
    }

    public String getReqDestinationName() {
        return reqDestinationName;
    }

    public void setReqDestinationName(String reqDestinationName) {
        this.reqDestinationName = reqDestinationName;
    }

    public String getReqSubscriberName() {
        return reqSubscriberName;
    }

    public void setReqSubscriberName(String reqSubscriberName) {
        this.reqSubscriberName = reqSubscriberName;
    }

    public String getReplySubscriberName() {
        return replySubscriberName;
    }

    public void setReplySubscriberName(String replySubscriberName) {
        this.replySubscriberName = replySubscriberName;
    }

    public Destination getReplyDestination() {
        return replyDestination;
    }

    public void setReplyDestination(Destination replyDestination) {
        this.replyDestination = replyDestination;
    }

    public String getReplyDestinationName() {
        return replyDestinationName;
    }

    public void setReplyDestinationName(String replyDestinationName) {
        this.replyDestinationName = replyDestinationName;
    }
    
    

}
