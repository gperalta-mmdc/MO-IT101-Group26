/**
 * ============================================================
 * Program : MotorPH Payroll System
 * File : Main.java
 * Description : A console-based payroll system for MotorPH.
 * This program loads employee profiles and
 * attendance records from CSV files, handles
 * user login, and computes payroll for two
 * cutoff periods per month (1st-15th and
 * 16th to end of month). Deductions such as
 * SSS, PhilHealth, Pag-IBIG, and Tax are
 * applied on the second cutoff.
 *
 * How to run : Compile with: javac Main.java
 * Run with : java Main
 *
 * CSV files required:
 * data/employees.csv - Employee profiles
 * data/attendance.csv - Daily attendance log-in/out records
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
    private static final String SEP =
        "============================================================";
    private static final String DASH = "------------------------------------------------------------";
 
    // ── EMPLOYEE DATA ARRAYS ──────────────────────────────────
    // These parallel arrays store employee data loaded from the CSV.
    // Each index position (0, 1, 2, ...) represents one employee.
    // For example, empIDs[0], empNames[0], empRates[0] all belong
    // to the same first employee.
    private static String[] empIDs;         // Employee ID numbers
    private static String[] empNames;       // Full names
    private static String[] empBirthdays;   // Birthdays (display only)
    private static double[] empHourlyRates; // Hourly pay rate in PHP
    private static double[] empSSS;         // SSS monthly deduction
    private static double[] empPhilHealth;  // PhilHealth monthly deduction
    private static double[] empPagIbig;     // Pag-IBIG monthly deduction
    private static double[] empTax;         // Withholding tax monthly deduction
    private static int empCount = 0;        // How many employees were loaded
 
    // ── ATTENDANCE DATA ARRAYS ────────────────────────────────
    // These parallel arrays store one attendance record per row.
    // Each index represents one day's log-in/log-out for one employee.
    private static String[] attEmpID;  // Which employee this record belongs to
    private static String[] attDate;   // The date (e.g., "06/03/2024")
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
        // If either file fails to load, the program will notify the user and exit.
        loadEmployees("data/employees.csv");
        loadAttendance("data/attendance.csv");
 
        // Step 2: Create a Scanner to read keyboard input from the user.
        Scanner sc = new Scanner(System.in);
 
        // ── LOGIN ─────────────────────────────────────────────
        // Display the system title and ask for credentials.
        System.out.println("\n" + BOLD + CYAN + " MOTORPH PAYROLL SYSTEM" + RESET);
        System.out.println(" " + SEP);
        System.out.print(" Username: ");
        String username = sc.nextLine().trim(); // Read and remove extra spaces
 
        System.out.print(" Password: ");
        String password = sc.nextLine().trim();
 
        // Check if the username is one of the two valid roles.
        boolean validUser = username.equals("payroll_staff") || username.equals("employee");
 
        // Check if the password matches (single shared password for simplicity).
        boolean validPass = password.equals("12345");
 
        // If either check fails, show an error and terminate the program.
        if (!validUser || !validPass) {
            System.out.println(RED + "\n Invalid username or password. Exiting." + RESET);
            sc.close();
            System.exit(0); // Terminate the entire program immediately
            return; // Defensive return (unreachable, but good practice)
        }
 
        // ── PAYROLL STAFF MENU ────────────────────────────────
        // If the user logged in as payroll_staff, show the staff menu.
        // This menu loops until the user chooses to exit.
        if (username.equals("payroll_staff")) {
            boolean staffRunning = true; // Controls the staff menu loop
            while (staffRunning) {
                System.out.println("\n" + BOLD + CYAN + "--- Payroll Staff Menu ---" + RESET);
                System.out.println(" 1. Process Payroll");
                System.out.println(" 2. Exit the program");
                System.out.print(" Option: ");
                String choice = sc.nextLine().trim();
 
                if (choice.equals("1")) {
                    // Show the payroll processing sub-menu
                    showPayrollMenu(sc);
                } else if (choice.equals("2")) {
                    // Exit the loop and end the program
                    System.out.println(" Exiting program. Goodbye!");
                    staffRunning = false;
                } else {
                    // Handle any input that is not 1 or 2
                    System.out.println(RED + " Invalid option. Please enter 1 or 2." + RESET);
                }
            }
        }
 
        // ── EMPLOYEE MENU ─────────────────────────────────────
        // If the user logged in as employee, show their own payroll view.
        else if (username.equals("employee")) {
            showEmployeeMenu(sc);
        }
 
        // Step 3: Close the scanner to free up system resources.
        sc.close();
    }
 
    // ── MENU: PAYROLL STAFF ───────────────────────────────────
    /**
     * Shows the payroll processing sub-menu for payroll staff.
     * Staff can view one employee's payroll or all employees at once.
     *
     * @param sc The shared Scanner for reading user input.
     */
    private static void showPayrollMenu(Scanner sc) {
        boolean menuRunning = true; // Controls this sub-menu loop
        while (menuRunning) {
            System.out.println("\n" + BOLD + CYAN + "--- Process Payroll ---" + RESET);
            System.out.println(" 1. One employee");
            System.out.println(" 2. All employees");
            System.out.println(" 3. Back to main menu");
            System.out.print(" Option: ");
            String choice = sc.nextLine().trim();
 
            switch (choice) {
                case "1":
                    // Ask for an employee ID and print their payroll
                    printSingleEmployeePayroll(sc);
                    break;
                case "2":
                    // Print payroll for every employee, sorted by ID
                    printAllEmployeesPayroll();
                    break;
                case "3":
                    // Return to the staff menu
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
     * The employee enters their own ID to view their payroll records.
     *
     * @param sc The shared Scanner for reading user input.
     */
    private static void showEmployeeMenu(Scanner sc) {
        boolean menuRunning = true; // Controls this menu loop
        while (menuRunning) {
            System.out.println("\n" + BOLD + CYAN + "--- Employee Menu ---" + RESET);
            System.out.println(" 1. View my payroll");
            System.out.println(" 2. Exit");
            System.out.print(" Option: ");
            String choice = sc.nextLine().trim();
 
            if (choice.equals("1")) {
                // Ask the employee to enter their own ID number
                System.out.print(" Enter your Employee #: ");
                String id = sc.nextLine().trim();
                int index = findEmployeeIndex(id); // Look up the employee by ID
                if (index >= 0) {
                    printPayrollSummary(index); // Print their payroll if found
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
     * Prompts the user for an employee ID and prints that employee's
     * full payroll summary if found. Shows an error if not found.
     *
     * @param sc The shared Scanner for reading user input.
     */
    private static void printSingleEmployeePayroll(Scanner sc) {
        System.out.print(" Enter Employee #: ");
        String id = sc.nextLine().trim();
        int index = findEmployeeIndex(id); // Search for the employee
        if (index >= 0) {
            printPayrollSummary(index);
        } else {
            System.out.println(RED + " Employee number doesn't exist." + RESET);
        }
    }
 
    // ── PRINT: ALL EMPLOYEES ──────────────────────────────────
    /**
     * Prints the full payroll summary for every loaded employee.
     * Employees are displayed in ascending order of their employee ID.
     */
    private static void printAllEmployeesPayroll() {
        // Build a sorted list of employee index positions by their ID string
        Integer[] indices = new Integer[empCount];
        for (int i = 0; i < empCount; i++) indices[i] = i;
 
        // Sort indices so the employee with the lowest ID prints first
        Arrays.sort(indices, Comparator.comparing(i -> empIDs[i]));
 
        // Print a header banner before listing all employees
        System.out.println("\n" + BOLD + CYAN + SEP + RESET);
        System.out.println(BOLD + CYAN + " ALL EMPLOYEES - FULL PAYROLL (ALL MONTHS)" + RESET);
        System.out.println(BOLD + CYAN + SEP + RESET);
 
        // Loop through each employee and print their payroll
        for (int idx : indices) {
            printPayrollSummary(idx);
        }
    }
 
    // ── PRINT: PAYROLL SUMMARY ────────────────────────────────
    /**
     * Prints the complete payroll summary for one employee.
     * This includes all months found in their attendance records,
     * broken into two cutoff periods each.
     *
     * Cutoff 1: 1st to 15th of the month (gross only, no deductions yet)
     * Cutoff 2: 16th to end of month (combined gross minus all deductions)
     *
     * @param empIndex The position of the employee in the data arrays.
     */
    private static void printPayrollSummary(int empIndex) {
        // Print the employee header block
        System.out.println("\n" + SEP);
        System.out.println(BOLD + CYAN + " MOTORPH PAYROLL SUMMARY" + RESET);
        System.out.println(SEP);
        System.out.printf(" %-20s %s%n", "Employee #:",    empIDs[empIndex]);
        System.out.printf(" %-20s %s%n", "Employee Name:", empNames[empIndex]);
        System.out.printf(" %-20s %s%n", "Birthday:",      empBirthdays[empIndex]);
        System.out.println(SEP);
 
        // Get a sorted list of unique year-month values for this employee
        // (e.g., ["2024-06", "2024-07", "2024-08"])
        List<String> months = getAvailableMonths(empIDs[empIndex]);
 
        // If no attendance records exist, say so and stop
        if (months.isEmpty()) {
            System.out.println(" No attendance records found.");
            System.out.println(SEP);
            return;
        }
 
        // Loop through each month and print both cutoff periods
        for (String yearMonth : months) {
            System.out.println(BOLD + YELLOW + "\n Month: " + formatYearMonth(yearMonth) + RESET);
            System.out.println(DASH);
 
            // ── CUTOFF 1: 1st to 15th ─────────────────────────
            double hours1 = computeHoursWorked(empIDs[empIndex], yearMonth, 1);
            double gross1 = hours1 * empHourlyRates[empIndex]; // Gross = hours x rate
            String label1 = getCutoffLabel(yearMonth, 1);      // Human-readable date range
 
            System.out.println(" Cutoff Period : " + label1);
            System.out.printf(" %-25s %.2f hrs%n",  "Total Hours Worked:", hours1);
            System.out.printf(" %-25s PHP %,.2f%n", "Gross Salary:",       gross1);
            // Note: Net salary is not shown for cutoff 1 because deductions
            // are only applied at the end of the month (cutoff 2).
            System.out.println();
 
            // ── CUTOFF 2: 16th to end of month ────────────────
            double hours2        = computeHoursWorked(empIDs[empIndex], yearMonth, 2);
            double gross2        = hours2 * empHourlyRates[empIndex]; // Gross for 2nd half
            double combinedGross = gross1 + gross2;                   // Total monthly gross
 
            // Retrieve each deduction amount for this employee
            double sss        = empSSS[empIndex];
            double philHealth = empPhilHealth[empIndex];
            double pagIbig    = empPagIbig[empIndex];
            double tax        = empTax[empIndex];
            double totalDed   = sss + philHealth + pagIbig + tax; // Sum all deductions
 
            // Net pay = total monthly gross minus all deductions
            double netPay = combinedGross - totalDed;
 
            String label2 = getCutoffLabel(yearMonth, 2); // Human-readable date range
 
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
     * Calculates the number of hours worked between a log-in and log-out time.
     *
     * Rules applied:
     * - Work starts no earlier than 8:00 AM (early arrivals are adjusted to 8:00).
     * - Grace period: arrivals at or before 8:05 AM are treated as 8:00 AM on time.
     * - Arrivals after 8:05 AM are counted from their actual log-in time (late).
     * - Work ends no later than 5:00 PM (overtime is not counted).
     * - If log-out is before or equal to log-in after adjustments, returns 0.
     *
     * @param logIn  Log-in time as a string (e.g., "8:02", "9:15").
     * @param logOut Log-out time as a string (e.g., "17:00", "16:45").
     * @return Hours worked as a decimal (e.g., 8.0, 7.5). Returns 0 on error.
     */
    private static double computeHours(String logIn, String logOut) {
        try {
            // Parse the time strings using H:mm format (allows single-digit hours)
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("H:mm");
            LocalTime start = LocalTime.parse(logIn.trim(),  fmt);
            LocalTime end   = LocalTime.parse(logOut.trim(), fmt);
 
            // Define key time boundaries
            LocalTime officialStart  = LocalTime.of(8,  0); // Earliest start: 8:00 AM
            LocalTime gracePeriodEnd = LocalTime.of(8,  5); // Grace period ends at 8:05 AM
            LocalTime officialEnd    = LocalTime.of(17, 0); // Latest end: 5:00 PM
 
            // If the employee arrived before 8:00 AM, adjust start to 8:00 AM.
            // Work before official hours is not counted.
            if (start.isBefore(officialStart)) {
                start = officialStart;
            }
 
            // If the employee arrived at or before 8:05 AM (within grace period),
            // treat their start time as exactly 8:00 AM (no deduction for slight tardiness).
            if (!start.isAfter(gracePeriodEnd)) {
                start = officialStart;
            }
 
            // If the employee logged out after 5:00 PM, cap the end at 5:00 PM.
            // Overtime hours are not counted in this system.
            if (end.isAfter(officialEnd)) {
                end = officialEnd;
            }
 
            // If the adjusted end time is not after the start time, no hours are counted.
            if (!end.isAfter(start)) {
                return 0;
            }
 
            // Calculate the difference in minutes, then convert to decimal hours.
            // Example: 480 minutes / 60 = 8.0 hours
            return Duration.between(start, end).toMinutes() / 60.0;
 
        } catch (Exception e) {
            // If either time string is invalid or missing, return 0 to avoid crashing.
            return 0;
        }
    }
 
    // ── TOTAL HOURS PER CUTOFF ────────────────────────────────
    /**
     * Totals the hours worked by one employee during a specific
     * cutoff period of a given month.
     *
     * Cutoff 1 = days 1 through 15.
     * Cutoff 2 = days 16 through end of month.
     *
     * @param empID     The employee's ID string.
     * @param yearMonth The year and month (e.g., "2024-06").
     * @param cutoff    1 for first half, 2 for second half.
     * @return Total hours worked as a decimal.
     */
    private static double computeHoursWorked(String empID, String yearMonth, int cutoff) {
        double total = 0;
 
        // Loop through every attendance record
        for (int i = 0; i < attCount; i++) {
            // Skip records that don't belong to this employee
            if (!attEmpID[i].equals(empID)) continue;
 
            // Extract the year-month portion from the date (e.g., "2024-06" from "06/03/2024")
            String recYearMonth = getYearMonth(attDate[i]);
            if (!recYearMonth.equals(yearMonth)) continue;
 
            // Determine which cutoff this date falls in based on the day number
            int day       = getDayOfMonth(attDate[i]);
            int recCutoff = (day <= 15) ? 1 : 2;
            if (recCutoff != cutoff) continue;
 
            // Add the hours for this attendance record to the running total
            total += computeHours(attLogIn[i], attLogOut[i]);
        }
 
        return total;
    }
 
    // ── AVAILABLE MONTHS ──────────────────────────────────────
    /**
     * Returns a sorted list of unique year-month strings for which
     * the given employee has at least one attendance record.
     *
     * Example result: ["2024-06", "2024-07", "2024-12"]
     *
     * @param empID The employee's ID string.
     * @return Sorted list of "YYYY-MM" strings.
     */
    private static List<String> getAvailableMonths(String empID) {
        // Use a TreeSet so months are automatically sorted and deduplicated
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
     * Example outputs:
     * Cutoff 1: "June 1 to June 15, 2024"
     * Cutoff 2: "June 16 to June 30, 2024"
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
 
            // Build a LocalDate to determine the month name and last day
            LocalDate date     = LocalDate.of(year, month, 1);
            String monthName   = date.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
            int lastDay        = date.withDayOfMonth(date.lengthOfMonth()).getDayOfMonth();
 
            if (cutoff == 1) {
                return monthName + " 1 to " + monthName + " 15, " + year;
            } else {
                return monthName + " 16 to " + monthName + " " + lastDay + ", " + year;
            }
        } catch (Exception e) {
            // Fallback label if date parsing fails
            return yearMonth + " Cutoff " + cutoff;
        }
    }
 
    // ── FORMAT YEAR-MONTH ─────────────────────────────────────
    /**
     * Converts a "YYYY-MM" string to a readable "Month YYYY" format.
     *
     * Example: "2024-06" becomes "June 2024"
     *
     * @param yearMonth The year and month string (e.g., "2024-06").
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
            return yearMonth; // Return as-is if parsing fails
        }
    }
 
    // ── HELPER: YEAR-MONTH FROM DATE ──────────────────────────
    /**
     * Extracts the "YYYY-MM" portion from a full date string.
     *
     * The date in attendance.csv uses MM/DD/YYYY format
     * (e.g., "06/03/2024"). This method reads the month and year
     * from that format and returns them as "YYYY-MM" (e.g., "2024-06")
     * so the rest of the program can group records by month.
     *
     * @param date A date string in "MM/DD/YYYY" format.
     * @return The "YYYY-MM" string, or "Unknown" if the format is invalid.
     */
    private static String getYearMonth(String date) {
        try {
            // Split by "/" to get month, day, and year separately
            String[] parts = date.split("/");
            String year  = parts[2].trim(); // Last part is the year (YYYY)
            String month = parts[0].trim(); // First part is the month (MM or M)
            // Pad single-digit months with a leading zero (e.g., "6" becomes "06")
            return year + "-" + (month.length() == 1 ? "0" + month : month);
        } catch (Exception e) {
            // If the date string is null, empty, or in an unexpected format, return Unknown
            return "Unknown";
        }
    }
 
    // ── HELPER: DAY OF MONTH FROM DATE ───────────────────────
    /**
     * Extracts the day number from a date string.
     *
     * The date in attendance.csv uses MM/DD/YYYY format
     * (e.g., "06/03/2024" returns 3). The day number is used
     * to determine whether a record belongs to cutoff 1 (days 1-15)
     * or cutoff 2 (days 16 to end of month).
     *
     * @param date A date string in "MM/DD/YYYY" format.
     * @return The day as an integer, or 0 if parsing fails.
     */
    private static int getDayOfMonth(String date) {
        try {
            // Split by "/" to get month, day, and year separately
            String[] parts = date.split("/");
            return Integer.parseInt(parts[1].trim()); // Middle part is the day (DD)
        } catch (Exception e) {
            return 0; // Return 0 if the date format is unexpected
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
        return -1; // Employee not found
    }
 
    // ── CSV LOADER: EMPLOYEES ─────────────────────────────────
    /**
     * Reads employee data from a CSV file and stores each field
     * into the corresponding parallel array.
     *
     * Expected CSV format (with header row):
     * Employee #, Name, Birthday, Basic Salary, Hourly Rate, SSS, Tax, Phil-Health, Pag-Ibig
     *
     * Example row:
     * 10001, Juan Dela Cruz, 03/15/1990, 30000.00, 171.00, 450.00, 500.00, 300.00, 100.00
     *
     * @param filename Path to the employees CSV file.
     */
    private static void loadEmployees(String filename) {
        try {
            // Read all lines from the file first so we know how many employees there are
            List<String> lines = new ArrayList<>();
            BufferedReader br = new BufferedReader(new FileReader(filename));
            String line;
            boolean firstLine = true;
 
            while ((line = br.readLine()) != null) {
                if (firstLine) { firstLine = false; continue; } // Skip the header row
                if (!line.trim().isEmpty()) lines.add(line);    // Skip blank lines
            }
            br.close();
 
            // Initialize all employee arrays to the number of data rows found
            int count = lines.size();
            empIDs         = new String[count];
            empNames       = new String[count];
            empBirthdays   = new String[count];
            empHourlyRates = new double[count];
            empSSS         = new double[count];
            empPhilHealth  = new double[count];
            empPagIbig     = new double[count];
            empTax         = new double[count];
 
            // Parse each line and store values into the arrays
            for (String dataLine : lines) {
                String[] parts = dataLine.split(",");
                empIDs[empCount]         = parts[0].trim(); // Employee #
                empNames[empCount]       = parts[1].trim(); // Name
                empBirthdays[empCount]   = parts[2].trim(); // Birthday
                // parts[3] is Basic Salary — not used in payroll computation, so it is skipped
                empHourlyRates[empCount] = Double.parseDouble(parts[4].trim()); // Hourly Rate
                empSSS[empCount]         = Double.parseDouble(parts[5].trim()); // SSS
                empTax[empCount]         = Double.parseDouble(parts[6].trim()); // Tax
                empPhilHealth[empCount]  = Double.parseDouble(parts[7].trim()); // Phil-Health
                empPagIbig[empCount]     = Double.parseDouble(parts[8].trim()); // Pag-Ibig
                empCount++;
            }
 
            System.out.println(" Loaded " + empCount + " employee(s) from " + filename);
 
        } catch (FileNotFoundException e) {
            // The CSV file was not found at the given path
            System.out.println(RED + " ERROR: Employee file not found: " + filename + RESET);
            System.exit(1); // Cannot continue without employee data
        } catch (Exception e) {
            // Something went wrong while reading or parsing the file
            System.out.println(RED + " ERROR: Failed to load employees. " + e.getMessage() + RESET);
            System.exit(1);
        }
    }
 
    // ── CSV LOADER: ATTENDANCE ────────────────────────────────
    /**
     * Reads attendance records from a CSV file and stores each field
     * into the corresponding parallel array.
     *
     * Expected CSV format (with header row):
     * Employee #, Last Name, First Name, Date, Log In, Log Out
     *
     * Example row:
     * 10001, Dela Cruz, Juan, 06/03/2024, 7:55, 17:10
     *
     * @param filename Path to the attendance CSV file.
     */
    private static void loadAttendance(String filename) {
        try {
            // Read all lines first to determine the array size needed
            List<String> lines = new ArrayList<>();
            BufferedReader br = new BufferedReader(new FileReader(filename));
            String line;
            boolean firstLine = true;
 
            while ((line = br.readLine()) != null) {
                if (firstLine) { firstLine = false; continue; } // Skip the header row
                if (!line.trim().isEmpty()) lines.add(line);    // Skip blank lines
            }
            br.close();
 
            // Initialize all attendance arrays to the number of data rows found
            int count = lines.size();
            attEmpID  = new String[count];
            attDate   = new String[count];
            attLogIn  = new String[count];
            attLogOut = new String[count];
 
            // Parse each line and store values into the arrays
            for (String dataLine : lines) {
                String[] parts = dataLine.split(",");
                attEmpID[attCount]  = parts[0].trim(); // Employee #
                // parts[1] is Last Name  — not used by this program, so it is skipped
                // parts[2] is First Name — not used by this program, so it is skipped
                attDate[attCount]   = parts[3].trim(); // Date
                attLogIn[attCount]  = parts[4].trim(); // Log In
                attLogOut[attCount] = parts[5].trim(); // Log Out
                attCount++;
            }
 
            System.out.println(" Loaded " + attCount + " attendance record(s) from " + filename);
 
        } catch (FileNotFoundException e) {
            System.out.println(RED + " ERROR: Attendance file not found: " + filename + RESET);
            System.exit(1); // Cannot continue without attendance data
        } catch (Exception e) {
            System.out.println(RED + " ERROR: Failed to load attendance. " + e.getMessage() + RESET);
            System.exit(1);
        }
    }
}
