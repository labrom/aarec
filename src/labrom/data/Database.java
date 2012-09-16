package labrom.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public abstract class Database {
	
    protected final SQLiteOpenHelper helper;
    private final ThreadLocal<SQLiteDatabase> dbs = new ThreadLocal<SQLiteDatabase>();
    
    
    protected Database(Context ctx) {
    	this.helper = createHelper(ctx);
    }
    
    protected abstract SQLiteOpenHelper createHelper(Context ctx);


    public void close() {
    	SQLiteDatabase db = dbs.get();
    	if(db != null && db.isOpen()) {
    	    try {
    	        db.close();
    	        dbs.remove();
    	    } catch(Exception e) {
    	        Log.e(L.T, "Unable to close database: " + e.getMessage());
    	    }
    	}
    }
    
	public SQLiteDatabase ensureOpen() {
    	SQLiteDatabase db = dbs.get();
    	if(db == null || !db.isOpen()) {
    		db = helper.getWritableDatabase();
    		dbs.set(db);
    	}
    	return db;
	}
	
	public <T extends ActiveRecord> boolean persist(T record) {
		SQLiteDatabase db = ensureOpen();
	    ContentValues v = new ContentValues();
	    record.populateFull(v);
        long id = db.insert(record.getTableName(), null, v);
        if(id >= 0) {
            record.attach(this, id);
            return true;
        }
        return false;
	}
	
	
	public <T extends ActiveRecord> boolean update(T record) {
		SQLiteDatabase db = ensureOpen();
	    ContentValues v = new ContentValues();
	    record.populateForUpdate(v);
	    if(record.getId() >= 0)
	        return db.update(record.getTableName(), v, BaseColumns._ID + "=" + record.getId(), null) == 1;
	    
	    return false;
	}
	
	public <T extends ActiveRecord> T read(T proto, long id) {
		SQLiteDatabase db = ensureOpen();
	    Cursor c = db.query(proto.getTableName(), null, BaseColumns._ID + "=" + id, null, null, null, null);
	    try {
    	    if(c.moveToFirst()) {
    	        @SuppressWarnings("unchecked")
                T copy = (T)proto.copy();
                copy.hydrateFromCursor(this, c);
    	        return copy;
    	    }
	    } finally {
	        c.close();
	    }
	    return null;
	}
	
	public <T extends ActiveRecord> boolean delete(T record) {
		SQLiteDatabase db = ensureOpen();
	    return db.delete(record.getTableName(), BaseColumns._ID + "=" + record.getId(), null) == 1;
	}
	


    public <T extends ActiveRecord> ActiveRecordList<T> query(T proto, String[] columns, String selection, String[] selectionArgs, String orderBy) {
    	return query(proto, false, columns, selection, selectionArgs, null, null, orderBy, null);
    }

    public <T extends ActiveRecord> ActiveRecordList<T> query(T proto, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy) {
    	return query(proto, false, columns, selection, selectionArgs, groupBy, having, orderBy, null);
    }

    public <T extends ActiveRecord> ActiveRecordList<T> query(T proto, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
    	return query(proto, false, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
    }

    public <T extends ActiveRecord> ActiveRecordList<T> query(T proto, boolean distinct, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
    	SQLiteDatabase db = ensureOpen();
        Cursor c = db.query(distinct, proto.getTableName(), columns, selection, selectionArgs, groupBy, having, orderBy, limit);
        return new ActiveRecordList<T>(this, proto, c);
    }

	public <T extends ActiveRecord> ActiveRecordList<T> queryByExample(T example, String orderBy) {
		SelectionArgs selection = new SelectionArgs();
		example.populateForQueryByExample(selection);
		return query(example, null, selection.buildSelectionString(), selection.buildSelectionValues(), orderBy);
	}
	
	public <T extends ActiveRecord> T getUnique(T proto, String selection, String[] selectionArgs) {
	    ActiveRecordList<T> list = query(proto, null, selection, selectionArgs, null);
	    return list.getUnique();
	}
	
	public <T extends ActiveRecord> int update(T proto, ContentValues values, String selection, String[] selectionArgs) {
		SQLiteDatabase db = ensureOpen();
	    return db.update(proto.getTableName(), values, selection, selectionArgs);
	}
	

}
