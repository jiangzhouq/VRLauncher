package qjizho.vrlauncher.usb;

import android.content.Context;
import android.util.Log;

import com.jiongbull.jlog.JLog;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class ThreadReadWriterIOSocket implements Runnable,HandleInput.HandleInputListener
{
	private Socket client;
	private Context context;
	private HandleInput handleInput;
	private BufferedOutputStream out;
	private BufferedInputStream in;
	public ThreadReadWriterIOSocket(Context context, Socket client)
	{
		this.client = client;
		this.context = context;
		handleInput = HandleInput.getInstance(this.context);
		handleInput.setHandleInputListener(this);
	}


	@Override
	public void sendToClient(JSONObject jsonObject) {
		try{
			JLog.json(jsonObject.toString());
			out.write(jsonObject.toString().getBytes("UTF-8"));
			out.flush();
		}catch (Exception e){

		}
	}

	@Override
	public void run()
	{
		Log.d("chl", "a client has connected to server!");

		try
		{
			/* PC端发来的数据msg */
			String currCMD = "";
			out = new BufferedOutputStream(client.getOutputStream());
			in = new BufferedInputStream(client.getInputStream());
			androidService.ioThreadFlag = true;
			while (androidService.ioThreadFlag)
			{
				try
				{
					if (!client.isConnected())
					{
						break;
					}
					/* 接收PC发来的数据 */
					Log.v(androidService.TAG, Thread.currentThread().getName() + "---->" + "will read......");
					/* 读操作命令 */
					currCMD = readCMDFromSocket(in);
					Log.v(androidService.TAG, Thread.currentThread().getName() + "---->" + "**currCMD ==== " + currCMD);

					out.write(handleInput.handleJSON(new JSONObject(currCMD)).toString().getBytes("UTF-8"));
					out.flush();

				} catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			out.close();
			in.close();
		} catch (Exception e)
		{
			e.printStackTrace();
		} finally
		{
			try
			{
				if (client != null)
				{
					Log.v(androidService.TAG, Thread.currentThread().getName() + "---->" + "client.close()");
					client.close();
				}
			} catch (IOException e)
			{
				Log.e(androidService.TAG, Thread.currentThread().getName() + "---->" + "read write error333333");
				e.printStackTrace();
			}
		}
	}

	/* 读取命令 */
	public String readCMDFromSocket(InputStream in)
	{
		int MAX_BUFFER_BYTES = 204800;
		String msg = "";
		byte[] tempbuffer = new byte[MAX_BUFFER_BYTES];
		try
		{
			int numReadedBytes = in.read(tempbuffer, 0, tempbuffer.length);
			msg = new String(tempbuffer, 0, numReadedBytes, "utf-8");
			tempbuffer = null;
		} catch (Exception e)
		{
			Log.v(androidService.TAG, Thread.currentThread().getName() + "---->" + "readFromSocket error");
			e.printStackTrace();
		}
		// Log.v(Service139.TAG, "msg=" + msg);
		return msg;
	}
}