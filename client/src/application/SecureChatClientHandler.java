/**
 * 
 * @author Aleksandr Pashkeev
 *
 */
package application;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;
import javafx.application.Platform;
import javafx.collections.ObservableList;

/**
 * Handles a client-side channel. ByteBuf used for increase performance.
 */
public class SecureChatClientHandler extends SimpleChannelInboundHandler<ByteBuf> {

	// binds ui messages displaying with received messages from server
	private final ObservableList<String> receivingMessagesModel;

	public SecureChatClientHandler(ObservableList<String> receivingMessagesModel) {
		this.receivingMessagesModel = receivingMessagesModel;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext arg0, ByteBuf in) throws Exception {
		final String cm = in.toString(CharsetUtil.UTF_8);
		Platform.runLater(() -> receivingMessagesModel.add(cm));
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}
}
