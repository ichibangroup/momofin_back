package ppl.momofin.momofinbackend.annotation;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RequireOrganizationAdminTest {

    @Test
    void requireOrganizationAdmin_ShouldHavePreAuthorizeAnnotation() {
        Annotation[] annotations = RequireOrganizationAdmin.class.getAnnotations();
        boolean hasPreAuthorize = false;
        for (Annotation annotation : annotations) {
            if (annotation instanceof PreAuthorize) {
                hasPreAuthorize = true;
                break;
            }
        }
        assertTrue(hasPreAuthorize, "RequireOrganizationAdmin should have @PreAuthorize annotation");
    }
}