package ppl.momofin.momofinbackend.response;

import org.junit.jupiter.api.Test;
import ppl.momofin.momofinbackend.model.User;

import static org.junit.jupiter.api.Assertions.*;

class AuthResponseSuccessTest {
    @Test
    void testAuthResponseSuccessConstructorEmpty() {
        AuthResponseSuccess responseSuccess = new AuthResponseSuccess();

        assertNotNull(responseSuccess);
        assertNull(responseSuccess.getJwt());
        assertNull(responseSuccess.getUser());
    }

    @Test
    void testAuthResponseSuccessConstructor() {
        User user = new User();
        String jwt = "jwt";
        AuthResponseSuccess responseSuccess = new AuthResponseSuccess(user, jwt);

        assertEquals(jwt, responseSuccess.getJwt());
        assertEquals(user, responseSuccess.getUser());
    }

    @Test
    void testGetSetJwt() {
        String jwt = "jwt being set";
        AuthResponseSuccess responseSuccess = new AuthResponseSuccess();

        responseSuccess.setJwt(jwt);
        assertEquals(jwt, responseSuccess.getJwt());
    }


    @Test
    void testGetSetUser() {
        User user = new User();
        AuthResponseSuccess responseSuccess = new AuthResponseSuccess();

        responseSuccess.setUser(user);
        assertEquals(user, responseSuccess.getUser());
    }
}
