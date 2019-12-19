package net.glasslauncher.legacy.util;

import com.google.gson.Gson;
import net.glasslauncher.jsontemplate.*;
import net.glasslauncher.legacy.Config;
import net.glasslauncher.legacy.Main;

import javax.swing.*;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.*;
import java.util.Objects;

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
        TempZipFile instanceZipFile = new TempZipFile(path);
        try {
            boolean isMultiMC = false;
            URL mmcPackURL = null;
            String mmcZipInstDir = "";

            // Because MMC makes a subfolder named after the instance and puts the instance files in there.
            for (File file : Objects.requireNonNull(instanceZipFile.getFile("").listFiles())) {
                if (file.isDirectory()) {
                    for (File subFile : Objects.requireNonNull(file.listFiles())) {
                        if (subFile.getName().equals("mmc-pack.json") && subFile.isFile()) {
                            isMultiMC = true;
                            mmcPackURL = subFile.toURI().toURL();
                            mmcZipInstDir = file.getName();
                        }
                    }
                }
            }

            if (isMultiMC) {
                Main.getLogger().info("Provided instance is a MultiMC instance. Importing...");
                if ((new File(Config.getGlassPath() + "instances/" + filename)).exists()) {
                    Main.getLogger().info("Instance \"" + filename + "\" already exists!");
                    return false;
                }
                InputStream inputStream = mmcPackURL.openStream();
                String jsonText = FileUtils.convertStreamToString(inputStream);
                inputStream.close();
                MultiMCPack multiMCPack = (new Gson()).fromJson(jsonText, MultiMCPack.class);
                importMultiMC(new File(path), filename, mmcZipInstDir, multiMCPack);

                return true;
            } else if (instanceZipFile.getFile("instance_config.json").exists()) {
                InstanceConfig instanceConfig = new InstanceConfig(instanceZipFile.getFile("instance_config.json").getPath());
                createBlankInstance(instanceConfig.getVersion(), filename);
                instanceZipFile.copyContentsToDir("", Config.getGlassPath() + "instances/" + filename);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //instanceZipFile.close(false);
        }
        return false;
    }

    public static boolean createBlankInstance(String version, String name) {
        Main.getLogger().info("Creating instance \"" + name + "\" on version " + version);
        String versionsCachePath = Config.getCachePath() + "versions";
        String instanceFolder = Config.getInstancePath(name);
        String minecraftFolder = instanceFolder + "/.minecraft";
        (new File(versionsCachePath)).mkdirs();
        (new File(minecraftFolder + "/bin/")).mkdirs();

        File versionCacheJar = new File(versionsCachePath + "/" + version + ".jar");
        if (Config.getMcVersions().getClient().containsKey(version)) {
            if (versionCacheJar.exists()) {
                try {
                    Files.copy(versionCacheJar.toPath(), new File(minecraftFolder + "/bin/minecraft.jar").toPath());
                } catch (FileAlreadyExistsException e) {
                    Main.getLogger().info("Instance \"" + name + "\" already exists!");
                    return false;
                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        org.apache.commons.io.FileUtils.deleteDirectory(new File(minecraftFolder));
                    } catch (Exception ex) {
                        e.printStackTrace();
                        ex.printStackTrace();
                    }
                    return false;
                }
            } else {
                try {
                    String url = Config.getMcVersions().getClient().get(version).getUrl();
                    if (!(url.startsWith("https://") || url.startsWith("http://"))) {
                        url = "https://launcher.mojang.com/v1/objects/" + Config.getMcVersions().getClient().get(version).getUrl() + "/client.jar";
                    }
                    FileUtils.downloadFile(url, versionsCachePath, null, version + ".jar");
                    Files.copy(versionCacheJar.toPath(), new File(minecraftFolder + "/bin/minecraft.jar").toPath());
                } catch (Exception e) {
                    try {
                        org.apache.commons.io.FileUtils.deleteDirectory(new File(minecraftFolder));
                    } catch (Exception ex) {
                        e.printStackTrace();
                        ex.printStackTrace();
                    }
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
        if (version.equals("custom")) {
            File lwjglCacheZip = new File(versionsCachePath + "/lwjgl.zip");
            if (!lwjglCacheZip.exists()) {
                FileUtils.downloadFile("https://files.pymcl.net/client/lwjgl/lwjgl." + Config.getOs() + ".zip", versionsCachePath, null, "lwjgl.zip");
            }
            FileUtils.extractZip(lwjglCacheZip.getPath(), minecraftFolder + "/bin");
            return true;
        }
        else {
            try {
                org.apache.commons.io.FileUtils.deleteDirectory(new File(minecraftFolder));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }
    }

    public static void addMod(String modpath, String instanceFolder) {
        String minecraftFolder = instanceFolder + "/.minecraft";
        try {
            TempZipFile jarFile = new TempZipFile(minecraftFolder + "/bin/minecraft.jar");
            if (jarFile.fileExists("META-INF")) {
                jarFile.deleteFile("META-INF");
            }
            jarFile.mergeZip(modpath);
            jarFile.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void addMods(String instance, ListModel<Mod> mods) {
        instance = Config.getGlassPath() + "instances/" + instance;
        try {
            TempZipFile jarFile = new TempZipFile(instance + "/.minecraft/bin/minecraft.jar");
            if (jarFile.fileExists("META-INF")) {
                jarFile.deleteFile("META-INF");
            }
            for (int i = 0; i < mods.getSize(); i++) {
                if (mods.getElementAt(i).isEnabled()) {
                    jarFile.mergeZip(instance + "/mods/" + mods.getElementAt(i).getFileName());
                }
            }
            jarFile.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void importMultiMC(File file, String instance, String mmcZipInstDir, MultiMCPack multiMCPack) throws GenericInvalidVersionException{
        TempZipFile mmcZip = new TempZipFile(file.getPath());
        String instPath = Config.getGlassPath() + "instances/" + instance;
        InstanceConfig instanceConfig = new InstanceConfig(instPath + "/instance_config.json");
        ModList modList = new ModList(instPath + "/mods/mods.json");
        boolean hasCustomJar = false;

        if (multiMCPack.getFormatVersion() != 1) {
            throw new GenericInvalidVersionException("MultiMC instance version is unsupported!");
        }
        for (MultiMCComponent component : multiMCPack.getComponents()) {
            if (component.isImportant()) {
                instanceConfig.setVersion(component.getCachedVersion());
            }
            else if (component.isDependencyOnly() || (component.getUid().equals("customjar") && component.isDisabled())) {}
            else if (component.getUid().equals("customjar") && !component.isDisabled()) {
                hasCustomJar = true;
            }
            else if (component.getCachedName() != null) {
                modList.getJarMods().add(modList.getJarMods().size(), new Mod(component.getUid().replace("org.multimc.jarmod.", "") + ".jar", component.getCachedName(), 0, !component.isDisabled()));
            }
        }
        createBlankInstance(instanceConfig.getVersion(), instance);

        for (Mod mod : modList.getJarMods()) {
            try {
                org.apache.commons.io.FileUtils.copyFile(mmcZip.getFile(mmcZipInstDir + "/jarmods/" + mod.getFileName()), new File(instPath + "/mods/" + mod.getFileName()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            if (hasCustomJar) {
                File originalJar = new File(instPath + "/.minecraft/bin/minecraft.jar");
                originalJar.delete();
                org.apache.commons.io.FileUtils.copyFile(mmcZip.getFile(mmcZipInstDir + "/libraries/customjar-1.jar"), originalJar);
                instanceConfig.setVersion("custom");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        instanceConfig.saveFile();
        modList.saveFile();
        mmcZip.copyContentsToDir(mmcZipInstDir + "/.minecraft", instPath + "/.minecraft");
        mmcZip.close(false);
    }
}
