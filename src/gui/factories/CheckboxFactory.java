package gui.factories;

import javafx.beans.value.ChangeListener;
import javafx.scene.control.CheckBox;

public class CheckboxFactory {

    public static CheckBox getInstacnce(){
        return getInstance("My Checkbox");
    }

    public static CheckBox getInstance(String label){
        return new CheckBox(label);
    }

    public static CheckBox getInstance(String label, ChangeListener<Boolean> listener){
        CheckBox cb = getInstance(label);
        cb.selectedProperty().addListener(listener);
        return cb;
    }
}
