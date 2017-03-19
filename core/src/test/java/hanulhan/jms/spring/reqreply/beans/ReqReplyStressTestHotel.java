/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hanulhan.jms.spring.reqreply.beans;

/**
 *
 * @author acentic
 */
public class ReqReplyStressTestHotel {

        private String systemIdent;
        private String response;
        private int reqCount;
        private int responseCount;

        public ReqReplyStressTestHotel(String systemIdent, String response) {
            this.systemIdent = systemIdent;
            this.response = response;
            reqCount = 0;
            responseCount = 0;
        }

        public String getSystemIdent() {
            return systemIdent;
        }

        public void setSystemIdent(String systemIdent) {
            this.systemIdent = systemIdent;
        }

        public String getResponse() {
            return response;
        }

        public void setResponse(String response) {
            this.response = response;
        }    
}
