import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class AttendanceRecord {

    private String date;
    private String logIn;
    private String logOut;
    private LocalDate parsedDate;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    public AttendanceRecord(String date, String logIn, String logOut) {
        this.date = date;
        this.logIn = logIn;
        this.logOut = logOut;
        try {
            this.parsedDate = LocalDate.parse(date, DATE_FORMATTER);
        } catch (Exception e) {
            this.parsedDate = null;
        }
    }

    public String getDate() { return date; }
    public String getLogIn() { return logIn; }
    public String getLogOut() { return logOut; }
    public LocalDate getParsedDate() { return parsedDate; }


    public int getCutoff() {
        if (parsedDate == null) return -1;
        return parsedDate.getDayOfMonth() <= 15 ? 1 : 2;
    }


    public String getYearMonth() {
        if (parsedDate == null) return "Unknown";
        return String.format("%04d-%02d", parsedDate.getYear(), parsedDate.getMonthValue());
    }


    public String getCutoffLabel() {
        if (parsedDate == null) return "Unknown";
        String monthName = parsedDate.getMonth().getDisplayName(
                java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH);
        int year = parsedDate.getYear();
        int lastDay = parsedDate.withDayOfMonth(parsedDate.lengthOfMonth()).getDayOfMonth();
        if (getCutoff() == 1) {
            return monthName + " 1 to " + monthName + " 15, " + year;
        } else {
            return monthName + " 16 to " + monthName + " " + lastDay + ", " + year;
        }
    }
}
