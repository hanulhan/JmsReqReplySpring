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
public enum ReqReplyStatusCode {
    STATUS_OK, STATUS_RESPONSE_TIMEOUT, STATUS_PAYLOAD_INCOMPLETE, STATUS_RESPONSE_HEADER_ERROR, STATUS_RESPONSE_FILTER_MISMATCH, STATUS_ERROR, STATUS_CORRELATION_MISMATCH;
    
    public boolean isOk()   {
        return this == STATUS_OK;
    }
    
    public boolean isError()   {
        return this != STATUS_OK;
    }
}
