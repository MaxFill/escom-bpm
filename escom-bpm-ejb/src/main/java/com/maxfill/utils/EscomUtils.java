package com.maxfill.utils;

import com.maxfill.model.BaseDict;
import com.maxfill.model.metadates.Metadates;
import com.maxfill.services.update.ReleaseInfo;
import com.maxfill.services.update.ReleaseInfo_Service;
import org.apache.commons.lang.StringUtils;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public final class EscomUtils {
    private static final Logger LOGGER = Logger.getLogger(EscomUtils.class.getName());             
    
    /**
     * Получение номера актуального релиза
     * не используется потому что к серверу по ssl не удалось подключиться. Нужно установить сертификат сервера, к которму коннектишься
     * в хранилище java. Это гемор, так как придётся после устаревания сертификата обновлять сертификат у всех клиентов.
     * Код оставлен для примера.
     * @param licenceNumber
     * @return 
     */
    public static String getReleaseInfo(String licenceNumber){
        String result= "";
        try { 
            ReleaseInfo_Service service = new ReleaseInfo_Service();
            ReleaseInfo port = service.getReleaseInfoPort();
            result = port.getReleaseInfo(licenceNumber);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);	
        }
        return result;
    }

    /* Преобразует List в строку с разделителем запятая */
    public static String listToString(List<String> ids){
        return StringUtils.join(ids, ",");
    }
    
    /* Разбивает строку на части */
    public static ArrayList<String> SplitString(String subject, String delimiters) {
        StringTokenizer strTkn = new StringTokenizer(subject, delimiters);
        ArrayList<String> arrLis = new ArrayList<>(subject.length());

        while(strTkn.hasMoreTokens())
           arrLis.add(strTkn.nextToken());

        return arrLis;
    }    

    /* Форматирует номер телефона для вывода только его части */
    public static String makeSecureFormatPhone(String phone){
        StringBuilder sb = new StringBuilder();
        sb.append(StringUtils.left(phone, 8)).append("*****").append(StringUtils.right(phone, 3));
        return sb.toString();
    }

    /* Убирает из номера телефона всё лишнее */
    public static String clearPhoneNumber(String phone){
        return phone.replaceAll("[^\\d]", "");
    }

    /* Преобразование строки в MD5 */
    public static String encryptPassword(String password) {
        String rezult = null;
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] bs;
            messageDigest.reset();
            bs = messageDigest.digest(password.getBytes()); 
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < bs.length; i++){
                String hexVal = Integer.toHexString(0xFF & bs[i]);
                if (hexVal.length() == 1){
                    stringBuilder.append("0");
                }
                stringBuilder.append(hexVal);
            }
            rezult = stringBuilder.toString();
        } catch (NoSuchAlgorithmException ex) {
            LOGGER.log(Level.SEVERE, null, ex);	
        }
        return rezult;
    }
    
    /* Выполняет преобразование unixTime в дату-строку */
    public static String IntToDateString(long unixTime){
        String newFormatDate = "---"; 
        if (unixTime != 0L) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.YY HH:mm");
            Date resultdate = new Date(unixTime * 1000);
            newFormatDate = sdf.format(resultdate);
        }
        return newFormatDate;  
    }

    /* Проверка корректности ИНН */
    private static final Pattern INN_PATTER = Pattern.compile("\\d{10}|\\d{12}");
    private static final int[] CHECK_ARR = new int[] {3,7,2,4,10,3,5,9,4,6,8};
    public static boolean isValidINN(String inn) {
        inn = inn.trim();
        if (!INN_PATTER.matcher(inn).matches()) {
            return false;
        }
        int length = inn.length();
        if (length == 12) {
            return INNStep(inn, 2, 1) && INNStep(inn, 1, 0);
        } else {
            return INNStep(inn, 1, 2);
        }
    }
    private static boolean INNStep(String inn, int offset, int arrOffset) {
        int sum = 0;
        int length = inn.length();
        for (int i = 0; i < length - offset; i++) {
            sum += (inn.charAt(i) - '0') * CHECK_ARR[i + arrOffset];
        }
        return (sum % 11) % 10 == inn.charAt(length - offset) - '0';
    }
    
    /* Возвращает два последних символа года YY  */
    public static String getYearYY(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        String year = String.valueOf(cal.get(Calendar.YEAR));
        String yy = year.substring(2, 4).toUpperCase();
        return yy;
    }
    
    /* Возвращает год как строку  */
    public static String getYearStr(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        String year = String.valueOf(cal.get(Calendar.YEAR));
        return year;
    }
    
    /* Формирует уникальный номер GUID */
    public static String generateGUID() {
        UUID uuid = UUID.randomUUID();
        String randomUUID = uuid.toString();
        return randomUUID;
    }
    
    /* формирование штрихкода */
    public static String getBarCode(BaseDict item, Metadates obj, Integer serverId){
        final int totalLenght = 12;        
        String first = serverId.toString() + obj.getId().toString();
        String id = item.getId().toString();
        Integer lenDigit = first.length() + id.length();
        Integer countZero = totalLenght - lenDigit;
        return first + StringUtils.leftPad(id, countZero, "0");
    }
    
    /* Копирует строку в буфер */
    public static void copyToClipboard(String sourceString){
        StringSelection selection = new StringSelection(sourceString);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);
    }
    
    public static byte[] compress(String data) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length());
        GZIPOutputStream gzip = new GZIPOutputStream(bos);
        gzip.write(data.getBytes("UTF-8"));
        gzip.close();
        byte[] compressed = bos.toByteArray();
        bos.close();
        return compressed;
    }
	
    public static String decompress(byte[] compressed) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(compressed);
        GZIPInputStream gis = new GZIPInputStream(bis);
        BufferedReader br = new BufferedReader(new InputStreamReader(gis, "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line;
        while((line = br.readLine()) != null) {
                sb.append(line);
        }
        br.close();
        gis.close();
        bis.close();
        return sb.toString();
    }
}
