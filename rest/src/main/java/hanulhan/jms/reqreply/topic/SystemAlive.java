/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hanulhan.jms.reqreply.topic;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PUT;
import javax.ws.rs.core.MediaType;
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
@Path("/rest/systemalive")
@Component
public class SystemAlive implements ApplicationContextAware {

    ApplicationContext applicationContext;
    private static final Logger LOGGER = Logger.getLogger(SystemAlive.class);    

    @Context
    private UriInfo context;
    
    @Context
    HttpServletResponse resp;

    
    /**
     * Creates a new instance of SystemAlive
     */
    public SystemAlive() {
    }

    /**
     * Retrieves representation of an instance of hanulhan.jms.reqreply.topic.SystemAlive
     * @return an instance of java.lang.String
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public String getJson() {
        //TODO return proper representation object
        throw new UnsupportedOperationException();
    }


    @Override
    public void setApplicationContext(ApplicationContext ac) throws BeansException {
        this.applicationContext= ac;
    }
}
