package com.maxfill.model.basedict.doc.numerator;

import com.maxfill.model.basedict.doc.Doc;
import com.maxfill.services.numerators.NumeratorService;
import com.maxfill.utils.Tuple;
import java.util.Set;

public interface DocNumerator extends NumeratorService{
    void registratedDoc(Doc doc, Set<Tuple> errors);
}
