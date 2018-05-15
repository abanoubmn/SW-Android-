package abanoubmagdi.home.sw;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;

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

public class PostActivity extends AppCompatActivity {
    ListView comments;
    int postId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        comments= (ListView) findViewById(R.id.comments_in_post_activity);
        TextView fullname = (TextView) findViewById(R.id.fullname);
        TextView content = (TextView) findViewById(R.id.postcontent);
        TextView date=(TextView) findViewById(R.id.datecreated);
        SimpleDraweeView pp = (SimpleDraweeView) findViewById(R.id.pp);
        final EditText comment=(EditText) findViewById(R.id.comment);
        final Button commentBtn=(Button) findViewById(R.id.create_comment);
        Bundle bundle=getIntent().getExtras();
        final MySQLite mySQLite=new MySQLite(this);
        final Post post =mySQLite.getPost(bundle.getInt("postId"));
        postId=post.postId;
        fullname.setText(post.fullName);
        content.setText(post.postContent);
        date.setText(post.dateCreated.toString());

        Uri uri=Uri.parse(post.imageUrl);
        pp.setImageURI(uri);

        CommentApi commentApi=new CommentApi();
        commentApi.execute(post.postId);

        comment.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    commentBtn.setVisibility(View.VISIBLE);
                }
                else {
                    commentBtn.setVisibility(View.GONE);
                }
            }
        });

        commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateCommentApi createCommentApi=new CreateCommentApi();
                createCommentApi.execute(comment.getText().toString(), mySQLite.getUserInfo().accountId,
                        ""+ post.postId);
                comment.setText("");
                InputMethodManager inputManager = (InputMethodManager)
                        getSystemService(PostActivity.INPUT_METHOD_SERVICE);

                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
                comment.clearFocus();
            }
        });
    }
    class CommentApi extends AsyncTask<Integer, Void, ArrayList<Comment>>
    {
        String dataString;
        @Override
        protected ArrayList<Comment> doInBackground(Integer... params) {
            try {
                URL url = new URL("http://192.168.1.5:6060/" +
                        "api/Account/GetComments/"+params[0]);
                HttpURLConnection urlConnection=(HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream= urlConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuffer  buffer = new StringBuffer();
                String line;

                while ((line = reader.readLine()) != null) {       // if reader buffer find data set it in String Buffer
                    buffer.append(line);
                }
                dataString=buffer.toString();
            }
            catch (IOException e){
                Log.e("connetion error", e.getMessage());

                cancel(true);
            }
            return getDataFromJSON(dataString);
        }

        private ArrayList<Comment> getDataFromJSON(String dataString)
        {
            ArrayList<Comment> commentArrayList = new ArrayList<>();
            try {
                JSONArray jsonArray=new JSONArray(dataString);
                for (int i=0; i<51; i++){
                    Comment comment=new Comment();
                    final JSONObject jsonObject= jsonArray.getJSONObject(i);
                    comment.commentContent =jsonObject.getString("CommentContent");
                    comment.fullName=jsonObject.getString("FullName");
                    comment.userName=jsonObject.getString("UserName");
                    comment.postId=jsonObject.getInt("PostID");
                    comment.commentId=jsonObject.getInt("CommentID");
                    String imageUrl=jsonObject.getString("ImageURL");
                    if (!imageUrl.equals(""))
                        comment.imageUrl="http://192.168.1.5:6060" +imageUrl;
                    else
                        comment.imageUrl=null;
                    commentArrayList.add(comment);
                    Log.e("Comment Content", comment.commentContent);
                }
            }
            catch (Exception e){
                Log.e("jsonException",e.getMessage());
            }
            return commentArrayList;
        }

        @Override
        protected void onPostExecute(ArrayList<Comment> commentArrayList) {
            CommentAdapter commentAdapter = new CommentAdapter(PostActivity.this, commentArrayList);
            comments.setAdapter(commentAdapter);
        }

        @Override
        protected void onCancelled() {
            Toast.makeText(PostActivity.this,"Connection Error", Toast.LENGTH_SHORT).show();
        }
    }

    class CreateCommentApi extends AsyncTask<String, Void, Integer>
    {
        @Override
        protected Integer doInBackground(String... params) {
            String dataString="";

            Log.e("createcomment",Integer.getInteger(params[2])+"");
            try {
                String urlParameters  =
                        "CommentContent=" +params[0]+
                                "&CommenterID=" + params[1]+
                                "&PostID"+params[2];
                byte[] postData       = urlParameters.getBytes( Charset.forName("UTF-8") );
                URL url = new URL( "http://192.168.1.5:6060/api/Account/CreateComment");
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
            return Integer.getInteger(params[2]);
        }

        @Override
        protected void onPostExecute(Integer id) {
            CommentApi commentApi=new CommentApi();
            commentApi.execute(postId);
        }
    }
}
