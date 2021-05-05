package classification;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import utilTool.Util;

public class FeatureMatrix {
	
	
	public void keepExtraFeature(String path, long sinceArticleID, long outletID, String prePath)
	{
		String firstLine = "AID,Art Len,Art Len No Stop,Art Neg,Art Pos,Art Hostile,"		
				+ "20 Com Len,20 Com Len No Stop,20 Com Neg,20 Com Pos,20 Com Hostile,"
				+ "5 Com Len,5 Com Len No Stop,5 Com Neg,5 Com Pos,5 Com Hostile";
		Util util = new Util();
		Connection con = util.mysqlConnection();
		//write the first line
		util.writeFileAppending(path, firstLine);
		
		//get word list
		HashSet<String> negList = new HashSet<String>(getFirstColInfo("G:\\demo\\crawler\\lexicon\\negative-words.txt"));
		HashSet<String> posList = new HashSet<String>(getFirstColInfo("G:\\demo\\crawler\\lexicon\\positive-words.txt"));
		HashSet<String> hostileList = new HashSet<String>(getFirstColInfo("G:\\demo\\crawler\\lexicon\\Hostile word list.txt"));
		HashSet<String> angerList = new HashSet<String>(getFirstColInfo("G:\\demo\\crawler\\lexicon\\Anger word list.txt"));
		HashSet<String> stopList = new HashSet<String>(getFirstColInfo("G:\\demo\\crawler\\lexicon\\stopwords.txt"));

		DBOperation db = new DBOperation();	
		//get the article ID from previous file
		ArrayList<String> idList = getFirstColInfo(prePath);
		for(String id : idList)
		{
			long articleID = Long.parseLong(id);
			String artContent = db.getArticleContentByArticleID(con, articleID);
			
			//get comment id, author, content, time, parent id of top comment	
			int topNum = 20;
			ArrayList<String[]> topCommentList = db.getTopCommentsByArticleID(articleID, topNum, con);
			//top 20 comments
			String top20Comment = "";
			for(int i=0; i<20; i++)
			{
				String[] comment = topCommentList.get(i);
				top20Comment = top20Comment + comment[2] + " ";
			}
			//top 5 comments
			String top5Comment = "";
			for(int i=0; i<5; i++)
			{
				String[] comment = topCommentList.get(i);
				top5Comment = top5Comment + comment[2] + " ";
			}
			
			//len, no stop len, #Neg, #Pos, #Hostile in article content
			String[] artNumItem = calculateContentItem(artContent, negList, posList, hostileList, angerList, stopList);
			//len, no stop len, #Neg, #Pos, #Hostile in top 20 comments
			String[] top20ComNumItem = calculateContentItem(top20Comment, negList, posList, hostileList, angerList, stopList);
			//len, no stop len, #Neg, #Pos, #Hostile in top 20 comments
			String[] top5ComNumItem = calculateContentItem(top5Comment, negList, posList, hostileList, angerList, stopList);
		
			String writeInfo = id+","+artNumItem[0]+","+artNumItem[1]+","+artNumItem[2]+","+artNumItem[3]+","+artNumItem[4]
					+","+top20ComNumItem[0]+","+top20ComNumItem[1]+","+top20ComNumItem[2]+","+top20ComNumItem[3]+","+top20ComNumItem[4]
					+","+top5ComNumItem[0]+","+top5ComNumItem[1]+","+top5ComNumItem[2]+","+top5ComNumItem[3]+","+top5ComNumItem[4];
			
			util.writeFileAppending(path, writeInfo);
		}//end of each article
		util.dbClose(con);
		System.out.println("Done for outlet "+outletID);
	}
	
	
	
	public void keepFeatureData(String path, long sinceArticleID, long outletID, HashMap<String, String[]> entityMap)
	{	
		String firstLine = "ID;DELAY;INTERVAL;MONTH;DAY;HOUR;TOPIC;UNIQUE USERS;THREAD;REPLYS;DEPTH;WIDTH;" //12
				+ "WORDS NUM;NUM QUESTION;NUM EXCLAMATION;COMPLEXITY;"  //4
				+ "NUM COM E;FRAC E A;FRAC E C;QUESTION TL;EXCLAMATION TL;URL C;"   //6
				+ "PUB TO APP;FC TO MIDNIGHT;DAY OF WEEK;WEEK OF MONTH;AUTHOR;CATEGORY;"  //6
				+ "VOLUME;TRUE USE;TRUE DEPTH;TRUE WIDTH;COMMENT HOURS;PUBLICATION HOURS;MONTH COMMENT"; 
		Util util = new Util();
		Connection con = util.mysqlConnection();
		//write the first line
		util.writeFileAppending(path, firstLine);

		DBOperation db = new DBOperation();	
		
		//article id, #comment, publish time (archived), topic, #user, #depth, #width, first comment, 
		//last comment, title, story appearance, publish time (retrieved), category, author
		ArrayList<String[]> articleList = db.getArticleInfoInOutlet(outletID, con);
		System.out.println("Total article size: "+articleList.size());		
		for(String[] article : articleList)
		{
			long articleID = Long.parseLong(article[0]);
			if(articleID < sinceArticleID)
				continue;
			int totalCommentNum = Integer.parseInt(article[1]);
			Timestamp pubTimeArc = Timestamp.valueOf(article[2]);
			Timestamp pubTimeRet = pubTimeArc;
//			if(article[11]!=null && article[11].length()>4)
//				pubTimeRet = Timestamp.valueOf(article[11]);
			Timestamp pubTime = pubTimeArc.getTime()<pubTimeRet.getTime() ? pubTimeArc : pubTimeRet;
			String topic = article[3];
			int trueUserNum = Integer.parseInt(article[4]);
			int trueDepthNum = Integer.parseInt(article[5]);
			int trueWidthNum = Integer.parseInt(article[6]);
			Timestamp comFirstTime = Timestamp.valueOf(article[7]);
			Timestamp comLastTime = Timestamp.valueOf(article[8]);
			String title = article[9];
			//label from news outlet
			String category = article[12];
			category = assignCategory(outletID, category);
			//reassigned label of topic 
			String topicCategory = article[14];
			String relabel = reAssignCategory(category, topicCategory);
			//skip the unsigned label articles
			if(relabel == null)
				continue;
			category = relabel;
			
			String author = article[13];
			if(author == null || author.length()<1)
				author = "?";
			else if(author.contains(";"))
				author = author.replace(";", ",");
			Timestamp storyAppearance = Timestamp.valueOf(article[10]);
			double divideHour = 60000*60; //in hour
			long diffPubAppMS = pubTime.getTime() - storyAppearance.getTime();
			int pubAppHours = (int)((double)diffPubAppMS / divideHour) + 1;		
			int dayOfWeek = 0;
			int weekOfMonth = 0;
			SimpleDateFormat format1=new SimpleDateFormat("yyyy-MM-dd");
			try{
				Date yourDate=format1.parse(article[2].substring(0, 10));
				Calendar c = Calendar.getInstance();
				c.setTime(yourDate);
				dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
				weekOfMonth = c.get(Calendar.WEEK_OF_MONTH);
			}catch(Exception e){
				System.out.println("Error in parsing date!"+e);
			}
			
			String fcHourStr = article[7].substring(11, 13); //keep the hh part
			String fcMinuteStr = article[7].substring(14, 16); //keep the mm part
			int fcToMidMinute = 60*Integer.parseInt(fcHourStr) + Integer.parseInt(fcMinuteStr);
		
			
			//check whether title contains ? or !
			int isQuestionTitle = 0;
			int isExclamTitle = 0;
			if(title.contains("?"))
				isQuestionTitle = 1;
			if(title.contains("!"))
				isExclamTitle = 1;
			
			
			int topNum = 50;	
			/**
			//get comment id, author, content, time, parent id of all comments
			ArrayList<String[]> allCommentList = db.getAllCommentsByArticleID(articleID, con);
			totalCommentNum = allCommentList.size();
			//ignore articles if less than topNum comments
			if(allCommentList.size() < topNum)
				continue;
			ArrayList<String[]> topCommentList = new ArrayList<String[]>();
			for(int i_com=0; i_com<topNum; i_com++)
				topCommentList.add(i_com, allCommentList.get(i_com));
			//get the last comment time after removing 10%
			int commentIndexOf90 = (int)((double)totalCommentNum * 0.9);
			String timeOfLast90 = allCommentList.get(commentIndexOf90)[3];
			comLastTime = Timestamp.valueOf(timeOfLast90);
			**/
			//get comment id, author, content, time, parent id of top comment		
			ArrayList<String[]> topCommentList = db.getTopCommentsByArticleID(articleID, topNum, con);
			if(topCommentList.size() < topNum)
				continue;
			
			//get the entity info
			String enNumStr = "?";
			String fracInArtStr = "?";
			String fracInComStr = "?";
			String[] entityInfo = entityMap.get(String.valueOf(articleID));
			if(entityInfo != null)
			{
				enNumStr = entityInfo[0];
				fracInArtStr = entityInfo[1];
				fracInComStr = entityInfo[2];
			}
			else
				System.out.println("No entity info for article "+articleID);
	
			int comHours = 0;
			int pubHours = 0;
			int moreMonth = 0;
			long diffPubMS = comLastTime.getTime() - pubTime.getTime();
			long diffComMS = comLastTime.getTime() - comFirstTime.getTime();		
			pubHours = (int)((double)diffPubMS / divideHour) + 1;
			comHours = (int)((double)diffComMS / divideHour) + 1;
			if(pubHours > 30*24)
				moreMonth = 1;
			
			String rootID = (topCommentList.get(0))[4];
			int[] userReplyDelay = calculateUserReplyDelay(topCommentList, rootID, pubTime); 
			int uniqUserNum = userReplyDelay[0];//user number
			int replyNum = userReplyDelay[1];//reply number
			int delay = userReplyDelay[2]; //delay
			int interval = userReplyDelay[3]; //average interval
			//create manyTree according to the commentList
			ManyTree manyTree = new ManyTree();			
			ManyTreeNode root = manyTree.creatTree(topCommentList, rootID);
			//calculate param[6]: depth, 1~5th width
			ArrayList<Integer> param = new ArrayList<Integer>();
			for(int i=0; i<6; i++)
				param.add(0);
			param = manyTree.depthTraversalTree(root, param);
			int depth = param.get(0); //depth
			int thread = param.get(1); //width in the first level
			int maxWidth = 0;   //width
			for(int i=1; i<6; i++)
			{
				//find the max width in the first 5 levels
				if(param.get(i) > maxWidth)
					maxWidth = param.get(i);
			}
			
			//calculate content info: #WORDS, QUESTION, EXCLAMATION, COMPLEXITY
			double[] contentInfo = calculateContentInfo(topCommentList);
			int wordsNum = (int)contentInfo[0];
			int questionNum = (int)contentInfo[1];
			int exclamNum = (int)contentInfo[2];
			double complexity = contentInfo[3];	
			int hasURL = (int)contentInfo[4];	

			
//			"ID;DELAY;INTERVAL;MONTH;DAY;HOUR;TOPIC;UNIQUE USERS;THREAD;REPLYS;DEPTH;WIDTH;" //12
//			+ "WORDS NUM;NUM QUESTION;NUM EXCLAMATION;COMPLEXITY;"  //4
//			+ "NUM COM E;FRAC E A;FRAC E C;QUESTION TL;EXCLAMATION TL;URL C;"   //6
//			+ "PUB TO APP;FC TO MIDNIGHT;DAY OF WEEK;WEEK OF MONTH;AUTHOR;CATEGORY;"  //6
//			+ "VOLUME;TRUE USE;TRUE DEPTH;TRUE WIDTH;COMMENT DAYS;PUBLICATION DAYS;MONTH COMMENT"; 	//7		
			//write all info into file		
			String pubHourStr = String.valueOf(pubTime).substring(11, 13); //keep the hh part
			String pubDayStr = String.valueOf(pubTime).substring(8, 10); //keep the day part
			String pubMonthStr = String.valueOf(pubTime).substring(5, 7); //keep the month part
			DecimalFormat format = new DecimalFormat("#.####");
			String writeInfo = String.valueOf(articleID)+";"+String.valueOf(delay)+";"+String.valueOf(interval)+";"
					+pubMonthStr+";"+pubDayStr+";"+pubHourStr+";"+topic+";"+String.valueOf(uniqUserNum)+";"
					+String.valueOf(thread)+";"+String.valueOf(replyNum)+";"+String.valueOf(depth)+";"+String.valueOf(maxWidth)+";"
					+String.valueOf(wordsNum)+";"+String.valueOf(questionNum)+";"+String.valueOf(exclamNum)+";"+format.format(complexity)+";"
					+enNumStr+";"+fracInArtStr+";"+fracInComStr+";"
					+String.valueOf(isQuestionTitle)+";"+String.valueOf(isExclamTitle)+";"+String.valueOf(hasURL)+";"
					+String.valueOf(pubAppHours)+";"+String.valueOf(fcToMidMinute)+";"+String.valueOf(dayOfWeek)+";"
					+String.valueOf(weekOfMonth)+";"+author+";"+category+";"
					+String.valueOf(totalCommentNum)+";"+String.valueOf(trueUserNum)+";"+String.valueOf(trueDepthNum)+";"
					+String.valueOf(trueWidthNum)+";"+String.valueOf(comHours)+";"+String.valueOf(pubHours)+";"+String.valueOf(moreMonth);
			util.writeFileAppending(path, writeInfo);
//			System.out.println(writeInfo);
			
		}//end of each article
		util.dbClose(con);
		System.out.println("Done for outlet "+outletID);
	}
	
	public void combinePreAndLike(String prePath, HashMap<String, String[]> likeMap, String path)
	{
		//read the previous file
		FileReader fileReader = null;  
        BufferedReader bufferedReader = null; 
        String line = "";
        try{       	
        	File file = new File(prePath);
        	fileReader = new FileReader(file);  
            bufferedReader = new BufferedReader(fileReader); 
            //write the headline
            line = bufferedReader.readLine();
            String firstLine = "ID;NUM LIKE;NUM DISLIKE; NUM TOTAL LIKE INFO";
            Util util = new Util();
    		util.writeFileAppending(path, firstLine);
    		//continue read each line
            line = bufferedReader.readLine();
            while(line != null)
            {
            	if(line.length()==0)
            	{
            		line = bufferedReader.readLine();
            		continue;
            	}  
            	
            	//get articleID in this line
            	int index = line.indexOf(";");
            	String articleIDStr = line.substring(0, index);
            	//get the like info
            	String likeNumStr = "?";
        		String dislikeNumStr = "?";
        		String totalLikeNumStr = "?";
        		String[] likeInfo = likeMap.get(articleIDStr);
        		if(likeInfo != null)
        		{
        			likeNumStr = likeInfo[0];
        			dislikeNumStr = likeInfo[1];
        			totalLikeNumStr = String.valueOf(Integer.parseInt(likeNumStr)+Integer.parseInt(dislikeNumStr));
        		}
        		else
        			System.out.println("No like info for article "+articleIDStr);  
        		
        		//write the combine info
        		String writeInfo = articleIDStr+";"+likeNumStr+";"+dislikeNumStr+";"+totalLikeNumStr;
        		util.writeFileAppending(path, writeInfo);
        		
            	//go to next line
            	line = bufferedReader.readLine();
            }//end of each line
        	
        }catch(Exception e){
			System.out.println("Fail to read input file!\n"+e);
		}finally {  
            try {  
                if (bufferedReader != null) {  
                    bufferedReader.close();  
                }  
                if (fileReader != null) {  
                    fileReader.close();  
                }  
            } catch (IOException e) {  
            	System.out.println("Fail to close reader!\n"+e);
            }  
        } 
		
		
		
	}
	
	public String reAssignCategory(String category, String topicLabel)
	{
		String relabel = null;
		//articleCategory is politics or Google category
		if(category.equals("politics") || category.equals("world") || category.equals("us")
				|| category.equals("sports") || category.equals("business")|| category.equals("health")
				|| category.equals("entertainment") || category.equals("technology")|| category.equals("science"))
			relabel = category;
		else if(category.equals("sport"))
			relabel = "sports";
		else if(category.equals("tech"))
			relabel = "technology";
		else if(category.equals("us-news"))
			relabel = "us";
		//reassign article category from the topic label
		else
		{
			//replace sport, tech, us-news
			topicLabel = topicLabel.replaceAll("us-news", "us");
			topicLabel = topicLabel.replaceAll("sciencetech", "science,technology");
			if(topicLabel.contains("sport") && !topicLabel.contains("sports"))
				topicLabel = topicLabel.replaceAll("sport", "sports");
			if(topicLabel.contains("tech") && !topicLabel.contains("technology"))
				topicLabel = topicLabel.replaceAll("tech", "technology");			
			//remove category from the topic label if it is not politics or Google category
			String[] labelArray = topicLabel.split(",");
			String cleanLabel = "";
			for(String each : labelArray)
			{
				if(each.equals("politics") || each.equals("world") || each.equals("us")
						|| each.equals("sports") || each.equals("business")|| each.equals("health")
						|| each.equals("entertainment") || each.equals("technology")|| each.equals("science"))
					cleanLabel = cleanLabel + each + ",";
			}
			if(cleanLabel.length() > 0)
				relabel = cleanLabel.substring(0, cleanLabel.length()-1);;
		}
		
		return relabel;
	}
	
	public String assignCategory(long outletID, String category)
	{
		//WSP: news, blogs, politics, world, opinion, sports, us, entertainment, other
		if(outletID == 9)
		{
			if(category.equals("news"))
				category = "news";
			else if(category.equals("blogs"))
				category = "blogs";
			else if(category.equals("politics"))
				category = "politics";
			else if(category.equals("world"))
				category = "world";
			else if(category.equals("opinions"))
				category = "opinion";
			else if(category.equals("sports") || category.equals("olympics"))
				category = "sports";			
			else if(category.equals("national") || category.equals("local"))
				category = "us";
			else if(category.equals("entertainment"))
				category = "entertainment";
			else
				category = "other";
		}
		//Fox: politics, world, us, sports, opinion, entertainment, other
		else if(outletID == 18)
		{
			if(!category.equals("politics") && !category.equals("world") && !category.equals("us")
					&& !category.equals("sports") && !category.equals("opinion") && !category.equals("entertainment"))
				category = "other";
		}
		//DailyMail: news, wires, sports, entertainment, other
		else if(outletID == 142)
		{
			if(category.equals("news"))
				category = "news";
			else if(category.equals("wires"))
				category = "wires";
			else if(category.equals("sport"))
				category = "sports";
			else if(category.equals("tvshowbiz"))
				category = "entertainment";
			else
				category = "other";
		}
		//Guardian: us, world, politics, sports, entertainment, other
		else if(outletID == 26)
		{
			if(category.equals("us-news"))
				category = "us";
			else if(category.equals("world"))
				category = "world";
			else if(category.equals("politics"))
				category = "politics";
			else if(category.equals("sport") || category.equals("football"))
				category = "sports";
			else if(category.equals("film") || category.equals("music"))
				category = "entertainment";
			else
				category = "other";
		}
		//NYTimes: us, world, sports, opinion, politics, entertainment, other
		else if(outletID == 51)
		{
			if(category.equals("movies"))
				category = "entertainment";
			else if(!category.equals("politics") && !category.equals("world") && !category.equals("us")
					&& !category.equals("sports") && !category.equals("opinion"))
				category = "other";
		}
		else if(outletID == 3)
			category = "other";
		
		return category;
	}
	
	//calculate the unique user, total reply number, delay, average interval in minutes
	public int[] calculateUserReplyDelay(ArrayList<String[]> commentList, String rootID, Timestamp pubTime)
	{
		int[] res = new int[4];
		Set<String> userSet = new HashSet<String>();
		int replyNum = 0;		
		double divide = 60000; //in minute
		int interNum = commentList.size() - 1;
		long averageIntervalMS = 0;
		Timestamp preComTime = Timestamp.valueOf(commentList.get(0)[3]);  //first comment
		long delayMS = preComTime.getTime() - pubTime.getTime();
		for(String[] comment : commentList)
		{
			userSet.add(comment[1]);
			if(comment[4]!=null && !comment[4].equals(rootID))
				replyNum++;
			Timestamp curComTime = Timestamp.valueOf(comment[3]);
			long intervalMS = curComTime.getTime() - preComTime.getTime();		
			preComTime = curComTime;
			averageIntervalMS = averageIntervalMS + intervalMS;
		}
					
		res[0] = userSet.size();
		res[1] = replyNum;
		res[2] = (int)((double)delayMS / divide);
		res[3] = (int)((double)averageIntervalMS / (double)(divide*interNum));
		return res;
	}
	
	public ArrayList<String> getFirstColInfo(String filepath)
	{
		ArrayList<String> idList = new ArrayList<String>();
		//read the file
		FileReader fileReader = null;  
        BufferedReader bufferedReader = null; 
        String line = "";
        try{       	
        	File file = new File(filepath);
        	fileReader = new FileReader(file);  
            bufferedReader = new BufferedReader(fileReader); 
            //skip the head, read each line
            line = bufferedReader.readLine();
            line = bufferedReader.readLine();
            while(line != null)
            {
            	if(line.length()==0)
            	{
            		line = bufferedReader.readLine();
            		continue;
            	}  
            	
            	//split this line, add to map
            	String[] strArray = line.split(";");
            	String articleID = strArray[0];
            	idList.add(articleID);   	            	
            	
            	//go to next line
            	line = bufferedReader.readLine();
            }//end of each document
        	
        }catch(Exception e){
			System.out.println("Fail to read input file!\n"+e);
		}finally {  
            try {  
                if (bufferedReader != null) {  
                    bufferedReader.close();  
                }  
                if (fileReader != null) {  
                    fileReader.close();  
                }  
            } catch (IOException e) {  
            	System.out.println("Fail to close reader!\n"+e);
            }  
        } 
		
		return idList;
	}
	
	public HashMap<String, String[]> getEntityInfo(String filepath)
	{
		HashMap<String, String[]> entityInfoMap = new HashMap<String, String[]>();
		//read the entity file
//		String filepath = "D:\\project\\workspace\\FrontWebProject\\WebContent\\feature\\entities\\NYTimes.txt";
		FileReader fileReader = null;  
        BufferedReader bufferedReader = null; 
        String line = "";
        try{       	
        	File file = new File(filepath);
        	fileReader = new FileReader(file);  
            bufferedReader = new BufferedReader(fileReader); 
            //skip the head, read each line
            line = bufferedReader.readLine();
            line = bufferedReader.readLine();
            while(line != null)
            {
            	if(line.length()==0)
            	{
            		line = bufferedReader.readLine();
            		continue;
            	}  
            	
            	//split this line, add to map
            	String[] strArray = line.split(";");
            	String articleID = strArray[0];
            	String[] record = new String[3];
            	record[0] = strArray[1];
            	record[1] = strArray[2];
            	record[2] = strArray[3];
            	entityInfoMap.put(articleID, record);      	            	
            	
            	//go to next line
            	line = bufferedReader.readLine();
            }//end of each document
        	
        }catch(Exception e){
			System.out.println("Fail to read input file!\n"+e);
		}finally {  
            try {  
                if (bufferedReader != null) {  
                    bufferedReader.close();  
                }  
                if (fileReader != null) {  
                    fileReader.close();  
                }  
            } catch (IOException e) {  
            	System.out.println("Fail to close reader!\n"+e);
            }  
        } 
		
		return entityInfoMap;
	}
	
	public HashMap<String, String[]> getLikeInfo(String filepath)
	{
		HashMap<String, String[]> likeInfoMap = new HashMap<String, String[]>();
		//read the like file
//		String filepath = "G:\\demo\\crawler\\experiment result\\feature\\like\\WSP.txt";
		FileReader fileReader = null;  
        BufferedReader bufferedReader = null; 
        String line = "";
        try{       	
        	File file = new File(filepath);
        	fileReader = new FileReader(file);  
            bufferedReader = new BufferedReader(fileReader); 
            //skip the head, read each line
            line = bufferedReader.readLine();
            line = bufferedReader.readLine();
            while(line != null)
            {
            	if(line.length()==0)
            	{
            		line = bufferedReader.readLine();
            		continue;
            	}  
            	
            	//split this line, add to map
            	String[] strArray = line.split(";");
            	String articleID = strArray[0];
            	String[] record = new String[2];
            	record[0] = strArray[1];
            	record[1] = strArray[2];
            	likeInfoMap.put(articleID, record);      	            	
            	
            	//go to next line
            	line = bufferedReader.readLine();
            }//end of each document
        	
        }catch(Exception e){
			System.out.println("Fail to read input file!\n"+e);
		}finally {  
            try {  
                if (bufferedReader != null) {  
                    bufferedReader.close();  
                }  
                if (fileReader != null) {  
                    fileReader.close();  
                }  
            } catch (IOException e) {  
            	System.out.println("Fail to close reader!\n"+e);
            }  
        } 
		
		return likeInfoMap;
	}
	
	public String[] calculateContentItem(String content, HashSet<String> negList, HashSet<String> posList, 
			HashSet<String> hostileList, HashSet<String> angerList, HashSet<String> stopList)
	{
		//len, no stop len, #Neg, #Pos, #Hostile
		String[] res = new String[5];
		int totalWordOccurrences = 0;
		int stopWordOccurrences = 0;
		int negWordOccurrences = 0;
		int posWordOccurrences = 0;
		int hostileWordOccurrences = 0;

		//only keep words
		content = strClean(content).toLowerCase();
		String[] wordArray = content.split(" ");
		//total words, unique word and frequency
		for(String word : wordArray)
		{
			if(word.length()==0)
				continue;
			
			totalWordOccurrences++;
			if(stopList.contains(word))
				stopWordOccurrences++;
			if(negList.contains(word))
				negWordOccurrences++;
			if(posList.contains(word))
				posWordOccurrences++;
			//check hostile or anger
			if(hostileList.contains(word))
				hostileWordOccurrences++;
			else
			{
				//traverse the anger list
				for(String angerWord : angerList)
				{
					if(angerWord.contains("*"))
					{
						angerWord = angerWord.substring(0, angerWord.length()-1);
						if(word.startsWith(angerWord))
						{
							hostileWordOccurrences++;
							break;
						}
					}
					else
					{
						if(word.equals(angerWord))
						{
							hostileWordOccurrences++;
							break;
						}
					}
				}//end of anger traverse
			}
		}
		
		res[0] = String.valueOf(totalWordOccurrences);
		res[1] = String.valueOf(totalWordOccurrences - stopWordOccurrences);
		res[2] = String.valueOf(negWordOccurrences);
		res[3] = String.valueOf(posWordOccurrences);
		res[4] = String.valueOf(hostileWordOccurrences);
		return res;
	}
	
	//get #WORDS, QUESTION, EXCLAMATION, COMPLEXITY, URL
	public double[] calculateContentInfo(ArrayList<String[]> commentList)
	{
		double[] res = new double[5];
		int totalWordOccurrences = 0;
		HashMap<String, Integer> wordMap = new HashMap<String, Integer>();  //word, frequency
		//combine all comments
		String comments = "";
		for(String[] comment : commentList)
			comments = comments + comment[2] + " ";

		//check number of QUESTION, EXCLAMATION
		res[1] = comments.split("\\?").length - 1;
		res[2] = comments.split("!").length - 1;
		//check whether contains url
		if(comments.contains("http://") || comments.contains("https://"))
			res[4] = 1;
		//only keep words
		comments = strClean(comments);
		String[] wordArray = comments.split(" ");
		//total words, unique word and frequency
		for(String word : wordArray)
		{
			if(word.length()==0)
				continue;
			
			totalWordOccurrences++;
			//put the word into wordMap
			if(wordMap.containsKey(word))
			{
				//exist, increase occurrence by 1
				int occurrence = wordMap.get(word);
				wordMap.put(word, occurrence+1);
			}
			else
				wordMap.put(word, 1);
		}
		int uniqWordNum = wordMap.size();
		//calculate complexity
		double complexity = 0;
		for (Map.Entry<String, Integer> entry : wordMap.entrySet()) 
		{
	        int tf = entry.getValue();
	        double log = Math.log10(uniqWordNum) - Math.log10(tf);
	        complexity = complexity + (double)tf * log;
	    }
		complexity = complexity / (double)uniqWordNum;
		
		res[0] = totalWordOccurrences; 
		res[3] = complexity;
		return res;
	}
	
	public String strClean(String str)
	{
		//change to lower case
		str = str.toLowerCase();
		
		//remove \t, \n
		str = str.replaceAll("[\n\t]", " ");
		
		//remove html tag
		str = removeTag(str);
		
		//remove url
		String urlPattern = "http\\S+"; //http together with several non-whitespace characters
		str = str.replaceAll(urlPattern, " ");	
		
		//only keep English letters and number, use in Heap's law
	    Pattern p_script;  
	    Matcher m_script; 		
        String  regEx_nonAlp =  "[^a-zA-Z0-9]";
        p_script = Pattern.compile(regEx_nonAlp, Pattern.CASE_INSENSITIVE);  
        m_script = p_script.matcher(str);  
        str = m_script.replaceAll(" "); 
        
        //replace two or three spaces with one space
        str = str.replaceAll("  ", " ");
        str = str.replaceAll("  ", " ");
        str = str.trim();
        
        return str;
	}
	
	public String removeTag(String htmlStr)
	{
	    String textStr = "";  
	    Pattern p_script;  
	    Matcher m_script;  
	    Pattern p_style;  
	    Matcher m_style;  
	    Pattern p_html;  
	    Matcher m_html; 
	    
		//regex for script { or <script[^>]*?>[\\s\\S]*?<\\/script>  
        String regEx_script = "<[\\s]*?script[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?script[\\s]*?>";   
        //regex for style { or <style[^>]*?>[\\s\\S]*?<\\/style>  
        String regEx_style = "<[\\s]*?style[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?style[\\s]*?>"; 
        // regex for html tag
        String regEx_html = "<[^>]+>";
        
        p_script = Pattern.compile(regEx_script, Pattern.CASE_INSENSITIVE);  
        m_script = p_script.matcher(htmlStr);  
        htmlStr = m_script.replaceAll(" "); // remove script tag
        p_style = Pattern.compile(regEx_style, Pattern.CASE_INSENSITIVE);  
        m_style = p_style.matcher(htmlStr);  
        htmlStr = m_style.replaceAll(" "); // remove style tag
        
        p_html = Pattern.compile(regEx_html, Pattern.CASE_INSENSITIVE);  
        m_html = p_html.matcher(htmlStr);  
        htmlStr = m_html.replaceAll(" "); // remove html tag
        textStr = htmlStr; 
//        System.out.println(textStr.length()+", "+textStr);
        return textStr;
	}
	
	
	
	public static void main(String[] args)
	{	
		FeatureMatrix matrix = new FeatureMatrix();
		String path = "G:\\demo\\crawler\\experiment result\\feature\\1702\\first20_likef\\NYTimes.txt";
		String entityPath = "G:\\demo\\crawler\\experiment result\\feature\\entities\\NYTimes.txt";
		String likePath = "G:\\demo\\crawler\\experiment result\\feature\\like\\NYTimes.txt";
		String prePath = "G:\\demo\\crawler\\experiment result\\feature\\1702\\first20_26f\\NYTimes.txt";
		
//		long outletID = 9;//WSP
//		long outletID = 18;//Fox
//		long outletID = 142;//DailyMail
//		long outletID = 26;//Guardian
//		long outletID = 3;//WSJ
		long outletID = 51;//NYTimes
		long sinceArticleID = 0;	
		/**
		HashMap<String, String[]> map = matrix.getEntityInfo(entityPath);
		matrix.keepFeatureData(path, sinceArticleID, outletID, map);
		matrix.keepExtraFeature(path, sinceArticleID, outletID, prePath);
		**/
		//add like info to previous file
		HashMap<String, String[]> likeMap = matrix.getLikeInfo(likePath);
		matrix.combinePreAndLike(prePath, likeMap, path);
		
//		String relabel = matrix.reAssignCategory("sciencetech", "sciencetech,politics,washwire,sports");
//		System.out.println(relabel);
		
		/***
		Util util = new Util();
		Connection con = util.mysqlConnection();				
		DBOperation db = new DBOperation();
		int top = 20;
		long articleID = 9;
		Timestamp pubTime = Timestamp.valueOf("2015-10-27 22:39:00");
		ArrayList<String[]> commentList = db.getTopCommentsByArticleID(articleID, 20, con);
		String rootID = (commentList.get(0))[4];
		int[] userReplyDelay = matrix.calculateUserReplyDelay(commentList, rootID, pubTime); 
		int uniqUserNum = userReplyDelay[0];//user number
		int replyNum = userReplyDelay[1];//reply number
		int delay = userReplyDelay[2]; //delay
		int interval = userReplyDelay[3]; //average interval
		//create manyTree according to the commentList
		ManyTree manyTree = new ManyTree();			
		ManyTreeNode root = manyTree.creatTree(commentList, rootID);
		//calculate param[6]: depth, 1~5th width
		ArrayList<Integer> param = new ArrayList<Integer>();
		for(int i=0; i<6; i++)
			param.add(0);
		param = manyTree.depthTraversalTree(root, param);
		int depth = param.get(0); //depth
		int thread = param.get(1); //width in the first level
		int maxWidth = 0;   //width
		for(int i=1; i<6; i++)
		{
			//find the max width in the first 5 levels
			if(param.get(i) > maxWidth)
				maxWidth = param.get(i);
		}
		
		double[] contentInfo = matrix.calculateContentInfo(commentList);
		int wordsNum = (int)contentInfo[0];
		int isquestion = (int)contentInfo[1];
		int isexclam = (int)contentInfo[2];
		double complexity = contentInfo[3];
		
		System.out.println("rootID:"+rootID);
		System.out.println("uniqUserNum:"+uniqUserNum);
		System.out.println("replyNum:"+replyNum);
		System.out.println("depth:"+depth);
		System.out.println("thread:"+thread);
		System.out.println("maxWidth:"+maxWidth);
		System.out.println("delay:"+delay);
		System.out.println("interval:"+interval);
		System.out.println("wordsNum:"+wordsNum);
		System.out.println("isquestion:"+isquestion);
		System.out.println("isexclam:"+isexclam);
		System.out.println("complexity:"+complexity);
		**/
	}

}
