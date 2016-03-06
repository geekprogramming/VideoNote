package t3team.com.utils;

import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * Created by Dinhthap on 3/6/2016.
 */
public class FileUtils {
    public static File getNoteDir(String name) {
        if (Environment.getExternalStorageState() == null) {
//            //create new file directory object
//            directory = new File(Environment.getDataDirectory()
//                    + "/RobotiumTestLog/");
//            photoDirectory = new File(Environment.getDataDirectory()
//                    + "/Robotium-Screenshots/");
//            /*
//             * this checks to see if there are any previous test photo files
//             * if there are any photos, they are deleted for the sake of
//             * memory
//             */
//            if (photoDirectory.exists()) {
//                File[] dirFiles = photoDirectory.listFiles();
//                if (dirFiles.length != 0) {
//                    for (int ii = 0; ii <= dirFiles.length; ii++) {
//                        dirFiles[ii].delete();
//                    }
//                }
//            }
//            // if no directory exists, create new directory
//            if (!directory.exists()) {
//                directory.mkdir();
//            }

            // if phone DOES have sd card
        } else if (Environment.getExternalStorageState() != null) {
//            // search for directory on SD card
//            directory = new File(Environment.getExternalStorageDirectory()
//                    + "/RobotiumTestLog/");
//            photoDirectory = new File(
//                    Environment.getExternalStorageDirectory()
//                            + "/Robotium-Screenshots/");
//            if (photoDirectory.exists()) {
//                File[] dirFiles = photoDirectory.listFiles();
//                if (dirFiles.length > 0) {
//                    for (int ii = 0; ii < dirFiles.length; ii++) {
//                        dirFiles[ii].delete();
//                    }
//                    dirFiles = null;
//                }
//            }
//            // if no directory exists, create new directory to store test
//            // results
//            if (!directory.exists()) {
//                directory.mkdir();
//            }
        }// end of SD card checking
        return null;
    }

    public static void writeDataToFile(File file, String data) throws IOException {
        OutputStream stream = new FileOutputStream(file);
        OutputStreamWriter writer = new OutputStreamWriter(stream);
        writer.write(data);
        writer.close();
        stream.flush();
        stream.close();
    }


}
