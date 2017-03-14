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
public interface ReqReplyFilterInterface {
    public Boolean getPropertyFilterActive(String aPropertyFilterValue);
    public String  getPropertyFilterResult(String aPropertyFilterValue);
}
