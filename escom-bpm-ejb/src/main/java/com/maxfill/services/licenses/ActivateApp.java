package com.maxfill.services.licenses;

import com.maxfill.model.licence.Licence;

public interface ActivateApp{
    boolean activate(String sourceStr);
    Licence initLicense();
}