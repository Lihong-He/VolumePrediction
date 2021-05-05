package classification;

import java.sql.Connection;
import java.util.ArrayList;

import utilTool.Util;

public class LikeOperation {
	
	//find #like and #dislike for article
	public static void main(String[] args)
	{
		long sinceArticleID = 0;
		long outletID = 9;//WSP
//		long outletID = 18;//Fox
//		long outletID = 142;//DailyMail
//		long outletID = 26;//Guardian
//		long outletID = 3;//WSJ
//		long outletID = 51;//NYTimes
		
		String path = "G:\\demo\\crawler\\experiment result\\feature\\like\\WSP.txt";
		Util util = new Util();
		Connection con = util.mysqlConnection();
		DBOperation db = new DBOperation();
		LikeOperation like = new LikeOperation();
		String firstLine = "ID;NUM LIKE;NUM DISLIKE";
		//write the first line
		util.writeFileAppending(path, firstLine);
		
		//article id, #like, #dislike
		ArrayList<String[]> articleList = db.getArticleLikeInOutlet(outletID, con);
		for(String[] article : articleList)
		{
			String articleID = article[0];
			String likeNum = article[1];
			String dislikeNum = article[2];
			//write entity info
			//ID;NUM LIKE;NUM DISLIKE
			String writeInfo = articleID+";"+likeNum+";"+dislikeNum;
			util.writeFileAppending(path, writeInfo);
		}//end of each article
		
		util.dbClose(con);
	}

}
