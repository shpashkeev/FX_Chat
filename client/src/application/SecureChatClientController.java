/**
 * 
 * @author Aleksandr Pashkeev
 *
 */
package application;

import java.net.InetSocketAddress;
import java.net.URISyntaxException;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;

/**
 * 
 * Class-controller for UI form.
 */
public class SecureChatClientController {

	@FXML
	MenuBar menuBar;
	
	@FXML
	TextArea taMessages;
	
	@FXML
	TextField tfMessage;
	
	@FXML
	Button btnSend;
	
	@FXML
	TextField tfHost;
	
	@FXML
	TextField tfPort;
	
	@FXML
	Button btnConnect;
	
	private SecureChatClient client;
	
	private BooleanProperty connected = new SimpleBooleanProperty(false);
	private StringProperty receivingMessageModel = new SimpleStringProperty("");
	private StringProperty outDataModel = new SimpleStringProperty("");
	private Channel channel;
	private EventLoopGroup group;
	
	public void setClient(SecureChatClient client){
		this.client = client;
	}
	
	
	@FXML
	public void initialize() {
		
		btnConnect.disableProperty().bind( connected );
		tfHost.disableProperty().bind( connected );
		tfPort.disableProperty().bind( connected );
		tfMessage.disableProperty().bind( connected.not() );
		//btnDisconnect.disableProperty().bind( connected.not() );
		btnSend.disableProperty().bind( connected.not() );

		taMessages.textProperty().bind(receivingMessageModel);
	}
	
	@FXML
	public void send() throws Exception {
		
		final String toSend = tfMessage.getText();
		
		Task<Void> task = new Task<Void>() {

			@Override
			protected Void call() throws Exception {

				ChannelFuture f = channel.writeAndFlush(Unpooled.copiedBuffer(toSend, CharsetUtil.UTF_8));
				f.sync();

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
				
				connected.set(false);
			}

		};
		
		new Thread(task).start();
		tfMessage.clear();
	}

	
	
	@FXML
	public void connect() throws URISyntaxException {
		
		String host = tfHost.getText();
		int port = Integer.parseInt(tfPort.getText());
		
		group = new NioEventLoopGroup();
				  
		Task<Channel> task = new Task<Channel>() {

			@Override
			protected Channel call() throws Exception {
				
				updateMessage("Bootstrapping");
				updateProgress(0.1d, 1.0d);
				
				Bootstrap b = new Bootstrap();
				b
					.group(group)
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
					        p.addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
					        p.addLast(new StringDecoder());
					        p.addLast(new StringEncoder());
							p.addLast(new SecureChatClientHandler(outDataModel));
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
				
				channel = getValue();
				connected.set(true);
			}

			@Override
			protected void failed() {
				
				Throwable exc = getException();
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Client");
				alert.setHeaderText( exc.getClass().getName() );
				alert.setContentText( exc.getMessage() );
				alert.showAndWait();
				
				connected.set(false);
			}
		};
		
		new Thread(task).start();
	}
	
	@FXML
	public void disconnect() {

		Task<Void> task = new Task<Void>() {

			@Override
			protected Void call() throws Exception {
				
				updateMessage("Disconnecting");
				updateProgress(0.1d, 1.0d);
				
				channel.close().sync();					

				updateMessage("Closing group");
				updateProgress(0.5d, 1.0d);
				group.shutdownGracefully().sync();

				return null;
			}

			@Override
			protected void succeeded() {
				
				connected.set(false);
			}

			@Override
			protected void failed() {
				
				connected.set(false);

				Throwable t = getException();
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Client");
				alert.setHeaderText( t.getClass().getName() );
				alert.setContentText( t.getMessage() );
				alert.showAndWait();

			}
			
		};

		new Thread(task).start();
	}
}
