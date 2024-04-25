package nu.marginalia.query;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import nu.marginalia.service.MainClass;
import nu.marginalia.service.discovery.ServiceRegistryIf;
import nu.marginalia.service.module.ServiceDiscoveryModule;
import nu.marginalia.service.ServiceId;
import nu.marginalia.service.module.ServiceConfigurationModule;
import nu.marginalia.service.module.DatabaseModule;
import nu.marginalia.service.server.Initialization;

public class QueryMain extends MainClass {
    private final QueryService service;

    @Inject
    public QueryMain(QueryService service) {
        this.service = service;
    }

    public static void main(String... args) {
        init(ServiceId.Query, args);

        Injector injector = Guice.createInjector(
                new QueryModule(),
                new DatabaseModule(false),
                new ServiceDiscoveryModule(),
                new ServiceConfigurationModule(ServiceId.Query)
        );

        // Ensure that the service registry is initialized early
        injector.getInstance(ServiceRegistryIf.class);

        injector.getInstance(QueryMain.class);
        injector.getInstance(Initialization.class).setReady();
    }

}
