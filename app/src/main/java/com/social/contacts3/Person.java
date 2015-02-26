package com.social.contacts3;

import android.content.ContentValues;
import android.database.Cursor;

/**
 * Created by rinat on 18.05.14.
 */
public class Person implements Cloneable {
    public static final String TABLE_NAME = "Persons";
    public static final String COL_ID = "_id";
    public static final String COL_FIO = "fio";
    public static final String COL_DR = "dr";
    public static final String COL_PARENTS = "parents";
    public static final String COL_M_ADDR = "m_addr";
    public static final String COL_A_ADDR = "a_addr";
    public static final String COL_INFO = "info";
    public static final String COL_D_DOLTEN = "d_dolten";
    public static final String COL_DOLTEN = "dolten";
    public static final String COL_D_NEXTDATE = "d_nextdate";
    public static final String COL_NEXTDATE = "nextdate";
    public static final String COL_D_LASTDATE = "d_lastdate";
    public static final String COL_LASTDATE = "lastdate";
    public static final String COL_STATUS = "status";
    //public static final String COL_LOG = "log";
    //public static final String COL_DAYS = "case when (d_lastdate > 0) then (strftime('%s', 'now','localtime')*1000 - d_lastdate)/86400000 else 'no' end as day";

    // For database projection so order is consistent
    public static final String[] FIELDS = {
            "rowid as _id", //COL_ID
            //COL_ID,
            COL_FIO,
            COL_DR,
            COL_PARENTS,
            COL_M_ADDR,
            COL_A_ADDR,
            COL_INFO,
            COL_D_DOLTEN,
            COL_DOLTEN,
            COL_D_NEXTDATE,
            COL_NEXTDATE,
            COL_D_LASTDATE,
            COL_LASTDATE,
            COL_STATUS};
            //COL_LOG};

    /*
     * The SQL code that creates a Table for storing Persons in.
     * Note that the last row does NOT end in a comma like the others.
     * This is a common source of error.
     */
    public static final String CREATE_TABLE =
            "CREATE VIRTUAL TABLE " + TABLE_NAME + " USING fts4("
            //"CREATE TABLE " + TABLE_NAME + "("
                    +"tokenize=unicode61,"
                    //+ COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COL_FIO + " TEXT NOT NULL DEFAULT '',"
                    + COL_DR + " INTEGER DEFAULT '',"
                    + COL_PARENTS + " TEXT DEFAULT '',"
                    + COL_M_ADDR + " TEXT DEFAULT '',"
                    + COL_A_ADDR + " TEXT DEFAULT '',"
                    + COL_INFO + " TEXT DEFAULT '',"
                    + COL_D_DOLTEN + " INTEGER DEFAULT '',"
                    + COL_DOLTEN + " TEXT DEFAULT '',"
                    + COL_D_NEXTDATE + " INTEGER DEFAULT '',"
                    + COL_NEXTDATE + " TEXT DEFAULT '',"
                    + COL_D_LASTDATE + " INTEGER DEFAULT '',"
                    + COL_LASTDATE + " TEXT DEFAULT '',"
                    + COL_STATUS + " INTEGER DEFAULT '1'"
                    //+ COL_LOG + " TEXT DEFAULT ''"
                    + ")";

    // Fields corresponding to database columns
    public long id = -1;
    public String fio = "";
    public Long dr = 0L;
    public String parents = "";
    public String m_addr = "";
    public String a_addr = "";
    public String info = "";
    public Long d_dolten = 0L;
    public String dolten = "";
    public Long d_nextdate = 0L;
    public String nextdate = "";
    public Long d_lastdate = 0L;
    public String lastdate = "";
    public int status = 1;
    //public String log = "";

    /**
     * No need to do anything, fields are already set to default values above
     */
    public Person() {
    }

    /**
     * Convert information from the database into a Person object.
     */
    public Person(final Cursor cursor) {
        // Indices expected to match order in FIELDS!
        //this.id = cursor.getLong(0);
        this.id = cursor.getLong(cursor.getColumnIndex(COL_ID));
        this.fio = cursor.getString(cursor.getColumnIndex(COL_FIO));
        this.dr = cursor.getLong(cursor.getColumnIndex(COL_DR));
        this.parents = cursor.getString(cursor.getColumnIndex(COL_PARENTS));
        this.m_addr = cursor.getString(cursor.getColumnIndex(COL_M_ADDR));
        this.a_addr = cursor.getString(cursor.getColumnIndex(COL_A_ADDR));
        this.info = cursor.getString(cursor.getColumnIndex(COL_INFO));
        this.d_dolten = cursor.getLong(cursor.getColumnIndex(COL_D_DOLTEN));
        this.dolten = cursor.getString(cursor.getColumnIndex(COL_DOLTEN));
        this.d_nextdate = cursor.getLong(cursor.getColumnIndex(COL_D_NEXTDATE));
        this.nextdate = cursor.getString(cursor.getColumnIndex(COL_NEXTDATE));
        this.d_lastdate = cursor.getLong(cursor.getColumnIndex(COL_D_LASTDATE));
        this.lastdate = cursor.getString(cursor.getColumnIndex(COL_LASTDATE));
        this.status = cursor.getInt(cursor.getColumnIndex(COL_STATUS));
        //this.log = cursor.getString(cursor.getColumnIndex(COL_LOG));
    }

    /**
     * Return the fields in a ContentValues object, suitable for insertion
     * into the database.
     */
    public ContentValues getContent() {
        final ContentValues values = new ContentValues();
        // Note that ID is NOT included here
        values.put(COL_FIO, fio);
        values.put(COL_DR, dr);
        values.put(COL_PARENTS, parents);
        values.put(COL_M_ADDR, m_addr);
        values.put(COL_A_ADDR, a_addr);
        values.put(COL_INFO, info);
        values.put(COL_D_DOLTEN, d_dolten);
        values.put(COL_DOLTEN, dolten);
        values.put(COL_D_NEXTDATE, d_nextdate);
        values.put(COL_NEXTDATE, nextdate);
        values.put(COL_D_LASTDATE, d_lastdate);
        values.put(COL_LASTDATE, lastdate);
        values.put(COL_STATUS, status);
        //values.put(COL_LOG, log);

        return values;
    }

    public Person clone() throws CloneNotSupportedException {
        return (Person)super.clone();
    }
}
