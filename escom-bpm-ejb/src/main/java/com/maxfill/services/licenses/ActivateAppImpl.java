package com.maxfill.services.licenses;

import com.maxfill.model.licence.Licence;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.ejb.Stateless;
import javax.xml.bind.JAXB;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless
public class ActivateAppImpl implements ActivateApp{
    protected static final Logger LOGGER = Logger.getLogger(ActivateAppImpl.class.getName());

    @Override
    public boolean activate(String sourceStr) {
        Boolean result = false;
        String propertyFile = System.getProperty("license-info");
        File outputFile = new File(propertyFile);

        try {
            FileOutputStream outputStream = new FileOutputStream(outputFile);
            outputStream.write(sourceStr.getBytes("UTF-8"));
            outputStream.close();
            result = true;
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return result;
    }

    /**
     * Инициализация лицензии
     */
    public Licence initLicense(){
        String propertyFile = System.getProperty("license-info");
        File inputFile = new File(propertyFile);
        if (!Files.exists(inputFile.toPath())) return null;
        Licence license = null;
        try {
            IvParameterSpec iv = new IvParameterSpec("222-222-222.2222".getBytes("UTF-8"));
            SecretKeySpec secretKey = new SecretKeySpec("1234567890124345".getBytes("UTF-8"), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
            byte[] bytes = Files.readAllBytes(inputFile.toPath());
            String sourceStr = new String(bytes, "UTF-8");
            byte[] encrypted = cipher.doFinal(Base64.getDecoder().decode(sourceStr));
            String resultXML = new String(encrypted);
            license = JAXB.unmarshal(new StringReader(resultXML), Licence.class);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidAlgorithmParameterException
                | InvalidKeyException | BadPaddingException | IllegalBlockSizeException | IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return license;
    }
}
