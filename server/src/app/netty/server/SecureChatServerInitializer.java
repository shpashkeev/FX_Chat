package app.netty.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslContext;

public class SecureChatServerInitializer extends ChannelInitializer<SocketChannel> {

	private final SslContext sslCtx;

	public SecureChatServerInitializer(SslContext sslCtx) {
		this.sslCtx = sslCtx;
	}

	@Override
	public void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();

		// Add SSL handler first to encrypt and decrypt everything.
		// In this example, we use a bogus certificate in the server side
		// and accept any invalid certificates in the client side.
		// You will need something more complicated to identify both
		// and server in the real world.
		pipeline.addLast(sslCtx.newHandler(ch.alloc()));

		// and then business logic.
		pipeline.addLast(new SecureChatServerHandler());
	}
}
