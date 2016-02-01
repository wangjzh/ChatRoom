package ChatRoomClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;


public class ChatRoomClient {

    private Selector selector = null;
    static final int port = 8888;
    private Charset charset = Charset.forName("UTF-8");
    private SocketChannel sc = null;
    
    public void init() throws IOException
    {
        selector = Selector.open();
        sc = SocketChannel.open(new InetSocketAddress("127.0.0.1",port));
        sc.configureBlocking(false);
        sc.register(selector, SelectionKey.OP_READ);
        // ����һ�����߳�����ȡ�ӷ������˵�����
        new Thread(new ClientThread()).start();
        // �����߳��� �Ӽ��̶�ȡ�������뵽��������
        Scanner scan = new Scanner(System.in);
        while(scan.hasNextLine())
        {
            String line = scan.nextLine();
            if("".equals(line)) continue; //����������Ϣ
            sc.write(charset.encode(line));
        }
        
    }
    
    private class ClientThread implements Runnable
    {
        public void run()
        {
            try
            {
                while(true) {
                    int readyChannels = selector.select();
                    if(readyChannels == 0) continue; 
                    Set<SelectionKey> selectedKeys = selector.selectedKeys();
                    Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
                    while(keyIterator.hasNext()) {
                         SelectionKey sk = (SelectionKey) keyIterator.next();
                         keyIterator.remove();
                         dealWithSelectionKey(sk);
                    }
                }
            }
            catch (IOException io)
            {}
        }

        private void dealWithSelectionKey(SelectionKey sk) throws IOException {
            if(sk.isReadable())
            {
                SocketChannel sc = (SocketChannel)sk.channel();
                
                ByteBuffer buff = ByteBuffer.allocate(1024);
                String content = "";
                while(sc.read(buff) > 0)
                {
                    buff.flip();
                    content += charset.decode(buff);
                }

                System.out.println(content);
                sk.interestOps(SelectionKey.OP_READ);
            }
        }
    }
    
    public static void main(String[] args) throws IOException
    {
    	System.out.println("--------------------------------------------------");
    	System.out.println(" CMD=0;Other=yourname;  --->  RegisterName");
    	System.out.println(" CMD=1;Other=conten;    --->  SendMesg");
    	System.out.println(" CMD=2;Other=roomname;  --->  CreateRoom");
    	System.out.println(" CMD=3;Other=roomname;  --->  EnterChatRoom");
    	System.out.println(" CMD=4;Other=;          --->  ExitChatRoom");
    	System.out.println("--------------------------------------------------");
        new ChatRoomClient().init();
    }
}