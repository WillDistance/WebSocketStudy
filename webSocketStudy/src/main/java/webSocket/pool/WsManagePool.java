package webSocket.pool;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.java_websocket.WebSocket;
import org.java_websocket.exceptions.WebsocketNotConnectedException;

import webSocket.exception.WebSocketException;

import com.thinkive.base.util.StringHelper;

/**
 * 
 * @����: WebSocket���ӹ����
 * @��Ȩ: Copyright (c) 2019 
 * @��˾: ˼�ϿƼ� 
 * @����: ����
 * @�汾: 1.0 
 * @��������: 2019��9��23�� 
 * @����ʱ��: ����10:19:30
 */
public class WsManagePool
{
    
    private static Logger                       logger  = Logger.getLogger(WsManagePool.class);
    
    /**
     * ���ӱ�ʶ�����ӹ�ϵӳ��Map ws-connId
     */
    private static final Map<WebSocket, String> wsMap   = new ConcurrentHashMap<WebSocket, String>();

    /**
     * connId-roomId
     */
    private static final Map<String, String>    roomMap = new ConcurrentHashMap<String, String>();
    
    /**
     * �������ʱ��
     */
    private static long                         sleep   = 300000;
    
    /**
     * 
     * @����������WebSocket���ӳع���
     * @���ߣ�����
     * @ʱ�䣺2019��9��23�� ����4:24:57
     */
    public static void startPool()
    {
        Thread t = new Thread()
        {
            public void run()
            {
              //��ѯ�������
                while(true)
                {
                    Set<WebSocket> wss = wsMap.keySet();
                    for (WebSocket ws : wss)
                    {
                        try
                        {
                            if(ws.isOpen())
                            {
                                ws.sendPing();
                            }
                            else
                            {
                                removeWebSocket("", "", ws);
                            }
                        }
                        catch (WebsocketNotConnectedException e)
                        {
                            logger.error("����������ping֡����ʧ�ܣ������Ѿ��ر�");
                            removeWebSocket("", "", ws);
                        }
                    }
                    
                    try
                    {
                        Thread.sleep(sleep);
                    }
                    catch (InterruptedException e)
                    {
                        logger.error("�߳����߳����쳣",e);
                    }
                }
            }
        };
        t.start();
    }
    
    /**
     * 
     * @������ͨ��websocket���ӻ�ȡ���Ӧ�����ӱ�ʶ
     * @���ߣ�����
     * @ʱ�䣺2019��9��23�� ����9:56:10
     * @param conn
     * @return
     */
    public static String getConnIdByWs(WebSocket conn)
    {
        if ( conn == null )
        {
            throw new WebSocketException("conn����Ϊ��");
        }
        return wsMap.get(conn);
    }
    
    /**
     * 
     * @������ͨ��websocket���ӻ�ȡ���Ӧ�ķ����ʶ
     * @���ߣ�����
     * @ʱ�䣺2019��9��23�� ����9:56:10
     * @param conn
     * @return
     */
    public static String getRoomIdByWs(WebSocket conn)
    {
        if ( conn == null )
        {
            throw new WebSocketException("conn����Ϊ��");
        }
        String connId = wsMap.get(conn);
        if(StringHelper.isNotBlank(connId))
        {
            return roomMap.get(connId);
        }
        return null;
    }
    
    /**
     * 
     * @�������������ӱ�ʶ��ȡWebSocket��connIdΪ������ȡroomId�����е�����
     * @���ߣ�����
     * @ʱ�䣺2019��9��23�� ����11:13:47
     * @param roomId
     * @param connId
     * @return
     */
    public static Set<WebSocket> getWsByRoomIdAndConnId(String roomId, String connId)
    {
        Set<WebSocket> wss = new HashSet<WebSocket>();
        if ( StringHelper.isBlank(roomId) )
        {
            //roomIdΪ����ȡ�� connIdָ�������
            if(StringHelper.isBlank(connId))
            {
                throw new WebSocketException("roomId��connId����ͬʱΪ��");
            }
            Set<WebSocket> keySet = wsMap.keySet();
            for (WebSocket webSocket : keySet)
            {
                if(connId.equals(wsMap.get(webSocket)))
                {
                    wss.add(webSocket);
                }
            }
        }
        else
        {
            //ȡ��roomId���������coonId
            Set<String> connIds = new HashSet<String>();
            Set<String> keySet = roomMap.keySet();
            for (String key : keySet)
            {
                if(roomMap.get(key).equals(roomId))
                {
                    connIds.add(key);
                }
            }
            
            if(StringHelper.isBlank(connId))
            {
                //ȡ��roomId�µ���������
                for (WebSocket webSocket : wsMap.keySet())
                {
                    if(connIds.contains(wsMap.get(webSocket)))
                    {
                        wss.add(webSocket);
                    }
                }
            }
            else
            {
                //��roomId�µ�connIds����connId����ȡ��connId��ָ������
                if(connIds.contains(connId))
                {
                    for (WebSocket webSocket : wsMap.keySet())
                    {
                        if(connId.equals(wsMap.get(webSocket)))
                        {
                            wss.add(webSocket);
                        }
                    }
                }
            }
        }
        return wss;
    }
    
    /**
     * 
     * @�����������ӳ���������ӣ����������ӳ���ϵ
     * @���ߣ�����
     * @ʱ�䣺2019��9��23�� ����2:07:39
     * @param roomId
     * @param connId
     * @param conn
     * @return
     */
    public static boolean addOrUpdateWebSocket(String roomId, String connId, WebSocket conn)
    {
        if (conn == null )
        {
            throw new WebSocketException("conn����Ϊ��");
        }
        if((StringHelper.isNotBlank(roomId)&&StringHelper.isNotBlank(connId))||StringHelper.isBlank(roomId+connId))
        {
            if(StringHelper.isBlank(roomId+connId))
            {
                wsMap.put(conn, "");
            }
            else
            {
                roomMap.put(connId, roomId);
                wsMap.put(conn, connId);
            }
            return true;
        }
        else
        {
            throw new WebSocketException("roomId��connId����ͬʱΪ�ջ򶼲���Ϊ��");
        }
    }
    
    /**
     * 
     * @�������Ƴ�roomId�µĵ�������connId��ȫ������
     * @���ߣ�����
     * @ʱ�䣺2019��9��23�� ����2:07:31
     * @param roomId
     * @param connId
     * @return
     */
    public static void removeWebSocket(String roomId, String connId,WebSocket conn)
    {
        if(conn != null)
        {
            conn.close();
            wsMap.remove(conn);
        }
        else if ( StringHelper.isNotBlank(connId) )
        {
            for (WebSocket webSocket : wsMap.keySet())
            {
                if(connId.equals(wsMap.get(webSocket)))
                {
                    webSocket.close();
                    wsMap.remove(webSocket);
                }
            }
        }
        else if ( StringHelper.isNotBlank(roomId) )
        {
            //��ȡ��Ҫɾ��������id
            Set<String> connIds = new HashSet<String>();
            for (String key : roomMap.keySet())
            {
                if(roomMap.get(key).equals(roomId))
                {
                    connIds.add(key);
                }
            }
            //ɾ������
            for (WebSocket webSocket : wsMap.keySet())
            {
                if(connIds.contains(wsMap.get(webSocket)))
                {
                    webSocket.close();
                    wsMap.remove(webSocket);
                }
            }
        }
    }
    
    /**
     * 
     * @�������ж������Ƿ����
     * @���ߣ�����
     * @ʱ�䣺2019��9��23�� ����4:59:26
     * @param conn
     * @return
     */
    public static boolean webSocketExists(WebSocket conn)
    {
        return wsMap.containsKey(conn);
    }
    
}
