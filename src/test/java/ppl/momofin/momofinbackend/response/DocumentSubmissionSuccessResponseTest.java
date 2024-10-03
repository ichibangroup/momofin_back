package ppl.momofin.momofinbackend.response;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DocumentSubmissionSuccessResponseTest {
    @Test
    void testDocumentSubmissionSuccessResponseConstructorEmpty() {
        DocumentSubmissionSuccessResponse successResponse = new DocumentSubmissionSuccessResponse();

        assertNotNull(successResponse);
        assertNull(successResponse.getDocumentSubmissionResult());
    }

    @Test
    void testDocumentSubmissionSuccessResponseConstructor() {
        String successMessage = "Success";
        DocumentSubmissionSuccessResponse successResponse = new DocumentSubmissionSuccessResponse(successMessage);

        assertEquals(successMessage, successResponse.getDocumentSubmissionResult());
    }

    @Test
    void testGetSetSuccessMessage() {
        String successMessage = "Success";
        DocumentSubmissionSuccessResponse successResponse = new DocumentSubmissionSuccessResponse();

        successResponse.setDocumentSubmissionResult(successMessage);
        assertEquals(successMessage, successResponse.getDocumentSubmissionResult());
    }
}
