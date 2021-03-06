package org.androidpn.demoapp;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.androidpn.client.Constants;
import org.androidpn.client.XmppManager;
import org.androidpn.server.model.User;
import org.androidpn.util.Xmler;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.Layout;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.os.Handler;
import android.widget.EditText;

public class ChatActivity extends Activity {
	private String LOGTAG = "chatActivity";
	List<ChatInfo> messages;
	private String USERNAME;
	private String PASSWORD;
	private String recipient = "";// who you are talking with
	private Handler mHandler = new Handler();
	private EditText mTextMessage;
	private ListView mListView;
	private List<User> mFriendList;
	private XmppManager xmppManager;
	private Map<String, View> viewList;// store the chat view

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		Log.i(LOGTAG, "hello i am oncreate");
		USERNAME = getIntent().getStringExtra("userID");
		PASSWORD = getIntent().getStringExtra("Pwd");// used for 8080 connection

		xmppManager = Constants.xmppManager;// if it is null, this will be a
											// trouble

		packetList = new ArrayList<Packet>();

		// messages stores the talk contents with friends
		messages = new ArrayList<ChatInfo>();
		messages.add(new ChatInfo("test", "helloworld", new Time()));

		// ArrayAdapter<String> mAdapter = new
		// ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,messages);
		MyAdapter mAdapter = new MyAdapter(this, messages);
		viewList = new HashMap<String, View>();

		// chatUri=getString(R.string.androidpnserver+"/send.xml?"); this is too
		// slow for chat
		mTextMessage = (EditText) this.findViewById(R.id.MessageEdit);

		mListView = (ListView) this.findViewById(R.id.MessageList);
		mListView.setAdapter(mAdapter);

		mFriendList = new ArrayList<User>();

		smThread = new SendMsgThread(xmppManager);
		smThread.start();

		// Set a listener to send a chat text message
		Button sendBtn = (Button) this.findViewById(R.id.SendBtn);
		
		TextView findBtn = (TextView) this.findViewById(R.id.FindUserBtn);

		findBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				EditText findText = (EditText) ChatActivity.this
						.findViewById(R.id.FindUserEdit);
				String s = findText.getText().toString();
				findUser(s);
			}
		});
//		// go back home button
//		Button homeBtn = (Button) this.findViewById(R.id.HomeBtn);
//		homeBtn.setBackgroundColor(Color.WHITE);
//		homeBtn.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View arg0) {
//				// TODO Auto-generated method stub
//				Intent intent = new Intent(ChatActivity.this,
//						DemoAppActivity.class);
//				startActivity(intent);
//			}
//		});
		// send message button
		sendBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				String to = recipient;
				if (to == null) {// 先发送给自己　做测试
					to = "abc";
				}
				String text = mTextMessage.getText().toString();

				Log.i(LOGTAG,
						"XMPPChatActivity#send.onclicklistener Sending text "
								+ text + " to " + to);
				Message msg = new Message("push", Message.Type.chat);
				msg.setBody(text);
				if (xmppManager == null) {
					Log.i(LOGTAG,
							"XMPPChatActivity#send.onclicklistener xmppmanager is null");
					return;
				} else {
					// 显示出来
					/*
					 * TextView v=new TextView(ChatActivity.this);
					 * v.setBackgroundColor(Color.BLUE);
					 * v.setText(msg.getBody());
					 */
					messages.add(new ChatInfo(USERNAME, msg.getBody(),
							new Time()));
					// mListView.getAdapter().get
					// mListView.addView(v);
					// 添加到索引
					// viewList.put(msg.getPacketID(), v);
					// 添加到发送队列
					addMsg(msg);
				}
				mTextMessage.setText("");
			}
		});
		xmppManager.addPacketListener(new PacketListener() {
			@Override
			public void processPacket(Packet packet) {
				// TODO Auto-generated method stub
				if (packet instanceof Message) {
					// 显示接收到的消息
					/*
					 * TextView v=new TextView(ChatActivity.this);
					 * v.setText(((Message)packet).getBody());
					 * mListView.addView(v); //添加到索引
					 * viewList.put(packet.getPacketID(),v);
					 */
					String username = packet.getFrom();
					messages.add(new ChatInfo(username, ((Message) packet)
							.getBody(), new Time()));
				} else if (packet instanceof IQ) {
					if (((IQ) packet).getType() == IQ.Type.RESULT) {
						if (((IQ) packet).getError() == null) {
							// 发送的消息成功被服务器接收
							/*
							 * TextView
							 * v=(TextView)(viewList.get(packet.getPacketID()));
							 */
							// if(v!=null)
							// v.setBackgroundColor(Color.WHITE);
							Log.i(LOGTAG, "recv a result IQ whose id is :"
									+ packet.getPacketID());
						}
					}
				}
			}
		}, new PacketFilter() {
			@Override
			public boolean accept(Packet packet) {
				// TODO Auto-generated method stub
				return packet.getPacketID() != null;
			}
		});
		new InitTask().run();
	}

	/*
	 * related to chat list
	 */
	private class ChatInfo {
		String username;
		String chatXml;
		Time time;

		public ChatInfo(String u, String chat, Time time) {
			this.username = u;
			this.chatXml = chat;
			this.time = time;
		}

		public Time getTime() {
			return time;
		}

		public String getContent() {
			return chatXml;
		}

		public String getName() {
			return username;
		}
	}

	private class MyAdapter extends BaseAdapter {
		List al;
		Context c;

		public MyAdapter(Context c, List al) {
			this.al = al;
			this.c = c;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return al.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return (ChatInfo) al.get(position);
		}

		@Override
		public long getItemId(int position) {

			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = null;
			LayoutInflater li = LayoutInflater.from(c); 
			//li=(LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);  li.inflate(itemLayout, layout, true); 
			v = li.inflate(R.layout.left_chat_list, null);
			((TextView) v.findViewById(R.id.tvdateandtime))
					.setText(((ChatInfo) al.get(position)).getTime().toString());
			((TextView) v.findViewById(R.id.tvname)).setText(((ChatInfo) al
					.get(position)).getName());
			((TextView) v.findViewById(R.id.tvcontent)).setText(((ChatInfo) al
					.get(position)).getContent());
			return v;
		}
	}

	/*
	 * related to find and add friend
	 */
	private void findUser(String s) {
		String androidpnURL = getString(R.string.androidpnserver);
		StringBuilder parameter = new StringBuilder();
		parameter.append("action=getUser"); //
		parameter.append("&username=" + s);
		/*--End--*/
		String resp = GetPostUtil.send("POST", androidpnURL + "user.xml",
				parameter);
		if (resp != null) {
			resp = resp.substring(resp.indexOf("\n") + 1);
			resp = resp.replaceAll("\n", "");
			int i = resp.indexOf("<user>"), j;
			if (i < 0 || (j = resp.indexOf("</user>")) < 0) {
				alert1("未找到相应用户");
				Log.i(LOGTAG, "USER NOT FOUND");
				return;
			} else {
				String str = resp.substring(i, j + 7);
				Log.i(LOGTAG, "user :" + str);
				Xmler.getInstance().alias("user", User.class);
				User u = (User) Xmler.getInstance().fromXML(str);

				if (u == null) {
					Log.i(LOGTAG, "user not valid");
					alert1("用户无效");
					return;
				}
				Log.i(LOGTAG, "USER FOUND:" + u.getName());
				displayUser(u);
			}
		}
	}

	private void displayUser(User u) {
		LayoutInflater inflater = (LayoutInflater) this
				.getSystemService(LAYOUT_INFLATER_SERVICE);
		// 这里的main布局是在inflate中加入的哦，以前都是直接this.setContentView()的吧？呵呵
		// 该方法返回的是一个View的对象，是布局中的根
		View layout = inflater.inflate(R.layout.user_info, null);
		TextView usernameLabel = (TextView) layout
				.findViewById(R.id.UsernameLabel);
		TextView foLink = (TextView) layout.findViewById(R.id.UserFoLink);
		foLink.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Log.i(LOGTAG, "FRIEND ADDED");
			}
		});
		usernameLabel.setText(u.getName());
		PopupWindow uWindow = new PopupWindow(layout, 200, 100);// LayoutParams.FILL_PARENT,
																// LayoutParams.WRAP_CONTENT);
																// //后两个参数是width和height
		uWindow.setBackgroundDrawable(new BitmapDrawable());
		uWindow.setFocusable(true);
		uWindow.setOutsideTouchable(false);
		uWindow.showAtLocation(inflater.inflate(R.layout.activity_chat, null),
				1, 0, 0);
	}

	/*
	 * related to alert a message window on the screen
	 */
	private void alert(String s) {
		LayoutInflater inflater = (LayoutInflater) this
				.getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.result_alert, null);
		TextView resultLabel = (TextView) layout
				.findViewById(R.id.ResultAlertLabel);
		resultLabel.setText(s == null ? "null" : s);
		resultLabel.setTextColor(Color.GREEN);
		resultLabel.setBackgroundColor(Color.BLACK);
		final PopupWindow uWindow = new PopupWindow(layout, 200, 100);
		uWindow.showAtLocation(inflater.inflate(R.layout.activity_chat, null),
				1, 0, 0);
		Button closeBtn = (Button) layout.findViewById(R.id.AlertCloseBtn);
		closeBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				uWindow.dismiss();
			}
		});
	}
	public void alert1(String s){
		Dialog dialog = new AlertDialog.Builder(this).setIcon(
	     android.R.drawable.ic_dialog_info).setTitle("结果").setMessage(
	     s).setPositiveButton("确定",
	     new OnClickListener() {
	      @Override
	      public void onClick(DialogInterface dialog, int which) {
	       // TODO Auto-generated method stub
//	       Toast.makeText(ChatActivity.this, "我很喜欢他的电影。",
//	         Toast.LENGTH_LONG).show();
	      }
	      }).create();
	   dialog.show();

	}

	/**
	 * related to send message thread always try to send packets to server if
	 * packet in packetList
	 */
	private List<Packet> packetList;
	private SendMsgThread smThread;

	public void addMsg(Packet msg) {
		synchronized (packetList) {
			packetList.add(msg);
		}
	}

	@SuppressWarnings("unused")
	private class SendMsgThread extends Thread {
		final XmppManager xmppManager;

		private SendMsgThread(XmppManager manager) {
			xmppManager = manager;
		}

		public void run() {
			Log.i(LOGTAG, "chatactivity#SendMsgTask#run()... ");
			XMPPConnection conn = null;
			Packet packet = null;
			while (true) {
				// since this thread is only reading the packetlist, no need to
				// synchronized
				// synchronized(packetList){
				if (packetList.isEmpty()) {
					try {
						Thread.currentThread();
						Thread.sleep(200);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					continue;
				}
				synchronized (packetList) {
					packet = packetList.get(0);
				}
				// packetList.remove(0);　//we may not need to remove it now, we
				// can remove when it's sent
				Log.i(LOGTAG, "XmppManager#sendMsgTask packet in sending:"
						+ packet.toXML());
				xmppManager.sendMsg(packet);

				Log.i(LOGTAG,
						"XmppManager#sendMsgTask waiting for packet to be sent");
				Log.i(LOGTAG, "XmppManager#sendMsgTask !!!!packet sent");
				synchronized (packetList) {
					packetList.remove(0);
				}
			}
		}

	}

	/*
	 * related to get roster(friend list) thread
	 */
	public class InitTask implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			Log.i(LOGTAG, "init task running");
			Roster rost = xmppManager.getConnection().getRoster();
			if (rost == null) {
				Log.d(LOGTAG, "roster null");
				return;
			}
			Log.i(LOGTAG, "roster:" + rost.toString());
			Collection<RosterEntry> entries = rost.getEntries();
			for (RosterEntry entry : entries) {
				User u = new User();
				u.setUsername(entry.getUser());
				Log.i(LOGTAG, "roster item:" + entry.getUser());
				mFriendList.add(new User());
			}
			Log.i(LOGTAG, "init task finished");
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.chat, menu);
		return true;
	}

}
