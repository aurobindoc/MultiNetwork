package version01;

public class Queues {
	static int qSizeUniform = 1000;
	int queueID, queueSize;
	String queueName;
	public Queues(int queueID, int queueSize, String queueName) {
		this.queueID = queueID;
		this.queueSize = queueSize;
		this.queueName = queueName;
	}
}
