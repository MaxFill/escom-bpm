package com.maxfill.escom.system.scaner;

import java.util.HashMap;
import java.util.Map;

public class DWTwainHelper {

    public static final String DWTWAIN_IMAGETYPE_PREFIX = "IT_";
    //public static final String JNDI_PRODUCT_KEY = "java:global/dwtwain-productkey";
      
    public static final String JNDI_PRODUCT_KEY = "CA4D8649BBE1EA5E0DB16C0095592DD2929FDFE7F0DF842BDD742D67E67F2588929FDFE7F0DF842BD7A5E9F0131F2E20929FDFE7F0DF842BD72015550FC59FEB929FDFE7F0DF842BAE94A6ED6A10AF18929FDFE7F0DF842B80A7129EC443FE8D929FDFE7F0DF842BF14CF6C02766F343929FDFE7F0DF842BCF5BDB740A8C25C5929FDFE7F0DF842B56571B25C75DCA2A929FDFE7F0DF842B02004EB5419F0EEA929FDFE7F0DF842B2873F8E1DF5E1A78929FDFE7F0DF842B52E6A8463991B52A929FDFE7F0DF842B810CBE906B1E5691929FDFE7F0DF842B264F1A9FC65F1BF8929FDFE7F0DF842BB756CCF43BACAE53E0000000";
    private Integer maxFileSize;
    private String productKey;
    private Boolean isTrial;

    private final Map<String, String> scanFileTypesLabel = new HashMap<String, String>();

    private enum ScanFileType {
        BMP,
        JPG,
        TIF,
        PNG,
        PDF
    };

    public DWTwainHelper(int maxFileSize) {
        this.maxFileSize = maxFileSize;
        productKey = JNDI_PRODUCT_KEY;
        isTrial = true;

        for (ScanFileType type : ScanFileType.values()) {
            String value = DWTWAIN_IMAGETYPE_PREFIX + type.name(), label = type.name().toLowerCase();
            scanFileTypesLabel.put(value, label);
        }
    }

    public Integer getMaxFileSize() {
        return maxFileSize;
    }

    public String getProductKey() {
        return productKey;
    }

    public Boolean getIsTrial() {
        return isTrial;
    }

    public Map<String, String> getScanFileTypesLabels() {
        return scanFileTypesLabel;
    }

    public Map<String, String> getScanFileTypesLabel() {
        return scanFileTypesLabel;
    }

    public void setMaxFileSize(Integer maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    public void setProductKey(String productKey) {
        this.productKey = productKey;
    }

    public void setIsTrial(Boolean isTrial) {
        this.isTrial = isTrial;
    }
}
