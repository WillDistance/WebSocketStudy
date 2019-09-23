package webSocket.exception;

/**
 * 
 * @描述: WebSocke异常
 * @版权: Copyright (c) 2019 
 * @公司: 思迪科技 
 * @作者: 严磊
 * @版本: 1.0 
 * @创建日期: 2019年9月23日 
 * @创建时间: 下午6:33:10
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
