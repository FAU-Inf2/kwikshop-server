package de.fau.cs.mad.kwikshop.server;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.wordnik.swagger.jaxrs.config.BeanConfig;
import com.wordnik.swagger.jaxrs.listing.ApiListingResource;
import de.fau.cs.mad.kwikshop.common.*;
import de.fau.cs.mad.kwikshop.common.rest.UserResource;
import de.fau.cs.mad.kwikshop.common.sorting.BoughtItem;
import de.fau.cs.mad.kwikshop.server.api.*;
import de.fau.cs.mad.kwikshop.server.auth.UserAuthenticator;
import de.fau.cs.mad.kwikshop.server.dao.*;
import de.fau.cs.mad.kwikshop.server.sorting.Edge;
import de.fau.cs.mad.kwikshop.server.sorting.Supermarket;
import de.fau.cs.mad.kwikshop.server.sorting.SupermarketChain;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.auth.AuthFactory;
import io.dropwizard.auth.basic.BasicAuthFactory;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;


public class ServerApplication extends Application<ServerConfiguration> {

    public static void main(String[] args) throws Exception {
        new ServerApplication().run(args);
    }

    private final HibernateBundle<ServerConfiguration> hibernate =
            new HibernateBundle<ServerConfiguration>(
                    User.class,
                    ShoppingListServer.class,
                    Item.class,
                    LastLocation.class,
                    Group.class,
                    Unit.class,
                    RecipeServer.class,
                    Supermarket.class,
                    SupermarketChain.class,
                    BoughtItem.class,
                    Edge.class,
                    SynchronizationLease.class) {
                @Override
                public DataSourceFactory getDataSourceFactory(ServerConfiguration configuration) {
                    DataSourceFactory fac = configuration.getDataSourceFactory();
                    return fac;
                }
            };

    @Override
    public String getName() {
        return "kwikshop-server";
    }

    @Override
    public void initialize(Bootstrap<ServerConfiguration> bootstrap) {
        bootstrap.addBundle(hibernate);

        //enables the use of environment variables in yaml config file
        bootstrap.setConfigurationSourceProvider(new SubstitutingSourceProvider(
                bootstrap.getConfigurationSourceProvider(),
                new EnvironmentVariableSubstitutor()
        ));

        //serve pages copied from https://github.com/swagger-api/swagger-ui/tree/master/dist
        bootstrap.addBundle(new AssetsBundle("/swagger/", "/docs", "index.html"));
    }

    @Override
    public void run(ServerConfiguration configuration, Environment environment) throws Exception {

        //create DAOs
        final UserDAO dao = new UserDAO(hibernate.getSessionFactory());
        final UserFacade facade = new UserFacade(dao);
        final UnitDAO unitDAO = new UnitDAO(hibernate.getSessionFactory());
        final GroupDAO groupDAO = new GroupDAO(hibernate.getSessionFactory());
        final LocationDAO locationDAO = new LocationDAO(hibernate.getSessionFactory());
        final BoughtItemDAO boughtItemDAO = new BoughtItemDAO(hibernate.getSessionFactory());
        final EdgeDAO edgeDAO = new EdgeDAO(hibernate.getSessionFactory(), boughtItemDAO);
        final SupermarketDAO supermarketDAO = new SupermarketDAO(hibernate.getSessionFactory());
        final SupermarketChainDAO supermarketChainDAO = new SupermarketChainDAO(hibernate.getSessionFactory(), supermarketDAO);
        final SynchronizationLeaseDAO leaseDAO = new SynchronizationLeaseDAO(hibernate.getSessionFactory());
        final RecipeDAO recipeDAO = new RecipeDAO(hibernate.getSessionFactory(), unitDAO, groupDAO, locationDAO);
        final ShoppingListDAO shoppingListDAO = new ShoppingListDAO(hibernate.getSessionFactory(), unitDAO, groupDAO, locationDAO);
        final SharedShoppingListDAO sharedShoppingListDAO = new SharedShoppingListDAO(hibernate.getSessionFactory(), unitDAO, groupDAO, locationDAO);

        //authentication setuup
        final UserResourceImpl userResource = new UserResourceImpl(facade);
        environment.jersey().register(userResource);
        final UserAuthenticator authenticator = new UserAuthenticator(facade);
        environment.jersey().register(AuthFactory.binder(
                new BasicAuthFactory<>(authenticator,
                "SUPER SECRET STUFF",
                User.class)));

        // shopping list resource
        final ShoppingListResourceImpl shoppingListResource = new ShoppingListResourceImpl(
                shoppingListDAO,
                sharedShoppingListDAO,
                edgeDAO, boughtItemDAO, supermarketDAO, supermarketChainDAO);

        environment.jersey().register(shoppingListResource);


        // recipe resource
        final RecipeResourceImpl recipeResource = new RecipeResourceImpl(recipeDAO);
        environment.jersey().register(recipeResource);

        final ApiListingResource api = new ApiListingResource();
        environment.jersey().register(api);

        // lease resource
        environment.jersey().register(new LeaseResourceImpl(leaseDAO, recipeDAO, shoppingListDAO, sharedShoppingListDAO, new LeaseValidator(leaseDAO)));

        // register feature that ensures only requests from clients with leases are process for resources annotated with @RequiresLease
        environment.jersey().register(RequiresLeaseFeature.class);

        //set up swagger
        configureSwagger(environment);
    }

    private void configureSwagger(Environment environment) {
        //option to provide correct json for swagger
        environment.getObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);

        BeanConfig config = new BeanConfig();
        config.setTitle(getName());
        config.setVersion("0.0.1");
        String pkg = UserResource.class.getPackage().toString().split(" ")[1];
        config.setResourcePackage(pkg);
        config.setScan(true);
    }
}
