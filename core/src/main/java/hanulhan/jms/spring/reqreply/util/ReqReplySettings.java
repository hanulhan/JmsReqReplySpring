/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hanulhan.jms.spring.reqreply.util;

/**
 *
 * @author uhansen
 */
public class ReqReplySettings {
    public static final String PROPERTY_NAME_COUNT = "count";
    public static final String PROPERTY_NAME_TOTAL_COUNT = "toTalcount";
    public static final String PROPERTY_NAME_IDENT = "systemIdent";
    private String filterPropertyName;

    public String getFilterPropertyName() {
        return filterPropertyName;
    }

    public void setFilterPropertyName(String filterPropertyName) {
        this.filterPropertyName = filterPropertyName;
    }
    
}
