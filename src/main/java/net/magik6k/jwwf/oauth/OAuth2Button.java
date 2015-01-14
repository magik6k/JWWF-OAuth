package net.magik6k.jwwf.oauth;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;

import net.magik6k.jwwf.handlers.ClickHandler;
import net.magik6k.jwwf.handlers.UserDataHandler;
import net.magik6k.jwwf.util.Json;
import net.magik6k.jwwf.widgets.basic.input.Button;

public class OAuth2Button extends Button{
	private final OAuth2Button thisButton;
	private String accessToken;
	private final Ref<OAuthHandler> authHandler = new Ref<>();
	private final String clientID;
	private final String clientSecret;
	private final String tokenExchangeUrl;
	
	
	public OAuth2Button(final String label, final String clientID, final String clientSecret, final String authUrl, final String scopes, final String redirectUrl, final String tokenExchangeUrl, OAuthHandler _authHandler) {
		super(label);
		thisButton = this;
		this.clientID = clientID;
		this.clientSecret = clientSecret;
		this.tokenExchangeUrl = tokenExchangeUrl;		
		
		super.setHandler(new ClickHandler() {
			
			@Override
			public void clicked() {
				thisButton.user.getUserData().get("__jwwf.oauth.token", new UserDataHandler() {
					
					@Override
					public void data(String key, String value) throws Exception {
						if(value.equals("")){
							thisButton.user.getUserData().get("__jwwf.oauth.code", new UserDataHandler() {
								
								@Override
								public void data(String codeKey, String codeValue) throws Exception {
									if(codeValue.equals("")){
										sendUserAuthorization(authUrl, clientID, redirectUrl, scopes);
									}else{
										doTokenExchange(tokenExchangeUrl, codeValue, clientID, clientSecret);
									}
								}
							});
						}else{
							accessToken = value;
							authHandler.ref.authorized(accessToken);
						}
					}
				});
			}
		});
		this.authHandler.ref = _authHandler;
		
	}
	
	private void doTokenExchange(String tokenExchangeUrl, String code, String clientID, String clientSecret){
		HttpURLConnection connection;
		try {
			connection = (HttpURLConnection) new URL(tokenExchangeUrl).openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("User-Agent", "JWWF-OAuth/2.0");
			connection.setDoOutput(true);
			
			DataOutputStream writer = new DataOutputStream(connection.getOutputStream());
			writer.writeBytes("code="+code+"&client_id="+clientID+"&client_secret="+clientSecret);
			writer.flush();
			writer.close();
			
			connection.getResponseCode();
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line;
			StringBuilder response = new StringBuilder();
			while ((line = reader.readLine()) != null) {
				response.append(line);
			}
			HashMap<String, String> values = new HashMap<>();
			String[] pairs = response.toString().split("\\&");
			for (int i = 0; i < pairs.length; i++) {
				String[] fields = pairs[i].split("=");
				if(fields.length < 2)
					continue;
				String name = URLDecoder.decode(fields[0], "UTF-8");
				String value = URLDecoder.decode(fields[1], "UTF-8");
				values.put(name, value);
			}
			if(values.get("access_token") != null){
				accessToken = values.get("access_token");
				user.getUserData().set("__jwwf.oauth.token", accessToken);
				
				authHandler.ref.authorized(accessToken);
			}else{
				user.getUserData().set("__jwwf.oauth.code","");
			}
			
			
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
	}
	
	//Redirect user for temporary code
	private void sendUserAuthorization(String authUrl, String clientID, String redirectUrl, String scopes){
		try {
			this.user.sendGlobal("JWWFOauth-go", "{\"url\":"+Json.escapeString(authUrl + "?client_id=" + clientID + 
					"&redirect_uri=" + URLEncoder.encode(redirectUrl, "UTF-8") + "&scope=" + URLEncoder.encode(scopes, "UTF-8"))+"}");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void onAttach() {
		super.onAttach();
		
		thisButton.user.getUserData().get("__jwwf.oauth.token", new UserDataHandler() {
			
			@Override
			public void data(String key, String value) throws Exception {
				if(value.equals("")){
					thisButton.user.getUserData().get("__jwwf.oauth.code", new UserDataHandler() {
						
						@Override
						public void data(String codeKey, String codeValue) throws Exception {
							if(!codeValue.equals("")){
								doTokenExchange(tokenExchangeUrl, codeValue, clientID, clientSecret);
							}
						}
					});
				}else{
					accessToken = value;
					authHandler.ref.authorized(accessToken);
				}
			}
		});
	}
	
	/**
	 * This method indicates state of authorization
	 * @return null is user is unauthenticated, access token otherwise.
	 */
	public String getAuthorizationStatus(){
		return accessToken;
	}
	
	@Override
	@Deprecated
	public final Button setHandler(ClickHandler clickHandler) {
		throw new IllegalStateException("Cannot set click handler of this button");
	}
	
	private static class Ref<T>{
		public T ref;
	}
	
}
