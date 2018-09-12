package gui;


import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import java.util.ArrayList;
import java.util.List;

public class MenuListener implements ChangeListener<Boolean> {

    private Boolean currentState;
    private List<MenuListener> exclusives = new ArrayList<>();

    @Override
    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        currentState = newValue;
        enforceExclusives();

    }

    private void enforceExclusives() {
        for(MenuListener ml : exclusives){
            ml.update(!currentState);
        }
    }

    public void update(Boolean state){
        currentState = state;
    }

    public void setState(Boolean state){
        currentState = state;
        enforceExclusives();
    }

    public Boolean getCurrentState(){
        return currentState;
    }

    public void setExclusiveWith(MenuListener menuListener){
        exclusives.add(menuListener);
    }
}
