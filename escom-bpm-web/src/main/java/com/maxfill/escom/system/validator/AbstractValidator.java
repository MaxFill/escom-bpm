package com.maxfill.escom.system.validator;

import java.util.Map;
import java.util.ResourceBundle;

import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;

import com.maxfill.dictionary.DictBundles;
import com.maxfill.escom.utils.EscomMsgUtils;
import org.primefaces.validate.ClientValidator;

public abstract class AbstractValidator implements Validator, ClientValidator {

    @Override
    public Map<String, Object> getMetadata() {
        return null;
    }
}