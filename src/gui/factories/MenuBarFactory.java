package gui.factories;

import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class MenuBarFactory {

    public static final String BREAK = "BREAK";
    public static final String MENU_ITEM = "menuItem";
    public static final String MENU = "menu";
    public static final String ERROR = "Bad argument list -- %s is not of type %s";

    public static MenuBar getInstance(){
        return new MenuBar();
    }

    @SuppressWarnings("unchecked")
    public static MenuBar getInstance(List<String> menuTree, List<ChangeListener<Boolean>> listeners,
                                      List<EventHandler<ActionEvent>> handlers){
        Stack<Menu> s = new Stack<>();
        MenuBar mb = new MenuBar();
        int listenerDex = 0;
        int handlerDex = 0;
        // Do a DFS for nested menus
        for (int i = 0; i < menuTree.size(); ++i) {
            String enty = menuTree.get(i);
            switch (enty){
                case MENU:
                    String label = menuTree.get(++i);
                    Menu m = MenuFactory.getInstance(label);
                    if(s.isEmpty()) // At top level, add this menu directly to the bar
                        mb.getMenus().add(m);
                    else
                        s.peek().getItems().add(m);
                    s.push(m);
                    break;
                case MENU_ITEM:
                    String type = menuTree.get(++i);
                    String label1 = menuTree.get(++i);
                    MenuItem mi;
                    if(MenuItemFactory.DEFAULT_MENU.equals(type))
                        mi = MenuItemFactory.getInstance(label1, handlers.get(handlerDex++));
                    else
                        mi = MenuItemFactory.getInstance(label1, listeners.get(listenerDex++));
                    s.peek().getItems().add(mi);
                    break;
                case BREAK:
                    s.pop();
                    break;
                default: throw new IllegalArgumentException("Unrecognized menu type");
            }
        }
        return mb;
    }

    private static void addMenus(List<String> menuTitles, List<List<String>> subMenuLabels,
                                 List<List<EventHandler<ActionEvent>>> subMenuActions, MenuBar mb) {
        for(int i = 0; i < subMenuLabels.size(); ++i){
            String menuTitle = menuTitles.get(i);
            var labels = subMenuLabels.get(i);
            var actions = subMenuActions.get(i);
            mb.getMenus().add(MenuFactory.getInstance(menuTitle, labels, actions));
        }
    }
}
