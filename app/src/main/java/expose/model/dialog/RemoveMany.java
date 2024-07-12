package expose.model.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;


import expose.model.meta.Diagram;
import expose.model.meta.UniversalModel;
import osm.expose.R;


public class RemoveMany extends DialogFragment {

    Diagram diagram;


    LinearLayout selection;
    boolean mode;

    public RemoveMany(Diagram set_diagram) {
        diagram = set_diagram;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());



        LayoutInflater inflater = requireActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.selection_multi, null);



        builder.setView(view);



        selection = view.findViewById(R.id.dialog_selection);

        CheckBox m = view.findViewById(R.id.dialog_mode);
        mode = m.isActivated();



        m.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mode = isChecked;
            refreshSelection(diagram, mode);
        });


        for (UniversalModel model : diagram.getStore().getModels()) {
            CheckBox checkbox = new CheckBox(getContext());
            checkbox.setTextColor(Color.LTGRAY);
            checkbox.setContentDescription(model.getId());

            selection.addView(checkbox);
        }

        refreshSelection(diagram, mode);


        String cancel = getContext().getString(R.string.dialog_cancel);
        builder.setNegativeButton(cancel, (dialog, which) -> dialog.cancel());

        String okay = getContext().getString(R.string.dialog_add_confirm);
        builder.setPositiveButton(okay, (dialog, which) -> {

            dialog.dismiss();

            for (int cb=0; cb<selection.getChildCount(); cb++) {

                CheckBox checkbox = (CheckBox) selection.getChildAt(cb);

                if (checkbox.isChecked()) {
                    String id = checkbox.getContentDescription().toString();
                    UniversalModel model = diagram.getStore().findModel(id);
                    String folder = model.getContent();

                    diagram.getStore().removeModel(id);

                }
            }

            diagram.getStore().saveLocalModel(diagram, diagram.getFolder());

            diagram.setFocus("", false);
            diagram.redraw(true);
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
    }


    private void refreshSelection(Diagram diagram, boolean mode) {

        for (int p=0; p<selection.getChildCount(); p++) {
            CheckBox checkbox = (CheckBox) selection.getChildAt(p);

            UniversalModel model = diagram.getStore().getModelAt(p);

            String s = "";

            if (!mode) s = model.getSubject();
            if (s.isEmpty()) s = model.getTitle();
            if (s.isEmpty()) s = model.getId();

            checkbox.setText(s);

        }
    }



}
