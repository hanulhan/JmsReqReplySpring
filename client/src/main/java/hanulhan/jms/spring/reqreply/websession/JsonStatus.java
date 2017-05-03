/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hanulhan.jms.spring.reqreply.websession;

/**
 *
 * @author UHansen
 */
public class JsonStatus {

    public static final String JSON_ERROR = "ERROR";
    public static final String JSON_OK = "OK";

    private String status;
    private String errorMsg;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public JsonStatus() {
        super();
        status = JSON_OK;
    }
}
