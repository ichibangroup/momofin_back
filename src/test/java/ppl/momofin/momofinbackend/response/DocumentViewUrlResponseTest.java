package ppl.momofin.momofinbackend.response;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DocumentViewUrlResponseTest {
    @Test
    void testDocumentViewUrlResponseConstructorEmpty() {
        DocumentViewUrlResponse documentViewUrlResponse = new DocumentViewUrlResponse();

        assertNotNull(documentViewUrlResponse);
        assertNull(documentViewUrlResponse.getUrl());
    }

    @Test
    void testDocumentViewUrlResponseConstructor() {
        String viewableUrl = "https://cdn.example.com/document-url";
        DocumentViewUrlResponse documentViewUrlResponse = new DocumentViewUrlResponse(viewableUrl);

        assertEquals(viewableUrl, documentViewUrlResponse.getUrl());
    }

    @Test
    void testGetSetUrl() {
        String viewableUrl = "https://cdn.example.com/document-url";
        DocumentViewUrlResponse documentViewUrlResponse = new DocumentViewUrlResponse();

        documentViewUrlResponse.setUrl(viewableUrl);
        assertEquals(viewableUrl, documentViewUrlResponse.getUrl());
    }
}
