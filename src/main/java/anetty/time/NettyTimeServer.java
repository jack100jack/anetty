package anetty.time;

//package com.netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
 
import java.util.Date;
 
 
/**
 * Created by lcq on 12/5/2016.
 */
public class NettyTimeServer {
 
    public static void main(String[] args) {
        int port = 8090;
        try {
            new NettyTimeServer().bind(port);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
 
    private void bind(int port) throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        //以上两个group就是Reactor线程组，第一个是用于服务端接受客户端的连接，第二个是进行SocketChannel网络读写
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup,workerGroup).channel(NioServerSocketChannel.class).option(ChannelOption.SO_BACKLOG,1024).childHandler(new ChildHandler());
            ChannelFuture f = null;
            f = b.bind(port).sync();
            f.channel().closeFuture().sync();
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
 
    /**
     * ChildHandler 用于处理IO事件
     */
    @Sharable
    private class ChildHandler extends ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel socketChannel) throws Exception {
            socketChannel.pipeline().addLast(new TimeServerHandler());
        }
 
        private class TimeServerHandler extends ChannelHandlerAdapter {
            //@Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                ByteBuf buf = (ByteBuf) msg;
                byte[] req = new byte[buf.readableBytes()];
                buf.readBytes(req);
                String body = new String(req,"UTF-8");
                System.out.println("time server receive order : " + body);
                String currentTime = "QUERY TIME ORDER".equalsIgnoreCase(body) ? new Date(System.currentTimeMillis()).toString() : "BAD ORDER";
                ByteBuf resp = Unpooled.copiedBuffer(currentTime.getBytes());
                ctx.write(resp);
            }
 
            //@Override
            public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
                ctx.flush();
            }
 
            @Override
            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                ctx.close();
            }
        }
    }
}
