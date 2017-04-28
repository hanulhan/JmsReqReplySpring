/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hanulhan.jms.spring.reqreply.client;

import com.opensymphony.xwork2.ActionSupport;
import hanulhan.jms.spring.reqreply.beans.ReqReplyProducer;
import javax.jms.JMSException;
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

    // Action input/output
    private int clientId;
    private String ident;
    String msgText;
    String msgResponse;
    private String jsonStatus;

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
        jsonStatus = JSON_OK;
        LOGGER.log(Level.TRACE, "JmsReqReplyActions.doSendMessage()");
        reqReplyProducer= (ReqReplyProducer)applicationContext.getBean("bean_vmReqReplyProducer");
        
        msgText = "Message " + msgCount + " from Client " + clientId;

        msgResponse = reqReplyProducer.getResponse("Hallo", "AAA", 2000);

        return SUCCESS;
    }

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public String getIdent() {
        return ident;
    }

    public void setIdent(String ident) {
        this.ident = ident;
    }

    public ReqReplyProducer getReqReplyProducer() {
        return reqReplyProducer;
    }

    public void setReqReplyProducer(ReqReplyProducer reqReplyProducer) {
        this.reqReplyProducer = reqReplyProducer;
    }

    public String getMsgText() {
        return msgText;
    }

    public void setMsgText(String msgText) {
        this.msgText = msgText;
    }

    public String getMsgResponse() {
        return msgResponse;
    }

    public void setMsgResponse(String msgResponse) {
        this.msgResponse = msgResponse;
    }

    @Override
    public void setApplicationContext(ApplicationContext ac) throws BeansException {
        this.applicationContext = ac;

    }

}
