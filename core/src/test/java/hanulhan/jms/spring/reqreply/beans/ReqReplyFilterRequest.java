/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hanulhan.jms.spring.reqreply.beans;

import java.util.ArrayList;
import java.util.List;
import hanulhan.jms.spring.reqreply.util.ReqReplyFilterInterface;

/**
 *
 * @author uhansen
 */
public class ReqReplyFilterRequest implements ReqReplyFilterInterface {

    private List<String> identList= new ArrayList<>();
    
    
    @Override
    public Boolean getPropertyFilterActive(String aPropertyFilterName) {
        return identList.contains(aPropertyFilterName);
    }

    @Override
    public String getPropertyFilterResult(String aPropertyFilterName) {
        return "Response from " + aPropertyFilterName;
    }
    
}
