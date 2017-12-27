package com.maxfill.escom.system.convertors;

import com.maxfill.model.docs.Doc;
import javax.faces.convert.FacesConverter;

@FacesConverter("docsConvertor")
public class DocConverter extends BaseBeanConvertor<Doc>{

    @Override
    protected String getBeanName() {
        return "docTypeBean";
    }
}
