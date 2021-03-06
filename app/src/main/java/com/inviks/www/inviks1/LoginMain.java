package com.inviks.www.inviks1;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.inviks.Helper.Helper;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;


public class LoginMain extends BaseActivityClass
{
    EditText email, password;
    TextView err;
    Context context=this;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_main);
        email = (EditText) findViewById(R.id.txtEmailLogin);
        password = (EditText) findViewById(R.id.txtPasswordLogin);
        err = (TextView) findViewById(R.id.lblErrorLogin);
    }

    public void onOkClick(View view)
    {
        new LoginCheck().execute(email.getText().toString(), password.getText().toString());
    }

    public void onCancelClick(View view)
    {
        this.finish();
    }

    public void onForgotClick(View view)
    {
        Intent intent = new Intent(this, ForgotPassword.class);
        startActivityForResult(intent, 1);
    }

    public void onRegisterClick(View view)
    {
        Intent intent = new Intent(this, NewUserRegistration.class);
        startActivityForResult(intent, 1);
    }

    class LoginCheck extends AsyncTask<String, String, Void>
    {
        //private ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        InputStream is = null;
        String result = "";

        @Override
        protected Void doInBackground(String... params)
        {
            String url_select = getString(R.string.serviceURL) + "users/userLogin";
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(url_select);

            try
            {
                httpGet.setHeader("userId", params[0]);
                httpGet.setHeader("pwd", params[1]);
                HttpResponse httpResponse = httpClient.execute(httpGet);
                HttpEntity httpEntity = httpResponse.getEntity();
                is = httpEntity.getContent();

            }
            catch (Exception e)
            {
                Log.e("Inviks", "Error in http connection " + e.toString());
            }
            try
            {
                BufferedReader br = new BufferedReader(new InputStreamReader(is));

                // it calls a stored procedure which returns result in form of true, false or pending
                result = br.readLine().toLowerCase();
                result=result.trim().replace("\"","");
                is.close();
            }
            catch (Exception e)
            {
                Log.e("Inviks", "Error converting result " + e.toString());
            }
            return null;
        }

        protected void onPostExecute(Void v)
        {
            if (result.equals("true")) //ok
            {
                err.setVisibility(View.INVISIBLE);
                // if already logged in
                if(Helper.isUserLoggedIn(context))
                {
                    signout();
                }
                Helper.putSharedPref(context, getString(R.string.isLoggedIn_sharedPref_string), "yes");
                Helper.putSharedPref(context, getString(R.string.loggedInUser_sharedPref_string), email.getText().toString());
                finish();
            }
            else if (result.equals("false")) //means userid/password do not match
            {
                err.setText("Username or password is invalid");
                err.setVisibility(View.VISIBLE);
            }
            else if(result.equals("pending"))
            {
                err.setText("Please activate your account by the link from your email.");
                err.setVisibility(View.VISIBLE);
            }
        }
    }
}
