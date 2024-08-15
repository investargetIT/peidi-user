package com.cyanrocks.boilerplate.security.authentication.password;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Author wjq
 * @Date 2024/8/8 16:17
 */
public class UsernamePasswordAuthFilter extends AbstractAuthenticationProcessingFilter {

    private static final Logger LOG = LoggerFactory.getLogger(UsernamePasswordAuthFilter.class);

    private String usernameParameter = "username";
    private String passwordParameter = "password";
    private boolean postOnly = true;

    // ~ Constructors
    // ===================================================================================================

    public UsernamePasswordAuthFilter(String processingUrl) {
        super(new AntPathRequestMatcher(processingUrl, "POST"));
    }

    // ~ Methods
    // ========================================================================================================

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
        throws AuthenticationException {
        if (postOnly && !request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        }
        String username = request.getParameter(usernameParameter);
        String password = request.getParameter(passwordParameter);
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            throw new UsernameNotFoundException("username or password is null");
        }
        try {
            String service = request.getParameter("service");
            UsernamePasswordAuthToken authRequest = new UsernamePasswordAuthToken(username+"&"+service, password);

            // Allow subclasses to set the "details" property
            setDetails(request, authRequest);
            return this.getAuthenticationManager().authenticate(authRequest);
        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalAuthenticationServiceException("attemptAuthentication error", e);
        }
    }

    protected void setDetails(HttpServletRequest request, UsernamePasswordAuthToken authRequest) {
        authRequest.setDetails(authenticationDetailsSource.buildDetails(request));
    }

    public boolean isPostOnly() {
        return postOnly;
    }

    public void setPostOnly(boolean postOnly) {
        this.postOnly = postOnly;
    }
}
