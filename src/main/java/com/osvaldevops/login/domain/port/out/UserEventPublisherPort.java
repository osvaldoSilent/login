package com.osvaldevops.login.domain.port.out;

import com.osvaldevops.login.domain.event.UserCreatedEvent;

public interface UserEventPublisherPort {

    void publishUserCreated(UserCreatedEvent event);
}

