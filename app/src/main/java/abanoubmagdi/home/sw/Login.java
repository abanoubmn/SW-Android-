package abanoubmagdi.home.sw;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class Login extends AppCompatActivity {

    private static String dataString;
    EditText username, password;
    Button login;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        username=(EditText) findViewById(R.id.username);
        password=(EditText) findViewById(R.id.password);
        login=(Button) findViewById(R.id.login);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SWApi swApi=new SWApi();
                swApi.execute("mofo", "12345");
            }
        });
    }

    class SWApi extends AsyncTask<String, Void, Account>
    {

        @Override
        protected Account doInBackground(String... params) {
            try {
                String urlParameters  =
                        "UserName=" +params[0]+
                        "&Password=" + params[1];
                byte[] postData       = urlParameters.getBytes( Charset.forName("UTF-8") );
                URL url = new URL( "http://192.168.1.7:6060/api/Account/Login");
                HttpURLConnection conn= (HttpURLConnection) url.openConnection();
                conn.setDoOutput( true );
                conn.setInstanceFollowRedirects( false );
                conn.setRequestMethod( "POST" );
                conn.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestProperty( "charset", "utf-8");
                conn.setRequestProperty( "Content-Length", Integer.toString( postData.length ));
                conn.setUseCaches( false );
                conn.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream( conn.getOutputStream());
                wr.write( postData );
                InputStream inputStream= conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuffer  buffer = new StringBuffer();
                String line;

                while ((line = reader.readLine()) != null) {       // if reader buffer find data set it in String Buffer
                    buffer.append(line);
                }

                dataString=buffer.toString();
                Log.e("login", dataString);
            }
            catch (IOException e){
                Log.e("connetion error", e.getMessage());
            }


            if (dataString.contains("AccountID"))
                return getDataFromJSON(dataString);
            else
                return null;

        }

        private Account getDataFromJSON(String dataString)
        {
            Account account=new Account();
            try {
                JSONObject jsonObject=new JSONObject(dataString);

                account.fullName=jsonObject.getString("FullName");
                account.userName=jsonObject.getString("UserName");
                account.accountId=jsonObject.getString("AccountID");
                String imageUrl=jsonObject.getString("ProfilePicture");
                if (!imageUrl.equals(""))
                    account.imageUrl="http://192.168.1.5:6060" +imageUrl;
                else
                    account.imageUrl=null;

            }
            catch (Exception e){
                Log.e("jsonException",e.getMessage());
            }
            return account;
        }

        @Override
        protected void onPostExecute(Account account) {
            if (account!= null) {
                MySQLite mySQLite = new MySQLite(Login.this);
                mySQLite.addUserInfo(account);
                Intent intent = new Intent(Login.this, Home.class);
                startActivity(intent);
            }
            else {
                Toast.makeText(Login.this,"password is incorrect",Toast.LENGTH_LONG).show();
            }
        }
    }
}
