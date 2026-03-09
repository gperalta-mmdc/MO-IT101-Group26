import java.io.*;
import java.util.*;

public class CSVLoader {


    private static String[] parseCSVLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString().trim()); // last field
        return fields.toArray(new String[0]);
    }


    private static double parseDouble(String value) {
        return Double.parseDouble(value.replace(",", "").trim());
    }


    public static HashMap<String, Employee> loadEmployees(String filePath) {
        HashMap<String, Employee> employees = new LinkedHashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line = br.readLine(); // skip header

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] data = parseCSVLine(line);
                if (data.length < 9) continue;

                Employee emp = new Employee(
                        data[0],               // Employee #
                        data[1],               // Name
                        data[2],               // Birthday
                        parseDouble(data[3]),  // Basic Salary
                        parseDouble(data[4]),  // Hourly Rate
                        parseDouble(data[5]),  // SSS
                        parseDouble(data[6]),  // Tax
                        parseDouble(data[7]),  // Phil-Health
                        parseDouble(data[8])   // Pag-Ibig
                );

                employees.put(emp.getEmployeeID(), emp);
            }

        } catch (Exception e) {
            System.err.println("Error loading employees: " + e.getMessage());
        }

        return employees;
    }


    public static void loadAttendance(String filePath, HashMap<String, Employee> employees) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line = br.readLine(); // skip header

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] data = parseCSVLine(line);
                if (data.length < 6) continue;

                String empID  = data[0];  // Employee #
                // data[1] = Last Name, data[2] = First Name (may contain spaces)
                String date   = data[3];  // Date
                String logIn  = data[4];  // Log In
                String logOut = data[5];  // Log Out

                AttendanceRecord record = new AttendanceRecord(date, logIn, logOut);

                if (employees.containsKey(empID)) {
                    employees.get(empID).addAttendance(record);
                }
            }

        } catch (Exception e) {
            System.err.println("Error loading attendance: " + e.getMessage());
        }
    }
}
