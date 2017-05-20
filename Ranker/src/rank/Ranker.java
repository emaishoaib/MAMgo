package rank;

import java.io.*;
import java.net.*;
import java.util.*;

import java.nio.file.*;

import java.sql.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

import org.apache.commons.io.*;
import org.apache.commons.lang3.*;

import org.tartarus.snowball.ext.*;

public class Ranker {
	
	public static double[][] tf_idf( ResultSet tag, List<String> queryArray) throws SQLException
	{
		
		double[][] tf_idf = new double[queryArray.size()][3];
		int i=0;
		for(String temp : queryArray)
		{
			tf_idf[i][0] = 0;
			tf_idf[i][1] = 0;
			tf_idf[i][2] = 0;
			
			while(tag.next())
			{
				
				if(temp.equalsIgnoreCase(tag.getString("term")))
				{
					
					tf_idf[i][0] = tf_idf[i][0] + 10 * tag.getInt("titleTag");
				
					tf_idf[i][0] = tf_idf[i][0] + 5 * tag.getInt("hTag");
				
					tf_idf[i][0] = tf_idf[i][0] + 3 * tag.getInt("boldTag");
				
					tf_idf[i][0] = tf_idf[i][0] + 2 * tag.getInt("italicTag");
				
					tf_idf[i][0] = tf_idf[i][0] + 1 * tag.getInt("contentTag");
					
					tf_idf[i][1] = tf_idf[i][1]=1;
					
					tf_idf[i][0] = tf_idf[i][0] / tag.getInt("docWordCount");
					
					i++;
					
					break;
				}
				
			}
			
			
		}
		
		return tf_idf;
	}
	
	public static double[][] tf_idf_2( List<String> doc, String query) throws SQLException
	{
		
		double[][] tf_idf = new double[1][3];
		int docSize=0;
		int i=0;
		tf_idf[1][0] = 0;
		tf_idf[1][1] = 0;
		tf_idf[1][2] = 0;
		
		for(String temp : doc)
		{
			docSize = docSize + temp.length();
			
			if(i == 0)
				
				tf_idf[1][0] = tf_idf[1][0] + 10 * StringUtils.countMatches(temp, query);
			
			else if(i == 1)
				
				tf_idf[1][0] = tf_idf[1][0] + 5 * StringUtils.countMatches(temp, query);
			
			else if(i == 2)
				
				tf_idf[1][0] = tf_idf[1][0] + 5 * StringUtils.countMatches(temp, query);
			
			else if(i == 3)
				
				tf_idf[1][0] = tf_idf[1][0] + 5 * StringUtils.countMatches(temp, query);
			
			else if(i == 4)
				
				tf_idf[1][0] = tf_idf[1][0] + 5 * StringUtils.countMatches(temp, query);
			
			else if(i == 5)
				
				tf_idf[1][0] = tf_idf[1][0] + 5 * StringUtils.countMatches(temp, query);
			
			else if(i == 6)
				
				tf_idf[1][0] = tf_idf[1][0] + 5 * StringUtils.countMatches(temp, query);
			
			else if(i == 7)
				
				tf_idf[1][0] = tf_idf[1][0] + 1 * StringUtils.countMatches(temp, query);
			
			else if(i == 8)
				
				tf_idf[1][0] = tf_idf[1][0] + 1 * StringUtils.countMatches(temp, query);
			
			if(StringUtils.countMatches(temp, query)>0)
				
				tf_idf[1][1] = 1;
			i++;
			
			
		}
		if(docSize > 0)
			
		tf_idf[1][0] = tf_idf[1][0] /docSize;
		
		return tf_idf;
	}
	
	public static List<String> phrase(ResultSet res)
	{
		Document doc = Jsoup.parse("<!DOCTYPE html>"
        		+ "<html>"
                + "<title>The Zoo Zoo Zoo Zoo Zoo</title>"
                + "<h1>Animal Kingdom: Cheetah in Zoo</h1>"
                + "<p>Many <a href = 'wikipedia.org/cheetah'>cheetahs</a> are taken care of here</p>"
                + "<p>The zoo also harbors various other animals, and not only cheetah and they are very great.</p>"
                + "</html>");
		
		Elements title = doc.select("title");
        Elements p = doc.select("p");
        Elements a = doc.select("a");
        Elements h1 = doc.select("h1");
        Elements h2 = doc.select("h2");
        Elements h3 = doc.select("h3");
        Elements h4 = doc.select("h4");
        Elements h5 = doc.select("h5");
        Elements h6 = doc.select("h6");

        //Extracting the string from Elements for each tag
        String titleStr = title.text();
        String pStr = p.text();
        String aStr = a.text();
        String h1Str = h1.text();
        String h2Str = h2.text();
        String h3Str = h3.text();
        String h4Str = h4.text();
        String h5Str = h5.text();
        String h6Str = h6.text();

        //Cleaning the string by replacing everything 
        //that is not a word character (a-1, 0-9, _); all what would be left is spaces
        //between each word
        titleStr = titleStr.replaceAll("[^\\w\\s]",""); 
        pStr = pStr.replaceAll("[^\\w\\s]",""); 
        aStr = aStr.replaceAll("[^\\w\\s]",""); 
        h1Str = h1Str.replaceAll("[^\\w\\s]","");
        h2Str = h2Str.replaceAll("[^\\w\\s]","");
        h3Str = h3Str.replaceAll("[^\\w\\s]","");
        h4Str = h4Str.replaceAll("[^\\w\\s]","");
        h5Str = h5Str.replaceAll("[^\\w\\s]","");
        h6Str = h6Str.replaceAll("[^\\w\\s]","");
        
    	List <String> docString = new ArrayList<String>();
        
        docString.add(titleStr);
        docString.add(h1Str);
        docString.add(h2Str);
        docString.add(h3Str);
        docString.add(h4Str);
        docString.add(h5Str);
        docString.add(h6Str);
        docString.add(pStr);
        docString.add(aStr);
        
		return docString;
	}
	
	public static void main(String[] args) 
	{	
	String query="";
	String url = "jdbc:mysql://localhost:3306/";
	String user = "root";
	String password = "";
	
	try{
		
	//Connecting to the database
	Class.forName("com.mysql.jdbc.Driver").newInstance();
	Connection con = DriverManager.getConnection(url, user, password);
	
	Statement stt = con.createStatement();
	stt.execute("USE search");
	
	ResultSet res;
	ResultSet Count;
	ResultSet Total;
	
	//Reading last recorded docID in database
	res = stt.executeQuery("SELECT docID FROM results");
	Count = stt.executeQuery("SELECT COUNT(docID) FROM results");
	Total = stt.executeQuery("SELECT COUNT(docID) FROM doc_links");
	int countRow = Count.getInt(0);
	int TCount = Total.getInt(0);
	int type=0;
	List <String> queryArray = null;
	List <String> docString = null;
	
	if(!query.startsWith("\"") && !query.endsWith("\""))
	{
		type=1;
		
		queryArray = new ArrayList<String> (Arrays.asList(query.split(" +")));
		PorterStemmer portStem = new PorterStemmer();
		int length = queryArray.size();
		
		for(int i = 0; i < length; i++)
		{
			String termStem = "";
			portStem.setCurrent(queryArray.get(i));
			portStem.stem();
			termStem = portStem.getCurrent();
			if(!queryArray.get(i).equalsIgnoreCase(termStem))
			{
				queryArray.add(termStem);
			}
		}
	}
	else
	{
		type=2;
		
	}
	
	double[][][] my_results_tf = new double[countRow][queryArray.size()][3];
	double[][][] my_results_tf_phrase = new double[countRow][1][3];
		
			
	//while (res.next())
		for(int i = 0; i < countRow; i++)
		{
			ResultSet tag;
			tag = stt.executeQuery("SELECT * FROM tag_index WHERE docID=" +res.getInt("docID"));
			res.next();
			if(type == 1)
				
			{
				
				double[][] temp = new double[queryArray.size()][2];
				temp = tf_idf( tag , queryArray );
				my_results_tf[i]=temp;
					
			}
			
			else if(type==2)
			{
				docString=phrase(res);
				double[][] temp = new double[1][2];
				temp = tf_idf_2( docString , query );
				my_results_tf_phrase[i]=temp;
					
					
			}
			
			
			
		}
		
		
			if(type == 1)
				
			{
				for(int i = 0; i <  queryArray.size(); i++)
				{
					double[] idf= new double[queryArray.size()];
						
					for(int j = 0; j < countRow; j++)
					{
						
						idf[i] = idf[i] + my_results_tf[j][i][1];
						my_results_tf[j][i][2] = 0;
					}	
					
				}
				
				for(int i = 0; i <  queryArray.size(); i++)
				{
					double[] idf= new double[queryArray.size()];
						
					for(int j = 0; j < countRow; j++)
					{
						if(idf[i]>0)
						{
							my_results_tf[j][i][1] = TCount/ idf[i];
							my_results_tf[j][i][2] = my_results_tf[j][i][0] * my_results_tf[j][i][1];
						}
					}	
					
				}
				ResultSet send=stt.executeQuery("SELECT docID FROM results");
				for(int i = 0; i <  countRow; i++)
				{
					
					double total = 0;
					
					for(int j = 0; j < queryArray.size(); j++)
					{
						total = total + my_results_tf[i][j][2];	
					}	
					stt.executeQuery("UPDATE results SET docRank = " + total + " WHERE docID = " + send.getInt("docID"));
					send.next();
				}
				
				
				
			}
			
			else if(type==2)
			{
				double idf= 0;
				
				for(int j = 0; j < countRow; j++)
				{
					idf = idf + my_results_tf_phrase[j][0][1];
					my_results_tf_phrase[j][0][2] = 0;
				}
				
				if(idf>0)
				{
				
					for(int j = 0; j < countRow; j++)
					{
						my_results_tf_phrase[j][0][1] = TCount / idf;
						my_results_tf_phrase[j][0][2] = my_results_tf_phrase[j][0][0] * my_results_tf_phrase[j][0][1];
					}
				}
				
				ResultSet send=stt.executeQuery("SELECT docID FROM results");
				for(int i = 0; i <  countRow; i++)
				{
					
					double total = my_results_tf[i][0][2];
					stt.executeQuery("UPDATE results SET docRank = " + total + " WHERE docID = " + send.getInt("docID"));
					send.next();
				}
				
					
			}
		
	
	}
	
	catch(Exception error){
		
		System.out.println(error.getMessage());
		
	}
	
		
	}
}
	
