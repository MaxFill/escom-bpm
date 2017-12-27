package com.maxfill.escom.system.convertors;

import com.maxfill.model.docs.docsTypes.docTypeGroups.DocTypeGroups;
import javax.faces.convert.FacesConverter;

@FacesConverter("docTypeGroupConvertor")
public class DocTypeGroupConverter extends BaseBeanConvertor<DocTypeGroups>{

    @Override
    protected String getBeanName() {
        return "DocTypeGroupsBean";
    }
}
