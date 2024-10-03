package ppl.momofin.momofinbackend.response;

import ppl.momofin.momofinbackend.model.Document;
import ppl.momofin.momofinbackend.model.User;

import java.util.List;

public class UserDocumentsResponse implements Response {
    private User user;
    private List<Document> documents;

    public UserDocumentsResponse(User user, List<Document> documents) {
        this.user = user;
        this.documents = documents;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Document> getDocuments() {
        return documents;
    }

    public void setDocuments(List<Document> documents) {
        this.documents = documents;
    }
}
