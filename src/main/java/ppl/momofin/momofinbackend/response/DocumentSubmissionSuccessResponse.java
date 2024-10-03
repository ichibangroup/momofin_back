package ppl.momofin.momofinbackend.response;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class DocumentSubmissionSuccessResponse implements Response{
    private String documentSubmissionResult;

    public DocumentSubmissionSuccessResponse() {}
    public DocumentSubmissionSuccessResponse(String documentSubmissionResult) {
        this.documentSubmissionResult = documentSubmissionResult;
    }
}
