package net.glasslauncher.legacy.components.handlers;

import net.glasslauncher.legacy.components.DragDropList;
import net.glasslauncher.legacy.jsontemplate.Mod;

import javax.swing.*;
import java.awt.datatransfer.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class DragDropHandler extends TransferHandler {
    private DragDropList list;
    private String instpath;

    public DragDropHandler(DragDropList list, String instpath) {
        this.list = list;
        this.instpath = instpath;
    }

    public boolean canImport(TransferSupport support) {
        if (!support.isDataFlavorSupported(DataFlavor.stringFlavor) && !support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            return false;
        }
        JList.DropLocation dl = (JList.DropLocation) support.getDropLocation();
        return dl.getIndex() != -1;
    }

    public boolean importData(TransferSupport support) {
        if (!canImport(support)) {
            return false;
        }

        Transferable transferable = support.getTransferable();
        DefaultListModel<Mod> model = list.model;
        JList.DropLocation dl = (JList.DropLocation) support.getDropLocation();
        int dropIndex = dl.getIndex();

        try {
            if (Arrays.stream(transferable.getTransferDataFlavors()).anyMatch(DataFlavor::isFlavorJavaFileListType)) {
                @SuppressWarnings("unchecked") List<File> files = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
                for (File file : files) {
                    String modName = file.getName();
                    if (modName.contains(".")) {
                        modName = modName.substring(0, modName.lastIndexOf('.'));
                    }
                    model.add(dropIndex, new Mod(file.getName(), modName, true, new String[]{""}, ""));
                    Files.copy(file.toPath(), new File(instpath + "mods/" + file.getName()).toPath());
                    dropIndex++;
                }
                return true;
            }
            else if (Arrays.stream(transferable.getTransferDataFlavors()).anyMatch(DataFlavor::isFlavorTextType)) {
                if (list.model.size() == 0) {
                    return false;
                }
                int draggedObjectIndex = Integer.parseInt((String) transferable.getTransferData(DataFlavor.stringFlavor));

                Mod draggedObject = model.get(draggedObjectIndex);
                if (model.indexOf(draggedObject) < dropIndex) {
                    dropIndex--;
                }
                model.removeElement(draggedObject);
                model.add(dropIndex, draggedObject);
                return true;
            }
            else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}