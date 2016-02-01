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
	
	// 加入房间
	public boolean Add(String username,SocketChannel channel)
	{
		if (Members.containsKey(channel))
			return false;
		
		Members.put(channel, username);
		String conten = username +" enter " + ChatRoomName + " chat room!";
		BroadCast(conten, channel);		
		return true;		
	}
	
	// 离开房间
	public void Exit(SocketChannel channel)
	{
		Members.remove(channel);
	}
	
	public boolean IsEmpty()
	{
		return Members.isEmpty();		
	}
	
	// 房间内广播消息
	public void BroadCast(String content, SocketChannel except)
	{
		if ("".equals(content)) return; // content为空，不转发
		
		String sendername = Members.get(except);
		
        //广播数据到该聊天室所有的SocketChannel中
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
