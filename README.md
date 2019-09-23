# WebSocketStudy
在java-websocket的基础上学习实现对WebSocket服务端的简单封装
java-websocket地址：https://github.com/TooTallNate/Java-WebSocket

环境：jdk1.6+ eclipse 

测试服务端地址：http://www.websocket-test.com/

输入：创建/删除_房间号_连接id
     online_room01_conn01
     offline_room01_conn01
控制台通过输入房间号 和 连接id 将消息发送到客户端
 
WsServer.onMessage() 接收客户端消息的方法为测试使用，需要根据业务需求修改方法实现

#运行：
webSocket.testMain.main()方法启动服务并测试
