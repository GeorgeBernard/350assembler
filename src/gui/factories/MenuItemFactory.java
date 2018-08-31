package gui.factories;


import gui.MenuListener;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;


public class MenuItemFactory {

    public static final String RADIO_MENU = "RadioMenuItem";
    public static final String CHECK_MENU = "CheckMenuItem";
    public static final String DEFAULT_MENU = "MenuItem";

    public static MenuItem getInstance(String label, String type, Object actionHandler){
        switch (type) {
            case RADIO_MENU:
            case CHECK_MENU:
                if(actionHandler instanceof ChangeListener)
                    return getInstance(label, (ChangeListener)actionHandler);
                else
                    throw new IllegalArgumentException(String.format(MenuBarFactory.ERROR, actionHandler.getClass().toString(), "ChangeListener"));
            case DEFAULT_MENU:
                if(actionHandler instanceof EventHandler)
                    return getInstance(label, (ChangeListener) actionHandler);
                break;
            default:
                throw new IllegalArgumentException("Unrecognized menu item type: " + type);
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public static MenuItem getInstance(String label, EventHandler action) {
        MenuItem mi = new MenuItem(label);
        mi.setOnAction(action);
        return mi;
    }

    @SuppressWarnings("unchecked")
    public static MenuItem getInstance(String label, ChangeListener listener) {
        RadioMenuItem mi = new RadioMenuItem(label);
        mi.selectedProperty().addListener(listener);
        return mi;
    }

}
