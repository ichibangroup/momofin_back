package ppl.momofin.momofinbackend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "edit_request")
public class EditRequest {
    @EmbeddedId
    private EditRequestKey id;

    @Getter
    @ManyToOne
    @JoinColumn(name = "userId", insertable = false, updatable = false)
    private User user;

    @Getter
    @ManyToOne
    @JoinColumn(name = "documentId", insertable = false, updatable = false)
    private Document document;

    public EditRequest() {
        id = new EditRequestKey();
    }

    public EditRequest(User user, Document document) {
        id = new EditRequestKey();
        this.user = user;
        this.document = document;
        id.setUserId(user.getUserId());
        id.setDocumentId(document.getDocumentId());
    }

    public void setUser(User user) {
        this.user = user;
        id.setUserId(user.getUserId());
    }

    public void setDocument(Document document) {
        this.document = document;
        id.setDocumentId(document.getDocumentId());
    }
}
