/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hanulhan.jms.spring.websession.sim;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author uhansen
 */
public class WebSimulationMonitor extends Thread {

    private boolean active = false;
    private static final Logger LOGGER = Logger.getLogger(WebSimulationMonitor.class);
    List<ClientWebSession> clientList;
    private int totalRequestCount;
    private int totalErrorCount;
    private long avgResponseTime;
    private long startTime;

    public WebSimulationMonitor(List<ClientWebSession> aClientSimList) {
        super();
        this.clientList = aClientSimList;
        startTime= new Date().getTime();
    }

    public void terminate() {
        this.active = false;
    }

    @Override
    @SuppressWarnings("SleepWhileInLoop")
    public void run() {
        this.active = true;
        long milliSeconds;
        while (active) {
            try {
                Thread.sleep(15000);
                totalRequestCount = 0;
                totalErrorCount = 0;
                avgResponseTime = 0;
                int timeoutCount = 0;
                int activeSessions = 0;
                for (ClientWebSession temp : clientList) {
                    totalRequestCount += temp.getRequestQuantity();
                    totalErrorCount += temp.getErrorQuantity();
                    timeoutCount += temp.getTimeoutQuantity();
                    avgResponseTime += temp.getAvgResponseTime();
                    
                    if (!temp.isTerminated()) {
                        activeSessions++;
                    }

                }
                avgResponseTime= avgResponseTime / clientList.size();
                milliSeconds= new Date().getTime() - startTime;
                
                LOGGER.log(Level.INFO, "####################################################");
                long myHours = TimeUnit.MILLISECONDS.toHours(milliSeconds);
                long myMinutes= TimeUnit.MILLISECONDS.toMinutes(milliSeconds) - TimeUnit.HOURS.toMinutes(myHours);
                long mySeconds= TimeUnit.MILLISECONDS.toSeconds(milliSeconds) - TimeUnit.HOURS.toSeconds(myHours) - TimeUnit.MINUTES.toSeconds(myMinutes);
                
                String myTime;
                if (myHours > 0)    {
                    myTime= String.format("%d h, %d min, %d s", myHours, myMinutes, mySeconds);
                } else if (myMinutes > 0)   {
                    myTime= String.format("%d min, %d s", myMinutes, mySeconds);
                } else {
                    myTime= String.format("%d s", mySeconds);
                }
                
                LOGGER.log(Level.INFO, "Run time             : " + myTime);
                LOGGER.log(Level.INFO, "No of active Sessions: " + activeSessions);
                LOGGER.log(Level.INFO, "No of total Request:   " + totalRequestCount);
                LOGGER.log(Level.INFO, "No of total Errors:    " + totalErrorCount);
                LOGGER.log(Level.INFO, "No of timeout Errors:  " + timeoutCount);
                
                LOGGER.log(Level.INFO, "Avg. Response time:    " + avgResponseTime + "ms");
                if (totalRequestCount > 0 ) {
                    LOGGER.log(Level.INFO, "Error rate:            " + (totalErrorCount * 100) / totalRequestCount + "%");
                }
                LOGGER.log(Level.INFO, "Press x + <Enter> to terminate the Server\n");

            } catch (InterruptedException ex) {
                LOGGER.log(Level.ERROR, ex);
            }
        }
    }
}
