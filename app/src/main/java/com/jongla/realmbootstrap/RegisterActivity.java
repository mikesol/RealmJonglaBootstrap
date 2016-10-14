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

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jongla.realmbootstrap.model.Group;
import com.jongla.realmbootstrap.model.GroupInfo;
import com.jongla.realmbootstrap.model.GroupMember;
import com.jongla.realmbootstrap.model.MyInfo;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Credentials;
import io.realm.ObjectServerError;
import io.realm.Realm;
import io.realm.SyncConfiguration;
import io.realm.User;

import static com.jongla.realmbootstrap.RealmJonglaBootstrapApplication.AUTH_URL;

public class RegisterActivity extends AppCompatActivity {

    static private final int nUsers = 100;
    static private final int usersPerGroup = 10;

    @BindView(R.id.register_progress)
    ProgressBar progressBar;
    @BindView(R.id.message)
    TextView successMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);
        doLoginAndMaybeRegistration(0, false);
    }

    private void reportResult(final String result) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                successMessage.setText(result);
                progressBar.setVisibility(View.GONE);
                successMessage.setVisibility(View.VISIBLE);

            }
        });
    }
    
    private void makeDummyData(User user, int i) {
        SyncConfiguration userConfig = new SyncConfiguration.Builder(user, RealmJonglaBootstrapApplication.USER_URL).build();
        Realm userRealm = Realm.getInstance(userConfig);
        final MyInfo myInfo = new MyInfo();
        myInfo.setId(0);
        myInfo.setName("User "+i);
        myInfo.setAvatarURL("https://api.adorable.io/avatars/285/user"+i+".png");
        userRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealmOrUpdate(myInfo);
            }
        });
        for (int j = Math.max(i - usersPerGroup, 0); j < Math.min(i + 1, nUsers - usersPerGroup); j++) {
            final Group group = new Group();
            String id = "group"+j;
            group.setId(id);
            group.setName("Group "+j);
            group.setAvatarURL("https://api.adorable.io/avatars/285/group"+j+".png");
            userRealm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.copyToRealmOrUpdate(group);
                }
            });
            SyncConfiguration groupConfig = new SyncConfiguration.Builder(user, RealmJonglaBootstrapApplication.makeGroupUrl(id)).build();
            Realm groupRealm = Realm.getInstance(groupConfig);
            final GroupInfo groupInfo = new GroupInfo();
            groupInfo.setId(group.getId());
            groupInfo.setName(group.getName());
            groupInfo.setAvatarURL(group.getAvatarURL());
            final GroupMember groupMember = new GroupMember();
            groupMember.setId(user.getIdentity());
            groupMember.setName(myInfo.getName());
            groupMember.setAvatarURL(myInfo.getAvatarURL());
            groupRealm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.copyToRealmOrUpdate(groupInfo);
                    realm.copyToRealmOrUpdate(groupMember);
                }
            });
            groupRealm.close();
        }
        userRealm.close();
    }

    Map<String, String> usernameToUid = new HashMap<>();

    private void doLoginAndMaybeRegistration(final int i, final boolean register) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                User.loginAsync(Credentials.usernamePassword("user"+i, "user"+i, register),
                        AUTH_URL, new User.Callback() {
                    @Override
                    public void onSuccess(User user) {
                        if (i < nUsers) {
                            usernameToUid.put("user"+i, user.getIdentity());
                            makeDummyData(user, i);
                            user.logout();
                            doLoginAndMaybeRegistration(i+1, false);
                        } else {
                            reportResult("Success!");
                        }
                    }

                    @Override
                    public void onError(ObjectServerError error) {
                        if (!register) {
                            doLoginAndMaybeRegistration(i, true);
                        } else {
                            reportResult("Failure at user" + i+" "+error.getErrorMessage());
                        }
                    }
                });
            }
        });

    }

}
