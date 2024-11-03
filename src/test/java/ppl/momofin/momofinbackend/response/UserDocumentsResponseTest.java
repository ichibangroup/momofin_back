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
        user.setUserId(UUID.fromString("292aeace-0148-4a20-98bf-bf7f12871efe"));
        user.setUsername("testuser");

        Document document1 = new Document();
        document1.setDocumentId(UUID.fromString("bd7ef7cf-8875-45fb-9fe5-f36319acddff"));
        document1.setName("Document1");

        Document document2 = new Document();
        document2.setDocumentId(UUID.fromString("d7d6c46d-19c9-4a2e-87ce-e92f0270b24c"));
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
