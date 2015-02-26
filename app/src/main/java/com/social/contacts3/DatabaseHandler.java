package com.social.contacts3;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import org.sqlite.database.sqlite.SQLiteDatabase;
import org.sqlite.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by rinat on 18.05.14.
 */
public class DatabaseHandler extends SQLiteOpenHelper {

    static {
        System.loadLibrary("sqliteX");
    }

    private static DatabaseHandler singleton;
    public static String DATABASE_FILE_PATH = Environment.getExternalStorageDirectory().toString() +
            File.separator + "Contacts";
    public static String BAKCUP_DIR_PATH = DATABASE_FILE_PATH + File.separator + "backup";

    static {
        File dirDB = new File(DATABASE_FILE_PATH);
        if (!dirDB.exists()){
            dirDB.mkdirs();
        }

        File dirBackup = new File(BAKCUP_DIR_PATH);
        if (!dirBackup.exists()){
            dirBackup.mkdirs();
        }
    }

    public static DatabaseHandler getInstance(final Context context) {
        if (singleton == null) {
            singleton = new DatabaseHandler(context);
        }
        return singleton;
    }

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "contacts.db";

    private final Context context;

    public DatabaseHandler(Context context) {
        super(context, DATABASE_FILE_PATH + File.separator + DATABASE_NAME, null, DATABASE_VERSION);
        // Good idea to use process context here
        this.context = context.getApplicationContext();

        //=======Делаем бэкап если нужно===============//

        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

        //Фильтр для файлов бэкапа
        FilenameFilter backupFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if(name.matches("^[0-9]+-[0-9][0-9]-[0-9][0-9][0-9][0-9].db$"))
                {
                    return true;
                }
                return false;
            }
        };

        File dirBackup = new File(BAKCUP_DIR_PATH);

        //Файл сегоднешнего бэкапа
        File CURRENT_BACKUP = new File(BAKCUP_DIR_PATH +
                File.separator + dateFormat.format(new Date())+".db");

        if (! CURRENT_BACKUP.exists()){
            if (dirBackup.list(backupFilter).length >= 5){
                removeOldest(dirBackup.list(backupFilter));
            }

            doBackup();
        }

        //=============================================//
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        //Таблица настроек
        db.execSQL("CREATE TABLE Settings (pos INTEGER NOT NULL DEFAULT (0), status TEXT NOT NULL DEFAULT (1), " +
                "id INTEGER NOT NULL DEFAULT(0), import TEXT NOT NULL DEFAULT (1))");
        ContentValues values = new ContentValues();
        values.put("pos",0);
        values.put("status","1");
        values.put("id","0");
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy hh:mm");
        values.put("import",sdf.format(new Date()));
        db.insert("Settings",null,values);
        //Основная таблица
        db.execSQL(Person.CREATE_TABLE);

        //db.setVersion();


        Person person = new Person();
        person.id = 10L;
        person.fio = "Иванова Алёна +79586541257 3 этаж";
        person.dr = 755654400000L;
        person.parents = "Андрей +7 999 33 44 44\nСветлана +7 999 555 55 55";
        person.m_addr = "ул. Ленина, д1, кв 45";
        person.a_addr = "";
        person.info = "";
        person.d_dolten = 1404432000000L;
        person.dolten = "Узнать, что случилось";
        person.d_nextdate = 1400889600000L;
        person.nextdate = "Просто поговорить";
        person.d_lastdate = 1400457600000L;
        person.lastdate = "07.07.2014 Просто пообщались\n----------\n05.05.2014 Уточнить, что случилось";
        person.status = 1;
        db.insert(Person.TABLE_NAME, null, person.getContent());

        person.id = 15L;
        person.fio = "Петров Сергей Первомайская налево +79125745896";
        person.dr = 895536000000L;
        person.parents = "Павел +7 999 33 44 44\nИрина +7 999 555 55 55";
        person.m_addr = "ул. Первомайская, д102, кв 435";
        person.a_addr = "";
        person.info = "";
        person.d_dolten = 1404259200000L;
        person.dolten = "Узнать, что случилось";
        person.d_nextdate = 1402790400000L;
        person.nextdate = "Просто поговорить";
        person.d_lastdate = 1315850400000L;
        person.lastdate = "07.07.2014 Просто пообщались\n----------\n05.05.2014 Уточнить, что случилось";
        person.status = 0;
        db.insert(Person.TABLE_NAME, null, person.getContent());

        person.id = 23L;
        person.fio = "Сидоров Пётр соседский дом +79455891247";
        person.dr = 916444800000L;
        person.parents = "Надежда +7 999 33 44 44\nИван +7 999 555 55 55";
        person.m_addr = "ул. 50 лет октября, д12, кв 35";
        person.a_addr = "";
        person.info = "";
        person.d_dolten = 1409356800000L;
        person.dolten = "Узнать, что случилось";
        person.d_nextdate = 1403827200000L;
        person.nextdate = "Просто поговорить";
        person.d_lastdate = 0L;
        person.lastdate = "07.07.2014 Просто пообщались\n----------\n05.05.2014 Уточнить, что случилось";
        person.status = 1;
        db.insert(Person.TABLE_NAME, null, person.getContent());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public synchronized Person getPerson(final long id) {
        final SQLiteDatabase db = this.getReadableDatabase();
        final Cursor cursor = db.query(Person.TABLE_NAME,
                //Person.FIELDS, Person.COL_ID + " IS ?",
                Person.FIELDS, "rowid IS ?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor == null || cursor.isAfterLast()) {
            return null;
        }

        Person item = null;
        if (cursor.moveToFirst()) {
            item = new Person(cursor);
        }
        cursor.close();

        return item;
    }

    public synchronized Cursor getCurCursor(){
        final SQLiteDatabase db = this.getReadableDatabase();
        final Cursor cursor = db.query(Person.TABLE_NAME,
                Person.FIELDS, MainActivity.whereStatus,
                MainActivity.curStatus, null, null, null, null);
        return cursor;
    }

    public synchronized boolean putPerson(final Person person) {
        boolean success = false;
        int result = 0;
        final SQLiteDatabase db = this.getWritableDatabase();

        if (person.id > -1) {
            result += db.update(Person.TABLE_NAME, person.getContent(),
                    //Person.COL_ID + " IS ?",
                    "rowid IS ?",
                    new String[] { String.valueOf(person.id) });
        }

        if (result > 0) {
            success = true;
        } else {
            // Update failed or wasn't possible, insert instead
            final long id = db.insert(Person.TABLE_NAME, null,
                    person.getContent());

            if (id > -1) {
                person.id = id;
                success = true;
            }
        }

        return success;
    }

    public synchronized int removePerson(final Long id) {
        final SQLiteDatabase db = this.getWritableDatabase();
        final int result = db.delete(Person.TABLE_NAME,
                //Person.COL_ID + " IS ?",
                "rowid IS ?",
                new String[] { Long.toString(id) });

        return result;
    }


    void setLastPos(int pos, String curStatus){
        SQLiteDatabase db = this.getReadableDatabase();

        ContentValues values = new ContentValues();
        values.put("pos",pos);
        if (curStatus == null){
            curStatus = "";
        }
        values.put("status",curStatus);
        db.update("Settings",values,"id IS ?",new String[]{"0"});
    }


    void getLastPos(int[] pos, String[] curStatus, String[] importData){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("Settings",new String[] {"pos","status","import"}, null, null, null, null, null, null);
        cursor.moveToFirst();

        pos[0] = cursor.getInt(0);
        if (cursor.getString(1).isEmpty()){
            curStatus = null;
        }
        else {
            curStatus[0] = cursor.getString(1);
        }
        importData[0] = cursor.getString(2);
    }


    private String getNameWithoutEx(String name){
        int lastIndex = name.lastIndexOf('.');
        return name.substring(0,lastIndex);
    }

    private void removeOldest(String[] entry){
        String fileForDel = entry[0];
        Date d1 = new Date();
        Date d2 = new Date();

        String strDate = getNameWithoutEx(entry[0]);

        try {
            d1 = new SimpleDateFormat("dd-MM-yyyy").parse(strDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        for (int i = 1; i < entry.length; i++){

            strDate = getNameWithoutEx(entry[i]);

            try {
                d2 = new SimpleDateFormat("dd-MM-yyyy").parse(strDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (d1.compareTo(d2) > 0){
                d1 = (Date) d2.clone();
                fileForDel = entry[i];
            }
        }

        File f = new File(BAKCUP_DIR_PATH + File.separator +fileForDel);
        f.delete();
    }

    public void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    void doBackup(){
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

        File db = new File(DATABASE_FILE_PATH + File.separator + DATABASE_NAME);
        File backup = new File(BAKCUP_DIR_PATH +
                File.separator + dateFormat.format(new Date())+".db");

        try {
            copy(db,backup);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
