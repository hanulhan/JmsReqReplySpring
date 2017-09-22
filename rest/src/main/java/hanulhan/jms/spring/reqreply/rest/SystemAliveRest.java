/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hanulhan.jms.spring.reqreply.rest;

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
@Path("/rest/basic/systemalive")
@Component
public class SystemAliveRest implements ApplicationContextAware {

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
    public SystemAliveRest() {
    }

    /**
     * Retrieves representation of an instance of
     * hanulhan.jms.spring.reqreply.topic.SystemAlive
     *
     * @param ident
     * @return an instance of java.lang.String
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response SystemAlive(@QueryParam("ident") String ident) {
        RequestObject myRequestObj;
        GregorianCalendar gregory = new GregorianCalendar();
        Response myResponse = Response.status(Status.BAD_REQUEST).build();
        gregory.setTime(new Date());
        myRequestObj = new RequestObject(ident);

        if (ident == null) {
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }

        if (reqReplyConsumer.ConnectSystem(myRequestObj)) {
            LOGGER.log(Level.INFO, "System [" + ident + "] connected. Total connections: " + reqReplyConsumer.getQuantityConnected());
            synchronized (myRequestObj) {

                LOGGER.log(Level.TRACE, "System [" + ident + "] waits " + holdTimeSec + "s for notify");
                try {
                    myRequestObj.wait(holdTimeSec * 1000);
                    if (myRequestObj.isBusy()) {
                        LOGGER.log(Level.TRACE, "System [" + ident + "] disconnect with Response: " + myRequestObj.toString());
                        myResponse = Response.ok(myRequestObj.getMessageObj()).build();
                    } else {
                        myResponse = Response.status(Status.NO_CONTENT).build();
                    }
                } catch (InterruptedException ex) {
                    myResponse = Response.status(Status.INTERNAL_SERVER_ERROR).build();
                    LOGGER.log(Level.ERROR, ex);
                } finally {
                    reqReplyConsumer.DisconnectSystem(ident);
                }
                LOGGER.log(Level.INFO, "System [" + ident + "] close connection with Status " + myResponse.getStatus() + " disconnected. Total connections: " + reqReplyConsumer.getQuantityConnected());
                return myResponse;
            }
        }
        return Response.status(Status.BAD_REQUEST).build();
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
