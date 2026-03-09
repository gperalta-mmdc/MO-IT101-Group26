import java.util.HashMap;
import java.util.Scanner;

public class Main {

    private static final String RED   = "\u001B[31m";
    private static final String BOLD  = "\u001B[1m";
    private static final String CYAN  = "\u001B[36m";
    private static final String RESET = "\u001B[0m";

    public static void main(String[] args) {

        // Load data
        HashMap<String, Employee> employees = CSVLoader.loadEmployees("data/employees.csv");
        CSVLoader.loadAttendance("data/attendance.csv", employees);

        Scanner sc = new Scanner(System.in);

        // ── LOGIN ──────────────────────────────────────────────────────────
        System.out.println("\n" + BOLD + CYAN + "  MOTORPH PAYROLL SYSTEM" + RESET);
        System.out.println("  ========================");

        System.out.print("Username: ");
        String username = sc.nextLine().trim();

        System.out.print("Password: ");
        String password = sc.nextLine().trim();

        boolean validUser = username.equals("payroll_staff") || username.equals("employee");
        boolean validPass = password.equals("12345");

        if (!validUser || !validPass) {
            System.out.println(RED + "Invalid username or password" + RESET);
            sc.close();
            System.exit(0);   // terminate the program
            return;
        }

        // ── PAYROLL STAFF ──────────────────────────────────────────────────
        if (username.equals("payroll_staff")) {
            while (true) {
                System.out.println("\n" + BOLD + CYAN + "--- Payroll Staff Menu ---" + RESET);
                System.out.println("1. Process Payroll");
                System.out.println("2. Exit the program");
                System.out.print("Option: ");

                String choice = sc.nextLine().trim();

                if (choice.equals("1")) {
                    new PayrollStaffView(employees, sc);
                } else if (choice.equals("2")) {
                    System.out.println("Exiting program.");
                    break;
                } else {
                    System.out.println(RED + "Invalid option. Please enter 1 or 2." + RESET);
                }
            }
        }

        // ── EMPLOYEE ───────────────────────────────────────────────────────
        else if (username.equals("employee")) {
            new EmployeeView(employees, sc);
        }

        sc.close();
    }
}
