/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hanulhan.jms.spring.websession.sim;

import hanulhan.jms.spring.reqreply.beans.ReqReplyProducer;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 *
 * @author uhansen
 */
public class WebSimulation {

    // injected stuff
    private static int timeoutSec;
    private static List<String> identList;
    private static String logpath;

    // internal stuff
    private static int identQuantity;
    private static int startIndex, endIndex, sessionsPerIdent;
    private static final Logger LOGGER = Logger.getLogger(WebSimulation.class);
//    private static FileAppender fa;

    public WebSimulation() {
    }

    public static void main(String[] args) {

        ApplicationContext context = new ClassPathXmlApplicationContext(new String[]{"applicationContext.xml"});
        ReqReplyProducer reqReplyProducer;
        Boolean terminate = false;
        reqReplyProducer = (ReqReplyProducer) context.getBean("bean_vmReqReplyProducer");
        List<ClientWebSession> clientSimList = new ArrayList();
        int clientId;

        if (args.length > 0 && args[0].equals("?")) {
            LOGGER.log(Level.TRACE, "java -jar ClientWebSession <sessionsPerIdent, startIndex, endIndex | ?> ");
            System.exit(0);
        } else if (args.length > 2) {
            sessionsPerIdent   = Integer.parseInt(args[0]);
            startIndex = Integer.parseInt(args[1]);
            endIndex = Integer.parseInt(args[2]);

        } else {
            LOGGER.log(Level.ERROR, "java -jar ClientWebSession <sessionsPerIdent, startIndex, endIndex | ?> ");
            System.exit(0);
        }

        if (startIndex < 1) {
            LOGGER.log(Level.ERROR, "startIndex > 0");
            System.exit(0);
        }
        if (endIndex > 500) {
            LOGGER.log(Level.ERROR, "endIndex < 501");
            System.exit(0);
        }

//        fa = new FileAppender();
//        fa.setName("simClient");
//        fa.setFile(logpath + "/simClient-" + clientId + ".log");
//        fa.setLayout(new PatternLayout("%d %-5p %c.%M:%L - %m%n"));
//        fa.setThreshold(Level.DEBUG);
//        fa.setAppend(true);
//        fa.activateOptions();
//        Logger.getRootLogger().addAppender(fa);
        

        WebSimulation.identQuantity = (WebSimulation.endIndex - WebSimulation.startIndex + 1);
        LOGGER.log(Level.DEBUG, "identList.size()= " + identList.size());
        LOGGER.log(Level.DEBUG, "identQuantity= " + WebSimulation.identQuantity);

        for (int i = (WebSimulation.startIndex - 1); i < WebSimulation.endIndex; i++) {
            for (clientId= 0; clientId < sessionsPerIdent; clientId++)   {
                clientSimList.add(new ClientWebSession(reqReplyProducer, identList.get(i), (clientId + 1), timeoutSec));
            }
            
        }

        for (ClientWebSession temp: clientSimList)  {
            temp.start();
        }

        WebSimulationMonitor simMonitor = new WebSimulationMonitor(clientSimList);
        simMonitor.start();

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

        LOGGER.log(Level.INFO, "Waiting for termination...");

        if (terminate) {
            simMonitor.terminate();
            for (ClientWebSession temp : clientSimList) {
                temp.terminate();
            }
        }

        for (ClientWebSession temp : clientSimList) {
            while (!temp.isTerminated()) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    LOGGER.log(Level.ERROR, ex);
                }
            }
        }

        System.exit(0);
    }

    public void setTimeoutSec(int timeoutSec) {
        WebSimulation.timeoutSec = timeoutSec;
    }

    public void setIdentList(List<String> identList) {
        WebSimulation.identList = identList;
    }

    public void setIdentQuantity(int identQuantity) {
        WebSimulation.identQuantity = identQuantity;
    }

    public void setLogpath(String logpath) {
        WebSimulation.logpath = logpath;
    }

    
}
