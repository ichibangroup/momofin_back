package ppl.momofin.momofinbackend.repository.specification;

import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;
import ppl.momofin.momofinbackend.model.AuditTrail;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuditTrailSpecificationsTest {

    private CriteriaBuilder cb;
    private CriteriaQuery<?> query;
    private Root root;

    @BeforeEach
    void setUp() {
        cb = mock(CriteriaBuilder.class);
        query = mock(CriteriaQuery.class);
        root = mock(Root.class);
    }

    @Test
    void testPrivateConstructorThrowsException() throws Exception {
        Constructor<AuditTrailSpecifications> constructor = AuditTrailSpecifications.class.getDeclaredConstructor();

        constructor.setAccessible(true);

        Exception exception = assertThrows(InvocationTargetException.class, constructor::newInstance);

        Throwable cause = exception.getCause();

        assertInstanceOf(IllegalStateException.class, cause);
        assertEquals("Utility class", cause.getMessage());
    }

    @Test
    void testHasAction_withNullAction() {
        Specification<AuditTrail> specification = AuditTrailSpecifications.hasAction(null);
        specification.toPredicate(root, query, cb);
        verify(cb).conjunction();
    }

    @Test
    void testHasAction_withNonNullAction() {
        String action = "SUBMIT";
        Specification<AuditTrail> specification = AuditTrailSpecifications.hasAction(action);

        specification.toPredicate(root, query, cb);
        verify(cb).equal(root.get("action"), action);
    }

    @Test
    void testHasUser_withNullUsername() {
        Specification<AuditTrail> specification = AuditTrailSpecifications.hasUser(null);
        specification.toPredicate(root, query, cb);
        verify(cb).conjunction();
    }

    @Test
    void testHasUser_withEmptyUsername() {
        Specification<AuditTrail> specification = AuditTrailSpecifications.hasUser("");
        specification.toPredicate(root, query, cb);
        verify(cb).conjunction();
    }

    @Test
    void testHasUser_withNonNullUsername() {
        String username = "user1";

        Path userPath = mock(Path.class);
        Path usernamePath = mock(Path.class);
        Expression lowerExpression = mock(Expression.class);

        when(root.get("user")).thenReturn(userPath);
        when(userPath.get("username")).thenReturn(usernamePath);
        when(cb.lower(usernamePath)).thenReturn(lowerExpression);

        Specification<AuditTrail> specification = AuditTrailSpecifications.hasUser(username);

        specification.toPredicate(root, query, cb);

        verify(cb).like(lowerExpression, "%" + username.toLowerCase() + "%");
    }

    @Test
    void testAfterTimestamp_withNullStartDateTime() {
        Specification<AuditTrail> specification = AuditTrailSpecifications.afterTimestamp(null);
        specification.toPredicate(root, query, cb);
        verify(cb).conjunction();
    }

    @Test
    void testAfterTimestamp_withNonNullStartDateTime() {
        LocalDateTime startDateTime = LocalDateTime.now();
        Specification<AuditTrail> specification = AuditTrailSpecifications.afterTimestamp(startDateTime);

        specification.toPredicate(root, query, cb);
        verify(cb).greaterThanOrEqualTo(root.get("timestamp"), startDateTime);
    }

    @Test
    void testBeforeTimestamp_withNullEndDateTime() {
        Specification<AuditTrail> specification = AuditTrailSpecifications.beforeTimestamp(null);
        specification.toPredicate(root, query, cb);
        verify(cb).conjunction();
    }

    @Test
    void testBeforeTimestamp_withNonNullEndDateTime() {
        LocalDateTime endDateTime = LocalDateTime.now();
        Specification<AuditTrail> specification = AuditTrailSpecifications.beforeTimestamp(endDateTime);

        specification.toPredicate(root, query, cb);
        verify(cb).lessThanOrEqualTo(root.get("timestamp"), endDateTime);
    }

    @Test
    void testHasDocumentName_withNullDocumentName() {
        Specification<AuditTrail> specification = AuditTrailSpecifications.hasDocumentName(null);
        specification.toPredicate(root, query, cb);
        verify(cb).conjunction();
    }

    @Test
    void testHasDocumentName_withEmptyDocumentName() {
        Specification<AuditTrail> specification = AuditTrailSpecifications.hasDocumentName("");
        specification.toPredicate(root, query, cb);
        verify(cb).conjunction();
    }

    @Test
    void testHasDocumentName_withNonNullDocumentName() {
        String documentName = "document1";

        Path documentPath = mock(Path.class);
        Path documentNamePath = mock(Path.class);

        when(root.get("document")).thenReturn(documentPath);
        when(documentPath.get("name")).thenReturn(documentNamePath);

        Specification<AuditTrail> specification = AuditTrailSpecifications.hasDocumentName(documentName);

        specification.toPredicate(root, query, cb);

        verify(cb).like(cb.lower(documentNamePath), "%" + documentName.toLowerCase() + "%");
    }

}