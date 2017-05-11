/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hanulhan.jms.spring.reqreply.util;

/**
 *
 * @author UHansen
 */
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.joda.time.DateTime;
import org.joda.time.Seconds;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

public final class DateConverter {

    public static final String DATEFORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DATEFORMAT_SHORT = "HH:mm:ss";

    private DateConverter() {

    }

    /**
     * gregorianCalendar conversion
     *
     * @param gregorianCalendar
     * @return
     */
    public static XMLGregorianCalendar createXmlGregorianCalendar(
            GregorianCalendar gregorianCalendar) {
        XMLGregorianCalendar xMLGregorianCalendar = null;

        try {
            xMLGregorianCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);
        } catch (DatatypeConfigurationException ex) {
            Logger.getLogger(DateConverter.class.getName()).log(Level.SEVERE, null, ex);
        }

        return xMLGregorianCalendar;
    }

    /**
     * xMLGregorianCalendar to be converted to GregorianCalendar
     *
     * @param xMLGregorianCalendar
     * @return
     */
    public static GregorianCalendar createCalendar(
            XMLGregorianCalendar xMLGregorianCalendar) {
        return xMLGregorianCalendar.toGregorianCalendar();
    }

    /*
     * Convert the date to Calendar.    
     */
    public static Calendar createCalendar(Date date) {

        Calendar calendar = GregorianCalendar
                .getInstance();
        calendar.setTime(date);

        return calendar;
    }

    public static Date GetUTCDateTimeFromDate(Date aDate, int AOffset) {
        Calendar newCal = Calendar.getInstance();
        newCal.setTime(aDate);
        newCal.add(Calendar.MINUTE, AOffset * -1);
        return newCal.getTime();
    }

    public static Date StringDateToDate(String StrDate) {
        Date dateToReturn = null;
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATEFORMAT);
        try {
            dateToReturn = (Date) dateFormat.parse(StrDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateToReturn;
    }

    public static Date GetUTCdatetimeAsDate(XMLGregorianCalendar dateTime) {
        final SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        final String utcTime = sdf.format(dateTime.toGregorianCalendar().getTime());
        return StringDateToDate(utcTime);
    }

    public static Date GetXMLGregorianCalendarAsDate(XMLGregorianCalendar dateTime) {
        final SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT);
        final String utcTime = sdf.format(dateTime.toGregorianCalendar().getTime());
        return StringDateToDate(utcTime);
    }

    public static int elapsedSeconds(Date lastAliveMessage) {
        return Seconds.secondsBetween(new DateTime(lastAliveMessage), new DateTime()).getSeconds();
    }

    public static long elapsedMilliSeconds(GregorianCalendar gregory)    {
        GregorianCalendar gcal= new GregorianCalendar();
        gcal.setTime(new Date());
        return gcal.getTimeInMillis() - gregory.getTimeInMillis();
    }
    
//    public static String getHumanReadableDaysElapsed(Date date) {
//        PeriodFormatter daysHoursMinutes = new PeriodFormatterBuilder()
//                .printZeroNever()
//                .appendDays()
//                .appendSuffix(" d", " d")
//                .appendSeparator(", ")
//                .appendHours()
//                .appendSuffix(" h", " h")
//                .appendSeparator(", ")
//                .appendMinutes()
//                .appendSuffix("  min.", " min.")
//                .toFormatter();
//
//        Period period = new Period(date.getTime(), DateConverter.GetUTCdatetimeAsDate().getTime(), PeriodType.dayTime());
//        return daysHoursMinutes.print(period);
//    }

//    public static String getHumanReadableDaysElapsedNoUTC(Date date) {
//        PeriodFormatter daysHoursMinutes = new PeriodFormatterBuilder()
//                .printZeroNever()
//                .appendDays()
//                .appendSuffix(" d", " d")
//                .appendSeparator(", ")
//                .appendHours()
//                .appendSuffix(" h", " h")
//                .appendSeparator(", ")
//                .appendMinutes()
//                .appendSuffix("  min.", " min.")
//                .toFormatter();
//
//        Period period = new Period(date.getTime(), new Date().getTime(), PeriodType.dayTime());
//        return daysHoursMinutes.print(period);
//    }
}
