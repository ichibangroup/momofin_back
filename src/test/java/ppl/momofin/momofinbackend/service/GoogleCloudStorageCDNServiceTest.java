package ppl.momofin.momofinbackend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import ppl.momofin.momofinbackend.model.Document;
import ppl.momofin.momofinbackend.model.Organization;
import ppl.momofin.momofinbackend.model.User;
import ppl.momofin.momofinbackend.repository.DocumentRepository;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoogleCloudStorageCDNServiceTest {

    @Mock
    private Storage mockStorage;
    @Mock
    private DocumentRepository documentRepository;

    private GoogleCloudStorageCDNService cdnService;
    private String bucketName;
    private String projectId;
    private String privateKeyId;
    private String privateKeyFilePath;
    private String serviceName;
    private String clientId;
    private User user;
    private MultipartFile mockFile;

    @BeforeEach
    void setUp() throws IOException {
        bucketName = System.getenv("GCP_BUCKET_NAME");
        projectId = System.getenv("GCP_PROJECT");
        privateKeyId = System.getenv("GCP_SA_PRIVATE_KEY_ID");
        privateKeyFilePath = "D:/gcp_sa_private_key.pem";
        serviceName = System.getenv("GCP_SA_NAME");
        clientId =System.getenv("GCP_SA_CLIENT_ID");

        Organization organization = new Organization("Momofin");
        user = new User(organization, "Executive John", "Marshall Jordan", "marshall@jordan.email.com", "1234567890999", "Executive");

        mockFile = new MockMultipartFile("test file", "test-file.pdf", MediaType.APPLICATION_PDF_VALUE,"test file content".getBytes());

        cdnService = new GoogleCloudStorageCDNService(bucketName, projectId, privateKeyId, privateKeyFilePath, serviceName, clientId, documentRepository) {
            @Override
            protected Storage createStorage(GoogleCredentials credentials) {
                return mockStorage;
            }
        };
    }

    @Test
    void testUploadFile() throws IOException {
        ArgumentCaptor<Document> documentCaptor = ArgumentCaptor.forClass(Document.class);

        cdnService.uploadFile(mockFile, user, "hashString");

        BlobId expectedBlobId = BlobId.of(bucketName, user.getOrganization().getName() + "/" + user.getName() + "/test-file.pdf");
        BlobInfo expectedBlobInfo = BlobInfo.newBuilder(expectedBlobId)
                .setContentType("application/pdf")
                .build();

        verify(documentRepository).save(documentCaptor.capture());

        Document savedDocument = documentCaptor.getValue();

        assertNotNull(savedDocument);
        assertEquals("test-file.pdf", savedDocument.getName());
        assertEquals("hashString", savedDocument.getHashString());
        assertEquals(user, savedDocument.getOwner());
        verify(mockStorage).create(eq(expectedBlobInfo), eq(mockFile.getBytes()));
    }

    @Test
    void testConstructorWithInvalidCredentials() {
        assertThrows(RuntimeException.class, () -> {
            new GoogleCloudStorageCDNService(bucketName, projectId, privateKeyId, "D:/wrongkey.pem", serviceName, clientId, documentRepository);
        });
    }
}