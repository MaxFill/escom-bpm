package com.maxfill.escom.system.servlets;

import com.google.gson.Gson;
import com.maxfill.model.folders.FoldersFacade;
import com.maxfill.model.users.UserFacade;
import com.maxfill.model.folders.Folder;
import com.maxfill.model.users.User;
import org.apache.commons.lang3.StringUtils;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@WebServlet("/folders")
public class FoldersServlet extends HttpServlet{
    private static final long serialVersionUID = -8280492193068871963L;
    protected static final Logger LOGGER = Logger.getLogger(FoldersServlet.class.getName());

    @EJB
    private FoldersFacade foldersFacade;
    @EJB
    private UserFacade userFacade;

    /**
     * Запрос на получение ветки дерева
     * @param request
     * @param response
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)  throws ServletException, IOException {
        BufferedReader reader = request.getReader();
        Gson gson = new Gson();

        Map<String, String> params = gson.fromJson(reader, Map.class);

        User user = userFacade.tokenCorrect(params.get("token"));
        if (user == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String sParentId = params.get("parent");
        if (StringUtils.isBlank(sParentId)){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        try {
            Integer parentId = Integer.parseInt(sParentId);
            Folder parent = foldersFacade.find(parentId);            

            List<FolderForTransfer> transfer = foldersFacade.findActualChilds(parent, user)
                    .map(f-> new FolderForTransfer(f.getId(), f.getName(), !foldersFacade.checkRightAddDetail(f, user)))
                    .collect(Collectors.toList());

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            try (PrintWriter out = response.getWriter()) {
                String jsonOut = gson.toJson(transfer, List.class);
                out.write(jsonOut);
            }
        } catch (Exception ex){
            LOGGER.log(Level.SEVERE, null, ex);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    /**
     * Служебный класс для выгрузки информации о папках
     */
    private class FolderForTransfer{
        public Integer id;
        public String name;
        public boolean readOnly;

        public FolderForTransfer(Integer id, String name, boolean readOnly) {
            this.id = id;
            this.name = name;
            this.readOnly = readOnly;
        }
    }
}
