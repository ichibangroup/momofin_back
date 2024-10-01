package ppl.momofin.momofinbackend.response;

import org.junit.jupiter.api.Test;
import ppl.momofin.momofinbackend.model.Document;

import javax.print.Doc;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DocumentVerificationSuccessResponseTest {
    @Test
    void testDocumentVerificationSuccessResponseConstructorEmpty() {
        DocumentVerificationSuccessResponse successResponse = new DocumentVerificationSuccessResponse();

        assertNotNull(successResponse);
        assertNull(successResponse.getDocument());
    }

    @Test
    void testDocumentVerificationSuccessResponseConstructor() {
        Document document = new Document();
        DocumentVerificationSuccessResponse successResponse = new DocumentVerificationSuccessResponse(document);

        assertEquals(document, successResponse.getDocument());
    }

    @Test
    void testGetSetDocument() {
        Document document = new Document();
        DocumentVerificationSuccessResponse successResponse = new DocumentVerificationSuccessResponse();

        successResponse.setDocument(document);
        assertEquals(document, successResponse.getDocument());
    }
}
