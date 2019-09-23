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
 * @描述: WebSocket连接管理池
 * @版权: Copyright (c) 2019 
 * @公司: 思迪科技 
 * @作者: 严磊
 * @版本: 1.0 
 * @创建日期: 2019年9月23日 
 * @创建时间: 上午10:19:30
 */
public class WsManagePool
{
    
    private static Logger                       logger  = Logger.getLogger(WsManagePool.class);
    
    /**
     * 连接标识与连接关系映射Map ws-connId
     */
    private static final Map<WebSocket, String> wsMap   = new ConcurrentHashMap<WebSocket, String>();

    /**
     * connId-roomId
     */
    private static final Map<String, String>    roomMap = new ConcurrentHashMap<String, String>();
    
    /**
     * 检测休眠时间
     */
    private static long                         sleep   = 300000;
    
    /**
     * 
     * @描述：启动WebSocket连接池管理
     * @作者：严磊
     * @时间：2019年9月23日 下午4:24:57
     */
    public static void startPool()
    {
        Thread t = new Thread()
        {
            public void run()
            {
              //轮询检查连接
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
                            logger.error("服务器发送ping帧发送失败，连接已经关闭");
                            removeWebSocket("", "", ws);
                        }
                    }
                    
                    try
                    {
                        Thread.sleep(sleep);
                    }
                    catch (InterruptedException e)
                    {
                        logger.error("线程休眠出现异常",e);
                    }
                }
            }
        };
        t.start();
    }
    
    /**
     * 
     * @描述：通过websocket连接获取其对应的连接标识
     * @作者：严磊
     * @时间：2019年9月23日 上午9:56:10
     * @param conn
     * @return
     */
    public static String getConnIdByWs(WebSocket conn)
    {
        if ( conn == null )
        {
            throw new WebSocketException("conn不能为空");
        }
        return wsMap.get(conn);
    }
    
    /**
     * 
     * @描述：通过websocket连接获取其对应的房间标识
     * @作者：严磊
     * @时间：2019年9月23日 上午9:56:10
     * @param conn
     * @return
     */
    public static String getRoomIdByWs(WebSocket conn)
    {
        if ( conn == null )
        {
            throw new WebSocketException("conn不能为空");
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
     * @描述：根据连接标识获取WebSocket，connId为空则会获取roomId下所有的连接
     * @作者：严磊
     * @时间：2019年9月23日 上午11:13:47
     * @param roomId
     * @param connId
     * @return
     */
    public static Set<WebSocket> getWsByRoomIdAndConnId(String roomId, String connId)
    {
        Set<WebSocket> wss = new HashSet<WebSocket>();
        if ( StringHelper.isBlank(roomId) )
        {
            //roomId为空则取出 connId指向的连接
            if(StringHelper.isBlank(connId))
            {
                throw new WebSocketException("roomId和connId不能同时为空");
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
            //取出roomId下面的所有coonId
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
                //取出roomId下的所有连接
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
                //若roomId下的connIds存在connId，则取出connId所指的连接
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
     * @描述：向连接池中添加连接，或更新连接映射关系
     * @作者：严磊
     * @时间：2019年9月23日 下午2:07:39
     * @param roomId
     * @param connId
     * @param conn
     * @return
     */
    public static boolean addOrUpdateWebSocket(String roomId, String connId, WebSocket conn)
    {
        if (conn == null )
        {
            throw new WebSocketException("conn不能为空");
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
            throw new WebSocketException("roomId，connId必须同时为空或都不能为空");
        }
    }
    
    /**
     * 
     * @描述：移除roomId下的单个连接connId或全部连接
     * @作者：严磊
     * @时间：2019年9月23日 下午2:07:31
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
            //获取需要删除的连接id
            Set<String> connIds = new HashSet<String>();
            for (String key : roomMap.keySet())
            {
                if(roomMap.get(key).equals(roomId))
                {
                    connIds.add(key);
                }
            }
            //删除连接
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
     * @描述：判断连接是否存在
     * @作者：严磊
     * @时间：2019年9月23日 下午4:59:26
     * @param conn
     * @return
     */
    public static boolean webSocketExists(WebSocket conn)
    {
        return wsMap.containsKey(conn);
    }
    
}
