/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hanulhan.jms.spring.reqreply.jaxb;

/**
 *
 * @author uhansen
 */
public interface Convertable<T> {

    String marshal(T object);

    T unmarshal(String objectAsString);
}
