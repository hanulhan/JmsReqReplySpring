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

    /**
     *
     */
    public static final String PROPERTY_NAME_COUNT = "MSG_COUNT";

    /**
     *
     */
    public static final String PROPERTY_NAME_TOTAL_COUNT = "TOTAL_COUNT";

    /**
     *
     */
    public static final String PROPERTY_NAME_MSG_TYPE = "MSG_TYPE";

    /**
     *
     */
    public static final String PROPERTY_VALUE_MSG_TYPE_ACK = "ACK";

    /**
     *
     */
    public static final String PROPERTY_VALUE_MSG_TYPE_PAYLOAD = "PAYLOAD";
    
    private Destination reqDestination;
    private String      reqDestinationName;

    private Destination replyDestination;
    private String      replyDestinationName;

    private int         maxMessageLength;
    private String      filterPropertyName;
    
    private String      reqSubscriberName;
    private String      replySubscriberName;
    
    private long        holdTimeSec;

    /**
     *
     * @return
     */
    public Destination getReqDestination() {
        return reqDestination;
    }

    /**
     *
     * @param reqDestination
     */
    public void setReqDestination(Destination reqDestination) {
        this.reqDestination = reqDestination;
    }

    /**
     *
     * @return
     */
    public String getReqDestinationName() {
        return reqDestinationName;
    }

    /**
     *
     * @param reqDestinationName
     */
    public void setReqDestinationName(String reqDestinationName) {
        this.reqDestinationName = reqDestinationName;
    }

    /**
     *
     * @return
     */
    public Destination getReplyDestination() {
        return replyDestination;
    }

    /**
     *
     * @param replyDestination
     */
    public void setReplyDestination(Destination replyDestination) {
        this.replyDestination = replyDestination;
    }

    /**
     *
     * @return
     */
    public String getReplyDestinationName() {
        return replyDestinationName;
    }

    /**
     *
     * @param replyDestinationName
     */
    public void setReplyDestinationName(String replyDestinationName) {
        this.replyDestinationName = replyDestinationName;
    }

    /**
     *
     * @return
     */
    public int getMaxMessageLength() {
        return maxMessageLength;
    }

    /**
     *
     * @param maxMessageLength
     */
    public void setMaxMessageLength(int maxMessageLength) {
        this.maxMessageLength = maxMessageLength;
    }

    /**
     *
     * @return
     */
    public String getFilterPropertyName() {
        return filterPropertyName;
    }

    /**
     *
     * @param filterPropertyName
     */
    public void setFilterPropertyName(String filterPropertyName) {
        this.filterPropertyName = filterPropertyName;
    }

    /**
     *
     * @return
     */
    public String getReqSubscriberName() {
        return reqSubscriberName;
    }

    /**
     *
     * @param reqSubscriberName
     */
    public void setReqSubscriberName(String reqSubscriberName) {
        this.reqSubscriberName = reqSubscriberName;
    }

    /**
     *
     * @return
     */
    public String getReplySubscriberName() {
        return replySubscriberName;
    }

    /**
     *
     * @param replySubscriberName
     */
    public void setReplySubscriberName(String replySubscriberName) {
        this.replySubscriberName = replySubscriberName;
    }

    public long getHoldTimeSec() {
        return holdTimeSec;
    }

    public void setHoldTimeSec(long holdTimeSec) {
        this.holdTimeSec = holdTimeSec;
    }


    
    
    

}
