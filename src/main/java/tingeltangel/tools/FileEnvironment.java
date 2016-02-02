/*
    Copyright (C) 2015   Martin Dames <martin@bastionbytes.de>
  
    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License along
    with this program; if not, write to the Free Software Foundation, Inc.,
    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
  
*/

package tingeltangel.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;

/**
 *
 * @author mdames
 */
public class FileEnvironment {
    
    private static boolean test = false;
    
    /**
     * switch on test mode
     * to be able to import books without overwriting existing books
     */
    public static void test() {
        test = true;
    }

    public static File getHomeDirectory() {
        return new File(System.getProperty("user.home"));
    }

    public static File getBooksDirectory() {
        if(test) {
            return getWorkingDirectory("books-test");
        }
        return getWorkingDirectory("books");
    }

    public static File getBookDirectory(int id) {
        String _id = Integer.toString(id);
        while (_id.length() < 5) {
            _id = "0" + _id;
        }
        File bd = new File(getBooksDirectory(), _id);
        if (bd.exists()) {
            if (!bd.isDirectory()) {
                throw new Error(bd.getAbsolutePath() + " exists but is not a directory");
            }
        } else {
            if (!bd.mkdirs()) {
                throw new Error("can't create directory " + bd.getAbsolutePath());
            }
        }
        return bd;
    }

    public static File getRepositoryDirectory() {
        return getWorkingDirectory("repository");
    }
    

    public static File getAudioDirectory(int id) {
        return(getBookSubDirectory(id, "audio"));
    }
    
    public static File getDistDirectory(int id) {
        return(getBookSubDirectory(id, "dist"));
    }
    
    public static File getBinDirectory(int id) {
        return(getBookSubDirectory(id, "bin"));
    }
    
    public static File getCodesDirectory(int id) {
        return(getBookSubDirectory(id, "codes"));
    }
    
    public static File getPagesDirectory(int id) {
        return(getBookSubDirectory(id, "pages"));
    }
    
    public static File getBinObjectFile(int mid, int oid) {
        return(new File(getBinDirectory(mid), Integer.toString(oid) + ".bin"));
    }
    
    private static File getBookSubDirectory(int id, String subDirectory) {
        File codes = new File(getBookDirectory(id), subDirectory);
        if (codes.exists()) {
            if (!codes.isDirectory()) {
                throw new Error(codes.getAbsolutePath() + " exists but is not a directory");
            }
        } else {
            if (!codes.mkdirs()) {
                throw new Error("can't create directory " + codes.getAbsolutePath());
            }
        }
        return codes;
    }
    
    public static File getXML(int id) {
        return(new File(getBookDirectory(id), "book.xml"));
    }
    
    private static File getWorkingDirectory(String subDirectory) {
        File wd = new File(getWorkingDirectoryRoot(), subDirectory);
        if (wd.exists()) {
            if (!wd.isDirectory()) {
                throw new Error(wd.getAbsolutePath() + " exists but is not a directory");
            }
        } else {
            if (!wd.mkdirs()) {
                throw new Error("can't create directory " + wd.getAbsolutePath());
            }
        }
        return wd;
    }

    public static File getWorkingDirectoryRoot() {
        File wd = null;
        if (OS.isWindows()) {
            String myDocuments = null;
            try {
                Process p = Runtime.getRuntime().exec("reg query \"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\Shell Folders\" /v personal");
                p.waitFor();
                InputStream in = p.getInputStream();
                byte[] b = new byte[in.available()];
                in.read(b);
                in.close();
                myDocuments = new String(b);
                myDocuments = myDocuments.split("\\s\\s+")[4];
            } catch (Exception e) {
                throw new Error(e);
            }
            wd = new File(myDocuments, "tingeltangel");
        } else {
            wd = new File(getHomeDirectory(), ".tingeltangel");
        }
        if (wd.exists()) {
            if (!wd.isDirectory()) {
                throw new Error(wd.getAbsolutePath() + " exists but is not a directory");
            }
        } else {
            if (!wd.mkdirs()) {
                throw new Error("can't create directory " + wd.getAbsolutePath());
            }
        }
        return wd;
    }

    public static void copy(File source, File destination) throws IOException {
        FileChannel sourceChannel = null;
        FileChannel destChannel;
        FileOutputStream fout = null;
        try {
            sourceChannel = new FileInputStream(source).getChannel();
            fout = new FileOutputStream(destination);
            destChannel = fout.getChannel();
            destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
        } catch (Exception e) {
            throw new IOException(e);
        } finally {
            if(sourceChannel != null) {
                sourceChannel.close();
            }
            if(fout != null) {
                fout.close();
            }
        }
    }
    
}
