package entities;

public class Message {
	public Message(String Sender, String SenderTeam, String SenderRole,  String Content) {
		this.Sender = Sender;
		this.SenderTeam = SenderTeam;
		this.SenderRole = SenderRole;
		this.Content = Content;
	}
	public String getSender() {
		return Sender;
	}
	public void setSender(String sender) {
		Sender = sender;
	}
	public String getSenderRole() {
		return SenderRole;
	}
	public void setSenderRole(String senderRole) {
		SenderRole = senderRole;
	}
	public String getSenderTeam() {
		return SenderTeam;
	}
	public void setSenderTeam(String senderTeam) {
		SenderTeam = senderTeam;
	}
	public String getContent() {
		return Content;
	}
	public void setContent(String content) {
		Content = content;
	}
	private String Sender;
	private String SenderRole;
	private String SenderTeam;
	private String Content;
	
	@Override
	public String toString() {
		return Sender +"(" + SenderTeam + "-" + SenderRole + "): " + Content;
	}
}
