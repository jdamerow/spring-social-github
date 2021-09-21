/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.social.github.oauth2;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.social.oauth2.AccessGrant;
import org.springframework.social.oauth2.OAuth2Template;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * OAuth2Operations implementation that uses REST-template to make the OAuth calls.
 * @author Keith Donald
 * @author Roy Clarkson
 */
public class GitHubOAuth2Template extends OAuth2Template {
	
	private final static Log logger = LogFactory.getLog(GitHubOAuth2Template.class);
	
	private final String clientId;
	
	private final String clientSecret;

	private final String accessTokenUrl;

	private final String authorizeUrl;

	private String authenticateUrl;
	
	private boolean useParametersForClientAuthentication; 
	
	/**
	 * Constructs an OAuth2Template for a given set of client credentials. 
	 * Assumes that the authorization URL is the same as the authentication URL.
	 * @param clientId the client ID
	 * @param clientSecret the client secret
	 * @param authorizeUrl the base URL to redirect to when doing authorization code or implicit grant authorization
	 * @param accessTokenUrl the URL at which an authorization code, refresh token, or user credentials may be exchanged for an access token.
	 */
	public GitHubOAuth2Template(String clientId, String clientSecret, String authorizeUrl, String accessTokenUrl) {
		this(clientId, clientSecret, authorizeUrl, null, accessTokenUrl);
	}

	/**
	 * Constructs an OAuth2Template for a given set of client credentials. 
	 * @param clientId the client ID
	 * @param clientSecret the client secret
	 * @param authorizeUrl the base URL to redirect to when doing authorization code or implicit grant authorization
	 * @param authenticateUrl the URL to redirect to when doing authentication via authorization code grant
	 * @param accessTokenUrl the URL at which an authorization code, refresh token, or user credentials may be exchanged for an access token
	 */
	public GitHubOAuth2Template(String clientId, String clientSecret, String authorizeUrl, String authenticateUrl, String accessTokenUrl) {
		super(clientId, clientSecret, authorizeUrl, authenticateUrl, accessTokenUrl);
		this.clientId = clientId;
		this.clientSecret = clientSecret;
		String clientInfo = "?client_id=" + formEncode(clientId);
		this.authorizeUrl = authorizeUrl + clientInfo;
		if (authenticateUrl != null) {
			this.authenticateUrl = authenticateUrl + clientInfo;
		} else {
			this.authenticateUrl = null;
		}
		this.accessTokenUrl = accessTokenUrl;
	}
	
	/**
	 * Set to true to pass client credentials to the provider as parameters instead of using HTTP Basic authentication.
	 * @param useParametersForClientAuthentication true if the client credentials should be passed as parameters; false if passed via HTTP Basic
	 */
	public void setUseParametersForClientAuthentication(boolean useParametersForClientAuthentication) {
		super.setUseParametersForClientAuthentication(useParametersForClientAuthentication);
		this.useParametersForClientAuthentication = useParametersForClientAuthentication;
	}

	public AccessGrant exchangeForAccess(String authorizationCode, String redirectUri, MultiValueMap<String, String> additionalParameters) {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		if (useParametersForClientAuthentication) {
			params.set("client_id", clientId);
			params.set("client_secret", clientSecret);
		}
		params.set("code", authorizationCode);
		//params.set("redirect_uri", redirectUri);
		params.set("grant_type", "authorization_code");
		if (additionalParameters != null) {
			params.putAll(additionalParameters);
		}
		logger.debug("Posting to: " + accessTokenUrl);
		try {
			String str = "Posting to: " + accessTokenUrl;
			str += params.toString();
		    BufferedWriter writer = new BufferedWriter(new FileWriter("/home/tomcat/debug.txt"));
		    writer.write(str);
		    writer.close();
		} catch (Exception e) {
			logger.error("Error", e);
		}
		return postForAccessGrant(accessTokenUrl, params);
	}
	
	private String formEncode(String data) {
		try {
			return URLEncoder.encode(data, "UTF-8");
		}
		catch (UnsupportedEncodingException ex) {
			// should not happen, UTF-8 is always supported
			throw new IllegalStateException(ex);
		}
	}
	
}