package ppl.momofin.momofinbackend.model;

import jakarta.persistence.Embeddable;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Embeddable @Getter @Setter
public class EditRequestKey implements Serializable {
    @ManyToOne
    private User user;

    @ManyToOne
    private Document document;
}
