package org.androidpn.demoapp;

import java.util.ArrayList;
import java.util.HashMap;
import android.app.Application;


public class UserInfo extends Application {

	private String myUserName;
	private String myUserPWD;
	private String myNotifierTitle;
	private String myNotifierMessage;
	private String myNotifierUri;
	private ArrayList<HashMap<String, String>> myNotifier = new ArrayList<HashMap<String,String>>();	

	//����֪ͨ�б�һ����ʼֵ����DemoAppActivity create��ʱ�����
	public void initUserInfo(){
		if (myNotifier.isEmpty()) {
			HashMap<String, String> addMap = new HashMap<String, String>();
		    addMap.put("ItemTitle", "����CampusPuSH");
		    addMap.put("ItemMessage", "������ѧ�����о���Ժ|ͨ������Ϣ��ȫʵ����\n\n���ߣ��Ż���\n\n���ܣ�������Ϣ������������Ϣ���ϴ�ͼƬ���ۿ���Ƶ���ۿ�ֱ��\n\nTips��\n��½��ʹ����http://push.pkusz.edu.cn��ע����û��������롣\n����ҳ�棬��ѡ���ύ������ȡ�����ģ�\n��Ƶֱ����������ֱ����Ƶ�������û���_��Ƶ���ƣ��ݲ����ţ�");
		    addMap.put("ItemUri", "http://push.pkusz.edu.cn");
		    this.myNotifier.add(addMap);    
		}

	}

	
	public void addMyNotifier(HashMap<String, String> addMap){
		this.myNotifier.add(0,addMap);
	}

	public ArrayList<HashMap<String, String>> getMyNotifier() {
		return myNotifier;
	}

	public void setMyNotifier(ArrayList<HashMap<String, String>> myNotifier) {
		this.myNotifier = myNotifier;
	}

	public String getMyNotifierTitle(){
		return myNotifierTitle;
	}
	
	public void setMyNotifierTitle(String myNotifierTitle){
		this.myNotifierTitle = myNotifierTitle;
	}
	
	public String getMyNotifierMessage(){
		return myNotifierMessage;
	}
	
	public void setMyNotifierMessage(String myNotifierMessage){
		this.myNotifierMessage = myNotifierMessage;
	}
	
	public String getMyNotifierUri(){
		return myNotifierUri;
	}
	
	public void setMyNotifierUri(String myNotifierUri){
		this.myNotifierUri = myNotifierUri;
	}
	
	public String getMyUserName() {
		return myUserName;
	}
	public void setMyUserName(String myUserName) {
		this.myUserName = myUserName;
	}
	public String getMyUserPWD() {
		return myUserPWD;
	}
	public void setMyUserPWD(String myUserPWD) {
		this.myUserPWD = myUserPWD;
	}
	
	
}