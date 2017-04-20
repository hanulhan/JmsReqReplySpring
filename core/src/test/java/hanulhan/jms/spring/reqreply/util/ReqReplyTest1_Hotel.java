/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hanulhan.jms.spring.reqreply.util;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

/**
 *
 * @author uhansen
 */
public class ReqReplyTest1_Hotel implements ApplicationEventPublisherAware {

    private ApplicationEventPublisher publisher;
    
    /**
     *
     * @param aep
     */
    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher aep) {
        this.publisher= aep;
    }

    /**
     *
     */
    public void publish() {
        ReqReplyTest_Event myEvent= new ReqReplyTest_Event(this);
        publisher.publishEvent(myEvent);
    }
}
