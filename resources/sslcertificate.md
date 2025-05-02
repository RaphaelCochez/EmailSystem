# Steps to Generate SSL Certificates for EmailServer

## find the keytool documentation
**link:** https://docs.oracle.com/en/java/javase/24/docs/specs/man/keytool.html#name

## Generate the Keystore and Key Pair
I first generated a keystore and the private key (for the server) in the keystore, which will be used for SSL communication.

first of all navigate to `EmailSystem\resources`
Here's the command to generate a key pair using `keytool`:
```bash
keytool -genkeypair -alias server -keyalg RSA -keysize 2048 -keystore server.p12 -storetype PKCS12 -validity 3650
```
Explanation:

- **alias server:** Alias name for the key pair (you can choose your own alias).

- **keyalg RSA:** Key algorithm to use for key pair generation (RSA).

- **keysize 2048:** Specifies the key size in bits (2048 is commonly used for RSA keys).

- **keystore server.p12:** Keystore file name where the private key and certificate are stored (server.p12).

- **storetype PKCS12:** Keystore type, specifying that it's a PKCS12 format.

- **validity 3650:** Specifies the validity period for the certificate in days (10 years in this case).

After running this, i was prompted to enter details such as the Distinguished Name (DN), which includes:

- **Common Name (CN)** : secureEmailHost

- **Organization (O)** : DKIT

- **Organizational Unit (OU)** : COMPUTING

- **Locality (L)** : Dundalk

- **State (S)** : Louth

- **Country (C)** : IE

You'll also be asked for a keystore password and a private key password.

I used:
- d00285437 (my student ID) for both

## Export the Certificate
After generating the key pair, you'll need to export the certificate so that clients can verify it. You can do this using the following command:

```bash
keytool -exportcert -alias server -keystore server.p12 -file server.cer
``` 
This will export the certificate to the file `server.cer`.

## Import the Certificate into the Keystore
If you want to add a trusted certificate (e.g., from a Certificate Authority), you can import it into your keystore with the following:
```bash
keytool -importcert -alias server -file server.cer -keystore server.p12
```
## Configuring `EmailServer.java` to Use SSL
Now, configure your EmailServer.java to use the keystore and SSL context aka:

```java 
public void start() {
    try {
        fileDatabase.loadAll();

        // Load the keystore
        System.setProperty("javax.net.ssl.keyStore", "server.p12");
        System.setProperty("javax.net.ssl.keyStorePassword", "yourpassword");
        System.setProperty("javax.net.ssl.keyStoreType", "PKCS12");

        SSLServerSocketFactory sslServerSocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        serverSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(port);
        isRunning = true;

        LogHandler.log("Email Server started on port: " + port);

        while (isRunning && !serverSocket.isClosed()) {
            try {
                Socket clientSocket = serverSocket.accept();
                LogHandler.log("Accepted new client: " + clientSocket.getRemoteSocketAddress());

                CommandHandler commandHandler = new CommandHandler(authService, emailService, sessionManager);
                ClientHandler clientHandler = new ClientHandler(clientSocket, commandHandler);

                try {
                    threadPool.submit(clientHandler);
                } catch (RejectedExecutionException e) {
                    LogHandler.log("ThreadPool rejected client connection: " + e.getMessage());
                    clientSocket.close();
                }

            } catch (IOException acceptException) {
                if (isRunning) {
                    LogHandler.log("Error accepting client connection: " + acceptException.getMessage());
                }
            }
        }

    } catch (IOException e) {
        LogHandler.log("Server error during startup: " + e.getMessage());
    } finally {
        stop();
    }
}

```
Testing SSL Connection

Once you've set everything up, you can test the server by using SSL sockets.

Run the server and try connecting with an SSL client like this:
```bash 
openssl s_client -connect localhost:8080
```
This will attempt to connect to your SSL-enabled server and display the certificate information.
