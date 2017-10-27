/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hanulhan.jms.spring.reqreply;

import hanulhan.jms.spring.reqreply.beans.ReqReplyProducer;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 *
 * @author uhansen
 */
public class EchoConsumer {

    // injected stuff
    private static int timeoutSec;
    private static String logpath;

    // internal stuff
    private static final Logger LOGGER = Logger.getLogger(EchoConsumer.class);

//    private static FileAppender fa;

    public EchoConsumer() {
    }

    public static void main(String[] args) {

        ApplicationContext context = new ClassPathXmlApplicationContext(new String[]{"applicationContext.xml"});
        Boolean terminate = false;

        Scanner keyboard = new Scanner(System.in);
        while (terminate == false) {
            LOGGER.log(Level.INFO, "Press x + <Enter> to terminate the Server");
            String input = keyboard.nextLine();
            if (input != null) {
                if ("x".equals(input)) {
                    terminate = true;
                }
            }
        }

        
        

        System.exit(0);
    }




    
}
