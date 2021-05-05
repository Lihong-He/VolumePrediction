package classification;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;

import utilTool.Util;

public class RateDistribution {
	
	public static void main(String[] args)
	{
//		String prePathRate = "G:\\demo\\crawler\\experiment result\\feature\\1702\\first20_rate\\NYTimes_1.txt";
		String prePathTotal = "G:\\demo\\crawler\\experiment result\\feature\\1702\\first20_cumulativeNum\\WSP_1.txt";
		String rateNormalizedpath = "G:\\demo\\crawler\\experiment result\\feature\\1702\\first20_rateNormalized\\WSP_1.txt";
		long sinceArticleID = 0;	
		
		long outletID = 9;//WSP
//		long outletID = 18;//Fox
//		long outletID = 142;//DailyMail
//		long outletID = 26;//Guardian
//		long outletID = 3;//WSJ
//		long outletID = 51;//NYTimes
		
		RateDistribution dis = new RateDistribution();
		//generate cumulative number distribution
//		dis.getNumberDistribution(ratePath, outletID, prePath, sinceArticleID);
		//generate rate distribution
//		dis.getRateDistribution(ratePath, outletID, prePath, sinceArticleID);
		//generate normalized rate distribution
		dis.getNormalizedRateDistribution(rateNormalizedpath, outletID, prePathTotal);
	}
	
	public void getNormalizedRateDistribution(String path, long outletID, String prePathTotal)
	{
		String firstLine = "AID,1h,2h,3h,...";
		Util util = new Util();
		//write the first line
		util.writeFileAppending(path, firstLine);
		
		//read the cumulative number file 
		FileReader fileReader = null;  
        BufferedReader bufferedReader = null; 
        String line = "";
        try{       	
        	File file = new File(prePathTotal);
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
            	String[] numArray = line.split(",");
            	String articleID = numArray[0];
            	double totalNum = Double.parseDouble(numArray[numArray.length-1]);
            	
            	ArrayList<String> comRateNormUtil = new ArrayList<String>();
            	//calculate the rate per hour
            	int preNum = 0;
            	for(int i=1; i<numArray.length; i++)
            	{
            		int curNum = Integer.parseInt(numArray[i]);
            		//count of new comments in this hour
            		int numDiff = curNum - preNum;
            		//percentage of new comments based on total
            		double percentage = (double)(numDiff*100)/totalNum;
            		preNum = curNum;
            		DecimalFormat format = new DecimalFormat("#.##");
            		comRateNormUtil.add(format.format(percentage));
            	}
            	  	            	
            	//write the comment number into the file
    			String writeInfo = articleID+",";
    			for(String rateStr: comRateNormUtil)
    				writeInfo = writeInfo + rateStr + ",";
    			writeInfo = writeInfo.substring(0, writeInfo.length()-1);
    			util.writeFileAppending(path, writeInfo);
    			
            	//go to next line
            	line = bufferedReader.readLine();
            }//end of each line for article
            System.out.println("Done for outlet "+outletID);
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
	
	public void getRateDistribution(String path, long outletID, String prePath, long sinceArticleID)
	{
		String firstLine = "AID,1h,2h,3h,...";
//		String firstLine = "AID,0.5h,1h,1.5h,...";
		Util util = new Util();
		Connection con = util.mysqlConnection();
		//write the first line
		util.writeFileAppending(path, firstLine);
		
		//read the cumulative number file
		FileReader fileReader = null;  
        BufferedReader bufferedReader = null; 
        String line = "";
        try{       	
        	File file = new File(prePath);
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
            	String[] numArray = line.split(",");
            	String articleID = numArray[0];
            	
            	ArrayList<String> comRateUtil = new ArrayList<String>();
            	//calculate the rate per hour
            	int preNum = 0;
            	for(int i=1; i<numArray.length; i++)
            	{
            		int curNum = Integer.parseInt(numArray[i]);
            		int numDiff = curNum - preNum;
            		preNum = curNum;
            		comRateUtil.add(String.valueOf(numDiff));
            	}
            	  	            	
            	//write the comment number into the file
    			String writeInfo = articleID+",";
    			for(String rateStr: comRateUtil)
    				writeInfo = writeInfo + rateStr + ",";
    			writeInfo = writeInfo.substring(0, writeInfo.length()-1);
    			util.writeFileAppending(path, writeInfo);
    			
            	//go to next line
            	line = bufferedReader.readLine();
            }//end of each line for article
            System.out.println("Done for outlet "+outletID);
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
	
	public void getNumberDistribution(String path, long outletID, String prePath, long sinceArticleID)
	{
		String firstLine = "AID,1h,2h,3h,...";
//		String firstLine = "AID,0.5h,1h,1.5h,...";
		Util util = new Util();
		Connection con = util.mysqlConnection();
		//write the first line
		util.writeFileAppending(path, firstLine);
				
		DBOperation db = new DBOperation();	
		FeatureMatrix feature = new FeatureMatrix();
		//get the article ID from previous file
		ArrayList<String> idList = feature.getFirstColInfo(prePath);
		for(String id : idList)
		{
			long articleID = Long.parseLong(id);
			if(articleID < sinceArticleID)
				continue;
			ArrayList<String[]> commentList = db.getAllCommentsTimeByArticleID(articleID, con);
			Timestamp firstComTime = Timestamp.valueOf(commentList.get(0)[1]);
			int addtoHour = 1;
			ArrayList<String> comNumUtil = new ArrayList<String>();
			double eachBucketMs = 60*60*1000;
//			double eachBucketMs = 30*60*1000;
			boolean keepFlag = false;
			for(int i=0; i<commentList.size(); i++)
			{
				String[] comment = commentList.get(i);
				Timestamp thisComTime = Timestamp.valueOf(comment[1]);
				long diffInMs = thisComTime.getTime()-firstComTime.getTime();
				int index = (int)((double)diffInMs/eachBucketMs);
				
				//it is time to keep cumulative number of comments
				if(index >= addtoHour)
					keepFlag = true;
				if(keepFlag)
				{
					//keep current number until addtoHour reaches index
					while(addtoHour <= index)
					{
						comNumUtil.add(String.valueOf(i));
						addtoHour = addtoHour + 1;
					}					
					keepFlag = false;
				}								
			}//end of traversing the comment list
			//keep the total comment number into the last bucket
			comNumUtil.add(String.valueOf(commentList.size()));
			
			//write the comment number into the file
			String writeInfo = id+",";
			for(String numStr: comNumUtil)
				writeInfo = writeInfo + numStr + ",";
			writeInfo = writeInfo.substring(0, writeInfo.length()-1);
			util.writeFileAppending(path, writeInfo);
		}//end of each article
		util.dbClose(con);
		System.out.println("Done for outlet "+outletID);
	}

}
