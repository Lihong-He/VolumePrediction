package classification;

import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import utilTool.Util;

public class KeepArtComContent {
	
	public static void main(String[] args)
	{
		long sinceArticleID = 0;
//		long outletID = 9;//WSP
//		String fileName = "WSP";
//		long outletID = 18;//Fox
//		String fileName = "Fox";
//		long outletID = 142;//DailyMail
//		String fileName = "DailyMail";
//		long outletID = 26;//Guardian
//		String fileName = "Guardian";
//		long outletID = 3;//WSJ
//		String fileName = "WSJ";
		long outletID = 51;//NYTimes
		String fileName = "NYTimes";
		
		String readPath = "G:/demo/crawler/experiment result/feature/entities/" + fileName + ".txt";
		String writePath = "G:/demo/crawler/experiment result/feature/artComContent/" + fileName + ".txt";
		
		KeepArtComContent keep = new KeepArtComContent();
		keep.keepContent(readPath, writePath);
	}
	
	public void keepContent(String readPath, String writePath)
	{
		Util util = new Util();
		Connection con = util.mysqlConnection();
		DBOperation db = new DBOperation();
		
		ArrayList<String> aidList = util.readAIDsFromTxt(readPath);
		System.out.println("Article size: "+aidList.size());
		for(String aid: aidList)
		{
			System.out.println(aid);
			JSONObject articleJson = new JSONObject();
			articleJson.put("ArticleID", aid);
			//extract article content from db
			long articleID = Long.parseLong(aid);
			String content = db.getArticleContentByArticleID(con, articleID);
			content = content.replaceAll("[\n\t]", " ");
			articleJson.put("ArticleContent", content);
			
			//get comment info from db
			JSONArray comArray = new JSONArray();
			int topNum = 10;
			ArrayList<String[]> commentList = db.getTopCommentsByArticleID(articleID, topNum, con);
			for(String[] comment : commentList)
			{
				JSONObject comJson = new JSONObject();
				String cID = comment[0];
				String cCont = comment[2];
				cCont = cCont.replaceAll("[\n\t]", " ");
				comJson.put("CommentID", cID);
				comJson.put("CommentContent", cCont);
				
				comArray.add(comJson);
			}
			
			articleJson.put("comments", comArray);
			
			//write json file
			util.writeFileAppending(writePath, articleJson.toJSONString());
		}//end of article list
		
		
		util.dbClose(con);
	}

}
