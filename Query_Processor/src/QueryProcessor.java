
import java.io.*;
import java.net.*;
import java.util.*;

public class QueryProcessor
{	
	public static void main(String[] args) throws Exception
	{
		ServerSocket serverSocket_r = null;
		ServerSocket serverSocket_wr = null;
		
		Socket socket_r = null;
		Socket socket_wr = null;
		
		String query = null;
		
		//Create a new socket at port for 1235 (Will be used to read string from PHP)
		try
		{
			serverSocket_r = new ServerSocket(1235);
		}
		catch (IOException e)
		{
			System.out.println("Could not listen to port 1235");
			System.exit(-1);
		}
		
		//Create a new socket at port 1236 (Will be used to send string to PHP)
		try
		{
			serverSocket_wr = new ServerSocket(1236);
		}
		catch (IOException e)
		{
			System.out.println("Could not listen to port 1236");
			System.exit(-1);
		}
		
		//Keep running
		while(true)
		{
			//Listen to port 1235 for reading
			try
			{
				socket_r = serverSocket_r.accept();
			}
			catch (Exception e)
			{
				System.out.println("Accept failed");
				System.exit(-1);
			}
			
			//Read string from port 1235
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
						
			//Processing done here
			//******
			
			//Listen to port 1236
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
