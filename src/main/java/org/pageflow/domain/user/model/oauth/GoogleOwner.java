package org.pageflow.domain.user.model.oauth;

import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.user.OAuth2User;

public class GoogleOwner extends OAuth2ProviderUser {
    
    
    public GoogleOwner(OAuth2User oAuth2User, ClientRegistration clientRegistration) {
        super(oAuth2User.getAttributes(), oAuth2User, clientRegistration);
    }
    
    @Override
    public String getId() {
        return getProvider() + "-" + ((String)getAttributes().get("sub"));
    }

}
