/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hanulhan.jms.spring.reqreply.jaxb;

import hanulhan.jms.spring.reqreply.jaxb.generated.ReqReply;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.apache.activemq.util.ByteArrayInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author uhansen
 */
public class MessageConverter implements Convertable<ReqReply> {
 	private static final Logger LOGGER = LoggerFactory.getLogger(MessageConverter.class);

	private final JAXBContext jaxbContext;
	private final Marshaller jaxbMarshaller;
	private final Unmarshaller jaxbUnmarshaller;

	public MessageConverter() throws JAXBException {
		jaxbContext = JAXBContext.newInstance(ReqReply.class);
		jaxbMarshaller = jaxbContext.createMarshaller();
		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		jaxbUnmarshaller = jaxbContext.createUnmarshaller();
	}

	@Override
	public String marshal(ReqReply object) {
		OutputStream stream = new ByteArrayOutputStream();
		try {
			jaxbMarshaller.marshal(object, stream);
		} catch (JAXBException e) {
			LOGGER.error("Exception occured while marshalling", e);
		}
		return stream.toString();
	}

	@Override
	public ReqReply unmarshal(String objectAsString) {
		try {
			return (ReqReply) jaxbUnmarshaller.unmarshal(new ByteArrayInputStream(objectAsString.getBytes()));
		} catch (JAXBException e) {
			LOGGER.error("Exception occured while marshalling", e);
		}
		return null;
	}

   
}
