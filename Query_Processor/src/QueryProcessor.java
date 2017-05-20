
import java.io.*;
import java.net.*;
import java.util.*;

import java.sql.*;

import org.tartarus.snowball.ext.*;

public class QueryProcessor
{	
	//List of stopwords
	private static String[] stopwords = {"a", "as", "able", "about", "above", "according", "accordingly", "across", "actually", "after", "afterwards", "again", "against", "aint", "all", "allow", "allows", "almost", "alone", "along", "already", "also", "although", "always", "am", "among", "amongst", "an", "and", "another", "any", "anybody", "anyhow", "anyone", "anything", "anyway", "anyways", "anywhere", "apart", "appear", "appreciate", "appropriate", "are", "arent", "around", "as", "aside", "ask", "asking", "associated", "at", "available", "away", "awfully", "be", "became", "because", "become", "becomes", "becoming", "been", "before", "beforehand", "behind", "being", "believe", "below", "beside", "besides", "best", "better", "between", "beyond", "both", "brief", "but", "by", "cmon", "cs", "came", "can", "cant", "cannot", "cant", "cause", "causes", "certain", "certainly", "changes", "clearly", "co", "com", "come", "comes", "concerning", "consequently", "consider", "considering", "contain", "containing", "contains", "corresponding", "could", "couldnt", "course", "currently", "definitely", "described", "despite", "did", "didnt", "different", "do", "does", "doesnt", "doing", "dont", "done", "down", "downwards", "during", "each", "edu", "eg", "eight", "either", "else", "elsewhere", "enough", "entirely", "especially", "et", "etc", "even", "ever", "every", "everybody", "everyone", "everything", "everywhere", "ex", "exactly", "example", "except", "far", "few", "ff", "fifth", "first", "five", "followed", "following", "follows", "for", "former", "formerly", "forth", "four", "from", "further", "furthermore", "get", "gets", "getting", "given", "gives", "go", "goes", "going", "gone", "got", "gotten", "greetings", "had", "hadnt", "happens", "hardly", "has", "hasnt", "have", "havent", "having", "he", "hes", "hello", "help", "hence", "her", "here", "heres", "hereafter", "hereby", "herein", "hereupon", "hers", "herself", "hi", "him", "himself", "his", "hither", "hopefully", "how", "howbeit", "however", "i", "id", "ill", "im", "ive", "ie", "if", "ignored", "immediate", "in", "inasmuch", "inc", "indeed", "indicate", "indicated", "indicates", "inner", "insofar", "instead", "into", "inward", "is", "isnt", "it", "itd", "itll", "its", "its", "itself", "just", "keep", "keeps", "kept", "know", "knows", "known", "last", "lately", "later", "latter", "latterly", "least", "less", "lest", "let", "lets", "like", "liked", "likely", "little", "look", "looking", "looks", "ltd", "mainly", "many", "may", "maybe", "me", "mean", "meanwhile", "merely", "might", "more", "moreover", "most", "mostly", "much", "must", "my", "myself", "name", "namely", "nd", "near", "nearly", "necessary", "need", "needs", "neither", "never", "nevertheless", "new", "next", "nine", "no", "nobody", "non", "none", "noone", "nor", "normally", "not", "nothing", "novel", "now", "nowhere", "obviously", "of", "off", "often", "oh", "ok", "okay", "old", "on", "once", "one", "ones", "only", "onto", "or", "other", "others", "otherwise", "ought", "our", "ours", "ourselves", "out", "outside", "over", "overall", "own", "particular", "particularly", "per", "perhaps", "placed", "please", "plus", "possible", "presumably", "probably", "provides", "que", "quite", "qv", "rather", "rd", "re", "really", "reasonably", "regarding", "regardless", "regards", "relatively", "respectively", "right", "said", "same", "saw", "say", "saying", "says", "second", "secondly", "see", "seeing", "seem", "seemed", "seeming", "seems", "seen", "self", "selves", "sensible", "sent", "serious", "seriously", "seven", "several", "shall", "she", "should", "shouldnt", "since", "six", "so", "some", "somebody", "somehow", "someone", "something", "sometime", "sometimes", "somewhat", "somewhere", "soon", "sorry", "specified", "specify", "specifying", "still", "sub", "such", "sup", "sure", "ts", "take", "taken", "tell", "tends", "th", "than", "thank", "thanks", "thanx", "that", "thats", "thats", "the", "their", "theirs", "them", "themselves", "then", "thence", "there", "theres", "thereafter", "thereby", "therefore", "therein", "theres", "thereupon", "these", "they", "theyd", "theyll", "theyre", "theyve", "think", "third", "this", "thorough", "thoroughly", "those", "though", "three", "through", "throughout", "thru", "thus", "to", "together", "too", "took", "toward", "towards", "tried", "tries", "truly", "try", "trying", "twice", "two", "un", "under", "unfortunately", "unless", "unlikely", "until", "unto", "up", "upon", "us", "use", "used", "useful", "uses", "using", "usually", "value", "various", "very", "via", "viz", "vs", "want", "wants", "was", "wasnt", "way", "we", "wed", "well", "were", "weve", "welcome", "well", "went", "were", "werent", "what", "whats", "whatever", "when", "whence", "whenever", "where", "wheres", "whereafter", "whereas", "whereby", "wherein", "whereupon", "wherever", "whether", "which", "while", "whither", "who", "whos", "whoever", "whole", "whom", "whose", "why", "will", "willing", "wish", "with", "within", "without", "wont", "wonder", "would", "would", "wouldnt", "yes", "yet", "you", "youd", "youll", "youre", "youve", "your", "yours", "yourself", "yourselves", "zero"};
	
	@SuppressWarnings("unused")
	//If this is set to true then the user tried to search for a stopword
	private static boolean singleSW = false;
	
	//Function that takes the query input by user and processes it
	public static boolean processQuery(String query)
	{
		singleSW = false;
		String url = "jdbc:mysql://localhost:3306/search?useSSL=false";
		String user = "root";
		String password = "";
		ArrayList<String> words;
		ArrayList<String> wordsST;
		ArrayList<String> stems = new ArrayList<String>();

		//Connecting to the database
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection con = DriverManager.getConnection(url, user, password);

			java.sql.PreparedStatement dv = con.prepareStatement("drop view if exists results_view");
			dv.execute();
			java.sql.PreparedStatement ps;

			if(query.startsWith("\"") && query.endsWith("\""))
			{
				//Phrase searching

				query = query.substring(1, query.length() - 1);

				words = new ArrayList<String>(Arrays.asList(query.split(" ")));

				String sqlST = "";
				if(words.size() > 1)
				{
					System.out.println("More than one word PS");

					sqlST = "create view results_view as select distinct * from doc_links where docID in (select a0.docID from ";

					for(int i = 0; i < words.size() - 1; i++)
					{
						sqlST = sqlST + "pos_index a" + i + ", ";
					}

					sqlST = sqlST + "pos_index a" + (words.size() - 1) + " where ";

					for(int i = 0; i < words.size() - 1; i++)
					{
						sqlST = sqlST + "a" + i + ".term = ? and ";
					}

					sqlST = sqlST + "a" + (words.size() - 1) + ".term = ? and ";

					for(int i = 1; i < words.size(); i++)
					{
						sqlST = sqlST + "a0.docID = a" + i + ".docID and ";
					}

					for(int i = 1; i < words.size(); i++)
					{
						sqlST = sqlST + "a0.tagNum = a" + i + ".tagNum and ";
					}

					for(int i = 0; i < words.size() - 1; i++)
					{
						sqlST = sqlST + "a" + (i + 1) + ".posNum - a" + i + ".posNum = 1 and ";
					}

					sqlST = sqlST + "a0.docID in (select docID from pos_index group by docID having count(*) > 1)) order by docID";

					ps = con.prepareStatement(sqlST);

					for(int i = 0; i < words.size(); i++)
					{
						ps.setString(i + 1, words.get(i));
					}

					ps.execute();
				}
				else
				{
					System.out.println("One word PS");

					ps = con.prepareStatement("create view results_view as select distinct a.docID, b.docLink, b.docTitle from pos_index a, doc_links b where a.term = ? and a.docID = b.docID");
					ps.setString(1, words.get(0));
					ps.execute();
				}

			}
			else
			{
				words = new ArrayList<String>(Arrays.asList(query.split(" ")));

				//Add stems to array
				PorterStemmer stemmer = new PorterStemmer();
				String stemmed;
				
				
				wordsST = new ArrayList<String>(words);
				String sqlST = "";
				boolean KeepStopWords = true;
				boolean AllStopWords = true;
				
				if(words.size() > 1)
				{
					System.out.println("More than one word");
					
					//Remove stopwords
					
					for(int i = 0; i < words.size(); i++)
					{
						KeepStopWords = true;
						
						for(int j = 0; j < stopwords.length; j++)
						{
							if(words.get(i).equals(stopwords[j]))
							{
								words.remove(i);
								i--;
								KeepStopWords = false;
								break;
							}
						}
						
						if(KeepStopWords)
						{
							AllStopWords = false;
							stemmer.setCurrent(words.get(i));
							stemmer.stem();
							stemmed = stemmer.getCurrent();
							if(!words.get(i).equals(stemmed))
							stems.add(stemmed);			
						}
					}
					
					if(AllStopWords)
					{
						words = new ArrayList<String>(wordsST);
					}

					sqlST = "create view results_view as select distinct * from doc_links where docID in (select a0.docID from ";

					for(int i = 0; i < words.size() - 1; i++)
					{
						sqlST = sqlST + "pos_index a" + i + ", ";
					}

					sqlST = sqlST + "pos_index a" + (words.size() - 1) + " where ";

					for(int i = 0; i < words.size() - 1; i++)
					{
						sqlST = sqlST + "a" + i + ".term = ? and ";
					}

					sqlST = sqlST + "a" + (words.size() - 1) + ".term = ? and ";

					for(int i = 1; i < words.size(); i++)
					{
						sqlST = sqlST + "a0.docID = a" + i + ".docID and ";
					}

					sqlST = sqlST + "a0.docID in (select docID from pos_index group by docID having count(*) > 1)) ";

					if(stems.size() > 0)
					{
						sqlST = sqlST + "union select distinct * from doc_links where docID in (select a0.docID from ";

						for(int i = 0; i < stems.size() - 1; i++)
						{
							sqlST = sqlST + "pos_index a" + i + ", ";
						}

						sqlST = sqlST + "pos_index a" + (stems.size() - 1) + " where ";

						for(int i = 0; i < stems.size() - 1; i++)
						{
							sqlST = sqlST + "a" + i + ".term = ? and ";
						}

						sqlST = sqlST + "a" + (stems.size() - 1) + ".term = ? and ";

						for(int i = 1; i < stems.size(); i++)
						{
							sqlST = sqlST + "a0.docID = a" + i + ".docID and ";
						}

						sqlST = sqlST + "a0.docID in (select docID from pos_index group by docID having count(*) > 1)) ";
					}

					sqlST = sqlST + "order by docID";

					ps = con.prepareStatement(sqlST);

					for(int i = 0; i < words.size(); i++)
					{
						ps.setString(i + 1, words.get(i));
					}

					for(int i = 0; i < stems.size(); i++)
					{
						ps.setString(i + words.size() + 1, stems.get(i));
					}

					ps.execute();
				}
				else
				{
					System.out.println("One word");
					
					for(int i = 0; i < stopwords.length; i++)
					{
						if(words.get(0).equals(stopwords[i]))
						{
							AllStopWords = false;
						}
					}
					
					if(AllStopWords)
					{
						stemmer.setCurrent(words.get(0));
						stemmer.stem();
						stemmed = stemmer.getCurrent();
						if(!words.get(0).equals(stemmed))
						stems.add(stemmed);	
					}

					sqlST = "create view results_view as select distinct a.docID, b.docLink, b.docTitle from pos_index a, doc_links b where a.term = ? and a.docID = b.docID";

					if(stems.size() > 0)
					{
						sqlST = sqlST + " union select distinct a.docID, b.docLink, b.docTitle from pos_index a, doc_links b where a.term = ? and a.docID = b.docID";
					}

					sqlST = sqlST + " order by docID";
					ps = con.prepareStatement(sqlST);
					ps.setString(1, words.get(0));
					if(stems.size() > 0)
					{
						ps.setString(2, stems.get(0));
					}
					ps.execute();
				}
			}

		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			return false;
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
		
		// Create a new socket at port 1235 (Will be used to read string from PHP)
		try
		{
			serverSocket_r = new ServerSocket(1235);
		}
		catch (IOException e)
		{
			System.out.println("Could not listen to port 1235");
			System.exit(-1);
		}
		
		// Create a new socket at port 1236 (Will be used to send string to PHP)
		try
		{
			serverSocket_wr = new ServerSocket(1236);
		}
		catch (IOException e)
		{
			System.out.println("Could not listen to port 1236");
			System.exit(-1);
		}
		
		// Keep running indefinitely (terminates manually)
		while(true)
		{
			// Listen to port 1235 for reading
			try
			{
				socket_r = serverSocket_r.accept();
			}
			catch (Exception e)
			{
				System.out.println("Accept failed");
				System.exit(-1);
			}
			
			// Read string from port 1235
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
						
			System.out.println("User query recieved:");
			System.out.println(query);
			
			//Sending user query to processQuery method for processing and storing results in 'results_view'
			boolean result;
			if(query != null)
				result = processQuery(query);

			// Listen to port 1236
			try
			{
				socket_wr = serverSocket_wr.accept();
			}
			catch (Exception e)
			{
				System.out.println("Accept failed");
				System.exit(-1);
			}
			
			//Write string to port 1236
			try
			{
				PrintStream ps = new PrintStream(socket_wr.getOutputStream());
				ps.print("Query received\n");					
			}
			catch (Exception e)
			{
				System.out.println("String writing on port to client failed");
				System.exit(-1);
			}
		}
	}
}
