//
//package search;
//
//import java.io.*;
//import java.net.*;
//import java.util.*;
//
//import java.sql.*;
//
//import org.jsoup.Jsoup;
//import org.jsoup.nodes.*;
//import org.jsoup.select.*;
//
//import org.apache.commons.io.*;
//import org.apache.commons.lang3.*;
//
//import org.tartarus.snowball.ext.*;
//
//
//public class IndexerV2
//{
//	//List of stopwords
//	public static String[] stopwords = {"a", "as", "able", "about", "above", "according", "accordingly", "across", "actually", "after", "afterwards", "again", "against", "aint", "all", "allow", "allows", "almost", "alone", "along", "already", "also", "although", "always", "am", "among", "amongst", "an", "and", "another", "any", "anybody", "anyhow", "anyone", "anything", "anyway", "anyways", "anywhere", "apart", "appear", "appreciate", "appropriate", "are", "arent", "around", "as", "aside", "ask", "asking", "associated", "at", "available", "away", "awfully", "be", "became", "because", "become", "becomes", "becoming", "been", "before", "beforehand", "behind", "being", "believe", "below", "beside", "besides", "best", "better", "between", "beyond", "both", "brief", "but", "by", "cmon", "cs", "came", "can", "cant", "cannot", "cant", "cause", "causes", "certain", "certainly", "changes", "clearly", "co", "com", "come", "comes", "concerning", "consequently", "consider", "considering", "contain", "containing", "contains", "corresponding", "could", "couldnt", "course", "currently", "definitely", "described", "despite", "did", "didnt", "different", "do", "does", "doesnt", "doing", "dont", "done", "down", "downwards", "during", "each", "edu", "eg", "eight", "either", "else", "elsewhere", "enough", "entirely", "especially", "et", "etc", "even", "ever", "every", "everybody", "everyone", "everything", "everywhere", "ex", "exactly", "example", "except", "far", "few", "ff", "fifth", "first", "five", "followed", "following", "follows", "for", "former", "formerly", "forth", "four", "from", "further", "furthermore", "get", "gets", "getting", "given", "gives", "go", "goes", "going", "gone", "got", "gotten", "greetings", "had", "hadnt", "happens", "hardly", "has", "hasnt", "have", "havent", "having", "he", "hes", "hello", "help", "hence", "her", "here", "heres", "hereafter", "hereby", "herein", "hereupon", "hers", "herself", "hi", "him", "himself", "his", "hither", "hopefully", "how", "howbeit", "however", "i", "id", "ill", "im", "ive", "ie", "if", "ignored", "immediate", "in", "inasmuch", "inc", "indeed", "indicate", "indicated", "indicates", "inner", "insofar", "instead", "into", "inward", "is", "isnt", "it", "itd", "itll", "its", "its", "itself", "just", "keep", "keeps", "kept", "know", "knows", "known", "last", "lately", "later", "latter", "latterly", "least", "less", "lest", "let", "lets", "like", "liked", "likely", "little", "look", "looking", "looks", "ltd", "mainly", "many", "may", "maybe", "me", "mean", "meanwhile", "merely", "might", "more", "moreover", "most", "mostly", "much", "must", "my", "myself", "name", "namely", "nd", "near", "nearly", "necessary", "need", "needs", "neither", "never", "nevertheless", "new", "next", "nine", "no", "nobody", "non", "none", "noone", "nor", "normally", "not", "nothing", "novel", "now", "nowhere", "obviously", "of", "off", "often", "oh", "ok", "okay", "old", "on", "once", "one", "ones", "only", "onto", "or", "other", "others", "otherwise", "ought", "our", "ours", "ourselves", "out", "outside", "over", "overall", "own", "particular", "particularly", "per", "perhaps", "placed", "please", "plus", "possible", "presumably", "probably", "provides", "que", "quite", "qv", "rather", "rd", "re", "really", "reasonably", "regarding", "regardless", "regards", "relatively", "respectively", "right", "said", "same", "saw", "say", "saying", "says", "second", "secondly", "see", "seeing", "seem", "seemed", "seeming", "seems", "seen", "self", "selves", "sensible", "sent", "serious", "seriously", "seven", "several", "shall", "she", "should", "shouldnt", "since", "six", "so", "some", "somebody", "somehow", "someone", "something", "sometime", "sometimes", "somewhat", "somewhere", "soon", "sorry", "specified", "specify", "specifying", "still", "sub", "such", "sup", "sure", "ts", "take", "taken", "tell", "tends", "th", "than", "thank", "thanks", "thanx", "that", "thats", "thats", "the", "their", "theirs", "them", "themselves", "then", "thence", "there", "theres", "thereafter", "thereby", "therefore", "therein", "theres", "thereupon", "these", "they", "theyd", "theyll", "theyre", "theyve", "think", "third", "this", "thorough", "thoroughly", "those", "though", "three", "through", "throughout", "thru", "thus", "to", "together", "too", "took", "toward", "towards", "tried", "tries", "truly", "try", "trying", "twice", "two", "un", "under", "unfortunately", "unless", "unlikely", "until", "unto", "up", "upon", "us", "use", "used", "useful", "uses", "using", "usually", "value", "various", "very", "via", "viz", "vs", "want", "wants", "was", "wasnt", "way", "we", "wed", "well", "were", "weve", "welcome", "well", "went", "were", "werent", "what", "whats", "whatever", "when", "whence", "whenever", "where", "wheres", "whereafter", "whereas", "whereby", "wherein", "whereupon", "wherever", "whether", "which", "while", "whither", "who", "whos", "whoever", "whole", "whom", "whose", "why", "will", "willing", "wish", "with", "within", "without", "wont", "wonder", "would", "would", "wouldnt", "yes", "yet", "you", "youd", "youll", "youre", "youve", "your", "yours", "yourself", "yourselves", "zero"};
//	
//	public static void main(String[] args) 
//	{	
//		//Extracting HTML docs from file to database
//		//String line;
//		
//		//try(BufferedReader file = new BufferedReader(new FileReader("C:\\Users\\emais\\workspace\\search\\Crawled\\HTML")))
////		String line;
////		
////		try(BufferedReader file = new BufferedReader(new FileReader("C:\\Users\\emais\\workspace\\search\\Crawled\\HTMLs\\" + state + ".html")))
////		{
////			while((line = file.readLine()) != null)
////			{
////				urlList.add(line);
////			}
////
////		} catch(IOException e)
////		{
////			e.printStackTrace();
////		}
//		
//		//Preparing variables for connecting to database
//		String url = "jdbc:mysql://localhost:3306/";
//		String user = "root";
//		String password = "0ASD";
//		
//		try
//		{
//			//Connecting to the database
//			Class.forName("com.mysql.jdbc.Driver").newInstance();
//			Connection con = DriverManager.getConnection(url, user, password);
//			
//			//Select database
//			Statement stt = con.createStatement();
//			stt.execute("USE search");
//			
//            //In case we are going to connect to a URL
//            
//            /*try 
//            {
//                doc = Jsoup.connect("https://www.google.com").get();
//
//                System.out.println(doc.title());
//            }
//            catch (IOException e)
//            {
//                e.printStackTrace();
//            }*/		
//
//            //Parsing the HTML doc
//            Document doc = Jsoup.parse("<!DOCTYPE html>"
//            		+ "<html>"
//                    + "<title>The Zoo Zoo Zoo Zoo Zoo</title>"
//                    + "<h1>Animal Kingdom: Cheetah in Zoo</h1>"
//                    + "<p>Many <a href = 'wikipedia.org/cheetah'>cheetahs</a> are taken care of here</p>"
//                    + "<p>The zoo also harbors various other animals, and not only cheetah</p>"
//                    + "<p>Testing 1 2 3</p>"
//                    + "</html>");
//
//            //Acquiring all that's written in each tag (separated)
//            Elements titleElems = doc.select("title");
//            Elements pElems = doc.select("p");
//            Elements aElems = doc.select("a");
//            Elements h1Elems = doc.select("h1");
//            Elements h2Elems = doc.select("h2");
//            Elements h3Elems = doc.select("h3");
//            Elements h4Elems = doc.select("h4");
//            Elements h5Elems = doc.select("h5");
//            Elements h6Elems = doc.select("h6");                   
//            
//            //Separating the tags in list, and extracting String
//            List<String> titleStr = new ArrayList<>();
//            Element titleFirstElem = pElems.first();
//            Elements titleNextElems = pElems.next();
//            
//            titleStr.add(titleFirstElem.text());
//            while (titleNextElems.hasText())
//            {
//            	titleStr.add(titleNextElems.text());
//            	titleNextElems = titleElems.next();
//            }
//            
//            List<String> pStr = new ArrayList<>();
//            Element pFirstElem = pElems.first();
//            Elements pNextElems = pElems.next();
//            
//            pStr.add(pFirstElem.text());
//            while (pNextElems.hasText())
//            {
//            	pStr.add(pNextElems.text());
//            	pNextElems = pElems.next();
//            }
//            
//            List<String> aStr = new ArrayList<>();
//            Element aFirstElem = aElems.first();
//            Elements aNextElems = aElems.next();
//            
//            aStr.add(aFirstElem.text());
//            while (aNextElems.hasText())
//            {
//            	aStr.add(aNextElems.text());
//            	aNextElems = aElems.next();
//            }
//
//            List<String> h1Str = new ArrayList<>();
//            Element h1FirstElem = h1Elems.first();
//            Elements h1NextElems = h1Elems.next();
//            
//            h1Str.add(h1FirstElem.text());
//            while (h1NextElems.hasText())
//            {
//            	h1Str.add(h1NextElems.text());
//            	h1NextElems = h1Elems.next();
//            }
//            
//            List<String> h2Str = new ArrayList<>();
//            Element h2FirstElem = h2Elems.first();
//            Elements h2NextElems = h2Elems.next();
//            
//            h2Str.add(h2FirstElem.text());
//            while (h2NextElems.hasText())
//            {
//            	h2Str.add(h2NextElems.text());
//            	h2NextElems = h2Elems.next();
//            }
//            
//            List<String> h3Str = new ArrayList<>();
//            Element h3FirstElem = h3Elems.first();
//            Elements h3NextElems = h3Elems.next();
//            
//            h3Str.add(h3FirstElem.text());
//            while (h3NextElems.hasText())
//            {
//            	h3Str.add(h3NextElems.text());
//            	h3NextElems = h3Elems.next();
//            }
//            
//            List<String> h4Str = new ArrayList<>();
//            Element h4FirstElem = h4Elems.first();
//            Elements h4NextElems = h4Elems.next();
//            
//            h4Str.add(h4FirstElem.text());
//            while (h4NextElems.hasText())
//            {
//            	h4Str.add(h4NextElems.text());
//            	h4NextElems = h4Elems.next();
//            }
//            
//            List<String> h5Str = new ArrayList<>();
//            Element h5FirstElem = h5Elems.first();
//            Elements h5NextElems = h5Elems.next();
//            
//            h5Str.add(h5FirstElem.text());
//            while (h5NextElems.hasText())
//            {
//            	h5Str.add(h5NextElems.text());
//            	h5NextElems = h5Elems.next();
//            }
//            
//            List<String> h6Str = new ArrayList<>();
//            Element h6FirstElem = h6Elems.first();
//            Elements h6NextElems = h6Elems.next();
//            
//            h6Str.add(h6FirstElem.text());
//            while (h6NextElems.hasText())
//            {
//            	h6Str.add(h6NextElems.text());
//            	h6NextElems = h6Elems.next();
//            }
//
//            //Lower casing each tag's string
//            // 				+
//            //Clearing string so that only remains words, space and _
//            for (int i = 0; i < titleStr.size(); ++i)
//            {
//            	titleStr.set(i, titleStr.get(i).toLowerCase());
//            	titleStr.set(i, titleStr.get(i).replaceAll("[^\\w\\s]",""));
//            }
//            
//            for (int i = 0; i < pStr.size(); ++i)
//            {
//            	pStr.set(i, pStr.get(i).toLowerCase());
//            	pStr.set(i, pStr.get(i).replaceAll("[^\\w\\s]",""));
//            }
//            
//            for (int i = 0; i < aStr.size(); ++i)
//            {
//            	aStr.set(i, aStr.get(i).toLowerCase());
//            	aStr.set(i, aStr.get(i).replaceAll("[^\\w\\s]",""));
//            }
//            
//            for (int i = 0; i < h1Str.size(); ++i)
//            {
//            	h1Str.set(i, h1Str.get(i).toLowerCase());
//            	h1Str.set(i, h1Str.get(i).replaceAll("[^\\w\\s]",""));
//            }
//            
//            for (int i = 0; i < h2Str.size(); ++i)
//            {
//            	h2Str.set(i, h2Str.get(i).toLowerCase());
//            	h2Str.set(i, h2Str.get(i).replaceAll("[^\\w\\s]",""));
//            }
//            
//            for (int i = 0; i < h3Str.size(); ++i)
//            {
//            	h3Str.set(i, h3Str.get(i).toLowerCase());
//            	h3Str.set(i, h3Str.get(i).replaceAll("[^\\w\\s]",""));
//            }
//            
//            for (int i = 0; i < h4Str.size(); ++i)
//            {
//            	h4Str.set(i, h4Str.get(i).toLowerCase());
//            	h4Str.set(i, h4Str.get(i).replaceAll("[^\\w\\s]",""));
//            }
//            
//            for (int i = 0; i < h5Str.size(); ++i)
//            {
//            	h5Str.set(i, h5Str.get(i).toLowerCase());
//            	h5Str.set(i, h5Str.get(i).replaceAll("[^\\w\\s]",""));
//            }
//            
//            for (int i = 0; i < h6Str.size(); ++i)
//            {
//            	h6Str.set(i, h6Str.get(i).toLowerCase());
//            	h6Str.set(i, h6Str.get(i).replaceAll("[^\\w\\s]",""));
//            }
//            
//            //Token the strings
//            List<String[]> titleArr = new ArrayList<>();
//            for (int i = 0; i < titleStr.size(); ++i)
//            {
//            	titleArr.set(i, titleStr.get(i).split(" "));
//            }
//            
//            List<String[]> pArr = new ArrayList<>();
//            for (int i = 0; i < pStr.size(); ++i)
//            {
//            	pArr.set(i, pStr.get(i).split(" "));
//            }
//            
//            List<String[]> aArr = new ArrayList<>();
//            for (int i = 0; i < aStr.size(); ++i)
//            {
//            	aArr.set(i, aStr.get(i).split(" "));
//            }
//            
//            List<String[]> h1Arr = new ArrayList<>();
//            for (int i = 0; i < titleStr.size(); ++i)
//            {
//            	h1Arr.set(i, h1Str.get(i).split(" "));
//            }
//            
//            List<String[]> h2Arr = new ArrayList<>();
//            for (int i = 0; i < titleStr.size(); ++i)
//            {
//            	h2Arr.set(i, h2Str.get(i).split(" "));
//            }
//            
//            List<String[]> h3Arr = new ArrayList<>();
//            for (int i = 0; i < titleStr.size(); ++i)
//            {
//            	h3Arr.set(i, h3Str.get(i).split(" "));
//            }
//            
//            List<String[]> h4Arr = new ArrayList<>();
//            for (int i = 0; i < titleStr.size(); ++i)
//            {
//            	h4Arr.set(i, h4Str.get(i).split(" "));
//            }
//            
//            List<String[]> h5Arr = new ArrayList<>();
//            for (int i = 0; i < titleStr.size(); ++i)
//            {
//            	h5Arr.set(i, h5Str.get(i).split(" "));
//            }
//            
//            List<String[]> h6Arr = new ArrayList<>();
//            for (int i = 0; i < titleStr.size(); ++i)
//            {
//            	h6Arr.set(i, h6Str.get(i).split(" "));
//            }
//
//            //Adding to HashSet to remove duplicates
//            //					+
//            //Storing position numbers
//            int posIndex = 0;
//            
//            HashSet <String> titleHash = new HashSet <String>();
//            int titlePos[];
//            
//            HashSet <String> pHash = new HashSet <String>();
//            int pPos[];
//            
//            HashSet <String> aHash = new HashSet <String>();
//            int aPos[];
//            
//            HashSet <String> h1Hash = new HashSet <String>();
//            int h1Pos[];
//            
//            HashSet <String> h2Hash = new HashSet <String>();
//            int h2Pos[];
//            
//            HashSet <String> h3Hash = new HashSet <String>();
//            int h3Pos[];
//            
//            HashSet <String> h4Hash = new HashSet <String>();
//            int h4Pos[];
//            
//            HashSet <String> h5Hash = new HashSet <String>();
//            int h5Pos[];
//            
//            HashSet <String> h6Hash = new HashSet <String>();
//            int h6Pos[];
//            
//            for (int i = 0; i < titleStr.size(); ++i)
//            {
//            	for (int j = 0; j < titleArr.get(i).length; ++i)
//            	{
//            		boolean checkStop = true;
//            		
//            		for (int z = 0; z < stopwords.length; ++z)
//            		{
//            			if (titleArr.get(i)[j].equals(stopwords[z]))
//            			{
//            				checkStop = false;
//                			break;
//            			}
//            		}
//            		
//            		//Each term will have position values for each document
//            		//Thus, down below, when working on each term, you
//            		//work on ea
//            		//index is tag number
//            		//value is position number
//            		if(checkStop)
//            		{
//            			titleHash.add(titleArr.get(i)[j]);
//            			titlePos[posIndex] = j;
//            			posIndex++;
//            		}
//            	}
//            	
//            	titlePos[posIndex] = -1; //separation indicator
//            }
//            
//            
//            //Count
//            //	+
//            //Stem
//            //	+
//            //Index
//			ResultSet res = stt.executeQuery("SELECT * FROM tag_index");
//            Iterator iterator = null;
//            PorterStemmer portStem = new PorterStemmer();
//            
//            String term = "";
//            int count = 0;
//            String indexStr = "";
//            String[] indexArr;
//
//            //Tag <title>
//            iterator = titleHash.iterator();
//            
//            while (iterator.hasNext())
//            {          
//            	term = iterator.next().toString();
//            	
//            	//If nothing in hash i.e. nothing was between tags
//            	if (term.isEmpty())
//            		break;
//            	
//            	//Counting
//            	count = StringUtils.countMatches(titleElems.text(), term); //All title tags together
//            	
//            	//Stemming
//                portStem.setCurrent(term);
//                portStem.stem();
//                term = portStem.getCurrent();
//                
//                //Indexing
//                res = stt.executeQuery("SELECT term"
//                		+ " FROM tag_index"
//                		+ " WHERE"
//                		+ " term = '" + term + "' AND"
//                		+ " docID = " + "1");
//                
//                if (res.next() == false)
//                	stt.execute("INSERT INTO tag_index (term, docID)"
//                			+ " VALUES ('" + term + "', " + "1)");	
//                
//                stt.execute("UPDATE tag_index"
//            			+ " SET titleTag = " + count
//            			+ " WHERE term = '" + term + "'");   
//            }
//		}
//               
//		catch (Exception error)
//		{
//			System.out.println(error.getMessage());
//		}
//	}
//
//}