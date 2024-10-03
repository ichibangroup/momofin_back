package ppl.momofin.momofinbackend.response;

import lombok.Getter;
import lombok.Setter;
import ppl.momofin.momofinbackend.model.Document;

@Getter @Setter
public class DocumentVerificationSuccessResponse implements Response{
    private Document document;

    public DocumentVerificationSuccessResponse() {}

    public DocumentVerificationSuccessResponse(Document document) {
        this.document = document;
    }
}
