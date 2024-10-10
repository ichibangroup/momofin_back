package ppl.momofin.momofinbackend.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
public class GoogleCloudStorageCDNService implements CDNService {

    private final Storage storage;
    private final String bucketName;

    public GoogleCloudStorageCDNService(@Value("${gcp.bucket.name}") String bucketName,
                                        @Value("${gcp.project-id}") String projectId,
                                        @Value("${gcp.sa.private-key-id}") String privateKeyId,
                                        @Value("${gcp.sa.private-key-file}") String privateKeyFilePath,
                                        @Value("${gcp.sa.name}") String serviceName,
                                        @Value("${gcp.sa.client-id}") String clientId) throws IOException {
        String privateKey = new String(Files.readAllBytes(Paths.get(privateKeyFilePath)));
        this.bucketName = bucketName;

        String serviceAccountJson = String.format(
                """
                        {
                          "type": "service_account",
                          "project_id": "%s",
                          "private_key_id": "%s",
                          "private_key": "%s",
                          "client_email": "%s@%s.iam.gserviceaccount.com",
                          "client_id": "%s",
                          "auth_uri": "https://accounts.google.com/o/oauth2/auth",
                          "token_uri": "https://oauth2.googleapis.com/token",
                          "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
                          "client_x509_cert_url": "https://www.googleapis.com/robot/v1/metadata/x509/%s@%s.iam.gserviceaccount.com"
                        }""",
                projectId, privateKeyId, privateKey, serviceName, projectId, clientId, serviceName, projectId
        );
        try (ByteArrayInputStream serviceAccountStream = new ByteArrayInputStream(serviceAccountJson.getBytes(StandardCharsets.UTF_8))) {
            GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccountStream);

            // Create a Google Cloud Storage service instance
            this.storage = createStorage(credentials);
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize Google Cloud Storage credentials", e);
        }
    }

    protected Storage createStorage(GoogleCredentials credentials) {
        return StorageOptions.newBuilder()
                .setCredentials(credentials)
                .build()
                .getService();
    }

    @Override
    public void uploadFile(byte[] fileBytes, String folderName, String fileName) throws IOException {
        BlobId blobId = BlobId.of(bucketName, folderName + "/" + fileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType("application/pdf")
                .build();
        storage.create(blobInfo, fileBytes);
    }
}
