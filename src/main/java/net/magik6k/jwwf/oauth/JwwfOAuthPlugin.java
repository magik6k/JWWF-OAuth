package net.magik6k.jwwf.oauth;

import java.io.IOException;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import net.magik6k.jwwf.core.JwwfPlugin;
import net.magik6k.jwwf.core.JwwfServer;
import net.magik6k.jwwf.core.plugin.IPluginGlobal;

public class JwwfOAuthPlugin extends JwwfPlugin implements IPluginGlobal{

	@Override
	public void onAttach(JwwfServer server) {
		server.bindServlet(new OAuth2UserAuthServlet(), "/__jwwf/oauth/*");
		try {
			server.getCreator().appendHead("<script>"+Resources.toString(
					Resources.getResource("net/magik6k/jwwf/oauth/OauthHandler.js"), Charsets.UTF_8)+"</script>");
		} catch (IOException e) {
			e.printStackTrace();
		}
	
		
	}
	
}
