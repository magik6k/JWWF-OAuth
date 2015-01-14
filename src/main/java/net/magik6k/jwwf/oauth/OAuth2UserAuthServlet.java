package net.magik6k.jwwf.oauth;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

class OAuth2UserAuthServlet extends HttpServlet{

	private static final long serialVersionUID = 4317954940204905503L;
	private static String code;
	
	static {
		try {
			code = Resources.toString(
					Resources.getResource("net/magik6k/jwwf/oauth/UserAuth.html"), Charsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		resp.setContentLength(code.length());
		resp.setContentType("text/html");
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.getWriter().append(code);
	}
}
