/*
    This file is part of windvolt.

    created 2020 by Max Sumer
*/
package expose.model.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.fragment.app.DialogFragment;

import java.io.File;
import java.util.ArrayList;


import expose.model.DiagramUtil;
import expose.model.impl.DiagramModel;
import expose.model.meta.Diagram;
import expose.model.meta.UniversalModel;
import osm.expose.R;

public class EditorProperties extends DialogFragment {

    Diagram diagram;

    UniversalModel model;

    ArrayList<String> types, states;



    public EditorProperties(Diagram set_diagram, ArrayList<String> set_types, ArrayList<String> set_states, UniversalModel set_model) {
        diagram = set_diagram;
        types = set_types;
        states = set_states;
        model = set_model;
    }


    EditText edit_id, edit_symbol, edit_title, edit_subject, edit_content, edit_tags, edit_targets, edit_specs;
    Spinner edit_type, edit_state;

    TextView display_id, display_date;

    ImageView image;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());




        LayoutInflater inflater = requireActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.editor_properties, null);




        builder.setView(view);
        {
            edit_id = view.findViewById(R.id.edit_id);
            display_id = view.findViewById(R.id.display_id);
            display_date = view.findViewById(R.id.display_date);

            edit_title = view.findViewById(R.id.edit_title);
            edit_subject = view.findViewById(R.id.edit_subject);
            edit_symbol = view.findViewById(R.id.edit_symbol);
            edit_content = view.findViewById(R.id.edit_content);
            edit_tags = view.findViewById(R.id.edit_tags);
            edit_targets = view.findViewById(R.id.edit_targets);

            edit_type = view.findViewById(R.id.edit_type);
            edit_state = view.findViewById(R.id.edit_state);

            edit_specs = view.findViewById(R.id.edit_specs);
            image = view.findViewById(R.id.symbol_view);

            // display location
            TextView loc = view.findViewById(R.id.display_location);
            //loc.setText(model.getLocation() + ": " +diagram.shortCoordinates2(model.getCoordinates()));


            TextView set_id = view.findViewById(R.id.set_id);
            {
                set_id.setOnClickListener(id -> {
                    AlertDialog.Builder id_dialog = new AlertDialog.Builder(
                            new ContextThemeWrapper(getContext(), R.style.Theme_SystemDialog));

                    String value = getString(R.string.model_id);
                    id_dialog.setTitle(value);

                    final EditText input = new EditText(getContext());
                    input.setText(edit_id.getText().toString());

                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    id_dialog.setView(input);

                    String okay = diagram.getContext().getString(R.string.dialog_add_confirm);
                    id_dialog.setPositiveButton(okay, (dialog, which) -> {
                        dialog.dismiss();

                        edit_id.setText(input.getText().toString());
                        display_id.setText(input.getText().toString());
                    });

                    String cancel = diagram.getContext().getString(R.string.dialog_cancel);
                    id_dialog.setNegativeButton(cancel, (dialog, which) -> dialog.cancel());

                    id_dialog.show();
                });
            }

            TextView set_symbol = view.findViewById(R.id.set_symbol);
            set_symbol.setOnClickListener(symbol -> {


                try {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(new ContextThemeWrapper(getContext(), R.style.Theme_SystemDialog));

                    String value1 = getString(R.string.model_symbol);
                    builder1.setTitle(value1);

                    final EditText input = new EditText(getContext());
                    input.setText(edit_symbol.getText().toString());

                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    builder1.setView(input);


                    builder1.setPositiveButton("OK", (dialog, which) -> {
                        dialog.dismiss();
                        edit_symbol.setText(input.getText().toString());
                    });
                    builder1.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

                    builder1.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            });


        }// fields

        registerHelpers(view);


        //edit_type.setAdapter(new ArrayAdapter<>(getContext(), R.layout.editor_item, types));
        //edit_state.setAdapter(new ArrayAdapter<>(getContext(), R.layout.editor_item, states));


        image.setOnClickListener(select_image -> {
            //
        });





        boolean create = model == null;
        String new_id = diagram.getStore().getNewId();


        {
            if (create) {

                edit_id.setText(new_id);
                display_id.setText(new_id);


                display_date.setText(diagram.getStore().today());

                edit_targets.setText("");

                edit_symbol.setText("");
                edit_title.setText("");
                edit_subject.setText("");

                edit_content.setText("");
                edit_tags.setText("");

                edit_specs.setText("");

                edit_type.setSelection(0);
                edit_state.setSelection(0);

            } else {

                edit_id.setText(model.getId());
                display_id.setText(model.getId());
                display_date.setText(model.getDate());

                edit_targets.setText(model.getTargets());

                edit_symbol.setText(model.getSymbol());
                edit_title.setText(model.getTitle());
                edit_subject.setText(model.getSubject());

                edit_content.setText(model.getContent());
                edit_tags.setText(model.getTags());

                edit_specs.setText(model.getSpecs());

                //setSpinText(edit_type, model.getType());
                //setSpinText(edit_state, model.getState());

                try {
                    int t = Integer.parseInt(model.getType());
                    if (t > types.size()-1) t = 0;

                    edit_type.setSelection(t);

                    int s = Integer.parseInt(model.getState());
                    if (s > states.size() - 1) s = 0;

                    edit_state.setSelection(s);

                } catch (Exception e) {}


            }

            drawImage(image);
        }
        // preset attributes





        String okay = diagram.getContext().getString(R.string.dialog_add_confirm);
        builder.setPositiveButton(okay, (dialog, id) -> {

            if (create) {
                model = new DiagramModel();
                model.setId(new_id);

                diagram.getStore().addModel(model);


                UniversalModel source = diagram.getSelectedModel();
                if (source != null) {
                    //diagram.getStore().makeTarget(source, model);
                }

                diagram.addCell(model);
            }

            model.setId(edit_id.getText().toString());

            model.setSymbol(edit_symbol.getText().toString());
            model.setTitle(DiagramUtil.trim(edit_title));
            model.setSubject(DiagramUtil.trim(edit_subject));
            model.setContent(DiagramUtil.trim(edit_content));
            model.setTargets(DiagramUtil.trim(edit_targets));
            model.setTags(DiagramUtil.trim(edit_tags));

            model.setSpecs(DiagramUtil.trim(edit_specs));


            //model.setDate(diagram.trim(display_date));

            // TODO type state
            /*
            int t_index = types.indexOf(edit_type.getSelectedItem().toString());
            model.setType(Integer.toString(t_index));

            int s_index = states.indexOf(edit_state.getSelectedItem().toString());
            model.setState(Integer.toString(s_index));
             */



            diagram.getStore().saveLocalModel(diagram, model.getTargets());


            // redraw diagram
            diagram.setFocus(model.getId(), false);
        });


        String cancel = diagram.getContext().getString(R.string.dialog_cancel);
        builder.setNegativeButton(cancel, (dialog, id) -> {
            model = null;
            dialog.cancel();
        });

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(dialog1 -> {
            Button negativeButton = ((AlertDialog) dialog1).getButton(DialogInterface.BUTTON_NEGATIVE);
            Button positiveButton = ((AlertDialog) dialog1).getButton(DialogInterface.BUTTON_POSITIVE);

            negativeButton.setTextColor(Color.RED);
            positiveButton.setTextColor(Color.GREEN);

            negativeButton.invalidate();
            positiveButton.invalidate();
        });


        return dialog;
    }//builder




    private void drawImage(ImageView image) {
        if (model == null) return;

        String symbol = model.getSymbol();
        String folder = diagram.getFolder();

        if (symbol.isEmpty()) {
            symbol = model.getContent();
        }
        if (symbol.isEmpty()) return;

        int size = getContext().getResources().getInteger(R.integer.cell_size_small);

        try {
            File home = new File(getContext().getFilesDir(), folder);
            File pic = new File(home, symbol);

            Bitmap bitmap = BitmapFactory.decodeFile(pic.toString());

            int w = bitmap.getWidth();
            int h = bitmap.getHeight();

            int iw, ih;

            if (w > h) { // landscape
                iw = size;
                ih = size * h / w;
            } else {
                ih = size;
                iw = size * w / h;
            }

            image.setMinimumWidth(iw);
            image.setMinimumHeight(iw);
            image.setMaxWidth(ih);
            image.setMaxHeight(ih);

            Bitmap scaled = Bitmap.createScaledBitmap(bitmap, iw, ih, false);
            image.setImageBitmap(scaled);
        } catch (Exception e) {
            // expected
        }

    }




    /* --------------------------------windvolt-------------------------------- */


    private void registerHelpers(View view) {
        view.findViewById(R.id.record_title).setOnClickListener(v -> edit_title.setText(""));
        view.findViewById(R.id.record_subject).setOnClickListener(v -> edit_subject.setText(""));

        view.findViewById(R.id.record_content).setOnClickListener(v -> edit_content.setText(""));
        view.findViewById(R.id.record_specs).setOnClickListener(v -> edit_specs.setText(""));
        view.findViewById(R.id.record_tags).setOnClickListener(v -> edit_tags.setText(""));

        view.findViewById(R.id.record_type).setOnClickListener(v -> edit_type.setSelection(0));
        view.findViewById(R.id.record_state).setOnClickListener(v -> edit_state.setSelection(0));
    }


    private void setSpinText(Spinner spin, String text) {
        for(int i=0; i<spin.getAdapter().getCount(); i++)
        {
            if(spin.getAdapter().getItem(i).toString().contains(text)) {
                spin.setSelection(i);
            }
        }

    }


    private void closeKeyboard(View view) {
        if (view != null) {
            InputMethodManager manager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }//closeKeyboard

}