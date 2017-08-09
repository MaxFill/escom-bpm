package com.maxfill.services.webDav;

import com.maxfill.model.attaches.Attaches;
import com.maxfill.model.users.User;

public interface WebDavService {
    void uploadFile(Attaches attache);
    void downloadFile(Attaches attache);
}
