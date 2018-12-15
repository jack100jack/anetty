package anetty.time;

//package com.netty.server;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
 
 
/**
 * Created by lcq on 12/5/2016.
 */
public class NettyTimeClient {
    public static void main(String[] args) {
        int port = 8090;
        try {
            new NettyTimeClient().connect(port, "127.0.0.1");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
 
    private void connect(int port, String host) throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true).handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel sc) throws Exception {
                    sc.pipeline().addLast(new TimeClientHandler());
                }
            });
            //对比server端的启动：绑定的是SocketChannel，而不是ServerSocketChannel
 
            ChannelFuture f = b.connect(host,port).sync();
            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }
    @Sharable
    private class TimeClientHandler extends ChannelHandlerAdapter {
        private ByteBuf firtMessage;
 
        public TimeClientHandler(){
            byte[] req = "QUERY TIME ORDER".getBytes();
            firtMessage = Unpooled.buffer(req.length);
            firtMessage.writeBytes(req);
        }
 
 
        //@Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            ctx.writeAndFlush(firtMessage);
        }
 
        //@Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            ByteBuf buf = (ByteBuf) msg;
            byte[] req = new byte[buf.readableBytes()];
            buf.readBytes(req);
            String body = new String(req,"UTF-8");
            System.out.println("Now is : " + body);
        }
 
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            ctx.close();
        }
    }
}

