import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

// ==================== STUDENT CLASS ====================
class Student implements Serializable {
    private static final long serialVersionUID = 1L;
    private String studentId;
    private String name;
    private String department;
    private double gpa;

    // Constructor
    public Student(String studentId, String name, String department, double gpa) {
        this.studentId = studentId;
        this.name = name;
        this.department = department;
        setGpa(gpa); // Using setter for validation
    }

    // Getters
    public String getStudentId() { return studentId; }
    public String getName() { return name; }
    public String getDepartment() { return department; }
    public double getGpa() { return gpa; }

    // Setters with validation
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public void setName(String name) { this.name = name; }
    public void setDepartment(String department) { this.department = department; }
    
    public void setGpa(double gpa) {
        if (gpa < 0.0 || gpa > 4.0) {
            throw new IllegalArgumentException("GPA must be between 0.0 and 4.0");
        }
        this.gpa = gpa;
    }

    @Override
    public String toString() {
        return String.format("ID: %s | Name: %s | Dept: %s | GPA: %.2f", 
                            studentId, name, department, gpa);
    }

    // For text file storage - convert to CSV format
    public String toCsvFormat() {
        return studentId + "," + name + "," + department + "," + gpa;
    }

    // For text file reading - create from CSV format
    public static Student fromCsvFormat(String csvLine) {
        String[] parts = csvLine.split(",");
        if (parts.length == 4) {
            return new Student(parts[0], parts[1], parts[2], Double.parseDouble(parts[3]));
        }
        return null;
    }
}

// ==================== FILE MANAGER CLASS ====================
class FileManager {
    private static final String BASE_DIR = "StudentRecords";
    private static final String TEXT_DIR = BASE_DIR + "/text";
    private static final String BINARY_DIR = BASE_DIR + "/binary";
    private static final String SERIAL_DIR = BASE_DIR + "/serialized";
    private static final String BACKUP_DIR = BASE_DIR + "/backup";
    
    private static final String TEXT_FILE = TEXT_DIR + "/students.txt";
    private static final String BINARY_FILE = BINARY_DIR + "/students.dat";
    private static final String SERIAL_FILE = SERIAL_DIR + "/students.ser";
    private static final String BACKUP_FILE = BACKUP_DIR + "/students_backup.ser";

    // Create directories and files
    public static void initializeDirectories() throws IOException {
        File[] directories = {
            new File(TEXT_DIR),
            new File(BINARY_DIR),
            new File(SERIAL_DIR),
            new File(BACKUP_DIR)
        };
        
        for (File dir : directories) {
            if (!dir.exists()) {
                if (dir.mkdirs()) {
                    System.out.println("Created directory: " + dir.getPath());
                }
            }
        }
        
        // Create empty files if they don't exist
        createFileIfNotExists(TEXT_FILE);
        createFileIfNotExists(BINARY_FILE);
        createFileIfNotExists(SERIAL_FILE);
    }

    private static void createFileIfNotExists(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            if (file.createNewFile()) {
                System.out.println("Created file: " + filePath);
            }
        }
    }

    // Display file properties
    public static void displayFileProperties(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            System.out.println("\n=== File Properties: " + file.getName() + " ===");
            System.out.println("Name: " + file.getName());
            System.out.println("Path: " + file.getAbsolutePath());
            System.out.println("Size: " + file.length() + " bytes");
            System.out.println("Last Modified: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    .format(new Date(file.lastModified())));
            System.out.println("Readable: " + file.canRead());
            System.out.println("Writable: " + file.canWrite());
            System.out.println("=====================================\n");
        } else {
            System.out.println("File does not exist: " + filePath);
        }
    }

    // ==================== TEXT FILE OPERATIONS ====================
    public static void saveToTextFile(List<Student> students) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(TEXT_FILE))) {
            for (Student student : students) {
                writer.println(student.toCsvFormat());
            }
        }
        System.out.println("✅ Data saved to text file successfully!");
        displayFileProperties(TEXT_FILE);
    }

    public static List<Student> loadFromTextFile() throws IOException {
        List<Student> students = new ArrayList<>();
        File file = new File(TEXT_FILE);
        
        if (!file.exists() || file.length() == 0) {
            return students;
        }

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (!line.isEmpty()) {
                    Student student = Student.fromCsvFormat(line);
                    if (student != null) {
                        students.add(student);
                    }
                }
            }
        }
        return students;
    }

    // ==================== BINARY FILE OPERATIONS ====================
    public static void saveToBinaryFile(List<Student> students) throws IOException {
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(BINARY_FILE))) {
            dos.writeInt(students.size());
            for (Student student : students) {
                dos.writeUTF(student.getStudentId());
                dos.writeUTF(student.getName());
                dos.writeUTF(student.getDepartment());
                dos.writeDouble(student.getGpa());
            }
        }
        System.out.println("✅ Data saved to binary file successfully!");
        displayFileProperties(BINARY_FILE);
    }

    public static List<Student> loadFromBinaryFile() throws IOException {
        List<Student> students = new ArrayList<>();
        File file = new File(BINARY_FILE);
        
        if (!file.exists() || file.length() == 0) {
            return students;
        }

        try (DataInputStream dis = new DataInputStream(new FileInputStream(BINARY_FILE))) {
            int count = dis.readInt();
            for (int i = 0; i < count; i++) {
                String id = dis.readUTF();
                String name = dis.readUTF();
                String dept = dis.readUTF();
                double gpa = dis.readDouble();
                students.add(new Student(id, name, dept, gpa));
            }
        }
        return students;
    }

    // ==================== SERIALIZATION OPERATIONS ====================
    public static void saveToSerializedFile(List<Student> students) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SERIAL_FILE))) {
            oos.writeObject(students);
        }
        System.out.println("✅ Data saved to serialized file successfully!");
        displayFileProperties(SERIAL_FILE);
    }

    @SuppressWarnings("unchecked")
    public static List<Student> loadFromSerializedFile() throws IOException, ClassNotFoundException {
        File file = new File(SERIAL_FILE);
        
        if (!file.exists() || file.length() == 0) {
            return new ArrayList<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(SERIAL_FILE))) {
            return (List<Student>) ois.readObject();
        }
    }

    // ==================== BACKUP OPERATIONS ====================
    public static void createBackup() throws IOException {
        File source = new File(SERIAL_FILE);
        File backup = new File(BACKUP_FILE);
        
        if (!source.exists() || source.length() == 0) {
            System.out.println("⚠️ No data to backup. Source file is empty or doesn't exist.");
            return;
        }

        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(source));
             BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(backup))) {
            
            byte[] buffer = new byte[1024];
            int bytesRead;
            long totalBytes = 0;
            
            while ((bytesRead = bis.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
                totalBytes += bytesRead;
            }
            
            System.out.println("✅ Backup created successfully!");
            System.out.println("Backup size: " + totalBytes + " bytes");
            displayFileProperties(BACKUP_FILE);
        }
    }

    public static List<Student> restoreFromBackup() throws IOException, ClassNotFoundException {
        File backup = new File(BACKUP_FILE);
        
        if (!backup.exists() || backup.length() == 0) {
            System.out.println("⚠️ No backup file found.");
            return new ArrayList<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(BACKUP_FILE))) {
            @SuppressWarnings("unchecked")
            List<Student> students = (List<Student>) ois.readObject();
            System.out.println("✅ Backup restored successfully! (" + students.size() + " students)");
            return students;
        }
    }
}

// ==================== STUDENT MANAGEMENT SYSTEM ====================
public class StudentManagementSystem {
    private List<Student> students;
    private Scanner scanner;

    public StudentManagementSystem() {
        this.students = new ArrayList<>();
        this.scanner = new Scanner(System.in);
    }

    // Main menu
    public void start() {
        try {
            FileManager.initializeDirectories();
            loadAllData();
        } catch (IOException e) {
            System.out.println("⚠️ Error initializing system: " + e.getMessage());
        }

        while (true) {
            displayMenu();
            int choice = getIntInput("Enter your choice: ");
            
            try {
                switch (choice) {
                    case 1: addStudent(); break;
                    case 2: searchStudent(); break;
                    case 3: updateStudent(); break;
                    case 4: deleteStudent(); break;
                    case 5: displayAllStudents(); break;
                    case 6: generateReport(); break;
                    case 7: saveAllData(); break;
                    case 8: backupData(); break;
                    case 9: restoreData(); break;
                    case 0: 
                        System.out.println("👋 Exiting system. Goodbye!");
                        scanner.close();
                        System.exit(0);
                    default: System.out.println("⚠️ Invalid choice. Please try again.");
                }
            } catch (Exception e) {
                System.out.println("❌ Error: " + e.getMessage());
            }
        }
    }

    private void displayMenu() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("   STUDENT RECORD MANAGEMENT SYSTEM");
        System.out.println("=".repeat(50));
        System.out.println("1. Add Student");
        System.out.println("2. Search Student by ID");
        System.out.println("3. Update Student Information");
        System.out.println("4. Delete Student");
        System.out.println("5. Display All Students");
        System.out.println("6. Generate Report");
        System.out.println("7. Save All Data");
        System.out.println("8. Create Backup");
        System.out.println("9. Restore from Backup");
        System.out.println("0. Exit");
        System.out.println("=".repeat(50));
    }

    // ==================== CRUD OPERATIONS ====================
    
    private void addStudent() {
        System.out.println("\n--- ADD NEW STUDENT ---");
        
        String id;
        while (true) {
            id = getStringInput("Enter Student ID (or type 'auto' for auto-generation): ");
            if (id.equalsIgnoreCase("auto")) {
                id = generateStudentId();
                System.out.println("Generated ID: " + id);
                break;
            }
            if (findStudentById(id) != null) {
                System.out.println("⚠️ Student ID already exists. Please enter a unique ID.");
            } else {
                break;
            }
        }
        
        String name = getStringInput("Enter Name: ");
        String department = getStringInput("Enter Department: ");
        double gpa = getDoubleInput("Enter GPA (0.0 - 4.0): ");
        
        try {
            Student student = new Student(id, name, department, gpa);
            students.add(student);
            System.out.println("✅ Student added successfully!");
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Invalid GPA: " + e.getMessage());
        }
    }

    private void searchStudent() {
        System.out.println("\n--- SEARCH STUDENT ---");
        String id = getStringInput("Enter Student ID to search: ");
        
        Student student = findStudentById(id);
        if (student != null) {
            System.out.println("\n✅ Student Found:");
            System.out.println(student);
            System.out.println("-".repeat(40));
        } else {
            System.out.println("❌ Student with ID " + id + " not found.");
        }
    }

    private void updateStudent() {
        System.out.println("\n--- UPDATE STUDENT ---");
        String id = getStringInput("Enter Student ID to update: ");
        
        Student student = findStudentById(id);
        if (student == null) {
            System.out.println("❌ Student with ID " + id + " not found.");
            return;
        }
        
        System.out.println("\nCurrent Information:");
        System.out.println(student);
        System.out.println("\nEnter new information (press Enter to keep current value):");
        
        String name = getStringInput("Name (" + student.getName() + "): ");
        if (!name.trim().isEmpty()) {
            student.setName(name);
        }
        
        String dept = getStringInput("Department (" + student.getDepartment() + "): ");
        if (!dept.trim().isEmpty()) {
            student.setDepartment(dept);
        }
        
        String gpaStr = getStringInput("GPA (" + student.getGpa() + "): ");
        if (!gpaStr.trim().isEmpty()) {
            try {
                double gpa = Double.parseDouble(gpaStr);
                student.setGpa(gpa);
            } catch (NumberFormatException e) {
                System.out.println("⚠️ Invalid GPA format. Keeping current value.");
            } catch (IllegalArgumentException e) {
                System.out.println("⚠️ " + e.getMessage() + ". Keeping current value.");
            }
        }
        
        System.out.println("✅ Student information updated successfully!");
        System.out.println("Updated: " + student);
    }

    private void deleteStudent() {
        System.out.println("\n--- DELETE STUDENT ---");
        String id = getStringInput("Enter Student ID to delete: ");
        
        Student student = findStudentById(id);
        if (student == null) {
            System.out.println("❌ Student with ID " + id + " not found.");
            return;
        }
        
        System.out.println("\nStudent to delete:");
        System.out.println(student);
        String confirm = getStringInput("\nAre you sure? (yes/no): ");
        
        if (confirm.equalsIgnoreCase("yes")) {
            students.remove(student);
            System.out.println("✅ Student deleted successfully!");
        } else {
            System.out.println("❌ Deletion cancelled.");
        }
    }

    private void displayAllStudents() {
        System.out.println("\n--- ALL STUDENTS ---");
        if (students.isEmpty()) {
            System.out.println("📭 No students in the system.");
            return;
        }
        
        System.out.println("\nTotal Students: " + students.size());
        System.out.println("-".repeat(50));
        
        // Sort by ID for better display
        students.stream()
                .sorted(Comparator.comparing(Student::getStudentId))
                .forEach(student -> System.out.println(student));
        
        System.out.println("-".repeat(50));
    }

    // ==================== REPORT GENERATION ====================
    
    private void generateReport() {
        System.out.println("\n=== STUDENT REPORT ===");
        
        if (students.isEmpty()) {
            System.out.println("📭 No students in the system to generate report.");
            return;
        }
        
        // Using Java Streams for calculations
        long totalStudents = students.size();
        
        OptionalDouble avgGpa = students.stream()
                .mapToDouble(Student::getGpa)
                .average();
        
        Optional<Student> highestGpaStudent = students.stream()
                .max(Comparator.comparingDouble(Student::getGpa));
        
        Optional<Student> lowestGpaStudent = students.stream()
                .min(Comparator.comparingDouble(Student::getGpa));
        
        System.out.println("Total Students: " + totalStudents);
        System.out.println("Average GPA: " + (avgGpa.isPresent() ? String.format("%.2f", avgGpa.getAsDouble()) : "N/A"));
        
        if (highestGpaStudent.isPresent()) {
            Student highest = highestGpaStudent.get();
            System.out.println("Highest GPA: " + String.format("%.2f", highest.getGpa()) + " - " + highest.getName() + " (" + highest.getStudentId() + ")");
        }
        
        if (lowestGpaStudent.isPresent()) {
            Student lowest = lowestGpaStudent.get();
            System.out.println("Lowest GPA: " + String.format("%.2f", lowest.getGpa()) + " - " + lowest.getName() + " (" + lowest.getStudentId() + ")");
        }
        
        // Department-wise statistics
        System.out.println("\n--- Department-wise Statistics ---");
        Map<String, Long> deptCount = students.stream()
                .collect(Collectors.groupingBy(Student::getDepartment, Collectors.counting()));
        
        deptCount.forEach((dept, count) -> 
            System.out.println(dept + ": " + count + " students"));
        
        System.out.println("=".repeat(40));
    }

    // ==================== DATA PERSISTENCE ====================
    
    private void loadAllData() {
        System.out.println("\n🔄 Loading data from all sources...");
        try {
            // Try loading from serialized first (most complete)
            List<Student> serializedStudents = FileManager.loadFromSerializedFile();
            if (!serializedStudents.isEmpty()) {
                students = serializedStudents;
                System.out.println("✅ Loaded " + students.size() + " students from serialized file.");
                return;
            }
            
            // Fallback to binary
            List<Student> binaryStudents = FileManager.loadFromBinaryFile();
            if (!binaryStudents.isEmpty()) {
                students = binaryStudents;
                System.out.println("✅ Loaded " + students.size() + " students from binary file.");
                return;
            }
            
            // Fallback to text
            List<Student> textStudents = FileManager.loadFromTextFile();
            if (!textStudents.isEmpty()) {
                students = textStudents;
                System.out.println("✅ Loaded " + students.size() + " students from text file.");
                return;
            }
            
            System.out.println("ℹ️ No existing data found. Starting with empty database.");
            
        } catch (Exception e) {
            System.out.println("⚠️ Error loading data: " + e.getMessage());
            students = new ArrayList<>();
        }
    }

    private void saveAllData() {
        System.out.println("\n💾 Saving data to all formats...");
        try {
            FileManager.saveToTextFile(students);
            FileManager.saveToBinaryFile(students);
            FileManager.saveToSerializedFile(students);
            System.out.println("✅ Data saved successfully to all formats!");
        } catch (IOException e) {
            System.out.println("❌ Error saving data: " + e.getMessage());
        }
    }

    private void backupData() {
        System.out.println("\n📦 Creating backup...");
        try {
            // First save current data to serialized file
            FileManager.saveToSerializedFile(students);
            // Then backup that file
            FileManager.createBackup();
        } catch (IOException e) {
            System.out.println("❌ Error creating backup: " + e.getMessage());
        }
    }

    private void restoreData() {
        System.out.println("\n🔄 Restoring from backup...");
        try {
            List<Student> restored = FileManager.restoreFromBackup();
            if (!restored.isEmpty()) {
                students = restored;
                System.out.println("✅ Data restored successfully! (" + students.size() + " students)");
                // Save restored data to all formats
                saveAllData();
            }
        } catch (Exception e) {
            System.out.println("❌ Error restoring from backup: " + e.getMessage());
        }
    }

    // ==================== HELPER METHODS ====================
    
    private Student findStudentById(String id) {
        return students.stream()
                .filter(s -> s.getStudentId().equalsIgnoreCase(id))
                .findFirst()
                .orElse(null);
    }

    private String generateStudentId() {
        if (students.isEmpty()) {
            return "STU001";
        }
        
        // Find max ID number
        int maxId = students.stream()
                .map(Student::getStudentId)
                .filter(id -> id.startsWith("STU"))
                .map(id -> id.substring(3))
                .filter(id -> id.matches("\\d+"))
                .mapToInt(Integer::parseInt)
                .max()
                .orElse(0);
        
        return String.format("STU%03d", maxId + 1);
    }

    private String getStringInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private int getIntInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("⚠️ Please enter a valid number.");
            }
        }
    }

    private double getDoubleInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                double value = Double.parseDouble(scanner.nextLine().trim());
                if (value >= 0 && value <= 4.0) {
                    return value;
                }
                System.out.println("⚠️ GPA must be between 0.0 and 4.0.");
            } catch (NumberFormatException e) {
                System.out.println("⚠️ Please enter a valid number.");
            }
        }
    }

    // ==================== MAIN METHOD ====================
    public static void main(String[] args) {
        System.out.println("=".repeat(60));
        System.out.println("   WELCOME TO STUDENT RECORD MANAGEMENT SYSTEM");
        System.out.println("=".repeat(60));
        System.out.println("System initialized: " + new Date());
        System.out.println("=".repeat(60));
        
        StudentManagementSystem system = new StudentManagementSystem();
        system.start();
    }
}