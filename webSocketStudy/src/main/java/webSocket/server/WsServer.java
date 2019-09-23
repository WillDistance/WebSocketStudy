package webSocket.server;

import java.net.InetSocketAddress;
import java.util.Set;

import org.apache.log4j.Logger;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import webSocket.pool.WsManagePool;

import com.thinkive.base.util.StringHelper;

/**
 * 
 * @����: WebSocket�����
 * @��Ȩ: Copyright (c) 2019 
 * @��˾: ˼�ϿƼ� 
 * @����: ����
 * @�汾: 1.0 
 * @��������: 2019��9��23�� 
 * @����ʱ��: ����4:38:00
 */
public class WsServer extends WebSocketServer
{
    private static Logger logger = Logger.getLogger(WsServer.class);
    
    private static int default_port = 8887;
    
    private static int state = 0;//����״̬ 0���ر�  1������
    
    public WsServer(int port)
    {
        super(new InetSocketAddress(port));
    }
    
    private static class WsServerInstance {
        private static final WsServer server = new WsServer(default_port);
    }

    public static WsServer getInstance() {
        return WsServerInstance.server;
    }
    
    /**
     * 
     * @��������������
     * @���ߣ�����
     * @ʱ�䣺2019��9��23�� ����5:56:46
     */
    public void startServer(){
        if(state == 0)
        {
            getInstance().start();
        }
    }
    
    /**
     * 
     * @������1.4�������󷽷�   �ڷ������ɹ�����ʱ���á���������κδ������Ϊ����onError��
     * @���ߣ�����
     * @ʱ�䣺2019��9��20�� ����11:04:12
     */
    @Override
    public void onStart()
    {
        logger.info("onStart:����WebSocket���񣬼����˿ڣ�"+getInstance().getPort());
        WsManagePool.startPool();
    }
    
    /**
     * 
     * @�������ͻ�������
     * @���ߣ�����
     * @ʱ�䣺2019��9��23�� ����4:17:08
     * @param conn
     * @param handshake
     */
    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake)
    {
        
        logger.info("onOpen:�ͻ������ӵ�ʱ�����");
        WsManagePool.addOrUpdateWebSocket("", "", conn);
    }
    
    /**
     * 
     * @������
     * @���ߣ�����
     * @ʱ�䣺2019��9��23�� ����4:17:29
     * @param conn
     * @param code
     * @param reason
     * @param remote
     */
    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote)
    {
        //�Ͽ�����ʱ�򴥷�����
        logger.info("�Ͽ�����");
        WsManagePool.removeWebSocket("", "", conn);
    }
    
    /**
     * 
     * @���������տͻ�����Ϣ
     * @���ߣ�����
     * @ʱ�䣺2019��9��23�� ����4:17:39
     * @param conn
     * @param message
     */
    @Override
    public void onMessage(WebSocket conn, String message)
    {
        /*****�������ݽ�Ϊ����ʹ��*****/
        //online_room01_conn01
        if ( null != message && message.startsWith("online") )
        {
            logger.info("onMessage���տͻ��˴���������Ϣ: " + message+",WebSocket:"+conn);
            
            //�������Ӳ�������Ϣ
            String[] split = message.split("_");
            if ( split.length == 3 )
            {
                WsManagePool.addOrUpdateWebSocket(split[1], split[2], conn);
            }
            else
            {
                WsManagePool.addOrUpdateWebSocket("", "", conn);
            }
        }
        else if ( null != message && message.startsWith("offline") )
        {
            logger.info("onMessage���տͻ���ɾ��������Ϣ: " + message+",WebSocket:"+conn);
            //�ر����Ӳ�������Ϣ
            String[] split = message.split("_");
            if ( split.length == 3 )
            {
                WsManagePool.removeWebSocket(split[1], split[2], conn);
            }
            else
            {
                WsManagePool.removeWebSocket("", "", conn);
            }
        }
        else
        {
            //�������� ������Ϣ
            if(WsManagePool.webSocketExists(conn))
            {
                System.out.println("�����"+WsManagePool.getRoomIdByWs(conn)+",����id"+WsManagePool.getConnIdByWs(conn)+"��Ϣ���ݣ�"+message);
            }
            else
            {
                logger.error("���Ӳ�������");
            }
            
        }
    }
    
    @Override
    public void onError(WebSocket conn, Exception ex)
    {
        //����ʱ�򴥷��Ĵ���
        logger.error("onError:�����쳣ʱ������", ex);
    }
    
    /**
     * 
     * @��������ͻ��˷�������
     * @���ߣ�����
     * @ʱ�䣺2019��9��23�� ����2:14:23
     * @param roomId
     * @param connId
     * @param message
     * @return
     */
    public boolean sendMessage(Set<WebSocket> conns, String message)
    {
        if ( StringHelper.isBlank(message) || conns.size() == 0 )
        {
            logger.error("message����Ϊ�ջ���connsΪ��");
            return false;
        }
        for (WebSocket conn : conns)
        {
            conn.send(message);
        }
        return true;
    }
    
}
