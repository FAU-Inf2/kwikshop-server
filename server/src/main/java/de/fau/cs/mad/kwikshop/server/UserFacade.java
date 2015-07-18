package de.fau.cs.mad.kwikshop.server;

import de.fau.cs.mad.kwikshop.common.User;
import de.fau.cs.mad.kwikshop.server.dao.UserDAO;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Andreas Kumlehn on 4/9/15.
 */
public class UserFacade {

    private final UserDAO dao;

    public UserFacade(UserDAO dao) {

        this.dao = dao;
    }

    public User getUserById(String id) {
        return this.dao.findById(id);
    }

    public User createNewUser(User user) {
        return this.dao.create(user);
    }

    public User updateUser(String id, User user) {
        return this.dao.updateOrCreate(id, user);
    }

    public boolean deleteUser(String id) {
        User toDelete = dao.findById(id);

        if (toDelete == null) {
            return false;
        }

        this.dao.delete(toDelete);
        return false;
    }

    public List<User> searchBySessionToken(String sessionToken) {
        return dao.findBySessionToken(sessionToken);
    }

    public List<User> searchUsers(List<String> ids) {
        return this.searchUsersByIds(ids);
    }


    private List<User> searchUsersByIds(List<String> ids) {
        if (ids == null || ids.size() == 0) {
            return this.dao.listAll();
        } else {
            return this.searchUsersByIdList(ids);
        }
    }

    public List<User> searchUsersByIdList(List<String> ids) {
        List<User> result = new LinkedList<User>();

        for (String id : ids) {
            result.add(this.getUserById(id));
        }

        return result;
    }


}
