package com.pluggdd.burnandearn.utils;

import android.util.Base64;

import org.scribe.builder.api.DefaultApi20;
import org.scribe.extractors.AccessTokenExtractor;
import org.scribe.extractors.JsonTokenExtractor;
import org.scribe.model.OAuthConfig;
import org.scribe.model.OAuthConstants;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuth20ServiceImpl;
import org.scribe.oauth.OAuthService;
import org.scribe.utils.OAuthEncoder;

/**
 * Created by User on 11-Apr-16.
 */
public class FitBitApi extends DefaultApi20 {

    private static final String AUTHORIZATION_URL = "https://www.fitbit.com/oauth2/authorize?response_type=code";

    @Override
    public String getAccessTokenEndpoint() {
        return "https://api.fitbit.com/oauth2/token";
    }

    @Override
    public String getAuthorizationUrl(OAuthConfig config) {
        StringBuilder authUrl = new StringBuilder();
        authUrl.append(AUTHORIZATION_URL);
        authUrl.append("&scope=").append(OAuthEncoder.encode(config.getScope()));
        authUrl.append("&redirect_uri=").append(OAuthEncoder.encode(config.getCallback()));
        authUrl.append("&client_id=").append(config.getApiSecret());
        return authUrl.toString();
    }

    @Override
    public Verb getAccessTokenVerb()
    {
        return Verb.POST;
    }

    @Override
    public AccessTokenExtractor getAccessTokenExtractor()
    {
        return new JsonTokenExtractor();
    }

    /*@Override
    public String getRequestTokenEndpoint() {
        return "https://api.fitbit.com/oauth2/token";
    }

    @Override
    public String getAccessTokenEndpoint() {
        return "https://api.fitbit.com/oauth2/token";
    }

    @Override
    public String getAuthorizationUrl(OAuthConfig config) {
        return null;
    }

    @Override
    public String getAuthorizationUrl(Token requestToken) {
        return String.format(AUTHORIZE_URL, requestToken.getToken());
    }*/

    @Override
    public OAuthService createService(OAuthConfig config) {
        return new FitBitOAuth2Service(this, config);
    }

    private class FitBitOAuth2Service extends OAuth20ServiceImpl {

        private static final String GRANT_TYPE_AUTHORIZATION_CODE = "authorization_code";
        private static final String GRANT_TYPE = "grant_type";
        private DefaultApi20 api;
        private OAuthConfig config;

        public FitBitOAuth2Service(DefaultApi20 api, OAuthConfig config) {
            super(api, config);
            this.api = api;
            this.config = config;
        }

        @Override
        public Token getAccessToken(Token requestToken, Verifier verifier) {
            OAuthRequest request = new OAuthRequest(api.getAccessTokenVerb(),
                    api.getAccessTokenEndpoint());
            String oauth2Credentials = this.config.getApiSecret() + ":" + this.config.getApiKey();
            request.addHeader("Authorization", "Basic " + new String(Base64.encode(oauth2Credentials.getBytes(),Base64.NO_WRAP)));
            switch (api.getAccessTokenVerb()) {
                case POST:
                    request.addBodyParameter(OAuthConstants.CLIENT_ID,
                            config.getApiSecret());
                    request.addBodyParameter(OAuthConstants.CLIENT_SECRET,
                            config.getApiKey());
                    request.addBodyParameter(OAuthConstants.CODE,
                            verifier.getValue());
                    request.addBodyParameter(OAuthConstants.REDIRECT_URI,
                            config.getCallback());
                    request.addBodyParameter(GRANT_TYPE,
                            GRANT_TYPE_AUTHORIZATION_CODE);
                    break;
                case GET:
                default:
                    request.addQuerystringParameter(OAuthConstants.CLIENT_ID,
                            config.getApiKey());
                    request.addQuerystringParameter(OAuthConstants.CLIENT_SECRET,
                            config.getApiSecret());
                    request.addQuerystringParameter(OAuthConstants.CODE,
                            verifier.getValue());
                    request.addQuerystringParameter(OAuthConstants.REDIRECT_URI,
                            config.getCallback());
                    if (config.hasScope())
                        request.addQuerystringParameter(OAuthConstants.SCOPE,
                                config.getScope());
            }
            Response response = request.send();
            return api.getAccessTokenExtractor().extract(response.getBody());
        }
    }
}
