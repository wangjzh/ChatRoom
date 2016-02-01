package ChatRoomServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

public class ChatRoomServer {

    private Selector selector = null;
    private static final int port = 8888;
    private Charset charset = Charset.forName("UTF-8");
    private RoomsManger MyRoomsManger = new RoomsManger();
    
    public void init() throws IOException
    {
        selector = Selector.open();
        ServerSocketChannel server = ServerSocketChannel.open();
        server.bind(new InetSocketAddress(port));        
        server.configureBlocking(false); //�������ķ�ʽ
        server.register(selector, SelectionKey.OP_ACCEPT); //ע�ᵽѡ�����ϣ�����Ϊ����״̬
        
        System.out.println("Server is listening now...");
        
        while(true) {
            int readyChannels = selector.select();
            if(readyChannels == 0) continue; 
            // ����selectedKeys
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
            while(keyIterator.hasNext()) {
                 SelectionKey sk = (SelectionKey) keyIterator.next();
                 keyIterator.remove();
                 dealWithSelectionKey(server,sk);
            }
        }
    }
    
    private void dealWithContent(SelectionKey sk, String content) throws IOException
    {
    	SocketChannel channel = (SocketChannel)sk.channel();
        if(content.length() > 0)
        {
        	// �����ʽ
        	// CMD=0;Other=XXXX;  --->   CMD=RegisterName;Name=XXXX;
        	// CMD=1;Other=XXXX;  --->   CMD=SendMesg;Content=XXXX;
        	// CMD=2;Other=XXXX;  --->   CMD=CreateRoom;Name=XXXX;
        	// CMD=3;Other=XXXX;  --->   CMD=EnterChatRoom;RoomNo=XXXX;
        	// CMD=4;Other=XXXX;  --->   CMD=ExitChatRoom;RoomNo=XXXX;
        	
        	int Command = -1;
        	String[] Infor = new String[1];
        	
        	Command = GetCommand(content, Infor);
        	String OtherInfor = Infor[0];
        	switch (Command)
        	{
			case 0:
				MyRoomsManger.RegisterName(OtherInfor, channel);
				break;
				
			case 1:
				MyRoomsManger.SendMessage(OtherInfor, channel);
				break;
				
			case 2:
				MyRoomsManger.CreateChatRoom(OtherInfor, channel);
				break;
				
			case 3:
				MyRoomsManger.EnterChatRoom(channel, OtherInfor);
				break;
			
			case 4:
				MyRoomsManger.ExitChatRoom(channel, true);
				break;

			default:
				MyRoomsManger.SendCommandResult("Unknown Command!", channel);
				break;
			}
        }
    	
    }
    
    // �����ص�SelectionKey
    public void dealWithSelectionKey(ServerSocketChannel server,SelectionKey sk) throws IOException
    {
    	// �µ��û�����
        if(sk.isAcceptable())
        {
            SocketChannel sc = server.accept();
            
            sc.configureBlocking(false); //������ģʽ
            //ע��ѡ������������Ϊ��ȡģʽ���յ�һ����������Ȼ����һ��SocketChannel����ע�ᵽselector��
            sc.register(selector, SelectionKey.OP_READ);
            
            //���˶�Ӧ��channel����Ϊ׼�����������ͻ�������
//            sk.interestOps(SelectionKey.OP_ACCEPT);
            System.out.println("Server is listening from client :" + sc.getRemoteAddress());
            sc.write(charset.encode("Please input your name:"));
        }
        //�������Կͻ��˵�����
        if(sk.isReadable())
        {
            //���ظ�SelectionKey��Ӧ�� Channel��������������Ҫ��ȡ
            SocketChannel sc = (SocketChannel)sk.channel(); 
            ByteBuffer buff = ByteBuffer.allocate(1024);
            StringBuilder content = new StringBuilder();
            try
            {
                while(sc.read(buff) > 0)
                {
                    buff.flip();
                    content.append(charset.decode(buff));                    
                }
                //���˶�Ӧ��channel����Ϊ׼����һ�ν�������
//                sk.interestOps(SelectionKey.OP_READ);
                
                dealWithContent(sk, content.toString());
            }
            catch (IOException io)
            {
                sk.cancel();
                SocketChannel channel = (SocketChannel)sk.channel();
                if(channel != null)
                {
                	MyRoomsManger.Logout(channel);
                	channel.close();
                    String temp = MyRoomsManger.FindUserNameByChannel(channel);
                    String username = temp != null ? temp : "nameless";
                    
                    System.out.println("Client :" + username + " disconnnect.");
                }
            }            
        }
    }
    
    // ��string�л�ȡcommand
    private int GetCommand(String content, String[] otherinfor)
    {
    	int cmd = -1;
    	if (content.startsWith("CMD=")) {
    		String[] contents = content.split(";");
    		if (contents.length == 2) {
        		char strCmd = contents[0].charAt(4);
        		cmd = strCmd - '0';
        		otherinfor[0] = contents[1].substring(6);    			
    		}
    	}
    	return cmd;
    }
    
    public static void main(String[] args) throws IOException 
    {
        new ChatRoomServer().init();
    }
}
