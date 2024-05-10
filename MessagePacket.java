import java.util.LinkedHashMap;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class MessagePacket {

  private String time;
  private String type;
  private String sender;
  private String receiver;
  private String content;

  // konstruktor za objekt MessagePacket
  public MessagePacket(String time, String type, String sender, String receiver, String content) {
    this.time = time;
    this.type = type;
    this.sender = sender;
    this.receiver = receiver;
    this.content = content;
  }

  public String messageToJSONString() {
    LinkedHashMap<String, String> map = new LinkedHashMap<>();

    map.put("time", time);
    map.put("type", type);
    map.put("sender", sender);
    map.put("receiver", receiver);
    map.put("content", content);

    JSONObject message = new JSONObject(map);
    // convert java object args to json
    return message.toJSONString();
  }

  public static MessagePacket jsonToJava(String jsonString) {
    JSONParser parser = new JSONParser();
    JSONObject jsonObject = null;

    try {
      jsonObject = (JSONObject) parser.parse(jsonString);
    } catch (ParseException e) {
      System.err.println("Error parsing JSON: " + e.getMessage());
      return null; // Return null if parsing fails
    }

    // If jsonObject is null, return null to prevent NullPointerException
    if (jsonObject == null) {
      return null;
    }

    // Extract values from JSON object
    String time = (String) jsonObject.get("time");
    String type = (String) jsonObject.get("type");
    String sender = (String) jsonObject.get("sender");
    String receiver = (String) jsonObject.get("receiver");
    String content = (String) jsonObject.get("content");

    // Create a new MessagePacket object
    return new MessagePacket(time, type, sender, receiver, content);
  }

  public String getTime() {
    return time;
  }

  public String getType() {
    return type;
  }

  public String getSender() {
    return sender;
  }

  public String getReceiver() {
    return receiver;
  }

  public String getContent() {
    return content;
  }
}
