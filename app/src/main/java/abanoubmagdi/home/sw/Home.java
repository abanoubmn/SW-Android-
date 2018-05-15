package abanoubmagdi.home.sw;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.ls.LSException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class Home extends MainActivity {

    private ListView posts;
    private static String dataString;
    MySQLite mySQLite=new MySQLite(Home.this);
    private ArrayList<Post> postArrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fresco.initialize(this);
        setContentView(R.layout.activity_home);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(Home.this,CreatePostActivity.class);
                startActivity(intent);
            }
        });
        posts=(ListView) findViewById(R.id.homeposts);

        AlertDialog.Builder builder =new AlertDialog.Builder(this);
        final AlertDialog alertDialog=builder.create();
        final View commentDialog=getLayoutInflater().inflate(R.layout.comment_dialog,null);
        alertDialog.setView(commentDialog);

        posts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                Intent intent=new Intent(Home.this, PostActivity.class);
                intent.putExtra("postId", postArrayList.get(position).postId);
                startActivity(intent);
            }
        });

        posts.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Button edit=(Button) commentDialog.findViewById(R.id.editBtn);
                Button delete=(Button) commentDialog.findViewById(R.id.deleteBtn);
                Post post =mySQLite.getPost(postArrayList.get(position).postId);
                Account account=mySQLite.getUserInfo();
                if (post.accountId.equals(account.accountId)){
                    edit.setClickable(false);
                    delete.setClickable(false);
                }
                alertDialog.show();
                return true;
            }
        });
        try {

            Account accountInfo=mySQLite.getUserInfo();
            if (mySQLite.getHomePosts()!= null){
                PostAdapter postAdapter=new PostAdapter(this, mySQLite.getHomePosts());
                posts.setAdapter(postAdapter);
            }
            ConnectivityManager cm=(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo=cm.getActiveNetworkInfo();
            if (networkInfo !=null && networkInfo.isConnectedOrConnecting()){
                HomeApi swApi=new HomeApi();
                swApi.execute(accountInfo.accountId);
            }
        }
        catch (Exception e){
            Log.e("home",e.getMessage());
            Intent intent=new Intent(Home.this,Login.class);
            startActivity(intent);
            finish();
        }

        posts.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                return false;
            }
        });
    }

    public void PPClick(View view)
    {
        ListView listView=(ListView) ((RelativeLayout) view.getParent()).getParent();
        int i =listView.getPositionForView((RelativeLayout) view.getParent());
        Toast.makeText(this,i+"",Toast.LENGTH_SHORT).show();
        String loggedIn=mySQLite.getUserInfo().accountId;
        String clicked=postArrayList.get(i).accountId;
        if (clicked.equals(loggedIn))
        {
            Intent intent=new Intent(this,ProfileActivity.class);
            startActivity(intent);
        }
        else {
            Intent intent=new Intent(this,OtherProfilesActivity.class);
            intent.putExtra("accountId",clicked);
            startActivity(intent);
        }
    }

    class HomeApi extends AsyncTask<String, Void, ArrayList<Post>>
    {

        @Override
        protected ArrayList<Post> doInBackground(String... params) {
            try {
                URL url = new URL("http://192.168.1.7:6060/" +
                        "api/Account/GetPosts/"+params[0]);
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

        private ArrayList<Post> getDataFromJSON(String dataString)
        {

            try {
                JSONArray jsonArray=new JSONArray(dataString);
                for (int i=0; i<51; i++){
                    Post post=new Post();
                    final JSONObject jsonObject= jsonArray.getJSONObject(i);
                    post.postContent =jsonObject.getString("PostContent");
                    post.fullName=jsonObject.getString("FullName");
                    post.userName=jsonObject.getString("UserName");
                    post.dateCreated=new DateTime(jsonObject.getString("DateCreated"));
                    post.postId=jsonObject.getInt("PostID");
                    post.accountId=jsonObject.getString("AccountID");
                    String imageUrl=jsonObject.getString("ImageURL");
                    if (!imageUrl.equals(""))
                        post.imageUrl="http://192.168.1.5:6060" +imageUrl;
                    else
                        post.imageUrl=null;
                    postArrayList.add(post);
                    Log.e("PostContent", post.imageUrl);
                }
            }
            catch (Exception e){
                Log.e("jsonException",e.getMessage());
            }
            return postArrayList;
        }

        @Override
        protected void onPostExecute(ArrayList<Post> postArrayList) {
            PostAdapter postAdapter = new PostAdapter(Home.this, postArrayList);
            posts.setAdapter(postAdapter);
            mySQLite.addHomePosts(postArrayList);
        }

        @Override
        protected void onCancelled() {
            Toast.makeText(Home.this,"Connection Error", Toast.LENGTH_SHORT).show();
        }
    }
}
