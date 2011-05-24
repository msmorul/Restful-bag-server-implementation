/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.chronopolis.ingest.exe;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.Parameter;
import java.io.File;

/**
 *
 * @author toaster
 */
public class ServerArgs {

    @Parameter(names = "-port", description = "Port to run on, defaut 8080")
    private int port = 8080;
    @Parameter(names = "-dir", description = "Bag repository directory", required = true, converter = FileConverter.class)
    private File directory = null;
    @Parameter(names = "-h", description = "This message")
    private boolean usage = false;

    public boolean isUsage() {
        return usage;
    }

    public File getDirectory() {
        return directory;
    }

    public int getPort() {
        return port;
    }

    public static class FileConverter implements IStringConverter<File> {

        @Override
        public File convert(String value) {
            return new File(value);
        }
    }
}
