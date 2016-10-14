/*
 * Copyright 2016 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jongla.realmbootstrap;

import android.app.Application;
import android.util.Log;

import io.realm.Realm;
import io.realm.log.AndroidLogger;
import io.realm.log.RealmLog;

public class RealmJonglaBootstrapApplication extends Application {

    public static final String AUTH_URL = "http://" + BuildConfig.OBJECT_SERVER_IP + ":9080/auth";
    public static final String USER_URL = "realm://" + BuildConfig.OBJECT_SERVER_IP + ":9080/~/user";
    public static final String makeGroupUrl(String groupId) {
        return "realm://" + BuildConfig.OBJECT_SERVER_IP + ":9080/"+groupId;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
        RealmLog.add(new AndroidLogger(Log.VERBOSE));
    }
}
