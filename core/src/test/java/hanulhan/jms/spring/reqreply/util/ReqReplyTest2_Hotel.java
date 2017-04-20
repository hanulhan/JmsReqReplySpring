/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hanulhan.jms.spring.reqreply.util;

/**
 *
 * @author acentic
 */
public class ReqReplyTest2_Hotel {

    private String systemIdent;
    private String response;
    private int responseCount;

    /**
     *
     * @param systemIdent
     * @param response
     */
    public ReqReplyTest2_Hotel(String systemIdent, String response) {
        this.systemIdent = systemIdent;
        this.response = response;
        responseCount = 0;
    }

    /**
     *
     * @return
     */
    public String getSystemIdent() {
        return systemIdent;
    }

    /**
     *
     * @param systemIdent
     */
    public void setSystemIdent(String systemIdent) {
        this.systemIdent = systemIdent;
    }

    /**
     *
     * @return
     */
    public String getResponse() {
        this.responseCount++;
        return response;
    }
    
    /**
     *
     * @param aMessage
     * @return
     */
    public Boolean compareResponse(String aMessage) {
        return response.equals(aMessage);
    }

    /**
     *
     * @param response
     */
    public void setResponse(String response) {
        this.response = response;
    }

    /**
     *
     * @return
     */
    public int getResponseCount() {
        return responseCount;
    }

}
