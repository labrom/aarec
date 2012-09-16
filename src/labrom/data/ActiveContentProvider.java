package labrom.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * A generic content provider for manipulating active records.
 * Implementing classes must implement {@link #getProto(Uri)} to determine
 * which type of active record the provider will work with in each operation.
 * 
 * @author Romain Laboisse labrom@gmail.com
 *
 */
public abstract class ActiveContentProvider extends ContentProvider {
	
	private Database db;
	
	/**
	 * Returns a prototype active record object to use for querying or updating or inserting data.
	 * If the URI is not supported by this content provider, it should return null.
	 * @param uri
	 * @return A prototype active record object or null if the specified URI is not supported.
	 */
	protected abstract <T extends ActiveRecord> T getProto(Uri uri);
	
	protected Database getDatabase() {
	    return db;
	}
	
	/**
	 * Implementing classes MUST call this method from {@link ContentProvider#onCreate()}.
	 * @param db
	 */
	protected void setDatabase(Database db) {
		this.db = db;
	}

	/**
	 * If provided URI ends with a DB _ID, will replace whatever was in selection and selectionArgs
	 * parameters with a WHERE clause that filters on the extracted DB _ID. Otherwise, passes the selection
	 * and selectionArgs parameters to the underlying DB without modifying them.
	 */
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        ActiveRecord proto = getProto(uri);
        if(proto == null)
            throw new IllegalArgumentException(uri.toString() + " is not supported by this content provider");
		long id = -1;
		try {
			id = ContentUris.parseId(uri);
		} catch(NumberFormatException e) {
			// URI doesn't end with an ID, that's fine
		}
		if(id >= 0) {
			selection = BaseColumns._ID + "=?";
			selectionArgs = new String[] {String.valueOf(id)};
		}
        return db.query(proto, projection, selection, selectionArgs, sortOrder).getCursor();
	}
	
	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
        ActiveRecord proto = getProto(uri);
        if(proto == null)
            throw new IllegalArgumentException(uri.toString() + " is not supported by this content provider");
		SQLiteDatabase sqlite = db.ensureOpen();
        long id = sqlite.insert(proto.getTableName(), null, values);
		if(id >= 0) {
            getContext().getContentResolver().notifyChange(uri, null);
		    return ContentUris.withAppendedId(uri, id);
		}
		return null;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
        ActiveRecord proto = getProto(uri);
        if(proto == null)
            throw new IllegalArgumentException(uri.toString() + " is not supported by this content provider");
		long id = ContentUris.parseId(uri);
		if(id >= 0) {
			SQLiteDatabase sqlite = db.ensureOpen();
			return sqlite.delete(proto.getTableName(), BaseColumns._ID + "=?", new String[] {String.valueOf(id)});
		}
		return 0;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        ActiveRecord proto = getProto(uri);
        if(proto == null)
            throw new IllegalArgumentException(uri.toString() + " is not supported by this content provider");
	    long id = ContentUris.parseId(uri);
	    if(id >= 0) {
	        selection = BaseColumns._ID + "=?";
	        selectionArgs = new String[] {String.valueOf(id)};
	    }
        SQLiteDatabase sqlite = db.ensureOpen();
        int modified = sqlite.update(proto.getTableName(), values, selection, selectionArgs);
        if(modified > 0)
            getContext().getContentResolver().notifyChange(uri, null);
        return modified;
	}

}
