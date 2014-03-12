package org.androidpn.demoapp;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

import javax.crypto.EncryptedPrivateKeyInfo;

import org.androidpn.client.Constants;
import org.androidpn.client.NotificationService;
import org.androidpn.client.NotificationService.LocalBinder;
import org.androidpn.client.ServiceManager;
import org.androidpn.client.XmppManager;
import org.androidpn.demoapp.R;
import org.androidpn.util.ActivityUtil;
import org.androidpn.util.IsNetworkConn;
import org.apache.http.util.ByteArrayBuffer;
import org.apache.http.util.EncodingUtils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.view.Gravity;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.inputmethod.InputMethodManager;

@SuppressLint("NewApi")
public class LoginActivity extends Activity {
	private SharedPreferences originSharedPrefs;
	private TextView txt_info = null;
	private Button btn_login = null;
	private Button btn_clean = null;
	private EditText userName = null;
	private EditText passWord = null;
	private ImageView logo;
	private InputMethodManager imm;
	private String encryptedPW = null;
	private boolean isInputOK = false;
	private LinearLayout loginLayout, centerLayout;
	private CheckBox autoLogin;
	UserInfo userInfo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		// ���ӵ�activitylist��������ͳһ�˳�
		ActivityUtil.getInstance().addActivity(this);

		setDisplay();

		originSharedPrefs = this.getSharedPreferences(
				Constants.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);

		// �ж��Ƿ�����
		IsNetworkConn isConn = new IsNetworkConn(LoginActivity.this);
		if (!isConn.isConnected) {
			Toast.makeText(getApplicationContext(), "δ��������������",
					Toast.LENGTH_LONG).show();
			// userName.setEnabled(false);
			// passWord.setEnabled(false);
			// btn_login.setEnabled(false);
			// btn_clean.setEnabled(false);
		}

		userInfo = (UserInfo) getApplication();

		// ����ѱ����û��������룬��ֱ�ӿ�ʼ����
		if (originSharedPrefs.contains(Constants.XMPP_USERNAME)
				&& originSharedPrefs.contains(Constants.XMPP_PASSWORD)) {
			Toast.makeText(LoginActivity.this, "��½...", Toast.LENGTH_LONG)
					.show();
			startConnect(); // ��ʼ����androidpn server
			Intent intent = new Intent(LoginActivity.this,
					DemoAppActivity.class);
			LoginActivity.this.startActivity(intent);
//			LoginActivity.this.finish();
		}

		// if(originSharedPrefs.contains(Constants.XMPP_USERNAME)
		// &&originSharedPrefs.contains(Constants.XMPP_PASSWORD)){
		// userName.setText(originSharedPrefs.getString(Constants.XMPP_USERNAME,
		// null));
		// encryptedPW = originSharedPrefs.getString(Constants.XMPP_PASSWORD,
		// null);
		// passWord.setText(encryptedPW);
		// }

		btn_login.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				hidekeyboard();
				String theUserName = userName.getText().toString().trim();
				String thePassWord = passWord.getText().toString().trim();

				if (theUserName.equals("")) {
					userName.setHint("�û�������Ϊ��");
				}
				if (thePassWord.equals("")) {
					passWord.setHint("���벻��Ϊ��");
				}
				if (!theUserName.isEmpty() && !thePassWord.isEmpty()) {
					isInputOK = true;
				}
				if (isInputOK) {
					// ������м���
					// if(encryptedPW == null){
					try {
						encryptedPW = toMD5((thePassWord).getBytes("GBK"));
//						passWord.setText(encryptedPW);
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					// }
					Log.i("xiaobingo", "MD5���ܺ�����룺" + encryptedPW);

					// �����û��������뵽android server������֤
					String androidpnURL = getString(R.string.androidpnserver);
					// ��POST��ʽ����
					/*--ƴ��POST�ַ���--*/
					StringBuilder parameter = new StringBuilder();
					parameter.append("action=checkUser"); //
					parameter.append("&androidName=");
					parameter.append(theUserName);
					parameter.append("&androidPwd=");
					parameter.append(encryptedPW);
					/*--End--*/
					String resp = GetPostUtil.send("POST", androidpnURL
							+ "user.do", parameter);
					Log.i("LoginActivity", "resp:" + resp);

					// ��֤�û�������ɹ�
					if (resp.contains("check:success")) {
						// ���潫��õ��û��������뱣����UserInfo������
						userInfo.setMyUserName(theUserName);
						userInfo.setMyUserPWD(encryptedPW);
						Log.i("xiaobingo",
								"userInfo���û���" + userInfo.getMyUserName());
						Log.i("xiaobingo",
								"userInfo������" + userInfo.getMyUserPWD());
						// ��ȡ���û���������д�뱣��
						Editor editor = originSharedPrefs.edit();
						editor.putString(Constants.XMPP_USERNAME, theUserName);
						editor.putString(Constants.XMPP_PASSWORD, encryptedPW);
						editor.commit();
						// ��ʼ����androidpn server
						startConnect();

						// ��½�ɹ��������û���������
						Intent intent = new Intent(LoginActivity.this,
								DemoAppActivity.class);
						Bundle bundle = new Bundle();
						bundle.putString("name", theUserName);
						bundle.putString("password", encryptedPW);
						intent.putExtras(bundle);
						LoginActivity.this.startActivity(intent);
						setContentView(R.layout.transition);
						// LoginActivity.this.finish();
					}
					// �������
					else if (resp.contains("check:password failure")) {
						Toast.makeText(getApplicationContext(), "�������",
								Toast.LENGTH_SHORT).show();
					}
					// �û�������
					else if (resp.contains("check:not exist")) {
						Toast.makeText(getApplicationContext(), "���û������ڣ�",
								Toast.LENGTH_SHORT).show();
					}
				}
			}
		});

		btn_clean.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				userName.setText("");
				passWord.setText("");
			}
		});
	}

	/*
	 * Start to connect to the androidpn server.
	 */
	private void startConnect() {
		// TODO Auto-generated method stub
		ServiceManager serviceManager = new ServiceManager(this);
		// ��serviceManager����Constants�У���ȫ�ֵ��ã��������˳���½ʱ�����serviceManager��stopService()
		Constants.serviceManager = serviceManager;
		serviceManager.setNotificationIcon(R.drawable.notification);
		
//		serviceManager.startService();
		
		Intent intent = new Intent(this,NotificationService.class);
		startService(intent);
		bindService(intent, myConnection, Context.BIND_AUTO_CREATE);
	}

	/*
	 * Encrypt the password to MD5.
	 */
	protected String toMD5(byte[] pwd) {
		// TODO Auto-generated method stub
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(pwd);
			StringBuffer sb = new StringBuffer();
			for (byte b : md.digest()) {
				sb.append(String.format("%02x", b & 0xff));
			}
			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

	}

	/*
	 * Set the display of the components.
	 */
	private void setDisplay() {
		userName = (EditText) findViewById(R.id.username);
		passWord = (EditText) findViewById(R.id.password);
		btn_login = (Button) findViewById(R.id.btn_login);
		btn_clean = (Button) findViewById(R.id.btn_clean);
		autoLogin = (CheckBox) findViewById(R.id.autologin);
		logo = (ImageView) findViewById(R.id.logo);
		imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		loginLayout = (LinearLayout) findViewById(R.id.login_layout);
		centerLayout = (LinearLayout) findViewById(R.id.centerlayout);
		LinearLayout.LayoutParams centerlayout_param = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

		centerlayout_param.width = getWindowManager().getDefaultDisplay()
				.getWidth() * 5 / 6;
		centerlayout_param.gravity = Gravity.CENTER_HORIZONTAL;

		centerLayout.setLayoutParams(centerlayout_param);

		loginLayout.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				loginLayout.requestFocus();
				hidekeyboard();
				return false;
			}
		});

		loginLayout.getViewTreeObserver().addOnGlobalLayoutListener(
				new OnGlobalLayoutListener() {

					public void onGlobalLayout() {
						// TODO Auto-generated method stub
						Rect r = new Rect();

						loginLayout.getWindowVisibleDisplayFrame(r);
						int heightDiff = loginLayout.getRootView().getHeight()
								- r.height();
						if (heightDiff > 100) {
							logo.setVisibility(View.GONE);
						} else {
							logo.setVisibility(View.VISIBLE);
						}
					}
				});
	}
	
	private void hidekeyboard(){
		imm.hideSoftInputFromWindow(loginLayout.getWindowToken(), 0);
		logo.setVisibility(View.VISIBLE);
	}
	
	/** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection myConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocalBinder binder = (LocalBinder) service;
//            mService = binder.getService();
//            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
//            mBound = false;
        }
    };
    
    @Override
    public void onDestroy(){
    	super.onDestroy();
    	unbindService(myConnection);
    }

}