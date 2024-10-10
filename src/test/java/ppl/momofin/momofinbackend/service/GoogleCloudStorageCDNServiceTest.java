package ppl.momofin.momofinbackend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoogleCloudStorageCDNServiceTest {

    @Mock
    private Storage mockStorage;

    private GoogleCloudStorageCDNService cdnService;
    private String bucketName;
    private String projectId;
    private String privateKeyId;
    private String privateKeyFilePath;
    private String serviceName;
    private String clientId;

    @BeforeEach
    void setUp() throws IOException {
        bucketName = System.getenv("GCP_BUCKET_NAME");
        projectId = System.getenv("GCP_PROJECT");
        privateKeyId = System.getenv("GCP_SA_PRIVATE_KEY_ID");
        privateKeyFilePath = "D:/gcp_sa_private_key.pem";
        serviceName = System.getenv("GCP_SA_NAME");
        clientId =System.getenv("GCP_SA_CLIENT_ID");

        cdnService = new GoogleCloudStorageCDNService(bucketName, projectId, privateKeyId, privateKeyFilePath, serviceName, clientId) {
            @Override
            protected Storage createStorage(GoogleCredentials credentials) {
                return mockStorage;
            }
        };
    }

    @Test
    void testUploadFile() throws IOException {
        byte[] fileBytes = "test file content".getBytes();
        String folderName = "test-folder";
        String fileName = "test-file.pdf";

        cdnService.uploadFile(fileBytes, folderName, fileName);

        BlobId expectedBlobId = BlobId.of(bucketName, folderName + "/" + fileName);
        BlobInfo expectedBlobInfo = BlobInfo.newBuilder(expectedBlobId)
                .setContentType("application/pdf")
                .build();

        verify(mockStorage).create(eq(expectedBlobInfo), eq(fileBytes));
    }

    @Test
    void testConstructorWithInvalidCredentials() {
        assertThrows(RuntimeException.class, () -> {
            new GoogleCloudStorageCDNService(bucketName, projectId, privateKeyId, "D:/wrongkey.pem", serviceName, clientId);
        });
    }
}