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

    public static class ReportEntry implements BaseColumns {
        public static final String TABLE_NAME = "reports";

        public static final String COLUMN_ID = "id";
        public static final String COLUMN_INFORM_ID = "inform_id"; // this report belongs to ...
        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_ASPECT = "aspect";
        public static final String COLUMN_POTENTIAL = "potential";
        public static final String COLUMN_STATE = "state";
        public static final String COLUMN_IMG = "image";
        public static final String COLUMN_IMG_ACTION = "image_action";
        public static final String COLUMN_PLANNED_DATE = "planned_date";
        public static final String COLUMN_DEADLINE = "deadline";
        public static final String COLUMN_INSPECTIONS = "inspections";
        public static final String COLUMN_DESCRIP = "description";
        public static final String COLUMN_ACTIONS = "actions";
        public static final String COLUMN_OBS = "observations";
        public static final String COLUMN_CREATED_AT = "created_at";
        public static final String COLUMN_WORK_FRONT_NAME = "work_front_name";
        public static final String COLUMN_AREA_NAME = "area_name";
        public static final String COLUMN_RESPONSIBLE = "responsible_name";
        public static final String COLUMN_CRIT_RISKS = "critical_risks_name";
    }
}