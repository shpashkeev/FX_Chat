/**
 * 
 * @author Aleksandr Pashkeev
 *
 */
package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Simple FX chat client.
 */
public class SecureChatClient extends Application {

	public static void main(String[] args) throws Exception {
		launch(args);
	}
	
	public void start(Stage primaryStage) throws Exception {
		
		FXMLLoader fxmlLoader = new FXMLLoader(SecureChatClient.class.getResource("/FXChatClient.fxml"));
		
		Parent p = fxmlLoader.load();
		
		Scene scene = new Scene(p);
		
		primaryStage.setScene( scene );
		primaryStage.setTitle("FX Chat Client");
		primaryStage.setWidth(320);
		primaryStage.setHeight(568);
		
		primaryStage.show();
	}
}
