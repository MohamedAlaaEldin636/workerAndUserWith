package com.mohamed.mario.worker.utils;

public class DatabaseUtils {
    public static final String USERES_DATABASE_NAME="users";
    public static final String USERES_DATABASE_QUERY_PHONE="phone";
    public static final String USERES_DATABASE_PHOTO_LOCATION="users/Images/";
    public static final String USERES_DATABASE_PHOTO_NAME="/image";

    //Worker Section
    public static final String WORKER_DATABASE_NAME="worker";
    public static final String WORKERES_DATABASE_PHOTO_LOCATION="workeres/Images/";
    public static final String WORKERES_DATABASE_PHOTO_NAME="/image";


    //Location
    public static final String USERES_DATABASE_LOCATION_USER_PATH="location/users";
    public static final String WORKERS_DATABASE_LOCATION_WORKER_PATH = "location/workeres";

    //Radious
    public static final double RADIOUS_KILLOSMETERS=7;

    public static String getWorkerPrevWorkImageNameByPosition(int position){
        return "/" + "prev_work_image_" + position;
    }

}
