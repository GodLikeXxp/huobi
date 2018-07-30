package org.yyx.message.push.push.client.handler;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yyx.message.push.push.client.util.Test;
import org.yyx.message.push.push.client.util.WebSocketUsers;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.util.CharsetUtil;

/**
 * WebSocketClientHandler
 * <p>
 * create by 叶云轩 at 2018/5/21-下午6:10
 * contact by tdg_yyx@foxmail.com
 */
public class WebSocketClientHandler extends SimpleChannelInboundHandler<Object> {
    /**
     * WebSocketClientHandler 日志控制器
     * Create by 叶云轩 at 2018/5/17 下午6:10
     * Concat at tdg_yyx@foxmail.com
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketClientHandler.class);

    private final WebSocketClientHandshaker webSocketClientHandshaker;

    private ChannelPromise handshakeFuture;

    public WebSocketClientHandler(WebSocketClientHandshaker webSocketClientHandshaker) {
        this.webSocketClientHandshaker = webSocketClientHandshaker;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        handshakeFuture = ctx.newPromise();
    }

    /**
     * 异常
     *
     * @param channelHandlerContext channelHandlerContext
     * @param cause                 异常
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable cause) throws Exception {
        LOGGER.info("\n\t⌜⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓\n" +
                "\t├ [exception]: {}\n" +
                "\t⌞⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓", cause.getMessage());
        // 移出通道
        WebSocketUsers.remove(channelHandlerContext.channel());
        channelHandlerContext.close();
    }

    /**
     * 当客户端主动链接服务端的链接后，调用此方法
     *
     * @param channelHandlerContext ChannelHandlerContext
     */
    @Override
    public void channelActive(ChannelHandlerContext channelHandlerContext) throws Exception {
        LOGGER.info("\n\t⌜⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓\n" +
                "\t├ [建立连接]\n" +
                "\t⌞⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓");

        Channel channel = channelHandlerContext.channel();
        channelHandlerContext.writeAndFlush("{\n" + 
        		"  'req': 'market.ethbtc.kline.1min',\n" + 
        		"  'id': 'id10'\n" + 
        		"}");
        // 握手
        webSocketClientHandshaker.handshake(channel);
        
        String text = "啊啊啊啊啊啊啊啊啊";
		byte[] byteMsg = text.getBytes(); 
		ByteBuf msg = Unpooled.buffer(byteMsg.length);
		msg.writeBytes(byteMsg);
		String s="{\n" + 
		        "  \"sub\": \"market.btcusdt.kline.1min\",\n" + 
		        "  \"id\": \"48178331\"\n" + 
		        "}";//
		channelHandlerContext.writeAndFlush(s);
		channelHandlerContext.writeAndFlush(msg);
		System.out.println("发送消息：" + text);
    }

    /**
     * 与服务端断开连接时
     *
     * @param channelHandlerContext channelHandlerContext
     */
    @Override
    public void channelInactive(ChannelHandlerContext channelHandlerContext) {
        Channel channel = channelHandlerContext.channel();
//        // 移出通道
        LOGGER.info("\n\t⌜⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓\n" +
                "\t├ [断开连接]：client [{}]\n" +
                "\t⌞⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓", channel.remoteAddress());

    }

    /**
     * 读完之后调用的方法
     *
     * @param channelHandlerContext ChannelHandlerContext
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext channelHandlerContext) throws Exception {
        channelHandlerContext.flush();
    }

    /**
     * 接收消息
     *
     * @param channelHandlerContext channelHandlerContext
     * @param msg                   msg
     */
    @Override
    protected void messageReceived(ChannelHandlerContext channelHandlerContext, Object msg) throws Exception {
        System.err.println("msg:"+msg);
        System.err.println("1");
    	System.err.println(msg.getClass().getSimpleName());
    	if (msg instanceof BinaryWebSocketFrame) {
    		System.out.println(msg.toString());
    	}
        Channel channel = channelHandlerContext.channel();
        if (!webSocketClientHandshaker.isHandshakeComplete()) {
            webSocketClientHandshaker.finishHandshake(channel, (FullHttpResponse) msg);
            handshakeFuture.setSuccess();
            // 将当前登陆用户保存起来
            WebSocketUsers.put(getUserNameInPath(), channel);
            return;
        }
        channelHandlerContext.flush();

        if (msg instanceof FullHttpResponse) {
            FullHttpResponse response = (FullHttpResponse) msg;
            throw new IllegalStateException(
                    "Unexpected FullHttpResponse (getStatus=" + response.status() +
                            ", content=" + response.content().toString(CharsetUtil.UTF_8) + ')');
        }
    }

    public static void decompress(InputStream is, OutputStream os) throws Exception {  
        GZIPInputStream gis = new GZIPInputStream(is);  
        int count;  
        byte data[] = new byte[1024];  
        while ((count = gis.read(data, 0, 1024)) != -1) {  
            os.write(data, 0, count);  
        }  
        gis.close();  
    }  
    /**
     * 获取登陆用户的方法
     *
     * @return 用户名
     */
    private String getUserNameInPath() {
        String path = webSocketClientHandshaker.uri().getPath();
        int i = path.lastIndexOf("/");
        return path.substring(i + 1, path.length());
    }
}

