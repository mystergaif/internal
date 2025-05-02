# Serious File Encryptor

A simple file and folder encryptor with a graphical user interface, built using Java Swing and AES-GCM encryption.

## Features

*   Encrypt and decrypt individual files.
*   Recursively encrypt and decrypt entire folders.
*   Uses AES with GCM mode for strong, authenticated encryption.
*   Derives encryption key from a user-provided password using PBKDF2.

## Prerequisites

*   Java Development Kit (JDK) 8 or higher.

## Building the Project

1.  Navigate to the root directory of the project in your terminal.
2.  Compile the Java source files:
    ```bash
    javac internal/src/FileEncryptor.java internal/src/FileEncryptorGUI.java
    ```
3.  Create the JAR file:
    ```bash
    jar cfm FileEncryptor.jar internal/src/META-INF/MANIFEST.MF -C internal/src/ .
    ```

## Running the Application

Execute the following command in the root directory of the project:

```bash
java -jar FileEncryptor.jar
```

## Usage

1.  Launch the application using the command above.
2.  Use the "Select File" or "Select Folder" button to choose the file or directory you want to encrypt or decrypt.
3.  Enter your password in the "Password" field.
4.  Click "Encrypt" to encrypt the selected file/folder or "Decrypt" to decrypt it.
5.  The status area will show the progress and result of the operation.

## License

[License Information - To be added]
