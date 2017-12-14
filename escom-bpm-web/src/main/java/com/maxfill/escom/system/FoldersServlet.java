package com.maxfill.escom.system;

import javax.servlet.http.HttpServlet;

public class FoldersServlet extends HttpServlet{
    private static final long serialVersionUID = -8280492193068871963L;

    /**
     * Служебный класс для выгрузки информации о папках
     */
    private class Folder{
        public Integer id;
        public String name;
        public boolean readOnly;

        public Folder(Integer id, String name, boolean readOnly) {
            this.id = id;
            this.name = name;
            this.readOnly = readOnly;
        }
    }
}
