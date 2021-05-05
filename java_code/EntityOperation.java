package classification;

import java.sql.Connection;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import utilTool.Util;

public class EntityOperation {
	
	//keep each entity in low case and frequency
	public HashMap<String, Integer> findNER(String text) 
	{	
		HashMap<String, Integer> wordMap = new HashMap<String, Integer>();  //word, frequency
		String entityStr = "";
	    Properties props = new Properties();
	    props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,depparse,entitymentions");
	    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);	    
	    Annotation annotation = new Annotation(text);
	    pipeline.annotate(annotation);
	    for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) 
	    {
	      //entity mentions
	      for (CoreMap entityMention : sentence.get(CoreAnnotations.MentionsAnnotation.class)) 
	      {
	        String entity = entityMention.get(CoreAnnotations.TextAnnotation.class).toLowerCase();
	        String type = entityMention.get(CoreAnnotations.NamedEntityTagAnnotation.class);
//	        System.out.println(entity + "\t"+type);
	        //only keep the entity belongs to ORGANIZATION, PERSON, LOCATION, MISC
	        if(type.equals("ORGANIZATION") || type.equals("PERSON") || 
	        		type.equals("LOCATION") || type.equals("MISC"))
	        {
	        	entityStr = entityStr + entity + ",";
	        	System.out.println(entity);
	        	
	        	//put the entity into wordMap
    			if(wordMap.containsKey(entity))
    			{
    				//exist, increase occurrence by 1
					int occurrence = wordMap.get(entity);
					wordMap.put(entity, occurrence+1);
    			}
    			else
    				wordMap.put(entity, 1);
    				
	        }
	        	
	      }	      
	    }//end of for CoreMap
//	    System.out.println(entityStr);
	    return wordMap;
	}
	
	
	//find the intersection of entities in two entity map
	public HashSet<String> findMapIntersection(HashMap<String, Integer> artMap, HashMap<String, Integer> comMap)
	{
		HashSet<String> interSet = new HashSet<String>();
		if(artMap.size()==0 || comMap.size()==0)
			return interSet;
		//for each entity in comMap, check whether it is in artMap
		for(Map.Entry<String, Integer> entry : comMap.entrySet())
		{
			String comEntity = entry.getKey();
			if(artMap.containsKey(comEntity))
				interSet.add(comEntity);
		}
		
		return interSet;
	}
	
	
	/**
	public static void main(String[] args)
	{
		EntityOperation entity = new EntityOperation();
		String title = "Trump vs. Carson: What the doctor needs to tell The Donald at Wednesday's debate";
		entity.findNER(title);
	}
	**/
	
	
	//keep all entities in article and comments
	public static void main(String[] args)
	{
		long sinceArticleID = 0;
//		long outletID = 9;//WSP
//		long outletID = 18;//Fox
//		long outletID = 142;//DailyMail
//		long outletID = 26;//Guardian
//		long outletID = 3;//WSJ
		long outletID = 51;//NYTimes
		
		
		String path = "D:\\project\\workspace\\FrontWebProject\\WebContent\\feature\\entities\\NYTimes.txt";
		Util util = new Util();
		Connection con = util.mysqlConnection();
		DBOperation db = new DBOperation();
		EntityOperation entity = new EntityOperation();
		String firstLine = "ID;NUM COM E;FRAC E A;FRAC E C;ARTICLE ENTITY;COMMENT ENTITY";
		//write the first line
		util.writeFileAppending(path, firstLine);
		//article id, #comment, publish time, topic, #user, #depth, #width, 
		//first comment, last comment, title, story appearance
		ArrayList<String[]> articleList = db.getArticleInfoInOutlet(outletID, con);
		System.out.println("Total article size: "+articleList.size());
		for(String[] article : articleList)
		{
			//get entities in article and top comments
			long articleID = Long.parseLong(article[0]);
			String entitiesInArticle = "";
			String entitiesInComment = "";
			String fracInArtStr = "?";
			String fracInComStr = "?";
			int entitiesInComNum = 0;
			System.out.println("article: "+articleID);
			if(articleID < sinceArticleID)
				continue;
			String content = db.getArticleContentByArticleID(con, articleID);
			content = content.replaceAll("[\n\t]", " ");
			HashMap<String, Integer> wordMapInArticle = new HashMap<String, Integer>();
			if(content.length() == 0)
				System.out.println("No content in this article!");			
			else
			{
				wordMapInArticle = entity.findNER(content);
				for(Map.Entry<String, Integer> entry : wordMapInArticle.entrySet())
					entitiesInArticle = entitiesInArticle + entry.getKey() + ",";
			}			
//			System.out.println(entitiesInArticle);
			
			//get comment id, author, content, time, parent id of top 5 comment
			int topNum = 5;
			ArrayList<String[]> commentList = db.getTopCommentsByArticleID(articleID, topNum, con);
			//ignore articles if less than 5 comments
			if(commentList.size() < topNum)
				continue;
			//combine top comment content
			String comments = "";
			for(String[] comment : commentList)
				comments = comments + comment[2] + " ";
			comments = comments.replaceAll("[\n\t]", " ");
			HashMap<String, Integer> wordMapInCom = entity.findNER(comments);			
			for(Map.Entry<String, Integer> entry : wordMapInCom.entrySet())
			{
				entitiesInComment = entitiesInComment + entry.getKey() + ",";
				entitiesInComNum = entitiesInComNum + entry.getValue();
			}				
//			System.out.println(entitiesInComment);
//			System.out.println(entitiesInComNum);
			
			//get the entity intersection, calculate the fraction
			HashSet<String> interSet = entity.findMapIntersection(wordMapInArticle, wordMapInCom);
			if(wordMapInArticle.size()>0 && wordMapInCom.size()>0)
			{
				DecimalFormat format = new DecimalFormat("#.####");
				double fracInArt = (double)interSet.size() / (double)wordMapInArticle.size();
				double fracInCom = (double)interSet.size() / (double)wordMapInCom.size();
				fracInArtStr = format.format(fracInArt);
				fracInComStr = format.format(fracInCom);
			}
			//write entity info
			//ID;NUM COM E;FRAC E A;FRAC E C;ARTICLE ENTITY;COMMENT ENTITY
			String writeInfo = String.valueOf(articleID)+";"+String.valueOf(entitiesInComNum)+";"+fracInArtStr+";"
					+fracInComStr+";"+entitiesInArticle+";"+entitiesInComment;
			util.writeFileAppending(path, writeInfo);
		}//end of each article
		
		util.dbClose(con);
	}
	
   
}
