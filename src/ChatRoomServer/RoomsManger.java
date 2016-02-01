package ChatRoomServer;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class RoomsManger {
    // 用户名-> SocketChannel
    private Map<SocketChannel, String> Users = new HashMap<SocketChannel, String>();
    // 用户名-> ChatRoom
    private Map<SocketChannel, ChatRoom> UserMapRoom = new HashMap<SocketChannel, ChatRoom>();    
    // 房间名->房间
    private Map<String, ChatRoom> Rooms = new HashMap<String, ChatRoom>();
    private Charset charset = Charset.forName("UTF-8");
    
    // 返回提示信息
    private static final String REGISTERNAMESUCCESS  = "Register name successfully.";
    private static final String NEEDNOTREGISTERAGAIN = "Need not register name again.";
    private static final String SENDMESSAGEFAILED    = "Send message failed! Enter a chat room first.";
    private static final String CHATROOMEXIST        = "Create chat room failed! The chat room exist!";
    private static final String REGISTERNAMEFIRST    = "Register name first!";
    private static final String EXITCURRENTROOMFIRST = "Exit current chat room first.";
    private static final String CREATEROOMSUCCESS    = "Create chat room successfully.";    
    private static final String CHATROOMNOTEXIST     = "Chat room don't exist.";
    private static final String ENTERROOMSUCCESS     = "Enter chat room successfully.";
    private static final String EXITROOMSUCCESS      = "Exit chat room successfully.";
    private static final String NOTINROOM            = "You are not in any chat room.";
    
    public void SendCommandResult(String result, SocketChannel channel)
    {
    	try {
    		channel.write(charset.encode(result));
    	} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public String FindUserNameByChannel(SocketChannel channel)
    {
		return Users.get(channel);    	
    }
    
    // RegisterName
    public void RegisterName(String name, SocketChannel channel) 
    {
		String result = null;    	
    	if (Users.containsKey(channel)) {
    		result = NEEDNOTREGISTERAGAIN;    		
    	} else {
    		Users.put(channel, name);
    		result = REGISTERNAMESUCCESS;    		
    	}    	
    	SendCommandResult(result, channel);
    }
    
    // SendMessage
    public void SendMessage(String msg, SocketChannel channel)
    {    	
    	ChatRoom room = UserMapRoom.get(channel);
    	if (room != null) {
    		room.BroadCast(msg, channel);
    	} else {
    		String result = SENDMESSAGEFAILED;
    		SendCommandResult(result, channel);
    	}
    }
    
    // CreateChatRoom
    public void CreateChatRoom(String roomname, SocketChannel channel)
    {
    	String result = CHATROOMEXIST;

    	String creatername = FindUserNameByChannel(channel);
    	if (creatername == null) {
    		result = REGISTERNAMEFIRST;
    	} else if (UserMapRoom.containsKey(channel)) {
    		result = EXITCURRENTROOMFIRST;    		
    	} else if (!Rooms.containsKey(roomname)) {
    		ChatRoom room = new ChatRoom(roomname);
    		Rooms.put(roomname, room);
    		EnterChatRoom(channel, roomname);
    		result = CREATEROOMSUCCESS;    		
    	}
    	
    	SendCommandResult(result, channel);    	
    }
    
    // EnterChatRoom
    public void EnterChatRoom(SocketChannel channel, String roomname)
    {
    	String result = CHATROOMNOTEXIST;
    	
    	ChatRoom room = Rooms.get(roomname);
    	if (UserMapRoom.containsKey(channel)) {
    		result = EXITCURRENTROOMFIRST;
    	} else if (room != null) {
    		String username = FindUserNameByChannel(channel);
    		if (username != null) {
        		room.Add(username, channel);
        		UserMapRoom.put(channel, room);
        		result = ENTERROOMSUCCESS;
    		} else {
    			result = REGISTERNAMEFIRST;
    		}
    	}

    	SendCommandResult(result, channel);
    }
    
    // ExitChatRoom
    public void ExitChatRoom(SocketChannel channel, boolean needsenderrorinfo)
    {
    	String result = null;
    	ChatRoom room = UserMapRoom.remove(channel);    		
    	if (room != null) {
    		room.Exit(channel);
    		result = EXITROOMSUCCESS;
    		
    		if (room.IsEmpty()) {
    			Rooms.remove(room.RoomName());
    			System.out.println("Chat room " + room.RoomName() + " is empty. delete the room!");
    		}
    	} else{
    		result = NOTINROOM;
    	}
    	
    	if (needsenderrorinfo) {
    		SendCommandResult(result, channel);
    	} 	
    }
    
    // Logout
    public void Logout(SocketChannel channel)
    {
    	ExitChatRoom(channel, false);
    }
    
}
