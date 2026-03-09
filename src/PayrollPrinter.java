import java.util.List;


public class PayrollPrinter {

    private static final String BOLD   = "\u001B[1m";
    private static final String CYAN   = "\u001B[36m";
    private static final String YELLOW = "\u001B[33m";
    private static final String RESET  = "\u001B[0m";
    private static final String SEP    = "============================================================";
    private static final String DASH   = "------------------------------------------------------------";


    public static void printEmployeePayroll(Employee emp) {
        System.out.println("\n" + SEP);
        System.out.println(BOLD + CYAN + "  MOTORPH PAYROLL SUMMARY" + RESET);
        System.out.println(SEP);
        System.out.printf("  %-20s %s%n", "Employee #:",    emp.getEmployeeID());
        System.out.printf("  %-20s %s%n", "Employee Name:", emp.getName());
        System.out.printf("  %-20s %s%n", "Birthday:",      emp.getBirthday());
        System.out.println(SEP);

        List<String> months = PayrollCalculator.getAvailableMonths(emp);

        if (months.isEmpty()) {
            System.out.println("  No attendance records found.");
            System.out.println(SEP);
            return;
        }

        for (String yearMonth : months) {
            System.out.println(BOLD + YELLOW + "\n  Month: " + formatYearMonth(yearMonth) + RESET);
            System.out.println(DASH);

            // --- Cutoff 1 (1st–15th) ---
            String label1 = PayrollCalculator.getCutoffLabel(emp, yearMonth, 1);
            double hours1 = PayrollCalculator.computeHoursWorked(emp, yearMonth, 1);
            double gross1 = PayrollCalculator.computeGross(emp, yearMonth, 1);
            double net1   = PayrollCalculator.computeNet(emp, yearMonth, 1);

            System.out.println("  Cutoff Date: " + label1);
            System.out.printf("  %-25s %.2f hrs%n", "Total Hours Worked:", hours1);
            System.out.printf("  %-25s PHP %,.2f%n", "Gross Salary:", gross1);
            System.out.printf("  %-25s PHP %,.2f%n", "Net Salary:", net1);

            System.out.println();

            // --- Cutoff 2 (16th–end) ---
            String label2 = PayrollCalculator.getCutoffLabel(emp, yearMonth, 2);
            double hours2        = PayrollCalculator.computeHoursWorked(emp, yearMonth, 2);
            double gross2        = PayrollCalculator.computeGross(emp, yearMonth, 2);
            double combinedGross = gross1 + gross2;  // both halves combined before deductions
            double net2          = PayrollCalculator.computeNet(emp, yearMonth, 2);

            double sss        = emp.getSSS();
            double philHealth = emp.getPhilHealth();
            double pagIbig    = emp.getPagIbig();
            double taxAmt     = emp.getTax();
            double totalDed   = emp.getTotalDeductions();

            System.out.println("  Cutoff Date: " + label2);
            System.out.printf("  %-25s %.2f hrs%n",  "Total Hours Worked:", hours2);
            System.out.printf("  %-25s PHP %,.2f%n", "Gross Salary (Cutoff 2):", gross2);
            System.out.printf("  %-25s PHP %,.2f%n", "Gross Salary (Cutoff 1):", gross1);
            System.out.println("  " + DASH);
            System.out.printf("  %-25s PHP %,.2f%n", "Combined Gross:", combinedGross);
            System.out.println("  Deductions:");
            System.out.printf("    %-23s PHP %,.2f%n", "SSS:",         sss);
            System.out.printf("    %-23s PHP %,.2f%n", "Phil-Health:", philHealth);
            System.out.printf("    %-23s PHP %,.2f%n", "Pag-IBIG:",    pagIbig);
            System.out.printf("    %-23s PHP %,.2f%n", "Tax:",          taxAmt);
            System.out.println("  " + DASH);
            System.out.printf("  %-25s PHP %,.2f%n", "Total Deductions:", totalDed);
            System.out.printf("  %-25s PHP %,.2f%n", "Net Salary:", net2);
        }

        System.out.println("\n" + SEP);
    }

    /** Formats "2024-06" → "June 2024" */
    private static String formatYearMonth(String yearMonth) {
        try {
            String[] parts = yearMonth.split("-");
            int year  = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            java.time.Month m = java.time.Month.of(month);
            return m.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH) + " " + year;
        } catch (Exception e) {
            return yearMonth;
        }
    }
}

