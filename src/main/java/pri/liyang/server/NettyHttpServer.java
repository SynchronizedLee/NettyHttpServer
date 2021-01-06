package pri.liyang.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import pri.liyang.handler.NettyHttpServerHandler;

// https://blog.csdn.net/sinat_39783636/article/details/81941476
public class NettyHttpServer {

    private int inetPort;

    public NettyHttpServer(int inetPort) {
        this.inetPort = inetPort;
    }

    public int getInetPort() {
        return inetPort;
    }

    public void init() throws Exception {
        EventLoopGroup parentGroup = new NioEventLoopGroup();
        EventLoopGroup childGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap server = new ServerBootstrap();
            server.group(parentGroup, childGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            // 请求解码器
                            socketChannel.pipeline().addLast("http-decoder", new HttpRequestDecoder());
                            // 将HTTP消息的多个部分合成一条完整的HTTP消息
                            socketChannel.pipeline().addLast("http-aggregator", new HttpObjectAggregator(65535));
                            // 响应转码器
                            socketChannel.pipeline().addLast("http-encoder", new HttpResponseEncoder());
                            // 解决大码流的问题，ChunkedWriteHandler：向客户端发送HTML5文件
                            socketChannel.pipeline().addLast("http-chunked", new ChunkedWriteHandler());
                            // 自定义处理handler
                            socketChannel.pipeline().addLast("http-server", new NettyHttpServerHandler());
                        }
                    });
            ChannelFuture future = server.bind(this.inetPort).sync();
            future.channel().closeFuture().sync();
        } finally {
            childGroup.shutdownGracefully();
            parentGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        NettyHttpServer server = new NettyHttpServer(8888);
        try {
            server.init();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("exception: " + e.getMessage());
        }
        System.out.println("server close!");
    }

}
