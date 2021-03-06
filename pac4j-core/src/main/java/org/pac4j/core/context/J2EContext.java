package org.pac4j.core.context;

import org.pac4j.core.context.session.J2ESessionStore;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.exception.TechnicalException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * This implementation uses the J2E request, response and session.
 *
 * @author Jerome Leleu
 * @since 1.4.0
 */
public class J2EContext implements WebContext {

    private final HttpServletRequest request;

    private final HttpServletResponse response;

    private final SessionStore<J2EContext> sessionStore;

    /**
     * Build a J2E context from the current HTTP request and response.
     *
     * @param request the current request
     * @param response the current response
     */
    public J2EContext(final HttpServletRequest request, final HttpServletResponse response) {
        this(request, response, null);
    }

    /**
     * Build a J2E context from the current HTTP request and response.
     *
     * @param request the current request
     * @param response the current response
     * @param sessionStore the session store to use
     */
    public J2EContext(final HttpServletRequest request, final HttpServletResponse response, final SessionStore<J2EContext> sessionStore) {
        this.request = request;
        this.response = response;
        if (sessionStore == null) {
            this.sessionStore = new J2ESessionStore();
        } else {
            this.sessionStore = sessionStore;
        }
    }

    @Override
    public String getRequestParameter(final String name) {
        return this.request.getParameter(name);
    }

    @Override
    public Object getRequestAttribute(final String name) { return this.request.getAttribute(name); }

    @Override
    public void setRequestAttribute(final String name, final Object value) { this.request.setAttribute(name, value); }

    @Override
    public Map<String, String[]> getRequestParameters() {
        return this.request.getParameterMap();
    }

    @Override
    public String getRequestHeader(final String name) {
        return this.request.getHeader(name);
    }

    @Override
    public void setSessionAttribute(final String name, final Object value) {
        sessionStore.set(this, name, value);
    }

    @Override
    public Object getSessionAttribute(final String name) {
        return sessionStore.get(this, name);
    }

    @Override
    public Object getSessionIdentifier() {
        return sessionStore.getOrCreateSessionId(this);
    }

    @Override
    public String getRequestMethod() {
        return this.request.getMethod();
    }

    @Override
    public String getRemoteAddr() { return this.request.getRemoteAddr(); }

    /**
     * Return the HTTP request.
     *
     * @return the HTTP request
     */
    public HttpServletRequest getRequest() {
        return this.request;
    }

    /**
     * Return the HTTP response.
     *
     * @return the HTTP response
     */
    public HttpServletResponse getResponse() {
        return this.response;
    }

    public SessionStore<J2EContext> getSessionStore() {
        return sessionStore;
    }

    @Override
    public void writeResponseContent(final String content) {
        if (content != null) {
            try {
                this.response.getWriter().write(content);
            } catch (final IOException e) {
                throw new TechnicalException(e);
            }
        }
    }

    @Override
    public void setResponseStatus(final int code) {
        if (code == HttpConstants.OK || code == HttpConstants.TEMP_REDIRECT) {
            this.response.setStatus(code);
        } else {
            try {
                this.response.sendError(code);
            } catch (final IOException e) {
                throw new TechnicalException(e);
            }
        }
    }

    @Override
    public void setResponseHeader(final String name, final String value) {
        this.response.setHeader(name, value);
    }

    @Override
    public void setResponseContentType(final String content) {
        this.response.setContentType(content);
    }

    @Override
    public String getServerName() {
        return this.request.getServerName();
    }

    @Override
    public int getServerPort() {
        return this.request.getServerPort();
    }

    @Override
    public String getScheme() {
        return this.request.getScheme();
    }

    @Override
    public boolean isSecure() { return this.request.isSecure(); }

    @Override
    public String getFullRequestURL() {
        StringBuffer requestURL = request.getRequestURL();
        String queryString = request.getQueryString();
        if (queryString == null) {
            return requestURL.toString();
        } else {
            return requestURL.append('?').append(queryString).toString();
        }
    }

    @Override
    public Collection<Cookie> getRequestCookies() {
        final Collection<Cookie> pac4jCookies = new LinkedHashSet<>();
        final javax.servlet.http.Cookie[] cookies = this.request.getCookies();

        if (cookies != null) {
	        for (javax.servlet.http.Cookie c : cookies) {
	            final Cookie cookie = new Cookie(c.getName(), c.getValue());
	            cookie.setComment(c.getComment());
	            cookie.setDomain(c.getDomain());
	            cookie.setHttpOnly(c.isHttpOnly());
	            cookie.setMaxAge(c.getMaxAge());
	            cookie.setPath(c.getPath());
	            cookie.setSecure(c.getSecure());
	            pac4jCookies.add(cookie);
	        }
        }
        return pac4jCookies;
    }

    @Override
    public void addResponseCookie(final Cookie cookie) {
        javax.servlet.http.Cookie c = new javax.servlet.http.Cookie(cookie.getName(), cookie.getValue());
        c.setSecure(cookie.isSecure());
        c.setPath(cookie.getPath());
        c.setMaxAge(cookie.getMaxAge());
        c.setHttpOnly(cookie.isHttpOnly());
        c.setComment(cookie.getComment());
        c.setDomain(cookie.getDomain());
        this.response.addCookie(c);
    }

    @Override
    public String getPath() {
        return request.getServletPath();
    }
}
