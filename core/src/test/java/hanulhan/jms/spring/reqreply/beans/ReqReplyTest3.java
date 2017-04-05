/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hanulhan.jms.spring.reqreply.beans;

import hanulhan.jms.spring.reqreply.util.ReqReplyTest3_Object;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PreDestroy;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author uhansen
 */
@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations = {"/spring/springTest-2.xml"})
//public class ReqReplyTest3 implements ApplicationContextAware {
public class ReqReplyTest3 {

    static final Logger LOGGER = Logger.getLogger(ReqReplyTest3.class);
    private final Map<String, ReqReplyTest3_Object> identMap;

    public class System extends Thread {

        private final String ident;

        @PreDestroy
        public void destroyIt() {
            this.interrupt();
        }

        public System(String ident) {
            this.ident = ident;
        }

        private synchronized boolean handle(ReqReplyTest3_Object myObj) throws InterruptedException {
            put(ident, myObj);
            while (myObj.getRequest() == null) {
                myObj.wait(2000);
            }
            return myObj.getRequest() != null;
        }
        
        public void cancel() {
            interrupt();
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                LOGGER.log(Level.DEBUG, "System [" + ident + "] connected");
                ReqReplyTest3_Object myObj = new ReqReplyTest3_Object(ident);

                if (IsIdentInMap(ident)) {
                    LOGGER.log(Level.ERROR, "ERROR: Ident already in map");
                } else {
                    try {
                        if (handle(myObj))  {
                            LOGGER.log(Level.DEBUG, "Disconnect system [" + ident + "] with Response: " + myObj.toString());
                        } else {
                            LOGGER.log(Level.DEBUG, "Disconnect system [" + ident + "]");
                        }
                    } catch (InterruptedException ex) {
                        LOGGER.log(Level.DEBUG, ex);
                    }

                }
            }
        }
    }

    public ReqReplyTest3() {
        identMap = Collections.synchronizedMap(new HashMap<String, ReqReplyTest3_Object>());
    }

    public void put(String aIdent, ReqReplyTest3_Object aObj) {
        this.identMap.put(aIdent, aObj);
    }

    public void remove(String aIdent) {
        this.identMap.remove(aIdent);
    }

    public boolean IsIdentInMap(String aIdent) {
        return this.identMap.containsKey(aIdent);
    }

    @Test
    public void MyTest() {

    }
}
