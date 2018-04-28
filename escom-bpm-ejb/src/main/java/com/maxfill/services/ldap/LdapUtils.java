/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.maxfill.services.ldap;

import com.maxfill.utils.EscomUtils;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

/**
 *
 * @author Maxim
 */
public final class LdapUtils {
    private static final Logger LOG = Logger.getLogger(LdapUtils.class.getName());
        
    /**
     * Инициализация подключения к LDAP
     * @param ldapUsername
     * @param ldapPassword
     * @param ldapAdServer
     * @return
     * @throws javax.naming.NamingException
     */
    public static LdapContext initLDAP(String ldapUsername, String ldapPassword, String ldapAdServer) throws NamingException{    
        Hashtable<String, Object> env = new Hashtable<>();
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        if(ldapUsername != null) {
            env.put(Context.SECURITY_PRINCIPAL, ldapUsername);
        }
        if(ldapPassword != null) {
            env.put(Context.SECURITY_CREDENTIALS, ldapPassword);
        }
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, ldapAdServer);

        //ensures that objectSID attribute values
        //will be returned as a byte[] instead of a String
        env.put("java.naming.ldap.attributes.binary", "objectSID");
        
        LdapContext ldapContext = null;
                 
        ldapContext = new InitialLdapContext(env, null);
        return ldapContext;
    }
    
    public static SearchResult findAccountByAccountName(DirContext ctx, String ldapSearchBase, String accountName) throws NamingException {     
        String searchFilter = "(&(objectClass=user)(sAMAccountName=" + accountName + "))";
        LOG.log(Level.INFO, "AD filter = {0}", searchFilter);
        
        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);

        NamingEnumeration<SearchResult> results = ctx.search(ldapSearchBase, searchFilter, searchControls);

        SearchResult searchResult = null;
        if(results.hasMoreElements()) {
            searchResult = (SearchResult) results.nextElement();
            return searchResult;
        }
        
        return null;
    }
        
    public static List<SearchResult> findAccounts(DirContext ctx, String ldapSearchBase, String groupFilter) throws NamingException {
        List<SearchResult> searchResults = new ArrayList<>();
        
        String searchFilter = "(&(objectClass=user)(memberOf="+groupFilter+","+ldapSearchBase+"))";
        LOG.log(Level.INFO, "AD filter = {0}", searchFilter);
        
        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);

        NamingEnumeration<SearchResult> results = ctx.search(ldapSearchBase, searchFilter, searchControls);

        SearchResult searchResult = null;
        if(results.hasMoreElements()) {
            searchResult = (SearchResult) results.nextElement();
            addSearchResults(searchResult, results, searchResults);
        }
        
        return searchResults;
    }
    
    private static void addSearchResults(SearchResult searchResult, NamingEnumeration<SearchResult> results, List<SearchResult> searchResults){
        searchResults.add(searchResult);
        if(results.hasMoreElements()) {
            searchResult = (SearchResult) results.nextElement();
            addSearchResults(searchResult, results, searchResults);  
        }
    }
    
    public static String decodeSID(byte[] sid) {
        
        final StringBuilder strSid = new StringBuilder("S-");

        // get version
        final int revision = sid[0];
        strSid.append(Integer.toString(revision));
        
        //next byte is the count of sub-authorities
        final int countSubAuths = sid[1] & 0xFF;
        
        //get the authority
        long authority = 0;
        //String rid = "";
        for(int i = 2; i <= 7; i++) {
           authority |= ((long)sid[i]) << (8 * (5 - (i - 2)));
        }
        strSid.append("-");
        strSid.append(Long.toHexString(authority));
        
        //iterate all the sub-auths
        int offset = 8;
        int size = 4; //4 bytes for each sub auth
        for(int j = 0; j < countSubAuths; j++) {
            long subAuthority = 0;
            for(int k = 0; k < size; k++) {
                subAuthority |= (long)(sid[offset + k] & 0xFF) << (8 * k);
            }
            
            strSid.append("-");
            strSid.append(subAuthority);
            
            offset += size;
        }
        
        return strSid.toString();    
    }
    
    public static String getPrimaryGroupSID(SearchResult srLdapUser) throws NamingException {
        byte[] objectSID = (byte[])srLdapUser.getAttributes().get("objectSid").get();
        String strPrimaryGroupID = (String)srLdapUser.getAttributes().get("primaryGroupID").get();
        
        String strObjectSid = decodeSID(objectSID);
        
        return strObjectSid.substring(0, strObjectSid.lastIndexOf('-') + 1) + strPrimaryGroupID;
    }
    
    public static String findGroupBySID(DirContext ctx, String ldapSearchBase, String sid) throws NamingException {
        
        String searchFilter = "(&(objectClass=group)(objectSid=" + sid + "))";

        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        
        NamingEnumeration<SearchResult> results = ctx.search(ldapSearchBase, searchFilter, searchControls);

        if(results.hasMoreElements()) {
            SearchResult searchResult = (SearchResult) results.nextElement();

            //make sure there is not another item available, there should be only 1 match
            if(results.hasMoreElements()) {
                System.err.println("Matched multiple groups for the group with SID: " + sid);
                return null;
            } else {
                return (String)searchResult.getAttributes().get("sAMAccountName").get();
            }
        }
        return null;
    }
    
    /**
     * Формирует из строки memberOf LDAP список названий групп 
     * @param memberOf
     * @return 
     */
    public static List<String> makeUserGroups(String memberOf){
        List<String> groups = new ArrayList<>();
        List<String> strlist = EscomUtils.SplitString(memberOf, " ");
        for (String str : strlist){
            Integer beginIndex = str.indexOf("=") + 1 ;
            Integer endIndex = str.indexOf(",");
            String group = str.substring(beginIndex, endIndex);
            groups.add(group);
        }
        return groups;
    }
}
