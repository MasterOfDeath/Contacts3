package com.social.contacts3;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.os.Environment;
import android.widget.Toast;

import com.lamerman.FileDialog;
import com.lamerman.SelectionMode;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;


public class MainActivity extends Activity {

    //public static String whereStatus = Person.COL_STATUS+" IS ?"; //Поумолчанию сортировка по статусу включена
    public static String whereStatus = Person.COL_STATUS+" MATCH ?"; //Поумолчанию сортировка по статусу включена
    public static String[] curStatus = {"1"}; //Поумолчанию отображать только активных
    public static String[]  statusBeforeSearch = {"1"};
    public static String[] importData = {""}; //Дата выгрузки базы
    public static Long idItem = -1L;
    public static int prevPos = 0;
    public static String searchStr = null;

    public static Cursor cursor;
    private static TextView tvRowsCount;
    public static Person mItem;
    public static Person prevItem;
    private MenuItem action_mode = null;
    public static Menu mainMenu;
    private int posBeforeSearch = 0;

    //UI
    private static EditText textFIO;
    private static EditText textParents;
    private static EditText textMAddr;
    private static EditText textInfo;
    private static EditText textDolTen;
    private static EditText textNextDate;
    private static EditText textLastDate;
    private static CheckBox chBoxStatus;
    private static Button btnUpdate;
    private static TextView textLog;
    /////////


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textFIO = (EditText) findViewById(R.id.textFIO);
        textParents = (EditText) findViewById(R.id.textParents);
        textMAddr = (EditText) findViewById(R.id.textMAddr);
        textInfo = (EditText) findViewById(R.id.textInfo);
        textDolTen = (EditText) findViewById(R.id.textDolTen);
        textNextDate = (EditText) findViewById(R.id.textNextDate);
        textLastDate = (EditText) findViewById(R.id.textLastDate);
        chBoxStatus = (CheckBox) findViewById(R.id.chBoxStatus);
        btnUpdate = (Button) findViewById(R.id.btnUpdate);
        textLog = (TextView) findViewById(R.id.textVLog);

        TextWatcher inputTW = new TextWatcher() {
            public void afterTextChanged(Editable s) {

            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after){

            }
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                prevItem.fio = textFIO.getText().toString();
                prevItem.parents = textParents.getText().toString();
                prevItem.m_addr = textMAddr.getText().toString();
                prevItem.info = textInfo.getText().toString();
                prevItem.dolten = textDolTen.getText().toString();
                prevItem.nextdate = textNextDate.getText().toString();
                prevItem.lastdate = textLastDate.getText().toString();
            }
        };

        textFIO.addTextChangedListener(inputTW);
        textParents.addTextChangedListener(inputTW);
        textMAddr.addTextChangedListener(inputTW);
        textInfo.addTextChangedListener(inputTW);
        textDolTen.addTextChangedListener(inputTW);
        textNextDate.addTextChangedListener(inputTW);
        textLastDate.addTextChangedListener(inputTW);

        chBoxStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox ch = (CheckBox) v;
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage((ch.isChecked()) ? R.string.activate_contact : R.string.deactivate_contact)
                        .setPositiveButton(android.R.string.ok, dialogActivateClickListener)
                        .setNegativeButton(android.R.string.cancel, dialogActivateClickListener)
                        .show();
            }
        });

        chBoxStatus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                if (!isChecked) {
                    chBoxStatus.setText(R.string.chBoxStatusTextDeactive);
                    prevItem.status = 0;
                }

                if (isChecked) {
                    chBoxStatus.setText(R.string.chBoxStatusTextActive);
                    prevItem.status = 1;
                }
            }
        });

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Do something in response to button click
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(R.string.drag_date)
                        .setPositiveButton(android.R.string.ok, dialogDragClickListener)
                        .setNegativeButton(android.R.string.cancel, dialogDragClickListener)
                        .show();
            }
        });

        cursor = DatabaseHandler.getInstance(this).getCurCursor();

        if (cursor.getCount() > 0){
            //cursor.moveToFirst();
            cursor.moveToPosition(prevPos);
            idItem = cursor.getLong(0);
            mItem = DatabaseHandler.getInstance(getBaseContext()).getPerson(idItem);

            enableUI();

            prevItem = copyPerson(mItem);

            updateUIFromPerson(mItem);
        }
        else {
            prevItem = new Person();
            prevItem.id = 1L;

            disableUI();
        }

        //Достаем и отображаем дату выгрузки базы
        DatabaseHandler.getInstance(this).getLastPos(new int[1],new String[1],importData);
        textLog.setText("Выгружена: "+importData[0]);

        //Убираем клавиатуру при старте
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

    }

    //Сохраняем данные при закрытие
    @Override
    protected void onPause() {
        super.onPause();

        if (prevItem != null){
            DatabaseHandler.getInstance(getBaseContext()).putPerson(prevItem);
        }

        if (curStatus == null){
            DatabaseHandler.getInstance(this).setLastPos(prevPos,null);
        }
        else {
            DatabaseHandler.getInstance(this).setLastPos(prevPos,curStatus[0]);
        }
    }

    //Восстанавливаем настройки по умолчанию каждое открытие
    @Override
    protected void onResume() {
        super.onResume();

        int[] pos = new int[1];
        pos[0] = 0;

        DatabaseHandler.getInstance(this).getLastPos(pos,curStatus,importData);

        if (curStatus == null){
            whereStatus = null;
            statusBeforeSearch = null;
        }
        else {
            whereStatus = Person.COL_STATUS+" MATCH ?";
            statusBeforeSearch = new String[]{curStatus[0]};
        }

        //whereStatus = Person.COL_STATUS+" MATCH ?"; //Поумолчанию сортировка по статусу включена
        //curStatus = new String[]{"1"}; //Поумолчанию отображать только активных

        //idItem = -1L;
        prevPos = pos[0];
        searchStr = null;

        cursor = DatabaseHandler.getInstance(this).getCurCursor();

        if (cursor.getCount() > 0){
            if (prevPos >= 0 & prevPos < cursor.getCount()){
                cursor.moveToPosition(prevPos);
                idItem = cursor.getLong(0);
                mItem = DatabaseHandler.getInstance(getBaseContext()).getPerson(idItem);

                prevItem = copyPerson(mItem);

                enableUI();

                updateUIFromPerson(mItem);
            }

        }
        else {
            prevItem = new Person();
            prevItem.id = 1L;

            disableUI();
        }
    }

    //События по закрытию диалога выбора файлов
    public synchronized void onActivityResult(final int requestCode,
                                              int resultCode, final Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            AlertDialog alertDialog1 = new AlertDialog.Builder(this).create();
            alertDialog1.setCancelable(false);
            alertDialog1.setMessage(getString(R.string.alert_shutdown));
            alertDialog1.setButton(DialogInterface.BUTTON_POSITIVE,"OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    //Закрываем программу
                    finish();
                    System.exit(0);
                }
            });
            alertDialog1.show();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        mainMenu = menu;

        tvRowsCount = new TextView(this);
        tvRowsCount.setText(textCounter());
        tvRowsCount.setTextColor(Color.WHITE);
        //tvRowsCount.setOnClickListener(this);
        tvRowsCount.setPadding(25, 0, 25, 0);
        tvRowsCount.setTypeface(null, Typeface.BOLD);
        tvRowsCount.setTextSize(14);
        menu.findItem(R.id.itemTest).setActionView(tvRowsCount).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_mode) {
            action_mode = item;

            return true;
        }

        //Добавить контакт
        if (id == R.id.newContact) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.add_contact)
                    .setPositiveButton(android.R.string.ok, dialogAddClickListener)
                    .setNegativeButton(android.R.string.cancel, dialogAddClickListener)
                    .show();

            return true;
        }

        //Удаление
        if (id == R.id.delContact) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.del_contact)
                    .setPositiveButton(android.R.string.ok, dialogDelClickListener)
                    .setNegativeButton(android.R.string.cancel, dialogDelClickListener)
                    .show();


            return true;
        }

        //Поиск
        if (id == R.id.action_search) {
            posBeforeSearch = cursor.getPosition();
            statusBeforeSearch = curStatus;

            SearchDialog dSearch = new SearchDialog();
            dSearch.show(getFragmentManager(),"dSearch");

            return true;
        }

        //Выход из режима поиска
        if (R.id.action_close_search == item.getItemId()) {
            searchStr = null;

            showAllExceptCancelSearch(mainMenu, true);

            enableUI();

            curStatus = statusBeforeSearch;

            if (curStatus == null) {
                whereStatus = null;
            }
            else {
                whereStatus = Person.COL_STATUS+" MATCH ?";
            }

            cursor = DatabaseHandler.getInstance(getBaseContext()).getCurCursor();

            moveWithSave(getBaseContext(),posBeforeSearch);

            return true;
        }

        //Вправо
        if (id == R.id.actionRight) {
            if (cursor.moveToNext()){
                moveWithSave(getBaseContext(),cursor.getPosition());
            }
            else {
                cursor.moveToPrevious();
            }

            return true;
        }

        //Влево
        if (id == R.id.actionLeft) {
            if (cursor.moveToPrevious()){
                moveWithSave(getBaseContext(),cursor.getPosition());
            }
            else {
                cursor.moveToNext();
            }

            return true;
        }

        //Импорт
        if (id == R.id.importContacts) {
            //Вставляем дату изменения
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
            Log.d("My","Создан/изменён: " + sdf.format(new Date()));


            Intent intent = new Intent(getBaseContext(), FileDialog.class);
            intent.putExtra(FileDialog.START_PATH, Environment.getExternalStorageDirectory().toString());
            intent.putExtra(FileDialog.FORMAT_FILTER, new String[] { "db" });

            int REQUEST_SAVE = 0;

            startActivityForResult(intent, REQUEST_SAVE);



            return true;
        }

        //Экспорт
        if (id == R.id.exportContacts) {
            Intent intent = new Intent(getBaseContext(), FileDialog.class);
            intent.putExtra(FileDialog.START_PATH, Environment.getExternalStorageDirectory().toString());
            intent.putExtra(FileDialog.FORMAT_FILTER, new String[] { "db" });
            intent.putExtra(FileDialog.SELECTION_MODE, SelectionMode.MODE_SAVE);

            int REQUEST_SAVE = 0;

            startActivityForResult(intent, REQUEST_SAVE);

            return true;
        }

        //Отображаем все контакты
        if (id == R.id.action_mode_all) {
            enableUI();

            whereStatus = null;
            curStatus = null;

            cursor = DatabaseHandler.getInstance(this).getCurCursor();

            if (action_mode != null) {
                action_mode.setIcon(R.drawable.ic_mode_all);
            }

            moveWithSave(getBaseContext(),0);

            return true;
        }

        //Отображаем только активные контакты
        if (id == R.id.action_mode_active) {
            enableUI();

            //whereStatus = Person.COL_STATUS+" IS ?";
            whereStatus = Person.COL_STATUS+" MATCH ?";
            curStatus = new String[]{"1"};

            cursor = DatabaseHandler.getInstance(this).getCurCursor();

            if (action_mode != null) {
                action_mode.setIcon(R.drawable.ic_mode_active);
            }

            moveWithSave(getBaseContext(),0);

            return true;
        }

        //Отображаем только архивные контакты
        if (id == R.id.action_mode_archive) {
            enableUI();

            //whereStatus = Person.COL_STATUS+" IS ?";
            whereStatus = Person.COL_STATUS+" MATCH ?";
            curStatus = new String[]{"0"};

            cursor = DatabaseHandler.getInstance(this).getCurCursor();

            if (action_mode != null) {
                action_mode.setIcon(R.drawable.ic_mode_deactive);
            }

            moveWithSave(getBaseContext(),0);

            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    public static Person copyPerson(Person srcPerson){
        Person destPerson = new Person();

        destPerson.id = srcPerson.id;
        destPerson.fio = srcPerson.fio;
        destPerson.dr = srcPerson.dr;
        destPerson.parents = srcPerson.parents;
        destPerson.m_addr = srcPerson.m_addr;
        destPerson.a_addr = srcPerson.a_addr;
        destPerson.info = srcPerson.info;
        destPerson.d_dolten = srcPerson.d_dolten;
        destPerson.dolten = srcPerson.dolten;
        destPerson.d_nextdate = srcPerson.d_nextdate;
        destPerson.nextdate = srcPerson.nextdate;
        destPerson.d_lastdate = srcPerson.d_lastdate;
        destPerson.lastdate = srcPerson.lastdate;
        destPerson.status = srcPerson.status;
        //destPerson.log = srcPerson.log;

        return destPerson;
    }

    public static Boolean isDiffPersons(){
        Boolean res = false;

        if (prevItem == null){
            res = true;
        }
        else {
            if (!mItem.fio.equals(prevItem.fio)) res = true;
            if (!mItem.parents.equals(prevItem.parents)) res = true;
            if (!mItem.m_addr.equals(prevItem.m_addr)) res = true;
            if (!mItem.info.equals(prevItem.info)) res = true;
            if (!mItem.dolten.equals(prevItem.dolten)) res = true;
            if (!mItem.nextdate.equals(prevItem.nextdate)) res = true;
            if (!mItem.lastdate.equals(prevItem.lastdate)) res = true;
            if (mItem.status != prevItem.status) res = true;
        }

        return res;
    }

    public static void updateUIFromPerson(Person mItem){
        if (mItem != null){
            textFIO.setText(mItem.fio);
            textParents.setText(mItem.parents);
            textMAddr.setText(mItem.m_addr);
            textInfo.setText(mItem.info);
            textDolTen.setText(mItem.dolten);
            textNextDate.setText(mItem.nextdate);
            textLastDate.setText(mItem.lastdate);
            chBoxStatus.setChecked(mItem.status != 0);
            chBoxStatus.setText((mItem.status!=0) ? R.string.chBoxStatusTextActive : R.string.chBoxStatusTextDeactive);
        }
    }

    private static String textCounter(){
        return Integer.toString(cursor.getPosition()+1)+" / "+
                Integer.toString(cursor.getCount());
    }

    public static void moveWithSave(Context context, int position){

        //Сохраняем только если были изменения.
        if (isDiffPersons()){
            if (prevItem != null){
                //Вставляем дату изменения
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
                prevItem.a_addr = "Создан/изменён: " + sdf.format(new Date());
                DatabaseHandler.getInstance(context).putPerson(prevItem);
            }
            else {
                prevItem = new Person();
                //Вставляем дату изменения
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
                prevItem.a_addr = "Создан/изменён: " + sdf.format(new Date());
            }
        }


        if (cursor.getCount() > 0){
            //Проверяем position на вхождение в диапазон
            if (position > cursor.getCount()-1){
                position = cursor.getCount()-1;
            }
            cursor.moveToPosition(position);
            idItem = cursor.getLong(0);
            mItem = DatabaseHandler.getInstance(context).getPerson(idItem);
            updateUIFromPerson(mItem);

            prevItem = copyPerson(mItem);
            prevPos = cursor.getPosition();

            //Если в режиме поиска, то помечаем найденные значения
            if (searchStr != null) selSearchText();
        }
        else {
            disableUI();
        }

        tvRowsCount.setText(textCounter());

    }

    public static void showAllExceptCancelSearch(Menu menu, boolean show){
        menu.findItem(R.id.newContact).setVisible(show);
        menu.findItem(R.id.action_mode).setVisible(show);
        menu.findItem(R.id.action_search).setVisible(show);
        menu.findItem(R.id.delContact).setVisible(show);

        menu.findItem(R.id.action_close_search).setVisible(!show);
    }

    private static void disableUI(){
        textFIO.setText("");
        textParents.setText("");
        textMAddr.setText("");
        textInfo.setText("");
        textDolTen.setText("");
        textNextDate.setText("");
        textLastDate.setText("");

        textFIO.setEnabled(false);
        textParents.setEnabled(false);
        textMAddr.setEnabled(false);
        textInfo.setEnabled(false);
        textDolTen.setEnabled(false);
        textNextDate.setEnabled(false);
        textLastDate.setEnabled(false);
        chBoxStatus.setEnabled(false);
        btnUpdate.setEnabled(false);

        prevItem = null;
    }

    private static void enableUI(){
        textFIO.setEnabled(true);
        textParents.setEnabled(true);
        textMAddr.setEnabled(true);
        textInfo.setEnabled(true);
        textDolTen.setEnabled(true);
        textNextDate.setEnabled(true);
        textLastDate.setEnabled(true);
        chBoxStatus.setEnabled(true);
        btnUpdate.setEnabled(true);
    }

    private static void selSearchText(){
        //Выделяем искомый текст
        int start;
        int stop;
        Spannable text;

        start = strIndex(textFIO.getText().toString(),searchStr);
        stop = start + searchStr.length();
        if (start >= 0 & stop <= textFIO.getText().toString().length()){
            text = new SpannableString(textFIO.getText().toString());
            text.setSpan(new BackgroundColorSpan(Color.rgb(173,227,247)), start, stop,  Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            textFIO.setText(text);
        }

        start = strIndex(textParents.getText().toString(),searchStr);
        stop = start + searchStr.length();
        if (start >= 0 & stop <= textParents.getText().toString().length()){
            text = new SpannableString(textParents.getText().toString());
            text.setSpan(new BackgroundColorSpan(Color.rgb(173,227,247)), start, stop,  Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            textParents.setText(text);
        }

        start = strIndex(textMAddr.getText().toString(),searchStr);
        stop = start + searchStr.length();
        if (start >= 0 & stop <= textMAddr.getText().toString().length()){
            text = new SpannableString(textMAddr.getText().toString());
            text.setSpan(new BackgroundColorSpan(Color.rgb(173,227,247)), start, stop,  Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            textMAddr.setText(text);
        }

        start = strIndex(textInfo.getText().toString(),searchStr);
        stop = start + searchStr.length();
        if (start >= 0 & stop <= textInfo.getText().toString().length()){
            text = new SpannableString(textInfo.getText().toString());
            text.setSpan(new BackgroundColorSpan(Color.rgb(173,227,247)), start, stop,  Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            textInfo.setText(text);
        }

        start = strIndex(textDolTen.getText().toString(),searchStr);
        stop = start + searchStr.length();
        if (start >= 0 & stop <= textDolTen.getText().toString().length()){
            text = new SpannableString(textDolTen.getText().toString());
            text.setSpan(new BackgroundColorSpan(Color.rgb(173,227,247)), start, stop,  Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            textDolTen.setText(text);
        }

        start = strIndex(textNextDate.getText().toString(),searchStr);
        stop = start + searchStr.length();
        if (start >= 0 & stop <= textNextDate.getText().toString().length()){
            text = new SpannableString(textNextDate.getText().toString());
            text.setSpan(new BackgroundColorSpan(Color.rgb(173,227,247)), start, stop,  Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            textNextDate.setText(text);
        }

        start = strIndex(textLastDate.getText().toString(),searchStr);
        stop = start + searchStr.length();
        if (start >= 0 & stop <= textLastDate.getText().toString().length()){
            text = new SpannableString(textLastDate.getText().toString());
            text.setSpan(new BackgroundColorSpan(Color.rgb(173,227,247)), start, stop,  Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            textLastDate.setText(text);
        }
    }

    private static int strIndex(String str,String search){
        str = str.toLowerCase();
        search = search.toLowerCase();
        return str.indexOf(search);
    }

    //Листнер для диалога активации контакта
    DialogInterface.OnClickListener dialogActivateClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    //Yes button clicked
                    prevPos = cursor.getPosition();

                    //Вставляем дату изменения
                    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
                    prevItem.a_addr = "Создан/изменён: " + sdf.format(new Date());

                    if (cursor.getCount() == cursor.getPosition()+1){
                        DatabaseHandler.getInstance(getBaseContext()).putPerson(prevItem);
                        cursor = DatabaseHandler.getInstance(getBaseContext()).getCurCursor();

                        prevItem = null;

                        moveWithSave(getBaseContext(),cursor.getCount() - 1);
                    }
                    else {
                        DatabaseHandler.getInstance(getBaseContext()).putPerson(prevItem);
                        cursor = DatabaseHandler.getInstance(getBaseContext()).getCurCursor();

                        prevItem = null;

                        moveWithSave(getBaseContext(),prevPos);
                    }

                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    chBoxStatus.setChecked(!chBoxStatus.isChecked());
                    break;
            }
        }
    };

    //Листнер для диалога добавления
    DialogInterface.OnClickListener dialogAddClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    //Yes button clicked
                    //mItem = new Person();

                    enableUI();

                    DatabaseHandler.getInstance(getBaseContext()).putPerson(new Person());
                    cursor = DatabaseHandler.getInstance(getBaseContext()).getCurCursor();

                    moveWithSave(getBaseContext(),cursor.getCount()-1);

                    //Если вставили самую первую запись
                    if (cursor.getCount() == 1) prevItem.id = cursor.getLong(0);

                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    break;
            }
        }
    };

    //Листнер для диалога удаления
    DialogInterface.OnClickListener dialogDelClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    //Yes button clicked
                    if (cursor.getCount() != 0){

                        prevPos = cursor.getPosition();

                        if (cursor.getCount()-1 == cursor.getPosition()){
                            DatabaseHandler.getInstance(getBaseContext()).removePerson(prevItem.id);
                            cursor = DatabaseHandler.getInstance(getBaseContext()).getCurCursor();

                            prevItem = null;

                            moveWithSave(getBaseContext(),cursor.getCount() - 1);
                        }
                        else {
                            DatabaseHandler.getInstance(getBaseContext()).removePerson(prevItem.id);
                            cursor = DatabaseHandler.getInstance(getBaseContext()).getCurCursor();

                            prevItem = null;

                            moveWithSave(getBaseContext(),prevPos);
                        }
                    }
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    break;
            }
        }
    };

    DialogInterface.OnClickListener dialogDragClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    //Yes button clicked
                    if (!textNextDate.getText().toString().isEmpty()) {
                        String div = (!textLastDate.getText().toString().isEmpty())
                                ? "\n--------\n" : "";
                        textLastDate.getEditableText().insert(0,
                                textNextDate.getText().toString() + div);
                        textNextDate.setText("");
                    }
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    break;
            }
        }
    };
}
