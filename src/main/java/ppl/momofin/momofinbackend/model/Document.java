package ppl.momofin.momofinbackend.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.bind.annotation.ModelAttribute;
import jakarta.persistence.*;
import java.security.Timestamp;


@Getter
@Setter
@Entity
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long documentId;
    private String hashString;
    private String name;
    private Timestamp createdTimestamp;
    private Timestamp modifiedTimestamp;
}
