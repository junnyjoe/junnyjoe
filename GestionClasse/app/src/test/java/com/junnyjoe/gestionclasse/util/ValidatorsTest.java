package com.junnyjoe.gestionclasse.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ValidatorsTest {
    @Test
    public void emailValide() {
        assertTrue(Validators.isEmailValid("jean.dupont@univ.cm"));
        assertTrue(Validators.isEmailValid(" a_b+c@mail.example.org "));
    }

    @Test
    public void emailInvalide() {
        assertFalse(Validators.isEmailValid(""));
        assertFalse(Validators.isEmailValid("jean"));
        assertFalse(Validators.isEmailValid("jean@univ"));
        assertFalse(Validators.isEmailValid("@univ.cm"));
        assertFalse(Validators.isEmailValid(null));
    }

    @Test
    public void telephone() {
        assertTrue(Validators.isPhoneValid("+237 6 12 34 56 78"));
        assertTrue(Validators.isPhoneValid("690123456"));
        assertFalse(Validators.isPhoneValid(""));
        assertFalse(Validators.isPhoneValid("abc"));
        assertFalse(Validators.isPhoneValid("123"));
    }

    @Test
    public void champVide() {
        assertTrue(Validators.isBlank("   "));
        assertFalse(Validators.isBlank("x"));
    }
}
