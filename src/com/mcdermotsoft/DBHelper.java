package com.mcdermotsoft;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils.InsertHelper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper
{
	Context context;
	SQLiteDatabase db;
	static String DBNAME = "glitchbuddy";
	static int DBVERSION = 1;
	static String auctionHistoryTable = "autctionHistory";
	static String costCol = "cost";
	static String categoryCol = "category";
	static String tsidCol = "class_tsid";
	static String seenCol = "seen";
	static String nameCol = "name";
	
	public DBHelper(Context context)
	{
		super(context,DBNAME,null,DBVERSION);
		db = getWritableDatabase();
	}

	@Override
	public void onCreate(SQLiteDatabase db) 
	{
		// the SQLite query string that will create our database table.
		String newTableQueryString = 	
			"create table " +
			auctionHistoryTable +
			" (" +
			tsidCol + " text not null," +
			categoryCol + " text," +
			nameCol + " text," +
			costCol + " real," +
			seenCol + " integer" +
			");";
	 
		// execute the query string to the database.
		db.execSQL(newTableQueryString);
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2)
	{
		// TODO Auto-generated method stub
		
	}
	
	public void addRow(String class_tsid, String category, String name, float cost, int seen)
	{
		InsertHelper ih = new InsertHelper(db, auctionHistoryTable);
		final int tsidColumn = ih.getColumnIndex(tsidCol);
		final int categoryColumn = ih.getColumnIndex(categoryCol);
		final int nameColumn = ih.getColumnIndex(nameCol);
		final int costColumn = ih.getColumnIndex(costCol);
		final int seenColumn = ih.getColumnIndex(seenCol);
		ih.prepareForInsert();
		ih.bind(tsidColumn, class_tsid);
		ih.bind(categoryColumn, category);
		ih.bind(nameColumn, name);
		ih.bind(costColumn, cost);
		ih.bind(seenColumn, seen);
	 
		// ask the database object to insert the new data 
		try
		{
			ih.execute();
		}
		catch(Exception e)
		{
			Log.e("DB ERROR", e.toString()); // prints the error message to the log
			e.printStackTrace(); // prints the stack trace to the log
		}
	}
	
	public void deleteRow(String class_tsid)
	{
		// ask the database manager to delete the row of given id
		try
		{
		    db.delete(auctionHistoryTable, "class_tsid=?", new String[]{class_tsid});
	    }
		catch (Exception e)
		{
			Log.e("DB ERROR", e.toString());
			e.printStackTrace();
		}
	}
	
	public void updateRow(String class_tsid, String category, String name, float cost, int seen)
	{
		// this is a key value pair holder used by android's SQLite functions
		ContentValues values = new ContentValues();
		values.put(tsidCol, class_tsid);
		values.put(categoryCol, category);
		values.put(nameCol, name);
		values.put(costCol, cost);
		values.put(seenCol, seen);
	 
		// ask the database object to update the database row of given rowID
		try
		{
			db.update(auctionHistoryTable, values, "class_tsid=?", new String[]{class_tsid});
		}
		catch (Exception e)
		{
			Log.e("DB Error", e.toString());
			e.printStackTrace();
		}
	}
	
	public Cursor getExactRow(String class_tsid)
	{
		Cursor cursor = null;
		cursor = db.rawQuery("SELECT rowid _id, * FROM " + auctionHistoryTable + " WHERE class_tsid = ?", new String[]{class_tsid});
		
		return cursor;
	}
	
	public Cursor getLikeRow(String name)
	{
		Cursor cursor = null;
		cursor = db.rawQuery("SELECT rowid _id, * FROM " + auctionHistoryTable + " WHERE name LIKE '%" + name + "%'", null);
	
		return cursor;
	}
}
