/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hanulhan.jms.spring.reqreply.util;

import hanulhan.jms.spring.reqreply.util.ReqReplyFilterInterface;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

/**
 *
 * @author uhansen
 */
public class ReqReplyTest2_FilterDelegator implements ReqReplyFilterInterface {

    private ReqReplyTest2_HotelList hotelList;

    @Autowired
    ApplicationContext applicationContext;
    
    /**
     *
     */
    public ReqReplyTest2_FilterDelegator() {
        super();
    }
    
    
    
    @Override
    public Boolean getPropertyFilterActive(String aPropertyFilterName) {
        hotelList= (ReqReplyTest2_HotelList)applicationContext.getBean("bean_HotelList");

        if (hotelList.getFilterPropertyActive(aPropertyFilterName))    {
            return(true);
        }
        
        return false;
    }

    @Override
    public String getPropertyFilterResult(String aPropertyFilterName) {

        hotelList= (ReqReplyTest2_HotelList)applicationContext.getBean("bean_HotelList");
        
        return hotelList.getFilterPropertyResult(aPropertyFilterName);
    }

 
    
}
