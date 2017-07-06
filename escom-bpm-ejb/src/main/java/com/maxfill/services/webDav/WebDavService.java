package com.maxfill.services.webDav;

import com.maxfill.model.attaches.Attaches;

public interface WebDavService {
    void uploadFile(Attaches attache);
    void downloadFile(Attaches attache);
}
