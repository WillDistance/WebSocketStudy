package webSocket.exception;

/**
 * 
 * @����: WebSocke�쳣
 * @��Ȩ: Copyright (c) 2019 
 * @��˾: ˼�ϿƼ� 
 * @����: ����
 * @�汾: 1.0 
 * @��������: 2019��9��23�� 
 * @����ʱ��: ����6:33:10
 */
public class WebSocketException extends RuntimeException
{
    private static final long serialVersionUID = 1L;
    
    public WebSocketException(String message)
    {
        super(message);
    }
    
    public WebSocketException(String message, Throwable cause) {
        super(message, cause);
    }
}
