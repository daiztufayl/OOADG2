import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ViewDoctorsFrame extends JFrame {

    public ViewDoctorsFrame() {

        setTitle("View Doctors");
        setSize(500, 300);
        setLocationRelativeTo(null);

        String[] columns = {
            "Doctor ID",
            "Doctor Name",
            "Specialisation"
        };
    
        Object[][] data = new Object[DoctorList.doctors.size()][3];
    
        for (int i = 0; i < DoctorList.doctors.size(); i++) {
    
            Doctor doctor = DoctorList.doctors.get(i);
    
            data[i][0] = doctor.getDoctorID();
            data[i][1] = doctor.getDoctorName();
            data[i][2] = doctor.getSpecialization();
        }

        JTable table = new JTable(
                new DefaultTableModel(data, columns)
        );

        JScrollPane scrollPane = new JScrollPane(table);

        add(scrollPane, BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ViewDoctorsFrame().setVisible(true);
        });
    }
}