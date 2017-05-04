package com.maxfill.utils;

import org.apache.commons.lang.StringUtils;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Утилиты
 * @author Filatov Maxim
 */

public final class EscomUtils {
     
    /**
     * Преобразует List в строку с разделителем запятая
     * @param ids
     * @return 
     */
    public static String listToString(List<String> ids){
        return StringUtils.join(ids, ",");
    }
    
    /**
     * Разбивает строку на части
     * @param subject
     * @param delimiters
     * @return 
     */
    public static ArrayList<String> SplitString(String subject, String delimiters) {
        StringTokenizer strTkn = new StringTokenizer(subject, delimiters);
        ArrayList<String> arrLis = new ArrayList<String>(subject.length());

        while(strTkn.hasMoreTokens())
           arrLis.add(strTkn.nextToken());

        return arrLis;
    }    
    
    /**
     * Преобразование строки в MD5
     * @param password
     * @return 
     * @throws java.security.NoSuchAlgorithmException
     */
    public static String encryptPassword(String password) throws NoSuchAlgorithmException {
    	MessageDigest messageDigest = MessageDigest.getInstance("MD5");
    	byte[] bs;
    	messageDigest.reset();
    	bs = messageDigest.digest(password.getBytes());
    	StringBuilder stringBuilder = new StringBuilder();
    	for (int i = 0; i < bs.length; i++)
    	{
    		String hexVal = Integer.toHexString(0xFF & bs[i]);
    		if (hexVal.length() == 1)
    		{
    			stringBuilder.append("0");
    		}
    		stringBuilder.append(hexVal);
    	}
    	return stringBuilder.toString();	
    }
    
    //преобразование в дату-строку
    public static String IntToDateString(long unixTime){
        String newFormatDate = "---"; 
        if (unixTime != 0L) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.YY HH:mm");
            Date resultdate = new Date(unixTime * 1000);
            newFormatDate = sdf.format(resultdate);
        }
        return newFormatDate;  
    }       
    
    /**
     * Проверка корректности ИНН
     */
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
    
    /**
     * Возвращает два последних символа года YY 
     * @param date
     * @return 
     */
    public static String getYearYY(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        String year = String.valueOf(cal.get(Calendar.YEAR));
        String yy = year.substring(2, 4).toUpperCase();
        return yy;
    }
    
    /**
     * Возвращает год как строку 
     * @param date
     * @return 
     */
    public static String getYearStr(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        String year = String.valueOf(cal.get(Calendar.YEAR));
        return year;
    }
    
    /**
     * Формирует уникальный номер GUID
     * @return 
     */
    public static String generateGUID() {
        UUID uuid = UUID.randomUUID();
        String randomUUID = uuid.toString();
        return randomUUID;
    }
    
    /**
     * Копирует строку в буфер
     * @param sourceString 
     */
    public static void copyToClipboard(String sourceString){
        StringSelection selection = new StringSelection(sourceString);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);
    }
}
