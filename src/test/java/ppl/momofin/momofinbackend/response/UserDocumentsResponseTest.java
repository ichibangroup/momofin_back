package ppl.momofin.momofinbackend.response;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ppl.momofin.momofinbackend.model.Document;
import ppl.momofin.momofinbackend.model.User;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserDocumentsResponseTest {

    private User user;
    private List<Document> documents;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUserId(1L);
        user.setUsername("testuser");

        Document document1 = new Document();
        document1.setDocumentId(1L);
        document1.setName("Document1");

        Document document2 = new Document();
        document2.setDocumentId(2L);
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
