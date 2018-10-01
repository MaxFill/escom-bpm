package com.maxfill.escom.system.servlets;

import com.maxfill.Configuration;
import com.maxfill.model.attaches.Attaches;
import com.maxfill.model.basedict.doc.DocFacade;
import com.maxfill.model.basedict.folder.FoldersFacade;
import com.maxfill.model.basedict.user.UserFacade;
import com.maxfill.model.basedict.doc.Doc;
import com.maxfill.model.basedict.folder.Folder;
import com.maxfill.model.basedict.user.User;
import com.maxfill.services.attaches.AttacheService;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

/**
 * Загрузка файлов на сервер
 * @author Maxim
 */
@WebServlet("/upload")
public class FileUploadServlet extends HttpServlet {
    private static final long serialVersionUID = 1224672188334273288L;
    protected static final Logger LOGGER = Logger.getLogger(FileUploadServlet.class.getName());
    
    @EJB 
    private UserFacade userFacade;
    @EJB
    private Configuration conf;
    @EJB
    private AttacheService attacheService;
    @EJB
    private DocFacade docFacade;
    @EJB
    private FoldersFacade folderFacade;
    
    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if (!isMultipart) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        DiskFileItemFactory factory = new DiskFileItemFactory();
        factory.setSizeThreshold(conf.getMaxFileSize()/10);
        File tempDir = (File)getServletContext().getAttribute("javax.servlet.context.tempdir");
        factory.setRepository(tempDir);
        ServletFileUpload upload = new ServletFileUpload(factory);
        upload.setSizeMax(conf.getMaxFileSize()); // 1024 * 1024 * 10
        upload.setHeaderEncoding("UTF-8"); 
        try {
            List items = upload.parseRequest(request);
            Iterator iter = items.iterator();
            String token = "";
            String fileName = "";
            Folder folder = null;
            while (iter.hasNext()) {
                FileItem item = (FileItem) iter.next();

                if (item.isFormField()) {                    
                    switch(item.getFieldName()){
                        case "token":{
                            token = item.getString();
                            break;
                        }
                        case "folder":{
                            Integer folderId = Integer.valueOf(item.getString());
                            folder = folderFacade.find(folderId);
                            break;
                        }
                        case "fileName":{
                            fileName = item.getString();
                            break;
                        }
                    }
                } else {
                    if (folder == null){
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        return;
                    }
                    
                    User author = userFacade.tokenCorrect(token);
                    if (author != null){
                        int hsr = processMakeDocument(item, fileName, folder, author);
                        response.setStatus(hsr);                    
                    } else {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        return;
                    }                            
                }
            }	                        
        } catch (FileUploadException | IOException e) {
            LOGGER.log(Level.SEVERE, null, e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    /* создание документа и загрузка файла */
    private int processMakeDocument(FileItem item, String fileName, Folder folder, User author) throws IOException{       
        //проверка на наличие прав доступа
        if (!folderFacade.checkRightAddDetail(folder, author)){
            return HttpServletResponse.SC_NO_CONTENT;
        }
        
        //проверка на наличие дубликата документа в папке
        if (folder.getDetailItems().stream().filter(doc->Objects.equals(doc.getName(), fileName)).findFirst().orElse(null) != null){
            return HttpServletResponse.SC_CONFLICT;
        }        
        
        Doc doc = docFacade.createDocInUserFolder(fileName, author, folder);        
        if (doc == null){ //если документ не создался
            return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        }        
        Map<String, Object> params = new HashMap<>();
        params.put("contentType", item.getContentType());
        params.put("fileName", fileName);
        params.put("size", item.getSize());
        params.put("author", author);
        params.put("doc", doc);
        attacheService.uploadAtache(params, item.getInputStream());
        return HttpServletResponse.SC_OK;
    }
    
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet FileUploadServlet</title>");            
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet FileUploadServlet at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("doGet!");
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
