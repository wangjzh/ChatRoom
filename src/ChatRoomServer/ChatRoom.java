package ChatRoomServer;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class ChatRoom {
	private String ChatRoomName;
	
	private Charset charset = Charset.forName("UTF-8");
	private Map<SocketChannel, String> Members = new HashMap<SocketChannel, String>();
	
	public ChatRoom(String roomname)
	{
		ChatRoomName = roomname;		
	}
	
	
	public String RoomName()
	{
		return ChatRoomName;		
	}
	
	// ���뷿��
	public boolean Add(String username,SocketChannel channel)
	{
		if (Members.containsKey(channel))
			return false;
		
		Members.put(channel, username);
		String conten = username +" enter " + ChatRoomName + " chat room!";
		BroadCast(conten, channel);		
		return true;		
	}
	
	// �뿪����
	public void Exit(SocketChannel channel)
	{
		Members.remove(channel);
	}
	
	public boolean IsEmpty()
	{
		return Members.isEmpty();		
	}
	
	// �����ڹ㲥��Ϣ
	public void BroadCast(String content, SocketChannel except)
	{
		if ("".equals(content)) return; // contentΪ�գ���ת��
		
		String sendername = Members.get(except);
		
        //�㲥���ݵ������������е�SocketChannel��
		for (SocketChannel dst : Members.keySet()) {
			if (dst != except) {
				try {
					dst.write(charset.encode(sendername + ": " + content));
				} catch (IOException e) {
					e.printStackTrace();
				}				
			}			
		}
	}
}
