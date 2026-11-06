package com.osvaldevops.login;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Smoke test verifying the {@link LoginServiceApplication} entry point class.
 */
class LoginServiceApplicationTests {

    @Test
    void applicationClassExists() {
        assertThat(LoginServiceApplication.class).isNotNull();
    }
}
