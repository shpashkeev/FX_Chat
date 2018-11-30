/**
 * 
 * @author Aleksandr Pashkeev
 *
 */
package application;

import java.net.URISyntaxException;

import javax.net.ssl.SSLException;

import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Simple FX chat client.
 */
public class SecureChatClient extends Application {

	// default connection values
	public static String Host = "localhost";
	public static String Port = "8007";

	private Stage primaryStage;

	public Stage getPrimaryStage() {
		return primaryStage;
	}

	// netty entities
	private SslContext sslCtx;

	public SslContext getSslCtx() {
		return sslCtx;
	}

	private Channel channel;

	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}

	private EventLoopGroup group;

	public EventLoopGroup getGroup() {
		return group;
	}

	public void setGroup(EventLoopGroup group) {
		this.group = group;
	}

	public BooleanProperty connected = new SimpleBooleanProperty(false);

	public static void main(String[] args) throws Exception {
		launch(args);
	}

	public void start(Stage primaryStage) throws Exception {

		FXMLLoader fxmlLoader = new FXMLLoader(SecureChatClient.class.getResource("/ChatClient.fxml"));

		Parent p = fxmlLoader.load();

		Scene scene = new Scene(p);

		this.setClient(fxmlLoader);

		primaryStage.setScene(scene);
		primaryStage.setTitle("Secure Chat Client");

		primaryStage.setMinWidth(300);
		primaryStage.setMinHeight(530);

		primaryStage.show();
	}

	private void setClient(FXMLLoader fxmlLoader) throws SSLException, URISyntaxException {

		// Configure SSL.
		this.sslCtx = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
		SecureChatClientController controller = fxmlLoader.getController();
		controller.setClient(this);
		controller.handleConnect();
	}
}
