package io.fusionauth.example;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;

import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.JWTConfiguration;
import io.fusionauth.domain.Key;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserRegistration;
import io.fusionauth.domain.api.ApplicationRequest;
import io.fusionauth.domain.api.ApplicationResponse;
import io.fusionauth.domain.api.KeyRequest;
import io.fusionauth.domain.api.KeyResponse;
import io.fusionauth.domain.api.TenantResponse;
import io.fusionauth.domain.api.UserResponse;
import io.fusionauth.domain.api.user.RegistrationRequest;
import io.fusionauth.domain.api.user.RegistrationResponse;
import io.fusionauth.domain.api.user.SearchRequest;
import io.fusionauth.domain.api.user.SearchResponse;
import io.fusionauth.domain.oauth2.GrantType;
import io.fusionauth.domain.oauth2.OAuth2Configuration;
import io.fusionauth.domain.oauth2.ProofKeyForCodeExchangePolicy;
import io.fusionauth.domain.search.UserSearchCriteria;

public class Setup {

  public static final String APPLICATION_ID = "E9FDB985-9173-4E01-9D73-AC2D60D1DC8E";

  public static void main(String[] args) throws URISyntaxException {
    final String apiKey = System.getProperty("fusionauth.api.key");
    final FusionAuthClient client = new FusionAuthClient(apiKey, "http://localhost:9011");

    // set the issuer up correctly    
    ClientResponse<TenantResponse, Void> retrieveTenantsResponse = client.retrieveTenants();
    if (!retrieveTenantsResponse.wasSuccessful()) {
      throw new RuntimeException("couldn't find tenants");
    }

    // should only be one
    Tenant tenant = retrieveTenantsResponse.successResponse.tenants.get(0);


    Map<String, Object> issuerUpdateMap = new HashMap<String, Object>();
    Map<String, Object> tenantMap = new HashMap<String, Object>();
    tenantMap.put("issuer","http://localhost:9011");
    issuerUpdateMap.put("tenant", tenantMap);
    ClientResponse<TenantResponse, Errors> patchTenantResponse = client.patchTenant(tenant.id, issuerUpdateMap );
    if (!patchTenantResponse.wasSuccessful()) {
      throw new RuntimeException("couldn't update tenant");
    }

    // generate RSA keypair
    UUID rsaKeyId = UUID.fromString("356a6624-b33c-471a-b707-48bbfcfbc593");

    Key rsaKey = new Key();
    rsaKey.algorithm = Key.KeyAlgorithm.RS256;
    rsaKey.name = "For JavaExampleApp";
    rsaKey.length = 2048;
    KeyRequest keyRequest = new KeyRequest(rsaKey);
    ClientResponse<KeyResponse, Errors> keyResponse = client.generateKey(rsaKeyId, keyRequest);
    if (!keyResponse.wasSuccessful()) {
      throw new RuntimeException("couldn't create RSA key");
    }

    // create application
    Application application = new Application();
    application.oauthConfiguration = new OAuth2Configuration();
    application.oauthConfiguration.authorizedRedirectURLs = new ArrayList<URI>();
    application.oauthConfiguration.authorizedRedirectURLs.add(new URI("http://localhost:8080/login/oauth2/code/fusionauth"));
    application.oauthConfiguration.requireRegistration = true;

    application.oauthConfiguration.enabledGrants = new HashSet<GrantType>(Arrays.asList(new GrantType[] {GrantType.authorization_code, GrantType.refresh_token}));
    application.oauthConfiguration.logoutURL = new URI("http://localhost:8080/logout");
    application.oauthConfiguration.proofKeyForCodeExchangePolicy = ProofKeyForCodeExchangePolicy.Required;
    application.name = "JavaExampleApp";

    // assign key from above to sign our tokens. This needs to be asymmetric
    application.jwtConfiguration = new JWTConfiguration();
    application.jwtConfiguration.enabled = true;
    application.jwtConfiguration.accessTokenKeyId = rsaKeyId;
    application.jwtConfiguration.idTokenKeyId = rsaKeyId;

    UUID clientId = UUID.fromString(APPLICATION_ID);
    String clientSecret = "change-this-in-production-to-be-a-real-secret";

    application.oauthConfiguration.clientSecret = clientSecret;
    ApplicationRequest applicationRequest = new ApplicationRequest(application);
    ClientResponse<ApplicationResponse, Errors> applicationResponse = client.createApplication(clientId, applicationRequest);
    if (!applicationResponse.wasSuccessful()) {
      throw new RuntimeException("couldn't create application");
    }

    // register user, there should be only one, so grab the first
    UserSearchCriteria userSearchCriteria = new UserSearchCriteria();
    userSearchCriteria.queryString = "*";
    SearchRequest searchRequest = new SearchRequest(userSearchCriteria );

    ClientResponse<SearchResponse, Errors> userSearchResponse = client.searchUsersByQuery(searchRequest);
    if (!userSearchResponse.wasSuccessful()) {
      throw new RuntimeException("couldn't find users");
    }
    User myUser = userSearchResponse.successResponse.users.get(0);

    // patch the user to make sure they have a full name, otherwise OIDC has issues
    Map<String, Object> fullNameUpdateMap = new HashMap<String, Object>();
    Map<String, Object> userMap = new HashMap<String, Object>();
    userMap.put("fullName",myUser.firstName+ " "+myUser.lastName);
    fullNameUpdateMap.put("user", userMap);
    ClientResponse<UserResponse, Errors> patchUserResponse = client.patchUser(myUser.id, fullNameUpdateMap);
    if (!patchUserResponse.wasSuccessful()) {
      throw new RuntimeException("couldn't update user");
    }

    // now register the user
    UserRegistration registration = new UserRegistration();
    registration.applicationId = clientId;

    // otherwise we try to create the user as well as add the registration
    User nullBecauseWeHaveExistingUser = null;

    RegistrationRequest registrationRequest = new RegistrationRequest(nullBecauseWeHaveExistingUser, registration );
    ClientResponse<RegistrationResponse, Errors> registrationResponse = client.register(myUser.id, registrationRequest);
    if (!registrationResponse.wasSuccessful()) {
      throw new RuntimeException("couldn't register user");
    }
  }
}
