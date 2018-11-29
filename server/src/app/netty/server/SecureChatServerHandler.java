package app.netty.server;

import java.net.InetAddress;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.GlobalEventExecutor;

public class SecureChatServerHandler extends SimpleChannelInboundHandler<ByteBuf> {

	static final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		Channel incoming = ctx.channel();
		System.out.println("[CONNECTED]" + incoming.remoteAddress());
	}

	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		Channel incoming = ctx.channel();
		System.out.println("[DISCONNECTED]" + incoming.remoteAddress());
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		System.out.println("[READ_COMPLETE]");
		ctx.flush();
	}

	@Override
	public void channelActive(final ChannelHandlerContext ctx) {
		// Once session is secured, send a greeting and register the channel to
		// the global channel
		// list so the channel received the messages from others.
		ctx.pipeline().get(SslHandler.class).handshakeFuture()
				.addListener(new GenericFutureListener<Future<Channel>>() {
					public void operationComplete(Future<Channel> future) throws Exception {
						ctx.writeAndFlush(Unpooled.copiedBuffer(
								"Welcome to " + InetAddress.getLocalHost().getHostName() + " secure chat service!\n",
								CharsetUtil.UTF_8));
						ctx.writeAndFlush(
								Unpooled.copiedBuffer(
										"Your session is protected by " + ctx.pipeline().get(SslHandler.class).engine()
												.getSession().getCipherSuite() + " cipher suite.\n",
										CharsetUtil.UTF_8));

						channels.add(ctx.channel());
					}
				});
		ctx.channel().closeFuture().addListener(f -> System.out.println("[CLOSE]"));
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
		// Send the received message to all channels but the current one.
		final String msg = in.toString(CharsetUtil.UTF_8);
		for (Channel c : channels) {

			if (c != ctx.channel()) {
				c.write(Unpooled.copiedBuffer("[" + ctx.channel().remoteAddress() + "] " + msg + '\n',
						CharsetUtil.UTF_8));
			} else {
				c.write(Unpooled.copiedBuffer("[you] " + msg + '\n', CharsetUtil.UTF_8));
			}
		}

		// Close the connection if the client has sent 'bye'.
		if ("bye".equals(msg.toLowerCase())) {
			ctx.close();
		}
	}
}
