package com.mcnsa.po8;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * A complete Java class that shows how to open a URL, then read data (text) from that URL,
 * HttpURLConnection class (in combination with an InputStreamReader and BufferedReader).
 *
 * @author alvin alexander, devdaily.com.
 *
 */
public class JavaHttpUrlConnectionReader
{
  public JavaHttpUrlConnectionReader()
  {
      
  }
  
  	public void returnMessage(CommandSender sender, String message) {
		if(sender instanceof Player) {
			sender.sendMessage(processColours(message));
		}
		else {
			sender.sendMessage(message);//plugin.stripColours(message));
		}
	}
  
  	// allow for colour tags to be used in strings..
	public String processColours(String str) {
		return str.replaceAll("(&([a-f0-9]))", "\u00A7$2");
	}

  public String loadURL(String myUrl){
      try
        {
          // if your url can contain weird characters you will want to
          // encode it here, something like this:
          // myUrl = URLEncoder.encode(myUrl, "UTF-8");
          //return "2.3";
          String results = doHttpUrlConnectionAction(myUrl);
          return results;
        }
        catch (Exception e){
          return "";
        }
  }
  /**
   * Returns the output from the given URL.
   *
   * I tried to hide some of the ugliness of the exception-handling
   * in this method, and just return a high level Exception from here.
   * Modify this behavior as desired.
   *
   * @param desiredUrl
   * @return
   * @throws Exception
   */
  private static String doHttpUrlConnectionAction(String desiredUrl)
  throws Exception
  {
    URL url = null;
    BufferedReader reader = null;
    StringBuilder stringBuilder;

    try
    {
      // create the HttpURLConnection
      url = new URL(desiredUrl);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();

      // just want to do an HTTP GET here
      connection.setRequestMethod("GET");

      // uncomment this if you want to write output to this url
      //connection.setDoOutput(true);

      //if (true) return null;
      // give it 3 seconds to respond
      connection.setReadTimeout(5000);
      connection.setConnectTimeout(5000);
      connection.connect();
      // read the output from the server
      reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      stringBuilder = new StringBuilder();

      String line = null;
      while ((line = reader.readLine()) != null)
      {
        stringBuilder.append(line + "\n");
      }
      String retStr = stringBuilder.toString();
      if (retStr!=null && retStr.contains("Commands:")){
          return retStr.substring(0,retStr.indexOf("Commands:"));
      }
      return retStr;
      
    }
    catch (SocketTimeoutException e)
    {
//      e.printStackTrace();
//      throw e;
        return "SocketTimeoutException";
    }
    finally
    {
      // close the reader; this can throw an exception too, so
      // wrap it in another try/catch block.
      if (reader != null)
      {
        try
        {
          reader.close();
        }
        catch (IOException ioe)
        {
//          ioe.printStackTrace();
            return "URL reader error";
        }
      }
    }
  }
}