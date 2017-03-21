/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hanulhan.jms.spring.reqreply.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.context.ApplicationContext;

/**
 *
 * @author uhansen
 */
public class ReqReplyStressTestHotelList {

    private Map<String, ReqReplyStressTestHotel> hotelList = new HashMap<>();

    public ReqReplyStressTestHotelList() {
        super();
    }

    String getFilterPropertyResult(String aPropertyFilterName) {

        if (hotelList.containsKey(aPropertyFilterName)) {
            return hotelList.get(aPropertyFilterName).getResponse();
        }
        return null;
    }

    boolean getFilterPropertyActive(String aPropertyFilterName) {
        return hotelList.containsKey(aPropertyFilterName);
    }

    public void add(ReqReplyStressTestHotel aHotel) {
        hotelList.put(aHotel.getSystemIdent(), aHotel);
    }

}
