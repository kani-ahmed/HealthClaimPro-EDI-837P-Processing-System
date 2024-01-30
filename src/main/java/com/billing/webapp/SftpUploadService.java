package com.billing.webapp;

import com.jcraft.jsch.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

@Service
public class SftpUploadService {
    /*
    * This class is used to upload files to an SFTP server.
    * It uses the JSch library to connect to the SFTP server.
    * The JSch library is a pure Java implementation of SSH2.
    * The library is available at http://www.jcraft.com/jsch/
     */

    private static final Logger logger = LoggerFactory.getLogger(SftpUploadService.class);

    // The SFTP channel used to connect to the server
    private ChannelSftp channelSftp;

    // The session used to connect to the server
    private Session session;

    // The SFTP server host name or IP address
    @Value("${sftp.server}")
    private String server;

    // The SFTP server port
    @Value("${sftp.port}")
    private int port;

    // The SFTP user name
    @Value("${sftp.user}")
    private String user;

    // The path to the private key file
    @Value("${sftp.privateKeyPath}")
    private String privateKeyPath;

    // The private key passphrase (if any)
    @Value("${sftp.privateKeyPassphrase}")
    private String privateKeyPassphrase;

    // The path to the known hosts file
    @Value("${sftp.knownHostsPath}")
    private String knownHostsPath;

    // The password (if any)
    @Value("${sftp.password}")
    private String password;


    public void connect() {
        try {
            // Create a new JSch instance
            JSch jsch = new JSch();

            // Set the known hosts file
            jsch.setKnownHosts(knownHostsPath);

            // Create a new session using the JSch instance and connect to the server using the provided credentials
            session = jsch.getSession(user, server, port);

            // Check if privateKeyPath is provided and valid
            File privateKeyFile = new File(privateKeyPath);

            if (privateKeyFile.exists() && privateKeyFile.length() > 0) {
                // If private key exists and is not empty, use it for authentication
                if (privateKeyPassphrase != null && ! privateKeyPassphrase.isEmpty()) {
                    // If a passphrase is provided, use it to decrypt the private key file before using it for authentication
                    jsch.addIdentity(privateKeyFile.getAbsolutePath(), privateKeyPassphrase);
                } else {
                    // If no passphrase is provided, use the private key as is for authentication
                    jsch.addIdentity(privateKeyFile.getAbsolutePath());
                }
            } else {
                // If privateKeyPath is not provided or is invalid, use password authentication.
                if (password != null && ! password.isEmpty()) {
                    session.setPassword(password);
                    logger.debug("Password authentication will be used.");
                } else {
                    logger.error("No valid authentication method available. Check privateKeyPath and password.");
                    return;
                }
            }
            // config properties for the session
            Properties config = new Properties();
            // Do you want to enable StrictHostKeyChecking? (yes/no)
            config.put("StrictHostKeyChecking", "yes");
            // Set the preferred host key algorithm
            config.put("HostKeyAlgorithms", "ssh-ed25519,ssh-rsa");
            // Set the preferred user authentication method
            session.setConfig(config);

            logger.info("Connecting to server: {} on port: {}", server, port);
            session.connect();
            logger.info("Session connected. Opening SFTP channel.");
            Channel channel = session.openChannel("sftp");
            channel.connect();
            channelSftp = (ChannelSftp) channel;
            logger.info("Connected successfully to FTP Server.");

        } catch (JSchException e) {
            // Handle JSch exceptions here (e.g. UnknownHostKey, UnknownService, etc...)
            logger.error("SFTP Connection error: ", e);
        }
    }

    /*
    * This method uploads a file to the SFTP server.
    * It takes the local file path and the remote file path as parameters.
    * The remote file path should be absolute (e.g. /home/user/myfile.txt)
    * If the remote file path is not absolute, the file will be uploaded to the user's home directory.
    * If the remote file already exists, it will be overwritten.
     */
    public void uploadFile(String localFilePath, String remoteFilePath) {
        // Check if the SFTP channel is connected and not closed before uploading the file to the server
        try (FileInputStream fis = new FileInputStream(localFilePath)) {
            if (channelSftp != null && channelSftp.isConnected()) {
                logger.info("Uploading file: {}", localFilePath);
                // Upload the file to the server using the put method of the SFTP channel object and close the input stream when done uploading.
                channelSftp.put(fis, remoteFilePath);
                logger.info("File uploaded successfully: {}", remoteFilePath);
            } else {
                // Log a warning if the SFTP channel is not connected or closed before uploading the file
                logger.warn("Connection to SFTP server not established. File not uploaded: {}", localFilePath);
            }
        } catch (Exception e) {
            // Handle exceptions here (e.g. FileNotFoundException, IOException, etc...) and log them
            logger.error("Failed to upload file: {}. Error: {}", localFilePath, e.getMessage(), e);
        }
    }

    /*
    * This method disconnects the SFTP channel and session.
    * It should be called after you are done uploading files to the server.
    * This method should be called only after the SFTP channel is connected and not closed.
     */
    public void disconnect() {
        // Disconnect the SFTP channel and session if they are connected and not closed already and log the disconnection
        if (channelSftp != null) {
            try {
                channelSftp.exit();
                logger.info("SFTP Channel exited.");
            } catch (Exception e) {
                logger.error("Error while exiting SFTP channel.", e);
            }
        }
        if (session != null && session.isConnected()) {
            session.disconnect();
            logger.info("Disconnected from SFTP server.");
        }
    }
}
