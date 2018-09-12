package gui.factories;


import javafx.event.EventHandler;
import javafx.scene.control.Menu;
import javafx.event.ActionEvent;

import java.util.List;

public class MenuFactory {

    public static Menu getInstance(String label){
        return new Menu(label);
    }

    public static Menu getInstance(String menuTitle, List<String> labels, List<EventHandler<ActionEvent>> actions){
        if(labels.size() != actions.size()) throw new IllegalArgumentException("Must have equal number of labels and handlers");
        Menu m = new Menu(menuTitle);
        for(int i = 0; i < labels.size(); ++i){
            String label = labels.get(i);
            var action = actions.get(i);
            m.getItems().add(MenuItemFactory.getInstance(label, action));
        }
        return m;
    }
}
