package org.programmingbasics.loom;

import java.io.File;
import java.net.MalformedURLException;

import com.google.gwt.core.shared.GwtIncompatible;
import com.user00.domjnate.api.Window;
import com.user00.domjnate.javafx.DomjnateFx;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

@GwtIncompatible
public class LoomPatternMakerJavaFx extends Application
{
  public void start(Stage stage) throws MalformedURLException {
    BorderPane border = new BorderPane();
    WebView webView = new WebView();
    border.setCenter(webView);
    Scene scene = new Scene(border);
    stage.setScene(scene);
    stage.show();

    WebEngine engine = webView.getEngine();
    engine.getLoadWorker().stateProperty().addListener(
        new ChangeListener<State>() {
            public void changed(ObservableValue ov, State oldState, State newState) {
                if (newState == State.SUCCEEDED) {
                  JSObject jsWin = (JSObject)engine.executeScript("window");
                  Window win = DomjnateFx.createJsBridgeGlobalsProxy(Window.class, jsWin);
                  new LoomPatternMaker(win).go();
                }
            }
        });
    engine.load(
        new File("src/main/webapp/LoomPatternMaker.html").toURI().toURL().toExternalForm());
  }
  
  public static void main(String[] args)
  {
    launch(args);
  }
}
