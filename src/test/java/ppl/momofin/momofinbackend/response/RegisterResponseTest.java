package ppl.momofin.momofinbackend.response;

import org.junit.jupiter.api.Test;
import ppl.momofin.momofinbackend.model.User;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RegisterResponseTest {
    @Test
    void testRegisterResponseSuccessConstructorEmpty() {
        RegisterResponseSuccess responseSuccess = new RegisterResponseSuccess();

        assertNotNull(responseSuccess);
        assertNull(responseSuccess.getUser());
    }

    @Test
    void testRegisterResponseSuccessConstructor() {
        User user = new User();
        RegisterResponseSuccess responseSuccess = new RegisterResponseSuccess(user);

        assertEquals(user, responseSuccess.getUser());
    }


    @Test
    void testGetSetUser() {
        User user = new User();
        RegisterResponseSuccess responseSuccess = new RegisterResponseSuccess();

        responseSuccess.setUser(user);
        assertEquals(user, responseSuccess.getUser());
    }
}
