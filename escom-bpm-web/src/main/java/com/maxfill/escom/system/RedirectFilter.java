package com.maxfill.escom.system;

import com.maxfill.dictionary.SysParams;
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

public class RedirectFilter implements Filter {    
    private FilterConfig filterConfig = null;
    
    private static final String AJAX_REDIRECT_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
        + "<partial-response><redirect url=\"%s\"></redirect></partial-response>";
    
    public RedirectFilter() {
    }    
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {        
      // ServletContext servletContext = filterConfig.getServletContext();
      // serverURL = servletContext.getInitParameter("ServerURL");
    }
    
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
        response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
        response.setDateHeader("Expires", 0); // Proxies.
        
        HttpSession session = request.getSession(false);
        String userId = (session != null) ? (String) session.getAttribute("UserLogin") : null;
        String ctxPath = request.getContextPath();        

        String serverURL = new URL(request.getScheme(), request.getServerName(), request.getServerPort(), "").toString();
        String reqURL = request.getRequestURI();

        if (reqURL.contains(SysParams.PRIME_URL) || reqURL.contains(SysParams.RESOURCE_URL)){
            chain.doFilter(request, response);
            return;
        }

        if (reqURL.contains(SysParams.ACTIVATE_PAGE)){
            chain.doFilter(request, response);
            return; 
        }        
        
        if (reqURL.contains(SysParams.LOGIN_PAGE)){ 
            chain.doFilter(request, response);
            return;
        }
        
        if (reqURL.equals(ctxPath+"/")){          
            StringBuilder indexURL = new StringBuilder();
            indexURL.append(serverURL).append(ctxPath).append("/faces").append(SysParams.MAIN_PAGE);
            response.sendRedirect(indexURL.toString());
            return;   
        }
        
        if (reqURL.contains(SysParams.LOGOUT_PAGE)){
            chain.doFilter(request, response);
            return;   
        }        
         
        if (reqURL.contains(SysParams.ERROR_PAGE)){
            chain.doFilter(request, response);
            return;
        } 
        
        if (userId == null){ 
            if ("partial/ajax".equals(request.getHeader("Faces-Request"))){
                response.setContentType("text/xml");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().printf(AJAX_REDIRECT_XML, reqURL);
                chain.doFilter(request, response);
                return;
            } else {                
                String targetUrl = reqURL.replaceAll(ctxPath, "").replaceAll("/faces", "").replaceAll("/", "%2F");
                StringBuilder loginURL = new StringBuilder();
                loginURL.append(serverURL).append(ctxPath).append("/faces/");
                loginURL.append(SysParams.LOGIN_PAGE).append("?from=").append(targetUrl).append(makeParams(request.getParameterMap()));
                Map<String,String[]> params = request.getParameterMap();
                if (params.containsKey("docId")){
                    String[] param = params.get("docId");
                    loginURL.append("?docId=").append(param[0]);
                }                
                response.sendRedirect(loginURL.toString());                
                return;
            }
        }        
        
        try {
            chain.doFilter(request, response);
        } 
        catch (ServletException e) {
            if (e.getRootCause() instanceof ViewExpiredException) {
                String errorURL = serverURL + ctxPath + "/faces/" + SysParams.LOGIN_PAGE;
                response.sendRedirect(errorURL);
            } else {
                throw e;
            }
        }
    }
    
    private String makeParams(Map<String, String[]> paramMap){
        StringBuilder result = new StringBuilder();
        if (paramMap.containsKey("itemId")){
            String[] values = paramMap.get("itemId");
            result.append("?itemId=").append(values[0]);

            if (paramMap.containsKey("openMode")){
                values = paramMap.get("openMode");
                result.append("&openMode=").append(values[0]);
            }
        }    
        return result.toString();
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