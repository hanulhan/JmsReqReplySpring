/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hanulhan.jms.spring.reqreply.basic;

import java.io.IOException;
import java.util.StringTokenizer;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.apache.commons.codec.binary.Base64;

/**
 *
 * @author uhansen
 */
public class APIUserBasicAuthFilter implements Filter {

    private final static Logger LOGGER = Logger.getLogger(APIUserBasicAuthFilter.class.getName());
    private final static String REALM = "Protected";
    private String username, password;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        WebApplicationContext springContext = WebApplicationContextUtils.getWebApplicationContext(filterConfig.getServletContext());
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest myRequest = (HttpServletRequest) request;
        HttpServletResponse myResponse = (HttpServletResponse) response;

        String authHeader = myRequest.getHeader("Authorization");
        if (authHeader != null) {
            StringTokenizer st = new StringTokenizer(authHeader);
            if (st.hasMoreTokens()) {
                String basic = st.nextToken();

                if (basic.equalsIgnoreCase("Basic")) {
                    try {
                        String myToken= st.nextToken();
                        String credentials = new String(Base64.decodeBase64(myToken), "UTF-8");
                        LOGGER.trace("Credentials: " + credentials);
                        int p = credentials.indexOf(":");
                        if (p != -1) {
                            String myUsername = credentials.substring(0, p).trim();
                            String myPassword = credentials.substring(p + 1).trim();
                            if (myUsername.equals(username) && myPassword.equals(password))  {
                                chain.doFilter(request, response);
                                return;
                            }
                        }
                    } catch (IOException | ServletException e) {
                        LOGGER.error("Couldn't retrieve authentication", e);
                    }
                }
            }
        }

        unauthorized(myResponse);
    }

    private void unauthorized(HttpServletResponse response, String message) throws IOException {
        response.setHeader("WWW-Authenticate", "Basic realm=\"" + REALM + "\"");
        response.sendError(401, message);
    }

    private void unauthorized(HttpServletResponse response) throws IOException {
        unauthorized(response, "Unauthorized");
    }

    @Override
    public void destroy() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
