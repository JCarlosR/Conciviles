package com.programacionymas.conciviles.io.sqlite;

import android.provider.BaseColumns;

public final class MyDbContract {
    // To prevent someone from accidentally instantiating the contract class
    private MyDbContract() {}


    public static class AreaEntry implements BaseColumns {
        public static final String TABLE_NAME = "areas";

        public static final String COLUMN_ID = "id";
        public static final String COLUMN_NAME = "name";
    }

    public static class InformEntry implements BaseColumns {
        public static final String TABLE_NAME = "informs";

        public static final String COLUMN_ID = "id";
        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_FROM_DATE = "from_date_format";
        public static final String COLUMN_TO_DATE = "to_date_format";
        public static final String COLUMN_CREATED_AT = "created_at";
        public static final String COLUMN_USER_NAME = "user_name";
        public static final String COLUMN_IS_EDITABLE = "is_editable";
    }
}