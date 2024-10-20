package ppl.momofin.momofinbackend.service;

import com.google.cloud.storage.Blob;
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoogleCloudStorageCDNServiceTest {

    @Mock
    private Storage mockStorage;
    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private Blob blob;

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
        privateKeyFilePath = System.getenv("PKEY_DIRECTORY") + "/gcp_sa_private_key.pem";
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

        BlobId expectedBlobId = BlobId.of(bucketName, user.getOrganization().getName() + "/" + user.getUsername() + "/test-file.pdf");
        BlobInfo expectedBlobInfo = BlobInfo.newBuilder(expectedBlobId)
                .setContentType("application/pdf")
                .build();

        verify(documentRepository).save(documentCaptor.capture());

        Document savedDocument = documentCaptor.getValue();

        assertNotNull(savedDocument);
        assertEquals("test-file.pdf", savedDocument.getName());
        assertEquals("hashString", savedDocument.getHashString());
        assertEquals(user, savedDocument.getOwner());
        verify(mockStorage).create(expectedBlobInfo, mockFile.getBytes());
    }

    @Test
    void testConstructorWithInvalidCredentials() {
        String wrongFilePath = System.getenv("PKEY_DIRECTORY")+"/wrongkey.pem";
        assertThrows(IOException.class, () -> {
            new GoogleCloudStorageCDNService(bucketName, projectId, privateKeyId, wrongFilePath, serviceName, clientId, documentRepository);
        });
    }

    @Test
    void testGetViewableUrl_Success() throws IOException {
        // Arrange
        String fileName = "test.pdf";
        String username = "user123";
        String organizationName = "OrgXYZ";
        String folderPath = organizationName + "/" + username + "/" + fileName;
        BlobId blobId = BlobId.of(bucketName, folderPath);

        when(mockStorage.get(blobId)).thenReturn(blob);
        URI signedUri = URI.create("https://signed-url.com");
        URL signedUrl = signedUri.toURL();
        when(blob.signUrl(1, TimeUnit.HOURS)).thenReturn(signedUrl);

        // Act
        String viewableUrl = cdnService.getViewableUrl(fileName, username, organizationName);

        // Assert
        assertNotNull(viewableUrl);
        assertEquals("https://signed-url.com", viewableUrl);
        verify(mockStorage).get(blobId); // Ensure the correct blob was fetched
        verify(blob).signUrl(1, TimeUnit.HOURS); // Ensure a signed URL was created
    }

    @Test
    void testGetViewableUrl_FileNotFound() {
        // Arrange
        String fileName = "nonexistent.pdf";
        String username = "user123";
        String organizationName = "OrgXYZ";
        String folderPath = organizationName + "/" + username + "/" + fileName;
        BlobId blobId = BlobId.of(bucketName, folderPath);

        when(mockStorage.get(blobId)).thenReturn(null);

        // Act & Assert
        FileNotFoundException exception = assertThrows(FileNotFoundException.class, () -> {
            cdnService.getViewableUrl(fileName, username, organizationName);
        });

        assertEquals("File not found: " + fileName, exception.getMessage());
        verify(mockStorage).get(blobId); // Ensure the correct blob was attempted to be fetched
    }
}