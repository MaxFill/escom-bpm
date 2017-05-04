package com.maxfill.escom.system.scaner;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import javax.faces.model.SelectItem;
import org.apache.commons.lang3.StringUtils;

public class ScanViewModel implements Serializable {
    private static final long serialVersionUID = -820159450980113421L;

    private String fileExtensionValue;
    private List<SelectItem> fileExtensions = new ArrayList<>();
    private byte[] data;
    private String fileName;
    
    public ScanViewModel(Map<String, String> scanFileTypesLabel) {
        for (String value : scanFileTypesLabel.keySet()) {
            fileExtensions.add(new SelectItem(value, scanFileTypesLabel.get(value)));
        }
    }

    public List<SelectItem> getFileExtensions() {
        return fileExtensions;
    }

    public void setFileExtensions(List<SelectItem> fileExtensions) {
        this.fileExtensions = fileExtensions;
    }

    public String getImageStringBase64() {
        return "";
    }

    public void setImageStringBase64(String imageStringBase64) {
        if (StringUtils.isEmpty(imageStringBase64)) {
            setData(null);
            return;
        }
        byte[] base64Bytes = imageStringBase64.getBytes();
        byte[] bytes = Base64.getDecoder().decode(base64Bytes);
        setData(bytes);
    }

    public String getFileExtensionValue() {
        return fileExtensionValue;
    }

    public void setFileExtensionValue(String fileExtensionValue) {
        this.fileExtensionValue = fileExtensionValue;
    }

    public String getFullFileName() {
        return getFileName() + "." + fileExtensionValue.replace(DWTwainHelper.DWTWAIN_IMAGETYPE_PREFIX, "");
    }
    
    public byte[] getData() {
        return data;
    }
    public void setData(byte[] data) {
        this.data = data;
    }
    
    public String getFileName() {
        return fileName;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}