
package com.maxfill.model.numPuttern;

import com.maxfill.model.BaseDataModel;
import com.maxfill.dictionary.DictNumerator;
import com.maxfill.utils.ItemUtils;
import java.util.ArrayList;
import java.util.List;
import javax.faces.model.SelectItem;

/**
 *
 * @author mfilatov
 */
public class NumeratorModel extends BaseDataModel{    
    private static final long serialVersionUID = 7047647421152880439L;
    
    private final List<SelectItem> numPatternTypes = new ArrayList<>();
    
    public NumeratorModel() {
        numPatternTypes.add(new SelectItem(DictNumerator.TYPE_AUTO, ItemUtils.getBandleLabel("Auto")));
        numPatternTypes.add(new SelectItem(DictNumerator.TYPE_MANUAL, ItemUtils.getBandleLabel("ManualInput")));
    }
    
    public String getLabel(String typeCode){
        for (SelectItem item : numPatternTypes){
            if (item.getValue().equals(typeCode)){
                return item.getLabel();
            }
        }
        return "";
    }
}
