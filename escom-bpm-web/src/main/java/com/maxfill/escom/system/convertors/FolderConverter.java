package com.maxfill.escom.system.convertors;

import com.maxfill.model.folders.Folder;
import javax.faces.convert.FacesConverter;

@FacesConverter("folderConvertor")
public class FolderConverter extends BaseBeanConvertor<Folder>{

    @Override
    protected String getBeanName() {
        return "foldersBean";
    }
}