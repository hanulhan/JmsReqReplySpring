/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hanulhan.jms.spring.reqreply.util;

import java.util.Arrays;

/**
 *
 * @author uhansen
 */
public class ReqReplyReturnObject {

    private ReqReplyStatusCode status;
    private String payload;
    private String msgType;
    private int msgCount;
    private int totalCount;

    public ReqReplyReturnObject() {
        super();
        status = ReqReplyStatusCode.STATUS_ERROR;
        payload = "";

    }

    public ReqReplyReturnObject(ReqReplyStatusCode aStatus) {
        payload = "";
        status = aStatus;
    }

    public ReqReplyStatusCode getStatus() {
        return status;
    }

    public void setStatus(ReqReplyStatusCode status) {
        this.status = status;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public void concatPayload(String str) {
        int otherLen = str.length();
        if (otherLen == 0) {
            return;
        }
        payload += str;
    }

    public Boolean getStatusOK() {
        return status == ReqReplyStatusCode.STATUS_OK;
    }

    public boolean getStatusError() {
        return status != ReqReplyStatusCode.STATUS_OK;
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public int getMsgCount() {
        return msgCount;
    }

    public void setMsgCount(int msgCount) {
        this.msgCount = msgCount;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

}
