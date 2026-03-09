import java.util.*;

public class PayrollStaffView {

    private static final String RED   = "\u001B[31m";
    private static final String BOLD  = "\u001B[1m";
    private static final String CYAN  = "\u001B[36m";
    private static final String RESET = "\u001B[0m";

    private HashMap<String, Employee> employees;
    private Scanner sc;

    public PayrollStaffView(HashMap<String, Employee> employees, Scanner sc) {
        this.employees = employees;
        this.sc = sc;
        showMenu();
    }

    private void showMenu() {
        while (true) {
            System.out.println("\n" + BOLD + CYAN + "--- Process Payroll ---" + RESET);
            System.out.println("1. One employee");
            System.out.println("2. All employees");
            System.out.println("3. Back to main menu");
            System.out.print("Option: ");

            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1":
                    viewSingleEmployee();
                    break;
                case "2":
                    viewAllEmployees();
                    break;
                case "3":
                    System.out.println("Returning to main menu.");
                    return;
                default:
                    System.out.println(RED + "Invalid option. Please enter 1, 2, or 3." + RESET);
            }
        }
    }

    private void viewSingleEmployee() {
        System.out.print("Enter Employee #: ");
        String id = sc.nextLine().trim();

        if (employees.containsKey(id)) {
            PayrollPrinter.printEmployeePayroll(employees.get(id));
        } else {
            System.out.println(RED + "Employee number doesn't exist." + RESET);
        }
    }

    private void viewAllEmployees() {
        
        List<Employee> sorted = new ArrayList<>(employees.values());
        sorted.sort(Comparator.comparing(Employee::getEmployeeID));

        System.out.println("\n" + BOLD + CYAN
                + "============================================================" + RESET);
        System.out.println(BOLD + CYAN
                + "  ALL EMPLOYEES - FULL PAYROLL (ALL MONTHS)" + RESET);
        System.out.println(BOLD + CYAN
                + "============================================================" + RESET);

        for (Employee emp : sorted) {
            PayrollPrinter.printEmployeePayroll(emp);
        }
    }
}

