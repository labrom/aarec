package labrom.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.provider.BaseColumns;

public abstract class ActiveRecord implements Cloneable {
    
    private Database db;
    private long id = -1;

    final void attach(Database db, long id) {
        this.db = db;
        this.id = id;
    }
    
    public final void detach() {
        this.db = null;
        this.id = -1;
    }
    
    /**
     * 
     * @param db May be null, in that case don't allow for updates.
     * @param c
     */
    final void hydrateFromCursor(Database db, Cursor c) {
        this.db = db;
        if(this.id < 0) {
            int idColdex = c.getColumnIndex(BaseColumns._ID);
            if(idColdex >= 0)
                this.id = c.getLong(idColdex);
            else
                this.id = c.getPosition();
        }
        hydrateFromCursor(c);
    }

    protected abstract void hydrateFromCursor(Cursor c);
    protected abstract void hydrateFromContentValues(ContentValues v);
    protected abstract void populateForUpdate(ContentValues v);
    protected abstract void populateFull(ContentValues v);
    protected abstract void populateForQueryByExample(SelectionArgs selection);
    public abstract String getTableName();
    
    public final ContentValues getInsertContentValues() {
    	ContentValues vals = new ContentValues();
    	populateFull(vals);
    	return vals;
    }
    
    public final ContentValues getUpdateContentValues() {
        ContentValues vals = new ContentValues();
        populateForUpdate(vals);
        return vals;
    }
    
    public long getId() {
        return id;
    }

    public final void update() {
        if(this.id < 0)
            throw new AssertionError("Object was not persisted before being updated");
        if(this.db == null)
            throw new AssertionError("Database object is null, updates are not possible");
        this.db.update(this);
    }
    
    public final void delete() {
        if(this.db == null)
            throw new AssertionError("Database object is null, deletes are not possible");
        this.db.delete(this);
    }

    ActiveRecord copy() {
        try {
            return (ActiveRecord)super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
    
    
    protected static int nowSeconds() {
        return (int)(System.currentTimeMillis() / 1000);
    }
    
    protected static long now() {
    	return System.currentTimeMillis();
    }

}
