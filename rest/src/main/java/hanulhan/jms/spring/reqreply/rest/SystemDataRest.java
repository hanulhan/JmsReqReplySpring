/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hanulhan.jms.spring.reqreply.rest;

import hanulhan.jms.spring.reqreply.beans.ReqReplyConsumer;
import hanulhan.jms.spring.reqreply.util.ReqReply;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Produces;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.POST;
import javax.ws.rs.core.Response;
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
@Path("/rest/basic/systemdata")
@Component
public class SystemDataRest implements ApplicationContextAware {

    // injected stuff
    private ApplicationContext applicationContext;
    private ReqReplyConsumer reqReplyConsumer;

    // internal
    private static final Logger LOGGER = Logger.getLogger(SystemDataRest.class);

    @Context
    private UriInfo context;

    @Context
    HttpServletResponse resp;

    /**
     * Creates a new instance of SystemDataRest
     */
    public SystemDataRest() {
    }

    /**
     * Retrieves representation of an instance of
     * hanulhan.jms.spring.reqreply.rest.SystemDataRest
     *
     * @param aReqReply
     * @return an instance of java.lang.String
     */
    @POST
    @Produces(MediaType.TEXT_HTML)
    @Consumes({"application/xml"})
    public Response SystemData(ReqReply aReqReply) {
        Response myResponse = Response.status(Response.Status.BAD_REQUEST).build();        

        if ((aReqReply != null) && (aReqReply.getRequest() != null)) {
            LOGGER.log(Level.DEBUG, "SystemData Response to: " + aReqReply.getRequest());
            reqReplyConsumer.sendResponse(aReqReply.getIdent(), aReqReply.getResponse(), aReqReply.getMessageid());

            myResponse = Response.status(Response.Status.OK).build();
        } else {
            LOGGER.log(Level.DEBUG, "SystemData return BAD_REQUEST");
        }
        return myResponse;
    }
    
    
    
    public void setReqReplyConsumer(ReqReplyConsumer reqReplyConsumer) {
        this.reqReplyConsumer = reqReplyConsumer;
    }

    @Override
    public void setApplicationContext(ApplicationContext ac) throws BeansException {
        this.applicationContext = ac;
    }
}
