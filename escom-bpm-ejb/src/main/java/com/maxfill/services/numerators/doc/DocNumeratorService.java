package com.maxfill.services.numerators.doc;

import com.maxfill.model.docs.Doc;
import com.maxfill.services.numerators.NumeratorService;
import java.util.Set;

public interface DocNumeratorService extends NumeratorService{
    void registratedDoc(Doc doc, Set<String> errors);
}
