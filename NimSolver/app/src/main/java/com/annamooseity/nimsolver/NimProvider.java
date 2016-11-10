package com.annamooseity.nimsolver;

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;


/**
 * Created by Anna on 11/7/2016.
 */
public class NimProvider extends ContentProvider
{
    private static final String DATABASE_NAME = "nimProvider.db";
    private static final int DATABASE_VERSION = 1;
    public static final String PROVIDER = "com.annamooseity.nim";

    // Table names
    private static final String GAME_TABLE_NAME = "games";
    private static final String RULES_TABLE_NAME = "rules";

    // Stuff for matching URIs
    private static final UriMatcher sUriMatcher;
    private static HashMap<String, String> gameProjectionMap;
    private static HashMap<String, String> rulesProjectionMap;

    // Integer IDs for the URI matcher
    private static final int GAME = 1;
    private static final int GAME_ID = 2;
    private static final int RULES = 3;
    private static final int RULES_ID = 4;

    // Helper for database
    private DatabaseHelper dbHelper;

    // The Database Helper
    private static class DatabaseHelper extends SQLiteOpenHelper
    {
        DatabaseHelper(Context context)
        {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
            // Create NimRules table
            db.execSQL("CREATE TABLE " + RULES_TABLE_NAME + " (" +
                    NimRules.RULES_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    NimRules.PILES + " TEXT," +
                    NimRules.TAKE_OPTIONS + " TEXT," +
                    NimRules.PLAYER_FIRST + " TEXT" + ");");

            // Create NimGame table
            db.execSQL("CREATE TABLE " + GAME_TABLE_NAME + " (" +
                    NimGame.GAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    NimGame.PILES + " INTEGER," +
                    NimGame.RULES_INDEX + " INTEGER," +
                    NimRules.OTHER_PLAYER + " TEXT," +
                    NimGame.MOVE + " TEXT" + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
        {
            db.execSQL("DROP TABLE IF EXISTS " + RULES_TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + GAME_TABLE_NAME);
            onCreate(db);
        }
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs)
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count;
        Uri notifyUri;
        switch (sUriMatcher.match(uri))
        {
            case GAME:
                notifyUri = NimGame.CONTENT_URI_game;
                count = db.delete(GAME_TABLE_NAME, where, whereArgs);
                break;
            case GAME_ID:
                notifyUri = NimGame.CONTENT_URI_game;
                where = where + "_id = " + uri.getLastPathSegment();
                count = db.delete(GAME_TABLE_NAME, where, whereArgs);
                break;
            case RULES:
                notifyUri = NimRules.CONTENT_URI_rules;
                count = db.delete(RULES_TABLE_NAME, where, whereArgs);
                break;
            case RULES_ID:
                notifyUri = NimRules.CONTENT_URI_rules;
                where = where + "_id = " + uri.getLastPathSegment();
                count = db.delete(RULES_TABLE_NAME, where, whereArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(notifyUri, null);
        return count;
    }

    @Override
    public String getType(Uri uri)
    {
        switch (sUriMatcher.match(uri))
        {
            case GAME:
                return NimGame.CONTENT_TYPE;
            case GAME_ID:
                return NimGame.CONTENT_TYPE;
            case RULES:
                return NimRules.CONTENT_TYPE;
            case RULES_ID:
                return NimRules.CONTENT_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues)
    {

        ContentValues values;
        if (initialValues != null)
        {
            values = new ContentValues(initialValues);
        }
        else
        {
            values = new ContentValues();
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long rowId;

        switch (sUriMatcher.match(uri))
        {
            case GAME:
            case GAME_ID:
                rowId = db.insert(GAME_TABLE_NAME, NimGame.PILES, values);
                if (rowId > 0)
                {
                    Uri gameUri = ContentUris.withAppendedId(NimGame.CONTENT_URI_game, rowId);
                    getContext().getContentResolver().notifyChange(gameUri, null);
                    return gameUri;
                }
                break;
            case RULES:
            case RULES_ID:
                rowId = db.insert(RULES_TABLE_NAME, NimRules.PILES, values);
                if (rowId > 0)
                {
                    Uri rulesUri = ContentUris.withAppendedId(NimRules.CONTENT_URI_rules, rowId);
                    getContext().getContentResolver().notifyChange(rulesUri, null);
                    return rulesUri;
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public boolean onCreate()
    {
        dbHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
    {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        Uri notifyUri;

        switch (sUriMatcher.match(uri))
        {
            case GAME:
                qb.setTables(GAME_TABLE_NAME);
                qb.setProjectionMap( rulesProjectionMap);
                notifyUri = NimGame.CONTENT_URI_game;
                break;
            case GAME_ID:
                qb.setTables(GAME_TABLE_NAME);
                qb.setProjectionMap( rulesProjectionMap);
                if (selection != null)
                {
                    selection = selection + "_id = " + uri.getLastPathSegment();
                }
                notifyUri = NimGame.CONTENT_URI_game;
                break;
            case RULES:
                qb.setTables(RULES_TABLE_NAME);
                qb.setProjectionMap(gameProjectionMap);
                notifyUri = NimRules.CONTENT_URI_rules;
                break;
            case RULES_ID:
                qb.setTables(RULES_TABLE_NAME);
                qb.setProjectionMap(gameProjectionMap);
                if (selection != null)
                {
                    selection = selection + "_id = " + uri.getLastPathSegment();
                }
                notifyUri = NimRules.CONTENT_URI_rules;
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);

        c.setNotificationUri(getContext().getContentResolver(), notifyUri);

        return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs)
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count;
        Uri notifyUri;
        switch (sUriMatcher.match(uri))
        {
            case GAME:
                notifyUri = NimGame.CONTENT_URI_game;
                count = db.update(GAME_TABLE_NAME, values, where, whereArgs);
                break;
            case RULES:
                notifyUri = NimRules.CONTENT_URI_rules;
                count = db.update(RULES_TABLE_NAME, values, where, whereArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(notifyUri, null);
        return count;
    }

    static
    {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(PROVIDER, "Accounts/transactions/1", GAME);
        sUriMatcher.addURI(PROVIDER, "Accounts/transactions/1" + "/#", GAME_ID);
        sUriMatcher.addURI(PROVIDER, RULES_TABLE_NAME, RULES);
        sUriMatcher.addURI(PROVIDER, RULES_TABLE_NAME + "/#", RULES_ID);

        gameProjectionMap = new HashMap<String, String>();
        gameProjectionMap.put(NimGame.GAME_ID, NimGame.GAME_ID);
        gameProjectionMap.put(NimGame.PILES, NimGame.PILES);
        gameProjectionMap.put(NimGame.RULES_INDEX, NimGame.RULES_INDEX);
        gameProjectionMap.put(NimGame.MOVE, NimGame.MOVE);
        gameProjectionMap.put(NimRules.OTHER_PLAYER, NimRules.OTHER_PLAYER);

         rulesProjectionMap = new HashMap<String, String>();
         rulesProjectionMap.put(NimRules.RULES_ID, NimRules.RULES_ID);
         rulesProjectionMap.put(NimRules.PILES, NimRules.PILES);
         rulesProjectionMap.put(NimRules.PLAYER_FIRST, NimRules.PLAYER_FIRST);
         rulesProjectionMap.put(NimRules.TAKE_OPTIONS, NimRules.TAKE_OPTIONS);

    }
}