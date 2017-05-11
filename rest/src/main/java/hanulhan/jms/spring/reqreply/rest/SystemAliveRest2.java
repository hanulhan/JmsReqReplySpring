/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hanulhan.jms.spring.reqreply.rest;

import com.sun.jersey.api.client.ClientResponse;
import hanulhan.jms.spring.reqreply.beans.ReqReplyConsumer;
import hanulhan.jms.spring.reqreply.util.*;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * REST Web Service
 *
 * @author UHansen
 */
@Path("/rest/systemalive2")
@Component
public class SystemAliveRest2 implements ApplicationContextAware {

    // injected stuff
    private ApplicationContext applicationContext;
    private ReqReplyConsumer reqReplyConsumer;
    private long holdTimeSec;

    // internal
    private static final Logger LOGGER = Logger.getLogger(SystemAliveRest.class);

    @Context
    private UriInfo context;
    private String ident;

    @Context
    HttpServletResponse resp;

    /**
     * Creates a new instance of SystemAlive
     */
    public SystemAliveRest2() {
    }


    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public void SystemAlive2(@QueryParam("ident") String ident) {
        RequestObject myRequestObj;
        GregorianCalendar gregory = new GregorianCalendar();
        gregory.setTime(new Date());
        myRequestObj = new RequestObject(ident);

        if (ident == null) {
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
        
        LOGGER.log(Level.DEBUG, "System [" + ident + "] call Rest service. Total connection: " +  reqReplyConsumer.getQuantityConnected());

        if (reqReplyConsumer.ConnectSystem(myRequestObj)) {
            synchronized (myRequestObj) {

                LOGGER.log(Level.TRACE, "System [" + ident + "] waits " + holdTimeSec + "s for notify");
                
                try {
                    myRequestObj.wait(holdTimeSec * 1000);
                } catch (InterruptedException ex) {
                    LOGGER.log(Level.ERROR, ex);
                    throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);

                }
                if (myRequestObj.isBusy()) {
                    LOGGER.log(Level.DEBUG, "System [" + ident + "] disconnect with Response: " + myRequestObj.toString());
                } else {

                    LOGGER.log(Level.DEBUG, "System [" + ident + "] disconnect and returns: " + Status.NO_CONTENT);
                    throw new WebApplicationException(204);
                }

            }
        }
        reqReplyConsumer.DisconnectSystem(ident);

    }

    public void setReqReplyConsumer(ReqReplyConsumer reqReplyConsumer) {
        this.reqReplyConsumer = reqReplyConsumer;
    }

    public void setIdent(String ident) {
        this.ident = ident;
    }

    public void setHoldTimeSec(long holdTimeSec) {
        this.holdTimeSec = holdTimeSec;
    }

    @Override
    public void setApplicationContext(ApplicationContext ac) throws BeansException {
        this.applicationContext = ac;
    }
}
