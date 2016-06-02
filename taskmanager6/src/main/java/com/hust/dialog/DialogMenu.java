package com.hust.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

/**
 * Created by laidong on 20/09/2015.
 */
public abstract class DialogMenu extends AlertDialog.Builder {
    String[] menu;

    public DialogMenu(Context context, String title, String[] menu) {
        super(context);
        this.menu = menu;
        DialogInterface.OnClickListener listener;
        setItems(menu, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onClickDialog(which);
            }
        });
        setTitle(title);
        show();

    }

    public abstract void onClickDialog(int position);

}
