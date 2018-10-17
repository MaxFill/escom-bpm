package com.maxfill.services.numerators.process;

import com.maxfill.model.basedict.process.Process;
import com.maxfill.services.numerators.NumeratorService;
import java.util.Set;

public interface ProcessNumeratorService extends NumeratorService{
    void registrate(Process process, Set<String> errors);
}
