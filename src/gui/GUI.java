package gui;

import assembulator.Assembler;
import assembulator.Assembulator;
import gui.factories.ButtonFactory;
import gui.factories.MenuBarFactory;
import gui.factories.MenuItemFactory;
import gui.factories.TextFieldFactory;
import instructions.BadInstructionException;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class GUI implements Executable{

    public static final int PREF_SIZE = 500;
    public static final int PADDING = 10;

    private TextField input ;
    private TextField output;
    private MenuBar topBar;
    private List<Button> buttons = new ArrayList<>();
    private Map<String, MenuListener<Boolean>> listenerMap = new HashMap<>();
    private Group root = new Group();
    private BorderPane bp = new BorderPane();
    private Assembler assembler = new Assembulator();
    private TextArea log = new TextArea();
    private ImageView title = new ImageView(new Image(this.getClass().getClassLoader().getResourceAsStream("350_Assembler.png")));

    public GUI(Stage s){
        init();
        bp.setPrefHeight(PREF_SIZE);
        bp.setPrefWidth(PREF_SIZE);
        bp.setPadding(new Insets(PADDING));
        log.setEditable(false);
        log.setPrefSize(0.9 * PREF_SIZE, 0.4 * PREF_SIZE);
        var masterPane = new BorderPane();
        var barPane = new BorderPane();
        barPane.setTop(topBar);
        masterPane.setTop(barPane);
        masterPane.setBottom(bp);
        Scene scene = new Scene(masterPane);
        s.setScene(scene);
        s.show();
    }

    private void init(){
        buildTextFields();
        buildMenuBar();
        buildButtons();
        BorderPane p = new BorderPane();
        BorderPane but = new BorderPane();
        but.setRight(buttons.get(0));
        //bp.setTop(topBar);
        BorderPane logoPane = new BorderPane();
        logoPane.setCenter(title);
        bp.setTop(logoPane);
        title.setPreserveRatio(true);
        title.setFitWidth(0.8 * PREF_SIZE);
        root.getChildren().add(p);
        bp.setBottom(log);
        var in = packageTextAndButton(input, buttons.get(1));
        var out = packageTextAndButton(output, buttons.get(2));
        var center = new VBox();
        center.getChildren().add(in);
        center.getChildren().add(out);
        center.getChildren().add(but);
        bp.setCenter(center);
    }

    private VBox packageTextAndButton(TextField tf, Button b){
        VBox ret = new VBox();
        ret.setPadding(new Insets(PADDING));
        ret.getChildren().add(tf);
        ret.getChildren().add(b);
        return ret;
    }

    private void buildButtons() {
        List<String> labels = List.of("Launch", "...", "...");
        List<EventHandler<ActionEvent>> actions = List.of((e)->go(),
                e->assignField(input, DialogFactory.fileLoadChooser("MIPS code (*.s)", "*.s",
                        "Assembly code (*.asm)", "*.asm")),
                e->assignField(output, DialogFactory.fileSaveChooser()));
        for(int i = 0; i < labels.size(); ++i)
            buttons.add(ButtonFactory.getInstance(labels.get(i), actions.get(i)));
    }

    private void buildMenuBar() {
        List<String> structure = List.of(
                MenuBarFactory.MENU, "File",
                    MenuBarFactory.MENU, "Protection Level",
                        MenuBarFactory.MENU_ITEM, MenuItemFactory.CHECK_MENU, "Pad all",
                        MenuBarFactory.MENU_ITEM, MenuItemFactory.CHECK_MENU, "No padding",
                    MenuBarFactory.BREAK,
                    MenuBarFactory.MENU_ITEM, MenuItemFactory.CHECK_MENU, "Overwrite files?",
                    MenuBarFactory.MENU_ITEM, MenuItemFactory.CHECK_MENU, "Search subdirs?",
                    MenuBarFactory.MENU_ITEM, MenuItemFactory.CHECK_MENU, "Add output dir?",
                MenuBarFactory.BREAK,
                MenuBarFactory.MENU, "Edit",
                    MenuBarFactory.MENU_ITEM, MenuItemFactory.DEFAULT_MENU, "Clear log",
                    MenuBarFactory.MENU_ITEM, MenuItemFactory.CHECK_MENU, "Clear log before runs",
                MenuBarFactory.BREAK
        );
        listenerMap.put("overwrite", new MenuListener<>());
        listenerMap.put("all", new MenuListener<>());
        listenerMap.put("none", new MenuListener<>());
        listenerMap.put("subdirs", new MenuListener<>());
        listenerMap.put("outdir", new MenuListener<>());
        listenerMap.put("clcEach", new MenuListener<>());
        for(MenuListener<Boolean> ml : listenerMap.values()) ml.setDefaultState(false);
        List<ChangeListener<Boolean>> listeners = List.of(
                listenerMap.get("all"), listenerMap.get("none"), listenerMap.get("overwrite"),
                listenerMap.get("subdirs"), listenerMap.get("outdir"), listenerMap.get("clcEach"));
        List<EventHandler<ActionEvent>> handlers = List.of(e->log.clear());
        topBar = MenuBarFactory.getInstance(structure, listeners, handlers);
    }

    private void buildTextFields() {
        input = TextFieldFactory.getInstance("Input file/directory");
        output = TextFieldFactory.getInstance("Output directory");
    }

    private void assignField(TextField tf, File f){
        if(f == null) return;
        try{
            String path = f.getCanonicalPath();
            tf.setText(path);
        } catch (IOException e){
            DialogFactory.showError(e);
        }
    }
    @Override
    public void go(){
        if(listenerMap.get("clcEach").getCurrentState()) log.clear(); // clear before each run == true
        String inString = input.getText();
        String outString = output.getText();
        if(inString.isEmpty() || outString.isEmpty()){
            DialogFactory.showError("Must specify input and output locations.");
            return;
        }
        File in = new File(inString);
        File out = (listenerMap.get("outdir").getCurrentState()) ? new File(String.join(File.separator, outString, "mif_outputs")) : new File(outString);
        if(!out.exists()) out.mkdirs();
        try {
            if (in.isDirectory()) {
                encodeAllFiles(in, out);
            } else {
                writeFile(in, getOutputFileFromDirectory(out, in.getName()));
            }
        } catch(IOException | BadInstructionException e){
            DialogFactory.showError(e);
            log.appendText(String.format("!!FAILURE!! -- %s\n", e.getMessage()));
        }
    }

    private void encodeAllFiles(File in, File out) throws IOException, BadInstructionException{
        Queue<File> files = new LinkedList<>(Arrays.asList(in.listFiles()));
        while(!files.isEmpty()) {
            File file = files.remove();
            if(listenerMap.get("subdirs").getCurrentState() && file.isDirectory()){
                files.addAll(Arrays.asList(file.listFiles()));
            } else if(!file.isDirectory()){
                String name = file.getName();
                File outputFile = getOutputFileFromDirectory(out, name);
                writeFile(file, outputFile);
            }
        }
    }

    private File getOutputFileFromDirectory(File out, String name) throws IOException {
        StringBuilder sb = new StringBuilder(name);
        sb.delete(sb.lastIndexOf("."), sb.length());
        sb.append(".mif");
        return new File(String.join(File.separator, out.getCanonicalPath(), sb.toString()));
    }

    private void writeFile(File fin, File fout) throws IOException {
        log.appendText(String.format("Attempting to write %s to %s\n", fin.getName(), fout.getName()));
        if(fout.exists() && !listenerMap.get("overwrite").getCurrentState()){
            log.appendText(String.format("File %s already exists. To set overwrite, update menu setting in File->Overwrite Files\n", fout.getName()));
            return;
        }
        FileInputStream fis = new FileInputStream(fin);
        FileOutputStream fos = new FileOutputStream(fout);
        try {
            assembler.writeTo(fis, fos, listenerMap.get("all").getCurrentState());
            fis.close();
            fos.close();
            log.appendText("Success\n");
        } catch (BadInstructionException e) {
            DialogFactory.showError(e);
            fos.close();
            fis.close();
            fout.delete();
            log.appendText(String.format("!!FAILURE!! -- %s", e.getMessage()));
        }
    }
}
