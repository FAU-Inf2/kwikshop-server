package de.fau.cs.mad.kwikshop.server.auth;

import com.google.common.base.Optional;

import de.fau.cs.mad.kwikshop.common.User;
import de.fau.cs.mad.kwikshop.server.UserFacade;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicCredentials;

import java.util.List;

public class UserAuthenticator implements Authenticator<BasicCredentials, User> {

    private final UserFacade facade;

    public UserAuthenticator(UserFacade facade) {
        this.facade = facade;
    }

    @Override
    public Optional<User> authenticate(BasicCredentials credentials) throws AuthenticationException {

        /*if(credentials.getUsername().equals("DEBUG") && credentials.getPassword().equals("DEBUG")) {

            List<User> debugUsers = facade.searchBySessionToken("debugToken");
            if(debugUsers.size()  == 0) {
                User debugUser = new User("debugUser");
                debugUser.setSessionToken("debugToken");
                facade.createNewUser(debugUser);
                return Optional.of(debugUser);
            } else {
                return Optional.of(debugUsers.get(0));
            }
        }*/

        User authUser = facade.getUserById(credentials.getUsername());
        if(authUser != null) {
            if(credentials.getPassword().equals(authUser.getSessionToken()))
                return Optional.of(authUser);
        }

        return Optional.absent();
    }

}
