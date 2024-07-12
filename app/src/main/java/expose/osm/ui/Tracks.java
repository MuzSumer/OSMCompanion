package expose.osm.ui;

import static java.util.concurrent.TimeUnit.SECONDS;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.Locale;

import osm.expose.R;
import expose.model.DiagramUtil;
import expose.model.dialog.EditorProperties;
import expose.model.impl.DiagramExpose;
import expose.model.impl.DiagramStore;
import expose.model.meta.Command;
import expose.model.meta.Store;
import expose.model.meta.UniversalModel;

public class Tracks extends Fragment implements Command, TextToSpeech.OnInitListener {


    DiagramExpose expo;

    long interval;
    double trigger;




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // setup
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());


        String i = preferences.getString("interval", "10");
        interval = SECONDS.toMillis(Integer.parseInt(i));

        String s = preferences.getString("sensitivity", "10");
        trigger = 0.001 * Integer.parseInt(s);

        tts = new TextToSpeech(getContext(),this);
    }//onCreate




    TextToSpeech tts;
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.GERMAN);

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(getContext(), "error tts", Toast.LENGTH_LONG).show();
            }
        }
    }


    private void speak(String subject) {

        tts.speak(subject, TextToSpeech.QUEUE_FLUSH, null, null);

    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.diagram, container, false);



        expo = new DiagramExpose(getContext(), view.findViewById(R.id.diagram), view.findViewById(R.id.scroll));
        Store store = new DiagramStore(expo(), "tracks.xml");
        expo().createStore(store, "tracks.xml", "");


        registerActions(view);


        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        expo().getDiagram().setLayoutManager(manager);

        ModelAdapter adapter = new ModelAdapter(getContext());
        expo().getDiagram().setAdapter(adapter);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }





    public View.OnClickListener selectCell() {
        return cellSelect;
    }


    public View.OnClickListener openCell() {
        return cellOpen;
    }


    public View.OnClickListener editCell() {
        return cellEdit;
    }


    private View.OnClickListener cellSelect = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String modelId = view.getContentDescription().toString();

            expo().setFocus(modelId, false);

            UniversalModel m = expo().getStore().findModel(modelId);
            speak(m.getSubject());
        }
    };

    private View.OnClickListener cellEdit = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String id = view.getContentDescription().toString();
            expo().setSelected(id);

            expo().setFocus(id, false);

            UniversalModel model = expo().getStore().findModel(id);

            EditorProperties editor = new EditorProperties(expo(),null, null, model);
            editor.show(getChildFragmentManager(), "");
        }
    };

    private View.OnClickListener cellOpen = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String id = view.getContentDescription().toString();
            UniversalModel model = expo().getStore().findModel(id);


            if (!expo().getSelected().equals(id)) {

                expo().setFocus(id, false);


                speak(model.getSubject());

                return;
            }



            Intent intent = new Intent(getActivity(), Track.class);
            intent.putExtra("name", model.getSubject());
            intent.putExtra("folder", model.getContent());

            startActivity(intent);
        }
    };



    private void addTrack() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());


        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dv = inflater.inflate(R.layout.editor_location, null);


        builder.setView(dv);


        builder.setPositiveButton("OK", (dialog, which) -> {


            EditText edit_subject = dv.findViewById(R.id.set_subject);

            if (edit_subject.getText().toString().isEmpty()) return;


            EditText edit_folder = dv.findViewById(R.id.set_folder);
            if (edit_folder.getText().toString().isEmpty()) return;



            UniversalModel model = expo().getStore().createDefaultModel(expo().getStore().getNewId(), "subject");
            //model.setCoordinates(latitude + " / " + longitude);


            //String loc = AlocUtil.findReverseAddress(latitude, longitude);
            //model.setLocation(loc);

            model.setSubject(edit_subject.getText().toString());//loc.split(",")[0]);
            model.setTitle(edit_subject.getText().toString());//loc.split(",")[0]);


            model.setContent(edit_folder.getText().toString());

            //File home = getContext().getFilesDir();
            //File path = new File(home, model.getContent());

            expo().getStore().saveLocalModel(expo(), expo().getFolder());

            expo().setFocus(model.getId(), false);
            expo().scrollToEnd();

            speak(model.getSubject());
        });

        builder.setNegativeButton("cancel", (dialog, which) -> {
            dialog.cancel();
        });


        builder.show();
    }

    private void removeFiles(@NonNull File folder) {

        try {
            if (folder.isDirectory()) {

                int size = folder.listFiles().length;
                for (int p=size-1; p>-1; p--) {

                    File f = folder.listFiles()[p];
                    //String s = f.getAbsolutePath();

                    f.delete();
                }
            }

            folder.delete();

        } catch (Exception e) {
            e.printStackTrace();
        }


    }
    private void registerActions(View view) {
        view.findViewById(R.id.record_add).setOnClickListener(
                v -> {

                    addTrack();
                }
        );

        view.findViewById(R.id.record_remove).setOnClickListener(
                v -> {
                    //RemoveWithFolder editor = new RemoveWithFolder(this);
                    //editor.show(getChildFragmentManager(), "remov

                    removeModel("");
                }
        );

        view.findViewById(R.id.record_share).setOnClickListener(
                v -> {

                }
        );


    }


    public DiagramExpose expo() { return expo; }
    public void addModel(UniversalModel model) {}

    public void removeModel(String id) {

        UniversalModel model = expo().getSelectedModel();
        if (model != null) {

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

            LayoutInflater inflater = requireActivity().getLayoutInflater();
            View dv = inflater.inflate(R.layout.editor_confirm, null);

            builder.setView(dv);


            builder.setPositiveButton("OK", (dialog, which) -> {
                String folder = model.getContent();

                expo().getStore().removeModel(model.getId());
                expo().getStore().saveLocalModel(expo(), expo().getFolder());
                expo().setFocus("", false);


                File home = getContext().getFilesDir();
                File path = new File(home, folder);
                if (path.exists()) removeFiles(path);

                dialog.dismiss();
            });

            builder.setNegativeButton("cancel", (dialog, which) -> {

                    dialog.cancel();

            });


            builder.show();
        }
    }



    class ModelAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @SuppressLint("UseCompatLoadingForDrawables")
        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

            UniversalModel model = expo().getStore().getModelAt(position);
            String id = model.getId();

            DiagramExpose.UniversalModelViewHolder mv = (DiagramExpose.UniversalModelViewHolder) holder;


            mv.itemView.setContentDescription(id);
            mv.itemView.setOnClickListener(openCell());



            ConstraintLayout layout = mv.getLayout();
            layout.setContentDescription(id);


            // hovering
            /*
            mv.getLayout().setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    String id1 = v.getContentDescription().toString();

                    message(id);
                }
            });

            mv.getLayout().setOnHoverListener((view1, event) -> {

                String id1 = view1.getContentDescription().toString();


                switch (event.getAction()) {
                    case MotionEvent.ACTION_HOVER_ENTER:
                        message("enter " + id1);
                        break;
                    case MotionEvent.ACTION_HOVER_MOVE:
                        message("move " + id1);
                        break;
                    case MotionEvent.ACTION_HOVER_EXIT:
                        message("exit " + id1);
                        break;
                }
                return false;
            });
             */


            {
                if (id.equals(expo().getSelected())) {
                    mv.itemView.setBackground(AppCompatResources.getDrawable(getContext(), R.drawable.app_rbox_selected));
                    registerForContextMenu(mv.getImage());
                } else {
                    mv.itemView.setBackground(AppCompatResources.getDrawable(getContext(), R.drawable.app_rbox_background));
                    unregisterForContextMenu(mv.getImage());
                }
            }// selection


            {
                if (id.equals(expo().getSelected())) {
                    layout.post(() -> {
                        int l = layout.getMeasuredWidth()*3/5;
                        // select
                        Drawable d = getContext().getDrawable(R.drawable.app_dot_green);
                        int t = 23;
                        DiagramUtil.setDBounds(d, 16, l, t);

                        // edit
                        Drawable e = getContext().getDrawable(R.drawable.app_dot_blue);
                        t = 77;
                        DiagramUtil.setDBounds(e, 16, l, t);

                        // location
                        Drawable f = getContext().getDrawable(R.drawable.app_dot_red);
                        t = layout.getMeasuredHeight() - 23;
                        DiagramUtil.setDBounds(f, 16, l, t);


                        // image
                        Drawable g = getContext().getDrawable(R.drawable.app_dot_white);
                        l = mv.getImage().getMeasuredWidth()/2;
                        t = mv.itemView.getMeasuredHeight()/2;
                        DiagramUtil.setDBounds(g, 32, l, t);



                        layout.getOverlay().clear();
                        layout.getOverlay().add(d);
                        layout.getOverlay().add(e);
                        layout.getOverlay().add(f);
                        layout.getOverlay().add(g);
                    });
                } else {
                    layout.post(() -> {
                        int l = layout.getMeasuredWidth()*3/5;

                        Drawable d = getContext().getDrawable(R.drawable.app_dot_yellow);
                        int t = 23;
                        DiagramUtil.setDBounds(d, 16, t, l);


                        layout.getOverlay().clear();
                        layout.getOverlay().add(d);
                        //layout.getOverlay().add(e);
                    });
                }
            }// overlay



            // date
            mv.getDate().setText(model.getDate());
            mv.getDate().setContentDescription(id);
            mv.getDate().setOnClickListener(editCell());


            {
                try {
                    mv.getType().setText(model.getType());
                    mv.getState().setText(model.getState());
                } catch (Exception e) {
                    mv.getType().setText(model.getType());
                    mv.getState().setText(model.getState());
                }


                mv.getType().setContentDescription(id);
                mv.getState().setContentDescription(id);

                mv.getType().setOnClickListener(editCell());
                mv.getState().setOnClickListener(editCell());

            }// type, state


            {
                mv.getTitle().setText(model.getTitle());
                mv.getTitle().setContentDescription(id);
                //mv.getTitle().setOnClickListener(selectCell());
                mv.getTitle().addTextChangedListener(new TextWatcher() {

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        String id = mv.getTitle().getContentDescription().toString();
                        UniversalModel edit = expo().getStore().findModel(id);

                        edit.setTitle(DiagramUtil.trim(mv.getTitle()));
                        expo().getStore().saveLocalModel(expo(), expo().getFolder());
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });


                mv.getSubject().setText(model.getSubject());
                mv.getSubject().setContentDescription(id);
                //mv.getSubject().setOnClickListener(selectCell());
                mv.getSubject().addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        String id = mv.getTitle().getContentDescription().toString();
                        UniversalModel edit = expo().getStore().findModel(id);

                        edit.setSubject(DiagramUtil.trim(mv.getSubject()));
                        expo().getStore().saveLocalModel(expo(), expo().getFolder());
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });
            }// title, subject


            {
                mv.getContent().setText(model.getContent());
                mv.getSpecs().setText(model.getSpecs());
                mv.getTags().setText(model.getTags());
            }// content, specs, tags


            {
                mv.getOpenLocation().setContentDescription(id);
                //mv.getOpenLocation().setOnClickListener(openMap());

                //mv.getLocation().setText(shortLocation(model.getLocation(), 1));
                mv.getLocation().setContentDescription(id);


                /*
                String l = model.getLocation();
                if (!l.isEmpty()) {
                    String words[] = model.getLocation().split(",");
                    for (int p=0; p<6; p++) {
                        if (p < words.length) {
                            if (l == "") {
                                l = words[p];
                            } else {
                                    l = "," + words[p];
                                }
                            }
                        }
                    }

                }
                 */

                String location = model.getLocation();

                // shor location
                if (!location.isEmpty()) {

                    int max = 64;

                    String l = "";

                    String words[] = location.split(",");
                    for (int p=0; p<words.length; p++) {

                        String w = words[p];

                        if (l.isEmpty()) {
                            l = w;
                        } else {
                            if ((l.length() + w.length() + 1) < max) {
                                l = l + "," + w;
                            }
                        }


                    }
                    location = l;
                }
                mv.getLocation().setText(location);

                //mv.getLocation().setOnClickListener(wrongLocation());
            }// location


            {
                mv.getImage().setContentDescription(id);
                mv.getImage().setOnClickListener(editCell());

                expo().setImage(mv.getImage(), model.getSymbol(), getResources().getInteger(R.integer.cell_size_small));

            }// image
        }






        Context context;
        public ModelAdapter(Context classContext) {
            context = classContext;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return expo().createViewHolder(LayoutInflater.from(context).inflate(R.layout.diagram_topic, parent, false));
        }

        @Override
        public int getItemCount() {
            return expo().getStore().size();
        }

    }//ModelAdapter





    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);


        //menu.setHeaderTitle("move");
        //menu.setHeaderIcon(R.drawable.ic_error);


        menu.add(0, v.getId(), 0, "up");
        menu.add(0, v.getId(), 0, "down");

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        UniversalModel source = expo().getSelectedModel();
        String selected = source.getId();

        if (item.getTitle() == "up") {

            int p0 = findPosition(selected);

            int p1 = p0 - 1;

            if (p1 < 0) {
                return false;
            }


            UniversalModel older = expo().getStore().getModelAt(p1);
            expo().getStore().swapModel(source, older);

            expo().getStore().saveLocalModel(expo(), expo().getFolder());
            expo().setFocus(null, false);
        }

        if (item.getTitle() == "down") {

            int p0 = findPosition(selected);

            int p1 = p0 + 1;

            if (p1 > expo().getStore().size() - 1) {
                return false;
            }


            UniversalModel younger = expo().getStore().getModelAt(p1);
            expo().getStore().swapModel(source, younger);

            expo().getStore().saveLocalModel(expo(), expo().getFolder());
            expo().setFocus(null, false);
        }



        return true;
    }

    private int findPosition(String id) {
        int found = -1;

        for (int p = 0; p< expo().getStore().size(); p++) {
            UniversalModel model = expo().getStore().getModelAt(p);

            if (model.getId().equals(id)) {

                if (found < 0) { // find first
                    found = p;
                }
            }
        }

        return found;
    }
}