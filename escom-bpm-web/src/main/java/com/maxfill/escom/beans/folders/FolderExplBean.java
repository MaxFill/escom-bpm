package com.maxfill.escom.beans.folders;

import com.maxfill.escom.beans.explorer.ExplorerBean;
import com.maxfill.model.BaseDict;
import com.maxfill.model.folders.Folder;
import org.apache.commons.lang3.StringUtils;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

/* Расширение контролёра обозревателя архива */
@Named
@ViewScoped
public class FolderExplBean extends ExplorerBean{
    private static final long serialVersionUID = 1718197265045722509L;

    /* Расширение для поиска в дереве папок по индексу дела */
    @Override
    protected boolean extTreeSearche(String searche, BaseDict item){
        Folder folder = (Folder) item;
        if (StringUtils.isBlank(folder.getFolderNumber())) return false;
        String folderNumber = folder.getFolderFullNumber();
        return folderNumber.equalsIgnoreCase(searche);
    }
}