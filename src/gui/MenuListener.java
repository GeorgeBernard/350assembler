package gui;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class MenuListener<T> implements ChangeListener<T> {

    private T currentState;

    @Override
    public void changed(ObservableValue<? extends T> observable, T oldValue, T newValue) {
        currentState = newValue;
    }

    public void setDefaultState(T state){
        if(currentState == null)
            currentState = state;
    }

    public T getCurrentState(){
        return currentState;
    }
}
