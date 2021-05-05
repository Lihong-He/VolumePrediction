package classification;

import java.util.ArrayList;


public class ManyTree {
	
	/**
	 * @param A list of String[5]: comment id, author, content, time, parent id
	 * @return the root of the manyTree
	 */
	public ManyTreeNode creatTree(ArrayList<String[]> commentList, String rootID)
	{
		String leafComment = "";
		int depth = 0;
		if(commentList==null || commentList.size()<0)
			return null;
		
		ManyTreeNode root = new ManyTreeNode(rootID, "ROOT", 0);
		
		for(String[] comment : commentList)
		{
			ManyTreeNode currentNode = new ManyTreeNode(comment[0], comment[1], 1);
			//if the parentID is null, add current element to the root's child list
			if(comment[4]==null || comment[4].equals(rootID) || comment[4].length()==0)
				root.getChildList().add(currentNode);
			else
			{
				ManyTreeNode parentNode = findParentNode(root, comment[4]);
				if(parentNode != null)
				{
					int parentDepth = parentNode.getDepth();
					currentNode.setDepth(parentDepth+1);
					parentNode.getChildList().add(currentNode);
					int currentDepth = parentDepth+1;
					if(currentDepth > depth)
					{
						depth = currentDepth;
						leafComment = currentNode.getCommentID();
					}
				}
				 
			}
		}
		
//		System.out.println("depth:"+depth+", leaf:"+leafComment);
		return root;
	}
	
	//find the parent node in the manyTree according to the parentID
	private ManyTreeNode findParentNode(ManyTreeNode root, String parentID)
	{
		ManyTreeNode parent = null;
		
		for(ManyTreeNode item : root.getChildList())
		{
			if(item.getCommentID().equals(parentID))
			{
				parent = item;
				break;
			}
			else
			{
				if(item.getChildList() != null && item.getChildList().size() > 0) 
				{
					parent = findParentNode(item, parentID);
					if(parent != null)
						break;
				}
					
			}
		}
		
		return parent;
	}
	
	//depth first traversal, param[6]: depth, 1~5th width
	public ArrayList<Integer> depthTraversalTree(ManyTreeNode root, ArrayList<Integer> param) 
	{
		String leafComment = "";
		if(root == null)
			return param;
		
		for (ManyTreeNode index : root.getChildList()) 
		{				
			//increase the depth
			int curDepth = index.getDepth();
			if(curDepth > param.get(0))
			{
				param.set(0, curDepth);
				leafComment = index.getCommentID();
			}
			
			//increase the corresponding width in first 5 levels
			if(curDepth <= 5)
				param.set(curDepth, param.get(curDepth)+1);
			
			if (index.getChildList() != null && index.getChildList().size() > 0 ) 
				param = depthTraversalTree(index, param);
		}
		
		return param;		
	}

}
