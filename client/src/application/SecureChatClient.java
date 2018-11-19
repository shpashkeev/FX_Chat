/**
 * 
 * @author Aleksandr Pashkeev
 *
 */
package application;

import javax.net.ssl.SSLException;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Simple FX chat client.
 */
public class SecureChatClient extends Application {

	// constants
	public static String Host = "localhost";
	public static String Port = "8007";
	
	private SslContext sslCtx;
	private Stage primaryStage;
	
	public SslContext getSslCtx() {
		return sslCtx;
	}

	public Stage getPrimaryStage() {
		return primaryStage;
	}

	
	public static void main(String[] args) throws Exception {
		launch(args);
	}
	
	public void start(Stage primaryStage) throws Exception {
		
		FXMLLoader fxmlLoader = new FXMLLoader(SecureChatClient.class.getResource("/ChatClient.fxml"));
		
		Parent p = fxmlLoader.load();
		
		Scene scene = new Scene(p);
		
		this.setClient(fxmlLoader);

		primaryStage.setScene( scene );
		primaryStage.setTitle("Secure Chat Client");
		primaryStage.setWidth(320);
		primaryStage.setHeight(568);
		
		primaryStage.show();
	}
	
	private void setClient(FXMLLoader fxmlLoader) throws SSLException{
        
		// Configure SSL.
        this.sslCtx = SslContextBuilder.forClient()
            .trustManager(InsecureTrustManagerFactory.INSTANCE).build();
		SecureChatClientController controller = fxmlLoader.getController();
		controller.setClient(this);
	}
}
