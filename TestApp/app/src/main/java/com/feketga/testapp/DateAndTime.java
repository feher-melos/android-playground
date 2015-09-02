package com.feketga.testapp;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class DateAndTime {
    public static String testCalendar() {
        long nowMillisSys = System.currentTimeMillis();
        Calendar nowCalGmt = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        Calendar nowCalLocal = Calendar.getInstance();

        Calendar calLocalFromSys = Calendar.getInstance();
        calLocalFromSys.setTimeInMillis(nowMillisSys);

        Calendar calGmtFromSys = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        calGmtFromSys.setTimeInMillis(nowMillisSys);

        Calendar calLocalMidnight = (Calendar) nowCalLocal.clone();
        calLocalMidnight.clear(Calendar.HOUR);
        calLocalMidnight.clear(Calendar.MINUTE);
        calLocalMidnight.clear(Calendar.SECOND);

        Calendar calLocalMidnight2 = (Calendar) nowCalLocal.clone();
        calLocalMidnight2.set(Calendar.HOUR, 0);
        calLocalMidnight2.set(Calendar.MINUTE, 0);
        calLocalMidnight2.set(Calendar.SECOND, 0);

        Calendar calLocalMidnight3 = (Calendar) nowCalLocal.clone();
        calLocalMidnight3.set(Calendar.HOUR, 0);
        calLocalMidnight3.set(Calendar.MINUTE, 0);
        calLocalMidnight3.set(Calendar.SECOND, 0);
        calLocalMidnight3.set(Calendar.DAY_OF_MONTH, 1);
        calLocalMidnight3.add(Calendar.MONTH, -1);

        return new StringBuilder()
                .append(dump(nowMillisSys, nowCalGmt, "nowCalGmt"))
                .append(dump(nowMillisSys, nowCalLocal, "nowCalLocal"))
                .append(dump(nowMillisSys, calLocalFromSys, "calLocalFromSys"))
                .append(dump(nowMillisSys, calGmtFromSys, "calGmtFromSys"))
                .append(dump(nowMillisSys, calLocalMidnight, "calLocalMidnight"))
                .append(dump(nowMillisSys, calLocalMidnight2, "calLocalMidnight2"))
                .append(dump(nowMillisSys, calLocalMidnight3, "calLocalMidnight3"))
                .toString();
    }

    private static String dump(long sysmillis, Calendar cal, String msg) {
        return new StringBuilder()
                .append(msg)
                .append(": timemillis: ").append(cal.getTimeInMillis())
                .append(", sysdiff ms: ").append(cal.getTimeInMillis() - sysmillis)
                .append(", sysdiff min: ").append((cal.getTimeInMillis() - sysmillis)/1000.0/60.0)
                .append(", sysdiff h: ").append((cal.getTimeInMillis() - sysmillis)/1000.0/60.0/60.0)
                .append(", month: ").append(cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()))
                .append(", day: ").append(cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault()))
                .append("\n\n").toString();
    }

    public static String testCalendarMonth() {
        //long t1 = 1432713687000L;
        //long t2 = 1432297100000L;
        Calendar today = Calendar.getInstance();

        Calendar may1 = (Calendar) Calendar.getInstance().clone();
        long t1 = 1432713687000L;
        may1.setTimeInMillis(t1);
        may1.set(Calendar.HOUR_OF_DAY, 0);
        may1.set(Calendar.MINUTE, 0);
        may1.set(Calendar.SECOND, 0);
        may1.set(Calendar.MILLISECOND, 0);
        may1.set(Calendar.DAY_OF_MONTH, 1);

        Calendar may2 = (Calendar) Calendar.getInstance().clone();
        long t2 = 1432297100000L;
        may2.setTimeInMillis(t2);
        may2.set(Calendar.HOUR_OF_DAY, 0);
        may2.set(Calendar.MINUTE, 0);
        may2.set(Calendar.SECOND, 0);
        may2.set(Calendar.MILLISECOND, 0);
        may2.set(Calendar.DAY_OF_MONTH, 1);

        long h = may1.getTimeInMillis() - may2.getTimeInMillis();

        return new StringBuilder()
                .append(dumpDate(today, "today"))
                .append(dumpDate(may1, "may1"))
                .append(dumpDate(may2, "may2"))
                .append(" " + (may1.getTimeInMillis() - may2.getTimeInMillis()))
                .toString();
    }

    private static String dumpDate(Calendar cal, String msg) {
        return new StringBuilder()
                .append(msg)
                .append(" ").append(cal.getTimeInMillis())
                .append(" ").append(cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()))
                .append(" ").append(cal.get(Calendar.DAY_OF_MONTH))
                .append(" h").append(cal.get(Calendar.HOUR))
                .append(" m").append(cal.get(Calendar.MINUTE))
                .append(" s").append(cal.get(Calendar.SECOND))
                .append(" ms").append(cal.get(Calendar.MILLISECOND))
                .append(" ").append(cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault()))
                .append("\n\n").toString();
    }

}
