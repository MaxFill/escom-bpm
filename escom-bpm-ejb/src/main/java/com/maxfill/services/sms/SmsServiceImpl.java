package com.maxfill.services.sms;

import com.maxfill.Configuration;
import com.maxfill.model.basedict.user.User;
import com.maxfill.utils.EscomUtils;
import org.apache.commons.lang3.StringUtils;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.net.URL;
import java.net.URLConnection;
import java.security.SecureRandom;
import java.text.MessageFormat;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless
public class SmsServiceImpl implements SmsService{
    private static final Logger LOGGER = Logger.getLogger(SmsServiceImpl.class.getName());

    @EJB
    private Configuration conf;

    /**
     * Генерация кода доступа
     * @param phone
     * @return
     */
    @Override
    public String generatePinCode() {
        SecureRandom random = new SecureRandom();
        int num = random.nextInt(100000);
        String pinCode = String.format("%05d", num);
        return pinCode;
    }

    /**
     * Отправка кода доступа пользователю на мобильник, указанный в его карточке
     * @param user
     * @return
     */
    @Override
    public String sendAccessCode(String phone, String pinCode){
        String result = sendSms(phone, pinCode, conf.getSmsSender());
        return result;
    }

    /**
     * Возвращает статус сервиса отправки SMS
     * @return
     */
    @Override
    public boolean isActive() {
        return conf.getSmsMaxCount() > 0
                && StringUtils.isNotBlank(conf.getSmsLogin())
                && StringUtils.isNotBlank(conf.getSmsHostGate());
    }

    /**
     * Отправка SMS через http шлюз
     * @param phone
     * @param text
     * @param sender
     */
    private String sendSms(String phone, String text, String sender){
        try {
            String name = conf.getSmsLogin();
            String password = conf.getSmsPwl();
            String clearPhone = EscomUtils.clearPhoneNumber(phone);
            String authString = name + ":" + password;
            String authStringEnc = Base64.getEncoder().encodeToString(authString.getBytes());
            Integer port = conf.getSmsHostPort();
            String smsProtocol = conf.getSmsHostProtocol().toLowerCase();
            String smsGate = conf.getSmsHostGate();

            String smsCommand = MessageFormat.format(conf.getSmsCommand(), new Object[]{clearPhone, URLEncoder.encode(text, "UTF-8"), sender});
            URL url = new URL(smsProtocol, smsGate, port, smsCommand);

            InputStream inputStream ;
            if (smsProtocol.equals("https")) {
                HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                urlConnection.setRequestProperty("Authorization", authStringEnc);
                inputStream = urlConnection.getInputStream();
            } else {
                URLConnection urlConnection = url.openConnection();
                urlConnection.setRequestProperty("Authorization", authStringEnc);
                inputStream = urlConnection.getInputStream();
            }

            InputStreamReader isr = new InputStreamReader(inputStream);

            int numCharsRead;
            char[] charArray = new char[1024];
            StringBuffer sb = new StringBuffer();
            while ((numCharsRead = isr.read(charArray)) > 0) {
                sb.append(charArray, 0, numCharsRead);
            }
            String result = sb.toString();
            if (!result.contains("error")){
                conf.changeSmsCount();  //уменьшить число доступных SMS
            }
            return result;

        } catch (MalformedURLException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
