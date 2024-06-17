import javax.swing.*;
import java.awt.*;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.File;

import javax.swing.table.DefaultTableModel;

import java.io.FileInputStream;

import java.io.IOException;

import java.io.OutputStream;
import javax.swing.table.DefaultTableCellRenderer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;


public class LoginTilang extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;

    private String loggedInUsername; // Track logged in user

    public LoginTilang() {
        super("Login");
        initComponents();
    }

    private void initComponents() {
        JPanel panel = new JPanel(new BorderLayout()); // Menggunakan BorderLayout untuk menempatkan komponen

        JLabel titleLabel = new JLabel("Welcome to Tilang App", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18)); // Set font dan ukuran untuk judul
        panel.add(titleLabel, BorderLayout.NORTH); // Tambahkan judul di bagian atas

        JPanel centerPanel = new JPanel(new GridLayout(3, 2)); // Panel untuk input dan tombol
        JLabel usernameLabel = new JLabel("Username:");
        usernameField = new JTextField(20);

        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField(20);

        loginButton = new JButton("Login");
        registerButton = new JButton("Register");

        centerPanel.add(usernameLabel);
        centerPanel.add(usernameField);
        centerPanel.add(passwordLabel);
        centerPanel.add(passwordField);
        centerPanel.add(registerButton);
        centerPanel.add(loginButton);
      

        panel.add(centerPanel, BorderLayout.CENTER); // Tambahkan panel input dan tombol di tengah

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent loginActionEvent) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());

                // Create HttpClient
                HttpClient client = HttpClient.newHttpClient();

                // Create JSON body
                String jsonBody = "{\"username\":\"" + username + "\", \"password\":\"" + password + "\"}";

                // Create HttpRequest
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8000/login")) // Sesuaikan URL
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                        .build();

                // Send async request
                client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                        .thenApply(response -> {
                            System.out.println("Response Code: " + response.statusCode());
                            System.out.println("Response Body: " + response.body());

                            // Handle response accordingly
                            if (response.statusCode() == 200) {
                                loggedInUsername = username; // Set user yang login
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        showDashboard();
                                    }
                                });
                            } else {
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        JOptionPane.showMessageDialog(LoginTilang.this,
                                                "Login failed. Status code: " + response.statusCode(),
                                                "Login Status",
                                                JOptionPane.ERROR_MESSAGE);
                                    }
                                });
                            }
                            return response;
                        })
                        .exceptionally(exception -> {
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    JOptionPane.showMessageDialog(LoginTilang.this,
                                            "Error sending HTTP request: " + exception.getMessage(),
                                            "Error",
                                            JOptionPane.ERROR_MESSAGE);
                                }
                            });
                            return null;
                        });

                // Bersihkan field password
                passwordField.setText("");
            }
        });

        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Buka dialog untuk registrasi
                RegisterDialog registerDialog = new RegisterDialog(LoginTilang.this);
                registerDialog.setVisible(true);
            }
        });

        add(panel);
        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void showDashboard() {
        DashboardFrame dashboardFrame = new DashboardFrame(loggedInUsername, this);
        dashboardFrame.setVisible(true);
        setVisible(false); // Sembunyikan frame login
    }

    public void logout() {
        loggedInUsername = null; // Bersihkan user yang login
        setVisible(true); // Tampilkan kembali frame login
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                LoginTilang mainApp = new LoginTilang();
                mainApp.setVisible(true);
            }
        });
    }
}


class DashboardFrame extends JFrame {
    private String username;
    private LoginTilang mainApp;
    private JTextField usernameField;
    private JTable resultsTable; // Replace JTextArea with JTable
    private DefaultTableModel tableModel;

    public DashboardFrame(String username, LoginTilang mainApp) {
        super("Dashboard");
        this.username = username;
        this.mainApp = mainApp;
        initComponents();
    }

    private void initComponents() {
        setSize(800, 400); // Adjust size for better display
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        JLabel welcomeLabel = new JLabel("Halo polisi " + username + ", selamat datang", JLabel.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18)); // Set font and text size
        panel.add(welcomeLabel, BorderLayout.NORTH); // Add welcome label at the top

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        JButton registerViolationButton = new JButton("Register Violation");
        registerViolationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Get username input
                String username = JOptionPane.showInputDialog(DashboardFrame.this, "Masukkan username yang akan dicek:");
                if (username != null && !username.isEmpty()) {
                    // Open register violation dialog
                    String violationType = JOptionPane.showInputDialog(DashboardFrame.this, "Masukkan jenis pelanggaran:");
                    if (violationType != null && !violationType.isEmpty()) {
                        String violationLocation = JOptionPane.showInputDialog(DashboardFrame.this, "Masukkan lokasi pelanggaran:");
                        if (violationLocation != null && !violationLocation.isEmpty()) {
                            String filename = chooseFile();
                            if (filename != null) {
                                // Print data for debugging
                                System.out.println("Upload file: " + filename);
                                System.out.println("Violation type: " + violationType);
                                System.out.println("Violation location: " + violationLocation);

                                // Send data to server
                                sendViolationData(username, violationType, violationLocation, filename);
                            }
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(DashboardFrame.this,
                            "Username harus diisi untuk melanjutkan",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        buttonPanel.add(registerViolationButton);

        JButton viewUnpaidUsersButton = new JButton("View Unpaid Users");
        viewUnpaidUsersButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Fetch unpaid users and display in the table
                fetchUnpaidUsers();
            }
        });
        buttonPanel.add(viewUnpaidUsersButton);

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainApp.logout(); // Call logout method from MainApp
                dispose(); // Close dashboard frame
            }
        });
        buttonPanel.add(logoutButton);

        panel.add(buttonPanel, BorderLayout.SOUTH); // Add buttons panel at the bottom

        // Create a table to display results
        tableModel = new DefaultTableModel();
        tableModel.addColumn("Username");
        tableModel.addColumn("Violation Type");
        tableModel.addColumn("Violation Time");// Add columns based on your server response
        resultsTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(resultsTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        JButton fetchNotificationsButton = new JButton("Fetch Notifications");
fetchNotificationsButton.addActionListener(new ActionListener() {
    @Override
    public void actionPerformed(ActionEvent e) {
        int rowCount = tableModel.getRowCount();
        if (rowCount > 0) {
            // Ambil semua username dari tabel
            for (int i = 0; i < rowCount; i++) {
                String username = (String) tableModel.getValueAt(i, 0);
                fetchNotifications(username);
            }
        } else {
            JOptionPane.showMessageDialog(DashboardFrame.this,
                    "No users available to fetch notifications for.",
                    "Notification Fetching",
                    JOptionPane.WARNING_MESSAGE);
        }
    }
});
panel.add(fetchNotificationsButton, BorderLayout.EAST); // Tambah

        add(panel);
        setLocationRelativeTo(null);
    }

    // Method untuk memilih file
    private String chooseFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(DashboardFrame.this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            return selectedFile.getAbsolutePath();
        }
        return null;
    }

    // Method untuk mengirim data pelanggaran ke server
    private void sendViolationData(String username, String violationType, String violationLocation, String filename) {
        String boundary = Long.toHexString(System.currentTimeMillis()); // Just generate some unique random value
        String LINE_FEED = "\r\n";
        HttpURLConnection httpConn;
        String charset = "UTF-8";

        try {
            URL url = URI.create("http://localhost:8000/registerviolation").toURL();
            httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setUseCaches(false);
            httpConn.setDoOutput(true); // Indicates POST method
            httpConn.setDoInput(true);
            httpConn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            httpConn.setRequestProperty("User-Agent", "CodeJava Agent");
            httpConn.setRequestProperty("Test", "Bonjour");

            try (OutputStream outputStream = httpConn.getOutputStream()) {
                // Add form fields
                StringBuilder sb = new StringBuilder();
                sb.append("--").append(boundary).append(LINE_FEED);
                sb.append("Content-Disposition: form-data; name=\"username\"").append(LINE_FEED);
                sb.append("Content-Type: text/plain; charset=").append(charset).append(LINE_FEED);
                sb.append(LINE_FEED);
                sb.append(username).append(LINE_FEED);

                sb.append("--").append(boundary).append(LINE_FEED);
                sb.append("Content-Disposition: form-data; name=\"violationtype\"").append(LINE_FEED);
                sb.append("Content-Type: text/plain; charset=").append(charset).append(LINE_FEED);
                sb.append(LINE_FEED);
                sb.append(violationType).append(LINE_FEED);

                sb.append("--").append(boundary).append(LINE_FEED);
                sb.append("Content-Disposition: form-data; name=\"violationlocation\"").append(LINE_FEED);
                sb.append("Content-Type: text/plain; charset=").append(charset).append(LINE_FEED);
                sb.append(LINE_FEED);
                sb.append(violationLocation).append(LINE_FEED);

                outputStream.write(sb.toString().getBytes(charset));

                // Add file part
                File file = new File(filename);
                sb = new StringBuilder();
                sb.append("--").append(boundary).append(LINE_FEED);
                sb.append("Content-Disposition: form-data; name=\"image\"; filename=\"").append(file.getName()).append("\"").append(LINE_FEED);
                sb.append("Content-Type: ").append(HttpURLConnection.guessContentTypeFromName(file.getName())).append(LINE_FEED);
                sb.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
                sb.append(LINE_FEED);

                outputStream.write(sb.toString().getBytes(charset));

                try (FileInputStream inputStream = new FileInputStream(file)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                }

                outputStream.write(LINE_FEED.getBytes(charset));
                outputStream.write(("--" + boundary + "--").getBytes(charset));
                outputStream.flush();
            }

            int responseCode = httpConn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Jika pengiriman sukses, simpan file ke direktori uploads
                // String savedFilePath = saveToUploads(filename);
                
                    JOptionPane.showMessageDialog(DashboardFrame.this, "Pelanggaran berhasil direkam", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(DashboardFrame.this, "Gagal menyimpan file ke uploads", "Error", JOptionPane.ERROR_MESSAGE);
                }
            
            httpConn.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Exception: " + e.getMessage());
            JOptionPane.showMessageDialog(DashboardFrame.this, "Gagal merekam pelanggaran. Terjadi kesalahan IO", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
private void fetchNotifications(String username) {
    try {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8000/login/" + username))
                .GET()
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    System.out.println("Response Code: " + response.statusCode());
                    System.out.println("Response Body: " + response.body());

                    if (response.statusCode() == 200) {
                        String responseBody = response.body().trim(); // Trim to remove any extra whitespace

                        // Check if response body contains "No new violations found"
                        if ("No new violations found".equals(responseBody)) {
                            // No need to show notification
                            System.out.println("No new violations found");
                        } else if ("New violations found".equals(responseBody)) {
                            // Show notification for new violations
                            SwingUtilities.invokeLater(() -> {
                                JOptionPane.showMessageDialog(DashboardFrame.this,
                                        "New violations found and notified!",
                                        "Notification",
                                        JOptionPane.INFORMATION_MESSAGE);
                            });
                        } else {
                            // Unexpected response
                            System.out.println("Unexpected response: " + responseBody);
                        }
                    } else {
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(DashboardFrame.this,
                                    "Error fetching notifications: Unexpected response code " + response.statusCode(),
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE);
                        });
                    }
                    return response;
                })
                .exceptionally(exception -> {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(DashboardFrame.this,
                                "Error fetching notifications: " + exception.getMessage(),
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    });
                    return null;
                });
    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(DashboardFrame.this,
                "Error fetching notifications: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
    }
}


    private void fetchUnpaidUsers() {
        try {
            URL url = URI.create("http://localhost:8000/userbelumbayar").toURL(); // Sesuaikan dengan URL endpoint yang benar
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new RuntimeException("Failed : HTTP error code : " + connection.getResponseCode());
            }

            // Kosongkan tabel sebelum menambahkan data baru
            tableModel.setRowCount(0);

            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                // Split the line to get username and violation type
                String[] data = line.split(",");
                String username = data[0].trim();
                String violationType = data[1].trim();
                String violationTime = data[2].trim();

                // Add row to table model
                tableModel.addRow(new Object[]{username, violationType, violationTime});
            }
            br.close();

            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching unpaid users: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
         DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        resultsTable.setDefaultRenderer(Object.class, centerRenderer);
    }
}



class RegisterDialog extends JDialog {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField emailField; // Added email field
    private JButton registerButton;

    public RegisterDialog(Frame parent) {
        super(parent, "Register", true);
        initComponents();
    }

    private void initComponents() {
        JPanel panel = new JPanel(new GridLayout(5, 2)); // Increased rows for email field

        JLabel usernameLabel = new JLabel("Username:");
        usernameField = new JTextField(20);

        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField(20);

        JLabel emailLabel = new JLabel("Email:"); // Added email label
        emailField = new JTextField(20); // Added email text field

        registerButton = new JButton("Register");

        panel.add(usernameLabel);
        panel.add(usernameField);
        panel.add(passwordLabel);
        panel.add(passwordField);
        panel.add(emailLabel); // Added email label
        panel.add(emailField); // Added email text field
        panel.add(new JLabel());
        panel.add(registerButton);
        panel.add(new JLabel());
        panel.add(new JLabel()); // Empty label for layout spacing

        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
                String email = emailField.getText(); // Get email input

                // Validate input
                if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
                    JOptionPane.showMessageDialog(RegisterDialog.this,
                            "Username, password, and email are required",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Create HttpClient
                HttpClient client = HttpClient.newHttpClient();

                // Prepare JSON body
                String jsonBody = "{\"username\":\"" + username + "\", \"password\":\"" + password +
                        "\", \"email\":\"" + email + "\", \"isadmin\": true}";

                // Create HttpRequest
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8000/register")) // Adjust URL accordingly
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                        .build();

                // Send async request
                client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                        .thenApply(response -> {
                            System.out.println("Response Code: " + response.statusCode());
                            System.out.println("Response Body: " + response.body());

                            // Handle response accordingly
                            if (response.statusCode() == 200) {
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        JOptionPane.showMessageDialog(RegisterDialog.this,
                                                "Registration successful!",
                                                "Success",
                                                JOptionPane.INFORMATION_MESSAGE);
                                        dispose(); // Close register dialog
                                    }
                                });
                            } else {
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        JOptionPane.showMessageDialog(RegisterDialog.this,
                                                "Registration failed. Status code: " + response.statusCode(),
                                                "Registration Status",
                                                JOptionPane.ERROR_MESSAGE);
                                    }
                                });
                            }
                            return response;
                        })
                        .exceptionally(exception -> {
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    JOptionPane.showMessageDialog(RegisterDialog.this,
                                            "Error sending HTTP request: " + exception.getMessage(),
                                            "Error",
                                            JOptionPane.ERROR_MESSAGE);
                                }
                            });
                            return null;
                        });
            }
        });

        add(panel);
        pack();
        setLocationRelativeTo(null);
    }
}


   