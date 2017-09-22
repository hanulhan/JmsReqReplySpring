/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hanulhan.jms.spring.reqreply.websession;

import com.opensymphony.xwork2.ActionSupport;
import hanulhan.jms.spring.reqreply.beans.ReqReplyProducer;
import hanulhan.jms.spring.reqreply.message.ReqReplyMessageContainer;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 *
 * @author uhansen
 */
public class JmsReqReplyActions extends ActionSupport implements ApplicationContextAware {

    // Injected stuff
    private ApplicationContext applicationContext;
    private ReqReplyProducer reqReplyProducer;
    private int initialTimeoutSec;

    // Action input/output
    private int clientId;
    private JsonStatus jsonStatus;
    private String ident, request, response, timeout, port, command, duration;

    // Internal
    private static final Logger LOGGER = Logger.getLogger(JmsReqReplyActions.class);
    private static final String JSON_OK = "OK";
    private static final String JSON_ERROR = "ERR";
    private static final String JSON_ERROR_CONFIG = "ERR_CONFIG";

    static int msgCount = 0;

    public JmsReqReplyActions() {
        super();
        msgCount++;
    }

    public String doSetClientId() {
        LOGGER.log(Level.TRACE, "JmsReqReplyActions.doSetClientId()");

        return SUCCESS;
    }


    public String doSendMessage() throws InterruptedException {
        ReqReplyMessageContainer myMsgObj;
        jsonStatus = new JsonStatus();
        LOGGER.log(Level.TRACE, "JmsReqReplyActions.doSendMessage()");
        reqReplyProducer = (ReqReplyProducer) applicationContext.getBean("bean_vmReqReplyProducer");

        LOGGER.log(Level.DEBUG, "Request: " + request + ", Ident: " + ident + ", Timeout: " + timeout + "s");

        myMsgObj = reqReplyProducer.getResponseObj(request, command, Integer.parseInt(port), ident, 1000 * Long.parseLong(timeout));
        response= myMsgObj.getResponse();
        duration= String.valueOf(myMsgObj.getResponseTime());
        
        return SUCCESS;
    }

    public String doGetTimeout() throws InterruptedException {
        jsonStatus = new JsonStatus();
        timeout = "" + (initialTimeoutSec);

        return SUCCESS;
    }

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public ReqReplyProducer getReqReplyProducer() {
        return reqReplyProducer;
    }

    public void setReqReplyProducer(ReqReplyProducer reqReplyProducer) {
        this.reqReplyProducer = reqReplyProducer;
    }

    public JsonStatus getJsonStatus() {
        return jsonStatus;
    }

    public void setJsonStatus(JsonStatus jsonStatus) {
        this.jsonStatus = jsonStatus;
    }

    public String getIdent() {
        return ident;
    }

    public void setIdent(String ident) {
        this.ident = ident;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getTimeout() {
        return timeout;
    }

    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }

    public int getInitialTimeoutSec() {
        return initialTimeoutSec;
    }

    public void setInitialTimeoutSec(int initialTimeoutSec) {
        this.initialTimeoutSec = initialTimeoutSec;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }


    
    @Override
    public void setApplicationContext(ApplicationContext ac) throws BeansException {
        this.applicationContext = ac;

    }

}
