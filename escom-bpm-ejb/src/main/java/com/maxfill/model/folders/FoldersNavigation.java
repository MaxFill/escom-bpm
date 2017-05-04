
package com.maxfill.model.folders;

import com.maxfill.model.BaseDict;
import java.io.Serializable;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author mfilatov
 */
public class FoldersNavigation implements Serializable{
    private static final long serialVersionUID = -6899928486498837884L;

    private BaseDict folder;
    private String name; 
    private String label;

    public FoldersNavigation(BaseDict folder) {
        this.folder = folder;
        this.name = StringUtils.abbreviate(folder.getName(), 25);
        this.label = folder.getName();
    }
        
    /* *** Get & Set *** */
     
    public String getLabel() {
        return label;
    }
    public void setLabel(String label) {
        this.label = label;
    }

    public BaseDict getFolder() { return folder; }
    public void setFolder(BaseDict folder) { this.folder = folder;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    
    
}
