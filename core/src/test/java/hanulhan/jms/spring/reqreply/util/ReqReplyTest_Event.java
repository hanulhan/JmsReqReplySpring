/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hanulhan.jms.spring.reqreply.util;

import org.springframework.context.ApplicationEvent;

/**
 *
 * @author uhansen
 */
public class ReqReplyTest_Event extends ApplicationEvent {
    
    /**
     *
     * @param source
     */
    public ReqReplyTest_Event(Object source) {
        super(source);
    }
    
}
