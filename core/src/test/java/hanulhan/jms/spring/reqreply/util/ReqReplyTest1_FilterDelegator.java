/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hanulhan.jms.spring.reqreply.util;

import hanulhan.jms.spring.reqreply.util.ReqReplyFilterInterface;
import static java.lang.Thread.sleep;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author uhansen
 */
public class ReqReplyTest1_FilterDelegator implements ReqReplyFilterInterface {

    private List<String> identList= new ArrayList<>();

    public ReqReplyTest1_FilterDelegator() {
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

    
    public void requestPropertyFilterResult(String afilterValue, String aMessageId) {
        
    }
    
    @Override
    public String getPropertyFilterResult(String aPropertyFilterValue) {
        String myMsg= "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

        String retValue= new String();
        for (int i=0; i< 2; i++)   {
            retValue+= myMsg;
        }
//        try {
//            sleep(200);
//        } catch (Exception e) {
//            return myMsg;
//        }
        return retValue;
    }

    public void setIdentList(List<String> identList) {
        this.identList = identList;
    }
 
    
}
