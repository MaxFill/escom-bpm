
package com.maxfill.escom.system;

import com.maxfill.utils.SysParams;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.Map;
import javax.faces.application.ViewExpiredException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author Filatov Maxim
 */

public class RedirectFilter implements Filter {    
    private FilterConfig filterConfig = null;
    
    public RedirectFilter() {
    }    
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {        
      // ServletContext servletContext = filterConfig.getServletContext();
      // serverURL = servletContext.getInitParameter("ServerURL");
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) req;
        HttpServletResponse httpServletResponse = (HttpServletResponse) res;
        HttpSession session = httpServletRequest.getSession(false);
        String userId = (session != null) ? (String) session.getAttribute("UserLogin") : null;
        String ctxPath = httpServletRequest.getContextPath();        

        String serverURL = new URL(httpServletRequest.getScheme(),
                               httpServletRequest.getServerName(),
                               httpServletRequest.getServerPort(), "").toString();

        String reqURL = httpServletRequest.getRequestURI();
        /**
         * Вызов для ресурсов
         */
        if (reqURL.contains(SysParams.PRIME_URL) || reqURL.contains(SysParams.RESOURCE_URL)){
            chain.doFilter(httpServletRequest, httpServletResponse);
            return; 
        }
        isSessionInvalid(httpServletRequest);
        /**
         * Проверка на то, что сессия истекла
         */
        /*
        if (!StringUtils.contains(reqURL, SysParams.EXPIRE_PAGE)) {
            if (isSessionInvalid(httpServletRequest)) {
                String timeoutUrl = serverURL + ctxPath + "/faces/" + SysParams.EXPIRE_PAGE;
                httpServletResponse.sendRedirect(timeoutUrl);
                return;
            }
        } else {
            chain.doFilter(httpServletRequest, httpServletResponse);
            return;   
        }
        */
        
        /**
         * Вызов для страницы входа
         */
        if (reqURL.contains(SysParams.LOGIN_PAGE)){
            chain.doFilter(httpServletRequest, httpServletResponse);
            return;
        }
        
        if (reqURL.equals(ctxPath+"/")){          
            StringBuilder indexURL = new StringBuilder();
            indexURL.append(serverURL).append(ctxPath).append("/faces").append(SysParams.MAIN_PAGE);
            httpServletResponse.sendRedirect(indexURL.toString());
            return;   
        }
        
        if (reqURL.contains(SysParams.LOGOUT_PAGE)){
            chain.doFilter(httpServletRequest, httpServletResponse);
            return;   
        }        
         
        if (reqURL.contains(SysParams.ERROR_PAGE)){
            chain.doFilter(httpServletRequest, httpServletResponse);
            return;
        } 
        
        if (userId == null){            
                String targetUrl = reqURL.replaceAll(ctxPath, "").replaceAll("/faces", "").replaceAll("/", "%2F");
                StringBuilder loginURL = new StringBuilder();
                loginURL.append(serverURL).append(ctxPath).append("/faces/");
                loginURL.append(SysParams.LOGIN_PAGE).append("?from=").append(targetUrl);
                Map<String,String[]> params = httpServletRequest.getParameterMap();
                if (params.containsKey("docId")){
                    String[] param = params.get("docId");
                    loginURL.append("?docId=").append(param[0]);
                }                
                httpServletResponse.sendRedirect(loginURL.toString());
                return;
        }
        
        try {
            chain.doFilter(httpServletRequest, httpServletResponse);
        } 
        catch (ServletException e) {
            if (e.getRootCause() instanceof ViewExpiredException) {
                String errorURL = serverURL + ctxPath + "/faces/" + SysParams.LOGIN_PAGE;
                httpServletResponse.sendRedirect(errorURL);
            } else {
                throw e;
            }
        }
    }

    private boolean isSessionInvalid(HttpServletRequest httpServletRequest) {
        boolean sessionInValid = httpServletRequest.getRequestedSessionId() != null
            && !httpServletRequest.isRequestedSessionIdValid();
        return sessionInValid;
    }
     
    public static String getStackTrace(Throwable t) {
        String stackTrace = null;
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            t.printStackTrace(pw);
            pw.close();
            sw.close();
            stackTrace = sw.getBuffer().toString();
        } catch (Exception ex) {
        }
        return stackTrace;
    }
    
    public void log(String msg) {
        filterConfig.getServletContext().log(msg);        
    }
    
    public FilterConfig getFilterConfig() {
        return (this.filterConfig);
    }

    public void setFilterConfig(FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
    }
        
    @Override
    public void destroy() {        
    }  
    
    @Override
    public String toString() {
        if (filterConfig == null) {
            return ("EM_RedirectFilter()");
        }
        StringBuffer sb = new StringBuffer("EM_RedirectFilter(");
        sb.append(filterConfig);
        sb.append(")");
        return (sb.toString());
    }
}