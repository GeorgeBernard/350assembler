import gui.GUI;
import javafx.application.Application;
import javafx.stage.Stage;


public class Main extends Application{

    public static void main(String[] args){
        launch(args);
    }

    @Override
    public void start(Stage s) throws Exception {
        GUI gui = new GUI(s);
    }
}
