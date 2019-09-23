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
     * @�����������ĵ�
     * ���Է���˵�ַ��http://www.websocket-test.com/
     * 
     * ���룺����/ɾ��_�����_����id
     *     online_room01_conn01
     *     offline_room01_conn01
     * ����̨ͨ�����뷿��� �� ����id ����Ϣ���͵��ͻ���
     * 
     * WsServer.onMessage() ���տͻ�����Ϣ�ķ���Ϊ����ʹ�ã���Ҫ����ҵ�������޸ķ���ʵ��
     */
    public static void main(String args[]){
        System.out.println("��������WebSocket�����");
        WsServer server = WsServer.getInstance();
        server.startServer();
        while(true)
        {
            System.out.println();
            Scanner input=new Scanner(System.in);
            System.out.println("����roomId");
            String roomId = input.nextLine();
            System.out.println("����connId");
            String connId = input.nextLine();
            Set<WebSocket> wsByRoomIdAndConnId = WsManagePool.getWsByRoomIdAndConnId(roomId, connId);
            System.out.println("����message");
            String message = input.nextLine();
            server.sendMessage(wsByRoomIdAndConnId, message);
        }
    }
}
