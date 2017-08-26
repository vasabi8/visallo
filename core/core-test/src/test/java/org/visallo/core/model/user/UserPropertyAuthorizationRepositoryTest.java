package org.visallo.core.model.user;

import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.visallo.core.config.Configuration;
import org.visallo.core.config.HashMapConfigurationLoader;
import org.visallo.core.user.User;
import org.visallo.core.util.VisalloInMemoryTestBase;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserPropertyAuthorizationRepositoryTest extends VisalloInMemoryTestBase {
    private UserPropertyAuthorizationRepository userPropertyAuthorizationRepository;

    @Mock
    private User user1;

    private User user2;

    @Before
    public void before() {
        Map config = new HashMap();
        config.put(
                UserPropertyAuthorizationRepository.CONFIGURATION_PREFIX + ".defaultAuthorizations",
                "userRepositoryAuthorization1,userRepositoryAuthorization2"
        );
        Configuration configuration = new HashMapConfigurationLoader(config).createConfiguration();

        userPropertyAuthorizationRepository = new UserPropertyAuthorizationRepository(
                getGraph(),
                getOntologyRepository(),
                configuration,
                getUserNotificationRepository(),
                getWorkQueueRepository(),
                getGraphAuthorizationRepository()
        ) {
            @Override
            protected UserRepository getUserRepository() {
                return UserPropertyAuthorizationRepositoryTest.this.getUserRepository();
            }
        };

        user2 = getUserRepository().findOrAddUser("user2", "User 2", "user2@visallo.com", "password");
        userPropertyAuthorizationRepository.setAuthorizations(user2, Collections.emptySet(), user1);
    }

    @Test
    public void testGetAuthorizationsForNewUser() {
        HashSet<String> expected = Sets.newHashSet("userRepositoryAuthorization1", "userRepositoryAuthorization2");

        Set<String> privileges = userPropertyAuthorizationRepository.getAuthorizations(user1);
        assertEquals(expected, privileges);
    }

    @Test
    public void testGetAuthorizationsForExisting() {
        String[] authorizationsArray = {"userAuthorization1", "userAuthorization2", "userRepositoryAuthorization1", "userRepositoryAuthorization2"};
        when(user1.getProperty(eq(UserPropertyAuthorizationRepository.AUTHORIZATIONS_PROPERTY_IRI)))
                .thenReturn("userAuthorization1,userAuthorization2");

        Set<String> privileges = userPropertyAuthorizationRepository.getAuthorizations(user1);
        assertEquals(Sets.newHashSet(authorizationsArray), privileges);
    }

    @Test
    public void testAddAuthorization() {
        String[] authorizationsArray = {"newAuth", "userRepositoryAuthorization1", "userRepositoryAuthorization2"};
        String authorization = "newAuth";

        userPropertyAuthorizationRepository.addAuthorization(user2, authorization, user1);
        Set<String> privileges = userPropertyAuthorizationRepository.getAuthorizations(user2);

        assertEquals(Sets.newHashSet(authorizationsArray), privileges);
    }

    @Test
    public void testAddAuthorizationShouldTrimWhitespace() {
        String[] authorizationsArray = {"newAuth", "userRepositoryAuthorization1", "userRepositoryAuthorization2"};
        String authorization = "  newAuth  \n";

        userPropertyAuthorizationRepository.addAuthorization(user2, authorization, user1);
        Set<String> privileges = userPropertyAuthorizationRepository.getAuthorizations(user2);

        assertEquals(Sets.newHashSet(authorizationsArray), privileges);
    }
}