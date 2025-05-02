package src;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class FileEncryptorGUI extends JFrame implements ActionListener {

    private JButton selectFileButton;
    private JButton selectFolderButton;
    private JButton encryptButton;
    private JButton decryptButton;
    private JTextField filePathField;
    private JPasswordField passwordField;
    private JTextArea statusArea;

    private FileEncryptor fileEncryptor;

    public FileEncryptorGUI() {
        super("Serious File Encryptor");

        fileEncryptor = new FileEncryptor();

        // Set up the frame
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLayout(new BorderLayout());

        // Create components
        selectFileButton = new JButton("Select File");
        selectFolderButton = new JButton("Select Folder");
        encryptButton = new JButton("Encrypt");
        decryptButton = new JButton("Decrypt");
        filePathField = new JTextField(30);
        passwordField = new JPasswordField(30);
        statusArea = new JTextArea();
        statusArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(statusArea);

        // Add action listeners
        selectFileButton.addActionListener(this);
        selectFolderButton.addActionListener(this);
        encryptButton.addActionListener(this);
        decryptButton.addActionListener(this);

        // Create panels
        JPanel filePanel = new JPanel();
        filePanel.add(new JLabel("File/Folder:"));
        filePanel.add(filePathField);
        filePanel.add(selectFileButton);
        filePanel.add(selectFolderButton);

        JPanel passwordPanel = new JPanel();
        passwordPanel.add(new JLabel("Password:"));
        passwordPanel.add(passwordField);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(encryptButton);
        buttonPanel.add(decryptButton);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel.add(filePanel);
        inputPanel.add(passwordPanel);
        inputPanel.add(buttonPanel);

        // Add panels to frame
        add(inputPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Make the frame visible
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String filePath = filePathField.getText();
        char[] password = passwordField.getPassword();

        if (filePath.isEmpty()) {
            statusArea.append("Please select a file or folder.\n");
            return;
        }
        if (password.length == 0) {
            statusArea.append("Please enter a password.\n");
            return;
        }

        File inputFile = new File(filePath);
        if (!inputFile.exists()) {
            statusArea.append("Error: File or folder not found.\n");
            return;
        }

        if (e.getSource() == selectFileButton) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int option = fileChooser.showOpenDialog(this);
            if (option == JFileChooser.APPROVE_OPTION) {
                filePathField.setText(fileChooser.getSelectedFile().getAbsolutePath());
                statusArea.append("File selected: " + filePathField.getText() + "\n");
            }
        } else if (e.getSource() == selectFolderButton) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int option = fileChooser.showOpenDialog(this);
            if (option == JFileChooser.APPROVE_OPTION) {
                filePathField.setText(fileChooser.getSelectedFile().getAbsolutePath());
                statusArea.append("Folder selected: " + filePathField.getText() + "\n");
            }
        } else if (e.getSource() == encryptButton) {
            statusArea.append("Encrypting...\n");
            new Thread(() -> {
                try {
                    if (inputFile.isFile()) {
                        File outputFile = new File(inputFile.getAbsolutePath() + ".enc");
                        fileEncryptor.encryptFile(inputFile, outputFile, password);
                        statusArea.append("File encrypted successfully: " + outputFile.getAbsolutePath() + "\n");
                    } else if (inputFile.isDirectory()) {
                        encryptFolder(inputFile, password);
                        statusArea.append("Folder encrypted successfully.\n");
                    }
                } catch (Exception ex) {
                    statusArea.append("Encryption failed: " + ex.getMessage() + "\n");
                    ex.printStackTrace();
                } finally {
                    Arrays.fill(password, ' '); // Clear password from memory
                }
            }).start();
        } else if (e.getSource() == decryptButton) {
            statusArea.append("Decrypting...\n");
            new Thread(() -> {
                try {
                    if (inputFile.isFile()) {
                         if (!inputFile.getName().endsWith(".enc")) {
                            statusArea.append("Error: Selected file does not have .enc extension.\n");
                            return;
                        }
                        String outputFilePath = inputFile.getAbsolutePath().substring(0, inputFile.getAbsolutePath().length() - 4); // Remove .enc
                        File outputFile = new File(outputFilePath);
                        fileEncryptor.decryptFile(inputFile, outputFile, password);
                        statusArea.append("File decrypted successfully: " + outputFile.getAbsolutePath() + "\n");
                    } else if (inputFile.isDirectory()) {
                        decryptFolder(inputFile, password);
                        statusArea.append("Folder decrypted successfully.\n");
                    }
                } catch (Exception ex) {
                    statusArea.append("Decryption failed: " + ex.getMessage() + "\n");
                    ex.printStackTrace();
                } finally {
                    Arrays.fill(password, ' '); // Clear password from memory
                }
            }).start();
        }
    }

    private void encryptFolder(File folder, char[] password) throws Exception {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    File outputFile = new File(file.getAbsolutePath() + ".enc");
                    fileEncryptor.encryptFile(file, outputFile, password);
                    statusArea.append("Encrypted: " + file.getAbsolutePath() + "\n");
                } else if (file.isDirectory()) {
                    encryptFolder(file, password); // Recurse into subdirectories
                }
            }
        }
    }

    private void decryptFolder(File folder, char[] password) throws Exception {
         File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".enc")) {
                    String outputFilePath = file.getAbsolutePath().substring(0, file.getAbsolutePath().length() - 4); // Remove .enc
                    File outputFile = new File(outputFilePath);
                    fileEncryptor.decryptFile(file, outputFile, password);
                    statusArea.append("Decrypted: " + file.getAbsolutePath() + "\n");
                } else if (file.isDirectory()) {
                    decryptFolder(file, password); // Recurse into subdirectories
                }
            }
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new FileEncryptorGUI();
            }
        });
    }
}
