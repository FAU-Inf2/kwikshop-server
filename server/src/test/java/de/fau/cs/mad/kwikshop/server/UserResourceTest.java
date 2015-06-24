package de.fau.cs.mad.kwikshop.server;

import de.fau.cs.mad.kwikshop.common.User;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class UserResourceTest {
    private static final UserFacade facade = mock(UserFacade.class);

    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder().addResource(new UserResourceImpl(facade)).build();

    private final User janeDoe = new User("Jane", "Doe"),
                       johnDoe = new User("John", "Doe"),
                       jamesDoe = new User("John", "Doe");


    private final List<User> list = new ArrayList<User>(2);
    public UserResourceTest() {
        list.add(jamesDoe);
        list.add(janeDoe);
        list.add(johnDoe);
    }

    @Before
    public void setup() {
        // clean dao before each test
        reset(facade);

        // NOTE: we have used the L suffix intentionally since otherwise we compare ints with longs and the matcher fails
        when(facade.getUserById(eq(4711L))).thenReturn(janeDoe);

        when(facade.searchUsers(isNull(List.class), isNull(String.class), eq("Doe"))).thenReturn(list);

        when(facade.getUserById(eq(42L))).thenReturn(johnDoe);
        when(facade.updateUser(eq(42L), any(User.class))).thenCallRealMethod();
    }

    @Test
    @Ignore
    public void testGetPerson() {
        assertThat(resources.client().target("/users/4711").request().get(User.class)).isEqualTo(janeDoe);
        verify(facade).getUserById(4711L);
    }

    @Test
    @Ignore
    public void testFindByLastnameNamedQuery() {
        assertThat(resources.client().target("/users/namedQuery?lastName=Doe").request().get(User.class)).isEqualTo(johnDoe);
//   TODO fix     verify(facade).searchUserByLastName("Doe");
    }

    @Test
    @Ignore
    public void testFindByLastname() {
        assertThat(resources.client().target("/users/?lastName=Doe").request().get(new GenericType<List<User>>(){})).isEqualTo(list);
//      TODO fix  verify(facade).searchUsersByFirstAndLastName(null, "Doe");
    }

    @Test
    @Ignore
    public void testUpdate() {
        assertThat(resources.client().target("/users/42").request().put(Entity.entity(jamesDoe, MediaType.APPLICATION_JSON)).readEntity(User.class)).isEqualTo(jamesDoe);
    }
}
