package org.jboss.hal.testsuite.test.configuration.undertow;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.hal.testsuite.creaper.command.AddSocketBinding;
import org.jboss.hal.testsuite.creaper.command.RemoveSocketBinding;
import org.jboss.hal.testsuite.util.AvailablePortFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wildfly.extras.creaper.commands.undertow.AddHttpsSecurityRealm;
import org.wildfly.extras.creaper.commands.undertow.RemoveHttpsSecurityRealm;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public final class UndertowOperations {

    private static final Logger log = LoggerFactory.getLogger(UndertowOperations.class);

    private final OnlineManagementClient client;
    private final Administration administration;
    private final Operations operations;
    private final ReferenceHolder referenceHolder = new ReferenceHolder();

    private static final Address IO_SUBSYSTEM_ADDRESS = Address.subsystem("io");
    private static final Address UNDERTOW_SUBSYSTEM_ADDRESS = Address.subsystem("undertow");

    public UndertowOperations(OnlineManagementClient client) {
        this.client = client;
        this.operations = new Operations(client);
        this.administration = new Administration(client, 60000);
    }

    private Address getWorkerAddress(String name) {
        return IO_SUBSYSTEM_ADDRESS.and("worker", name);
    }

    private Address getBufferPoolAddress(String name) {
        return IO_SUBSYSTEM_ADDRESS.and("buffer-pool", name);
    }

    private Address getBufferCacheAddress(String name) {
        return UNDERTOW_SUBSYSTEM_ADDRESS.and("buffer-cache", name);
    }

    public String createSecurityRealm() throws InterruptedException, CommandFailedException, TimeoutException, IOException {
        String name = "UndertowSecurityRealm_" + RandomStringUtils.randomAlphanumeric(6);
        client.apply(new AddHttpsSecurityRealm.Builder(name)
                .keystorePassword("random")
                .truststorePassword("random")
                .keyPassword("random")
                .keystorePath(getClass().getClassLoader().getResource("clientkeystore").getPath())
                .build());
        administration.reload();
        referenceHolder.saveReference(name, ReferenceType.SECURITY_REALM);
        return name;
    }

    public String createWorker() throws InterruptedException, TimeoutException, IOException {
        String name = "UndertowWorker_" + RandomStringUtils.randomAlphanumeric(6);
        operations.add(getWorkerAddress(name));
        referenceHolder.saveReference(name, ReferenceType.WORKER);
        return name;
    }

    public String createBufferPool() throws IOException {
        String name = "UndertowBufferPool_" + RandomStringUtils.randomAlphanumeric(6);
        operations.add(getBufferPoolAddress(name));
        referenceHolder.saveReference(name, ReferenceType.BUFFER_POOL);
        return name;
    }

    public String createSocketBinding() throws IOException, CommandFailedException {
        String name = createSocketBindingWithoutReference();
        referenceHolder.saveReference(name, ReferenceType.SOCKET_BINDING);
        return name;
    }

    public String createSocketBindingWithoutReference() throws IOException, CommandFailedException {
        String name = "UndertowSocketBinding_" + RandomStringUtils.randomAlphanumeric(6);
        int port = AvailablePortFinder.getNextAvailableNonPrivilegedPort();
        log.info("Obtained port for socket binding '{}' is '{}'.", name, port);
        client.apply(new AddSocketBinding.Builder(name)
                .port(port)
                .build());
        return name;
    }

    public String createBufferCache() throws IOException {
        String name = "UndertowBufferCache_" + RandomStringUtils.randomAlphanumeric(6);
        operations.add(getBufferCacheAddress(name));
        referenceHolder.saveReference(name, ReferenceType.BUFFER_CACHE);
        return name;
    }

    public void cleanupReferences() throws IOException, OperationException, CommandFailedException {
        for (String socketBinding : referenceHolder.getReferencesByType(ReferenceType.SOCKET_BINDING)) {
            client.apply(new RemoveSocketBinding(socketBinding));
        }
        for (String securityRealm : referenceHolder.getReferencesByType(ReferenceType.SECURITY_REALM)) {
            client.apply(new RemoveHttpsSecurityRealm(securityRealm));
        }
        for (String worker : referenceHolder.getReferencesByType(ReferenceType.WORKER)) {
            operations.remove(getWorkerAddress(worker));
        }
        for (String bufferCache : referenceHolder.getReferencesByType(ReferenceType.BUFFER_CACHE)) {
            operations.remove(getBufferCacheAddress(bufferCache));
        }
        for (String bufferPool : referenceHolder.getReferencesByType(ReferenceType.BUFFER_POOL)) {
            operations.remove(getBufferPoolAddress(bufferPool));
        }
    }

    private final class ReferenceHolder {

        private final Map<ReferenceType, List<String>> references;

        ReferenceHolder() {
            this.references = new HashMap<>();
        }

        public void saveReference(String reference, ReferenceType type) {
            if (!references.containsKey(type)) {
                references.put(type, new LinkedList<>());
            }
            references.get(type).add(reference);
        }

        public List<String> getReferencesByType(ReferenceType type) {
            if (!references.containsKey(type)) {
                return Collections.emptyList();
            }
            return references.get(type);
        }
    }

    private enum ReferenceType {
        SOCKET_BINDING, BUFFER_CACHE, BUFFER_POOL, WORKER, SECURITY_REALM
    }
}
