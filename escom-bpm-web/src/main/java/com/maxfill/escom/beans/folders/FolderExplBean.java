package com.maxfill.escom.beans.folders;

import com.maxfill.escom.beans.explorer.ExplorerBean;
import com.maxfill.model.BaseDict;
import com.maxfill.model.filters.Filter;
import com.maxfill.model.folders.Folder;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;

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

    @Override
    public void onAfterFormLoad(){
        if (getFilterId() != null) {
            Filter filter = filtersFacade.find(getFilterId());
            if (filter != null) {
                makeSelectedFilter(filter);
                PrimeFaces.current().ajax().update("explorer_west");
                PrimeFaces.current().ajax().update("explorer");
            }
        }
    }
}