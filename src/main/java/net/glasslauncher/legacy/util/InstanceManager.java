package net.glasslauncher.legacy.util;

import net.glasslauncher.legacy.Config;
import net.glasslauncher.legacy.Main;

import java.io.File;
import java.net.URL;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;

public class InstanceManager {

    /**
     * Detects what kind of modpack zip has been provided and then calls the related install function for the type.
     * @param path Path to instance zip file.
     * @return true if succeeded, false otherwise.
     */
    public static boolean installModpack(String path) {
        path = path.replace("\\", "/");
        Main.getLogger().info("Installing " + path);
        boolean isURL = true;
        try {
            try {
                new URL(path);
            } catch (Exception e) {
                isURL = false;
            }

            String filename = path.substring(path.lastIndexOf('/') + 1);
            if (isURL) {
                FileUtils.downloadFile(path, Config.getCachePath() + "instancezips");
                installModpackZip(Config.getCachePath() + "instancezips", filename);
            } else {
                if ((new File(path)).exists()) {
                    installModpackZip(path, filename);
                } else {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean installModpackZip(String path, String filename) {
        Main.getLogger().info(path + " : " + filename);
        createBlankInstance("b1.7.3", filename);
        return true;
    }

    public static boolean createBlankInstance(String version, String name) {
        Main.getLogger().info("Creating instance \"" + name + "\" on version " + version);
        String versionsCachePath = Config.getCachePath() + "versions";
        String instanceFolder = Config.getInstancePath(name);
        String minecraftFolder = instanceFolder + "/.minecraft";
        (new File(versionsCachePath)).mkdirs();
        (new File(minecraftFolder + "/bin/")).mkdirs();

        File versionCacheJar = new File(versionsCachePath + "/" + version + ".jar");
        if (versionCacheJar.exists()) {
            try {
                Files.copy(versionCacheJar.toPath(), new File(minecraftFolder + "/bin/minecraft.jar").toPath());
            } catch (FileAlreadyExistsException e) {
                Main.getLogger().info("Instance \"" + name + "\" already exists!");
                return false;
            } catch (Exception e) {
                e.printStackTrace();
                /*try {
                    org.apache.commons.io.FileUtils.deleteDirectory(new File(minecraftFolder));
                } catch (Exception ignored) {}*/
                return false;
            }
        } else {

            try {
                String versionID =Config.getMcVersions().getClient().get(version).getUrl();
                FileUtils.downloadFile("https://launcher.mojang.com/v1/objects/" + versionID + "/client.jar", versionsCachePath, null, version + ".jar");
                Files.copy(versionCacheJar.toPath(), new File(minecraftFolder + "/bin/minecraft.jar").toPath());
            } catch (Exception e) {
                /*try {
                    org.apache.commons.io.FileUtils.deleteDirectory(new File(minecraftFolder));
                } catch (Exception ignored) {}*/
                Main.getLogger().info("Version not found: \"" + version + "\". Aborting.");
                return false;
            }
        }
        File lwjglCacheZip = new File(versionsCachePath + "/lwjgl.zip");
        if (!lwjglCacheZip.exists()) {
            FileUtils.downloadFile("https://files.pymcl.net/client/lwjgl/lwjgl." + Config.getOs() + ".zip", versionsCachePath, null, "lwjgl.zip");
        }
        FileUtils.extractZip(lwjglCacheZip.getPath(), minecraftFolder + "/bin");
        return true;
    }

    public void addMod(String modpath, String instanceFolder) {
        String minecraftFolder = instanceFolder + "/.minecraft";
        try {
            TempZipFile jarFile = new TempZipFile(minecraftFolder + "/bin/minecraft.jar");
            if (jarFile.fileExists("META-INF")) {
                jarFile.deleteFile("META-INF");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }
}
