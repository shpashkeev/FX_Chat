/**
 * 
 * @author Aleksandr Pashkeev
 *
 */
package application;

import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Optional;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;
import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;
import javafx.scene.control.Alert.AlertType;

/**
 * 
 * Class-controller for UI form.
 */
public class SecureChatClientController {

	@FXML
	private MenuItem itemConnect;
	
	@FXML
	private MenuItem itemDisconnect;
	
	@FXML
	private ListView<String> lvMessages;
	
	@FXML
	private TextField tfMessage;
	
	@FXML
	private Button btnSend;
	
	private SecureChatClient client;
	
	private ObservableList<String> receivingMessageModel = FXCollections.observableArrayList(new ArrayList<String>());

	protected ListProperty<String> listProperty = new SimpleListProperty<>();
	
	public void setClient(SecureChatClient client){
		this.client = client;
	}
	
	
	@FXML
	public void initialize() {
		listProperty.set(receivingMessageModel);
		lvMessages.itemsProperty().bind(listProperty);		
	}
	
	@FXML
	public void send() throws Exception {
		
		final String toSend = tfMessage.getText();
		
		Task<Void> task = new Task<Void>() {

			@Override
			protected Void call() throws Exception {

				ChannelFuture f = client.getChannel()
						.writeAndFlush(Unpooled.copiedBuffer(toSend, CharsetUtil.UTF_8));
				f.sync();

				if (!f.isSuccess()) {
				    f.cause().printStackTrace();
				} 
				
				return null;
			}
				
			@Override
			protected void failed() {
				
				Throwable exc = getException();
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Client");
				alert.setHeaderText( exc.getClass().getName() );
				alert.setContentText( exc.getMessage() );
				alert.showAndWait();
				
				client.connected.set(false);
			}

		};
		
		new Thread(task).start();
		tfMessage.clear();
	}
	
	@FXML
	public void handleConnect() throws URISyntaxException {
		
		Boolean connect = this.showConnectDialog();
		
		if(connect){
			String host = SecureChatClient.Host;
			int port = Integer.parseInt(SecureChatClient.Port);
			
			this.client.setGroup(new NioEventLoopGroup());
					  
			Task<Channel> task = new Task<Channel>() {

				@Override
				protected Channel call() throws Exception {
					
					updateMessage("Bootstrapping");
					updateProgress(0.1d, 1.0d);
					
					Bootstrap b = new Bootstrap();
					b
						.group(client.getGroup())
						.channel(NioSocketChannel.class)
						.remoteAddress( new InetSocketAddress(host, port) )
						.handler( new ChannelInitializer<SocketChannel>() {
							@Override
							protected void initChannel(SocketChannel ch) throws Exception {
								ChannelPipeline p = ch.pipeline();

						        // Add SSL handler first to encrypt and decrypt everything.
						        // In this example, we use a bogus certificate in the server side
						        // and accept any invalid certificates in the client side.
						        // You will need something more complicated to identify both
						        // and server in the real world.
						        p.addLast(client.getSslCtx().newHandler(ch.alloc(), host, port));

						        // On top of the SSL handler, add the text line codec.
								p.addLast(new SecureChatClientHandler(receivingMessageModel));
							}
						});
					
					updateMessage("Connecting");
					updateProgress(0.2d, 1.0d);

					ChannelFuture f = b.connect();				
					f.sync();
					Channel chn = f.channel();

					return chn;
				}

				@Override
				protected void succeeded() {
					
					client.setChannel(getValue());
					client.connected.set(true);
				}

				@Override
				protected void failed() {
					
					Throwable exc = getException();
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("Client");
					alert.setHeaderText( exc.getClass().getName() );
					alert.setContentText( exc.getMessage() );
					alert.showAndWait();
					
					client.connected.set(false);
				}
			};
			
			itemConnect.visibleProperty().bind(client.connected.not());
			itemDisconnect.visibleProperty().bind(client.connected);
			btnSend.disableProperty().bind(client.connected.not());
			
			new Thread(task).start();
		}
	}
	
	@FXML
	public void handleDisconnect() {

		if (client.connected.get()) {
			Task<Void> task = new Task<Void>() {

				@Override
				protected Void call() throws Exception {

					updateMessage("Disconnecting");
					updateProgress(0.1d, 1.0d);

					client.getChannel().close().sync();

					updateMessage("Closing group");
					updateProgress(0.5d, 1.0d);
					client.getGroup().shutdownGracefully().sync();

					return null;
				}

				@Override
				protected void succeeded() {

					client.connected.set(false);
				}

				@Override
				protected void failed() {

					client.connected.set(false);

					Throwable t = getException();
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("Client");
					alert.setHeaderText(t.getClass().getName());
					alert.setContentText(t.getMessage());
					alert.showAndWait();

				}

			};
			
			itemConnect.visibleProperty().bind(client.connected.not());
			itemDisconnect.visibleProperty().bind(client.connected);
			btnSend.disableProperty().bind(client.connected.not());
			tfMessage.disableProperty().bind( client.connected.not() );

			new Thread(task).start();
		}
	}
	
	// Menu -> Close
	@FXML
	private void handleClose() throws Exception {
		this.handleDisconnect();
		client.stop();
		Platform.exit();
	}
	
	// Help -> About
	@FXML
	private void handleAbout() {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("About");
		alert.setHeaderText("");
		alert.setContentText("");
		alert.showAndWait();
	}
	
	// Show Connect dialog
	// returns allow/ not allow to connect
	private boolean showConnectDialog(){
		
		// Create the custom dialog.
		Dialog<Pair<String, String>> dialog = new Dialog<>();
		dialog.setTitle("Connect to Server");

		// Set the button types.
		dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

		// Create the host and port labels and fields.
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 150, 10, 10));

		TextField hostField = new TextField();
		TextField portField = new TextField();

		hostField.setText(SecureChatClient.Host);
		portField.setText(SecureChatClient.Port);

		grid.add(new Label("Host:"), 0, 0);
		grid.add(hostField, 1, 0);
		grid.add(new Label("Port"), 0, 1);
		grid.add(portField, 1, 1);

		// Enable/Disable ok button depending on whether a host was
		// entered.
		Node okButton = dialog.getDialogPane().lookupButton(ButtonType.OK);
		okButton.setAccessibleText("Connect");

		// Do some validation (using the Java 8 lambda syntax).
		hostField.textProperty().addListener((observable, oldValue, newValue) -> {
			okButton.setDisable(newValue.trim().isEmpty());
		});
		portField.textProperty().addListener((observable, oldValue, newValue) -> {
			okButton.setDisable(newValue.trim().isEmpty());
		});

		dialog.getDialogPane().setContent(grid);

		// Convert the result to a host-port-pair when the ok button
		// is clicked.
		dialog.setResultConverter(dialogButton -> {
			if (dialogButton == ButtonType.OK) {
				return new Pair<>(hostField.getText(), portField.getText());
			}
			return null;
		});
		
		Optional<Pair<String, String>> result = dialog.showAndWait();

		// update client's host and port
		result.ifPresent(hostPort -> {
			SecureChatClient.Host = hostPort.getKey();
			SecureChatClient.Port = hostPort.getValue();
		});

		// return flag to start connecting
		return result.isPresent();
	}
}
