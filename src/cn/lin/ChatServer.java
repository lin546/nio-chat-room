package cn.lin;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class ChatServer {
    public static void main(String[] args) throws IOException {
        Selector selector = Selector.open();
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(8080));
        serverSocketChannel.configureBlocking(false);

        //将accept事件绑定到selector上
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        while (true){
            //阻塞在select上
            selector.select();
            Set<SelectionKey> selectionKeys = selector.selectedKeys();

            //遍历selectKeys
            Iterator<SelectionKey> iterator = selectionKeys.iterator();

            while (iterator.hasNext()){
                SelectionKey selectionKey = iterator.next();

                //如果是accept事件
                if (selectionKey.isAcceptable()){
                    ServerSocketChannel ssc = (ServerSocketChannel) selectionKey.channel();
                    SocketChannel socketChannel = ssc.accept();
                    System.out.println("accept new connection:"+socketChannel.getRemoteAddress());
                    socketChannel.configureBlocking(false);
                    socketChannel.register(selector,SelectionKey.OP_READ);
                    //加入群聊
                    ChatHolder.join(socketChannel);
                }else if(selectionKey.isReadable()){
                    //如果是读取事件
                    SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                    ByteBuffer  buffer = ByteBuffer.allocate(1024);

                    //将数据读入到buffer中
                    int length = socketChannel.read(buffer);
                    if(length > 0){
                        buffer.flip();
                        byte[] bytes = new byte[buffer.remaining()];
                        //将数据读入到byte数组中
                        buffer.get(bytes);

                        //换行符会跟着消息一起传过来
                        String content = new String(bytes,"UTF-8").replace("\r\n","");
                        if (content.equalsIgnoreCase("quit")){
                            //退出群聊
                            ChatHolder.quit(socketChannel);
                            selectionKey.cancel();
                            socketChannel.close();
                        }else {
                            //扩散
                            ChatHolder.propagate(socketChannel,content);
                        }
                    }

                }
                iterator.remove();
            }
        }
    }
}
