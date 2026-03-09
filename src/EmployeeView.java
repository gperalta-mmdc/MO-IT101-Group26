import java.util.*;

public class EmployeeView {

    private static final String RED   = "\u001B[31m";
    private static final String BOLD  = "\u001B[1m";
    private static final String CYAN  = "\u001B[36m";
    private static final String RESET = "\u001B[0m";

    private HashMap<String, Employee> employees;
    private Scanner sc;

    public EmployeeView(HashMap<String, Employee> employees, Scanner sc) {
        this.employees = employees;
        this.sc = sc;
        showMenu();
    }

    private void showMenu() {
        while (true) {
            System.out.println("\n" + BOLD + CYAN + "--- Employee Portal ---" + RESET);
            System.out.println("1. View my payroll");
            System.out.println("2. Exit the program");
            System.out.print("Option: ");

            String choice = sc.nextLine().trim();

            if (choice.equals("1")) {
                System.out.print("Enter Employee #: ");
                String id = sc.nextLine().trim();

                if (employees.containsKey(id)) {
                    PayrollPrinter.printEmployeePayroll(employees.get(id));
                } else {
                    System.out.println(RED + "Employee number doesn't exist." + RESET);
                }

            } else if (choice.equals("2")) {
                System.out.println("Exiting program.");
                System.exit(0);

            } else {
                System.out.println(RED + "Invalid option. Please enter 1 or 2." + RESET);
            }
        }
    }
}
