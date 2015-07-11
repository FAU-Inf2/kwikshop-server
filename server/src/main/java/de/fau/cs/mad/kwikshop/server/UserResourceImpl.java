package de.fau.cs.mad.kwikshop.server;

import de.fau.cs.mad.kwikshop.common.User;
import de.fau.cs.mad.kwikshop.common.rest.UserResource;
import io.dropwizard.hibernate.UnitOfWork;

/**
 * Created by Andreas Kumlehn on 3/26/15. 
 */
public class UserResourceImpl implements UserResource {

    private final UserFacade facade;

    public UserResourceImpl(UserFacade facade) {
        this.facade = facade;
    }

    @Override
    @UnitOfWork
    public String auth(String token) {
        String userId = new TokenHandler().TokenCheck(token);
        if(userId == null) // invalid token
            return null;
        else {
            User sessionUser = facade.getUserById(userId); // try to find this user in the db
            if(sessionUser == null) { // new user -> create a session token
                sessionUser = new User(userId);
                sessionUser.setSessionToken(new TokenHandler().nextSessionId());
                facade.updateUser(userId, sessionUser); // save this user
            }
            return sessionUser.getSessionToken();
        }
    }

    /*@Override
    @UnitOfWork
    public User get(String id) {
        return this.facade.getUserById(id);
    }

    @Override
    @UnitOfWork
    public List<User> get(List<String> ids, String firstName, String lastName) {
        boolean idsGiven = ids != null && ids.size() > 0;
        boolean nameGiven = firstName != null || lastName != null;

        if (idsGiven && nameGiven) {
            //TODO return 400
        }

        return this.facade.searchUsers(ids, firstName, lastName);
    }

    @Override
    @UnitOfWork
    public User createUser(User newUser) {
        return this.facade.createNewUser(newUser);
    }

    @Override
    @UnitOfWork
    public User createUser(String id, String firstName, String lastName) {
        User newUser = new User(id);
        return this.facade.createNewUser(newUser);
    }

    @Override
    @UnitOfWork
    public User updateUser(String id, User user) {
        return this.facade.updateUser(id, user);
    }

    @Override
    @UnitOfWork
    public void deleteUser(String id) {
        //TODO check return value
        boolean result = this.facade.deleteUser(id);
    }*/
}
