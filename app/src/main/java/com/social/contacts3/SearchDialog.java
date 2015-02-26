package com.social.contacts3;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Created by rinat on 07.06.14.
 */
public class SearchDialog extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.search_dialog, null);
        final EditText etSearchDialog = (EditText)view.findViewById(R.id.etSearchDialog);

        ImageView ivSearchLabel = (ImageView)view.findViewById(R.id.ivSearchLabel);
        TextView tvSearchLabel = (TextView)view.findViewById(R.id.tvSearchLabel);
        if (MainActivity.curStatus == null){
            tvSearchLabel.setText(R.string.tvSaerchLabelAll);
            ivSearchLabel.setImageResource(R.drawable.ic_mode_all_dark);
        }
        else {
            if (MainActivity.curStatus[0] == "1"){
                tvSearchLabel.setText(R.string.tvSaerchLabelActiv);
                ivSearchLabel.setImageResource(R.drawable.ic_mode_active_dark);
            }
            if (MainActivity.curStatus[0] == "0"){
                tvSearchLabel.setText(R.string.tvSaerchLabelArchiv);
                ivSearchLabel.setImageResource(R.drawable.ic_mode_deactive_dark);
            }
        }



        builder.setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (!etSearchDialog.getText().toString().isEmpty()){

                            MainActivity.searchStr = etSearchDialog.getText().toString();

                            if (MainActivity.curStatus == null) {
                                MainActivity.whereStatus = Person.TABLE_NAME+" MATCH ?";
                            }
                            else {
                                MainActivity.whereStatus = Person.TABLE_NAME+" MATCH ? AND "+Person.COL_STATUS+" = "+MainActivity.curStatus[0];
                            }

                            MainActivity.curStatus = new String[] {etSearchDialog.getText().toString()+"*"};

                            //MainActivity.prevItem = null;

                            MainActivity.cursor =
                                    DatabaseHandler.getInstance(getActivity()).getCurCursor();

                            MainActivity.moveWithSave(getActivity(),0);

                            MainActivity.showAllExceptCancelSearch(MainActivity.mainMenu,false);
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                })
                .setTitle(R.string.search_title);
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
