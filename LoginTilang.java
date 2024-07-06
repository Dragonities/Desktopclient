import javax.swing.*;
import java.awt.*;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.File;
import java.net.URLEncoder;

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
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;



public class LoginTilang extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;

    private String loggedInUsername; 

    public LoginTilang() {
        super("Login");
        initComponents();
    }

    private void initComponents() {
        JPanel panel = new JPanel(new BorderLayout()); 
        JLabel titleLabel = new JLabel("Welcome to Tilang App", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18)); 
        panel.add(titleLabel, BorderLayout.NORTH); 

        JPanel centerPanel = new JPanel(new GridLayout(3, 2));
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
      

        panel.add(centerPanel, BorderLayout.CENTER); 

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
                        .uri(URI.create("http://localhost:8000/login")) 
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
                                loggedInUsername = username;
                                SwingUtilities.invokeLater(() -> showDashboard());
                            } else if (response.statusCode() == 401) {
                                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(LoginTilang.this,
                                        "Incorrect password. Please try again.",
                                        "Login Status",
                                        JOptionPane.ERROR_MESSAGE));
                            } else if (response.statusCode() == 404) {
                                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(LoginTilang.this,
                                        "Username not found. Please try again.",
                                        "Login Status",
                                        JOptionPane.ERROR_MESSAGE));
                            } else {
                                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(LoginTilang.this,
                                        "Login failed. Status code: " + response.statusCode(),
                                        "Login Status",
                                        JOptionPane.ERROR_MESSAGE));
                            }
                            return response;
                        })
                        .exceptionally(exception -> {
                            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(LoginTilang.this,
                                    "Error sending HTTP request: " + exception.getMessage(),
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE));
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
        setVisible(false); 
    }

    public void logout() {
        loggedInUsername = null;
        setVisible(true);
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
    private JTable resultsTable; 
    private DefaultTableModel tableModel;

    public DashboardFrame(String username, LoginTilang mainApp) {
        super("Tilang App");
        this.username = username;
        this.mainApp = mainApp;
        initComponents();
    }

    private void initComponents() {
        setSize(800, 400); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        JLabel welcomeLabel = new JLabel("Halo polisi " + username + ", selamat datang", JLabel.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18)); 
        panel.add(welcomeLabel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

       JButton registerViolationButton = new JButton("Register Violation");
registerViolationButton.addActionListener(new ActionListener() {
    @Override
    public void actionPerformed(ActionEvent e) {
        String username = JOptionPane.showInputDialog(DashboardFrame.this, "Masukkan username yang akan dicek:", "Register Violation", JOptionPane.PLAIN_MESSAGE);
        if (username != null && !username.isEmpty()) {
            
            String[] violationTypes = {"Menabrak", "Menerobos", "Melanggar"};
            int violationTypeOption = JOptionPane.showOptionDialog(DashboardFrame.this, 
                "Pilih jenis pelanggaran:", 
                "Register Violation", 
                JOptionPane.DEFAULT_OPTION, 
                JOptionPane.PLAIN_MESSAGE, 
                null, 
                violationTypes, 
                violationTypes[0]);
            
            if (violationTypeOption != JOptionPane.CLOSED_OPTION) {
                String violationType = violationTypes[violationTypeOption];
                
                String violationLocation = JOptionPane.showInputDialog(DashboardFrame.this, "Masukkan lokasi pelanggaran:", "Register Violation", JOptionPane.PLAIN_MESSAGE);
                if (violationLocation != null && !violationLocation.isEmpty()) {
                    String filename = chooseFile();
                    if (filename != null) {
                        
                        System.out.println("Upload file: " + filename);
                        System.out.println("Violation type: " + violationType);
                        System.out.println("Violation location: " + violationLocation);

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
                fetchUnpaidUsers();
            }
        });
        buttonPanel.add(viewUnpaidUsersButton);

          JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainApp.logout();
                dispose();
            }
        });
        buttonPanel.add(logoutButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        tableModel = new DefaultTableModel();
        tableModel.addColumn("Username");
        tableModel.addColumn("Violation Type");
        tableModel.addColumn("Violation Time");
        resultsTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(resultsTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // JButton fetchNotificationsButton = new JButton("Send Notifications");
        // buttonPanel.add(fetchNotificationsButton);

        JButton viewViolationProofButton = new JButton("View Violation Proof");
        viewViolationProofButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = resultsTable.getSelectedRow();
                if (selectedRow >= 0) {
                    String username = (String) tableModel.getValueAt(selectedRow, 0);
                    fetchViolationProof(username);
                } else {
                    JOptionPane.showMessageDialog(null, "Please select a user to view violation proof.");
                }
            }
        });
JButton viewPaymentStatusButton = new JButton("View Payment Status");
viewPaymentStatusButton.addActionListener(new ActionListener() {
    @Override
    public void actionPerformed(ActionEvent e) {
        int selectedRow = resultsTable.getSelectedRow();
        if (selectedRow >= 0) {
            String username = (String) tableModel.getValueAt(selectedRow, 0);
            // Misalnya, kita ingin melihat payment status dengan index 0 (pertama)
            int index = 0;
            fetchPaymentStatus(username, index); // Memasukkan index ke dalam method call
        } else {
            JOptionPane.showMessageDialog(null, "Please select a user to view payment status.");
        }
    }
});
buttonPanel.add(viewPaymentStatusButton);





        buttonPanel.add(viewViolationProofButton);

        add(panel);
    
// fetchNotificationsButton.addActionListener(new ActionListener() {
//     @Override
//     public void actionPerformed(ActionEvent e) {
//         int rowCount = tableModel.getRowCount();
//         if (rowCount > 0) {
//             // Ambil semua username dari tabel
//             for (int i = 0; i < rowCount; i++) {
//                 String username = (String) tableModel.getValueAt(i, 0);
//                 fetchNotifications(username);
//             }
//         } else {
//             JOptionPane.showMessageDialog(DashboardFrame.this,
//                     "No users available to fetch notifications for.",
//                     "Notification Fetching",
//                     JOptionPane.WARNING_MESSAGE);
//         }
//     }
// });
// panel.add(fetchNotificationsButton, BorderLayout.EAST); 

//         add(panel);
//         setLocationRelativeTo(null);
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

private void fetchPaymentStatus(String username, int index) {
    HttpURLConnection connection = null;
    try {
        URI uri = URI.create("http://localhost:8000/seepaymentstatus/" + username + "/" + index);
        URL url = uri.toURL();
        connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setUseCaches(false);
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestProperty("User-Agent", "JavaClient");
        connection.setRequestProperty("Accept", "image/jpeg");

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            // Handle successful response (image available)
            InputStream inputStream = connection.getInputStream();
            BufferedImage image = ImageIO.read(inputStream); // Use ImageIO to read the image
            if (image != null) {
                ImageIcon icon = new ImageIcon(image);
                JLabel label = new JLabel(icon);

                // Create a panel to hold the image and button
                JPanel panel = new JPanel(new BorderLayout());
                panel.add(label, BorderLayout.CENTER);

                // Create the update button per index
                JButton updateButton = new JButton("Update Payment Status");
                updateButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        updatePaymentStatus(username, String.valueOf(index));
                    }
                });
                panel.add(updateButton, BorderLayout.SOUTH);

                // Show the dialog with the image and button
                JOptionPane.showMessageDialog(null, panel, "Payment Proof for " + username, JOptionPane.PLAIN_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "Error: Received image is null.");
            }
        } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
            // Handle case where no payment status found
            JOptionPane.showMessageDialog(null, "No payment status found for " + username);
        } else {
            // Handle other error cases
            JOptionPane.showMessageDialog(null, "Error fetching payment status: HTTP " + responseCode);
        }
    } catch (Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(null, "Error fetching payment status: " + ex.getMessage());
    } finally {
        if (connection != null) {
            connection.disconnect();
        }
    }
}

private void updatePaymentStatus(String username, String violationcode) {
    HttpURLConnection connection = null;
    try {
        URI uri = URI.create("http://localhost:8000/updatepaymentstatus");
        URL url = uri.toURL();
        connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setUseCaches(false);
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("User-Agent", "JavaClient");

        // Adjust postData to include username and violationcode
        String postData = "username=" + URLEncoder.encode(username, "UTF-8") + "&violationcode=" + URLEncoder.encode(violationcode, "UTF-8");

        // Logging the postData for debugging
        System.out.println("Sending data: " + postData);

        OutputStream os = connection.getOutputStream();
        os.write(postData.getBytes("UTF-8"));
        os.close();

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            // Read response from server if needed
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String response = in.readLine();
            in.close();

            // Display success message based on server response
            JOptionPane.showMessageDialog(null, response);
        } else {
            // Handle non-200 response codes
            JOptionPane.showMessageDialog(null, "Failed to update payment status. Response code: " + responseCode);
        }

    } catch (Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(null, "Error updating payment status: " + ex.getMessage());
    } finally {
        if (connection != null) {
            connection.disconnect();
        }
    }
}

private void fetchViolationProof(String username) {
    HttpURLConnection connection = null;
    try {
        URI uri = URI.create("http://localhost:8000/seeviolationproof/" + username);
        URL url = uri.toURL();
        connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setUseCaches(false);
        connection.setDoOutput(true); 
        connection.setDoInput(true);
        connection.setRequestProperty("User-Agent", "JavaClient");
        connection.setRequestProperty("Accept", "image/jpeg");

        InputStream inputStream = connection.getInputStream();
        ImageIcon imageIcon = new ImageIcon(inputStream.readAllBytes());
        JLabel imageLabel = new JLabel(imageIcon);
        JOptionPane.showMessageDialog(null, imageLabel, "Violation Proof for " + username, JOptionPane.PLAIN_MESSAGE);

        inputStream.close();
    } catch (Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(null, "Error fetching violation proof: " + ex.getMessage());
    } finally {
        if (connection != null) {
            connection.disconnect();
        }
    }
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
        httpConn.setDoOutput(true); 
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
            
            JOptionPane.showMessageDialog(DashboardFrame.this, "Pelanggaran berhasil direkam", "Sukses", JOptionPane.INFORMATION_MESSAGE);
        } else if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
            JOptionPane.showMessageDialog(DashboardFrame.this, "Unauthorized access. Admins cannot register violations.", "Error", JOptionPane.ERROR_MESSAGE);
        } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
            JOptionPane.showMessageDialog(DashboardFrame.this, "Username not found. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
        } else if (responseCode == HttpURLConnection.HTTP_INTERNAL_ERROR) {
            JOptionPane.showMessageDialog(DashboardFrame.this, "Internal Server Error. Please try again later.", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(DashboardFrame.this, "Failed to save file to uploads. Status code: " + responseCode, "Error", JOptionPane.ERROR_MESSAGE);
        }

        httpConn.disconnect();
    } catch (IOException e) {
        e.printStackTrace();
        System.err.println("Exception: " + e.getMessage());
        JOptionPane.showMessageDialog(DashboardFrame.this, "Gagal merekam pelanggaran. Terjadi kesalahan IO", "Error", JOptionPane.ERROR_MESSAGE);
    }
}
// private void fetchNotifications(String username) {
//     try {
//         HttpClient client = HttpClient.newHttpClient();
//         HttpRequest request = HttpRequest.newBuilder()
//                 .uri(URI.create("http://localhost:8000/login/" + username))
//                 .GET()
//                 .build();

//         client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
//                 .thenApply(response -> {
//                     System.out.println("Response Code: " + response.statusCode());
//                     System.out.println("Response Body: " + response.body());

//                     if (response.statusCode() == 200) {
//                         String responseBody = response.body().trim(); 

                        
//                         if ("No new violations found".equals(responseBody)) {
                          
//                             System.out.println("No new violations found");
//                         } else if ("New violations found".equals(responseBody)) {
                           
//                             SwingUtilities.invokeLater(() -> {
//                                 JOptionPane.showMessageDialog(DashboardFrame.this,
//                                         "New violations found and notified!",
//                                         "Notification",
//                                         JOptionPane.INFORMATION_MESSAGE);
//                             });
//                         } else {
                          
//                             System.out.println("Unexpected response: " + responseBody);
//                         }
//                     } else {
//                         SwingUtilities.invokeLater(() -> {
//                             JOptionPane.showMessageDialog(DashboardFrame.this,
//                                     "Error fetching notifications: Unexpected response code " + response.statusCode(),
//                                     "Error",
//                                     JOptionPane.ERROR_MESSAGE);
//                         });
//                     }
//                     return response;
//                 })
//                 .exceptionally(exception -> {
//                     SwingUtilities.invokeLater(() -> {
//                         JOptionPane.showMessageDialog(DashboardFrame.this,
//                                 "Error fetching notifications: " + exception.getMessage(),
//                                 "Error",
//                                 JOptionPane.ERROR_MESSAGE);
//                     });
//                     return null;
//                 });
//     } catch (Exception e) {
//         e.printStackTrace();
//         JOptionPane.showMessageDialog(DashboardFrame.this,
//                 "Error fetching notifications: " + e.getMessage(),
//                 "Error",
//                 JOptionPane.ERROR_MESSAGE);
//     }
// }


    private void fetchUnpaidUsers() {
        try {
            URL url = URI.create("http://localhost:8000/userbelumbayar").toURL(); // Sesuaikan dengan URL endpoint yang benar
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new RuntimeException("Failed : HTTP error code : " + connection.getResponseCode());
            }

            
            tableModel.setRowCount(0);

            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
              
                String[] data = line.split(",");
                String username = data[0].trim();
                String violationType = data[1].trim();
                String violationTime = data[2].trim();

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
  JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
    
        JLabel titleLabel = new JLabel("Register your account", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy++;

     
        JLabel usernameLabel = new JLabel("Username:");
        gbc.gridx = 0;
        panel.add(usernameLabel, gbc);

        usernameField = new JTextField(20);
        gbc.gridx = 1;
        panel.add(usernameField, gbc);

       
        gbc.gridy++;
        gbc.gridx = 0;
        JLabel passwordLabel = new JLabel("Password:");
        panel.add(passwordLabel, gbc);

        passwordField = new JPasswordField(20);
        gbc.gridx = 1;
        panel.add(passwordField, gbc);

     
        gbc.gridy++;
        gbc.gridx = 0;
        JLabel emailLabel = new JLabel("Email:");
        panel.add(emailLabel, gbc);

        emailField = new JTextField(20);
        gbc.gridx = 1;
        panel.add(emailField, gbc);

        registerButton = new JButton("Register");
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(registerButton, gbc);

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

               
                HttpClient client = HttpClient.newHttpClient();

                String jsonBody = "{\"username\":\"" + username + "\", \"password\":\"" + password +
                        "\", \"email\":\"" + email + "\", \"isadmin\": true}";

      
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8000/register")) // Adjust URL accordingly
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                        .build();

        
                client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                        .thenApply(response -> {
                            System.out.println("Response Code: " + response.statusCode());
                            System.out.println("Response Body: " + response.body());

                        
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


   