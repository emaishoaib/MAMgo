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

public class Ranker 
{
	private static String htmls_folder = "C:\\xampp\\htdocs\\MAMgo\\_crawler\\HTMLs\\";
	
	private static double[][] tf_idf( ResultSet tag, List<String> queryArray) throws SQLException
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
	
	private static double[][] tf_idf_2( List<String> doc, String query) throws SQLException
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
				
				tf_idf[1][0] = tf_idf[1][0] + 1 * StringUtils.countMatches(temp, query);
			
			if(StringUtils.countMatches(temp, query)>0)
				tf_idf[1][1] = 1;
			
			i++;
			
			
		}
		if(docSize > 0)
			
		tf_idf[1][0] = tf_idf[1][0] /docSize;
		
		return tf_idf;
	}
	
	private static List<String> phrase(ResultSet res) throws SQLException, IOException
	{
		//Parsing the doc's corresponding HTML file
		File file = new File(htmls_folder + res.getInt("docID") + ".html");
        Document doc = Jsoup.parse(file, "UTF-8");
        
		Elements title = doc.select("title");
        Elements h = doc.select("h1, h2, h3, h4, h5, h6");
        
        //Extracting the content of the HTML using <html> tag
        Elements contentAll = doc.select("html");

		//Removing <script> and <style> tags from the content of the HTML
        contentAll.select("script").remove();
        contentAll.select("style").remove();
        
		//Assuming there is always one <html> tag, thus of interest is only first one
		Element content = contentAll.first();

        //Extracting the string from Elements for each tag
        String titleStr = title.text();
        String hStr = h.text();
        String contentStr = content.text();

        //Cleaning the string by replacing everything 
        //that is not a word character (a-1, 0-9, _); all what would be left is spaces
        //between each word
        titleStr = titleStr.replaceAll("[^\\w\\s]",""); 
        hStr = hStr.replaceAll("[^\\w\\s]","");
        contentStr = contentStr.replaceAll("[^\\w\\s]","");
        
    	List <String> docString = new ArrayList<String>();
        
        docString.add(titleStr);
        docString.add(hStr);
        docString.add(contentStr);
        
		return docString;
	}
	
	private static boolean rankDocs(String queryReceived)
	{
		String query = queryReceived;
		String url = "jdbc:mysql://localhost:3306/search?useSSL=false";
		String user = "root";
		String password = "";
		
		try{
			
		//Connecting to the database
		Class.forName("com.mysql.jdbc.Driver").newInstance();
		Connection con = DriverManager.getConnection(url, user, password);
		
		Statement sttRes = con.createStatement();
		Statement sttCount = con.createStatement();
		Statement sttTotal = con.createStatement();
		Statement sttTag = con.createStatement();
		Statement sttSend = con.createStatement();
		Statement sttTemp = con.createStatement();
		
		ResultSet res;
		ResultSet Count;
		ResultSet Total;
		
		//Reading last recorded docID in database
		res = sttRes.executeQuery("SELECT docID FROM results");
		Count = sttCount.executeQuery("SELECT COUNT(docID) FROM results");
		Total = sttTotal.executeQuery("SELECT COUNT(docID) FROM doc_links");
		
		Count.next();
		Total.next();
		
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
			
				
		res.next();
		
		//while (res.next())
			for(int i = 0; i < countRow; i++)
			{
				ResultSet tag;
				tag = sttTag.executeQuery("SELECT * FROM tag_index WHERE docID=" +res.getInt("docID"));

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
				
				res.next();
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
					
					ResultSet send = sttSend.executeQuery("SELECT docID FROM results");
					for(int i = 0; i <  countRow; i++)
					{
						
						double total = 0;
						
						for(int j = 0; j < queryArray.size(); j++)
						{
							total = total + my_results_tf[i][j][2];	
						}	
						sttTemp.executeQuery("UPDATE results SET docRank = " + total + " WHERE docID = " + send.getInt("docID"));
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
					
					ResultSet send=sttSend.executeQuery("SELECT docID FROM results");
					for(int i = 0; i <  countRow; i++)
					{
						
						double total = my_results_tf[i][0][2];
						sttTemp.executeQuery("UPDATE results SET docRank = " + total + " WHERE docID = " + send.getInt("docID"));
						send.next();
					}
					
						
				}
		}
		
		catch(Exception error)
		{	
			System.out.println(error.getMessage());	
		}
		
		return true;
	}

	public static void main(String[] args) throws Exception
	{
		// Declaring ServerSockets
		ServerSocket serverSocket_r = null;
		ServerSocket serverSocket_wr = null;
		
		// Declaring Sockets
		Socket socket_r = null;
		Socket socket_wr = null;
		
		String query = null;
		
		// Create a new socket at port 1245 (Will be used to read string from PHP)
		try
		{
			serverSocket_r = new ServerSocket(1245);
		}
		catch (IOException e)
		{
			System.out.println("Could not listen to port 1245");
			System.exit(-1);
		}
		
		// Create a new socket at port 1246 (Will be used to send string to PHP)
		try
		{
			serverSocket_wr = new ServerSocket(1246);
		}
		catch (IOException e)
		{
			System.out.println("Could not listen to port 1246");
			System.exit(-1);
		}
		
		// Keep running indefinitely (terminates manually)
		while(true)
		{
			// Listen to port 1245 for reading
			try
			{
				socket_r = serverSocket_r.accept();
			}
			catch (Exception e)
			{
				System.out.println("Accept failed");
				System.exit(-1);
			}
			
			// Read string from port 1245
			try
			{
				InputStreamReader isr = new InputStreamReader(socket_r.getInputStream());
				BufferedReader br = new BufferedReader(isr);
				
				query = br.readLine();
			}
			catch (Exception e)
			{
				System.out.println("String reading on port from client failed");
				System.exit(-1);
			}
			
			//Printing in console message indicating permission received from PHP
			System.out.println("Permission to rank table 'results' received");
			
			boolean result = false;
			String rankerResponse = "";
			
			//Execute method to rank the documents in table 'results'
			if(query != null)
				result = rankDocs(query);
			
			//If result returned true, then ranker worked
			if (result == true)
				rankerResponse = "1\n";
			else
				rankerResponse = "-1\n";
			
			// Listen to port 1246
			try
			{
				socket_wr = serverSocket_wr.accept();
			}
			catch (Exception e)
			{
				System.out.println("Accept failed");
				System.exit(-1);
			}
						
			//Write string of a number to port 1246
			try
			{
				PrintStream ps = new PrintStream(socket_wr.getOutputStream());
				ps.print(rankerResponse);					
			}
			catch (Exception e)
			{
				System.out.println("String writing on port to client failed");
				System.exit(-1);
			}
		}
	}
}
