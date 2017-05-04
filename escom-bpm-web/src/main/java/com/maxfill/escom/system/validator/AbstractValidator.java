package com.maxfill.escom.system.validator;

import java.util.Map;
import java.util.ResourceBundle;

import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;

import org.primefaces.validate.ClientValidator;

public abstract class AbstractValidator implements Validator, ClientValidator {

    private static final String VALIDATOR_BUNDLE = "validator";

    protected ResourceBundle getResourceBundle(FacesContext context) {
        return context.getApplication().getResourceBundle(context, VALIDATOR_BUNDLE);
    }

    protected String getMessageFromBundle(FacesContext context, String key) {
        return getResourceBundle(context).getString(key);
    }

    @Override
    public Map<String, Object> getMetadata() {
        return null;
    }
}