package webSocket;

import java.util.Scanner;
import java.util.Set;

import org.java_websocket.WebSocket;

import webSocket.pool.WsManagePool;
import webSocket.server.WsServer;

public class testMain
{
    /**
     * 
     * @描述：测试文档
     * 测试服务端地址：http://www.websocket-test.com/
     * 
     * 输入：创建/删除_房间号_连接id
     *     online_room01_conn01
     *     offline_room01_conn01
     * 控制台通过输入房间号 和 连接id 将消息发送到客户端
     * 
     * WsServer.onMessage() 接收客户端消息的方法为测试使用，需要根据业务需求修改方法实现
     */
    public static void main(String args[]){
        System.out.println("测试启动WebSocket服务端");
        WsServer server = WsServer.getInstance();
        server.startServer();
        while(true)
        {
            System.out.println();
            Scanner input=new Scanner(System.in);
            System.out.println("输入roomId");
            String roomId = input.nextLine();
            System.out.println("输入connId");
            String connId = input.nextLine();
            Set<WebSocket> wsByRoomIdAndConnId = WsManagePool.getWsByRoomIdAndConnId(roomId, connId);
            System.out.println("输入message");
            String message = input.nextLine();
            server.sendMessage(wsByRoomIdAndConnId, message);
        }
    }
}
