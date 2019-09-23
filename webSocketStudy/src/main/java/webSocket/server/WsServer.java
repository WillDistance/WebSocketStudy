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
 * @描述: WebSocket服务端
 * @版权: Copyright (c) 2019 
 * @公司: 思迪科技 
 * @作者: 严磊
 * @版本: 1.0 
 * @创建日期: 2019年9月23日 
 * @创建时间: 下午4:38:00
 */
public class WsServer extends WebSocketServer
{
    private static Logger logger = Logger.getLogger(WsServer.class);
    
    private static int default_port = 8887;
    
    private static int state = 0;//服务状态 0：关闭  1：启动
    
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
     * @描述：启动服务
     * @作者：严磊
     * @时间：2019年9月23日 下午5:56:46
     */
    public void startServer(){
        if(state == 0)
        {
            getInstance().start();
        }
    }
    
    /**
     * 
     * @描述：1.4新增抽象方法   在服务器成功启动时调用。如果发生任何错误，则改为调用onError。
     * @作者：严磊
     * @时间：2019年9月20日 上午11:04:12
     */
    @Override
    public void onStart()
    {
        logger.info("onStart:启动WebSocket服务，监听端口："+getInstance().getPort());
        WsManagePool.startPool();
    }
    
    /**
     * 
     * @描述：客户端连接
     * @作者：严磊
     * @时间：2019年9月23日 下午4:17:08
     * @param conn
     * @param handshake
     */
    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake)
    {
        
        logger.info("onOpen:客户端链接的时候调用");
        WsManagePool.addOrUpdateWebSocket("", "", conn);
    }
    
    /**
     * 
     * @描述：
     * @作者：严磊
     * @时间：2019年9月23日 下午4:17:29
     * @param conn
     * @param code
     * @param reason
     * @param remote
     */
    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote)
    {
        //断开连接时候触发代码
        logger.info("断开连接");
        WsManagePool.removeWebSocket("", "", conn);
    }
    
    /**
     * 
     * @描述：接收客户端消息
     * @作者：严磊
     * @时间：2019年9月23日 下午4:17:39
     * @param conn
     * @param message
     */
    @Override
    public void onMessage(WebSocket conn, String message)
    {
        /*****以下内容仅为测试使用*****/
        //online_room01_conn01
        if ( null != message && message.startsWith("online") )
        {
            logger.info("onMessage接收客户端创建连接消息: " + message+",WebSocket:"+conn);
            
            //创建连接并发送消息
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
            logger.info("onMessage接收客户端删除连接消息: " + message+",WebSocket:"+conn);
            //关闭连接并发送消息
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
            //已有连接 发送消息
            if(WsManagePool.webSocketExists(conn))
            {
                System.out.println("房间号"+WsManagePool.getRoomIdByWs(conn)+",连接id"+WsManagePool.getConnIdByWs(conn)+"消息内容："+message);
            }
            else
            {
                logger.error("连接不存在了");
            }
            
        }
    }
    
    @Override
    public void onError(WebSocket conn, Exception ex)
    {
        //错误时候触发的代码
        logger.error("onError:出现异常时被调用", ex);
    }
    
    /**
     * 
     * @描述：向客户端发送数据
     * @作者：严磊
     * @时间：2019年9月23日 下午2:14:23
     * @param roomId
     * @param connId
     * @param message
     * @return
     */
    public boolean sendMessage(Set<WebSocket> conns, String message)
    {
        if ( StringHelper.isBlank(message) || conns.size() == 0 )
        {
            logger.error("message不能为空或者conns为空");
            return false;
        }
        for (WebSocket conn : conns)
        {
            conn.send(message);
        }
        return true;
    }
    
}
