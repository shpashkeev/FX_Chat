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
import javafx.beans.property.StringProperty;

/**
 * Handles a client-side channel.
 * ByteBuf used for increase performance.
 */
public class SecureChatClientHandler extends SimpleChannelInboundHandler<ByteBuf> {

	private final StringProperty receivingMessageModel;
	
	public SecureChatClientHandler(StringProperty receivingMessageModel) {
		this.receivingMessageModel = receivingMessageModel;
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext arg0, ByteBuf in) throws Exception {
		final String cm = in.toString(CharsetUtil.UTF_8);
		Platform.runLater( () -> receivingMessageModel.set(cm) );
	}
	
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
