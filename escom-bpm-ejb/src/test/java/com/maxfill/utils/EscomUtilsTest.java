/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.maxfill.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author maksim
 */
public class EscomUtilsTest {
    
    public EscomUtilsTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    @Test
    @Ignore
    public void testGetReleaseInfo() {
        System.out.println("getReleaseInfo");
        String licenceNumber = "";
        String expResult = "";
        String result = EscomUtils.getReleaseInfo(licenceNumber);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    @Test
    public void testFindDublicate() {
        System.out.println("TEST RUN: [EscomUtils] findDublicate");
        List<String> list = Arrays.asList("A", "B", "B", "C", "D", "D", "Z", "E", "E");
        List<String> expectResult = Arrays.asList("B", "D", "E");
        List<String> factResult = (List<String>)EscomUtils.findDublicate(list);
        assertTrue(factResult.containsAll(expectResult));
        assertEquals(factResult.size(), expectResult.size());
        System.out.println("TEST OK");
    }
    
    @Test
    @Ignore
    public void testListToString() {
        System.out.println("listToString");
        List<String> ids = null;
        String expResult = "";
        String result = EscomUtils.listToString(ids);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    @Test
    @Ignore
    public void testSplitString() {
        System.out.println("SplitString");
        String subject = "";
        String delimiters = "";
        ArrayList<String> expResult = null;
        ArrayList<String> result = EscomUtils.SplitString(subject, delimiters);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    @Test
    @Ignore
    public void testMakeSecureFormatPhone() {
        System.out.println("makeSecureFormatPhone");
        String phone = "";
        String expResult = "";
        String result = EscomUtils.makeSecureFormatPhone(phone);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    @Test
    @Ignore
    public void testClearPhoneNumber() {
        System.out.println("clearPhoneNumber");
        String phone = "";
        String expResult = "";
        String result = EscomUtils.clearPhoneNumber(phone);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
    @Test
    @Ignore
    public void testEncryptPassword() {
        System.out.println("encryptPassword");
        String password = "";
        String expResult = "";
        String result = EscomUtils.encryptPassword(password);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
    @Test
    @Ignore
    public void testIntToDateString() {
        System.out.println("IntToDateString");
        long unixTime = 0L;
        String expResult = "";
        String result = EscomUtils.IntToDateString(unixTime);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
    @Test
    @Ignore
    public void testIsValidINN() {
        System.out.println("isValidINN");
        String inn = "";
        boolean expResult = false;
        boolean result = EscomUtils.isValidINN(inn);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
    @Test
    @Ignore
    public void testGetYearYY() {
        System.out.println("getYearYY");
        Date date = null;
        String expResult = "";
        String result = EscomUtils.getYearYY(date);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    @Test
    @Ignore
    public void testGetYearStr() {
        System.out.println("getYearStr");
        Date date = null;
        String expResult = "";
        String result = EscomUtils.getYearStr(date);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
    @Test
    @Ignore
    public void testGenerateGUID() {
        System.out.println("generateGUID");
        String expResult = "";
        String result = EscomUtils.generateGUID();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    @Test
    @Ignore
    public void testGetBarCode_Integer() {
        System.out.println("getBarCode");
        Integer id = null;
        String expResult = "";
        String result = EscomUtils.getBarCode(id);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
    @Test
    @Ignore
    public void testGetBarCode_0args() {
        System.out.println("getBarCode");
        String expResult = "";
        String result = EscomUtils.getBarCode();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
    @Test
    @Ignore
    public void testGenerateUniqueId() {
        System.out.println("generateUniqueId");
        Integer expResult = null;
        Integer result = EscomUtils.generateUniqueId();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    @Test
    @Ignore
    public void testCopyToClipboard() {
        System.out.println("copyToClipboard");
        String sourceString = "";
        EscomUtils.copyToClipboard(sourceString);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
    @Test
    @Ignore
    public void testCompress() throws Exception {
        System.out.println("compress");
        String data = "";
        byte[] expResult = null;
        byte[] result = EscomUtils.compress(data);
        assertArrayEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
    @Test
    @Ignore
    public void testDecompress() throws Exception {
        System.out.println("decompress");
        byte[] compressed = null;
        String expResult = "";
        String result = EscomUtils.decompress(compressed);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
