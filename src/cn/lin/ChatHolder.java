package cn.lin;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class ChatHolder {
    private static final Map<SocketChannel,String> USER_MAP = new ConcurrentHashMap<>();

    /**
     * 加入群聊
     * @param socketChannel
     */
    public static void join(SocketChannel socketChannel){
        //有人加入就给他分配一个id
        String userId = "用户"+ ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE);
        send(socketChannel,"您的Id为:"+userId+"\r\n");

        for (SocketChannel channel:USER_MAP.keySet()){
            send(channel,userId+"加入了群聊"+"\r\n");
        }
        USER_MAP.put(socketChannel,userId);
    }

    /**
     * 退出群聊
     * @param socketChannel
     */
    public static void quit(SocketChannel socketChannel) {
        String userId = USER_MAP.get(socketChannel);
        send(socketChannel,"您退出了群聊"+"\r\n");
        USER_MAP.remove(socketChannel);

        for (SocketChannel channel:USER_MAP.keySet()){
            if (channel != socketChannel){
                send(channel,userId+"退出了群聊"+"\r\n");
            }
        }
    }

    /**
     * 扩散说话的内容
     * @param socketChannel
     * @param content
     */
    public static void propagate(SocketChannel socketChannel, String content) {
        String userId = USER_MAP.get(socketChannel);
        for (SocketChannel channel:USER_MAP.keySet()){
            if (channel != socketChannel){
                send(channel,userId+":"+content+"\r\n");
            }
        }
    }

    /**
     * 发送消息
     * @param socketChannel
     * @param msg
     */
    private static void send(SocketChannel socketChannel,String msg){
        try {
            ByteBuffer writeBuffer = ByteBuffer.allocate(1024);
            writeBuffer.put(msg.getBytes());
            writeBuffer.flip();
            socketChannel.write(writeBuffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
