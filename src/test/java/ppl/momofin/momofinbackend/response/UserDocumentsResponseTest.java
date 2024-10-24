package ppl.momofin.momofinbackend.response;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ppl.momofin.momofinbackend.model.Document;
import ppl.momofin.momofinbackend.model.User;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserDocumentsResponseTest {

    private User user;
    private List<Document> documents;

    @BeforeEach
    void setUp() {
        user = new User();
        UUID userId = UUID.fromString("ff354956-c4c4-4697-9814-e34cd5ef5d4b");
        user.setUserId(userId);
        user.setUsername("testuser");

        Document document1 = new Document();
        UUID documentId = UUID.fromString("ff354956-c4c4-4697-9814-e34cd5ef5d4b");
        document1.setDocumentId(documentId);
        document1.setName("Document1");

        Document document2 = new Document();
        documentId = UUID.fromString("ff60092f-2882-4bed-9503-52eed38cc14c");
        document1.setDocumentId(documentId);
        document2.setName("Document2");

        documents = Arrays.asList(document1, document2);
    }

    @Test
    void testConstructorAndGetters() {
        UserDocumentsResponse response = new UserDocumentsResponse(user, documents);

        assertEquals(user, response.getUser());
        assertEquals(documents, response.getDocuments());
    }

    @Test
    void testSetUser() {
        UserDocumentsResponse response = new UserDocumentsResponse(null, documents);
        assertNull(response.getUser());

        response.setUser(user);
        assertEquals(user, response.getUser());
    }

    @Test
    void testSetDocuments() {
        UserDocumentsResponse response = new UserDocumentsResponse(user, null);
        assertNull(response.getDocuments());

        response.setDocuments(documents);
        assertEquals(documents, response.getDocuments());
    }
}
