package de.fau.cs.mad.kwikshop.server.auth;

import com.google.common.base.Optional;

import de.fau.cs.mad.kwikshop.common.User;
import de.fau.cs.mad.kwikshop.server.UserFacade;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicCredentials;

public class UserAuthenticator implements Authenticator<BasicCredentials, User> {

    private static UserFacade facade;

    public UserAuthenticator(UserFacade facade) {
        this.facade = facade;
    }

    @Override
    public Optional<User> authenticate(BasicCredentials credentials) throws AuthenticationException {
        User authUser = facade.getUserById(credentials.getUsername());
        if(authUser != null) {
            if(credentials.getPassword().equals(authUser.getSessionToken()))
                return Optional.of(authUser);
        }

        return Optional.absent();
    }

}
