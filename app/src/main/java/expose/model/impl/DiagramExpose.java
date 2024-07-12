package expose.model.impl;

import static android.content.Context.NOTIFICATION_SERVICE;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;

import osm.expose.R;
import expose.model.meta.Cell;
import expose.model.meta.Diagram;
import expose.model.meta.Store;
import expose.model.meta.UniversalModel;

public class DiagramExpose implements Diagram {

    Context context;
    RecyclerView diagram;
    NestedScrollView scroll;

    String folder = "";
    String namespace = "";

    Store store = null;
    public DiagramExpose(Context set_context, RecyclerView set_diagram, NestedScrollView set_scroll) {
        context = set_context;
        diagram = set_diagram;
        scroll = set_scroll;
    }



    public Context getContext() { return context; }
    public RecyclerView getDiagram() { return diagram; }

    public String getFolder() { return folder; }
    public void setFolder(String value) { folder = value; }
    public String getNamespace() { return namespace; }

    public Store getStore() { return store; }


    public boolean createStore(Store set_store, String set_namespace, String set_folder) {

        store = set_store;
        namespace = set_namespace;
        setFolder(set_folder);

        getStore().loadLocalModel(this, getFolder());

        return true;
    }


    String selected = "";
    public String getSelected() { return selected; }
    public void setSelected(String value) { selected = value; }


    public void setFocus(String id, boolean expand) {
        setSelected(id);
        redraw(false);
    }


    public void redraw(boolean full_redraw) {

        diagram.getAdapter().notifyDataSetChanged();

        if (full_redraw) {
            scrollToEnd();
        }
    }


    public UniversalModel getRootModel() {
        UniversalModel model = getStore().findModel(getStore().getRootId());
        return model;
    }
    public UniversalModel getSelectedModel() {
        if (getStore() == null) return null;
        return getStore().findModel(getSelected());
    }



    public Cell findCell(UniversalModel model) { return null; }
    public Cell addCell(UniversalModel model) {
        return null;
    }
    public void removeCell(UniversalModel model) {

    }




    public void scrollToEnd() {
        scroll.post(new Runnable() {
            @Override
            public void run() {
                scroll.smoothScrollTo(0, diagram.getMeasuredHeight());
            }
        });
    }
    public void scrollToTop() {
        scroll.smoothScrollTo(0, 0);
    }
    public void scrollBy(int x, int y) {
        scroll.smoothScrollBy(x, y);
    }





    public RecyclerView.ViewHolder createViewHolder(View itemView) {
        return new UniversalModelViewHolder(itemView);
    }

    public class UniversalModelViewHolder extends RecyclerView.ViewHolder {

        ConstraintLayout layout;
        public ConstraintLayout getLayout() { return layout; }

        TextView date, type, state;
        public TextView getDate() { return date; }
        public TextView getType() { return type; }
        public TextView getState() { return state; }

        public TextView title, subject;
        public TextView getTitle() { return title; }
        public TextView getSubject() { return subject; }


        TextView content, specs, tags;
        public TextView getContent() { return content; }
        public TextView getSpecs() { return specs; }
        public TextView getTags() { return tags; }


        TextView location;
        public TextView getLocation() { return location; }


        ImageView image;
        public ImageView getImage() { return image; }


        ImageView openLocation;
        public ImageView getOpenLocation() { return openLocation; }


        public UniversalModelViewHolder(@NonNull View itemView) {
            super(itemView);

            layout = itemView.findViewById(R.id.layout);
            image = itemView.findViewById(R.id.item_image);


            date = itemView.findViewById(R.id.item_date);
            type = itemView.findViewById(R.id.item_type);
            state = itemView.findViewById(R.id.item_state);

            title = itemView.findViewById(R.id.item_title);//quickedit_title
            subject = itemView.findViewById(R.id.item_subject);

            content = itemView.findViewById(R.id.item_content);
            specs = itemView.findViewById(R.id.item_specs);
            tags = itemView.findViewById(R.id.item_tags);

            location = itemView.findViewById(R.id.item_location);
            openLocation = itemView.findViewById(R.id.open_location);
        }
    }


















    @SuppressLint("NotificationPermission")
    public void displayBusy(boolean value) {
        NotificationManager manager = (NotificationManager) getContext().getSystemService(NOTIFICATION_SERVICE);

        if (value) {
            Notification notify = new Notification.Builder(getContext())
                    .setContentTitle(context.getString(R.string.app_name))
                    .setContentText("busy")
                    .setSmallIcon(R.drawable.app_progress_notification)
                    .build();


            notify.flags |= Notification.FLAG_AUTO_CANCEL;
            manager.notify(0, notify);
        } else {
            manager.cancel(0);
        }
    }



    public void setImage(ImageView image, String image_path, int size) {

        if (image_path.isEmpty()) {
            //image.setImageDrawable(getContext().getDrawable(R.drawable.ic_launcher_foreground));
            return;
        }

        File home = new File(image.getContext().getFilesDir(), getFolder());
        File symbol = new File(home, image_path);

        int iw, ih;

        if (symbol.exists()) {
            try {
                BitmapDrawable drawable = new BitmapDrawable(symbol.getAbsolutePath());

                int w = drawable.getIntrinsicWidth();
                int h = drawable.getIntrinsicHeight();

                if (w > h) { // landscape
                    ih = size;
                    iw = size * h/w;
                } else {
                    ih = size;
                    iw = size * w/h;
                }

                Bitmap scaled = Bitmap.createScaledBitmap(drawable.getBitmap(), iw, ih, false);

                image.setMinimumWidth(iw);
                image.setMaxWidth(iw);
                image.setMinimumHeight(ih);
                image.setMaxHeight(ih);

                image.setImageBitmap(scaled);

                //getImage().setImageURI();
            } catch (Exception e) {
                // expected
            }
        }
    }



    public void beep() {
        ToneGenerator beep = new ToneGenerator(AudioManager.FLAG_PLAY_SOUND, 80);
        beep.startTone(ToneGenerator.TONE_CDMA_KEYPAD_VOLUME_KEY_LITE, 400);
    }


    public void protocol(String value) {}


    @SuppressLint("NotificationPermission")
    public void notify(boolean value, String title, String subject) {
        NotificationManager manager = (NotificationManager) getContext().getSystemService(NOTIFICATION_SERVICE);

        if (value) {
            Notification notify = new Notification.Builder(getContext())
                    .setContentTitle(title)
                    .setContentText(subject)
                    .setSmallIcon(R.drawable.app_progress_notification)
                    .build();


            notify.flags |= Notification.FLAG_AUTO_CANCEL;
            manager.notify(0, notify);
        } else {
            manager.cancel(0);
        }
    }


}
