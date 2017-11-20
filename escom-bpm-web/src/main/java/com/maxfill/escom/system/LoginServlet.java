package com.maxfill.escom.system;

import com.google.gson.Gson;
import com.maxfill.facade.UserFacade;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;

public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = -8193588474462871260L;
    protected static final Logger LOGGER = Logger.getLogger(LoginServlet.class.getName());
    
    @EJB 
    private UserFacade userFacade;
        
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)  throws ServletException, IOException {
        BufferedReader reader = request.getReader();
        Gson gson = new Gson();

        Map<String, String> loginMap = gson.fromJson(reader, Map.class);
        
        String json = userFacade.makeJsonToken(loginMap);
        
        if (StringUtils.isBlank(json)){
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.write(json);
        }                        
    }        
        
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet LoginServlet</title>");            
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet LoginServlet at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
