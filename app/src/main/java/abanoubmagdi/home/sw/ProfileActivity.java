package abanoubmagdi.home.sw;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

public class ProfileActivity extends MainActivity {

    private ListView posts;
    MySQLite mySQLite=new MySQLite(ProfileActivity.this);
    private ArrayList<Post> postArrayList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(ProfileActivity.this,CreatePostActivity.class);
                startActivity(intent);
            }
        });
        posts=(ListView) findViewById(R.id.profileposts);
        postArrayList=mySQLite.getProfilePosts();
        posts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                Intent intent=new Intent(ProfileActivity.this, PostActivity.class);
                intent.putExtra("postId", postArrayList.get(position).postId);
                startActivity(intent);
            }
        });
        PostAdapter postAdapter=new PostAdapter(this, postArrayList);
        posts.setAdapter(postAdapter);
    }

    public void PPClick(View view)
    {

    }
}
