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

    public EditRequest() {
        id = new EditRequestKey();
    }

    public EditRequest(User user, Document document) {
        id = new EditRequestKey();
        id.setUser(user);
        id.setDocument(document);
    }

    public void setUser(User user) {
        id.setUser(user);
    }

    public void setDocument(Document document) {
        id.setDocument(document);
    }

    public User getUser() {
        return id.getUser();
    }

    public Document getDocument() {
        return id.getDocument();
    }
}
