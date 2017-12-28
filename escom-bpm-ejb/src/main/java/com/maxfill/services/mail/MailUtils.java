package com.maxfill.services.mail;

import com.maxfill.Configuration;
import com.maxfill.services.Services;
import com.maxfill.utils.EscomUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.*;
import javax.xml.bind.JAXB;

/**
 * Утилиты для работы с email
 */
public final class MailUtils {
    private static final Logger LOG = Logger.getLogger(MailUtils.class.getName());



}