MO-IT101-Computer Programming | Group 26
Helen Joy Prieto 
Hadrian James Delaguiado
Harvin Ong
Jairo Miguel Cordova
Giannros Peralta
Program details: How it works, start to finish:

Startup — the system reads two CSV files: one for employee profiles (rates, deductions, etc.) and one for daily attendance (log-in/log-out times).
Login — the user enters a username and password. There are two roles: payroll_staff and employee, both using the password 12345. Wrong credentials shut the program down immediately.
Payroll Staff — gets a menu to process payroll for one employee or all employees. The system calculates hours worked from attendance logs, computes gross pay based on hourly rate, applies deductions (SSS, PhilHealth, Pag-IBIG, Tax) on the second cutoff, and prints a formatted salary summary to the console.
Employee — goes directly into their own view, presumably to check their own payroll records.
Pay periods — each month is split into two cutoffs: the 1st–15th and the 16th–end of month. Net pay is only finalized on the second cutoff, when all deductions are applied against the combined gross of both halves.
In short: load data → log in → calculate pay → display results.
