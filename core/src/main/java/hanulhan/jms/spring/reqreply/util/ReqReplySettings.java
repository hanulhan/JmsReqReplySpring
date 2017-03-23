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
    
    private Destination destination;
    private int maxMessageLength;
    private String serverId;
    private String filterPropertyName;

    public String getFilterPropertyName() {
        return filterPropertyName;
    }

    public void setFilterPropertyName(String filterPropertyName) {
        this.filterPropertyName = filterPropertyName;
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

    public Destination getDestination() {
        return destination;
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
    }

}
