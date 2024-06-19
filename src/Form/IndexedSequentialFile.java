package Form;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Sarah
 */
import java.io.*;
import java.util.*;

/*import java.util.KeyNotFoundException;*/

public class IndexedSequentialFile<T> implements AutoCloseable {
    private final String dataFilePath;
    private final String indexFilePath;
    private final Map<Integer, Long> index;

    public IndexedSequentialFile(String dataFilePath, String indexFilePath) {
        this.dataFilePath = dataFilePath != null ? dataFilePath : "";
        this.indexFilePath = indexFilePath != null ? indexFilePath : "";
        this.index = new HashMap<>();
    }

    public List<T> getAllRecords() throws IOException {
        loadIndex();

        List<T> records = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(dataFilePath))) {
            for (long position : index.values()) {
                reader.skip(position);
                String line = reader.readLine();
                T record = parseRecord(line);
                records.add(record);
            }
        }

        return records;
    }

    public void insertRecord(int key, T record) throws IOException {
        long position = getEndOfFilePosition(dataFilePath);

        try (BufferedWriter dataWriter = new BufferedWriter(new FileWriter(dataFilePath, true));
             BufferedWriter indexWriter = new BufferedWriter(new FileWriter(indexFilePath, true))) {
            dataWriter.write(record.toString() + "\n");
            indexWriter.write(key + ":" + position + "\n");
        }
    }

    public T getRecordByKey(int key) throws IOException, Exception {
        loadIndex();

        Long position = index.get(key);
        if (position == null) {
            throw new Exception("Key " + key + " not found in index.");
        }

        try (RandomAccessFile file = new RandomAccessFile(dataFilePath, "r")) {
            file.seek(position);
            String line = file.readLine();
            return parseRecord(line);
        }
    }

    public void loadIndex() throws IOException {
        index.clear();

        if (!new File(indexFilePath).exists()) {
            System.out.println("Error: The index file does not exist at the specified location: " + indexFilePath);
            return;
        }

        try (BufferedReader indexReader = new BufferedReader(new FileReader(indexFilePath))) {
            String line;
            while ((line = indexReader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    int key = Integer.parseInt(parts[0]);
                    long position = Long.parseLong(parts[1]);
                    index.put(key, position);
                } else {
                    System.out.println("Error: Line with incorrect format in the index file: " + line);
                }
            }
        }
    }

    private long getEndOfFilePosition(String filePath) throws IOException {
        try (RandomAccessFile file = new RandomAccessFile(filePath, "r")) {
            return file.length();
        }
    }

    private T parseRecord(String line) {
        if (line == null || line.isEmpty()) {
            return null;
        }

        String[] parts = line.split(",");
        if (parts.length != 4) {
            return null;
        }

        int id = Integer.parseInt(parts[0]);
        String name = parts[1];
        String phone = parts[2];
        String email = parts[3];

        return (T) new Contact(id, name, phone, email);
    }

    public void updateRecord(int key, T newRecord) throws IOException {
        Long position = index.get(key);
        if (position == null) {
            return;
        }

        try (RandomAccessFile file = new RandomAccessFile(dataFilePath, "rw")) {
            file.seek(position);
            file.writeBytes(newRecord.toString() + "\n");
        }
    }

    public void deleteRecord(int key) throws IOException {
        Long position = index.remove(key);
        if (position == null) {
            return;
        }

        try (BufferedWriter indexWriter = new BufferedWriter(new FileWriter(indexFilePath))) {
            for (Map.Entry<Integer, Long> entry : index.entrySet()) {
                indexWriter.write(entry.getKey() + ":" + entry.getValue() + "\n");
            }
        }

        updateDataFile();
    }

    private void updateDataFile() throws IOException {
        File tempFile = File.createTempFile("temp", null);

        try (BufferedWriter tempWriter = new BufferedWriter(new FileWriter(tempFile));
             BufferedReader reader = new BufferedReader(new FileReader(dataFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                tempWriter.write(line + "\n");
            }
        }

        File dataFile = new File(dataFilePath);
        if (dataFile.delete()) {
            tempFile.renameTo(dataFile);
        }
    }

    @Override
    public void close() throws Exception {
        // No hay recursos adicionales que cerrar, pero puedes agregarlos si es necesario
    }
}
