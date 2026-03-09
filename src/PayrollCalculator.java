import java.time.*;
import java.time.format.*;
import java.util.*;

public class PayrollCalculator {


    public static double computeHours(String logIn, String logOut) {
        try {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("H:mm");
            LocalTime start = LocalTime.parse(logIn.trim(), fmt);
            LocalTime end   = LocalTime.parse(logOut.trim(), fmt);

            LocalTime officialStart  = LocalTime.of(8, 0);
            LocalTime gracePeriodEnd = LocalTime.of(8, 5);
            LocalTime officialEnd    = LocalTime.of(17, 0);


            if (start.isBefore(officialStart)) {
                start = officialStart;
            }

            if (!start.isAfter(gracePeriodEnd)) {
                start = officialStart;
            }

            if (end.isAfter(officialEnd)) {
                end = officialEnd;
            }

            if (!end.isAfter(start)) {
                return 0;
            }

            return Duration.between(start, end).toMinutes() / 60.0;

        } catch (Exception e) {
            return 0;
        }
    }


    public static List<String> getAvailableMonths(Employee emp) {
        Set<String> months = new TreeSet<>();
        for (AttendanceRecord rec : emp.getAttendance()) {
            String ym = rec.getYearMonth();
            if (!ym.equals("Unknown")) months.add(ym);
        }
        return new ArrayList<>(months);
    }


    public static double computeHoursWorked(Employee emp, String yearMonth, int cutoff) {
        double total = 0;
        for (AttendanceRecord rec : emp.getAttendance()) {
            if (rec.getYearMonth().equals(yearMonth) && rec.getCutoff() == cutoff) {
                total += computeHours(rec.getLogIn(), rec.getLogOut());
            }
        }
        return total;
    }


    public static double computeGross(Employee emp, String yearMonth, int cutoff) {
        return computeHoursWorked(emp, yearMonth, cutoff) * emp.getHourlyRate();
    }


    public static double computeNet(Employee emp, String yearMonth, int cutoff) {
        double gross = computeGross(emp, yearMonth, cutoff);
        if (cutoff == 2) {
            double gross1        = computeGross(emp, yearMonth, 1);
            double combinedGross = gross1 + gross;
            return combinedGross - emp.getTotalDeductions();
        }
        return gross;
    }


    public static String getCutoffLabel(Employee emp, String yearMonth, int cutoff) {
        for (AttendanceRecord rec : emp.getAttendance()) {
            if (rec.getYearMonth().equals(yearMonth) && rec.getCutoff() == cutoff) {
                return rec.getCutoffLabel();
            }
        }

        try {
            String[] parts = yearMonth.split("-");
            int year  = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            LocalDate d = LocalDate.of(year, month, cutoff == 1 ? 1 : 16);
            String monthName = d.getMonth().getDisplayName(
                    java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH);
            int lastDay = d.withDayOfMonth(d.lengthOfMonth()).getDayOfMonth();
            if (cutoff == 1) return monthName + " 1 to " + monthName + " 15, " + year;
            else             return monthName + " 16 to " + monthName + " " + lastDay + ", " + year;
        } catch (Exception e) {
            return yearMonth + " Cutoff " + cutoff;
        }
    }


    public static String getLatestMonth(Map<String, Employee> employees) {
        String latest = "";
        for (Employee emp : employees.values()) {
            for (String ym : getAvailableMonths(emp)) {
                if (ym.compareTo(latest) > 0) latest = ym;
            }
        }
        return latest;
    }
}
