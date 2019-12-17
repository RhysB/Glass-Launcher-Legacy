package net.glasslauncher.legacy.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Data;
import net.glasslauncher.legacy.Main;

import java.io.*;
import java.lang.reflect.Type;

@Data
public abstract class JsonConfig {
    private String path;

    /**
     * @param path Path to the JSON file.
     */
    public JsonConfig(String path) {
        this.path = path;
    }

    public static JsonConfig loadConfig(String path, Type pClass) {
        try {
            JsonConfig jsonObj = (new Gson()).fromJson(new FileReader(path), pClass);
            jsonObj.setPath(path);
            return jsonObj;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Saves the JSON object stored in memory.
     */
    public void saveFile() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Main.getLogger().info(path + " : " + (new File(path)).exists());
        try {
            PrintStream out = new PrintStream(new FileOutputStream(path));
            out.print(gson.toJson(this));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
