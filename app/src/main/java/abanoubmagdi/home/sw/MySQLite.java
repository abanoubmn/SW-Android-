package abanoubmagdi.home.sw;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.joda.time.DateTime;

import java.util.ArrayList;

/**
 * Created by Home on 6/26/2017.
 */

public class MySQLite extends SQLiteOpenHelper {

    static private final String Database_Name ="SWDB";
    MySQLite(Context context){super(context, Database_Name, null,1);}

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE AccountInfo(" +
                "fullname text," +
                "username text," +
                "ID text," +
                "image text);");

        db.execSQL("create table HomePosts(" +
                "PostId INTEGER," +
                "fullname text," +
                "username text," +
                "content text," +
                "AccountID text," +
                "image text," +
                "DateCreated text," +
                "id INTEGER PRIMARY KEY AUTOINCREMENT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    void addHomePosts(ArrayList<Post> posts){
        SQLiteDatabase db=this.getWritableDatabase();
        db.execSQL("drop table HomePosts");
        db.execSQL("create table HomePosts(" +
                "PostId INTEGER," +
                "fullname text," +
                "username text," +
                "content text," +
                "AccountID text," +
                "image text," +
                "DateCreated text," +
                "id INTEGER PRIMARY KEY AUTOINCREMENT);");
        for(Post post : posts)
        {
            ContentValues contentValues=new ContentValues();
            contentValues.put("PostId",post.postId);
            contentValues.put("fullname",post.fullName);
            contentValues.put("username",post.userName);
            contentValues.put("content",post.postContent);
            contentValues.put("image",post.imageUrl);
            contentValues.put("AccountID",post.accountId);
            contentValues.put("DateCreated",post.dateCreated.toString());
            db.insert("HomePosts",null,contentValues);
            Log.e("sqlite",post.accountId);
        }
    }

    void addUserInfo(Account userInfo){
        SQLiteDatabase db=this.getWritableDatabase();
        db.execSQL("delete from AccountInfo");
        ContentValues contentValues=new ContentValues();
        contentValues.put("fullname",userInfo.fullName);
        contentValues.put("username",userInfo.userName);
        contentValues.put("image",userInfo.imageUrl);
        contentValues.put("ID",userInfo.accountId);
        db.insert("AccountInfo",null,contentValues);
    }

    ArrayList<Post> getHomePosts(){
        SQLiteDatabase db=this.getWritableDatabase();
        Cursor cursor=db.rawQuery("select * from HomePosts",null);
        ArrayList<Post> posts=new ArrayList<>();
        if (cursor.moveToFirst())
        {
            do {
                Post post=new Post();
                post.postId=cursor.getInt(0);
                post.fullName=cursor.getString(1);
                post.userName=cursor.getString(2);
                post.postContent =cursor.getString(3);
                post.accountId=cursor.getString(4);
                post.imageUrl=cursor.getString(5);
                post.dateCreated=new DateTime(cursor.getString(6));
                posts.add(post);
            }while (cursor.moveToNext());
        }
        cursor.close();
        return posts;
    }

    Post getPost(int id){
        SQLiteDatabase db=this.getWritableDatabase();
        Cursor cursor=db.rawQuery("select * from HomePosts where PostId ="+id,null);
        Post post=new Post();
        cursor.moveToFirst();

        post.postId=cursor.getInt(0);
        post.fullName=cursor.getString(1);
        post.userName=cursor.getString(2);
        post.postContent =cursor.getString(3);
        post.accountId=cursor.getString(4);
        post.imageUrl=cursor.getString(5);
        post.dateCreated=new DateTime(cursor.getString(6));
        cursor.close();
        return post;
    }

    Account getUserInfo(){
        SQLiteDatabase db=this.getWritableDatabase();
        Cursor cursor=db.rawQuery("select * from AccountInfo", null);
        Account userInfo=new Account();
        cursor.moveToFirst();
        userInfo.fullName=cursor.getString(0);
        userInfo.userName=cursor.getString(1);
        userInfo.accountId=cursor.getString(2);
        userInfo.imageUrl=cursor.getString(3);
        cursor.close();
        return userInfo;
    }

    ArrayList<Post> getProfilePosts(){
        SQLiteDatabase db=this.getWritableDatabase();
        Cursor cursor=db.rawQuery("select * from HomePosts where AccountID IN" +
                "(select ID from AccountInfo)",null);
        ArrayList<Post> posts=new ArrayList<>();
        if (cursor.moveToFirst())
        {
            do {
                Log.e("sqlprofile", cursor.getInt(0)+"");
                Post post=new Post();
                post.postId=cursor.getInt(0);
                post.fullName=cursor.getString(1);
                post.userName=cursor.getString(2);
                post.postContent =cursor.getString(3);
                post.accountId=cursor.getString(4);
                post.imageUrl=cursor.getString(5);
                post.dateCreated=new DateTime(cursor.getString(6));
                posts.add(post);
            }while (cursor.moveToNext());
        }
        cursor.close();
        return posts;
    }

    ArrayList<Post> getProfilePosts(String id){
        SQLiteDatabase db=this.getWritableDatabase();
        Cursor cursor=db.rawQuery("select * from HomePosts where AccountID ="+
                "'"+id+"'",null);
        ArrayList<Post> posts=new ArrayList<>();
        if (cursor.moveToFirst())
        {
            do {
                Log.e("sqlprofile", cursor.getInt(0)+"");
                Post post=new Post();
                post.postId=cursor.getInt(0);
                post.fullName=cursor.getString(1);
                post.userName=cursor.getString(2);
                post.postContent =cursor.getString(3);
                post.accountId=cursor.getString(4);
                post.imageUrl=cursor.getString(5);
                post.dateCreated=new DateTime(cursor.getString(6));
                posts.add(post);
            }while (cursor.moveToNext());
        }
        cursor.close();
        return posts;
    }
}
