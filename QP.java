package search;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

import org.tartarus.snowball.ext.PorterStemmer;

public class QP {

	private static String[] stopwords = {"a", "as", "able", "about", "above", "according", "accordingly", "across", "actually", "after", "afterwards", "again", "against", "aint", "all", "allow", "allows", "almost", "alone", "along", "already", "also", "although", "always", "am", "among", "amongst", "an", "and", "another", "any", "anybody", "anyhow", "anyone", "anything", "anyway", "anyways", "anywhere", "apart", "appear", "appreciate", "appropriate", "are", "arent", "around", "as", "aside", "ask", "asking", "associated", "at", "available", "away", "awfully", "be", "became", "because", "become", "becomes", "becoming", "been", "before", "beforehand", "behind", "being", "believe", "below", "beside", "besides", "best", "better", "between", "beyond", "both", "brief", "but", "by", "cmon", "cs", "came", "can", "cant", "cannot", "cant", "cause", "causes", "certain", "certainly", "changes", "clearly", "co", "com", "come", "comes", "concerning", "consequently", "consider", "considering", "contain", "containing", "contains", "corresponding", "could", "couldnt", "course", "currently", "definitely", "described", "despite", "did", "didnt", "different", "do", "does", "doesnt", "doing", "dont", "done", "down", "downwards", "during", "each", "edu", "eg", "eight", "either", "else", "elsewhere", "enough", "entirely", "especially", "et", "etc", "even", "ever", "every", "everybody", "everyone", "everything", "everywhere", "ex", "exactly", "example", "except", "far", "few", "ff", "fifth", "first", "five", "followed", "following", "follows", "for", "former", "formerly", "forth", "four", "from", "further", "furthermore", "get", "gets", "getting", "given", "gives", "go", "goes", "going", "gone", "got", "gotten", "greetings", "had", "hadnt", "happens", "hardly", "has", "hasnt", "have", "havent", "having", "he", "hes", "hello", "help", "hence", "her", "here", "heres", "hereafter", "hereby", "herein", "hereupon", "hers", "herself", "hi", "him", "himself", "his", "hither", "hopefully", "how", "howbeit", "however", "i", "id", "ill", "im", "ive", "ie", "if", "ignored", "immediate", "in", "inasmuch", "inc", "indeed", "indicate", "indicated", "indicates", "inner", "insofar", "instead", "into", "inward", "is", "isnt", "it", "itd", "itll", "its", "its", "itself", "just", "keep", "keeps", "kept", "know", "knows", "known", "last", "lately", "later", "latter", "latterly", "least", "less", "lest", "let", "lets", "like", "liked", "likely", "little", "look", "looking", "looks", "ltd", "mainly", "many", "may", "maybe", "me", "mean", "meanwhile", "merely", "might", "more", "moreover", "most", "mostly", "much", "must", "my", "myself", "name", "namely", "nd", "near", "nearly", "necessary", "need", "needs", "neither", "never", "nevertheless", "new", "next", "nine", "no", "nobody", "non", "none", "noone", "nor", "normally", "not", "nothing", "novel", "now", "nowhere", "obviously", "of", "off", "often", "oh", "ok", "okay", "old", "on", "once", "one", "ones", "only", "onto", "or", "other", "others", "otherwise", "ought", "our", "ours", "ourselves", "out", "outside", "over", "overall", "own", "particular", "particularly", "per", "perhaps", "placed", "please", "plus", "possible", "presumably", "probably", "provides", "que", "quite", "qv", "rather", "rd", "re", "really", "reasonably", "regarding", "regardless", "regards", "relatively", "respectively", "right", "said", "same", "saw", "say", "saying", "says", "second", "secondly", "see", "seeing", "seem", "seemed", "seeming", "seems", "seen", "self", "selves", "sensible", "sent", "serious", "seriously", "seven", "several", "shall", "she", "should", "shouldnt", "since", "six", "so", "some", "somebody", "somehow", "someone", "something", "sometime", "sometimes", "somewhat", "somewhere", "soon", "sorry", "specified", "specify", "specifying", "still", "sub", "such", "sup", "sure", "ts", "take", "taken", "tell", "tends", "th", "than", "thank", "thanks", "thanx", "that", "thats", "thats", "the", "their", "theirs", "them", "themselves", "then", "thence", "there", "theres", "thereafter", "thereby", "therefore", "therein", "theres", "thereupon", "these", "they", "theyd", "theyll", "theyre", "theyve", "think", "third", "this", "thorough", "thoroughly", "those", "though", "three", "through", "throughout", "thru", "thus", "to", "together", "too", "took", "toward", "towards", "tried", "tries", "truly", "try", "trying", "twice", "two", "un", "under", "unfortunately", "unless", "unlikely", "until", "unto", "up", "upon", "us", "use", "used", "useful", "uses", "using", "usually", "value", "various", "very", "via", "viz", "vs", "want", "wants", "was", "wasnt", "way", "we", "wed", "well", "were", "weve", "welcome", "well", "went", "were", "werent", "what", "whats", "whatever", "when", "whence", "whenever", "where", "wheres", "whereafter", "whereas", "whereby", "wherein", "whereupon", "wherever", "whether", "which", "while", "whither", "who", "whos", "whoever", "whole", "whom", "whose", "why", "will", "willing", "wish", "with", "within", "without", "wont", "wonder", "would", "would", "wouldnt", "yes", "yet", "you", "youd", "youll", "youre", "youve", "your", "yours", "yourself", "yourselves", "zero"};

	@SuppressWarnings("unused")
	//if this is set to true then the user tried to search for a stopword
	private boolean singleSW = false;

	public boolean QueryProcessor(String query)
	{
		singleSW = false;
		String url = "jdbc:mysql://localhost:3306/search?useSSL=false";
		String user = "root";
		String password = "11147878";
		ArrayList<String> words;
		ArrayList<String> stems = new ArrayList<String>();

		//Connecting to the database
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection con = DriverManager.getConnection(url, user, password);

			java.sql.PreparedStatement dv = con.prepareStatement("drop view if exists result");
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

					sqlST = "create view result as select * from doc_links where docID in (select a0.docID from ";

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

					ps = con.prepareStatement("create view result as select a.docID, b.docLink from pos_index a, doc_links b where a.term = ? and a.docID = b.docID");
					ps.setString(1, words.get(0));
					ps.execute();
				}

			}
			else
			{
				words = new ArrayList<String>(Arrays.asList(query.split(" ")));

				//Remove stopwords
				/*for(int i = 0; i < stopwords.length; i++)
				{
					for(int j = 0; j < words.size(); j++)
					{
						if(words.get(j).equals(stopwords[i]))
						{
							words.remove(j);
						}
					}
				}*/

				if(words.size() == 0)
				{
					singleSW = true;
					return false;
				}

				//Add stems to array
				PorterStemmer stemmer = new PorterStemmer();
				String stemmed;
				for(int i = 0; i < words.size(); i++)
				{
					stemmer.setCurrent(words.get(i));
					stemmer.stem();
					stemmed = stemmer.getCurrent();
					if(!words.get(i).equals(stemmed))
						stems.add(stemmed);
				}

				String sqlST = "";
				if(words.size() > 1)
				{
					System.out.println("More than one word");

					sqlST = "create view result as select * from doc_links where docID in (select a0.docID from ";

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
						sqlST = sqlST + "union select * from doc_links where docID in (select a0.docID from ";

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

					sqlST = "create view result as select a.docID, b.docLink from pos_index a, doc_links b where a.term = ? and a.docID = b.docID";

					if(stems.size() > 0)
					{
						sqlST = sqlST + " union select a.docID, b.docLink from pos_index a, doc_links b where a.term = ? and a.docID = b.docID";
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

	public static void main(String[] args)
	{
		QP q = new QP();

		//Phrase Searching
		//		q.QueryProcessor("\"Ahmed Hamouda\"");

		//Normal Searching
		q.QueryProcessor("Ahmed Hamouda");

	}

}
