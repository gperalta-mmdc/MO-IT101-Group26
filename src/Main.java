/**
 * ============================================================
 * Program     : MotorPH Payroll System
 * File        : Main.java
 * Description : A console-based payroll system for MotorPH.
 *               This program loads employee profiles and
 *               attendance records from CSV files, handles
 *               user login, and computes payroll for two
 *               cutoff periods per month (1st-15th and
 *               16th to end of month). Deductions such as
 *               SSS, PhilHealth, Pag-IBIG, and Tax are
 *               applied on the second cutoff.
 *
 * How to run  : Compile with: javac Main.java
 *               Run with    : java Main
 *
 * CSV files required:
 *   data/employees.csv  - Employee profiles
 *   data/attendance.csv - Daily attendance log-in/out records
 * ============================================================
 */

import java.io.*;
import java.time.*;
import java.time.format.*;
import java.util.*;

public class Main {

    // ── ANSI COLOR CODES ──────────────────────────────────────
    // These are special text codes that add color in the terminal.
    // \u001B[ starts the code, the number sets the style or color,
    // and RESET clears all formatting back to normal.
    private static final String RED    = "\u001B[31m"; // Red text (used for errors)
    private static final String BOLD   = "\u001B[1m";  // Bold text
    private static final String CYAN   = "\u001B[36m"; // Cyan text (used for headers)
    private static final String YELLOW = "\u001B[33m"; // Yellow text (used for month labels)
    private static final String RESET  = "\u001B[0m";  // Resets color back to default

    // ── SEPARATOR LINES ───────────────────────────────────────
    // Reusable visual dividers for the console output.
    private static final String SEP  = "============================================================";
    private static final String DASH = "------------------------------------------------------------";

    // ── EMPLOYEE DATA ARRAYS ──────────────────────────────────
    // These parallel arrays store employee data loaded from the CSV.
    // Each index position (0, 1, 2, ...) represents one employee.
    private static String[] empIDs;        // Employee ID numbers
    private static String[] empNames;      // Full names
    private static String[] empBirthdays;  // Birthdays (display only)
    private static double[] empHourlyRates; // Hourly pay rate in PHP
    private static double[] empSSS;        // SSS monthly deduction
    private static double[] empPhilHealth; // PhilHealth monthly deduction
    private static double[] empPagIbig;    // Pag-IBIG monthly deduction
    private static double[] empTax;        // Withholding tax monthly deduction
    private static int empCount = 0;       // How many employees were loaded

    // ── ATTENDANCE DATA ARRAYS ────────────────────────────────
    // These parallel arrays store one attendance record per row.
    private static String[] attEmpID;  // Which employee this record belongs to
    private static String[] attDate;   // The date (e.g., "2024-06-03")
    private static String[] attLogIn;  // Log-in time (e.g., "8:00")
    private static String[] attLogOut; // Log-out time (e.g., "17:00")
    private static int attCount = 0;   // How many attendance records were loaded

    // ── MAIN METHOD ───────────────────────────────────────────
    /**
     * Entry point of the program.
     * Loads data, handles login, and routes user to the correct menu.
     *
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {

        // Step 1: Load employee profiles and attendance records from CSV files.
        loadEmployees("data/employees.csv");
        loadAttendance("data/attendance.csv");

        // Step 2: Create a Scanner to read keyboard input from the user.
        Scanner sc = new Scanner(System.in);

        // ── LOGIN ─────────────────────────────────────────────
        System.out.println("\n" + BOLD + CYAN + " MOTORPH PAYROLL SYSTEM" + RESET);
        System.out.println(" " + SEP);
        System.out.print(" Username: ");
        String username = sc.nextLine().trim();

        System.out.print(" Password: ");
        String password = sc.nextLine().trim();

        boolean validUser = username.equals("payroll_staff") || username.equals("employee");
        boolean validPass = password.equals("12345");

        if (!validUser || !validPass) {
            System.out.println(RED + "\n Invalid username or password. Exiting." + RESET);
            sc.close();
            System.exit(0);
            return;
        }

        // ── PAYROLL STAFF MENU ────────────────────────────────
        if (username.equals("payroll_staff")) {
            boolean staffRunning = true;
            while (staffRunning) {
                System.out.println("\n" + BOLD + CYAN + "--- Payroll Staff Menu ---" + RESET);
                System.out.println(" 1. Process Payroll");
                System.out.println(" 2. Exit the program");
                System.out.print(" Option: ");
                String choice = sc.nextLine().trim();

                if (choice.equals("1")) {
                    showPayrollMenu(sc);
                } else if (choice.equals("2")) {
                    System.out.println(" Exiting program. Goodbye!");
                    staffRunning = false;
                } else {
                    System.out.println(RED + " Invalid option. Please enter 1 or 2." + RESET);
                }
            }
        }

        // ── EMPLOYEE MENU ─────────────────────────────────────
        else if (username.equals("employee")) {
            showEmployeeMenu(sc);
        }

        // Step 3: Close the scanner to free up system resources.
        sc.close();
    }

    // ── MENU: PAYROLL STAFF ───────────────────────────────────
    /**
     * Shows the payroll processing sub-menu for payroll staff.
     *
     * @param sc The shared Scanner for reading user input.
     */
    private static void showPayrollMenu(Scanner sc) {
        boolean menuRunning = true;
        while (menuRunning) {
            System.out.println("\n" + BOLD + CYAN + "--- Process Payroll ---" + RESET);
            System.out.println(" 1. One employee");
            System.out.println(" 2. All employees");
            System.out.println(" 3. Back to main menu");
            System.out.print(" Option: ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1":
                    printSingleEmployeePayroll(sc);
                    break;
                case "2":
                    printAllEmployeesPayroll();
                    break;
                case "3":
                    System.out.println(" Returning to main menu.");
                    menuRunning = false;
                    break;
                default:
                    System.out.println(RED + " Invalid option. Please enter 1, 2, or 3." + RESET);
            }
        }
    }

    // ── MENU: EMPLOYEE ────────────────────────────────────────
    /**
     * Shows the self-service menu for a logged-in employee.
     *
     * @param sc The shared Scanner for reading user input.
     */
    private static void showEmployeeMenu(Scanner sc) {
        boolean menuRunning = true;
        while (menuRunning) {
            System.out.println("\n" + BOLD + CYAN + "--- Employee Menu ---" + RESET);
            System.out.println(" 1. View my payroll");
            System.out.println(" 2. Exit");
            System.out.print(" Option: ");
            String choice = sc.nextLine().trim();

            if (choice.equals("1")) {
                System.out.print(" Enter your Employee #: ");
                String id = sc.nextLine().trim();
                int index = findEmployeeIndex(id);
                if (index >= 0) {
                    printPayrollSummary(index);
                } else {
                    System.out.println(RED + " Employee number not found." + RESET);
                }
            } else if (choice.equals("2")) {
                System.out.println(" Exiting. Goodbye!");
                menuRunning = false;
            } else {
                System.out.println(RED + " Invalid option. Please enter 1 or 2." + RESET);
            }
        }
    }

    // ── PRINT: SINGLE EMPLOYEE ────────────────────────────────
    /**
     * Prompts for an employee ID and prints that employee's full payroll summary.
     *
     * @param sc The shared Scanner for reading user input.
     */
    private static void printSingleEmployeePayroll(Scanner sc) {
        System.out.print(" Enter Employee #: ");
        String id = sc.nextLine().trim();
        int index = findEmployeeIndex(id);
        if (index >= 0) {
            printPayrollSummary(index);
        } else {
            System.out.println(RED + " Employee number doesn't exist." + RESET);
        }
    }

    // ── PRINT: ALL EMPLOYEES ──────────────────────────────────
    /**
     * Prints the full payroll summary for every loaded employee,
     * sorted by employee ID in ascending order.
     */
    private static void printAllEmployeesPayroll() {
        Integer[] indices = new Integer[empCount];
        for (int i = 0; i < empCount; i++) indices[i] = i;
        Arrays.sort(indices, Comparator.comparing(i -> empIDs[i]));

        System.out.println("\n" + BOLD + CYAN + SEP + RESET);
        System.out.println(BOLD + CYAN + " ALL EMPLOYEES - FULL PAYROLL (ALL MONTHS)" + RESET);
        System.out.println(BOLD + CYAN + SEP + RESET);

        for (int idx : indices) {
            printPayrollSummary(idx);
        }
    }

    // ── PRINT: PAYROLL SUMMARY ────────────────────────────────
    /**
     * Prints the complete payroll summary for one employee,
     * covering all months found in their attendance records.
     *
     * Cutoff 1: 1st to 15th (gross only, no deductions)
     * Cutoff 2: 16th to end of month (combined gross minus all deductions)
     *
     * @param empIndex The position of the employee in the data arrays.
     */
    private static void printPayrollSummary(int empIndex) {
        System.out.println("\n" + SEP);
        System.out.println(BOLD + CYAN + " MOTORPH PAYROLL SUMMARY" + RESET);
        System.out.println(SEP);
        System.out.printf(" %-20s %s%n", "Employee #:",    empIDs[empIndex]);
        System.out.printf(" %-20s %s%n", "Employee Name:", empNames[empIndex]);
        System.out.printf(" %-20s %s%n", "Birthday:",      empBirthdays[empIndex]);
        System.out.println(SEP);

        List<String> months = getAvailableMonths(empIDs[empIndex]);

        if (months.isEmpty()) {
            System.out.println(" No attendance records found.");
            System.out.println(SEP);
            return;
        }

        for (String yearMonth : months) {
            System.out.println(BOLD + YELLOW + "\n Month: " + formatYearMonth(yearMonth) + RESET);
            System.out.println(DASH);

            // ── CUTOFF 1: 1st to 15th ─────────────────────────
            double hours1 = computeHoursWorked(empIDs[empIndex], yearMonth, 1);
            double gross1 = hours1 * empHourlyRates[empIndex];
            String label1 = getCutoffLabel(yearMonth, 1);

            System.out.println(" Cutoff Period : " + label1);
            System.out.printf(" %-25s %.2f hrs%n",    "Total Hours Worked:", hours1);
            System.out.printf(" %-25s PHP %,.2f%n",   "Gross Salary:",       gross1);
            System.out.println();

            // ── CUTOFF 2: 16th to end of month ────────────────
            double hours2       = computeHoursWorked(empIDs[empIndex], yearMonth, 2);
            double gross2       = hours2 * empHourlyRates[empIndex];
            double combinedGross = gross1 + gross2;

            double sss       = empSSS[empIndex];
            double philHealth = empPhilHealth[empIndex];
            double pagIbig   = empPagIbig[empIndex];
            double tax       = empTax[empIndex];
            double totalDed  = sss + philHealth + pagIbig + tax;
            double netPay    = combinedGross - totalDed;

            String label2 = getCutoffLabel(yearMonth, 2);

            System.out.println(" Cutoff Period : " + label2);
            System.out.printf(" %-25s %.2f hrs%n",  "Total Hours Worked:",      hours2);
            System.out.printf(" %-25s PHP %,.2f%n", "Gross Salary (Cutoff 2):", gross2);
            System.out.printf(" %-25s PHP %,.2f%n", "Gross Salary (Cutoff 1):", gross1);
            System.out.println(" " + DASH);
            System.out.printf(" %-25s PHP %,.2f%n", "Combined Gross:", combinedGross);
            System.out.println(" Deductions:");
            System.out.printf(" %-23s PHP %,.2f%n", "SSS:",        sss);
            System.out.printf(" %-23s PHP %,.2f%n", "Phil-Health:", philHealth);
            System.out.printf(" %-23s PHP %,.2f%n", "Pag-IBIG:",   pagIbig);
            System.out.printf(" %-23s PHP %,.2f%n", "Tax:",        tax);
            System.out.println(" " + DASH);
            System.out.printf(" %-25s PHP %,.2f%n", "Total Deductions:", totalDed);
            System.out.printf(" %-25s PHP %,.2f%n", "Net Salary:",       netPay);
        }

        System.out.println("\n" + SEP);
    }

    // ── HOURS COMPUTATION ─────────────────────────────────────
    /**
     * Calculates hours worked between a log-in and log-out time.
     *
     * Rules:
     * - Work starts no earlier than 8:00 AM.
     * - Arrivals at or before 8:05 AM are treated as 8:00 AM (grace period).
     * - Arrivals after 8:05 AM are counted from their actual log-in time.
     * - Work ends no later than 5:00 PM (overtime not counted).
     *
     * @param logIn  Log-in time string (e.g., "8:02").
     * @param logOut Log-out time string (e.g., "17:00").
     * @return Hours worked as a decimal. Returns 0 on error.
     */
    private static double computeHours(String logIn, String logOut) {
        try {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("H:mm");
            LocalTime start = LocalTime.parse(logIn.trim(),  fmt);
            LocalTime end   = LocalTime.parse(logOut.trim(), fmt);

            LocalTime officialStart  = LocalTime.of(8,  0);
            LocalTime gracePeriodEnd = LocalTime.of(8,  5);
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

    // ── TOTAL HOURS PER CUTOFF ────────────────────────────────
    /**
     * Totals hours worked by one employee during a specific cutoff period.
     *
     * Cutoff 1 = days 1–15.
     * Cutoff 2 = days 16 to end of month.
     *
     * @param empID     The employee's ID string.
     * @param yearMonth The year and month (e.g., "2024-06").
     * @param cutoff    1 for first half, 2 for second half.
     * @return Total hours worked as a decimal.
     */
    private static double computeHoursWorked(String empID, String yearMonth, int cutoff) {
        double total = 0;

        for (int i = 0; i < attCount; i++) {
            if (!attEmpID[i].equals(empID)) continue;

            String recYearMonth = getYearMonth(attDate[i]);
            if (!recYearMonth.equals(yearMonth)) continue;

            int day       = getDayOfMonth(attDate[i]);
            int recCutoff = (day <= 15) ? 1 : 2;
            if (recCutoff != cutoff) continue;

            total += computeHours(attLogIn[i], attLogOut[i]);
        }

        return total;
    }

    // ── AVAILABLE MONTHS ──────────────────────────────────────
    /**
     * Returns a sorted list of unique year-month strings for which
     * the given employee has at least one attendance record.
     *
     * @param empID The employee's ID string.
     * @return Sorted list of "YYYY-MM" strings.
     */
    private static List<String> getAvailableMonths(String empID) {
        Set<String> months = new TreeSet<>();

        for (int i = 0; i < attCount; i++) {
            if (!attEmpID[i].equals(empID)) continue;
            String ym = getYearMonth(attDate[i]);
            if (!ym.equals("Unknown")) months.add(ym);
        }

        return new ArrayList<>(months);
    }

    // ── CUTOFF LABEL ──────────────────────────────────────────
    /**
     * Generates a human-readable label for a cutoff period.
     *
     * Examples:
     *   Cutoff 1: "June 1 to June 15, 2024"
     *   Cutoff 2: "June 16 to June 30, 2024"
     *
     * @param yearMonth The year and month string (e.g., "2024-06").
     * @param cutoff    1 for first half, 2 for second half.
     * @return A formatted date range string.
     */
    private static String getCutoffLabel(String yearMonth, int cutoff) {
        try {
            String[] parts = yearMonth.split("-");
            int year  = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);

            LocalDate date     = LocalDate.of(year, month, 1);
            String monthName   = date.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
            int lastDay        = date.withDayOfMonth(date.lengthOfMonth()).getDayOfMonth();

            if (cutoff == 1) {
                return monthName + " 1 to " + monthName + " 15, " + year;
            } else {
                return monthName + " 16 to " + monthName + " " + lastDay + ", " + year;
            }
        } catch (Exception e) {
            return yearMonth + " Cutoff " + cutoff;
        }
    }

    // ── FORMAT YEAR-MONTH ─────────────────────────────────────
    /**
     * Converts "YYYY-MM" to "Month YYYY".
     * Example: "2024-06" becomes "June 2024"
     *
     * @param yearMonth The year and month string.
     * @return A formatted month and year string.
     */
    private static String formatYearMonth(String yearMonth) {
        try {
            String[] parts = yearMonth.split("-");
            int year  = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            String monthName = Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH);
            return monthName + " " + year;
        } catch (Exception e) {
            return yearMonth;
        }
    }

    // ── HELPER: YEAR-MONTH FROM DATE ──────────────────────────
    /**
     * Extracts "YYYY-MM" from a date string in MM/DD/YYYY format.
     * Example: "06/03/2024" returns "2024-06"
     *
     * @param date A date string in "MM/DD/YYYY" format.
     * @return A "YYYY-MM" string, or "Unknown" if invalid.
     */
    private static String getYearMonth(String date) {
        try {
            String[] parts = date.split("/");
            String year  = parts[2].trim();
            String month = parts[0].trim();
            return year + "-" + (month.length() == 1 ? "0" + month : month);
        } catch (Exception e) {
            return "Unknown";
        }
    }

    // ── HELPER: DAY OF MONTH FROM DATE ────────────────────────
    /**
     * Extracts the day number from a date string in MM/DD/YYYY format.
     * Example: "06/03/2024" returns 3
     *
     * @param date A date string in "MM/DD/YYYY" format.
     * @return The day as an integer, or 0 if parsing fails.
     */
    private static int getDayOfMonth(String date) {
        try {
            String[] parts = date.split("/");
            return Integer.parseInt(parts[1].trim());
        } catch (Exception e) {
            return 0;
        }
    }

    // ── HELPER: FIND EMPLOYEE INDEX ───────────────────────────
    /**
     * Searches for an employee by their ID and returns their array index.
     *
     * @param empID The employee ID to search for.
     * @return The index in the employee arrays, or -1 if not found.
     */
    private static int findEmployeeIndex(String empID) {
        for (int i = 0; i < empCount; i++) {
            if (empIDs[i].equals(empID)) return i;
        }
        return -1;
    }

    // ── CSV LOADER: EMPLOYEES ─────────────────────────────────
    /**
     * Reads employee data from a CSV file into parallel arrays.
     *
     * Expected CSV format (with header row):
     *   Employee #, Name, Birthday, Basic Salary, Hourly Rate, SSS, Tax, Phil-Health, Pag-Ibig
     *
     * @param filename Path to the employees CSV file.
     */
    private static void loadEmployees(String filename) {
        try {
            List<String> lines = new ArrayList<>();
            BufferedReader br = new BufferedReader(new FileReader(filename));
            String line;
            boolean firstLine = true;

            while ((line = br.readLine()) != null) {
                if (firstLine) { firstLine = false; continue; }
                if (!line.trim().isEmpty()) lines.add(line);
            }
            br.close();

            int count = lines.size();
            empIDs         = new String[count];
            empNames       = new String[count];
            empBirthdays   = new String[count];
            empHourlyRates = new double[count];
            empSSS         = new double[count];
            empPhilHealth  = new double[count];
            empPagIbig     = new double[count];
            empTax         = new double[count];

            for (String dataLine : lines) {
                String[] parts = dataLine.split(",");
                empIDs[empCount]         = parts[0].trim(); // Employee #
                empNames[empCount]       = parts[1].trim(); // Name
                empBirthdays[empCount]   = parts[2].trim(); // Birthday
                // parts[3] = Basic Salary (ignored)
                empHourlyRates[empCount] = Double.parseDouble(parts[4].trim()); // Hourly Rate
                empSSS[empCount]         = Double.parseDouble(parts[5].trim()); // SSS
                empTax[empCount]         = Double.parseDouble(parts[6].trim()); // Tax
                empPhilHealth[empCount]  = Double.parseDouble(parts[7].trim()); // Phil-Health
                empPagIbig[empCount]     = Double.parseDouble(parts[8].trim()); // Pag-Ibig
                empCount++;
            }

            System.out.println(" Loaded " + empCount + " employee(s) from " + filename);

        } catch (FileNotFoundException e) {
            System.out.println(RED + " ERROR: Employee file not found: " + filename + RESET);
            System.exit(1);
        } catch (Exception e) {
            System.out.println(RED + " ERROR: Failed to load employees. " + e.getMessage() + RESET);
            System.exit(1);
        }
    }

    // ── CSV LOADER: ATTENDANCE ────────────────────────────────
    /**
     * Reads attendance records from a CSV file into parallel arrays.
     *
     * Expected CSV format (with header row):
     *   Employee #, Last Name, First Name, Date, Log In, Log Out
     *
     * @param filename Path to the attendance CSV file.
     */
    private static void loadAttendance(String filename) {
        try {
            List<String> lines = new ArrayList<>();
            BufferedReader br = new BufferedReader(new FileReader(filename));
            String line;
            boolean firstLine = true;

            while ((line = br.readLine()) != null) {
                if (firstLine) { firstLine = false; continue; }
                if (!line.trim().isEmpty()) lines.add(line);
            }
            br.close();

            int count = lines.size();
            attEmpID  = new String[count];
            attDate   = new String[count];
            attLogIn  = new String[count];
            attLogOut = new String[count];

            for (String dataLine : lines) {
                String[] parts = dataLine.split(",");
                attEmpID[attCount]  = parts[0].trim(); // Employee #
                // parts[1] = Last Name  (ignored)
                // parts[2] = First Name (ignored)
                attDate[attCount]   = parts[3].trim(); // Date
                attLogIn[attCount]  = parts[4].trim(); // Log In
                attLogOut[attCount] = parts[5].trim(); // Log Out
                attCount++;
            }

            System.out.println(" Loaded " + attCount + " attendance record(s) from " + filename);

        } catch (FileNotFoundException e) {
            System.out.println(RED + " ERROR: Attendance file not found: " + filename + RESET);
            System.exit(1);
        } catch (Exception e) {
            System.out.println(RED + " ERROR: Failed to load attendance. " + e.getMessage() + RESET);
            System.exit(1);
        }
    }
}
