package org.keycloak.operator.controllers;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.utils.KubernetesResourceUtil;
import org.keycloak.operator.crds.v2alpha1.deployment.Keycloak;

import java.util.Optional;
import java.util.UUID;

public class KeycloakAdminSecret extends OperatorManagedResource {

    private final String secretName;

    public KeycloakAdminSecret(KubernetesClient client, Keycloak keycloak) {
        super(client, keycloak);
        if (keycloak.getExistingAdminSecret() == null) {
            this.secretName = KubernetesResourceUtil.sanitizeName(keycloak.getMetadata().getName() + "-initial-admin");
        } else {
            this.secretName = keycloak.getExistingAdminSecret()
        }
    }

    @Override
    protected Optional<HasMetadata> getReconciledResource() {
        if (client.secrets().inNamespace(getNamespace()).withName(secretName).get() != null) {
            return Optional.empty();
        } else {
            if (keycloak.getExistingAdminSecret() == null) {
                return Optional.of(createSecret());
            } else {
                Log.error("No existing Keycloak Admin Secret %s is found. Please create secret first", secretName);
                return Optional.empty();
            }
        }
    }

    private Secret createSecret() {
        return new SecretBuilder()
                .withNewMetadata()
                .withName(secretName)
                .withNamespace(getNamespace())
                .endMetadata()
                .withType("kubernetes.io/basic-auth")
                .addToStringData("username", "admin")
                .addToStringData("password", UUID.randomUUID().toString().replace("-", ""))
                .build();
    }

    @Override
    public String getName() { return secretName; }

}
