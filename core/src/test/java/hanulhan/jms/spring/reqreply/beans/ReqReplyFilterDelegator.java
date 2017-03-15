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
public class ReqReplyFilterDelegator implements ReqReplyFilterInterface {

    private List<String> identList= new ArrayList<>();

    public ReqReplyFilterDelegator() {
        super();
    }
    
    
    
    @Override
    public Boolean getPropertyFilterActive(String aPropertyFilterValue) {
//        return identList.contains(aPropertyFilterValue);
        for (String temp: identList)    {
            if (temp.equals(aPropertyFilterValue))  {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getPropertyFilterResult(String aPropertyFilterValue) {
        String myMg= "0123456789";
        String retValue= new String();
        for (int i=0; i< 10; i++)   {
            retValue+= myMg;
        }
        return retValue;
    }

    public void setIdentList(List<String> identList) {
        this.identList = identList;
    }
 
    
}
