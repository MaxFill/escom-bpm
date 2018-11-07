package com.maxfill.services.numerators.doc;

import com.maxfill.model.basedict.doc.Doc;
import com.maxfill.services.numerators.NumeratorService;
import com.maxfill.utils.Tuple;
import java.util.Set;

public interface DocNumeratorService extends NumeratorService{
    void registratedDoc(Doc doc, Set<Tuple> errors);
}
