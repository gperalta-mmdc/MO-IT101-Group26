import java.util.ArrayList;

public class Employee {

    private String employeeID;
    private String name;
    private String birthday;
    private double basicSalary;
    private double hourlyRate;
    private double sss;
    private double tax;
    private double philHealth;
    private double pagIbig;

    private ArrayList<AttendanceRecord> attendance = new ArrayList<>();

    public Employee(String employeeID, String name, String birthday,
                    double basicSalary, double hourlyRate,
                    double sss, double tax, double philHealth, double pagIbig) {
        this.employeeID = employeeID;
        this.name = name;
        this.birthday = birthday;
        this.basicSalary = basicSalary;
        this.hourlyRate = hourlyRate;
        this.sss = sss;
        this.tax = tax;
        this.philHealth = philHealth;
        this.pagIbig = pagIbig;
    }

    public String getEmployeeID()  { return employeeID; }
    public String getName()        { return name; }
    public String getBirthday()    { return birthday; }
    public double getBasicSalary() { return basicSalary; }
    public double getHourlyRate()  { return hourlyRate; }
    public double getSSS()         { return sss; }
    public double getTax()         { return tax; }
    public double getPhilHealth()  { return philHealth; }
    public double getPagIbig()     { return pagIbig; }

    public void addAttendance(AttendanceRecord record) {
        attendance.add(record);
    }

    public ArrayList<AttendanceRecord> getAttendance() {
        return attendance;
    }

    public double getTotalDeductions() {
        return sss + tax + philHealth + pagIbig;
    }
}
