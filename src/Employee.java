/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Sarah
 */
    import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;

public class Employee {
    private final int ID;
    private final String name;
    private final String department;

    public Employee(int id, String name, String department) {
        this.ID = id;
        this.name = name;
        this.department = department;
    }

    public int getID() {
        return ID;
    }

    public String getName() {
        return name;
    }

    public String getDepartment() {
        return department;
    }

    public void writeToFile(DataOutputStream writer) throws IOException {
        writer.writeInt(ID);
        writer.writeUTF(name);
        writer.writeUTF(department);
    }

    public static Employee readFromFile(DataInputStream reader) throws IOException {
        try {
            int id = reader.readInt();
            String name = reader.readUTF();
            String department = reader.readUTF();
            return new Employee(id, name, department);
        } catch (EOFException e) {
            throw new IOException("The data from the file could not be read correctly..", e);
        }
    }
}

