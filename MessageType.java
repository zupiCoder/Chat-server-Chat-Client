
public class MessageType {

  public static String PrivateMessage(String time, String type, String sender, String receiver, String content) {
    MessagePacket message = new MessagePacket(time, type, sender, receiver, content);
    return message.messageToJSONString();
  }

  public static String PublicMessage(String time, String type, String sender, String receiver, String content) {
    MessagePacket message = new MessagePacket(time, type, sender, null, content);
    return message.messageToJSONString();
  }
}
