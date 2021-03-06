package all.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

import java.util.concurrent.Executors;

public class NettyServer {
    private void start() {
        EventLoopGroup bossGroup = new EpollEventLoopGroup(0x1, Executors.newCachedThreadPool());//接收组，处理来访问服务器的客户的连接请求
        EventLoopGroup workGroup = new EpollEventLoopGroup(Runtime.getRuntime().availableProcessors() * 0X3, Executors.newCachedThreadPool());//工作组，实现数据的读写

        try {
            ServerBootstrap bootstrap=new ServerBootstrap();//服务端来设置通道参数的工具
            bootstrap.group(bossGroup, workGroup)//将两个工作线程与通道绑定
                    .channel(EpollServerSocketChannel.class)//指定NIO模式
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {//设定回调类
                            socketChannel.pipeline().addLast(new HttpResponseEncoder());//server端发送的是httpResponse,要进行编码

                            socketChannel.pipeline().addLast(new HttpRequestDecoder());//server端接收的是httpRequest,要进行解码

                            socketChannel.pipeline().addLast(new HttpObjectAggregator(65535));
                            //等待解码后的报文头和报文体一起扔给下一层
                            socketChannel.pipeline().addLast(new ChunkedWriteHandler());
                            socketChannel.pipeline().addLast(new Handler());
                            //        socketChannel.pipeline().addLast(new WebSocket());
                        }
                    })//设置回调
                    .option(ChannelOption.SO_BACKLOG, 128)//设置TCP缓冲区
                    .childOption(ChannelOption.SO_KEEPALIVE, true);//设置长连接
//172.31.247.4
            ChannelFuture future = bootstrap.bind("127.0.0.1", 8080).sync();//绑定端口
            System.out.println("oknetty");
            //阻止程序关闭
            future.channel().closeFuture().sync();

            System.out.println("xxx");


        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }

    }

    public static void main(String[] args) {
        new Thread(){
            @Override
            public void run() {
                NettyServer nettyServer = new NettyServer();
               /* EsUtil.getClient();
                System.out.println("okes");
                System.out.println(MybatisUtil.ready);
                System.out.println("okMybatisUtil");
                RedisUtil.getjedis();
                System.out.println("okRedisUtil");*/
                nettyServer.start();

            }
        }.start();



    }
}
