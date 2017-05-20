
package indexer;

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


public class Indexer
{	
	//Declaring important variables
	private static String state_txt = "C:\\xampp\\htdocs\\MAMgo\\_crawler\\state.txt";
	private static String links_txt = "C:\\xampp\\htdocs\\MAMgo\\_crawler\\links.txt";
	private static String htmls_folder = "C:\\xampp\\htdocs\\MAMgo\\_crawler\\HTMLs\\";
	
	private static String url = "jdbc:mysql://localhost:3306/";
	private static String user = "root";
	private static String password = "";
	
	private static void indexTerm(Statement stt, ResultSet res, String tagType, int lastID, String term, String termStem) throws SQLException
	{
		//Selecting original form from DB, to check if existent for the resp. doc
        res = stt.executeQuery("SELECT term"
        		+ " FROM tag_index"
        		+ " WHERE"
        		+ " term = '" + term + "' AND"
        		+ " docID = " + lastID);
        
        //If original form of term not in database, then add it
        if (res.next() == false)
        	stt.execute("INSERT INTO tag_index (term, docID, " + tagType + ")"
        			+ " VALUES ('" + term + "', " + lastID + " , 1)");
        //...otherwise, increment its count
        else
        	stt.execute("UPDATE tag_index"
        			+ " SET " + tagType + " = " + tagType + " + 1"
        			+ " WHERE term = '" + term + "'");
        
        //Only if original form is different from stem, do the same for stemmed form
        if (!termStem.equals(term))
        {
        	//Selecting stemmed form from DB, to check if existent for the resp. doc
        	res = stt.executeQuery("SELECT term"
        			+ " FROM tag_index"
        			+ " WHERE"
        			+ " term = '" + termStem + "' AND"
        			+ " docID = " + lastID);
        
        	//If stemmed form of term not in database, then add it
        	if (res.next() == false)
        		stt.execute("INSERT INTO tag_index (term, docID, " + tagType + ")"
        				+ " VALUES ('" + termStem + "', " + lastID + ", 1)");
        	//...otherwise, increment its count
        	else
        		stt.execute("UPDATE tag_index"
            			+ " SET " + tagType + " = " + tagType + " + 1"
            			+ " WHERE term = '" + termStem + "'");	
        }
	}
	
	private static void indexPos(Statement stt, int lastID, String term, String termStem, int tagNum, int pos) throws SQLException
	{
		//Insert original form of term along with its position in the resp. tag number
        stt.execute("INSERT INTO pos_index (term, docID, tagNum, posNum)"
        		+ " VALUES ('" + term + "', " + lastID + ", " + tagNum + ", " + pos + ")");
        	
        //If stemmed form is not same as original form, then do the same for stemmed form
        if (!termStem.equals(term))
        	stt.execute("INSERT INTO pos_index (term, docID, tagNum, posNum)" 
        			+ " VALUES ('" + termStem + "', " + lastID + ", " + tagNum + ", " + pos + ")");	
	}
	
	public static void main(String[] args) 
	{	
		//Reading current state number
		String fileText;
		int stateNum = 0;
		try
		{
			BufferedReader stateReader = new BufferedReader(new FileReader(state_txt));
			fileText = stateReader.readLine();
			stateNum = Integer.parseInt(fileText);
			
			stateReader.close();
		}
		catch (FileNotFoundException e)
		{
			System.out.println("File not found!");
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		//Connecting and performing operations on database
		try
		{
			//Connecting to the database
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection con = DriverManager.getConnection(url, user, password);
			
			//Select database 'search'
			Statement stt = con.createStatement();
			stt.execute("USE search");
			ResultSet res;
			
			//Reading last recorded docID in database
			int lastID = -1;
			res = stt.executeQuery("SELECT * FROM doc_links");
			if (res.next())
			{
				res = stt.executeQuery("SELECT MAX(docID) AS docID FROM doc_links");
				res.next();
				lastID = res.getInt("docID");	
			}
			
			//If current state number is equal to last docID + 1, then no new pages to index
			if (stateNum - lastID == 1)
			{
				System.out.println("No pages to index!");
				return;
			}
			
			//While current state number is not equal to last docID + 1, then keep indexing
			while (stateNum - lastID != 1)
			{
				//Next doc ID to be indexed
				lastID++;
				
				//Selecting link, respective of doc ID to be indexed
	            BufferedReader linksReader = new BufferedReader(new FileReader(links_txt));
				String link = null;
				for (int i = -1; i < lastID; i++)
				{
					link = linksReader.readLine();
				}				
				linksReader.close();
				
				//Parsing the doc's corresponding HTML file
				File file = new File(htmls_folder + lastID + ".html");
	            Document doc = Jsoup.parse(file, "UTF-8");
	            
				//Extracting the first <title> tag from the HTML doc
				Element titleFirst = doc.select("title").first();
				String titleFirstStr = titleFirst.text();
				
				//Storing the doc's ID, title and link in database
				stt.execute("INSERT INTO doc_links (docID, docTitle, docLink)"
						+ " VALUES (" + lastID + ", '" + titleFirstStr + "', '" + link + "')");

				//NOTE: Elements is a list of Element objects

	            //Extracting the desired tags from the parsed HTML doc
				Elements titleAll = doc.select("title");
				Elements hAll = doc.select("h1, h2, h3, h4, h5, h6");
				Elements boldAll = doc.select("b, strong");
				Elements italicAll = doc.select("i, em");
	            
	            //Getting the combined text for each tag
	            String titleStr = titleAll.text();
	            String hStr = hAll.text();
	            String boldStr = boldAll.text();
	            String italicStr = italicAll.text();
	            
	            //Lower casing each tag's string
	            titleStr = titleStr.toLowerCase();
	            hStr = hStr.toLowerCase();
	            boldStr = boldStr.toLowerCase();
	            italicStr = italicStr.toLowerCase();   
	            
	            //Cleaning the string - results in only letters and spaces
	            titleStr = titleStr.replaceAll("[^\\w\\s]",""); 
	            hStr = hStr.replaceAll("[^\\w\\s]","");
	            boldStr = boldStr.replaceAll("[^\\w\\s]","");
	            italicStr = italicStr.replaceAll("[^\\w\\s]","");
	            
	            //Token the string (the '+' is there to indicate one or more spaces)
	            String[] titleArr = titleStr.split(" +");
	            String[] hArr = hStr.split(" +");
	            String[] boldArr = boldStr.split(" +");
	            String[] italicArr = italicStr.split(" +"); 

	            //Extracting the content of the HTML using <html> tag
	            Elements contentAll = doc.select("html");

				//Removing <script> and <style> tags from the content of the HTML
	            contentAll.select("script").remove();
	            contentAll.select("style").remove();
	            
				//Assuming there is always one <html> tag, thus of interest is only first one
				Element content = contentAll.first();
				
				//Getting total count of words in HTML doc
				String[] wordArray = content.text().trim().split("\\s");
				int wordCount = wordArray.length;
				
				//Storing the total word count for the resp. doc ID in database
				stt.execute("UPDATE doc_links"
						+ " SET docWordCount = " + wordCount
						+ " WHERE docID = " + lastID);
				
				//Getting the own text of each tag within content, then...
				//...lower-casing, cleaning and storing each in an array...
	            Elements contentTagsWithin = content.getAllElements();
	            int size = contentTagsWithin.size();
	            
	            Element currTag = null;
	            String currTagText = null;	
	            
	            int index = 0;
	            String contentArr[] = new String[size];

	            for (int i = 0; i < size; ++i)
	            {
	            	currTag = contentTagsWithin.get(i);
	            	currTagText = currTag.ownText();
	            	
	            	if (!currTagText.isEmpty())
	            	{
	            		currTagText = currTagText.toLowerCase();
	            		currTagText = currTagText.replaceAll("[^\\w\\s]","");
	            		
	            		contentArr[index] = currTagText;
	            		index++;
	            	}
	            	
	            }
	                 

	            //Declaring variables for counting, stemming and indexing
	            PorterStemmer portStem = new PorterStemmer();
	            String term = "";
	            String termStem = "";
	            int tagNum = -1;
	            int pos = -1;

	            //Stemming and indexing each term, then storing count, under the...
	            
	            //...<title> tag     
	            for(int i = 0; i < titleArr.length; ++i)
	            {          
	            	//Getting the current word
	            	term = titleArr[i];
	            	
	            	//If term is empty, do nothing and move to next index
	            	if (term.isEmpty())
	            		continue;
	            	
	            	//Stemming
	                portStem.setCurrent(term);
	                portStem.stem();
	                termStem = portStem.getCurrent();
	                
	                //Indexing term
	                indexTerm(stt, res, "titleTag", lastID, term, termStem);
	                
	                //Indexing position done in content as this tag is included there  
	            }
	            
	            //...tags <h1>, <h2>...<h6>
	            for(int i = 0; i < hArr.length; ++i)
	            {          
	            	//Getting the current word
	            	term = hArr[i];
	            	
	            	//If term is empty, do nothing and move to next index
	            	if (term.isEmpty())
	            		continue;
	            	
	            	//Stemming
	                portStem.setCurrent(term);
	                portStem.stem();
	                termStem = portStem.getCurrent();
	                
	                //Indexing term
	                indexTerm(stt, res, "hTag", lastID, term, termStem);
	                
	                //Indexing position done in content as this tag is included there
	            }
	            
	            //...tag <bold>
	            for(int i = 0; i < boldArr.length; ++i)
	            {          
	            	//Getting the current word
	            	term = boldArr[i];
	            	
	            	//If term is empty, do nothing and move to next index
	            	if (term.isEmpty())
	            		continue;
	            	
	            	//Stemming
	                portStem.setCurrent(term);
	                portStem.stem();
	                termStem = portStem.getCurrent();
	                
	                //Indexing term
	                indexTerm(stt, res, "boldTag", lastID, term, termStem);
	                
	                //Indexing position done in content as this tag is included there
	            }
	            
	            //...tag <italic>
	            for(int i = 0; i < italicArr.length; ++i)
	            {          
	            	//Getting the current word
	            	term = italicArr[i];
	            	
	            	//If term is empty, do nothing and move to next index
	            	if (term.isEmpty())
	            		continue;
	            	
	            	//Stemming
	                portStem.setCurrent(term);
	                portStem.stem();
	                termStem = portStem.getCurrent();
	                
	                //Indexing term
	                indexTerm(stt, res, "italicTag", lastID, term, termStem);
	                
	                //Indexing position done in content as this tag is included there
	            }
	            
	            //...content
	            String tagText = "";
	            for(int i = 0; i < contentArr.length; ++i)
	            {                      		            
	            	//Getting the current word
	            	tagText = contentArr[i];
	            	
	            	//Just in case term is null
	            	if (tagText == null)
	            		continue;
	            	
	            	//Splitting into String array on one or many spaces
	            	String[] tagArr = tagText.split(" +");
            		
	            	for (int j = 0; j < tagArr.length; ++j)
	            	{	
	            		//Storing the individual term
	            		term = tagArr[j];
	            			 
	            		//If term is empty, do nothing and move to next index
		            	if (term.isEmpty())
		            		continue;
		            	
	            		//Stemming
		                portStem.setCurrent(term);
		                portStem.stem();
		                termStem = portStem.getCurrent();

		                //Indexing term
		                indexTerm(stt, res, "contentTag", lastID, term, termStem); 
		                
		                //Tag number is the current index 'i'
		                tagNum = i;	
		                
		                //Position of the term in the current tag number is the current index 'j'
		                pos = j;
		                
		                //Indexing position
		                indexPos(stt, lastID, term, termStem, tagNum, pos);
	            	}
	            }
	            
	            System.out.println("Indexed document # " + lastID);
			}
			
			System.out.println("Indexing complete!");
		}
		catch (Exception error)
		{
			System.out.println(error.getMessage());
		}
	}
}