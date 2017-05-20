
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
		//Connecting and performing operations on database
		try
		{
			//Connecting to the database
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection con = DriverManager.getConnection(url, user, password);
			
			//Select database 'search'
			Statement stt = con.createStatement();
			stt.execute("USE search");
			ResultSet res = null;
			ResultSet resRec = null;
			
			//Checking if there are any pages that need to be indexed
			resRec = stt.executeQuery("SELECT * FROM doc_links WHERE isIndexed = 0");
			if (!resRec.next())
			{
				System.out.println("No pages to index!");
				
				//Putting the pointer back to where it was, just in case
				resRec.beforeFirst();
				
				return;
			}
			
			//Shifting pointer backwards one step, to start from true next
			resRec.beforeFirst();
			
			//Iterate over the entire result set of docs that haven't been indexed
			while (resRec.next())
			{				
				//Get the value of current record's docID column
				int currID = resRec.getInt("docID");
				
				//Setting the result record as being indexed
				stt.execute("UPDATE doc_links"
						+ " SET isIndexed = 1"
						+ " WHERE docID = " + currID);
				
				//Parsing the doc's corresponding HTML file
				File file = new File(htmls_folder + currID + ".html");
	            Document doc = Jsoup.parse(file, "UTF-8");
	            
				//Extracting the first <title> tag from the HTML doc
				Element titleFirst = doc.select("title").first();
				String titleFirstStr = titleFirst.text();
				
				//Storing the doc's title
				stt.execute("UPDATE doc_links"
						+ " SET docTitle = '" + titleFirstStr + "'"
						+ " WHERE docID = " + currID);
				
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

	            //Getting total count of words in document
	            int wordCount = hArr.length + contentArr.length;
	            
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
	                indexTerm(stt, res, "titleTag", currID, term, termStem);
	                
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
	                indexTerm(stt, res, "hTag", currID, term, termStem);
	                
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
	                indexTerm(stt, res, "boldTag", currID, term, termStem);
	                
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
	                indexTerm(stt, res, "italicTag", currID, term, termStem);
	                
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
		                indexTerm(stt, res, "contentTag", currID, term, termStem); 
		                
		                //Tag number is the current index 'i'
		                tagNum = i;	
		                
		                //Position of the term in the current tag number is the current index 'j'
		                pos = j;
		                
		                //Indexing position
		                indexPos(stt, currID, term, termStem, tagNum, pos);
	            	}
	            }
	            
	            System.out.println("Indexed document # " + currID);
			}
			
			System.out.println("Indexing complete!");
		}
		catch (Exception error)
		{
			System.out.println(error.getMessage());
		}
	}
}