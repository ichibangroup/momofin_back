package ppl.momofin.momofinbackend.response;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ppl.momofin.momofinbackend.model.Document;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class DocumentEditSuccessResponseTest {
    Document document;

    @BeforeEach
    void setUp() {
        document = new Document();
    }

    @Test
    void argConstructorTest() {
        DocumentEditSuccessResponse response = new DocumentEditSuccessResponse(document);

        assertEquals(document, response.getEditedDocument());
    }

    @Test
    void nullConstructorTest() {
        DocumentEditSuccessResponse response = new DocumentEditSuccessResponse();

        assertNull(response.getEditedDocument());
    }

    @Test
    void getSetEditedDocumentTest() {
        DocumentEditSuccessResponse response = new DocumentEditSuccessResponse();

        assertNull(response.getEditedDocument());

        response.setEditedDocument(document);

        assertEquals(document, response.getEditedDocument());
    }
}
