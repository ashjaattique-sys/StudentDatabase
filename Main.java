import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class Main extends JFrame {

    private static final String URL = "jdbc:sqlite:student.db";

    private JTextField rollField;
    private JTextField nameField;
    private JTextField classField;
    private JTable table;
    private DefaultTableModel model;

    public Main() {
        setTitle("Student Database Management System");
        setSize(750, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        createDatabase();
        createGUI();
        loadStudents();
    }

    private void createDatabase() {
        String sql = """
                CREATE TABLE IF NOT EXISTS students (
                    roll_no INTEGER PRIMARY KEY,
                    name TEXT NOT NULL,
                    class_name TEXT NOT NULL
                )
                """;

        try (Connection con = DriverManager.getConnection(URL);
             Statement st = con.createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            showError(e);
        }
    }

    private void createGUI() {
        setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel(new GridLayout(3, 2, 8, 8));
        form.setBorder(BorderFactory.createTitledBorder("Student Information"));

        rollField = new JTextField();
        nameField = new JTextField();
        classField = new JTextField();

        form.add(new JLabel("Roll Number:"));
        form.add(rollField);
        form.add(new JLabel("Student Name:"));
        form.add(nameField);
        form.add(new JLabel("Class:"));
        form.add(classField);

        JButton addButton = new JButton("Add Student");
        JButton editButton = new JButton("Edit Student");
        JButton deleteButton = new JButton("Delete Student");
        JButton clearButton = new JButton("Clear Fields");

        JPanel buttons = new JPanel(new FlowLayout());
        buttons.add(addButton);
        buttons.add(editButton);
        buttons.add(deleteButton);
        buttons.add(clearButton);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(form, BorderLayout.CENTER);
        topPanel.add(buttons, BorderLayout.SOUTH);

        model = new DefaultTableModel(
                new String[]{"Roll Number", "Name", "Class"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        addButton.addActionListener(e -> addStudent());
        editButton.addActionListener(e -> editStudent());
        deleteButton.addActionListener(e -> deleteStudent());
        clearButton.addActionListener(e -> clearFields());

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    rollField.setText(model.getValueAt(row, 0).toString());
                    nameField.setText(model.getValueAt(row, 1).toString());
                    classField.setText(model.getValueAt(row, 2).toString());
                }
            }
        });
    }

    private void addStudent() {
        try {
            int roll = Integer.parseInt(rollField.getText().trim());
            String name = nameField.getText().trim();
            String studentClass = classField.getText().trim();

            if (name.isEmpty() || studentClass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields.");
                return;
            }

            String sql = "INSERT INTO students (roll_no, name, class_name) VALUES (?, ?, ?)";

            try (Connection con = DriverManager.getConnection(URL);
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, roll);
                ps.setString(2, name);
                ps.setString(3, studentClass);
                ps.executeUpdate();
            }

            loadStudents();
            clearFields();
            JOptionPane.showMessageDialog(this, "Student added successfully!");

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Roll number must be a valid number.");
        } catch (SQLException e) {
            showError(e);
        }
    }

    private void editStudent() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a student to edit.");
            return;
        }

        try {
            int oldRoll = Integer.parseInt(model.getValueAt(row, 0).toString());
            int newRoll = Integer.parseInt(rollField.getText().trim());
            String name = nameField.getText().trim();
            String studentClass = classField.getText().trim();

            if (name.isEmpty() || studentClass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields.");
                return;
            }

            String sql = "UPDATE students SET roll_no=?, name=?, class_name=? WHERE roll_no=?";

            try (Connection con = DriverManager.getConnection(URL);
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, newRoll);
                ps.setString(2, name);
                ps.setString(3, studentClass);
                ps.setInt(4, oldRoll);

                int updated = ps.executeUpdate();
                if (updated > 0) {
                    loadStudents();
                    clearFields();
                    JOptionPane.showMessageDialog(this, "Student updated successfully!");
                }
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Roll number must be a valid number.");
        } catch (SQLException e) {
            showError(e);
        }
    }

    private void deleteStudent() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a student to delete.");
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete this student?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            int roll = Integer.parseInt(model.getValueAt(row, 0).toString());
            String sql = "DELETE FROM students WHERE roll_no=?";

            try (Connection con = DriverManager.getConnection(URL);
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, roll);
                ps.executeUpdate();
            }

            loadStudents();
            clearFields();
            JOptionPane.showMessageDialog(this, "Student deleted successfully!");

        } catch (SQLException e) {
            showError(e);
        }
    }

    private void loadStudents() {
        model.setRowCount(0);
        String sql = "SELECT roll_no, name, class_name FROM students ORDER BY roll_no";

        try (Connection con = DriverManager.getConnection(URL);
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("roll_no"),
                        rs.getString("name"),
                        rs.getString("class_name")
                });
            }
        } catch (SQLException e) {
            showError(e);
        }
    }

    private void clearFields() {
        rollField.setText("");
        nameField.setText("");
        classField.setText("");
        table.clearSelection();
    }

    private void showError(SQLException e) {
        JOptionPane.showMessageDialog(
                this,
                "Database error: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Main app = new Main();
            app.setVisible(true);
        });
    }
}
