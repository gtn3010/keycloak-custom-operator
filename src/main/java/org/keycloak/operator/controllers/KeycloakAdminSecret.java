package org.keycloak.operator.controllers;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.utils.KubernetesResourceUtil;
import org.keycloak.operator.crds.v2alpha1.deployment.Keycloak;

import java.util.Optional;
import java.util.UUID;
import io.quarkus.logging.Log;

public class KeycloakAdminSecret extends OperatorManagedResource {

    private final String secretName;
    private String existSecretName;

    // Constructor method
    public KeycloakAdminSecret(KubernetesClient client, Keycloak keycloak) {
        super(client, keycloak);
        this.existSecretName = keycloak.getSpec().getExistingAdminSecret();
        if (this.existSecretName == null) {
            this.secretName = KubernetesResourceUtil.sanitizeName(keycloak.getMetadata().getName() + "-initial-admin");
        } else {
            this.secretName = this.existSecretName;
        }
    }

    @Override
    protected Optional<HasMetadata> getReconciledResource() {
        if (client.secrets().inNamespace(getNamespace()).withName(secretName).get() != null) {
            return Optional.empty();
        } else {
            if (existSecretName == null) {
                return Optional.of(createSecret());
            } else {
                Log.errorf("No existing Keycloak Admin Secret %s is found. Please create secret first", secretName);
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
