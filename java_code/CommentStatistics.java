package classification;

import java.sql.Connection;
import java.util.ArrayList;

import utilTool.Util;

public class CommentStatistics {
	
	public void calculate(long outletID, Connection con, DBOperation db)
	{
		ArrayList<Long> articleList = db.getArticleWithCommentByOutlet(con, outletID);
		int articleNum = articleList.size();
		double avgPercent = 0;
		ArrayList<Double> percentList = new ArrayList<Double>();
		double avgReplyNum = 0;
		ArrayList<Double> replyNumList = new ArrayList<Double>();
		
		for(long articleID : articleList)
		{		
//			articleID = 191719;
			//parentID of the first comment, generally it is NULL, sometimes it is article id string
			String firstParentID = db.getParentIDofFirstComment(con, articleID);
			//# of comment has reply, # of total comment
			int[] num = db.getCommentNumberByArticleID(con, articleID);
			if(num[0] > 0)
				num[0] = num[0] -1; //-1 because of the article id
			if(num[1] == 0)
			{
				articleNum--;
				continue;
			}
			double percentage = (double)num[0]*100 / (double)num[1]; 
			avgPercent = avgPercent + percentage;
			percentList.add(percentage);
			//avg, std of reply
			double[] reply = db.getAvgStdReply(articleID, firstParentID, con);
			double replyNum = reply[0];
			avgReplyNum = avgReplyNum + replyNum;
			replyNumList.add(replyNum);
			System.out.println(percentage+", "+replyNum+", "+articleID);
		}//end of each article
		
		avgPercent = avgPercent / (double)articleNum;
		avgReplyNum = avgReplyNum / (double)articleNum;
		System.out.println("In outlet "+outletID+", avgPercent: "+avgPercent+", avgReplyNum: "+avgReplyNum);
	}
	
	
	public static void main(String[] args)
	{
		DBOperation db = new DBOperation();	
		Util util = new Util();
		Connection con = util.mysqlConnection();
		long outletID = 9;//WSP
//		long outletID = 18;//Fox
//		long outletID = 142;//DailyMail
//		long outletID = 26;//Guardian
//		long outletID = 3;//WSJ
//		long outletID = 51;//NYTimes
				
		CommentStatistics cs = new CommentStatistics();
		cs.calculate(outletID, con, db);
	}

}
