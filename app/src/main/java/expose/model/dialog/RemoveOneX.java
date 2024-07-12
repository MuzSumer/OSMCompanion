package expose.model.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.io.File;
import java.util.ArrayList;

import osm.expose.R;
import expose.model.meta.Command;
import expose.model.meta.UniversalModel;

public class RemoveOneX extends DialogFragment {

    Command diagram;
    public RemoveOneX(Command set_diagram) {
        diagram = set_diagram;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());



        LayoutInflater inflater = requireActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.selection_single, null);



        builder.setView(view);


        TextView notice = view.findViewById(R.id.dialog_notice);
        notice.setText(getContext().getString(R.string.record_delete_warning));

        Spinner spinner = view.findViewById(R.id.dialog_selection);


        ArrayList<String> subjects = new ArrayList<>();

        // add all store subjects
        for (UniversalModel model:diagram.expo().getStore().getModels()) {


            String s = model.getSubject();
            if (s.isEmpty()) s = model.getTitle();
            if (s.isEmpty()) s = model.getId();

            subjects.add(s);
        }


        SpinnerAdapter adapter = new ArrayAdapter<>(diagram.expo().getContext(), R.layout.editor_item, subjects);
        spinner.setAdapter(adapter);



        String cancel = getContext().getString(R.string.dialog_cancel);
        builder.setNegativeButton(cancel, (dialog, which) -> dialog.cancel());

        String okay = getContext().getString(R.string.dialog_add_confirm);
        builder.setPositiveButton(okay, (dialog, which) -> {

            dialog.dismiss();


            int index = spinner.getSelectedItemPosition();

            UniversalModel model = diagram.expo().getStore().getModelAt(index);

            diagram.expo().getStore().removeModelPosition(index);
            diagram.expo().getStore().saveLocalModel(diagram.expo(), diagram.expo().getFolder());


            File home = getContext().getFilesDir();
            File path = new File(home, model.getContent());

            diagram.removeModel(model.getId());

            if (path.exists()) removeFiles(path);

            diagram.expo().setFocus("", false);
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


    private void removeFiles(@NonNull File folder) {


        if (folder.isDirectory()) {

            int size = folder.listFiles().length;
            for (int p=size-1; p>-1; p--) {

                File f = folder.listFiles()[p];
                //String s = f.getAbsolutePath();

                f.delete();
            }
        }


        folder.delete();
    }
}
