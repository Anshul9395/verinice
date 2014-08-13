/*******************************************************************************
 * Copyright (c) 2014 Benjamin Weißenfels.
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Benjamin Weißenfels <bw[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.logging;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.xml.DOMConfigurator;

import sernet.verinice.interfaces.ILogPathService;

import org.apache.commons.lang.SystemUtils;

import static sernet.gs.ui.rcp.main.logging.LogDirectoryProvider.*;

/**
 * Provides additional logging configuration.
 * 
 * <p>
 * Sets the logging path to the verinice workspace if no path is configured for
 * a {@link FileAppender}. Verinice can be also forced to use a specific log
 * path with setting the "-Dlogging.file=<path>" in verinice.ini. This will
 * override the defined file path of all FileAppender in the root logger.
 * </p>
 * 
 * <p>
 * It is also possible to provide your own log4j file with the parameter
 * "-Dlog4j.configuration" in the verinice.ini file.
 * </p>
 * 
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 * 
 */
public class LoggerInitializer implements ILogPathService {

    private static LogDirectoryProvider logDirectoryProvider;

    protected static String currentLogFilePath = null;

    @SuppressWarnings("static-access")
    protected LoggerInitializer() {

        tryReadingCustomLog4jFile();
        tryConfiguringLoggingPath();

        this.logDirectoryProvider = getLogDirectoryProvider();

    }

    private LogDirectoryProvider getLogDirectoryProvider() {
        if (SystemUtils.IS_OS_WINDOWS) {
            return new WindowsLogDirectory(currentLogFilePath);
        } else {
            return new UnixLogDirectory(currentLogFilePath);
        }
    }

    /**
     * Checks if the -Dlog4j.configuration system property is set and if so it
     * reconfigures the verinice client logger.
     * 
     */
    private static void tryReadingCustomLog4jFile() {

        if (existsCustomLog4jConfigurationFile()) {
            configureWithCustomLog4jFile();
        }
    }

    /**
     * Checks all {#link {@link FileAppender}, if a log path is already defined.
     * If a log path is set by the system property "logging.file" in verinic.ini
     * this path is applied to all {@link FileAppender} and overrides always the
     * origin file path defined in a log4j file.
     */
    private static void tryConfiguringLoggingPath() {
        getLogFilePath();
        configureAllFileAppender();
    }

    private static boolean existsCustomLog4jConfigurationFile() {
        return System.getProperty(LogDirectoryProvider.LOG4J_CONFIGURATION_JVM_ENV_KEY) != null;
    }

    private static void configureWithCustomLog4jFile() {

        Logger.getRootLogger().getLoggerRepository().resetConfiguration();
        String config = System.getProperty(LogDirectoryProvider.LOG4J_CONFIGURATION_JVM_ENV_KEY);
        String extension = FilenameUtils.getExtension(config);

        if (("xml").equals(extension)) {
            DOMConfigurator.configure(config);
        }

        else if ("properties".equals(extension)) {
            PropertyConfigurator.configure(config);
        }
    }

    private static void configureAllFileAppender() {

        Logger log = Logger.getRootLogger();
        Enumeration<Appender> appenders = log.getAllAppenders();

        while (appenders.hasMoreElements()) {
            Appender appender = appenders.nextElement();
            if (appender instanceof FileAppender) {

                FileAppender fileAppender = (FileAppender) appender;
                if (!isFilePathConfigured(fileAppender) || isConfiguredInVeriniceIniFile()) {
                    fileAppender.setFile(currentLogFilePath);

                    // without this call, the changes does have no effect
                    fileAppender.activateOptions();
                }
            }
        }
    }

    private static boolean isConfiguredInVeriniceIniFile() {
        return System.getProperty(LOGGING_PATH_KEY) != null;
    }

    private static boolean isFilePathConfigured(FileAppender fileAppender) {
        return fileAppender.getFile() != null;
    }

    private static String getLogFilePath() {

        String filePath = null;
        
        if (isConfiguredInVeriniceIniFile()) {
            filePath = readFromVeriniceIniFile();
        } else if (existsFilePathInRootLogger()) {
            filePath = getPathFromRootLogger();
        } else {
            filePath = getStandardDirectory() + DEFAULT_VERINICE_LOG;
        }

        currentLogFilePath = removeInvalidPrefix(filePath);
        return currentLogFilePath;
    }
    
    private static String removeInvalidPrefix(String directory){
        if (directory.startsWith("file:") ){
            return directory.substring(5);
        }
        
        return directory;
    }

    private static String getStandardDirectory() {
        return FilenameUtils.concat(System.getProperty(WORKSPACE_PROPERTY_KEY), LOG_FOLDER);
    }

    private static boolean existsFilePathInRootLogger() {
        Logger log = Logger.getRootLogger();
        Enumeration<Appender> appenders = log.getAllAppenders();

        while (appenders.hasMoreElements()) {
            Appender appender = appenders.nextElement();
            if (appender instanceof FileAppender) {

                FileAppender fileAppender = (FileAppender) appender;
                return isFilePathConfigured(fileAppender);

            }
        }

        return false;
    }

    protected static String getPathFromRootLogger() {
        Logger log = Logger.getRootLogger();
        Enumeration<Appender> appenders = log.getAllAppenders();

        while (appenders.hasMoreElements()) {
            Appender appender = appenders.nextElement();
            if (appender instanceof FileAppender) {

                FileAppender fileAppender = (FileAppender) appender;
                return fileAppender.getFile();
            }
        }

        return null;
    }

    private static String readFromVeriniceIniFile() {
        return System.getProperty(LOGGING_PATH_KEY);
    }

    @Override
    public String getLogDirectory() {
        return logDirectoryProvider.getLogDirectory();

    }

    public void setLogDirectoryProvider(LogDirectoryProvider logDirectoryProvider) {
        this.logDirectoryProvider = logDirectoryProvider;
    }
    
    public static LoggerInitializer setupLogFilePath(){
        return new LoggerInitializer();
    }

}