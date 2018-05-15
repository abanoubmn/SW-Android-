package abanoubmagdi.home.sw;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.facebook.drawee.view.SimpleDraweeView;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

public class CreatePostActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);
        final EditText post=(EditText) findViewById(R.id.post);
        final Button postBtn=(Button) findViewById(R.id.postBtn);
        SimpleDraweeView pp=(SimpleDraweeView) findViewById(R.id.pp);
        final MySQLite mySQLite=new MySQLite(this);
        Account account=mySQLite.getUserInfo();
        Uri uri=Uri.parse(account.imageUrl);
        pp.setImageURI(uri);

        post.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.length()>0)
                {
                    postBtn.setClickable(true);
                }
                else {
                    postBtn.setClickable(false);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length()>0)
                {
                    postBtn.setClickable(true);
                }
                else {
                    postBtn.setClickable(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        postBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (post.getText().toString().length()>0)
                {
                CreatePostApi createPostApi=new CreatePostApi();
                createPostApi.execute(post.getText().toString(), mySQLite.getUserInfo().accountId);
                Intent intent = new Intent(CreatePostActivity.this, Home.class);
                startActivity(intent);
                finish();
                }
            }
        });
    }

    class CreatePostApi extends AsyncTask<String, Void, String>
    {

        @Override
        protected String doInBackground(String... params) {
            String dataString="";
            try {
                String urlParameters  =
                        "PostContent=" +params[0]+
                                "&AccountID=" + params[1];
                byte[] postData       = urlParameters.getBytes( Charset.forName("UTF-8") );
                URL url = new URL( "http://192.168.1.5:6060/api/Account/CreatePost");
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
            return dataString;
        }

        @Override
        protected void onPostExecute(String result) {

        }
    }
}
