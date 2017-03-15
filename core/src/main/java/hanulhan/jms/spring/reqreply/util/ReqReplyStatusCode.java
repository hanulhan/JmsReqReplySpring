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
    STATUS_OK, STATUS_ACK_TIMEOUT, STATUS_PAYLOAD_TIMEOUT, STATUS_PAYLOAD_INCOMPLETE, STATUS_SERVER_SEND_NACK, STATUS_RESPONSE_HEADER_ERROR, STATUS_FILTER_MISMATCH, STATUS_ERROR;
}
