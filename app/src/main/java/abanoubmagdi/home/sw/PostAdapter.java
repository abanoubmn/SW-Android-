package abanoubmagdi.home.sw;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.facebook.common.executors.CallerThreadExecutor;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.Priority;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import java.util.ArrayList;

/**
 * Created by Home on 6/25/2017.
 */

class PostAdapter extends ArrayAdapter {

    private static ArrayList<Post> posts;
    private Context context;
    PostAdapter(Context context, ArrayList<Post> posts){
        super(context, R.layout.post_item, posts);
        this.context=context;
        this.posts=posts;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater= (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view;
        view= inflater.inflate(R.layout.post_item, null);
        TextView postContent = (TextView) view.findViewById(R.id.postcontent);
        TextView fullName=(TextView) view.findViewById(R.id.fullname);
        TextView dateCreated=(TextView) view.findViewById(R.id.datecreated);
        SimpleDraweeView pp=(SimpleDraweeView) view.findViewById(R.id.pp);

        postContent.setText(posts.get(position).postContent);
        fullName.setText(posts.get(position).fullName);
        dateCreated.setText(posts.get(position).dateCreated.toString("MM-DD"));

        Uri uri=Uri.parse(posts.get(position).imageUrl);
        pp.setImageURI(uri);

        ImagePipeline imagePipeline= Fresco.getImagePipeline();
        ImageRequest imageRequest= ImageRequestBuilder
                .newBuilderWithSource(uri)
                .setRequestPriority(Priority.HIGH)
                .setLowestPermittedRequestLevel(ImageRequest.RequestLevel.FULL_FETCH)
                .build();
        DataSource<CloseableReference<CloseableImage>> dataSource =
                imagePipeline.fetchDecodedImage(imageRequest, null);
        try {
            dataSource.subscribe(new BaseBitmapDataSubscriber() {
                @Override
                public void onNewResultImpl(@Nullable Bitmap bitmap) {
                    if (bitmap == null) {
                        Log.d("tag", "Bitmap data source returned success, but bitmap null.");
                        return;
                    }
                    // The bitmap provided to this method is only guaranteed to be around
                    // for the lifespan of this method. The image pipeline frees the
                    // bitmap's memory after this method has completed.
                    //
                    // This is fine when passing the bitmap to a system process as
                    // Android automatically creates a copy.
                    //
                    // If you need to keep the bitmap around, look into using a
                    // BaseDataSubscriber instead of a BaseBitmapDataSubscriber.
                }

                @Override
                public void onFailureImpl(DataSource dataSource) {
                    // No cleanup required here
                }
            }, CallerThreadExecutor.getInstance());
        } finally {
            if (dataSource != null) {
                dataSource.close();
            }
        }
//        Picasso.with(context).load(posts.get(position).imageUrl).placeholder(R.drawable.empty).into(pp);

        return view;
    }
}
