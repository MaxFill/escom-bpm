package com.maxfill.escom.system.convertors;

import com.maxfill.model.basedict.docType.DocType;
import javax.faces.convert.FacesConverter;

@FacesConverter("docsTypesConvertor")
public class DocTypeConverter extends BaseBeanConvertor<DocType>{

    @Override
    protected String getBeanName() {
        return "docTypeBean";
    }
}
