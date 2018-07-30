package org.yyx.message.push.push.client.handler;

import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * WebSocketHandlerClient
 * <p>
 * create by 叶云轩 at 2018/5/21-下午6:11
 * contact by tdg_yyx@foxmail.com
 */
public class WebSocketHandlerClient extends ChannelInitializer<SocketChannel> {

    private WebSocketClientHandler webSocketClientHandler;

    public WebSocketHandlerClient(WebSocketClientHandler webSocketClientHandler) {
        this.webSocketClientHandler = webSocketClientHandler;
    }

    /**
     * 初始化Channel
     *
     * @param socketChannel socketChannel
     */
    @Override
    protected void initChannel(SocketChannel socketChannel) {
        ChannelPipeline pipeline = socketChannel.pipeline();

		try {
//			SSLEngine sslEngine = SSLContext.getDefault().createSSLEngine();
//	        sslEngine.setUseClientMode(true);
//	        pipeline.addLast("ssl", new SslHandler(sslEngine));
	        // 将请求与应答消息编码或者解码为HTTP消息
	        pipeline.addLast(new HttpClientCodec());
	        // 将http消息的多个部分组合成一条完整的HTTP消息
	        pipeline.addLast("aggregator", new HttpObjectAggregator(65536));
	        pipeline.addLast("http-chunked", new ChunkedWriteHandler());
	        // 客户端Handler
	        pipeline.addLast("handler", webSocketClientHandler);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
     
}