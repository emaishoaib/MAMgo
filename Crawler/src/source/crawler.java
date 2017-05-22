package source;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.io.*;
import java.net.*;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import source.SimpleRobotRules.RobotRulesMode;

@SuppressWarnings("unused")
public class crawler implements Runnable
{
	static private String state_txt = "C:\\xampp\\htdocs\\MAMgo\\_crawler\\state.txt";
	static private String links_txt = "C:\\xampp\\htdocs\\MAMgo\\_crawler\\links.txt";
	static private String htmls_folder = "C:\\xampp\\htdocs\\MAMgo\\_crawler\\HTMLs\\";
	
	private int threads;
	private int maxDepth;
	private Set<String> visited;
	private ArrayList<String> unvisited;
	private ArrayList<String> allLinks;
	private int state;
	private long crawlDelay = 0;
	private long lastVisit = 0;
	private String oldURL = "";
	private static java.sql.Connection con;
	private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.112 Safari/535.1";
	Object lock = new Object();
	
	public crawler(Set<String> visited, ArrayList<String> unvisited, ArrayList<String> allLinks, int threads, int maxDepth, int state)
	{
		this.threads = threads > 0 ? threads : 1;
		this.maxDepth = maxDepth;
		this.visited = visited;
		this.allLinks = allLinks;
		this.unvisited = unvisited;
		this.state = state;
		
		String url = "jdbc:mysql://localhost:3306/search?useSSL=false";
		String user = "root";
		String password = "";
		
		try {
			
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			con = DriverManager.getConnection(url, user, password);
			
		} catch (SQLException | InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			e.printStackTrace();
			System.out.println("Sql Exception");
		}
	}
		
	@SuppressWarnings("resource")
	private synchronized String nextUrl()
	{
	      String nextUrl = "";
	      
	      java.sql.PreparedStatement counts;
    	  ResultSet tempSet;
    	  int testMe;
    	  int indexTemp;
	      
	      do
	      {
	    	  if(unvisited.size() == 0)
	    	  {
	    		  return "";
	    	  }
	    	  nextUrl = unvisited.remove(0);
	      }
	      while(visited.contains(nextUrl));
	    	  
	      if(!nextUrl.equals(""))
	      {
	    	  visited.add(nextUrl);
	    	  
	    	  saveState(state);
	    	  
	    	  indexTemp = allLinks.indexOf(nextUrl);

	    	  if(indexTemp > -1)
				{
					try {
						
						counts = con.prepareStatement("select * from doc_hits where docLink = ?");
						counts.setString(1, nextUrl);
						tempSet = counts.executeQuery();
						
						tempSet.next();
						
						if(tempSet.getRow() == 0)
						{
							counts = con.prepareStatement("INSERT INTO doc_hits (`docLink`,`docHits`) VALUES(?, ?)");
							counts.setString(1, nextUrl);
							counts.setInt(2, 1);
							counts.execute();
						}
						else
						{
							counts = con.prepareStatement("UPDATE doc_hits SET `docHits` = ? WHERE `docLink` = ?");
							testMe = tempSet.getInt(2) + 1;
							counts.setInt(1, tempSet.getInt(2) + 1);
							counts.setString(2, nextUrl);
							counts.execute();
						}
						
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}

	      }

	      return nextUrl;
	}
	
	
	@Override
	public void run() 
	{
		for(int i = 0; i < threads; i++)
		{
			this.extractLinks(nextUrl(), 0);
		}
	}
	
	private synchronized boolean addUnvisited(String URL)
	{
		return unvisited.add(URL);
	}
	
	private synchronized int getState()
	{
		return state;
	}
	
	private synchronized void saveState(int State)
	{
		state = State;
		
		try(BufferedWriter file = new BufferedWriter(new FileWriter(links_txt, false))) {
			
			for(String urls : visited)
			{
				file.write(urls + "\n");
			}
			
			for(String urls : unvisited)
			{
				file.write(urls + "\n");
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try(BufferedWriter file = new BufferedWriter(new FileWriter(state_txt, false))) {

			file.write(String.valueOf(state));

		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	private boolean checkRobots(String URL) throws ClientProtocolException, IOException
	{
		String temp;
		
		URL urlObj = new URL(URL);

		String hostId = urlObj.getProtocol() + "://" + urlObj.getHost() + (urlObj.getPort() > -1 ? ":" + urlObj.getPort() : "");
		
		if(!urlObj.getHost().equals(oldURL) && !oldURL.equals(""))
		{
			lastVisit = System.currentTimeMillis();
		}
		else
		{
			crawlDelay = System.currentTimeMillis() - lastVisit;
		}
		
		Map<String, BaseRobotRules> robotsTxtRules = new HashMap<String, BaseRobotRules>();
		BaseRobotRules rules = robotsTxtRules.get(hostId);
		
		if (rules == null) {
			CloseableHttpClient httpclient = HttpClients.createDefault();
		    HttpGet httpget = new HttpGet(hostId + "/robots.txt");
		    HttpContext context = new BasicHttpContext();
		    HttpResponse response = httpclient.execute(httpget, context);
		    if (response.getStatusLine() != null && response.getStatusLine().getStatusCode() == 404) {
		        rules = new SimpleRobotRules(RobotRulesMode.ALLOW_ALL);
		        // consume entity to deallocate connection
		        EntityUtils.consumeQuietly(response.getEntity());
		    } else {
		        BufferedHttpEntity entity = new BufferedHttpEntity(response.getEntity());
		        SimpleRobotRulesParser robotParser = new SimpleRobotRulesParser();
		        rules = robotParser.parseContent(hostId, IOUtils.toByteArray(entity.getContent()),
		                "text/plain", USER_AGENT);
		    }
		    robotsTxtRules.put(hostId, rules);
		}
		
		while(rules.getCrawlDelay() >= crawlDelay)
		{
			crawlDelay = System.currentTimeMillis() - lastVisit;
			if(rules.getCrawlDelay() >= crawlDelay)
			{
				lastVisit = System.currentTimeMillis();
			}
		}
		
		return rules.isAllowed(URL);
	}
	
	@SuppressWarnings("resource")
	private void extractLinks(String URL, int depth)
	{
		Connection connection;
		Document doc;
		Elements links;
		String l;
		int end;
		
		java.sql.PreparedStatement counts;
		ResultSet tempSet;
		int testMe;
		int indexTemp;
		
		int ind1;
		int ind2;
		
		if(depth <= maxDepth)
		{

			try {
				
				if(!checkRobots(URL))
				{
					extractLinks(nextUrl(), depth);
					return;
				}
				
				System.out.println(">> " + state + " Depth: " + depth + " [" + URL + "]");
								
				connection = Jsoup.connect(URL).userAgent(USER_AGENT);
				doc = connection.get();

				if(connection.response().statusCode() == 200)
				{
					System.out.println("**Visiting** Received web page at " + URL);
				}
				else
				{
					System.out.println("**Failure** " + URL + " might be down.");
					ind1 = unvisited.indexOf(URL);
					ind2 = unvisited.indexOf(URL);
					
					if(ind1 > -1)
						unvisited.remove(ind1);
					if(ind2 > -1)
						allLinks.remove(ind2);
					
					saveState(state);
					URL = nextUrl();
					if(!URL.equals(""))
					{
						extractLinks(URL, depth);
					}
					else
					{
						return;
					}				
				}
				
				if(!connection.response().contentType().contains("text/html"))
				{
					System.out.println("**Failure** Retrieved something other than HTML");
					ind1 = unvisited.indexOf(URL);
					ind2 = unvisited.indexOf(URL);
					
					if(ind1 > -1)
						unvisited.remove(ind1);
					if(ind2 > -1)
						allLinks.remove(ind2);
					
					saveState(state);
					URL = nextUrl();
					if(!URL.equals(""))
					{
						extractLinks(URL, depth);
					}
					else
					{
						return;
					}				
				}
				
				links = doc.select("a[href]");
				
				try(BufferedWriter file = new BufferedWriter(new FileWriter(htmls_folder + state + ".html"))) {

					file.write(doc.toString());

				} catch (IOException e) {
//					e.printStackTrace();
					System.out.println("The specified path doesn't exist.");
				}
				
				state += threads;
				saveState(state);
				depth++;

				for(Element link : links)
				{
					l = link.attr("abs:href");
					end = l.indexOf('#') - 1 > 0 ? l.indexOf('#') : l.length();
					
					if(l.endsWith("/"))
						end = l.lastIndexOf('/') - 1 > 0 ? l.lastIndexOf('/') : l.length();
					
					l = l.substring(0, end);
					
					if(l != "")
					{
						if(!visited.contains(l.replaceAll("https", "http")) && !visited.contains(l.replaceAll("http", "https")) && !visited.contains(l))
						{
							if(addUnvisited(l))
							{
								if(!allLinks.contains(l))
									allLinks.add(l);
								
								URL = nextUrl();
								if(!URL.equals(""))
								{
									extractLinks(URL, depth);
								}
								else
								{
									System.out.println("**Failure** " + URL + " was visited before");
								}
							}
						}
						else
						{
							indexTemp = allLinks.indexOf(l);
							
							if(indexTemp > -1)
							{
								try {
									
									counts = con.prepareStatement("select * from doc_hits where docLink = ?");
									counts.setString(1, l);
									tempSet = counts.executeQuery();
									
									tempSet.next();
									
									if(tempSet.getRow() == 0)
									{
										counts = con.prepareStatement("INSERT INTO doc_hits (`docLink`,`docHits`) VALUES(?, ?)");
										counts.setString(1, l);
										counts.setInt(2, 1);
										counts.execute();
									}
									else
									{
										counts = con.prepareStatement("UPDATE doc_hits SET `docHits` = ? WHERE `docLink` = ?");
										testMe = tempSet.getInt(2) + 1;
										counts.setInt(1, tempSet.getInt(2) + 1);
										counts.setString(2, l);
										counts.execute();
									}
									
								} catch (SQLException e) {
									e.printStackTrace();
								}
							}
						}
					}
				}

			} catch (IOException e) {
				System.out.println("Error connecting to link\n");
				
				//********************
				try(BufferedWriter file = new BufferedWriter(new FileWriter(htmls_folder + state + ".html"))) {

					file.write("<html></html>");

				} catch (IOException e1) {
//					e.printStackTrace();
					System.out.println("The specified path doesn't exist.");
				}
				state++;
				//********************
				
				ind1 = unvisited.indexOf(URL);
				ind2 = unvisited.indexOf(URL);
				
				if(ind1 > -1)
					unvisited.remove(ind1);
				if(ind2 > -1)
					allLinks.remove(ind2);
				
//				visited.remove(URL);
				
				saveState(state);
				URL = nextUrl();
				if(!URL.equals(""))
				{
					extractLinks(URL, depth);
				}
				else
				{
					return;
				}
//				e.printStackTrace();
			} 
		}	
	}
	
	public static void main(String[] args) 
	{
		Set<String> visited = new LinkedHashSet<String>();
		ArrayList<String> unvisited = new ArrayList<String>();
		ArrayList<String> allLinks = new ArrayList<String>();
		Thread[] threads;
		String line;
		int state = 0;
		int nt = 1;
		
		try(BufferedReader file = new BufferedReader(new FileReader(state_txt)))
		{
			state = Integer.parseInt(file.readLine());

		} catch(IOException e)
		{
//			e.printStackTrace();
			System.out.println("The state file doesn't exist.\n");
		}
		
		try(BufferedReader file = new BufferedReader(new FileReader(links_txt)))
		{
			int i = 0;
			
			while((line = file.readLine()) != null)
			{
				i++;
				if(i < state)
				{
					visited.add(line);
				}
				else
				{
					unvisited.add(line);
				}
				
				if(!allLinks.contains(line))
					allLinks.add(line);
			}

		} catch(IOException e)
		{
//			e.printStackTrace();
			System.out.println("The links file doesn't exist.\n");
		}
		
		threads = new Thread[nt];
		
		if(!unvisited.isEmpty())
		{
			crawler c1 = new crawler(visited, unvisited, allLinks, 1, 10, state);
			
			for(int i = 0; i < nt; i++)
			{
				threads[i] = new Thread(c1);
				threads[i].start();
			}
			
			try
			{
				for(int i = 0; i < nt; i++)
				{
					threads[i].join();
				}
			}catch(InterruptedException e)
			{
			}
			
//			//Single threaded approach	
//			crawler c1 = new crawler(visited, unvisited, 1, 10, state);
//			c1.extractLinks(c1.nextUrl(), 0);
		}
		else
		{
			//defensive coding URL
			unvisited.add("http://www.mkyong.com");
			crawler c1 = new crawler(visited, unvisited, allLinks, 1, 10, state);
			
			for(int i = 0; i < nt; i++)
			{
				threads[i] = new Thread(c1);
				threads[i].start();
			}
			
			try
			{
				for(int i = 0; i < nt; i++)
				{
					threads[i].join();
				}
			}catch(InterruptedException e)
			{
			}
		}

		visited.clear();
		unvisited.clear();
	}

}
