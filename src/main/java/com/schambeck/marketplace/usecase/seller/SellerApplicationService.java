package com.schambeck.marketplace.usecase.seller;

import com.schambeck.marketplace.exception.OfferValidationException;
import com.schambeck.marketplace.usecase.user.User;
import com.schambeck.marketplace.usecase.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SellerApplicationService {
    private final SellerApplicationRepository applications;
    private final SellerApplicationHistoryRepository history;
    private final UserRepository users;
    private final RestClient.Builder restClientBuilder;

    @Value("${seller.keycloak.base-url:}") private String keycloakBaseUrl;
    @Value("${seller.keycloak.realm:marketplace}") private String keycloakRealm;
    @Value("${seller.keycloak.client-id:}") private String clientId;
    @Value("${seller.keycloak.client-secret:}") private String clientSecret;

    public SellerApplicationWeb mine(String externalId) {
        return applications.findFirstByUser_ExternalIdOrderBySubmittedAtDesc(externalId)
                .map(SellerApplicationWeb::from).orElse(null);
    }

    @Transactional
    public SellerApplicationWeb submit(String externalId, SellerApplicationRequest request) {
        if (request == null || !Boolean.TRUE.equals(request.getTermsAccepted())) throw new OfferValidationException("Seller terms must be accepted.");
        if (applications.existsByUser_ExternalIdAndStatus(externalId, SellerApplicationStatus.PENDING)) throw new OfferValidationException("A seller application is already pending.");
        require(request.getPublicName(), "publicName"); require(request.getDescription(), "description");
        require(request.getContactEmail(), "contactEmail"); require(request.getItemTypes(), "itemTypes");
        require(request.getDeliveryMethod(), "deliveryMethod"); require(request.getTermsVersion(), "termsVersion");
        User user = users.findByExternalIdOrThrow(externalId);
        SellerApplication a = applications.save(SellerApplication.builder().user(user)
                .publicName(request.getPublicName().trim()).description(request.getDescription().trim())
                .contactEmail(request.getContactEmail().trim()).itemTypes(request.getItemTypes().trim())
                .deliveryMethod(request.getDeliveryMethod().trim()).termsVersion(request.getTermsVersion().trim())
                .status(SellerApplicationStatus.PENDING).version(0).build());
        history.save(SellerApplicationHistory.of(a, null, SellerApplicationStatus.PENDING, null, externalId));
        return SellerApplicationWeb.from(a);
    }

    public Page<SellerApplicationWeb> pending(Pageable pageable) {
        return applications.findAllByStatusOrderBySubmittedAtAsc(SellerApplicationStatus.PENDING, pageable).map(SellerApplicationWeb::from);
    }

    @Transactional
    public SellerApplicationWeb withdraw(Integer id, String externalId) {
        SellerApplication a = applications.findById(id).orElseThrow(() -> new OfferValidationException("Seller application not found."));
        if (!a.getUser().getExternalId().equals(externalId)) throw new OfferValidationException("Application does not belong to the authenticated user.");
        if (a.getStatus() != SellerApplicationStatus.PENDING) throw new OfferValidationException("Only pending applications can be withdrawn.");
        a.setStatus(SellerApplicationStatus.WITHDRAWN);
        applications.save(a);
        history.save(SellerApplicationHistory.of(a, SellerApplicationStatus.PENDING, SellerApplicationStatus.WITHDRAWN, "withdrawn by applicant", externalId));
        return SellerApplicationWeb.from(a);
    }

    @Transactional
    public SellerApplicationWeb decide(Integer id, boolean approve, String reason, String actor) {
        SellerApplication a = applications.findById(id).orElseThrow(() -> new OfferValidationException("Seller application not found."));
        if (a.getStatus() != SellerApplicationStatus.PENDING) throw new OfferValidationException("Only pending applications can be decided.");
        require(reason, "reason");
        if (approve) assignSellerRole(a.getUser().getExternalId());
        a.setStatus(approve ? SellerApplicationStatus.APPROVED : SellerApplicationStatus.REJECTED);
        a.setDecisionReason(reason.trim()); a.setDecidedBy(actor);
        applications.save(a);
        history.save(SellerApplicationHistory.of(a, SellerApplicationStatus.PENDING, a.getStatus(), reason.trim(), actor));
        return SellerApplicationWeb.from(a);
    }

    @Transactional
    public SellerApplicationWeb setSuspended(Integer id, boolean suspend, String reason, String actor) {
        SellerApplication a = applications.findById(id).orElseThrow(() -> new OfferValidationException("Seller application not found."));
        SellerApplicationStatus expected = suspend ? SellerApplicationStatus.APPROVED : SellerApplicationStatus.SUSPENDED;
        SellerApplicationStatus target = suspend ? SellerApplicationStatus.SUSPENDED : SellerApplicationStatus.APPROVED;
        if (a.getStatus() != expected) throw new OfferValidationException("Invalid seller application transition.");
        require(reason, "reason");
        if (suspend) removeSellerRole(a.getUser().getExternalId()); else assignSellerRole(a.getUser().getExternalId());
        a.setStatus(target); a.setDecisionReason(reason.trim()); a.setDecidedBy(actor);
        applications.save(a);
        history.save(SellerApplicationHistory.of(a, expected, target, reason.trim(), actor));
        return SellerApplicationWeb.from(a);
    }

    private void assignSellerRole(String externalId) {
        if (keycloakBaseUrl.isBlank() || clientId.isBlank() || clientSecret.isBlank()) throw new IllegalStateException("Seller role provisioning is not configured.");
        RestClient client = restClientBuilder.baseUrl(keycloakBaseUrl).build();
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        Map<?, ?> token = client.post().uri("/realms/{realm}/protocol/openid-connect/token", keycloakRealm)
                .body(form)
                .retrieve().body(Map.class);
        String accessToken = (String) token.get("access_token");
        String userId = client.get().uri("/admin/realms/{realm}/users/{id}", keycloakRealm, externalId)
                .header("Authorization", "Bearer " + accessToken).retrieve().body(Map.class).get("id").toString();
        Map role = client.get().uri("/admin/realms/{realm}/roles/SELLER", keycloakRealm)
                .header("Authorization", "Bearer " + accessToken).retrieve().body(Map.class);
        client.post().uri("/admin/realms/{realm}/users/{id}/role-mappings/realm", keycloakRealm, userId)
                .header("Authorization", "Bearer " + accessToken).body(java.util.List.of(role)).retrieve().toBodilessEntity();
    }

    private void removeSellerRole(String externalId) {
        if (keycloakBaseUrl.isBlank() || clientId.isBlank() || clientSecret.isBlank()) throw new IllegalStateException("Seller role provisioning is not configured.");
        RestClient client = restClientBuilder.baseUrl(keycloakBaseUrl).build();
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials"); form.add("client_id", clientId); form.add("client_secret", clientSecret);
        Map<?, ?> token = client.post().uri("/realms/{realm}/protocol/openid-connect/token", keycloakRealm).body(form).retrieve().body(Map.class);
        String accessToken = (String) token.get("access_token");
        Map role = client.get().uri("/admin/realms/{realm}/roles/SELLER", keycloakRealm)
                .header("Authorization", "Bearer " + accessToken).retrieve().body(Map.class);
        client.get().uri("/admin/realms/{realm}/users/{id}", keycloakRealm, externalId)
                .header("Authorization", "Bearer " + accessToken).retrieve().toBodilessEntity();
        client.method(org.springframework.http.HttpMethod.DELETE)
                .uri("/admin/realms/{realm}/users/{id}/role-mappings/realm", keycloakRealm, externalId)
                .header("Authorization", "Bearer " + accessToken).body(java.util.List.of(role)).retrieve().toBodilessEntity();
    }

    private static void require(String value, String field) {
        if (value == null || value.isBlank()) throw new OfferValidationException(field + " is required.");
    }
}
