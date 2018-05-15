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
 * Created by Home on 6/28/2017.
 */

public class CommentAdapter extends ArrayAdapter<Comment> {

    private static ArrayList<Comment> comments;
    private Context context;
    CommentAdapter(Context context, ArrayList<Comment> comments){
        super(context, R.layout.comment_item, comments);
        this.context=context;
        this.comments=comments;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater= (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view;
        view= inflater.inflate(R.layout.comment_item, null);
        TextView commentContent = (TextView) view.findViewById(R.id.commentContent);
        TextView fullName=(TextView) view.findViewById(R.id.commenter_fullname);
        SimpleDraweeView pp=(SimpleDraweeView) view.findViewById(R.id.commenter_pp);

        commentContent.setText(comments.get(position).commentContent);
        fullName.setText(comments.get(position).fullName);

        Uri uri=Uri.parse(comments.get(position).imageUrl);
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

