package com.maxfill.services.licenses;

import com.maxfill.model.licence.Licence;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OperatingSystem;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.ejb.Stateless;
import javax.xml.bind.JAXB;
import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
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
    public boolean activate(String licNumber, String sourceStr) {
        Boolean result = false;
        String serverDir = System.getProperty("server-dir");
        File licenseFile = new File(serverDir + File.separator + licNumber + ".lic");
        try {
            FileOutputStream outputStream = new FileOutputStream(licenseFile);
            outputStream.write(sourceStr.getBytes("UTF-8"));
            outputStream.close();
            result = true;
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return result;
    }

    /**
     * Инициализация лицензии из лицензионного файла
     * @return 
     */
    @Override
    public Licence initLicense(){
        File licenseFile = getLicenseFile();
        if (licenseFile == null) return null;
        Licence license = null;
        try {
            IvParameterSpec iv = new IvParameterSpec(makeIv(licenseFile));
            SecretKeySpec secretKey = new SecretKeySpec(makeKey(), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
            byte[] bytes = Files.readAllBytes(licenseFile.toPath());
            String sourceStr = new String(bytes, "UTF-8");
            byte[] encrypted = cipher.doFinal(Base64.getDecoder().decode(sourceStr));
            String resultXML = new String(encrypted, "UTF-8");
            license = JAXB.unmarshal(new StringReader(resultXML), Licence.class);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidAlgorithmParameterException
                | InvalidKeyException | BadPaddingException | IllegalBlockSizeException | IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return license;
    }

    private File getLicenseFile(){
        String serverDir = System.getProperty("server-dir");
        Path dir = FileSystems.getDefault().getPath(serverDir);
        File licenseFile = null;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.lic")) {
            for (Path path : stream) {
                licenseFile = path.toFile();
            }
        }
        catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return licenseFile;
    }

    /**
     * Получение вектора для расшифровки файла лицензии
     * @return
     * @throws UnsupportedEncodingException
     */
    private byte[] makeIv(File licenseFile) throws UnsupportedEncodingException{
        String fileName = licenseFile.getName();
        String licNumber = fileName.substring(0, fileName.lastIndexOf("."));
        return licNumber.getBytes("UTF-8");
    }

    /**
     * Формирует данные для секретного ключа из параметров сервера
     * @return
     */
    @Override
    public String makeKeyInfo() {
        SystemInfo systemInfo = new SystemInfo();
        OperatingSystem operatingSystem = systemInfo.getOperatingSystem();
        HardwareAbstractionLayer hardwareAbstractionLayer = systemInfo.getHardware();
        CentralProcessor centralProcessor = hardwareAbstractionLayer.getProcessor();

        String vendor = operatingSystem.getManufacturer();
        String processorSerialNumber = centralProcessor.getProcessorID();
        String processorIdentifier = centralProcessor.getIdentifier();
        String bios = systemInfo.getHardware().getComputerSystem().getBaseboard().getSerialNumber();
        HWDiskStore[] hwDiskStHw = hardwareAbstractionLayer.getDiskStores();

        int processors = centralProcessor.getLogicalProcessorCount();

        StringBuilder sb = new StringBuilder();
        sb.append("#").append(processorSerialNumber);
        sb.append("#").append(processorIdentifier);
        sb.append("#").append(processors);
        sb.append("#").append(vendor);
        sb.append("#").append(bios);
        if (hwDiskStHw.length >0){
            sb.append("#").append(hwDiskStHw[0].getSerial());
        }
        String result = sb.toString().replaceAll("\\s","");
        return result;
    }

    /**
     * Формирует секретный ключ
     * @return
     * @throws UnsupportedEncodingException
     */
    private byte[] makeKey() throws UnsupportedEncodingException{
        String keyInfo = makeKeyInfo().substring(0, 16);
        return keyInfo.getBytes("UTF-8");
    }
}