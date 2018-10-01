package com.maxfill.services.licenses;

import com.maxfill.model.core.licence.Licence;

public interface ActivateApp{
    boolean activate(String licNumber, String sourceStr);
    Licence initLicense();
    String makeKeyInfo();
}