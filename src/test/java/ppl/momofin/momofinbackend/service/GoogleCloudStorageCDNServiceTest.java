package ppl.momofin.momofinbackend.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.Resource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class GoogleCloudStorageCDNServiceTest {

    @Mock
    private Storage mockStorage;

    @Mock
    private Resource mockConfigFile;

    @Mock
    private GoogleCredentials mockCredentials;

    private GoogleCloudStorageCDNService cdnService;

    private static final String BUCKET_NAME = "test-bucket";

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);

        InputStream mockInputStream = new ByteArrayInputStream("mock credentials".getBytes());
        when(mockConfigFile.getInputStream()).thenReturn(mockInputStream);

        try (var mocked = mockStatic(GoogleCredentials.class)) {
            mocked.when(() -> GoogleCredentials.fromStream(any(InputStream.class)))
                    .thenReturn(mockCredentials);

            cdnService = spy(new GoogleCloudStorageCDNService(BUCKET_NAME, mockConfigFile) {
                @Override
                protected Storage createStorage(GoogleCredentials credentials) {
                    return mockStorage;
                }
            });
        }
    }

    @Test
    void uploadFileCorrect() throws IOException {
        // Arrange
        byte[] fileBytes = "test content".getBytes();
        String folderName = "testFolder";
        String fileName = "testFile.pdf";

        // Act
        cdnService.uploadFile(fileBytes, folderName, fileName);

        // Assert
        BlobId expectedBlobId = BlobId.of(BUCKET_NAME, folderName + "/" + fileName);
        BlobInfo expectedBlobInfo = BlobInfo.newBuilder(expectedBlobId)
                .setContentType("application/pdf")
                .build();

        verify(mockStorage).create(eq(expectedBlobInfo), eq(fileBytes));
    }
}