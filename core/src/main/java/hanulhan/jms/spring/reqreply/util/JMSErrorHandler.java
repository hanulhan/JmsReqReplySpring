/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hanulhan.jms.spring.reqreply.util;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.util.ErrorHandler;

/**
 *
 * @author uhansen
 */
public class JMSErrorHandler implements ErrorHandler    {

    private static final Logger LOGGER = Logger.getLogger(JMSErrorHandler.class);
    @Override
    public void handleError(Throwable thrwbl) {
        LOGGER.log(Level.ERROR, thrwbl);
    }
    
}
