package classification;

import java.util.ArrayList;
import java.util.List;

public class ManyTreeNode {
	private String commentID;
	private String author;
	private int depth;
	private List<ManyTreeNode> childList;
	
	public ManyTreeNode(String commentID, String author, int depth)
	{
		this.commentID = commentID;
		this.author = author;
		this.depth = depth;
		this.childList = new ArrayList<ManyTreeNode>();
	}
	
	public ManyTreeNode(String commentID, String author, int depth, List<ManyTreeNode> childList)
	{
		this.commentID = commentID;
		this.author = author;
		this.depth = depth;
		this.childList = childList;
	}
	
	public String getCommentID() {
		return commentID;
	}

	public void setCommentID(String commentID) {
		this.commentID = commentID;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	public List<ManyTreeNode> getChildList() {
		return childList;
	}

	public void setChildList(List<ManyTreeNode> childList) {
		this.childList = childList;
	}

}
