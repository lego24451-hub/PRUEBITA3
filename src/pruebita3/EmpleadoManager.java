
package pruebita3;

import java.io.*;
import java.util.*;

public class EmpleadoManager{

    private static final String DATA_DIR = "datos";
    
    private static final String EMP_FILE = DATA_DIR + "/empleados.emp";
    
    private static final int EMP_REC_SIZE = 93;
    
    private static final int MONTH_REC_SIZE = 9;
    
    private static final int RECIBO_REC_SIZE = 44;
    
    private static final Scanner scanner = new Scanner(System.in);

    private static String getEmployeeFolder(int code) {
        return DATA_DIR + "/empleados/" + code;
    }

    private static RandomAccessFile openEmployeeFile() throws IOException {
        File f = new File(EMP_FILE);
        if (!f.exists()) {
            f.getParentFile().mkdirs();
            f.createNewFile();
        }
        return new RandomAccessFile(f, "rw");
    }
    private static int findEmployeePosition(int code) throws IOException {
        RandomAccessFile raf = openEmployeeFile();
        int pos = -1;
        int count = (int) (raf.length() / EMP_REC_SIZE);
        for (int i = 0; i < count; i++) {
            raf.seek(i * EMP_REC_SIZE);
            if (raf.readInt() == code) {
                pos = i;
                break;
            }
        }
        raf.close();
        return pos;
    }

    private static boolean employeeExists(int code) throws IOException {
        return findEmployeePosition(code) != -1;
    }
    private static boolean isEmployeeActive(int code) throws IOException {
        int pos = findEmployeePosition(code);
        if (pos == -1) return false;
        RandomAccessFile raf = openEmployeeFile();
        raf.seek(pos * EMP_REC_SIZE + EMP_REC_SIZE - 1);
        boolean active = raf.readBoolean();
        raf.close();
        return active;
    }

    private static String getEmployeeName(int code) throws IOException {
        int pos = findEmployeePosition(code);
        if (pos == -1) return null;
        RandomAccessFile raf = openEmployeeFile();
        raf.seek(pos * EMP_REC_SIZE + 4);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 30; i++) {
            char c = raf.readChar();
            if (c != ' ') sb.append(c);
        }
        raf.close();
        return sb.toString().trim();
    }
    private static double getEmployeeSalary(int code) throws IOException {
        int pos = findEmployeePosition(code);
        if (pos == -1) return 0;
        RandomAccessFile raf = openEmployeeFile();
        raf.seek(pos * EMP_REC_SIZE + 4 + 60);
        double salary = raf.readDouble();
        raf.close();
        return salary;
    }
    private static RandomAccessFile salesFileFor(int code) throws IOException {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        String folder = getEmployeeFolder(code);
        new File(folder).mkdirs();
        String path = folder + "/ventas" + year + ".emp";

        RandomAccessFile raf = new RandomAccessFile(path, "rw");
        long expectedSize = 12 * MONTH_REC_SIZE;
        if (raf.length() < expectedSize) {
            raf.setLength(expectedSize);
        }
        return raf;
    }
    public static RandomAccessFile billsFilefor(int code) throws IOException {
        String folder = getEmployeeFolder(code);
        new File(folder).mkdirs();
        String path = folder + "/recibos.emp";
        return new RandomAccessFile(path, "rw");
    }
    public static void addSaleToEmployee(int code, double monto) throws IOException {
        if (!employeeExists(code)) {
            System.out.println("Empleado no encontrado");
            return;
        }

        RandomAccessFile raf = salesFileFor(code);
        int month = Calendar.getInstance().get(Calendar.MONTH); 
        long pos = month * MONTH_REC_SIZE;

        raf.seek(pos);
        double ventasActuales = raf.readDouble();
        raf.seek(pos);
        raf.writeDouble(ventasActuales + monto);
        raf.close();

        System.out.println("Venta agregada exitosamente");
    }
    public static boolean isEmployeePayed(int code) throws IOException {
        RandomAccessFile raf = salesFileFor(code);
        int month = Calendar.getInstance().get(Calendar.MONTH);
        long pos = month * MONTH_REC_SIZE + 8; 
        raf.seek(pos);
        boolean payed = raf.readBoolean();
        raf.close();
        return payed;
    }
    public static void payEmployee(int code) throws IOException {
      
        if (!employeeExists(code)) {
            System.out.println("No se pudo pagar");
            return;
        }
        if (!isEmployeeActive(code)) {
            System.out.println("No se pudo pagar");
            return;
        }
        if (isEmployeePayed(code)) {
            System.out.println("No se pudo pagar");
            return;
        }

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH); 

        String nombre = getEmployeeName(code);
        double salarioBase = getEmployeeSalary(code);

        RandomAccessFile salesRaf = salesFileFor(code);
        salesRaf.seek(month * MONTH_REC_SIZE);
        double ventas = salesRaf.readDouble();

        double sueldo = salarioBase + (ventas * 0.10);
        double deduccion = sueldo * 0.035;
        double total = sueldo - deduccion;

        RandomAccessFile billsRaf = billsFilefor(code);
        billsRaf.seek(billsRaf.length()); 

        String fecha = String.format("%td/%tm/%tY", cal, cal, cal);
        for (int i = 0; i < 10; i++) {
            if (i < fecha.length()) {
                billsRaf.writeChar(fecha.charAt(i));
            } else {
                billsRaf.writeChar(' ');
            }
        }
        billsRaf.writeDouble(sueldo);
        billsRaf.writeDouble(deduccion);
        billsRaf.writeInt(year);
        billsRaf.writeInt(month + 1); 

        billsRaf.close();

        salesRaf.seek(month * MONTH_REC_SIZE + 8);
        salesRaf.writeBoolean(true);
        salesRaf.close();

        System.out.printf("Empleado %s se le pago Lps. %.2f%n", nombre, total);
    }
    public static void printEmployee(int code) throws IOException {
        // Paso 1: Buscar empleado y mostrar datos
        int pos = findEmployeePosition(code);
        if (pos == -1) {
            System.out.println("Empleado no encontrado");
            return;
        }
        RandomAccessFile empRaf = openEmployeeFile();
        empRaf.seek(pos * EMP_REC_SIZE);

        int codigo = empRaf.readInt();
        StringBuilder nombreSb = new StringBuilder();
        for (int i = 0; i < 30; i++) {
            char c = empRaf.readChar();
            if (c != ' ') nombreSb.append(c);
        }
        double salario = empRaf.readDouble();
        StringBuilder fechaSb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            char c = empRaf.readChar();
            if (c != ' ') fechaSb.append(c);
        }
        empRaf.close();
        
        System.out.println("Codigo: " + codigo);
        System.out.println("Nombre: " + nombreSb.toString().trim());
        System.out.println("Salario: " + salario);
        System.out.println("Fecha de contratacion: " + fechaSb.toString().trim());

        RandomAccessFile salesRaf = salesFileFor(code);
        double totalVentas = 0;
        for (int i = 0; i < 12; i++) {
            salesRaf.seek(i * MONTH_REC_SIZE);
            double venta = salesRaf.readDouble();
            System.out.println("Mes " + (i + 1) + " : " + venta);
            totalVentas += venta;
        }
        salesRaf.close();

        System.out.println("Total de ventas del ano: " + totalVentas);

        RandomAccessFile billsRaf = billsFilefor(code);
        int totalPagos = (int) (billsRaf.length() / RECIBO_REC_SIZE);
        billsRaf.close();

        System.out.println("Total de pagos realizados: " + totalPagos);
    }
    public static void registrarEmpleado(int codigo, String nombre, double salario, 
                                          String fechaContratacion, boolean activo) throws IOException {
        if (employeeExists(codigo)) {
            System.out.println("El codigo de empleado ya existe");
            return;
        }

        RandomAccessFile raf = openEmployeeFile();
        raf.seek(raf.length()); 

        raf.writeInt(codigo);
        
        for (int i = 0; i < 30; i++) {
            if (i < nombre.length()) {
                raf.writeChar(nombre.charAt(i));
            } else {
                raf.writeChar(' ');
            }
        }
        raf.writeDouble(salario);
       
        for (int i = 0; i < 10; i++) {
            if (i < fechaContratacion.length()) {
                raf.writeChar(fechaContratacion.charAt(i));
            } else {
                raf.writeChar(' ');
            }
        }
        raf.writeBoolean(activo);

        raf.close();
        System.out.println("Empleado registrado exitosamente");
    }
    public static void main(String[] args) {
        
        new File(DATA_DIR + "/empleados").mkdirs();

        int opcion;
        do {
            System.out.println("\n=== SISTEMA DE GESTION DE EMPLEADOS ===");
            System.out.println("1. Registrar nuevo empleado");
            System.out.println("2. Agregar venta a empleado");
            System.out.println("3. Pagar empleado");
            System.out.println("4. Mostrar reporte de empleado");
            System.out.println("5. Salir");
            System.out.print("Seleccione una opcion: ");

            opcion = scanner.nextInt();
            scanner.nextLine(); 

            switch (opcion) {
                case 1:
                    try {
                        System.out.print("Ingrese codigo del empleado: ");
                        int cod = scanner.nextInt();
                        scanner.nextLine();
                        System.out.print("Ingrese nombre del empleado: ");
                        String nom = scanner.nextLine();
                        System.out.print("Ingrese salario base: ");
                        double sal = scanner.nextDouble();
                        scanner.nextLine();
                        System.out.print("Ingrese fecha de contratacion (dd/MM/yyyy): ");
                        String fec = scanner.nextLine();
                        registrarEmpleado(cod, nom, sal, fec, true);
                    } catch (IOException e) {
                        System.out.println("Error al registrar empleado: " + e.getMessage());
                    }
                    break;

                case 2:
                    try {
                        System.out.print("Ingrese codigo del empleado: ");
                        int code1 = scanner.nextInt();
                        System.out.print("Ingrese monto de venta: ");
                        double monto = scanner.nextDouble();
                        addSaleToEmployee(code1, monto);
                    } catch (IOException e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                    break;

                case 3:
                    try {
                        System.out.print("Ingrese codigo del empleado: ");
                        int code2 = scanner.nextInt();
                        payEmployee(code2);
                    } catch (IOException e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                    break;

                case 4:
                    try {
                        System.out.print("Ingrese codigo del empleado: ");
                        int code3 = scanner.nextInt();
                        printEmployee(code3);
                    } catch (IOException e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                    break;

                case 5:
                    System.out.println("Saliendo del sistema...");
                    break;

                default:
                    System.out.println("Opcion no valida");
            }
        } while (opcion != 5);
    }
}
