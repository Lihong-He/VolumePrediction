package classification;

import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class ReadJson {
	
	public void readJson(String filepath)
	{
		JSONParser parser = new JSONParser();
		try{
			Object obj = parser.parse(new FileReader(filepath));
			JSONArray jsonArray = (JSONArray) obj;
			System.out.println("Array size: "+jsonArray.size());
			String str = jsonArray.toString();
//			String substr = str.substring(469-5, 470+10);
//			System.out.println(substr);
			for(int i=0; i<jsonArray.size(); i++)
			{
				JSONObject eleObj = (JSONObject)jsonArray.get(i);
				String content = (String)eleObj.get("Content");;
//				System.out.println(content);
			}
			
		}catch(Exception e){
			System.out.println(e);
		}
	}
	
	public void changeBackslash(String origFile, String newFile)
	{
		File file = new File(origFile);
		FileReader fileReader = null;  
        BufferedReader bufferedReader = null; 
        BufferedWriter bw = null;
        int changeCount = 0;
        try{
        	bw = new BufferedWriter(new FileWriter(newFile, true));
        	fileReader = new FileReader(file);  
            bufferedReader = new BufferedReader(fileReader);             
            String line = bufferedReader.readLine();
            while(line != null && line.length() > 0)
            {   
            	//parse backslash in the content
            	/**
            	if(line.contains("\"Content\""))
            	{
            		if(line.contains("\'"))
            		{
            			line = line.replaceAll("\\'", "'");
            			changeCount++;
            		}
            	}
            	**/
            	if(line.contains("\'"))
        		{
        			line = line.replaceAll("\\\\'", "'");
        			changeCount++;
        		}
            	
            	bw.write(line);
				bw.newLine();
            	
            	//next line
            	line = bufferedReader.readLine();
            }
            System.out.println("Change "+changeCount);
         }catch(Exception e){
			System.out.println("Fail to read input file!\n"+e);
		}finally {  
            try {  
                if (bufferedReader != null)  
                    bufferedReader.close();   
                if (fileReader != null)   
                    fileReader.close();   
                if(bw != null)
                	bw.close();
            } catch (IOException e) {  
            	System.out.println("Fail to close reader!\n"+e);
            }  
        } 
	}
	
	
	
	public void changeCommentFile(String origFile, String newFile)
	{
		File file = new File(origFile);
		FileReader fileReader = null;  
        BufferedReader bufferedReader = null; 
        BufferedWriter bw = null;
        try{
        	bw = new BufferedWriter(new FileWriter(newFile, true));
        	fileReader = new FileReader(file);  
            bufferedReader = new BufferedReader(fileReader);             
            String line = bufferedReader.readLine();
            while(line != null && line.length() > 0)
            {   
            	//parse each key in the line
            	if(line.contains("'commentID'"))
            		line = line.replaceAll("'commentID'", "\"CommentID\"");
            	else if(line.contains("'AuthorName'"))
            		line = line.replaceAll("'AuthorName'", "\"AuthorName\"");
            	else if(line.contains("'Content'"))
            		line = line.replaceAll("'Content'", "\"Content\"");           	
            	else if(line.contains("'time'"))
            		line = line.replaceAll("'time'", "\"Time\"");
            	else if(line.contains("'ParentID'"))
            	{
            		line = line.replaceAll("'ParentID'", "\"ParentID\"");
            		line = line.replaceAll("NULL", "\"NULL\"");
            	}
            	else if(line.contains("'ArticleID'"))
            		line = line.replaceAll("'ArticleID'", "\"ArticleID\"");
            	else if(line.contains("'PosiCount'"))
            		line = line.replaceAll("'PosiCount'", "\"PosiCount\"");
            	else if(line.contains("'NegCount'"))
            		line = line.replaceAll("'NegCount'", "\"NegCount\"");
            	
            	bw.write(line);
				bw.newLine();
            	
            	//next line
            	line = bufferedReader.readLine();
            }
            System.out.println("Done with change!");
         }catch(Exception e){
			System.out.println("Fail to read input file!\n"+e);
		}finally {  
            try {  
                if (bufferedReader != null)  
                    bufferedReader.close();   
                if (fileReader != null)   
                    fileReader.close();   
                if(bw != null)
                	bw.close();
            } catch (IOException e) {  
            	System.out.println("Fail to close reader!\n"+e);
            }  
        } 
	}
	
	
	public HashSet<String> extractTopUser(int topNum, String userFile)
	{
		File file = new File(userFile);
		HashSet<String> userSet = new HashSet<String>();
		FileReader fileReader = null;  
        BufferedReader bufferedReader = null; 
        try{       	
        	fileReader = new FileReader(file);  
            bufferedReader = new BufferedReader(fileReader);             
            String line = bufferedReader.readLine();
            int lineCount = 0;
            while(line != null && line.length() > 0)
            {     
            	lineCount++;
            	//find author name
            	String[] strArray = line.split(",");
            	String name = strArray[0].trim();
            	userSet.add(name);
            	
            	if(lineCount >= topNum)
            		break;
            	
            	//next line
            	line = bufferedReader.readLine();
            }
            /***
            System.out.println("User size: "+userSet.size());
            Iterator iter = userSet.iterator();
            while (iter.hasNext()) 
                System.out.println(iter.next());
            ***/
         }catch(Exception e){
			System.out.println("Fail to read input file!\n"+e);
		}finally {  
            try {  
                if (bufferedReader != null)  
                    bufferedReader.close();   
                if (fileReader != null)   
                    fileReader.close();   
            } catch (IOException e) {  
            	System.out.println("Fail to close reader!\n"+e);
            }  
        } 
        
        return userSet;
	}
	
	
	public void keepCommentFromTopUser(int topNum, String userFile, String origCommentDire)
	{
		HashSet<String> userSet = extractTopUser(topNum, userFile); //keep top users
		//parse comments
		File file = new File(origCommentDire+"\\comments.json");
		FileReader fileReader = null;  
        BufferedReader bufferedReader = null; 
        BufferedWriter bw = null;
        try{
        	bw = new BufferedWriter(new FileWriter(origCommentDire+"\\comments_"+String.valueOf(topNum)+"2.json", true));     	
        	fileReader = new FileReader(file);  
            bufferedReader = new BufferedReader(fileReader);             
            String line = bufferedReader.readLine();
            ArrayList<String> commentObject = new ArrayList<String>();
            while(line != null && line.length() > 0)
            { 
            	//write [ or ]
            	if(line.equals("[") || line.equals("]"))
            	{
            		bw.write(line);
    				bw.newLine();
    				line = bufferedReader.readLine();
            	}
            	
            	//come to the end of this comment
            	else if(line.equals("\t}") || line.equals("\t},"))
            	{
            		commentObject.add(line);
            		//get the user name
            		String username = commentObject.get(2);
            		int beginIndex = username.indexOf("\" : \"")+5;
                	int endIndex = username.indexOf("\",", beginIndex);
                	username = username.substring(beginIndex, endIndex);
                	//check whether it is top user 
                	if(userSet.contains(username))
                	{
                		//change the comment content if 
                		//write this comment
                		for(int i=0; i<commentObject.size(); i++)
                		{
                			String info = commentObject.get(i);
                			bw.write(info);
            				bw.newLine();
                		}
                	}
                	commentObject.clear();
            		
            		line = bufferedReader.readLine();          		
            	}
            	//keep info about this comment
            	else
            	{
            		commentObject.add(line);
            		line = bufferedReader.readLine();
            	}
            	
            	
            }
            System.out.println("Done with read file!");
            
         }catch(Exception e){
			System.out.println("Fail to read input file!\n"+e);
		}finally {  
            try {  
                if(bufferedReader != null)  
                    bufferedReader.close();   
                if(fileReader != null)   
                    fileReader.close();   
                if(bw != null)
                	bw.close();
            } catch (IOException e) {  
            	System.out.println("Fail to close reader!\n"+e);
            }  
        } 
	}
	
	public void topCommentClean(String origComFile, String newComFile)
	{
		//parse comments
		File file = new File(origComFile);
		FileReader fileReader = null;  
        BufferedReader bufferedReader = null; 
        BufferedWriter bw = null;
        try{
        	bw = new BufferedWriter(new FileWriter(newComFile, true));     	
        	fileReader = new FileReader(file);  
            bufferedReader = new BufferedReader(fileReader);             
            String line = bufferedReader.readLine();
            String nextLine = bufferedReader.readLine();
            while(line != null && line.length() > 0)
            { 
            	//write [ or ]
            	if(line.equals("]"))
            	{
            		bw.write(line);
    				bw.newLine();
    				line = bufferedReader.readLine();
            	}
             }
            System.out.println("Done with read file!");
            
         }catch(Exception e){
			System.out.println("Fail to read input file!\n"+e);
		}finally {  
            try {  
                if(bufferedReader != null)  
                    bufferedReader.close();   
                if(fileReader != null)   
                    fileReader.close();   
                if(bw != null)
                	bw.close();
            } catch (IOException e) {  
            	System.out.println("Fail to close reader!\n"+e);
            }  
        } 
	}
	
	
	public void countUserComment(String filepath)
	{
		File file = new File(filepath);
		HashMap<String, Long> userMap = new HashMap<String, Long>();  //user, comment number
		FileReader fileReader = null;  
        BufferedReader bufferedReader = null; 
        try{       	
        	fileReader = new FileReader(file);  
            bufferedReader = new BufferedReader(fileReader);             
            String line = bufferedReader.readLine();
            while(line != null && line.length() > 0)
            {              	
            	if(!line.contains("\"AuthorName\""))
            	{
            		line = bufferedReader.readLine();
            		continue;
            	}
            		
            	//find author name
            	int beginIndex = line.indexOf("\" : \"")+5;
            	int endIndex = line.indexOf("\",", beginIndex);
            	String name = line.substring(beginIndex, endIndex);
            	//keep author and comment number
            	if(userMap.containsKey(name))
    			{
    				//exist, increase occurrence by 1
					long occurrence = userMap.get(name);
					userMap.put(name, occurrence+1);
    			}
    			else
    				userMap.put(name, (long)1);
            	
            	//next line
            	line = bufferedReader.readLine();
            }
            System.out.println("Done with read file!");
            
            
            //rank the users
            List<HashMap.Entry<String, Long>> list = new ArrayList<HashMap.Entry<String, Long>>(userMap.entrySet());
    		Collections.sort(list, new Comparator<HashMap.Entry<String, Long>>(){         
                public int compare(Entry<String, Long> o1, Entry<String, Long> o2) {
                    return o2.getValue().compareTo(o1.getValue());
                }            
            });	
    		//output the user distribution
    		for(HashMap.Entry<String, Long> mapping:list)
    		{
    			String user = mapping.getKey();
    			long times = mapping.getValue();
    			//keep word, freq in disk
    			System.out.println(user+ " , "+times);
    		}
            
         }catch(Exception e){
			System.out.println("Fail to read input file!\n"+e);
		}finally {  
            try {  
                if (bufferedReader != null)  
                    bufferedReader.close();   
                if (fileReader != null)   
                    fileReader.close();   
            } catch (IOException e) {  
            	System.out.println("Fail to close reader!\n"+e);
            }  
        } 
	}
	

	
	
	public static void main(String[] args)
	{
		ReadJson read = new ReadJson();
		read.readJson("G:\\demo\\crawler\\data\\chao han\\test.json");
//		read.changeBackslash("G:\\demo\\crawler\\data\\chao han\\1474.json", "G:\\demo\\crawler\\data\\chao han\\1474_2.json");
//		read.changeCommentFile("G:\\demo\\crawler\\data\\chao han\\6month\\comments_parseError.json", "G:\\demo\\crawler\\data\\chao han\\6month\\comments.json");
//		read.countUserComment("G:\\demo\\crawler\\data\\chao han\\6month\\comments.json");
//		read.keepCommentFromTopUser(300, "G:\\demo\\crawler\\data\\chao han\\6month\\userRank.csv", "G:\\demo\\crawler\\data\\chao han\\6month");
	}

}
