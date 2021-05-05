package classification;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;

public class DBOperation {
	
	//get id, #comment, time, topic, #user, #depth, #width, first comment, last comment, 
	//title, story appearance, RecoveredCategory, Author
	public ArrayList<String[]> getArticleInfoInOutlet(long outletID, Connection con)
	{
		ArrayList<String[]> articleList = new ArrayList<String[]>();
		String sql = "";
		
		if(outletID > 0)
		{
			sql = "select a.ArticleID, s.RevisitCommentCount as volume, a.CommentConsistentPublishTime as pub, t.Name as topic, "
					+ "s.UserNumber as userNum, s.DepthOfTree as depth, s.Width as width, s.TimeOfFirstComment, s.TimeOfLastComment, "
					+ "a.Title, t.StartTime, a.CommentConsistentRetrievedPublishTime, a.RecoveredCategory, a.Author, t.RecoveredCategory as reLabel "
					+ "from newsarticle a, articlestatistics s, newsline l, newsstory t "
					+ "where a.ArticleID=s.ArticleID and a.NewsLineID=l.NewsLineID and l.NewsStoryID=t.NewsStoryID "
					+ "and a.NewsOutletID=? "
					+ "and a.CommentConsistentPublishTime is not null "
//					+ "and a.CommentConsistentRetrievedPublishTime is not null "
					+ "and s.TimeOfFirstComment is not null and s.TimeOfLastComment is not null "
					+ "order by a.ArticleID";
		}
		else
		{
			sql = "select a.ArticleID, s.RevisitCommentCount as volume, a.CommentConsistentPublishTime as pub, t.Name as topic, "
					+ "s.UserNumber as userNum, s.DepthOfTree as depth, s.Width as width, s.TimeOfFirstComment, s.TimeOfLastComment, "
					+ "a.Title, t.StartTime, a.CommentConsistentRetrievedPublishTime, a.RecoveredCategory, a.Author, t.RecoveredCategory as reLabel "
					+ "from newsarticle a, articlestatistics s, newsline l, newsstory t "
					+ "where a.ArticleID=s.ArticleID and a.NewsLineID=l.NewsLineID and l.NewsStoryID=t.NewsStoryID "
					+ "and a.NewsOutletID>? "
					+ "and a.CommentConsistentPublishTime is not null "
//					+ "and a.CommentConsistentRetrievedPublishTime is not null "
					+ "and s.TimeOfFirstComment is not null and s.TimeOfLastComment is not null "
					+ "order by a.ArticleID";
		}
		try{
			PreparedStatement statement = con.prepareStatement(sql);
			statement.setLong(1, outletID);
			ResultSet rs = statement.executeQuery();
			
			while(rs.next())
			{
				String[] record = new String[15];
				record[0] = String.valueOf(rs.getLong(1));//id
				record[1] = String.valueOf(rs.getLong(2));//#comment
				record[2] = String.valueOf(rs.getTimestamp(3));//publication time from archived version
				record[3] = rs.getString(4);//topic
				record[4] = String.valueOf(rs.getInt(5));//#user
				record[5] = String.valueOf(rs.getInt(6));//#depth
				record[6] = String.valueOf(rs.getInt(7));//#width
				record[7] = String.valueOf(rs.getTimestamp(8));//first comment time
				record[8] = String.valueOf(rs.getTimestamp(9));//last comment time
				record[9] = rs.getString(10); //title
				record[10] = String.valueOf(rs.getTimestamp(11));//story appearance
				record[11] = String.valueOf(rs.getTimestamp(12));//publication time from retrieved file
				record[12] = rs.getString(13); //category
				record[13] = rs.getString(14); //author
				record[14] = rs.getString(15); //reassigned category of the topic
				articleList.add(record);
			}
			
			rs.close();
			statement.close();
		}catch(Exception e){
			System.out.println(e);
		}	
		
		return articleList;
	}
	
	//get id, #like, #dislike
	public ArrayList<String[]> getArticleLikeInOutlet(long outletID, Connection con)
	{
		ArrayList<String[]> articleList = new ArrayList<String[]>();
		String sql = "select a.ArticleID, sum(c.LikeCount), sum(c.DislikeCount) from newsarticle a, comments c "
				+ "where a.ArticleID=c.ArticleID and a.NewsOutletID=? group by a.ArticleID";
		try{
			PreparedStatement statement = con.prepareStatement(sql);
			statement.setLong(1, outletID);
			ResultSet rs = statement.executeQuery();
			
			while(rs.next())
			{
				String[] record = new String[3];
				record[0] = String.valueOf(rs.getLong(1));//id
				record[1] = String.valueOf(rs.getInt(2));//#like
				record[2] = String.valueOf(rs.getInt(3));//#dislike
				articleList.add(record);
			}
			
			rs.close();
			statement.close();
		}catch(Exception e){
			System.out.println(e);
		}	
		
		return articleList;
	}
		
	//select top comments in this article
	public ArrayList<String[]> getTopCommentsByArticleID(long articleID, int topNum, Connection con)
	{
		ArrayList<String[]> commentList = new ArrayList<String[]>();
		//in sqlserver
//		String sql = "select top "+topNum+" * from comments where articleID = "+articleID+" order by Time";
		//in mysql
		String sql = "select * from comments where ArticleID="+articleID+" order by Time limit "+topNum;
		try{
			PreparedStatement statement = con.prepareStatement(sql);
			ResultSet rs = statement.executeQuery();
			
			while(rs.next())
			{
				String[] record = new String[5];
				record[0] = rs.getString(1);//comment id
				record[1] = rs.getString(2);//author
				record[2] = rs.getString(3);//content
				record[3] = String.valueOf(rs.getTimestamp(4));//time
				record[4] = rs.getString(5);//parent id
				commentList.add(record);
			}
			
			rs.close();
			statement.close();
		}catch(Exception e){
			System.out.println(e);
		}			
		
		return commentList;		
	}
	
	//select all comments in this article
	public ArrayList<String[]> getAllCommentsByArticleID(long articleID, Connection con)
	{
		ArrayList<String[]> commentList = new ArrayList<String[]>();
		String sql = "select * from comments where ArticleID=? order by Time";
		try{
			PreparedStatement statement = con.prepareStatement(sql);
			statement.setLong(1, articleID);
			ResultSet rs = statement.executeQuery();
			
			while(rs.next())
			{
				String[] record = new String[5];
				record[0] = rs.getString(1);//comment id
				record[1] = rs.getString(2);//author
				record[2] = rs.getString(3);//content
				record[3] = String.valueOf(rs.getTimestamp(4));//time
				record[4] = rs.getString(5);//parent id
				commentList.add(record);
			}
			
			rs.close();
			statement.close();
		}catch(Exception e){
			System.out.println(e);
		}			
		
		return commentList;		
	}
	
	public ArrayList<String[]> getAllCommentsTimeByArticleID(long articleID, Connection con)
	{
		ArrayList<String[]> commentList = new ArrayList<String[]>();
		String sql = "select commentID, Time from comments where ArticleID=? order by Time";
		try{
			PreparedStatement statement = con.prepareStatement(sql);
			statement.setLong(1, articleID);
			ResultSet rs = statement.executeQuery();
			
			while(rs.next())
			{
				String[] record = new String[2];
				record[0] = rs.getString(1);//comment id
				record[1] = String.valueOf(rs.getTimestamp(2));//time

				commentList.add(record);
			}
			
			rs.close();
			statement.close();
		}catch(Exception e){
			System.out.println(e);
		}			
		
		return commentList;		
	}
	
	public Timestamp[] getCommentTimeByArticleID(Connection con, long articleID)
	{
		Timestamp[] time = new Timestamp[2];
		String sql = "SELECT TimeOfFirstComment, TimeOfLastComment FROM articlestatistics where ArticleID=?";
		try{
			PreparedStatement statement = con.prepareStatement(sql);
			statement.setLong(1, articleID);
			ResultSet rs = statement.executeQuery();
			
			while(rs.next())
			{
				time[0] = rs.getTimestamp(1);
				time[1] = rs.getTimestamp(2);
			}
			
			rs.close();
			statement.close();
		}catch(Exception e){
			System.out.println(e);
		}
		
		return time;
	}
		
	public String getArticleContentByArticleID(Connection con, long articleID)
	{
		String cont = "";
		String sql = "select ExtractText from articletext where ArticleID=?";
		try{
			PreparedStatement statement = con.prepareStatement(sql);
			statement.setLong(1, articleID);
			ResultSet rs = statement.executeQuery();
			
			while(rs.next())
				cont = rs.getString(1);
			
			rs.close();
			statement.close();
		}catch(Exception e){
			System.out.println(e);
		}
		
		return cont;
	}
	
	public ArrayList<Long> getArticleWithCommentByOutlet(Connection con, long outletID)
	{
		ArrayList<Long> list = new ArrayList<Long>();
		String sql = "select s.ArticleID from newsarticle a, articlestatistics s "
				+ "where a.ArticleID=s.ArticleID and a.NewsOutletID=?";
		try{
			PreparedStatement statement = con.prepareStatement(sql);
			statement.setLong(1, outletID);
			ResultSet rs = statement.executeQuery();
			
			while(rs.next())
				list.add(rs.getLong(1));
			
			rs.close();
			statement.close();
		}catch(Exception e){
			System.out.println(e);
		}
		
		return list;
	}
	
	public int[] getCommentNumberByArticleID(Connection con, long articleID)
	{
		int[] num = new int[2];
		String sql = "select count(distinct ParentID), count(commentID) from comments where ArticleID=?";
		try{
			PreparedStatement statement = con.prepareStatement(sql);
			statement.setLong(1, articleID);
			ResultSet rs = statement.executeQuery();
			
			while(rs.next())
			{
				num[0] = rs.getInt(1);
				num[1] = rs.getInt(2);
			}
			
			rs.close();
			statement.close();
		}catch(Exception e){
			System.out.println(e);
		}		
		
		return num;
	}
	
	public String getParentIDofFirstComment(Connection con, long articleID)
	{
		String res = null;
		String sql = "select ParentID from comments where ArticleID=? order by time asc limit 1";
		try{
			PreparedStatement statement = con.prepareStatement(sql);
			statement.setLong(1, articleID);
			ResultSet rs = statement.executeQuery();
			
			while(rs.next())
				res = rs.getString(1);
			
			rs.close();
			statement.close();
		}catch(Exception e){
			System.out.println(e);
		}
		
		return res;
	}
	
	public double[] getAvgStdReply(long articleID, String parentID, Connection con)
	{
		double[] res = new double[2];
		String sql = null;
		PreparedStatement statement = null;
		try{
			if(parentID == null)
			{
				sql = "select avg(count), std(count) from "
					+ "(select count(commentID) as count, ParentID from comments where ArticleID=? "
					+ "and ParentID is not null group by ParentID) as A";
				statement = con.prepareStatement(sql);
				statement.setLong(1, articleID);
			}
			else
			{
				sql = "select avg(count), std(count) from "
					+ "(select count(commentID) as count, ParentID from comments where ArticleID=? "
					+ "and ParentID!=? group by ParentID) as A";
				statement = con.prepareStatement(sql);
				statement.setLong(1, articleID);
				statement.setString(2, parentID);
			}
			
			ResultSet rs = statement.executeQuery();			
			while(rs.next())
			{
				res[0] = rs.getDouble(1);//avg
				res[1] = rs.getDouble(2);//std
			}
			
			rs.close();
			statement.close();
		}catch(Exception e){
			System.out.println(e);
		}			
		
		return res;		
	}

}
