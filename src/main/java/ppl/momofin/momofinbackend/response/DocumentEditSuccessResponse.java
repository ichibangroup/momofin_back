package ppl.momofin.momofinbackend.response;

import lombok.Getter;
import lombok.Setter;
import ppl.momofin.momofinbackend.model.Document;

@Getter @Setter
public class DocumentEditSuccessResponse implements Response{
    private Document editedDocument;

    public DocumentEditSuccessResponse(Document editedDocument) {
        this.editedDocument = editedDocument;
    }

    public DocumentEditSuccessResponse() {}
}
