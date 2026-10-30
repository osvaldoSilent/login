package com.osvaldevops.login.domain.port.out;

import com.osvaldevops.login.domain.model.User;

public interface TokenProviderPort {

    String generateToken(User user);

    long getExpirationMs();
}

