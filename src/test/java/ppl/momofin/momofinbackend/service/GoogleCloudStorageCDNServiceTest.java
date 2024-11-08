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
import ppl.momofin.momofinbackend.model.DocumentVersion;
import ppl.momofin.momofinbackend.model.Organization;
import ppl.momofin.momofinbackend.model.User;
import ppl.momofin.momofinbackend.repository.DocumentRepository;
import ppl.momofin.momofinbackend.repository.DocumentVersionRepository;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.UUID;
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
    private DocumentVersionRepository documentVersionRepository;

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
        user.setUserId(UUID.fromString("292aeace-0148-4a20-98bf-bf7f12871efe"));

        mockFile = new MockMultipartFile("test file", "test-file.pdf", MediaType.APPLICATION_PDF_VALUE,"test file content".getBytes());

        cdnService = new GoogleCloudStorageCDNService(bucketName, projectId, privateKeyId, privateKeyFilePath, serviceName, clientId, documentRepository, documentVersionRepository) {
            @Override
            protected Storage createStorage(GoogleCredentials credentials) {
                return mockStorage;
            }
        };
    }

    @Test
    void testUploadFile() throws IOException {
        ArgumentCaptor<Document> documentCaptor = ArgumentCaptor.forClass(Document.class);

        Document document = new Document();
        document.setDocumentId(UUID.randomUUID());
        when(documentRepository.save(any(Document.class))).thenReturn(document);
        cdnService.submitDocument(mockFile, user, "hashString");

        BlobId expectedBlobId = BlobId.of(bucketName, user.getOrganization().getName() + "/" + user.getUserId() + "/test-file/version_1_test-file.pdf");
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
            new GoogleCloudStorageCDNService(bucketName, projectId, privateKeyId, wrongFilePath, serviceName, clientId, documentRepository, documentVersionRepository);
        });
    }

    @Test
    void testGetViewableUrl_Success() throws IOException {
        Document document = new Document();
        String fileName = "test.pdf";
        document.setName(fileName);
        DocumentVersion currentVersion = new DocumentVersion();
        currentVersion.setVersion(4);
        document.setCurrentVersion(4);
        UUID userId = UUID.fromString("292aeace-0148-4a20-98bf-bf7f12871efe");
        String organizationName = "OrgXYZ";
        String folderPath = organizationName + "/" + userId + "/test/version_" + currentVersion.getVersion() + "_" +fileName;
        BlobId blobId = BlobId.of(bucketName, folderPath);

        when(mockStorage.get(blobId)).thenReturn(blob);
        URI signedUri = URI.create("https://signed-url.com");
        URL signedUrl = signedUri.toURL();
        when(blob.signUrl(1, TimeUnit.HOURS)).thenReturn(signedUrl);

        // Act
        String viewableUrl = cdnService.getViewableUrl(document, userId, organizationName);

        // Assert
        assertNotNull(viewableUrl);
        assertEquals("https://signed-url.com", viewableUrl);
        verify(mockStorage).get(blobId); // Ensure the correct blob was fetched
        verify(blob).signUrl(1, TimeUnit.HOURS); // Ensure a signed URL was created
    }

    @Test
    void testGetViewableUrl_FileNotFound() {
        // Arrange
        Document document = new Document();
        String fileName = "nonexistent.pdf";
        document.setName(fileName);
        DocumentVersion currentVersion = new DocumentVersion();
        currentVersion.setVersion(4);
        document.setCurrentVersion(4);
        UUID userId = UUID.fromString("292aeace-0148-4a20-98bf-bf7f12871efe");
        String organizationName = "OrgXYZ";
        String folderPath = organizationName + "/" + userId + "/nonexistent/version_" + currentVersion.getVersion() + "_" +fileName;
        BlobId blobId = BlobId.of(bucketName, folderPath);

        when(mockStorage.get(blobId)).thenReturn(null);

        // Act & Assert
        FileNotFoundException exception = assertThrows(FileNotFoundException.class, () -> {
            cdnService.getViewableUrl(document, userId, organizationName);
        });

        assertEquals("File not found: " + fileName, exception.getMessage());
        verify(mockStorage).get(blobId); // Ensure the correct blob was attempted to be fetched
    }

    @Test
    void testEditDocument_NewVersionUploaded() throws IOException {
        String fileName = "test-file.pdf";
        String cleanedFileName =  fileName.replaceFirst("[.][^.]+$", "");


        // Expected values
        Document document = new Document();
        DocumentVersion documentVersion = new DocumentVersion();
        documentVersion.setVersion(4);
        document.setCurrentVersion(4);
        document.setOwner(user);
        document.setName(fileName);
        int nextVersion = document.getCurrentVersion() + 1;
        String folderPath = user.getOrganization().getName() + "/" + user.getUserId() + "/" + cleanedFileName+ "/version_" + nextVersion+ "_" +fileName;

        String expectedVersionedFileName = String.format("%s", folderPath);

        BlobId expectedBlobId = BlobId.of(bucketName, expectedVersionedFileName);
        BlobInfo expectedBlobInfo = BlobInfo.newBuilder(expectedBlobId).setContentType("application/pdf").build();

        // Mock storage interactions
        when(documentRepository.save(any(Document.class))).thenReturn(document);

        // Execute the method
        Document updatedDocument = cdnService.editDocument(mockFile, document, "newHash", user);

        // Verify interactions and results
        verify(mockStorage).create(expectedBlobInfo, mockFile.getBytes());
        verify(documentRepository).save(document);

        assertEquals("newHash", updatedDocument.getHashString());
        assertEquals(nextVersion, updatedDocument.getCurrentVersion());
    }
}