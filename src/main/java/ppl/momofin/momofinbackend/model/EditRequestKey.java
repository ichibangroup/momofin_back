package ppl.momofin.momofinbackend.model;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Embeddable @Getter @Setter
public class EditRequestKey implements Serializable {
    private UUID userId;

    private UUID documentId;
}
