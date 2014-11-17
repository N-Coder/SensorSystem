/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Niko Fink
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package de.ncoder.sensorsystem.android.app.componentInfo;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import de.ncoder.sensorsystem.android.app.R;
import de.ncoder.sensorsystem.android.app.data.UserManager;
import de.ncoder.sensorsystem.events.EventListener;
import de.ncoder.sensorsystem.events.EventManager;
import de.ncoder.sensorsystem.events.event.Event;
import de.ncoder.sensorsystem.events.event.FutureDoneEvent;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class UserInfoActivity extends ComponentInfoActivity implements EventListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_component_user);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        super.onServiceConnected(name, service);
        updateUI();

        EventManager eventManager = getComponent(EventManager.KEY);
        if (eventManager != null) {
            eventManager.subscribe(this);
        } else {
            Log.w(getClass().getSimpleName(), "No EventManager available");
        }
    }

    @Override
    public void onPause() {
        EventManager eventManager = getComponent(EventManager.KEY);
        if (eventManager != null) {
            eventManager.unsubscribe(this);
        }
        super.onPause();
    }

    private void updateUI() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                UserManager userManager = getComponent(UserManager.KEY);
                if (userManager != null) {
                    boolean l = userManager.isLoggedIn();
                    findViewById(R.id.loginContainer).setVisibility(l ? View.GONE : View.VISIBLE);
                    findViewById(R.id.logoutContainer).setVisibility(l ? View.VISIBLE : View.GONE);
                    if (l) {
                        ((TextView) findViewById(R.id.userInfo)).setText("Logged in as " + userManager.getUserName());
                    } else {
                        ((TextView) findViewById(R.id.userInfo)).setText("Logged out");
                    }
                }
                if (future == null || future.isDone()) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
            }
        });
    }

    Future<UserManager> future;
    ProgressDialog dialog;

    public void doLogin(View view) {
        UserManager userManager = getComponent(UserManager.KEY);
        if (userManager != null) {
            dialog = new ProgressDialog(this);
            dialog.setTitle("Logging in");
            dialog.setIndeterminate(true);
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    if (future != null) {
                        future.cancel(true);
                    }
                }
            });
            dialog.show();

            future = userManager.login(
                    ((EditText) findViewById(R.id.userName)).getText().toString(),
                    ((EditText) findViewById(R.id.password)).getText().toString().toCharArray()
            );
        }
    }

    public void doLogout(View view) {
        UserManager userManager = getComponent(UserManager.KEY);
        if (userManager != null) {
            dialog = new ProgressDialog(this);
            dialog.setTitle("Logging out");
            dialog.setIndeterminate(true);
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    if (future != null) {
                        future.cancel(false);
                    }
                }
            });
            dialog.show();

            future = userManager.logout();
        }
    }

    @Override
    public void handle(final Event event) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (event instanceof FutureDoneEvent) {
                    FutureDoneEvent fde = (FutureDoneEvent) event;
                    if (fde.getFuture() == future) {
                        dialog.dismiss();
                        try {
                            fde.getFuture().get();
                            Log.i(UserInfoActivity.this.getClass().getSimpleName(), "Succes");
                        } catch (InterruptedException e) {
                            Log.i(UserInfoActivity.this.getClass().getSimpleName(), "Interrupted", e);
                        } catch (CancellationException e) {
                            Log.i(UserInfoActivity.this.getClass().getSimpleName(), "Cancelled", e);
                        } catch (ExecutionException e) {
                            Log.i(UserInfoActivity.this.getClass().getSimpleName(), "Failed", e);
                            if (e.getCause() instanceof LoginException) {
                                ((TextView) findViewById(R.id.userName)).setError(e.getCause().getLocalizedMessage());
                            } else if (e.getCause() instanceof IOException) {
                                Toast.makeText(UserInfoActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                } else if (event instanceof UserManager.UserStatusChangedEvent) {
                    updateUI();
                    if (((UserManager.UserStatusChangedEvent) event).isLoggedIn()) {
                        finish();
                    } else {
                        Toast.makeText(UserInfoActivity.this, "Logged out", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}
