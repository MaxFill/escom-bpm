package com.maxfill.services.users;

import com.maxfill.Configuration;
import com.maxfill.dictionary.SysParams;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.lang.StringUtils;

@Stateless
public class UsersServiceImpl implements UsersService {
    private static final String COMMAND_WIN = "add-user.bat";
    private static final String COMMAND_UNIX = "./add-user.sh";
    private static final Logger LOGGER = Logger.getLogger(UsersServiceImpl.class.getName());
    @EJB
    private Configuration conf;
    
    @Asynchronous
    @Override
    public void addUserInRealm(String user, String pwl) {
        String separator = File.separator;
        StringBuilder command = new StringBuilder(conf.getServerPath());
        command.append("bin").append(separator);
        try {
            switch (conf.getServerOS()){
                case SysParams.OS_UNIX:{
                    command.append(COMMAND_UNIX);
                    break;
                }
                case SysParams.OS_WIN:{
                    command.append(COMMAND_WIN);
                    break;
                }
            }
            CommandLine commandLine = CommandLine.parse(command.toString());
            commandLine.addArgument("-a");
            commandLine.addArgument(user);
            commandLine.addArgument(pwl);
            DefaultExecutor executor = new DefaultExecutor();
            executor.setExitValue(0);
            executor.execute(commandLine);
            
            StringBuilder pathLine = new StringBuilder(conf.getServerPath());
            pathLine.append("standalone").append(separator).append("configuration").append(separator).append("application-roles.properties");
            Path path = Paths.get(pathLine.toString());
            
            if (!checkUserInRole(user, path)){
                addUserInRoleProperty(user, path);
            }
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    } 
    
    private boolean checkUserInRole(String user, Path path){
        String line;
        try (InputStream fis = new FileInputStream(path.toString());
            InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
            BufferedReader br = new BufferedReader(isr);
        ) {
            while ((line = br.readLine()) != null) {
                if (StringUtils.contains(line, user)){
                    return true;
                }
            }
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    private void addUserInRoleProperty(String user, Path path){
        StringBuilder sb = new StringBuilder(System.lineSeparator());
        sb.append(user).append("=").append("connect, readwrite, users");
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardOpenOption.APPEND)) {
            writer.write(sb.toString());
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
    
}
