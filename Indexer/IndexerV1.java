
package search;

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

/*
 * The indexer interact with the database such that any link in the database is one of an HTML
 * document that has already been indexed, thus there is no column in such a table indicating
 * whether link has been indexed yet or not. This is due to the fact that the web crawler is
 * implemented file-wise, and not database-wise.
 */

public class IndexerV1
{
	//List of stopwords
	public static String[] stopwords = {"a", "as", "able", "about", "above", "according", "accordingly", "across", "actually", "after", "afterwards", "again", "against", "aint", "all", "allow", "allows", "almost", "alone", "along", "already", "also", "although", "always", "am", "among", "amongst", "an", "and", "another", "any", "anybody", "anyhow", "anyone", "anything", "anyway", "anyways", "anywhere", "apart", "appear", "appreciate", "appropriate", "are", "arent", "around", "as", "aside", "ask", "asking", "associated", "at", "available", "away", "awfully", "be", "became", "because", "become", "becomes", "becoming", "been", "before", "beforehand", "behind", "being", "believe", "below", "beside", "besides", "best", "better", "between", "beyond", "both", "brief", "but", "by", "cmon", "cs", "came", "can", "cant", "cannot", "cant", "cause", "causes", "certain", "certainly", "changes", "clearly", "co", "com", "come", "comes", "concerning", "consequently", "consider", "considering", "contain", "containing", "contains", "corresponding", "could", "couldnt", "course", "currently", "definitely", "described", "despite", "did", "didnt", "different", "do", "does", "doesnt", "doing", "dont", "done", "down", "downwards", "during", "each", "edu", "eg", "eight", "either", "else", "elsewhere", "enough", "entirely", "especially", "et", "etc", "even", "ever", "every", "everybody", "everyone", "everything", "everywhere", "ex", "exactly", "example", "except", "far", "few", "ff", "fifth", "first", "five", "followed", "following", "follows", "for", "former", "formerly", "forth", "four", "from", "further", "furthermore", "get", "gets", "getting", "given", "gives", "go", "goes", "going", "gone", "got", "gotten", "greetings", "had", "hadnt", "happens", "hardly", "has", "hasnt", "have", "havent", "having", "he", "hes", "hello", "help", "hence", "her", "here", "heres", "hereafter", "hereby", "herein", "hereupon", "hers", "herself", "hi", "him", "himself", "his", "hither", "hopefully", "how", "howbeit", "however", "i", "id", "ill", "im", "ive", "ie", "if", "ignored", "immediate", "in", "inasmuch", "inc", "indeed", "indicate", "indicated", "indicates", "inner", "insofar", "instead", "into", "inward", "is", "isnt", "it", "itd", "itll", "its", "its", "itself", "just", "keep", "keeps", "kept", "know", "knows", "known", "last", "lately", "later", "latter", "latterly", "least", "less", "lest", "let", "lets", "like", "liked", "likely", "little", "look", "looking", "looks", "ltd", "mainly", "many", "may", "maybe", "me", "mean", "meanwhile", "merely", "might", "more", "moreover", "most", "mostly", "much", "must", "my", "myself", "name", "namely", "nd", "near", "nearly", "necessary", "need", "needs", "neither", "never", "nevertheless", "new", "next", "nine", "no", "nobody", "non", "none", "noone", "nor", "normally", "not", "nothing", "novel", "now", "nowhere", "obviously", "of", "off", "often", "oh", "ok", "okay", "old", "on", "once", "one", "ones", "only", "onto", "or", "other", "others", "otherwise", "ought", "our", "ours", "ourselves", "out", "outside", "over", "overall", "own", "particular", "particularly", "per", "perhaps", "placed", "please", "plus", "possible", "presumably", "probably", "provides", "que", "quite", "qv", "rather", "rd", "re", "really", "reasonably", "regarding", "regardless", "regards", "relatively", "respectively", "right", "said", "same", "saw", "say", "saying", "says", "second", "secondly", "see", "seeing", "seem", "seemed", "seeming", "seems", "seen", "self", "selves", "sensible", "sent", "serious", "seriously", "seven", "several", "shall", "she", "should", "shouldnt", "since", "six", "so", "some", "somebody", "somehow", "someone", "something", "sometime", "sometimes", "somewhat", "somewhere", "soon", "sorry", "specified", "specify", "specifying", "still", "sub", "such", "sup", "sure", "ts", "take", "taken", "tell", "tends", "th", "than", "thank", "thanks", "thanx", "that", "thats", "thats", "the", "their", "theirs", "them", "themselves", "then", "thence", "there", "theres", "thereafter", "thereby", "therefore", "therein", "theres", "thereupon", "these", "they", "theyd", "theyll", "theyre", "theyve", "think", "third", "this", "thorough", "thoroughly", "those", "though", "three", "through", "throughout", "thru", "thus", "to", "together", "too", "took", "toward", "towards", "tried", "tries", "truly", "try", "trying", "twice", "two", "un", "under", "unfortunately", "unless", "unlikely", "until", "unto", "up", "upon", "us", "use", "used", "useful", "uses", "using", "usually", "value", "various", "very", "via", "viz", "vs", "want", "wants", "was", "wasnt", "way", "we", "wed", "well", "were", "weve", "welcome", "well", "went", "were", "werent", "what", "whats", "whatever", "when", "whence", "whenever", "where", "wheres", "whereafter", "whereas", "whereby", "wherein", "whereupon", "wherever", "whether", "which", "while", "whither", "who", "whos", "whoever", "whole", "whom", "whose", "why", "will", "willing", "wish", "with", "within", "without", "wont", "wonder", "would", "would", "wouldnt", "yes", "yet", "you", "youd", "youll", "youre", "youve", "your", "yours", "yourself", "yourselves", "zero"};
	
	public static void main(String[] args) 
	{	
		String state_txt = "C:\\Users\\emais\\workspace\\search\\Crawled\\state.txt";
		String links_txt = "C:\\Users\\emais\\workspace\\search\\Crawled\\links.txt";
		String htmls_folder = "C:\\Users\\emais\\workspace\\search\\Crawled\\HTMLs\\";
		
		String url = "jdbc:mysql://localhost:3306/";
		String user = "root";
		String password = "0ASD";
		
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
		
		//Starting to connect and perform operations on database
		try
		{
			//Connecting to the database
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection con = DriverManager.getConnection(url, user, password);
			
			//Select database
			Statement stt = con.createStatement();
			stt.execute("USE search");
			ResultSet res;
			
			//Reading last recorded docID in database
			int currID = -1;
			res = stt.executeQuery("SELECT * FROM doc_links");
			if (res.next())
			{
				res = stt.executeQuery("SELECT MAX(docID) AS docID FROM doc_links");
				res.next();
				currID = res.getInt("docID");	
			}
			
			//In case we are going to connect to a URL          
            /*try 
            {
                doc = Jsoup.connect("https://www.google.com").get();

                System.out.println(doc.title());
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }*/		

			if (stateNum - currID == 1)
			{
				System.out.println("No pages to index!");
				return;
			}
			
			while (stateNum - currID != 1)
			{
				//Next ID
				currID++;
				
				//Selecting link to record in database, thus indicating indexed
	            BufferedReader linksReader = new BufferedReader(new FileReader(links_txt));
				String link = null;
				
				for (int i = -1; i < currID; i++)		//(***)
				{
					link = linksReader.readLine();
				}
				
				linksReader.close();
				
				stt.execute("INSERT INTO doc_links (docID, docLink)"
						+ " VALUES (" + currID + ", '" + link + "')");
				
				//Parsing the corresponding HTML file
				File file = new File(htmls_folder + currID + ".html");
	            Document doc = Jsoup.parse(file, "UTF-8");

	            //Acquiring all that's written in each tag (overall)
	            Elements title = doc.select("title");	//selecting all <title> tags
	            Elements h1 = doc.select("h1");			//selecting all <h1> tags
	            Elements h2 = doc.select("h2");
	            Elements h3 = doc.select("h3");
	            Elements h4 = doc.select("h4");
	            Elements h5 = doc.select("h5");
	            Elements h6 = doc.select("h6");
	            
	            /*NOTE: 
	             * <bold> and such tags will be counted twice if nested within another
	             * tag, which is valid because such text would indeed be counted as bold
	             * for example and at the same time, counted as part of the other tag
	             */
	            Elements bold = doc.select("bold, strong");	//selecting all <bold> tags
	            Elements italic = doc.select("italic, em");	//selecting all <italic> tags
	            
	            Elements other = doc.select("body");	//selecting all tags   
	            other.select("title").remove();	//removing <title> tags          
	            other.select("h1").remove();
	            other.select("h2").remove();
	            other.select("h3").remove();
	            other.select("h4").remove();
	            other.select("h5").remove();
	            other.select("h6").remove();
	            
	            //Extracting the string from Elements for each tag
	            String titleStr = title.text();
	            String h1Str = h1.text();
	            String h2Str = h2.text();
	            String h3Str = h3.text();
	            String h4Str = h4.text();
	            String h5Str = h5.text();
	            String h6Str = h6.text();
	            
	            String boldStr = bold.text();
	            String italicStr = italic.text();
	            
	            String otherStr = other.text();
	            
	            //Lower casing each tag's string
	            titleStr = titleStr.toLowerCase();
	            h1Str = h1Str.toLowerCase();
	            h2Str = h2Str.toLowerCase();
	            h3Str = h3Str.toLowerCase();
	            h4Str = h4Str.toLowerCase();
	            h5Str = h5Str.toLowerCase();
	            h6Str = h6Str.toLowerCase();
	            
	            boldStr = boldStr.toLowerCase();
	            italicStr = italicStr.toLowerCase();
	            
	            otherStr = otherStr.toLowerCase();

	            //Cleaning the string
	            /* (replacing everything that is not a 
	             * word character (a-z, 0-9, _). All that 
	             * would be left is spaces between each word)
	             */
	            titleStr = titleStr.replaceAll("[^\\w\\s]",""); 
	            h1Str = h1Str.replaceAll("[^\\w\\s]","");
	            h2Str = h2Str.replaceAll("[^\\w\\s]","");
	            h3Str = h3Str.replaceAll("[^\\w\\s]","");
	            h4Str = h4Str.replaceAll("[^\\w\\s]","");
	            h5Str = h5Str.replaceAll("[^\\w\\s]","");
	            h6Str = h6Str.replaceAll("[^\\w\\s]","");
	            
	            boldStr = boldStr.replaceAll("[^\\w\\s]","");
	            italicStr = italicStr.replaceAll("[^\\w\\s]","");
	            
	            otherStr = otherStr.replaceAll("[^\\w\\s]","");
	            	            
	            //Token the string
	            String[] titleArr = titleStr.split(" +");	//The '+' is there for one or more spaces
	            String[] h1Arr = h1Str.split(" +");
	            String[] h2Arr = h2Str.split(" +");
	            String[] h3Arr = h3Str.split(" +");
	            String[] h4Arr = h4Str.split(" +");
	            String[] h5Arr = h5Str.split(" +");
	            String[] h6Arr = h6Str.split(" +");
	            
	            String[] boldArr = boldStr.split(" +");
	            String[] italicArr = italicStr.split(" +");
	            
	            String[] otherArr = otherStr.split(" +");

	            //Adding to HashSet to remove duplicates
	            HashSet <String> titleHash = new HashSet <String>();
	            HashSet <String> h1Hash = new HashSet <String>();
	            HashSet <String> h2Hash = new HashSet <String>();
	            HashSet <String> h3Hash = new HashSet <String>();
	            HashSet <String> h4Hash = new HashSet <String>();
	            HashSet <String> h5Hash = new HashSet <String>();
	            HashSet <String> h6Hash = new HashSet <String>();
	            
	            HashSet <String> boldHash = new HashSet <String>();
	            HashSet <String> italicHash = new HashSet <String>();
	            
	            HashSet <String> otherHash = new HashSet <String>();
	            
	            //Eliminating stop words
	            //			+
	            //Removing duplicates by storing in HashSet
	            for (int i = 0; i < titleArr.length; ++i)
	            {
	            	boolean checkStop = true;
	            	
	            	for (int j=0; j < stopwords.length; j++) 
	            	{
	            		if (titleArr[i].equals(stopwords[j]))
	            		{
	            			checkStop = false;
	            			break;
	            		}
	            	}
	            	
	            	if (checkStop)
	            		titleHash.add(titleArr[i]);	//adding the word (term)
	            }
	            
	            for (int i = 0; i < h1Arr.length; ++i)
	            {
	            	boolean checkStop = true;
	            	
	            	for (int j=0; j < stopwords.length; j++) 
	            	{
	            		if (h1Arr[i].equals(stopwords[j]))
	            		{
	            			checkStop = false;
	            			break;
	            		}
	            	}
	            	
	            	if (checkStop)
	            		h1Hash.add(h1Arr[i]);
	            }
	            	
	            
	            for (int i = 0; i < h2Arr.length; ++i)
	            {
	            	boolean checkStop = true;
	            	
	            	for (int j=0; j < stopwords.length; j++) 
	            	{
	            		if (h2Arr[i].equals(stopwords[j]))
	            		{
	            			checkStop = false;
	            			break;
	            		}
	            	}
	            	
	            	if (checkStop)
	            		h2Hash.add(h2Arr[i]);
	            }
	            	
	            
	            for (int i = 0; i < h3Arr.length; ++i)
	            {
	            	boolean checkStop = true;
	            	
	            	for (int j=0; j<stopwords.length; j++) 
	            	{
	            		if (h3Arr[i].equals(stopwords[j]))
	            		{
	            			checkStop = false;
	            			break;
	            		}
	            	}
	            	
	            	if (checkStop)
	            		h3Hash.add(h3Arr[i]);
	            }
	            	
	            
	            for (int i = 0; i < h4Arr.length; ++i)
	            {
	            	boolean checkStop = true;
	            	
	            	for (int j=0; j < stopwords.length; j++) 
	            	{
	            		if (h4Arr[i].equals(stopwords[j]))
	            		{
	            			checkStop = false;
	            			break;
	            		}
	            	}
	            	
	            	if (checkStop)
	            		h4Hash.add(h4Arr[i]);
	            }
	            
	            for (int i = 0; i < h5Arr.length; ++i)
	            {
	            	boolean checkStop = true;
	            	
	            	for (int j=0; j < stopwords.length; j++) 
	            	{
	            		if (h5Arr[i].equals(stopwords[j]))
	            		{
	            			checkStop = false;
	            			break;
	            		}
	            	}
	            	if (checkStop)
	            		h5Hash.add(h5Arr[i]);
	            }
	            
	            for (int i = 0; i < h6Arr.length; ++i)
	            {
	            	boolean checkStop = true;
	            	
	            	for (int j=0; j < stopwords.length; j++) 
	            	{
	            		if (h6Arr[i].equals(stopwords[j]))
	            		{
	            			checkStop = false;
	            			break;
	            		}
	            	}
	            	
	            	if (checkStop)
	            		h6Hash.add(h6Arr[i]);
	            }
	            
	            for (int i = 0; i < boldArr.length; ++i)
	            {
	            	boolean checkStop = true;
	            	
	            	for (int j=0; j < stopwords.length; j++) 
	            	{
	            		if (boldArr[i].equals(stopwords[j]))
	            		{
	            			checkStop = false;
	            			break;
	            		}
	            	}
	            	
	            	if (checkStop)
	            		boldHash.add(boldArr[i]);
	            }
	            
	            for (int i = 0; i < italicArr.length; ++i)
	            {
	            	boolean checkStop = true;
	            	
	            	for (int j=0; j < stopwords.length; j++) 
	            	{
	            		if (italicArr[i].equals(stopwords[j]))
	            		{
	            			checkStop = false;
	            			break;
	            		}
	            	}
	            	
	            	if (checkStop)
	            		italicHash.add(italicArr[i]);
	            }
	            
	            for (int i = 0; i < otherArr.length; ++i)
	            {
	            	boolean checkStop = true;
	            	
	            	for (int j=0; j < stopwords.length; j++) 
	            	{
	            		if (otherArr[i].equals(stopwords[j]))
	            		{
	            			checkStop = false;
	            			break;
	            		}
	            	}
	            	
	            	if (checkStop)
	            		otherHash.add(otherArr[i]);
	            }
	            
	            //Count, Stem & Index	            
	            PorterStemmer portStem = new PorterStemmer();
	            Iterator iterator = null;

	            String term = "";
	            String termStem = "";
	            int pos = -1;
	            int count = 0;
	            String indexStr = "";
	            String[] indexArr;

	            //...of Tag <title>
	            iterator = titleHash.iterator();
	            	            
	            while (iterator.hasNext())
	            {          
	            	term = iterator.next().toString();
	            	
	            	//If term was not in tag
	            	if (term.isEmpty())
	            		break;
	            	
	            	//Counting
	            	count = StringUtils.countMatches(titleStr, term);
	            	
	            	//Stemming
	                portStem.setCurrent(term);
	                portStem.stem();
	                termStem = portStem.getCurrent();
	                
	                //Indexing term
	                res = stt.executeQuery("SELECT term"
	                		+ " FROM tag_index"
	                		+ " WHERE"
	                		+ " term = '" + termStem + "' AND"
	                		+ " docID = " + currID);
	                
	                if (res.next() == false)	//if term not there, then add it
	                	stt.execute("INSERT INTO tag_index (term, docID)"
	                			+ " VALUES ('" + termStem + "', " + currID + ")");
	                
	                //Term there, and so update count for respective tag
	                stt.execute("UPDATE tag_index"
	            			+ " SET titleTag = " + count
	            			+ " WHERE term = '" + termStem + "'");
	                
	                //Indexing position
	                
	                /* Since our system works such that any docID in database has been indexed
	                 * then it's safe not to check whether the term's positional values for
	                 * the current document AND current tag exists or not, and instead, safe 
	                 * to always assume it's not there for the respective document and tag 
	                 * since this is the only time we are indexing the document for the resp. tag
	                 */
	                
	                pos = Arrays.asList(titleArr).indexOf(term);
	                while (pos != -1)
	                {
	                	stt.execute("INSERT INTO pos_index (term, docID, tagType, tagNum, posNum)"
		                		+ " VALUES ('" + termStem + "', " + currID + ", 'title', -1," + pos + ")");
	                	
	                	titleArr[pos] = "NULL";	//removing term from that index
	                	pos = Arrays.asList(titleArr).indexOf(term);	//if not found, then -1
	                }   
	            }
	            
	            //...of Tag <h1>
	            iterator = h1Hash.iterator();
	            
	            while (iterator.hasNext())
	            {         
	            	term = iterator.next().toString();
	            	
	            	if (term.isEmpty())
	            		break;
	            	
	            	//Counting
	            	count = StringUtils.countMatches(h1Str, term);
	            	
	            	//Stemming
	                portStem.setCurrent(term);
	                portStem.stem();
	                termStem = portStem.getCurrent();
	                
	                //Indexing term
	                res = stt.executeQuery("SELECT term"
	                		+ " FROM tag_index"
	                		+ " WHERE"
	                		+ " term = '" + termStem + "' AND"
	                		+ " docID = " + currID);
	                
	                if (res.next() == false)
	                	stt.execute("INSERT INTO tag_index (term, docID)"
	                			+ " VALUES ('" + termStem + "', " + currID + ")");	
	                
	                stt.execute("UPDATE tag_index"
	            			+ " SET h1Tag = " + count
	            			+ " WHERE term = '" + termStem + "'");   
	                
	                //Indexing position
	                pos = Arrays.asList(h1Arr).indexOf(term);
	                while (pos != -1)
	                {
	                	stt.execute("INSERT INTO pos_index (term, docID, tagType, tagNum, posNum)"
		                		+ " VALUES ('" + termStem + "', " + currID + ", 'h1', -1," + pos + ")");
	                	
	                	h1Arr[pos] = "NULL";
	                	pos = Arrays.asList(h1Arr).indexOf(term);
	                }   
	            }
	            
	            //...of Tag <h2>
	            iterator = h2Hash.iterator();
	            
	            while (iterator.hasNext())
	            {                 	
	            	term = iterator.next().toString();
	            	
	            	if (term.isEmpty())
	            		break;
	            	
	            	//Counting
	            	count = StringUtils.countMatches(h2Str, term);
	            	
	            	//Stemming
	                portStem.setCurrent(term);
	                portStem.stem();
	                termStem = portStem.getCurrent();
	                
	                //Indexing term
	                res = stt.executeQuery("SELECT term"
	                		+ " FROM tag_index"
	                		+ " WHERE"
	                		+ " term = '" + termStem + "' AND"
	                		+ " docID = " + currID);
	                
	                if (res.next() == false)
	                	stt.execute("INSERT INTO tag_index (term, docID)"
	                			+ " VALUES ('" + termStem + "', " + currID + ")");	
	                
	                stt.execute("UPDATE tag_index"
	            			+ " SET h2Tag = " + count
	            			+ " WHERE term = '" + termStem + "'");  
	                
	                //Indexing position
	                pos = Arrays.asList(h2Arr).indexOf(term);
	                while (pos != -1)
	                {
	                	stt.execute("INSERT INTO pos_index (term, docID, tagType, tagNum, posNum)"
		                		+ " VALUES ('" + termStem + "', " + currID + ", 'h2', -1," + pos + ")");
	                	
	                	h2Arr[pos] = "NULL";
	                	pos = Arrays.asList(h2Arr).indexOf(term);
	                }   
	            }
	            
	            //...of Tag <h3>
	            iterator = h3Hash.iterator();
	            
	            while (iterator.hasNext())
	            {             	
	            	term = iterator.next().toString();
	            	
	            	if (term.isEmpty())
	            		break;
	            	
	            	//Counting
	            	count = StringUtils.countMatches(h3Str, term);
	            	
	            	//Stemming
	                portStem.setCurrent(term);
	                portStem.stem();
	                termStem = portStem.getCurrent();
	                
	                //Indexing term
	                res = stt.executeQuery("SELECT term"
	                		+ " FROM tag_index"
	                		+ " WHERE"
	                		+ " term = '" + termStem + "' AND"
	                		+ " docID = " + currID);
	                
	                if (res.next() == false)
	                	stt.execute("INSERT INTO tag_index (term, docID)"
	                			+ " VALUES ('" + termStem + "', " + currID + ")");	
	                
	                stt.execute("UPDATE tag_index"
	            			+ " SET h3Tag = " + count
	            			+ " WHERE term = '" + termStem + "'"); 
	                
	                //Indexing position
	                pos = Arrays.asList(h3Arr).indexOf(term);
	                while (pos != -1)
	                {
	                	stt.execute("INSERT INTO pos_index (term, docID, tagType, tagNum, posNum)"
		                		+ " VALUES ('" + termStem + "', " + currID + ", 'h3', -1," + pos + ")");
	                	
	                	h3Arr[pos] = "NULL";
	                	pos = Arrays.asList(h3Arr).indexOf(term);
	                }  
	            }
	            
	            //...of Tag <h4>
	            iterator = h4Hash.iterator();
	            
	            while (iterator.hasNext())
	            {       
	            	term = iterator.next().toString();
	            	
	            	if (term.isEmpty())
	            		break;
	            	
	            	//Counting
	            	count = StringUtils.countMatches(h4Str, term);
	            	
	            	//Stemming
	                portStem.setCurrent(term);
	                portStem.stem();
	                termStem = portStem.getCurrent();
	                
	                //Indexing term
	                res = stt.executeQuery("SELECT term"
	                		+ " FROM tag_index"
	                		+ " WHERE"
	                		+ " term = '" + termStem + "' AND"
	                		+ " docID = " + currID);
	                
	                if (res.next() == false)
	                	stt.execute("INSERT INTO tag_index (term, docID)"
	                			+ " VALUES ('" + termStem + "', " + currID + ")");
	                
	                stt.execute("UPDATE tag_index"
	            			+ " SET h4Tag = " + count
	            			+ " WHERE term = '" + termStem + "'");   
	                
	                //Indexing position
	                pos = Arrays.asList(h4Arr).indexOf(term);
	                while (pos != -1)
	                {
	                	stt.execute("INSERT INTO pos_index (term, docID, tagType, tagNum, posNum)"
		                		+ " VALUES ('" + termStem + "', " + currID + ", 'h4', -1," + pos + ")");
	                	
	                	h4Arr[pos] = "NULL";
	                	pos = Arrays.asList(h4Arr).indexOf(term);
	                }  
	            }
	            
	            //...of Title <h5>
	            iterator = h5Hash.iterator();
	            
	            while (iterator.hasNext())
	            {       
	            	term = iterator.next().toString();
	            	
	            	if (term.isEmpty())
	            		break;
	            	
	            	//Counting
	            	count = StringUtils.countMatches(h5Str, term);
	            	
	            	//Stemming
	                portStem.setCurrent(term);
	                portStem.stem();
	                termStem = portStem.getCurrent();
	                
	                //Indexing term
	                res = stt.executeQuery("SELECT term"
	                		+ " FROM tag_index"
	                		+ " WHERE"
	                		+ " term = '" + termStem + "' AND"
	                		+ " docID = " + currID);
	                
	                if (res.next() == false)
	                	stt.execute("INSERT INTO tag_index (term, docID)"
	                			+ " VALUES ('" + termStem + "', " + currID + ")");	
	                
	                stt.execute("UPDATE tag_index"
	            			+ " SET h5Tag = " + count
	            			+ " WHERE term = '" + termStem + "'");   
	                
	                //Indexing position
	                pos = Arrays.asList(h5Arr).indexOf(term);
	                while (pos != -1)
	                {
	                	stt.execute("INSERT INTO pos_index (term, docID, tagType, tagNum, posNum)"
		                		+ " VALUES ('" + termStem + "', " + currID + ", 'h5', -1," + pos + ")");
	                	
	                	h5Arr[pos] = "NULL";
	                	pos = Arrays.asList(h5Arr).indexOf(term);
	                } 
	            }
	            
	            //...of Tag <h6>
	            iterator = h6Hash.iterator();
	            
	            while (iterator.hasNext())
	            {       
	            	term = iterator.next().toString();
	            	
	            	if (term.isEmpty())
	            		break;
	            	
	            	//Counting
	            	count = StringUtils.countMatches(h6Str, term);
	            	
	            	//Stemming
	                portStem.setCurrent(term);
	                portStem.stem();
	                termStem = portStem.getCurrent();
	                
	                //Indexing term
	                res = stt.executeQuery("SELECT term"
	                		+ " FROM tag_index"
	                		+ " WHERE"
	                		+ " term = '" + termStem + "' AND"
	                		+ " docID = " + currID);
	                
	                if (res.next() == false)
	                	stt.execute("INSERT INTO tag_index (term, docID)"
	                			+ " VALUES ('" + termStem + "', " + currID + ")");	
	                
	                stt.execute("UPDATE tag_index"
	            			+ " SET h6Tag = " + count
	            			+ " WHERE term = '" + termStem + "'");
	                
	                //Indexing position
	                pos = h6Str.indexOf(term);
	                while (pos >= 0)
	                {
	                	stt.execute("INSERT INTO pos_index (term, docID, tagType, tagNum, posNum)"
		                		+ " VALUES ('" + termStem + "', " + currID + ", 'h6', -1," + pos + ")");
	                	
	                	pos = h6Str.indexOf(term, pos + 1);	//if end reached, pos becomes -1
	                }  
	            }
	            
	          //...of Tag <bold>
	            iterator = boldHash.iterator();
	            
	            while (iterator.hasNext())
	            {       
	            	term = iterator.next().toString();
	            	
	            	if (term.isEmpty())
	            		break;
	            	
	            	//Counting
	            	count = StringUtils.countMatches(boldStr, term);
	            	
	            	//Stemming
	                portStem.setCurrent(term);
	                portStem.stem();
	                termStem = portStem.getCurrent();
	                
	                //Indexing term
	                res = stt.executeQuery("SELECT term"
	                		+ " FROM tag_index"
	                		+ " WHERE"
	                		+ " term = '" + term + "' AND"
	                		+ " docID = " + currID);
	                
	                if (res.next() == false)
	                	stt.execute("INSERT INTO tag_index (term, docID)"
	                			+ " VALUES ('" + termStem + "', " + currID + ")");
	                
	                stt.execute("UPDATE tag_index"
	            			+ " SET boldTag = " + count
	            			+ " WHERE term = '" + termStem + "'");
	                
	                //Indexing position
	                pos = boldStr.indexOf(term);
	                while (pos >= 0)
	                {
	                	stt.execute("INSERT INTO pos_index (term, docID, tagType, tagNum, posNum)"
		                		+ " VALUES ('" + termStem + "', " + currID + ", 'bold', -1," + pos + ")");
	                	
	                	pos = boldStr.indexOf(term, pos + 1);	//if end reached, pos becomes -1
	                }  
	            }
	            
	          //...of Tag <italic>
	            iterator = italicHash.iterator();
	            
	            while (iterator.hasNext())
	            {       
	            	term = iterator.next().toString();
	            	
	            	if (term.isEmpty())
	            		break;
	            	
	            	//Counting
	            	count = StringUtils.countMatches(italicStr, term);
	            	
	            	//Stemming
	                portStem.setCurrent(term);
	                portStem.stem();
	                termStem = portStem.getCurrent();
	                
	                //Indexing term
	                res = stt.executeQuery("SELECT term"
	                		+ " FROM tag_index"
	                		+ " WHERE"
	                		+ " term = '" + termStem + "' AND"
	                		+ " docID = " + currID);
	                
	                if (res.next() == false)
	                	stt.execute("INSERT INTO tag_index (term, docID)"
	                			+ " VALUES ('" + termStem + "', " + currID + ")");	
	                
	                stt.execute("UPDATE tag_index"
	            			+ " SET italicTag = " + count
	            			+ " WHERE term = '" + termStem + "'");
	                
	                //Indexing position
	                pos = Arrays.asList(italicArr).indexOf(term);
	                while (pos != -1)
	                {
	                	stt.execute("INSERT INTO pos_index (term, docID, tagType, tagNum, posNum)"
		                		+ " VALUES ('" + termStem + "', " + currID + ", 'italic', -1," + pos + ")");
	                	
	                	italicArr[pos] = "NULL";
	                	pos = Arrays.asList(italicArr).indexOf(term);
	                }
	            }
	            
	          //...of Tag <other>
	            iterator = otherHash.iterator();
	            
	            while (iterator.hasNext())
	            {       
	            	term = iterator.next().toString();
	            	
	            	if (term.isEmpty())
	            		break;
	            	
	            	//Counting
	            	count = StringUtils.countMatches(otherStr, term);
	            	
	            	//Stemming
	                portStem.setCurrent(term);
	                portStem.stem();
	                termStem = portStem.getCurrent();
	                
	                //Indexing term
	                res = stt.executeQuery("SELECT term"
	                		+ " FROM tag_index"
	                		+ " WHERE"
	                		+ " term = '" + termStem + "' AND"
	                		+ " docID = " + currID);
	                
	                if (res.next() == false)
	                	stt.execute("INSERT INTO tag_index (term, docID)"
	                			+ " VALUES ('" + termStem + "', " + currID + ")");
	                
	                stt.execute("UPDATE tag_index"
	            			+ " SET otherTag = " + count
	            			+ " WHERE term = '" + termStem + "'");
	                
	                //Indexing position
	                pos = Arrays.asList(otherArr).indexOf(term);
	                while (pos != -1)
	                {
	                	stt.execute("INSERT INTO pos_index (term, docID, tagType, tagNum, posNum)"
		                		+ " VALUES ('" + termStem + "', " + currID + ", 'other', -1," + pos + ")");
	                	
	                	otherArr[pos] = "NULL";
	                	pos = Arrays.asList(otherArr).indexOf(term);
	                }  
	            }
			}
			
			System.out.println("Indexing complete!");
		}
		catch (Exception error)
		{
			System.out.println(error.getMessage());
		}
	}
}