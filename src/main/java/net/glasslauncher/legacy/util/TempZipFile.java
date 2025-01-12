package net.glasslauncher.legacy.util;

import net.glasslauncher.common.CommonConfig;
import net.glasslauncher.common.FileUtils;
import net.glasslauncher.legacy.Main;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

import static net.glasslauncher.legacy.Config.destDirBypass;

public class TempZipFile {
    private final String originalPath;
    private final String tempPath;

    public TempZipFile(String zipFilePath) {
        File zipFile = new File(zipFilePath);
        originalPath = zipFilePath;
        tempPath = CommonConfig.getGlassPath() + "temp/" + zipFile.getName();
        File tempFile = new File(destDirBypass + tempPath);
        if (tempFile.exists()) {
            try {
                FileUtils.delete(tempFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        tempFile.mkdirs();

        FileUtils.extractZip(zipFilePath, tempPath);
    }

    public void deleteFile(String relativePath) {
        File fileToDelete = new File(tempPath + "/" + relativePath);
        try {
            if (fileToDelete.exists()) {
                if (fileToDelete.isDirectory()) {
                    FileUtils.delete(fileToDelete);
                } else {
                    fileToDelete.delete();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean fileExists(String relativePath) {
        return getFile(relativePath).exists();
    }

    public File getFile(String relativePath) {
        return new File(tempPath + "/" + relativePath);
    }

    public void mergeZip(String zipToMergePath) {
        File zipToMerge = new File(zipToMergePath);
        if (zipToMerge.exists()) {
            FileUtils.extractZip(zipToMergePath, tempPath);
        }
    }

    public void copyContentsToDir(String relative, String target) {
        File targetPath = new File(target);
        File relativePath = new File(tempPath + "/" + relative);
        try {
            FileUtils.copyRecursive(relativePath.toPath(), targetPath.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
        close(true);
    }

    public void close(boolean doSave) {
        if (doSave) {
            File original = new File(originalPath);
            try {
                original.delete();
            } catch (Exception e) {
                Main.LOGGER.info("Failed to delete/check read permissions for \"" + originalPath + "\"");
                e.printStackTrace();
                return;
            }
            try {
                List<String> args = new ArrayList<>();
                args.add("jar");
                args.add("cMf");
                args.add(originalPath);
                args.add("./*");
                ProcessBuilder processBuilder = new ProcessBuilder(args);
                processBuilder.redirectInput(ProcessBuilder.Redirect.INHERIT);
                processBuilder.directory(new File(tempPath));
                Process zipProcess = processBuilder.start();
                zipProcess.waitFor(10, TimeUnit.MINUTES);
                if (zipProcess.isAlive()) {
                    Main.LOGGER.warning("Zip process has been running for longer than 10 minutes! Terminating process...");
                    Main.LOGGER.warning("Target folder to zip was: \"" + tempPath + "\"");
                    zipProcess.destroy();
                    zipProcess.waitFor(10, TimeUnit.SECONDS);
                    if (zipProcess.isAlive()) {
                        Main.LOGGER.warning("Zip process has taken longer than 10 seconds to terminate! Force terminating process...");
                        zipProcess.destroyForcibly();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            FileUtils.delete(new File(destDirBypass + tempPath));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
