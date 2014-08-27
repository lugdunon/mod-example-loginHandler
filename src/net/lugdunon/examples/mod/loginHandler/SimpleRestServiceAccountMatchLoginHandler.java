package net.lugdunon.examples.mod.loginHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import net.lugdunon.Server;
import net.lugdunon.command.CommandRequest;
import net.lugdunon.server.login.ILoginCheckHandler;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author christophergray
 *
 * This login check handler contacts a simple external REST request that returns a JSONArray
 * containing a list of account names.
 * 
 * If the account name is in the list, then the connection allowed.
 * 
 * If the account name is not in the list, then the connection not allowed.
 *
 */
public class SimpleRestServiceAccountMatchLoginHandler implements ILoginCheckHandler
{
	//GET OUR LOGGER INSTANCE
	private static final Logger LOGGER=LoggerFactory.getLogger(SimpleRestServiceAccountMatchLoginHandler.class);
	
	/**
	 * Handles a login check against a specific account and request.
	 * 
	 * @param {Account} account - the account that is attempting to log in.
	 * @param {CommandRequest} request - the request associated with this log in attempt.
	 * @returns {Boolean} - true if the login is successful.
	 */
	@Override
	public boolean handleLoginCheck(String accountName, CommandRequest request)
	{
		boolean loginChecked=false;
		
		//MATCH SERVICE IS ENABLED
		if(Server.getServerProperty("net.lugdunon.examples.match.service.enabled",false))
		{
			BufferedReader br=null;
			
			try
			{
				//GET URL FROM SERVER PROPERTY
		        URL               url                =new URL(Server.getServerProperty("net.lugdunon.examples.match.service.url", "http://lugdunon.net/exampleMatch.json"));
		        URLConnection     conn               =url.openConnection();
		        InputStreamReader isr                =new InputStreamReader(conn.getInputStream());
		        StringBuilder     rawResults         =new StringBuilder();
		        String            buffer             =null;
		        JSONArray         matchServiceResults=null;
		        
	            br=new BufferedReader(isr);
		        
		        while ((buffer=br.readLine())!=null)
		        {
		        	rawResults.append(buffer);
		        }
		        
		        matchServiceResults=new JSONArray(rawResults.toString());
		        
		        //LOOK FOR ACCOUNT IN MATCH SERVICE RESULTS
		        for(int i=0;i<matchServiceResults.length();i++)
		        {
		        	//ACCOUNT IS IN MATCH SERVICE RESULTS
		        	if(accountName.equals(matchServiceResults.get(i)))
		        	{
		        		loginChecked=true;
		        		break;
		        	}
		        }
			}
			catch(Exception e)
			{
				LOGGER.error("Error fetching results from Match Service.",e);
			}
			finally
			{
				try
                {
					if(br != null)
					{
						br.close();
					}
                }
                catch (IOException e)
                {
	                ;
                }
			}
		}
		
		return(loginChecked);
	}

	/**
	 * Returns the message that is displayed on the client when a login check fails.
	 * 
	 * @returns {String} - the message displayed on the client when a login check fails.
	 */
	@Override
	public String getLoginCheckFailedMessage()
	{
		return("<br/>Account is not registered with the match service.");
	}
}
