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

    /**
     *
     * @param aPropertyFilterName
     * @return
     */
    public Boolean getPropertyFilterActive(String aPropertyFilterName);

    /**
     *
     * @param aPropertyFilterName
     * @return
     */
    public String  getPropertyFilterResult(String aPropertyFilterName);
}
